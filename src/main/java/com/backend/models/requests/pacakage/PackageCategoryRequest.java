package com.backend.models.requests.pacakage;

import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Data
@ToString
public class PackageCategoryRequest {

    private UUID id;

    private String name;

    private Double price;

    List<PackageTimeRequest> packageTimes;
}
