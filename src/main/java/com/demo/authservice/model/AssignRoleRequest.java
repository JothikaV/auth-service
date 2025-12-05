package com.demo.authservice.model;


import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class AssignRoleRequest {
    private Set<String> roleNames;
}
