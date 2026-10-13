package com.backend.services.impl;

import com.backend.exception.PackageException;
import com.backend.mapper.PackageCategoryMapper;
import com.backend.mapper.PackageTimeMapper;
import com.backend.models.PackageCategory;
import com.backend.models.PackageTime;
import com.backend.models.requests.pacakage.PackageCategoryRequest;
import com.backend.models.requests.pacakage.PackageTimeRequest;
import com.backend.repositories.BookingRepository;
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
    private final BookingRepository bookingRepository;

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
    public PackageCategoryRequest updatePackageCategory(UUID id, PackageCategoryRequest request) {

        PackageCategory category = packageCategoryRepository.findById(id)
                .orElseThrow(() -> new PackageException("Package Category not found with id: " + id));

        if (packageCategoryRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new PackageException("Package Category already exists with name: " + request.getName());
        }

        category.setName(request.getName());
        category.setPrice(request.getPrice());
        if (category.getPackageTimes() == null) {
            category.setPackageTimes(new ArrayList<>());
        }

        // null = leave times untouched, empty list = remove all times
        if (request.getPackageTimes() != null) {

            // 0. the same name twice in one request
            Set<String> names = new HashSet<>();
            for (PackageTimeRequest req : request.getPackageTimes()) {
                if (req.getName() == null || !names.add(req.getName().trim().toLowerCase())) {
                    throw new PackageException("Duplicate or empty package time name: " + req.getName());
                }
            }

            // 1. REMOVE slots that are no longer in the request
            Set<UUID> incomingIds = request.getPackageTimes().stream()
                    .map(PackageTimeRequest::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<PackageTime> toRemove = category.getPackageTimes().stream()
                    .filter(t -> !incomingIds.contains(t.getId()))
                    .toList();

            for (PackageTime t : toRemove) {
                if (bookingRepository.existsByPackageTime_Id(t.getId())) {
                    throw new PackageException("Package time '" + t.getName()
                            + "' has bookings and cannot be removed.");
                }
            }
            category.getPackageTimes().removeAll(toRemove);   // in place -> orphanRemoval deletes rows
            packageTimingRepository.flush();                  // so a removed name can be reused below

            // 2. UPDATE existing / ADD new
            for (PackageTimeRequest req : request.getPackageTimes()) {
                PackageTime time;

                if (req.getId() != null) {
                    time = category.getPackageTimes().stream()
                            .filter(t -> t.getId().equals(req.getId()))
                            .findFirst()
                            .orElseThrow(() -> new PackageException(
                                    "Package Time does not belong to this category: " + req.getId()));

                    if (packageTimingRepository.existsByNameIgnoreCaseAndIdNot(req.getName(), time.getId())) {
                        throw new PackageException("Package Time already exists with name: " + req.getName());
                    }
                } else {
                    if (packageTimingRepository.existsByNameIgnoreCase(req.getName())) {
                        throw new PackageException("Package Time already exists with name: " + req.getName());
                    }
                    time = new PackageTime();
                    time.setPackageCategory(category);
                    category.getPackageTimes().add(time);
                }

                time.setName(req.getName());
                time.setStartTime(req.getStartTime());
                time.setEndTime(req.getEndTime());
            }
        }

        return packageCategoryMapper.toDto(packageCategoryRepository.save(category));
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
    public Boolean deleteTimeSlot(UUID id) {
        PackageTime packageTime = packageTimingRepository.findById(id).orElseThrow(() -> new PackageException("Slot not found with id: " + id));
        packageTimingRepository.delete(packageTime);
        return true;
    }

    @Override
    public List<PackageCategoryRequest> getAllPackageCategories() {
    return packageCategoryRepository.findAll().stream().map(packageCategoryMapper::toDto).toList();
    }
}
