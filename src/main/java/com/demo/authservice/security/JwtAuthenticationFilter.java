package com.demo.authservice.security;

import com.demo.authservice.constants.AuthEndpoints;
import com.demo.authservice.entity.UserEntity;
import com.demo.authservice.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Authentication filter that validates JWT tokens for incoming HTTP requests.
 *
 * <p>This filter intercepts every request once per request lifecycle and:
 * <ul>
 *   <li>Skips authentication for public endpoints (login, register, Swagger docs).</li>
 *   <li>Extracts and validates the JWT token from the "Authorization" header.</li>
 *   <li>Loads the user from the database and verifies token validity.</li>
 *   <li>Builds a Spring Security authentication object using user roles.</li>
 * </ul>
 *
 * <p>If the token is missing, invalid, or expired, the filter returns a
 * {@code 401 Unauthorized} response with a JSON error message.</p>
 *
 * <p>On successful validation, the authenticated user's details and authorities
 * are stored in the {@link SecurityContextHolder} for downstream processing.</p>
 */

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Skip public endpoints
        String path = request.getServletPath();

        // Skip public paths
        if (path.equals(AuthEndpoints.REGISTER)
                || path.equals(AuthEndpoints.LOGIN)
                || path.equals(AuthEndpoints.SWAGGER_UI_HTML)
                || path.startsWith(AuthEndpoints.SWAGGER_UI)
                || path.startsWith(AuthEndpoints.API_DOCS)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(AuthEndpoints.CONTENT_TYPE);
            response.getWriter().write("{\"error\": \"Authorization header with Bearer token is required\"}");
            return;
        }

        final String jwt = authHeader.substring(7);
        final String email;

        try {
            email = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(AuthEndpoints.CONTENT_TYPE);
            response.getWriter().write("{\"error\": \"JWT token is invalid or expired\"}");
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserEntity userEntity = userRepository.findByEmail(email)
                    .orElse(null);

            if (userEntity == null || !jwtService.isTokenValid(jwt, userEntity)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(AuthEndpoints.CONTENT_TYPE);
                response.getWriter().write("{\"error\": \"JWT token is invalid or expired\"}");
                return;
            }

            // Map DB roles to Spring Security authorities with ROLE_ prefix
            List<SimpleGrantedAuthority> authorities = userEntity.getRoles().stream()
                    .map(r -> new SimpleGrantedAuthority(
                            r.getName().startsWith("ROLE_") ? r.getName() : "ROLE_" + r.getName()))
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userEntity.getEmail(), null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }

}