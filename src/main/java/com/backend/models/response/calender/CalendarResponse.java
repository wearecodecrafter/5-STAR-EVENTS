package com.backend.models.response;

public record CalendarResponse(CalendarView view, LocalDate from, LocalDate to,
                               CalendarSummary summary, List<CalendarDay> days) {
}
