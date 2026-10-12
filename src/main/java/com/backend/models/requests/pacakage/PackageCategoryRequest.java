package com.backend.models.requests.pacakage;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PackageCategoryRequest {

    private UUID id;

    private String name;

    List<PackageTimeRequest> packageTimes;
}
