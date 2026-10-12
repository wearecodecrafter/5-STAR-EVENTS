package com.backend.mapper;

import com.backend.models.Booking;
import com.backend.models.requests.booking.BookingRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    Booking toEntity(BookingRequest request);

    BookingRequest toDto(Booking booking);

    void update(BookingRequest request, @MappingTarget Booking booking);
}
