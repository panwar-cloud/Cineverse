package com.assignment.authservice.dto;

import com.assignment.authservice.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private long expiresIn;
    private Long id;
    private String name;
    private String email;
    private Role role;
}
