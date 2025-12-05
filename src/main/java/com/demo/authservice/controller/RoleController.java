package com.demo.authservice.controller;

import com.demo.authservice.model.AdminStatsResponse;
import com.demo.authservice.model.AssignRoleRequest;
import com.demo.authservice.service.RoleService;
import com.demo.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;


@RestController
@RequestMapping("/roles")
@Tag(name = "RoleEntity Management", description = "Endpoints for role management")
public class RoleController {

    private final RoleService roleService;

    private final UserService userService;

    public RoleController(RoleService roleService, UserService userService) {
        this.roleService = roleService;
        this.userService = userService;
    }

    @PostMapping("")
    @Operation(
            summary = "Create roles",
            description = "Create one or more roles in the system"
    )
    public ResponseEntity<List<String>> createRoles(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestBody AssignRoleRequest request) {

        List<String> savedRoles = roleService.createRoles(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRoles);
    }

    @GetMapping("/admin/stats")
    @Operation(
            summary = "Get user statistics",
            description = "Retrieve administrative statistics about users and roles"
    )
    public ResponseEntity<AdminStatsResponse> getAdminStats(@RequestHeader(value = "Authorization") String authHeader) {
        AdminStatsResponse stats = roleService.getAdminStats();
        return ResponseEntity.ok(stats);
    }


}