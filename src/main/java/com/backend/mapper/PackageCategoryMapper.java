package com.backend.mapper;

import com.backend.models.PackageCategory;
import com.backend.models.requests.pacakage.PackageCategoryRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PackageCategoryMapper {

    PackageCategory toEntity(PackageCategoryRequest request);

    PackageCategoryRequest toDto(PackageCategory packageCategory);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "packageTimes", ignore = true)
    void update(PackageCategoryRequest request, PackageCategory packageCategory);
}
