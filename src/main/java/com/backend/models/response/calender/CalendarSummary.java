package com.backend.models.response.calender;

public record CalendarSummary(int available, int inProcess, int booked, int ownerEvent) {
}
