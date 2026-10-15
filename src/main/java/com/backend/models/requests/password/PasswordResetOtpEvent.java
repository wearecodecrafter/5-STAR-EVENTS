package com.backend.models.requests.password;

public record PasswordResetOtpEvent(String email, String name, String otp, int validMinutes) {}