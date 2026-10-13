package com.backend.services;

import com.backend.models.enums.CalendarView;
import com.backend.models.enums.SlotStatus;
import com.backend.models.response.calender.CalendarResponse;

import java.time.LocalDate;

public interface CalenderService {
    CalendarResponse getCalendar(CalendarView view, LocalDate date, SlotStatus statusFilter);
}
