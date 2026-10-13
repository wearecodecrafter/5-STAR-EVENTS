package com.backend.services.impl;

import com.backend.exception.PackageException;
import com.backend.mapper.PackageCategoryMapper;
import com.backend.mapper.PackageTimeMapper;
import com.backend.models.PackageCategory;
import com.backend.models.PackageTime;
import com.backend.models.requests.pacakage.PackageCategoryRequest;
import com.backend.models.requests.pacakage.PackageTimeRequest;
import com.backend.repositories.PackageCategoryRepository;
import com.backend.repositories.PackageTimingRepository;
import com.backend.services.PackageCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PackageCategoryServiceImpl implements PackageCategoryService {

    private final PackageCategoryRepository packageCategoryRepository;
    private final PackageTimingRepository packageTimingRepository;
    private final PackageCategoryMapper packageCategoryMapper;
    private final PackageTimeMapper packageTimeMapper;
    private final PaginationResponseImpl paginationResponse;
    @Transactional
    @Override
    public PackageCategoryRequest cratePackageCategory(PackageCategoryRequest request) {

        if (packageCategoryRepository.existsByName(request.getName())) {
            throw new PackageException("Package Category already exists with name: " + request.getName());
        }

        PackageCategory category = packageCategoryMapper.toEntity(request);
        category.setPackageTimes(new ArrayList<>());
        Set<UUID> incomingIds = request.getPackageTimes().stream()
                .map(PackageTimeRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        category.getPackageTimes().removeIf(t -> !incomingIds.contains(t.getId()) && t.getId() != null);
        Set<String> seen = new HashSet<>();
        List<PackageTimeRequest> times =
                request.getPackageTimes() == null ? List.of() : request.getPackageTimes();

        for (PackageTimeRequest req : times) {
            if (!seen.add(req.getName())) {
                throw new PackageException("Duplicate package time in request: " + req.getName());
            }
            PackageTime time = packageTimeMapper.toEntity(req);
            time.setPackageCategory(category);
            category.getPackageTimes().add(time);
        }

        PackageCategory saved = packageCategoryRepository.save(category);
        return packageCategoryMapper.toDto(saved);
    }

    @Transactional
    @Override
    public PackageCategoryRequest updatePackageCategory(
            UUID id, PackageCategoryRequest request) {

        PackageCategory category = packageCategoryRepository.findById(id)
                .orElseThrow(() -> new PackageException(
                        "Package Category not found with id: " + id));

        // Validate duplicate category name
        if (packageCategoryRepository.existsByNameIgnoreCaseAndIdNot(
                request.getName(), id)) {
            throw new PackageException(
                    "Package Category already exists with name: "
                            + request.getName());
        }

        category.setName(request.getName());
        category.setPrice(request.getPrice());
        if (category.getPackageTimes() == null) {
            category.setPackageTimes(new ArrayList<>());
        }

        if (request.getPackageTimes() != null) {

            for (PackageTimeRequest req : request.getPackageTimes()) {

                PackageTime time;

                // Update existing package time
                if (req.getId() != null) {

                    // Find existing package time in this category
                    time = category.getPackageTimes().stream()
                            .filter(t -> t.getId().equals(req.getId()))
                            .findFirst()
                            .orElseThrow(() -> new PackageException(
                                    "Package Time does not belong to this category: "
                                            + req.getId()));

                    // Check duplicate name only against other package times
                    boolean duplicate = packageTimingRepository
                            .existsByNameIgnoreCaseAndIdNot(
                                    req.getName(), time.getId());

                    if (duplicate) {
                        throw new PackageException(
                                "Package Time already exists with name: "
                                        + req.getName());
                    }

                } else {

                    // Create a new package time
                    time = new PackageTime();
                    time.setPackageCategory(category);
                    category.getPackageTimes().add(time);

                    if (packageTimingRepository.existsByNameIgnoreCase(req.getName())) {
                        throw new PackageException(
                                "Package Time already exists with name: "
                                        + req.getName());
                    }
                }
                time.setName(req.getName());
                time.setStartTime(req.getStartTime());
                time.setEndTime(req.getEndTime());
            }
        }

        PackageCategory saved = packageCategoryRepository.save(category);

        return packageCategoryMapper.toDto(saved);
    }
    @Override
    public PackageCategoryRequest getById(UUID id) {
        return packageCategoryRepository.findById(id)
                .map(packageCategoryMapper::toDto)
                .orElseThrow(() -> new PackageException(
                        "Package Category not found with id: " + id));
    }

    @Override
    public Boolean deletePackage(UUID id) {
        PackageCategory packageCategory = packageCategoryRepository.findById(id)
                .orElseThrow(() -> new PackageException(
                        "Package Category not found with id: " + id));
        packageCategoryRepository.delete(packageCategory);
        return true;
    }

    @Override
    public List<PackageCategoryRequest> getAllPackageCategories() {
    return packageCategoryRepository.findAll().stream().map(packageCategoryMapper::toDto).toList();
    }
}
