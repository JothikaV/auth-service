package com.demo.authservice.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Implementation of {@link AuditorAware} that provides the current auditor (username)
 * for Spring Data JPA auditing.
 *
 * <p>This class retrieves the authenticated user's username from the Spring Security
 * context. It is used by JPA to automatically populate audit fields such as
 * {@code createdBy} and {@code updatedBy}.</p>
 *
 * <p>Behavior:</p>
 * <ul>
 *   <li>If a user is authenticated, their username is returned.</li>
 *   <li>If no authentication exists, or the request is anonymous, it returns
 *       {@code "system"} as a fallback auditor.</li>
 * </ul>
 *
 * <p>This ensures audit fields are always populated, even for background jobs,
 * initial data loads, or unauthenticated operations.</p>
 */

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null ||
                !auth.isAuthenticated() ||
                auth instanceof AnonymousAuthenticationToken) {
            return Optional.of("system"); // fallback
        }


        return Optional.ofNullable(auth.getName());
    }
}
