package com.backend.services.impl;


import com.backend.models.enums.BOOKING_STATUS;
import com.backend.models.response.DashboardResponse;
import com.backend.repositories.BookingRepository;
import com.backend.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final BookingRepository bookingRepository;

    @Override
    public DashboardResponse getDashboard(int month, int year) {
        Long total = bookingRepository.count();
        Long inProgress = bookingRepository.countCreatedByMonthAndYear(month,year,BOOKING_STATUS.IN_PROGRESS);
        Long confirmed = bookingRepository.countCreatedByMonthAndYear(month,year,BOOKING_STATUS.CONFIRMED_BOOKED);
        Long avaSlots = countAvailableDates(month, year);
        LocalDate currentDate = LocalDate.now();
        LocalDate endDate = currentDate.plusDays(15);
        Long upcoming = bookingRepository.findUpcomingEvents(
                currentDate, endDate,BOOKING_STATUS.CONFIRMED_BOOKED).stream().count();

        DashboardResponse response = new DashboardResponse();
        response.setTotalBookings(total);
        response.setInProcess(inProgress);
        response.setConfirmBookings(confirmed);
        response.setAvailableSlots(avaSlots);
        response.setUpcomingEvents(upcoming);
        response.setMonth(String.valueOf(month));
        response.setYear(String.valueOf(year));
        return response;
    }

    public Long countAvailableDates(int month, int year) {

        YearMonth yearMonth = YearMonth.of(year, month);

        long totalDays = yearMonth.lengthOfMonth();

        Long bookedDates = bookingRepository
                .countBookedDatesByMonthAndYear(month, year);

        return totalDays - bookedDates;
    }
}
