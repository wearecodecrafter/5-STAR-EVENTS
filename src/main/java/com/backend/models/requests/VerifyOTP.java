package com.backend.models.requests;

import lombok.Data;

@Data
public class VerifyOTP {
    private String username;
    private int otp;
}
