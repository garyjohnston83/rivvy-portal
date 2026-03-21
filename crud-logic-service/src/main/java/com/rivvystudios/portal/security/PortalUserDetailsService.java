package com.rivvystudios.portal.security;

import com.rivvystudios.portal.model.OrgRoleAssignment;
import com.rivvystudios.portal.model.OrganizationMember;
import com.rivvystudios.portal.model.UserAccount;
import com.rivvystudios.portal.model.enums.UserAccountStatus;
import com.rivvystudios.portal.repository.OrgRoleAssignmentRepository;
import com.rivvystudios.portal.repository.OrganizationMemberRepository;
import com.rivvystudios.portal.repository.UserAccountRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PortalUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrgRoleAssignmentRepository orgRoleAssignmentRepository;

    public PortalUserDetailsService(UserAccountRepository userAccountRepository,
                                    OrganizationMemberRepository organizationMemberRepository,
                                    OrgRoleAssignmentRepository orgRoleAssignmentRepository) {
        this.userAccountRepository = userAccountRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.orgRoleAssignmentRepository = orgRoleAssignmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        boolean enabled = userAccount.getStatus() == UserAccountStatus.ACTIVE;

        // Traverse role path: UserAccount -> OrganizationMember -> OrgRoleAssignment -> Role
        List<OrganizationMember> memberships = organizationMemberRepository.findByUserAccount(userAccount);
        List<OrgRoleAssignment> roleAssignments = memberships.isEmpty()
                ? List.of()
                : orgRoleAssignmentRepository.findByOrganizationMemberIn(memberships);

        List<SimpleGrantedAuthority> authorities = roleAssignments.stream()
                .map(assignment -> new SimpleGrantedAuthority("ROLE_" + assignment.getRole().getCode()))
                .distinct()
                .collect(Collectors.toList());

        return new User(
                userAccount.getEmail(),
                userAccount.getPasswordHash() != null ? userAccount.getPasswordHash() : "",
                enabled,
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities
        );
    }
}
