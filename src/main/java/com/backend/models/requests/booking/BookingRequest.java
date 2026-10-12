package com.backend.models.requests.booking;

import com.backend.models.Customer;
import com.backend.models.PackageCategory;
import com.backend.models.PackageTime;
import com.backend.models.enums.BOOKING_STATUS;
import com.backend.models.requests.pacakage.PackageCategoryRequest;
import com.backend.models.requests.pacakage.PackageTimeRequest;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class BookingRequest {

    private UUID id;

    private LocalDate eventDate;

    private Customer customer;

    private PackageCategoryRequest packageCategory;

    private PackageTimeRequest packageTime;

    private String venue;

    private Integer expectedGuests;

    private Boolean isCateringRequired=false;

    private Double packageAmount;

    private Double advanceAmount;

    private BOOKING_STATUS bookingStatus;

    private String remarks;
}
