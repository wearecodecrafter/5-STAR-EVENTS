package com.backend.services.impl;

import com.backend.exception.PackageException;
import com.backend.mapper.PackageTimeMapper;
import com.backend.models.PackageTime;
import com.backend.models.requests.pacakage.PackageTimeRequest;
import com.backend.repositories.BookingRepository;
import com.backend.repositories.PackageTimingRepository;
import com.backend.services.PackageTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PackageTimeServiceImpl implements PackageTimeService {

    private final PackageTimingRepository packageTimingRepository;
    private final PackageTimeMapper packageTimeMapper;
    private final BookingRepository bookingRepository;

    @Transactional
    @Override
    public PackageTimeRequest create(PackageTimeRequest request) {
        validate(request);
        if (packageTimingRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new PackageException("Time slot already exists with name: " + request.getName());
        }
        PackageTime time = packageTimeMapper.toEntity(request);
        time.setName(request.getName().trim());
        return packageTimeMapper.toDto(packageTimingRepository.save(time));
    }

    @Transactional
    @Override
    public PackageTimeRequest update(UUID id, PackageTimeRequest request) {
        PackageTime time = packageTimingRepository.findById(id)
                .orElseThrow(() -> new PackageException("Slot not found with id: " + id));
        validate(request);
        if (packageTimingRepository.existsByNameIgnoreCaseAndIdNot(request.getName().trim(), id)) {
            throw new PackageException("Time slot already exists with name: " + request.getName());
        }
        packageTimeMapper.update(request, time);
        time.setName(request.getName().trim());
        return packageTimeMapper.toDto(packageTimingRepository.save(time));
    }

    @Transactional
    @Override
    public Boolean delete(UUID id) {
        PackageTime time = packageTimingRepository.findById(id)
                .orElseThrow(() -> new PackageException("Slot not found with id: " + id));
        if (bookingRepository.existsByPackageTime_Id(id)) {
            throw new PackageException("This slot has bookings and cannot be deleted.");
        }
        packageTimingRepository.delete(time);
        return true;
    }

    @Transactional(readOnly = true)
    @Override
    public List<PackageTimeRequest> getAll() {
        return packageTimingRepository.findAll(Sort.by("startTime")).stream()
                .map(packageTimeMapper::toDto).toList();
    }

    private void validate(PackageTimeRequest r) {
        if (r.getName() == null || r.getName().isBlank()) {
            throw new PackageException("Slot name is required.");
        }
        if (r.getStartTime() == null || r.getEndTime() == null) {
            throw new PackageException("Start and end time are required.");
        }
    }
}