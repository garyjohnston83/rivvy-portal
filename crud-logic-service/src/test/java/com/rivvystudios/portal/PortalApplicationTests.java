package com.rivvystudios.portal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class PortalApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @LocalServerPort
    private int port;

    @Test
    void contextLoads() {
        // Test that Spring application context loads successfully
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void applicationStartsWithoutErrors() {
        // Test that application starts without errors by verifying main bean exists
        assertThat(applicationContext.getBean(PortalApplication.class)).isNotNull();
    }

    @Test
    void webServerStartsOnConfiguredPort() {
        // Test that web server starts on a valid port
        assertThat(port).isGreaterThan(0);
    }
}
