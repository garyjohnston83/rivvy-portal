package com.rivvystudios.portal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ApplicationConfigurationTests {

    @Autowired
    private Environment environment;

    @Test
    void serverPortIsConfiguredCorrectly() {
        // Test that server.port is configured to 9090
        String serverPort = environment.getProperty("server.port");
        assertThat(serverPort).isEqualTo("9090");
    }

    @Test
    void actuatorHealthEndpointIsExposed() {
        // Test that actuator health endpoint is exposed
        String exposedEndpoints = environment.getProperty("management.endpoints.web.exposure.include");
        assertThat(exposedEndpoints).isEqualTo("health");
    }
}
