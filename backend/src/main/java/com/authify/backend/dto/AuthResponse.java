package com.authify.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String message;
    private String email;
    private String firstName;
    private String lastName;
    private Set<String> roles;
    private boolean tfaEnabled;
    private boolean tfaRequired;
}
