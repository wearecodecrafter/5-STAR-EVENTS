package com.backend.models.requests.pacakage;

import lombok.Data;
import lombok.ToString;

import java.time.LocalTime;
import java.util.UUID;

@Data
@ToString
public class PackageTimeRequest {
    private UUID id;

    private String name;

    private LocalTime startTime;
    private LocalTime endTime;

}
