package com.backend.models.response;

import lombok.Data;

@Data
public class DashboardResponse {

    private Long totalBookings;

    private Long inProcess;

    private Long confirmBookings;

    private Long availableSlots;

    private Long upcomingEvents;

    private String month;

    private String year;
}
