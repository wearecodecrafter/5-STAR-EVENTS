package com.backend.services;

import com.backend.models.Booking;
import com.backend.models.requests.payment.PaymentRequest;
import com.backend.models.response.PaginatedResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface PaymentService {
    PaymentRequest createPayment(PaymentRequest request);
    PaymentRequest updatePayment(UUID id, PaymentRequest request);
    PaymentRequest getPaymentById(UUID id);
    Boolean deletePayment(UUID id);
    PaginatedResponse<PaymentRequest> searchPayments(String keyword, int page, int size);
}
