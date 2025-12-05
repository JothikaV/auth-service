package com.demo.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration class that enables Spring Data JPA auditing and registers
 * the {@link AuditorAware} implementation used for automatically populating
 * audit-related fields such as {@code createdBy} and {@code lastModifiedBy}.
 *
 * <p>The {@code @EnableJpaAuditing} annotation activates auditing support,
 * and the {@code auditorAwareRef} attribute points to the bean that determines
 * the current auditor (typically the authenticated username).</p>
 *
 * <p>This configuration allows entities annotated with
 * {@code @CreatedBy}, {@code @LastModifiedBy}, {@code @CreatedDate},
 * and {@code @LastModifiedDate} to be automatically populated by Spring.</p>
 */

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return new AuditorAwareImpl();
    }
}
