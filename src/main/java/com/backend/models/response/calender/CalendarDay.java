package com.backend.models.response.calender;

import com.backend.models.enums.SlotStatus;

import java.time.LocalDate;
import java.util.List;

public record CalendarDay(LocalDate date, boolean today, SlotStatus status,
                          String label, List<CalendarSlot> slots) {
}
