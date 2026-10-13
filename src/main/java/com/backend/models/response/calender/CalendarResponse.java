package com.backend.models.response.calender;

import com.backend.models.enums.CalendarView;

import java.time.LocalDate;
import java.util.List;

public record CalendarResponse(CalendarView view, LocalDate from, LocalDate to,
                               CalendarSummary summary, List<CalendarDay> days) {
}
