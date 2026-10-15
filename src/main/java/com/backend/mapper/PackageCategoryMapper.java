package com.backend.mapper;

import com.backend.models.PackageCategory;
import com.backend.models.requests.pacakage.PackageCategoryRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
@Mapper(componentModel = "spring", uses = {AmenitiesMapper.class})
public interface PackageCategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    PackageCategory toEntity(PackageCategoryRequest request);

    PackageCategoryRequest toDto(PackageCategory packageCategory);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    void update(PackageCategoryRequest request, @MappingTarget PackageCategory packageCategory);
}
