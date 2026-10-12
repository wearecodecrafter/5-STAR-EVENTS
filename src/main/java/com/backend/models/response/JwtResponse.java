package com.backend.models.response;

import com.backend.models.requests.RoleDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class JwtResponse {
    private UUID userId;
    private String username;
    private String name;
    private String email;
    private String contact;

    private String jwtToken;
    private String fcmToken;
    private RoleDto role;
    private String imageUrl;
    private Boolean isLoggedIn;
    private Boolean isMultiFactor;
    private Boolean isActive;
    private String position;
    private String employeeCode;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}