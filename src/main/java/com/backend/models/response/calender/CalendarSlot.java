package com.backend.models.response.calender;

import com.backend.models.enums.SlotStatus;

import java.time.LocalTime;
import java.util.UUID;

public record CalendarSlot(UUID packageTimeId, String name, LocalTime startTime, LocalTime endTime,
                           SlotStatus status, int inquiryCount, UUID bookingId,
                           String customerName, String remarks) {
}
