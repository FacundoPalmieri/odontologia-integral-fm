package com.odontologiaintegralfm.feature.consultation.catalogs.prestation.repository;

import com.odontologiaintegralfm.feature.user.model.UserSec;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@TestConfiguration
@EnableJpaAuditing(auditorAwareRef = "testAuditorProvider")
class TestJpaAuditingConfig {

    @Bean("testAuditorProvider")
    AuditorAware<UserSec> testAuditorProvider() {
        return Optional::empty;
    }
}
