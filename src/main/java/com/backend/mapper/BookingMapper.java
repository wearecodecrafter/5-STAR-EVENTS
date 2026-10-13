package com.backend.mapper;

import com.backend.models.Booking;
import com.backend.models.requests.booking.BookingRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring",
        uses = {CustomerMapper.class, PackageCategoryMapper.class, PackageTimeMapper.class})
public interface BookingMapper {

    BookingRequest toDto(Booking booking);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "packageTime", ignore = true)
    @Mapping(target = "packageCategory", ignore = true)
    @Mapping(target = "bookingStatus", ignore = true)
    void update(BookingRequest request, @MappingTarget Booking booking);
}