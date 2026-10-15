package com.backend.configurations;

import com.backend.models.requests.password.PasswordResetOtpEvent;
import com.backend.services.impl.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.HtmlUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordResetOtpListener {

    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOtp(PasswordResetOtpEvent e) {
        try {
            String html = """
                <div style="font-family:Arial,sans-serif;max-width:480px">
                  <p>Hello %s,</p>
                  <p>Your password reset code is:</p>
                  <p style="font-size:28px;letter-spacing:6px;font-weight:bold">%s</p>
                  <p>This code is valid for %d minutes. If you did not request it, ignore this email.</p>
                  <p>Five Star Events</p>
                </div>
                """.formatted(HtmlUtils.htmlEscape(e.name() == null ? "" : e.name()), e.otp(), e.validMinutes());

            emailService.sendHtmlEmail(e.email(), "Your password reset code", html);
        } catch (Exception ex) {
            log.error("OTP email failed for {}", e.email(), ex);
        }
    }
}