package com.backend.services;

import com.backend.models.requests.pacakage.PackageCategoryRequest;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.UUID;

public interface PackageCategoryService {

    PackageCategoryRequest cratePackageCategory(PackageCategoryRequest request);

    PackageCategoryRequest updatePackageCategory(UUID id, PackageCategoryRequest request);

    PackageCategoryRequest getById(UUID id);

    Boolean deletePackage(UUID id);

    List<PackageCategoryRequest> getAllPackageCategories();
}
