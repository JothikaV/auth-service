package com.demo.authservice.controller;

import com.demo.authservice.model.AssignRoleRequest;
import com.demo.authservice.model.UserResponse;
import com.demo.authservice.model.UserLoginRequest;
import com.demo.authservice.model.UserRegisterRequest;

import com.demo.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
@RequestMapping("/users")
@Tag(name = "UserEntity Management", description = "Endpoints for user management")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Registers a new user with username, email, and password"
    )
    public ResponseEntity<UserResponse> register(@RequestBody @Valid UserRegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping("/login")
    @Operation(
            summary = "UserEntity login",
            description = "Authenticate user and return JWT token"
    )
    public ResponseEntity<String> login(@RequestBody @Valid UserLoginRequest request) {
        String token = userService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body(token);
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current user info",
            description = "Returns information about the currently authenticated user"
    )
    public ResponseEntity<UserResponse> getCurrentUser(
            @RequestHeader(value = "Authorization") String authHeader) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserResponse user = userService.getCurrentUser(email);
        return ResponseEntity.ok(user);
    }


    @PostMapping("/{userId}/roles")
    @Operation(
            summary = "Assign roles to a user",
            description = "Assign one or more roles to a specific user by ID"
    )
    public ResponseEntity<String> assignRole(
            @RequestHeader(value = "Authorization") String authHeader,
            @PathVariable Long userId,
            @RequestBody AssignRoleRequest request
    ) {
        userService.assignRoles(userId, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Role(s) assigned successfully");
    }
}
