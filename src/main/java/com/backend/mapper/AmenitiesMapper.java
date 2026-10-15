package com.backend.mapper;


import com.backend.models.Amenities;
import com.backend.models.requests.pacakage.AmenitiesRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
@Mapper(componentModel = "spring")
public interface AmenitiesMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "packageCategory", ignore = true)
    Amenities toEntity(AmenitiesRequest request);

    AmenitiesRequest toDto(Amenities amenities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "packageCategory", ignore = true)
    void update(AmenitiesRequest request, @MappingTarget Amenities amenities);
}