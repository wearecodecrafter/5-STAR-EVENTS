package com.backend.mapper;

import com.backend.models.Customer;
import com.backend.models.requests.CustomerRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "email", ignore = true)
    Customer toEntity(CustomerRequest request);

    CustomerRequest toDto(Customer customer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    void update(CustomerRequest request, @MappingTarget Customer customer);
}