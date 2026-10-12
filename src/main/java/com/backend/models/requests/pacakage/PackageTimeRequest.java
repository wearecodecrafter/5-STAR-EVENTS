package com.backend.models.requests.pacakage;

import lombok.Data;

import java.time.LocalTime;
import java.util.UUID;

@Data
public class PackageTimeRequest {
    private UUID id;

    private String name;

    private LocalTime startTime;
    private LocalTime endTime;

    private PackageCategoryRequest packageCategory;
}
