package com.rivvystudios.portal.config;

import com.rivvystudios.portal.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class LockoutPropertiesTests {

    @Autowired
    private LockoutProperties lockoutProperties;

    @Test
    void lockoutPropertiesAreLoadedWithDefaults() {
        assertThat(lockoutProperties).isNotNull();
        assertThat(lockoutProperties.getThreshold()).isEqualTo(5);
        assertThat(lockoutProperties.getWindowMinutes()).isEqualTo(15);
        assertThat(lockoutProperties.getDurationMinutes()).isEqualTo(30);
    }

    @Test
    void lockoutThresholdIsPositive() {
        assertThat(lockoutProperties.getThreshold()).isPositive();
    }

    @Test
    void lockoutWindowMinutesIsPositive() {
        assertThat(lockoutProperties.getWindowMinutes()).isPositive();
    }
}
