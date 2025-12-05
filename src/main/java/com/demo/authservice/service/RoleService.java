package com.demo.authservice.service;


import com.demo.authservice.entity.RoleEntity;
import com.demo.authservice.exception.InvalidRequestException;
import com.demo.authservice.exception.ResourceAlreadyExistsException;
import com.demo.authservice.mapper.RoleMapper;
import com.demo.authservice.model.AdminStatsResponse;
import com.demo.authservice.model.AssignRoleRequest;
import com.demo.authservice.repository.RoleRepository;
import com.demo.authservice.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.HashMap;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    private final UserRepository userRepository;

    private final RoleMapper roleMapper;

    public RoleService(RoleRepository roleRepo, UserRepository userRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepo;
        this.userRepository = userRepository;
        this.roleMapper = roleMapper;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<String> createRoles(AssignRoleRequest request) {

        Set<String> roleNames = request.getRoleNames();
        if (roleNames == null || roleNames.isEmpty()) {
            throw new InvalidRequestException("roleNames cannot be empty");
        }

        List<String> savedRoles = new ArrayList<>();

        for (String roleName : roleNames) {

            if (roleRepository.existsByName(roleName)) {
                throw new ResourceAlreadyExistsException("Role already exists: " + roleName);
            }

            RoleEntity roleEntity = roleMapper.toEntity(roleName);
            RoleEntity savedRole = roleRepository.save(roleEntity);

            // Add the saved role name to the response list
            savedRoles.add(savedRole.getName());
        }

        return savedRoles;
    }


    @PreAuthorize("hasRole('ADMIN')")
    public AdminStatsResponse getAdminStats() {

        long totalUsers = userRepository.count();

        Map<String, ZonedDateTime> lastLoginTimes = new HashMap<>();
        userRepository.findAll().forEach(user -> {
            ZonedDateTime lastLogin = user.getLastLogin() != null
                    ? user.getLastLogin()
                    : ZonedDateTime.now(ZoneOffset.UTC);
            lastLoginTimes.put(user.getEmail(), lastLogin);
        });

        AdminStatsResponse response = new AdminStatsResponse();
        response.setTotalUsers(totalUsers);
        response.setLastLoginTimes(lastLoginTimes);

        return response;
    }

}
