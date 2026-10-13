package com.backend.services.impl;

import com.backend.models.Booking;
import com.backend.models.PackageTime;
import com.backend.models.enums.BOOKING_STATUS;
import com.backend.models.enums.CalendarView;
import com.backend.models.enums.SlotStatus;
import com.backend.models.response.calender.CalendarDay;
import com.backend.models.response.calender.CalendarResponse;
import com.backend.models.response.calender.CalendarSlot;
import com.backend.models.response.calender.CalendarSummary;
import com.backend.repositories.BookingRepository;
import com.backend.repositories.PackageTimingRepository;
import com.backend.services.CalenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalenderService {

        private final BookingRepository bookingRepository;
        private final PackageTimingRepository packageTimingRepository;

        @Transactional(readOnly = true)
        @Override
        public CalendarResponse getCalendar(CalendarView view, LocalDate date, SlotStatus statusFilter) {
            LocalDate anchor = date != null ? date : LocalDate.now();
            LocalDate from;
            LocalDate to;
            switch (view) {
                case MONTH -> {
                    YearMonth ym = YearMonth.from(anchor);
                    from = ym.atDay(1);
                    to = ym.atEndOfMonth();
                }
                case WEEK -> {
                    from = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
                    to = from.plusDays(6);
                }
                default -> {
                    from = anchor;
                    to = anchor;
                }
            }

            Map<LocalDate, List<Booking>> byDate = bookingRepository
                    .findCalendarBookings(from, to, List.of(BOOKING_STATUS.CANCELLED, BOOKING_STATUS.REJECTED))
                    .stream().collect(Collectors.groupingBy(Booking::getEventDate));

            List<PackageTime> times = packageTimingRepository.findAll().stream()
                    .sorted(Comparator.comparing(PackageTime::getStartTime)).toList();

            LocalDate today = LocalDate.now();
            List<CalendarDay> all = new ArrayList<>();
            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                List<Booking> dayBookings = byDate.getOrDefault(d, List.of());
                List<CalendarSlot> slots = times.stream().map(t -> buildSlot(t, dayBookings)).toList();

                SlotStatus dayStatus = slots.stream().map(CalendarSlot::status)
                        .max(Comparator.naturalOrder()).orElse(SlotStatus.AVAILABLE);

                all.add(new CalendarDay(d, d.equals(today), dayStatus, buildLabel(slots), slots));
            }

            CalendarSummary summary = new CalendarSummary(
                    count(all, SlotStatus.AVAILABLE), count(all, SlotStatus.IN_PROCESS),
                    count(all, SlotStatus.BOOKED), count(all, SlotStatus.OWNER_EVENT));

            List<CalendarDay> days = statusFilter == null ? all
                    : all.stream()
                    .filter(day -> day.slots().stream().anyMatch(s -> s.status() == statusFilter))
                    .toList();

            return new CalendarResponse(view, from, to, summary, days);
        }

        private CalendarSlot buildSlot(PackageTime slot, List<Booking> dayBookings) {
            // a booking affects this slot if its time range overlaps (so a full-day booking blocks both halves)
            List<Booking> hits = dayBookings.stream()
                    .filter(b -> overlaps(slot, b.getPackageTime())).toList();

            Booking owner = find(hits, BOOKING_STATUS.OWNER_EVENT);
            Booking booked = find(hits, BOOKING_STATUS.CONFIRMED_BOOKED);
            if (booked == null) booked = find(hits, BOOKING_STATUS.COMPLETED);
            int inquiries = (int) hits.stream()
                    .filter(b -> b.getBookingStatus() == BOOKING_STATUS.IN_PROGRESS).count();

            SlotStatus status;
            Booking main = null;
            if (owner != null)        { status = SlotStatus.OWNER_EVENT; main = owner; }
            else if (booked != null)  { status = SlotStatus.BOOKED;      main = booked; }
            else if (inquiries > 0)   { status = SlotStatus.IN_PROCESS; }
            else                      { status = SlotStatus.AVAILABLE; }

            return new CalendarSlot(
                    slot.getId(), slot.getName(), slot.getStartTime(), slot.getEndTime(),
                    status, inquiries,
                    main != null ? main.getId() : null,
                    main != null && main.getCustomer() != null ? main.getCustomer().getFullName() : null,
                    main != null ? main.getRemarks() : null);
        }

        private String buildLabel(List<CalendarSlot> slots) {
            Set<SlotStatus> distinct = slots.stream().map(CalendarSlot::status).collect(Collectors.toSet());
            if (distinct.size() > 1) return "Partially booked";
            return switch (distinct.iterator().next()) {
                case AVAILABLE   -> "All slots open";
                case IN_PROCESS  -> "All slots in process";
                case BOOKED      -> "All slots booked";
                case OWNER_EVENT -> "Owner's event";
            };
        }

        private Booking find(List<Booking> list, BOOKING_STATUS status) {
            return list.stream().filter(b -> b.getBookingStatus() == status).findFirst().orElse(null);
        }

        private int count(List<CalendarDay> days, SlotStatus s) {
            return (int) days.stream().filter(d -> d.status() == s).count();
        }

        // ---- overlap: "12:00 AM" end time means end of day (1440 min) ----
        private boolean overlaps(PackageTime a, PackageTime b) {
            if (a.getId().equals(b.getId())) return true;
            return start(a) < end(b) && start(b) < end(a);
        }
        private int start(PackageTime t) { return t.getStartTime().toSecondOfDay() / 60; }
        private int end(PackageTime t) {
            int e = t.getEndTime().toSecondOfDay() / 60;
            return e <= start(t) ? 1440 : e;
        }
}

