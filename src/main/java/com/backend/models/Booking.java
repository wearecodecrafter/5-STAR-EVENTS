package com.backend.models;

import com.backend.models.enums.BOOKING_STATUS;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Booking extends BaseAuditableEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDate eventDate;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "package_category_id")
    private PackageCategory packageCategory;

    @ManyToOne
    @JoinColumn(name = "package_time_id")
    private PackageTime packageTime;

    private String venue;

    private Integer expectedGuests;

    private Boolean isCateringRequired=false;

    private Double packageAmount;

    private Double advanceAmount;

    @Enumerated(EnumType.STRING)
    private BOOKING_STATUS bookingStatus;

    private String remarks;
}
