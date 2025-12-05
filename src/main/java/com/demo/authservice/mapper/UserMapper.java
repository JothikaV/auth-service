package com.demo.authservice.mapper;


import com.demo.authservice.model.UserRegisterRequest;
import com.demo.authservice.model.UserResponse;
import com.demo.authservice.entity.RoleEntity;
import com.demo.authservice.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles")
    UserResponse toDto(UserEntity userEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    UserEntity toEntity(UserRegisterRequest request);

    default Set<String> mapRoles(Set<RoleEntity> roleEntities) {
        return roleEntities.stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());
    }
}