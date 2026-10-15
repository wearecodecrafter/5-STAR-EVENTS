package com.backend.services.impl;

import com.backend.exception.PackageException;
import com.backend.mapper.AmenitiesMapper;
import com.backend.mapper.PackageCategoryMapper;
import com.backend.models.Amenities;
import com.backend.models.PackageCategory;
import com.backend.models.requests.pacakage.AmenitiesRequest;
import com.backend.models.requests.pacakage.PackageCategoryRequest;
import com.backend.repositories.BookingRepository;
import com.backend.repositories.PackageCategoryRepository;
import com.backend.services.PackageCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class PackageCategoryServiceImpl implements PackageCategoryService {

    private final PackageCategoryRepository packageCategoryRepository;
    private final PackageCategoryMapper packageCategoryMapper;
    private final AmenitiesMapper amenitiesMapper;
    private final BookingRepository bookingRepository;

    // ------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------
    @Transactional
    @Override
    public PackageCategoryRequest cratePackageCategory(PackageCategoryRequest request) {
        validate(request);
        if (packageCategoryRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new PackageException("Package Category already exists with name: " + request.getName());
        }

        PackageCategory category = packageCategoryMapper.toEntity(request);
        category.setName(request.getName().trim());
        category.setAmenities(new ArrayList<>());

        if (request.getAmenities() != null) {
            for (AmenitiesRequest req : request.getAmenities()) {
                Amenities amenity = amenitiesMapper.toEntity(req);
                amenity.setName(req.getName().trim());
                amenity.setPackageCategory(category);      // owning side
                category.getAmenities().add(amenity);
            }
        }
        return packageCategoryMapper.toDto(packageCategoryRepository.save(category));
    }
    @Transactional
    @Override
    public PackageCategoryRequest updatePackageCategory(UUID id, PackageCategoryRequest request) {
        PackageCategory category = packageCategoryRepository.findById(id)
                .orElseThrow(() -> new PackageException("Package Category not found with id: " + id));

        validate(request);
        if (packageCategoryRepository.existsByNameIgnoreCaseAndIdNot(request.getName().trim(), id)) {
            throw new PackageException("Package Category already exists with name: " + request.getName());
        }

        packageCategoryMapper.update(request, category);   // name, price, description only
        category.setName(request.getName().trim());

        if (request.getAmenities() != null) {
            syncAmenities(category, request.getAmenities());
        }
        return packageCategoryMapper.toDto(packageCategoryRepository.save(category));
    }

    private void syncAmenities(PackageCategory category, List<AmenitiesRequest> incoming) {
        if (category.getAmenities() == null) {
            category.setAmenities(new ArrayList<>());
        }
        List<Amenities> current = category.getAmenities();   // always edit THIS list in place

        // 1. the same amenity twice in one request
        Set<String> names = new HashSet<>();
        for (AmenitiesRequest req : incoming) {
            if (!names.add(req.getName().trim().toLowerCase())) {
                throw new PackageException("Duplicate amenity in request: " + req.getName());
            }
        }

        // 2. every id sent must belong to this package
        Map<UUID, Amenities> existingById = current.stream()
                .collect(Collectors.toMap(Amenities::getId, a -> a));
        for (AmenitiesRequest req : incoming) {
            if (req.getId() != null && !existingById.containsKey(req.getId())) {
                throw new PackageException("Amenity does not belong to this package: " + req.getId());
            }
        }

        // 3. REMOVE amenities that are no longer in the request (orphanRemoval deletes the rows)
        Set<UUID> keepIds = incoming.stream()
                .map(AmenitiesRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        current.removeIf(a -> !keepIds.contains(a.getId()));

        // 4. UPDATE existing / ADD new
        for (AmenitiesRequest req : incoming) {
            if (req.getId() != null) {
                Amenities amenity = existingById.get(req.getId());
                amenity.setName(req.getName().trim());
            } else {
                Amenities amenity = amenitiesMapper.toEntity(req);
                amenity.setName(req.getName().trim());
                amenity.setPackageCategory(category);
                current.add(amenity);
            }
        }
    }

    @Transactional(readOnly = true)
    @Override
    public PackageCategoryRequest getById(UUID id) {
        return packageCategoryRepository.findById(id)
                .map(packageCategoryMapper::toDto)
                .orElseThrow(() -> new PackageException("Package Category not found with id: " + id));
    }

    @Transactional(readOnly = true)
    @Override
    public List<PackageCategoryRequest> getAllPackageCategories() {
        return packageCategoryRepository.findAll().stream()
                .map(packageCategoryMapper::toDto).toList();
    }

    @Transactional
    @Override
    public Boolean deletePackage(UUID id) {
        PackageCategory category = packageCategoryRepository.findById(id)
                .orElseThrow(() -> new PackageException("Package Category not found with id: " + id));
        if (bookingRepository.existsByPackageCategory_Id(id)) {
            throw new PackageException("This package has bookings and cannot be deleted.");
        }
        packageCategoryRepository.delete(category);   // amenities are deleted by cascade
        return true;
    }

    private void validate(PackageCategoryRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new PackageException("Package name is required.");
        }
        if (request.getPrice() == null || request.getPrice() < 0) {
            throw new PackageException("Valid package price is required.");
        }
        if (request.getAmenities() != null) {
            for (AmenitiesRequest a : request.getAmenities()) {
                if (a.getName() == null || a.getName().isBlank()) {
                    throw new PackageException("Amenity name cannot be empty.");
                }
            }
        }
    }
}