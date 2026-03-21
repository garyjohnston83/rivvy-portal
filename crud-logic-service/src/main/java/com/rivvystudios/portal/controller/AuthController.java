package com.rivvystudios.portal.controller;

import com.rivvystudios.portal.controller.dto.LoginRequest;
import com.rivvystudios.portal.controller.dto.LoginResponse;
import com.rivvystudios.portal.controller.dto.UserInfoResponse;
import com.rivvystudios.portal.model.UserAccount;
import com.rivvystudios.portal.repository.UserAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final int REMEMBER_ME_SECONDS = 180 * 24 * 60 * 60; // 180 days
    private static final int DEFAULT_TIMEOUT_SECONDS = 1800; // 30 minutes

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository userAccountRepository;
    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();

    public AuthController(AuthenticationManager authenticationManager,
                          UserAccountRepository userAccountRepository) {
        this.authenticationManager = authenticationManager;
        this.userAccountRepository = userAccountRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest,
                                   HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Create SecurityContext and store in session
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            HttpSession session = request.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    securityContext
            );

            // Configure session timeout based on rememberMe
            if (loginRequest.isRememberMe()) {
                session.setMaxInactiveInterval(REMEMBER_ME_SECONDS);
            } else {
                session.setMaxInactiveInterval(DEFAULT_TIMEOUT_SECONDS);
            }

            // Update last_login_at
            UserAccount userAccount = userAccountRepository.findByEmail(loginRequest.getEmail())
                    .orElse(null);
            if (userAccount != null) {
                userAccount.setLastLoginAt(Instant.now());
                userAccountRepository.save(userAccount);
            }

            // Compute redirect URL
            String redirectUrl = computeRedirectUrl(request, authentication);

            // Build role code list (strip ROLE_ prefix)
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(a -> a.startsWith("ROLE_"))
                    .map(a -> a.substring(5))
                    .collect(Collectors.toList());

            LoginResponse response = new LoginResponse(
                    redirectUrl,
                    userAccount != null ? userAccount.getEmail() : loginRequest.getEmail(),
                    userAccount != null ? userAccount.getFirstName() : null,
                    userAccount != null ? userAccount.getLastName() : null,
                    roles
            );

            return ResponseEntity.ok(response);

        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid email or password"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized"));
        }

        String email = authentication.getName();
        UserAccount userAccount = userAccountRepository.findByEmail(email).orElse(null);

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .collect(Collectors.toList());

        UserInfoResponse response = new UserInfoResponse(
                email,
                userAccount != null ? userAccount.getFirstName() : null,
                userAccount != null ? userAccount.getLastName() : null,
                roles
        );

        return ResponseEntity.ok(response);
    }

    private String computeRedirectUrl(HttpServletRequest request, Authentication authentication) {
        // Check for saved request (deep-link return)
        SavedRequest savedRequest = requestCache.getRequest(request, null);
        if (savedRequest != null) {
            String savedUrl = savedRequest.getRedirectUrl();
            // Extract the path from the full URL
            try {
                java.net.URI uri = java.net.URI.create(savedUrl);
                String path = uri.getPath();
                if (path != null && path.startsWith("/")) {
                    requestCache.removeRequest(request, null);
                    return path;
                }
            } catch (Exception ignored) {
                // Fall through to role-based routing
            }
        }

        // Role-based routing
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Set<String> roleSet = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (roleSet.contains("ROLE_RIVVY_ADMIN")) {
            return "/admin";
        }
        if (roleSet.contains("ROLE_RIVVY_PRODUCER") || roleSet.contains("ROLE_CLIENT")) {
            return "/dashboard";
        }

        // Default fallback
        return "/dashboard";
    }
}
