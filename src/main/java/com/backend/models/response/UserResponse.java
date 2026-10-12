package com.backend.models.response;

import com.backend.models.requests.RoleDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private String username;
    private String password;
    private String contact;
    private String imageUrl;
    private Boolean isMultiFactor;
    private String secretKey;
    private String qrCodeImage;
    private Boolean isUserLoggedIn;
    private Boolean isActive;
    private String position;
    private String employeeCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private RoleDto roleDto;

}
