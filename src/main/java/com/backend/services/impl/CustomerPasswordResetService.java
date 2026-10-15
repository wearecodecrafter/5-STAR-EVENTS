package com.backend.services.impl;

import com.backend.exception.CustomerException;
import com.backend.models.Customer;
import com.backend.models.PasswordResetOtp;
import com.backend.models.requests.password.PasswordResetOtpEvent;
import com.backend.models.requests.password.ResetPasswordRequest;
import com.backend.repositories.CustomerRepository;
import com.backend.repositories.PasswordResetOtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerPasswordResetService {
    private static final int OTP_VALID_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_REQUESTS_PER_HOUR = 3;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CustomerRepository customerRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void requestOtp(String rawEmail) {
        String email = normalize(rawEmail);
        if (email == null || !email.contains("@")) {
            throw new CustomerException("Valid email is required.");
        }

        // Same response whether or not the email exists, so nobody can find out who is a customer
        Optional<Customer> customer = customerRepository.findByEmailIgnoreCase(email);
        if (customer.isEmpty()) return;

        long recent = otpRepository.countByEmailAndCreatedAtAfter(
                email, LocalDateTime.now().minusHours(1));
        if (recent >= MAX_REQUESTS_PER_HOUR) {
            throw new CustomerException("Too many requests. Please try again later.");
        }

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));

        PasswordResetOtp entity = new PasswordResetOtp();
        entity.setEmail(email);
        entity.setOtpHash(passwordEncoder.encode(otp));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_VALID_MINUTES));
        otpRepository.save(entity);

        // sent after the transaction commits, so a mail failure can't break the request
        eventPublisher.publishEvent(new PasswordResetOtpEvent(
                email, customer.get().getFullName(), otp, OTP_VALID_MINUTES));
    }

    // keep the wrong-attempt counter even though we throw
    @Transactional(noRollbackFor = CustomerException.class)
    public void resetPassword(ResetPasswordRequest req) {
        String email = normalize(req.getEmail());

        if (req.getNewPassword() == null || req.getNewPassword().length() < 6) {
            throw new CustomerException("Password must be at least 6 characters.");
        }
        if (email == null || req.getOtp() == null) {
            throw new CustomerException("Invalid or expired OTP.");
        }

        PasswordResetOtp otp = otpRepository.findFirstByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new CustomerException("Invalid or expired OTP."));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now()) || otp.getAttempts() >= MAX_ATTEMPTS) {
            throw new CustomerException("Invalid or expired OTP.");
        }

        if (!passwordEncoder.matches(req.getOtp().trim(), otp.getOtpHash())) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);
            throw new CustomerException("Invalid or expired OTP.");
        }

        Customer customer = customerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new CustomerException("Invalid or expired OTP."));

        customer.setPassword(passwordEncoder.encode(req.getNewPassword()));
        customerRepository.save(customer);

        otpRepository.deleteByEmail(email);     // an OTP can only be used once
    }

    // remove expired rows every hour
    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void cleanup() {
        otpRepository.deleteByExpiresAtBefore(LocalDateTime.now().minusDays(1));
    }

    private String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

}
