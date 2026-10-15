package com.backend.services;

import com.backend.models.requests.pacakage.PackageTimeRequest;

import java.util.List;
import java.util.UUID;

public interface PackageTimeService {
    PackageTimeRequest create(PackageTimeRequest request);
    PackageTimeRequest update(UUID id, PackageTimeRequest request);
    Boolean delete(UUID id);
    List<PackageTimeRequest> getAll();
}
