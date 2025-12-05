package com.demo.authservice.mapper;

import com.demo.authservice.entity.RoleEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    default RoleEntity toEntity(String roleName) {
        return RoleEntity.builder()
                .name(roleName)
                .build();
    }

}

