package com.backend.models.payment;

import com.backend.models.Booking;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private Double amount;

    private String paymentType;

    private String paymentMethod;

    private String transactionId;

    private String notes;

    private LocalDateTime paymentDate;
}
