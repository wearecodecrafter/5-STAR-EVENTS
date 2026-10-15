package com.backend.models.requests.password;

import lombok.Data;
import lombok.ToString;

@Data
public class ResetPasswordRequest {

    private String email;
    private String otp;

    @ToString.Exclude
    private String newPassword;
}
