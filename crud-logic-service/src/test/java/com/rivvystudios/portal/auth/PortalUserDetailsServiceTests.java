package com.rivvystudios.portal.auth;

import com.rivvystudios.portal.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class PortalUserDetailsServiceTests {

    @Autowired
    private UserDetailsService userDetailsService;

    @Test
    void loadUserByUsernameReturnsValidUserDetailsForActiveUser() {
        // admin@rivvy.local is ACTIVE with RIVVY_ADMIN role from seed data
        UserDetails details = userDetailsService.loadUserByUsername("admin@rivvy.local");
        assertThat(details.getUsername()).isEqualTo("admin@rivvy.local");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities()).extracting("authority")
                .contains("ROLE_RIVVY_ADMIN");
    }

    @Test
    void loadUserByUsernameThrowsForUnknownEmail() {
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown@test.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsernameResolvesRoleAuthorities() {
        // producer@rivvy.local should have ROLE_RIVVY_PRODUCER
        UserDetails details = userDetailsService.loadUserByUsername("producer@rivvy.local");
        assertThat(details.getAuthorities()).extracting("authority")
                .contains("ROLE_RIVVY_PRODUCER");

        // client@rivvy.local should have ROLE_CLIENT
        UserDetails clientDetails = userDetailsService.loadUserByUsername("client@rivvy.local");
        assertThat(clientDetails.getAuthorities()).extracting("authority")
                .contains("ROLE_CLIENT");
    }
}
