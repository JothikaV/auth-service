package com.demo.authservice;

import com.demo.authservice.entity.RoleEntity;
import com.demo.authservice.repository.RoleRepository;
import com.demo.authservice.repository.UserRepository;
import com.demo.authservice.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.demo.authservice.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Integration tests for security-related endpoints and role-based access control.
 *
 * <p>Tests include:
 * <ul>
 *     <li>Accessing current user information with and without JWT</li>
 *     <li>Assigning roles to users as ADMIN</li>
 *     <li>Ensuring forbidden access for non-admin users</li>
 *     <li>Admin statistics endpoint</li>
 * </ul>
 *
 * <p>This class uses MockMvc to simulate HTTP requests and verifies proper authentication,
 * authorization, and response status codes.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuthServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    /**
     * Helper method: creates user in DB and returns generated JWT
     */
    /**
     * Helper method: creates user in DB and returns the UserEntity
     */
    private UserEntity createUserWithRole(String email, String roleName) {
        RoleEntity role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    RoleEntity newRole = new RoleEntity();
                    newRole.setName(roleName);
                    return roleRepository.save(newRole);
                });

        UserEntity user = new UserEntity();
        user.setUsername(email.split("@")[0]);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRoles(Set.of(role));

        return userRepository.save(user);
    }

    private String generateJwt(UserEntity user) {
        return jwtService.generateToken(user);
    }


    // -------------------------------------------------------------
    // TEST 1 — /users/me without token => 401
    // -------------------------------------------------------------
    @Test
    void testCurrentUserWithoutToken_shouldFail() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------
    // TEST 2 — /users/me with valid JWT => OK
    // -------------------------------------------------------------
    @Test
    void testCurrentUserWithValidJwt() throws Exception {
        UserEntity user = createUserWithRole("user@gmail.com", "ROLE_DEVELOPER");
        String jwt = generateJwt(user);

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@gmail.com"));
    }

    // -------------------------------------------------------------
    // TEST 3 — Assign roles => should FAIL for USER role (403)
    // -------------------------------------------------------------
    @Test
    void testAssignRole_WithUserRole_ShouldFail403() throws Exception {
        UserEntity user = createUserWithRole("user@gmail.com", "ROLE_USER");
        String jwt = generateJwt(user);

        mockMvc.perform(post("/users/" + user.getId() + "/roles")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                      "roleNames": ["ROLE_ADMIN"]
                                    }
                                """))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------
    // TEST 4 — Assign roles => PASS for ADMIN role
    // -------------------------------------------------------------
    @Test
    void testAssignRole_WithAdminRole_ShouldPass() throws Exception {
        // Fetch the existing admin user from the DB
        UserEntity admin = createUserWithRole("admin@system.com", "ROLE_ADMIN");

        String adminJwt = generateJwt(admin);

        // Create a normal user who will receive the new role
        UserEntity normalUser = new UserEntity();
        normalUser.setUsername("user1");
        normalUser.setEmail("user1@system.com");
        normalUser.setPassword(passwordEncoder.encode("password123"));
        normalUser.setRoles(Set.of(roleRepository.findByName("ROLE_USER").orElseThrow()));
        userRepository.save(normalUser);

        // Assign ROLE_ADMIN to the normal user
        mockMvc.perform(post("/users/" + normalUser.getId() + "/roles")
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                      "roleNames": ["ROLE_ADMIN"]
                                    }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Role(s) assigned successfully"));
    }

    // -------------------------------------------------------------
    // TEST 5 — Admin Stats => OK for ADMIN
    // -------------------------------------------------------------
    @Test
    void testAdminStats_WithAdminRole() throws Exception {
        UserEntity admin = createUserWithRole("admin@gmail.com", "ROLE_ADMIN");
        String jwt = generateJwt(admin);

        mockMvc.perform(get("/roles/admin/stats")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").exists());
    }

    // -------------------------------------------------------------
    // TEST 6 — Admin Stats => FAIL for USER (403)
    // -------------------------------------------------------------
    @Test
    void testAdminStats_WithUserRole_ShouldFail403() throws Exception {
        UserEntity user = createUserWithRole("user@gmail.com", "ROLE_USER");
        String jwt = generateJwt(user);

        mockMvc.perform(get("/roles/admin/stats")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isForbidden());
    }
}

