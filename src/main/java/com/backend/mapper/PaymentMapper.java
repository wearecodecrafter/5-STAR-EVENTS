package com.backend.mapper;

import com.backend.models.payment.Payment;
import com.backend.models.requests.payment.PaymentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "customerId", source = "booking.customer.id")
    @Mapping(target = "customerName", source = "booking.customer.fullName")
    @Mapping(target = "customerPhone", source = "booking.customer.phoneNumber")
    @Mapping(target = "eventDate", source = "booking.eventDate")
    PaymentRequest toDto(Payment payment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "booking", ignore = true)
    Payment toEntity(PaymentRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "booking", ignore = true)
    void update(PaymentRequest request, @MappingTarget Payment payment);
}