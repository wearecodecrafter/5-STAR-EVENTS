package com.backend.mapper;

import com.backend.models.PackageTime;
import com.backend.models.requests.pacakage.PackageTimeRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PackageTimeMapper {

    PackageTime toEntity(PackageTimeRequest request);

    PackageTimeRequest toDto(PackageTime packageTime);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "packageCategory", ignore = true)
    void update(PackageTimeRequest request,@MappingTarget PackageTime packageTime);
}
