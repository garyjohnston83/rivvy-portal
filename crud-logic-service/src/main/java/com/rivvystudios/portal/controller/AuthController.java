package com.rivvystudios.portal.controller;

import com.rivvystudios.portal.config.LockoutProperties;
import com.rivvystudios.portal.controller.dto.LoginRequest;
import com.rivvystudios.portal.controller.dto.LoginResponse;
import com.rivvystudios.portal.controller.dto.UserInfoResponse;
import com.rivvystudios.portal.model.UserAccount;
import com.rivvystudios.portal.model.enums.UserAccountStatus;
import com.rivvystudios.portal.repository.UserAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final int REMEMBER_ME_SECONDS = 180 * 24 * 60 * 60; // 180 days
    private static final int DEFAULT_TIMEOUT_SECONDS = 1800; // 30 minutes
    private static final String DUMMY_HASH = "$2a$10$dummyHashValueForTimingAttackPrevention1234567890";

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final LockoutProperties lockoutProperties;
    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();

    public AuthController(AuthenticationManager authenticationManager,
                          UserAccountRepository userAccountRepository,
                          PasswordEncoder passwordEncoder,
                          LockoutProperties lockoutProperties) {
        this.authenticationManager = authenticationManager;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.lockoutProperties = lockoutProperties;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest,
                                   HttpServletRequest request) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        // Check if user exists
        Optional<UserAccount> userAccountOpt = userAccountRepository.findByEmail(email);

        if (userAccountOpt.isEmpty()) {
            // Unknown email: perform dummy BCrypt operation for timing consistency
            passwordEncoder.matches(password, DUMMY_HASH);
            logger.warn("Failed login attempt for user {} - reason: unknown_email", email);
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid email or password"));
        }

        UserAccount userAccount = userAccountOpt.get();
        Instant now = Instant.now();

        // Check if account is locked
        if (userAccount.getLockedUntil() != null) {
            if (userAccount.getLockedUntil().isAfter(now)) {
                // Account is still locked
                logger.warn("Failed login attempt for user {} - reason: account_locked", userAccount.getId());
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Invalid email or password"));
            } else {
                // Lockout has expired - clear lockout fields
                logger.info("Account lockout expired for user {} at {}", userAccount.getId(), userAccount.getLockedUntil());
                clearLockoutFields(userAccount);
                userAccountRepository.save(userAccount);
            }
        }

        // Check for PENDING status (invited users)
        if (userAccount.getStatus() == UserAccountStatus.PENDING) {
            logger.warn("Failed login attempt for user {} - reason: invited_account", userAccount.getId());
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid email or password"));
        }

        // Check for INACTIVE/SUSPENDED status (disabled users)
        if (userAccount.getStatus() == UserAccountStatus.INACTIVE ||
            userAccount.getStatus() == UserAccountStatus.SUSPENDED) {
            // Check if password is correct
            if (passwordEncoder.matches(password, userAccount.getPasswordHash())) {
                // Correct password - reveal disabled message
                logger.warn("Failed login attempt for user {} - reason: disabled_account (correct password)", userAccount.getId());
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Your account has been disabled. Please contact support."));
            } else {
                // Incorrect password - generic error
                logger.warn("Failed login attempt for user {} - reason: disabled_account (incorrect password)", userAccount.getId());
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Invalid email or password"));
            }
        }

        // At this point, user is ACTIVE and not locked - attempt authentication
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // Authentication successful - clear lockout counters and update last login
            if (userAccount.getFailedAttemptsCount() > 0) {
                logger.info("Successful login for user {} - lockout counters reset", userAccount.getId());
            }
            clearLockoutFields(userAccount);
            userAccount.setLastLoginAt(now);
            userAccountRepository.save(userAccount);

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
                    userAccount.getEmail(),
                    userAccount.getFirstName(),
                    userAccount.getLastName(),
                    roles
            );

            return ResponseEntity.ok(response);

        } catch (AuthenticationException ex) {
            // Authentication failed - handle failed attempt tracking
            handleFailedAttempt(userAccount, now);
            logger.warn("Failed login attempt for user {} - reason: wrong_password", userAccount.getId());
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid email or password"));
        }
    }

    private void handleFailedAttempt(UserAccount userAccount, Instant now) {
        int windowMinutes = lockoutProperties.getWindowMinutes();
        Instant windowStart = now.minusSeconds(windowMinutes * 60L);

        // Check if first failed attempt is outside the window
        if (userAccount.getFirstFailedAttemptAt() == null ||
            userAccount.getFirstFailedAttemptAt().isBefore(windowStart)) {
            // Reset window
            userAccount.setFirstFailedAttemptAt(now);
            userAccount.setFailedAttemptsCount(1);
        } else {
            // Increment within window
            userAccount.setFailedAttemptsCount(userAccount.getFailedAttemptsCount() + 1);
        }

        userAccount.setLastFailedAttemptAt(now);

        // Check if threshold reached
        if (userAccount.getFailedAttemptsCount() >= lockoutProperties.getThreshold()) {
            int durationMinutes = lockoutProperties.getDurationMinutes();
            Instant lockedUntil = now.plusSeconds(durationMinutes * 60L);
            userAccount.setLockedUntil(lockedUntil);
            logger.warn("Account locked for user {} - threshold {} reached", userAccount.getId(), lockoutProperties.getThreshold());
        }

        userAccountRepository.save(userAccount);
    }

    private void clearLockoutFields(UserAccount userAccount) {
        userAccount.setFailedAttemptsCount(0);
        userAccount.setFirstFailedAttemptAt(null);
        userAccount.setLastFailedAttemptAt(null);
        userAccount.setLockedUntil(null);
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
