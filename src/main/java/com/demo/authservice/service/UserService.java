package com.demo.authservice.service;


import com.demo.authservice.entity.RoleEntity;
import com.demo.authservice.entity.UserEntity;
import com.demo.authservice.exception.EmailAlreadyExistsException;
import com.demo.authservice.exception.InvalidCredentialsException;
import com.demo.authservice.exception.ResourceNotFoundException;
import com.demo.authservice.kafka.KafkaProducerService;
import com.demo.authservice.model.AssignRoleRequest;
import com.demo.authservice.model.UserLoginRequest;
import com.demo.authservice.model.UserRegisterRequest;
import com.demo.authservice.model.UserResponse;

import com.demo.authservice.mapper.UserMapper;
import com.demo.authservice.repository.RoleRepository;
import com.demo.authservice.repository.UserRepository;
import com.demo.authservice.security.JwtService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final KafkaProducerService kafkaProducer;

    public UserService(UserRepository userRepo, RoleRepository roleRepo, PasswordEncoder passwordEncoder, JwtService jwtService, UserMapper userMapper, KafkaProducerService kafkaProducer) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.kafkaProducer = kafkaProducer;
    }

    public UserResponse register(UserRegisterRequest request) {

        if (userRepo.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email " + request.getEmail() + " already exists");
        }

        UserEntity userEntity = userMapper.toEntity(request);

        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));

        RoleEntity defaultRoleEntity = roleRepo.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role USER not found"));

        userEntity.getRoles().add(defaultRoleEntity);

        UserEntity savedUserEntity = userRepo.save(userEntity);

        // Publish Kafka event
        kafkaProducer.sendMessage("user-registration", savedUserEntity.getEmail());

        return userMapper.toDto(savedUserEntity);
    }


    public String login(UserLoginRequest request) {

        UserEntity userEntity = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), userEntity.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        userEntity.setLastLogin(ZonedDateTime.now(ZoneOffset.UTC));
        userRepo.save(userEntity);

        // Publish Kafka event
        kafkaProducer.sendMessage("user-login", userEntity.getEmail());

        return jwtService.generateToken(userEntity);
    }


    @Cacheable(value = "currentUser", key = "#email")
    public UserResponse getCurrentUser(String email) {

        System.out.println("Fetching user from DB for: " + email); // optional logging to verify caching

        UserEntity userEntity = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userMapper.toDto(userEntity);
    }


    @PreAuthorize("hasRole('ADMIN')")
    public void assignRoles(Long userId, AssignRoleRequest request) {

        UserEntity userEntity = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Set<String> roleNames = request.getRoleNames();
        // Clear all existing roles
        userEntity.getRoles().clear();

        for (String roleName : roleNames) {
            RoleEntity roleEntity = roleRepo.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

            if (!userEntity.getRoles().contains(roleEntity)) {
                userEntity.getRoles().add(roleEntity);
            }
        }

        userRepo.save(userEntity);
    }


}
