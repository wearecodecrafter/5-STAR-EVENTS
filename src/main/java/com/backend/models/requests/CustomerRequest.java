package com.backend.models.requests;

import lombok.Data;

import java.util.UUID;

@Data
public class CustomerRequest {
    private UUID id;

    private String fullName;
    private String phoneNumber;
    private String email;
    private String password;
}
