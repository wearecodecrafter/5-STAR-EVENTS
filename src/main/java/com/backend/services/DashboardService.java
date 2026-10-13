package com.backend.services;

import com.backend.models.response.DashboardResponse;

public interface DashboardService {

    DashboardResponse getDashboard(int month, int year);
}
