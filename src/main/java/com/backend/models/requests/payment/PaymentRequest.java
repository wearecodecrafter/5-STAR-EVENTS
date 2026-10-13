package com.backend.models.requests.payment;

import com.backend.models.Booking;
import com.backend.models.requests.booking.BookingRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
public class PaymentRequest {
    private UUID id;
    private UUID bookingId;
    private Double amount;
    private String paymentType;
    private String paymentMethod;
    private String transactionId;
    private String notes;
    private LocalDateTime paymentDate;

    // read-only, filled by the mapper
    private UUID customerId;
    private String customerName;
    private String customerPhone;
    private LocalDate eventDate;
}