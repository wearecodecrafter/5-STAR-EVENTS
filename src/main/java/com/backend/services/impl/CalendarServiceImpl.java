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
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalenderService {

    private static final List<BOOKING_STATUS> EXCLUDED =
            List.of(BOOKING_STATUS.CANCELLED, BOOKING_STATUS.REJECTED);

    private final BookingRepository bookingRepository;
    private final PackageTimingRepository packageTimingRepository;

    // ------------------------------------------------------------------
    // MAIN
    // ------------------------------------------------------------------
    @Transactional(readOnly = true)
    @Override
    public CalendarResponse getCalendar(CalendarView view, LocalDate date, SlotStatus statusFilter) {

        CalendarView v = view != null ? view : CalendarView.MONTH;
        LocalDate anchor = date != null ? date : LocalDate.now();
        LocalDate from;
        LocalDate to;

        switch (v) {
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
                .findCalendarBookings(from, to, EXCLUDED)
                .stream()
                .collect(Collectors.groupingBy(Booking::getEventDate));

        // slots with missing times go last instead of crashing the sort
        List<PackageTime> times = packageTimingRepository.findAll().stream()
                .sorted(Comparator.comparing(PackageTime::getStartTime,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        if (times.isEmpty()) {
            return new CalendarResponse(v, from, to, new CalendarSummary(0, 0, 0, 0), List.of());
        }

        LocalDate today = LocalDate.now();
        List<CalendarDay> all = new ArrayList<>();

        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            List<Booking> dayBookings = byDate.getOrDefault(d, List.of());
            List<CalendarSlot> slots = times.stream()
                    .map(t -> buildSlot(t, dayBookings))
                    .toList();

            all.add(new CalendarDay(
                    d,
                    d.equals(today),
                    computeDayStatus(slots),
                    buildLabel(slots),
                    slots));
        }

        // legend counts: over the whole period, before the status filter
        CalendarSummary summary = new CalendarSummary(
                count(all, SlotStatus.AVAILABLE),
                count(all, SlotStatus.IN_PROCESS),
                count(all, SlotStatus.BOOKED),
                count(all, SlotStatus.OWNER_EVENT));

        List<CalendarDay> days = statusFilter == null
                ? all
                : all.stream()
                .filter(day -> day.slots().stream().anyMatch(s -> s.status() == statusFilter))
                .toList();

        return new CalendarResponse(v, from, to, summary, days);
    }

    // ------------------------------------------------------------------
    // ONE SLOT OF ONE DAY
    // ------------------------------------------------------------------
    private CalendarSlot buildSlot(PackageTime slot, List<Booking> dayBookings) {

        // only bookings made for THIS exact slot
        List<Booking> forThisSlot = dayBookings.stream()
                .filter(b -> b.getPackageTime().getId().equals(slot.getId()))
                .toList();

        int inquiries = (int) forThisSlot.stream()
                .filter(b -> b.getBookingStatus() == BOOKING_STATUS.IN_PROGRESS)
                .count();

        Booking owner = find(forThisSlot, BOOKING_STATUS.OWNER_EVENT);
        Booking booked = find(forThisSlot, BOOKING_STATUS.CONFIRMED_BOOKED);
        if (booked == null) booked = find(forThisSlot, BOOKING_STATUS.COMPLETED);

        SlotStatus status;
        Booking main = null;

        if (owner != null)       { status = SlotStatus.OWNER_EVENT; main = owner; }
        else if (booked != null) { status = SlotStatus.BOOKED;      main = booked; }
        else if (inquiries > 0)  { status = SlotStatus.IN_PROCESS; }
        else                     { status = SlotStatus.AVAILABLE; }

        return new CalendarSlot(
                slot.getId(), slot.getName(), slot.getStartTime(), slot.getEndTime(),
                status, inquiries,
                main != null ? main.getId() : null,
                main != null && main.getCustomer() != null ? main.getCustomer().getFullName() : null,
                main != null ? main.getRemarks() : null);
    }
    // ------------------------------------------------------------------
    // DAY TILE STATUS + LABEL
    // ------------------------------------------------------------------
    private SlotStatus computeDayStatus(List<CalendarSlot> slots) {
        if (slots.isEmpty()) return SlotStatus.AVAILABLE;

        boolean allTaken = slots.stream().allMatch(s ->
                s.status() == SlotStatus.BOOKED || s.status() == SlotStatus.OWNER_EVENT);
        if (allTaken) {
            return slots.stream().anyMatch(s -> s.status() == SlotStatus.OWNER_EVENT)
                    ? SlotStatus.OWNER_EVENT : SlotStatus.BOOKED;
        }

        if (slots.stream().anyMatch(s -> s.status() == SlotStatus.IN_PROCESS)) {
            return SlotStatus.IN_PROCESS;
        }
        return SlotStatus.AVAILABLE;   // some slots may be booked: the label says "Partially booked"
    }

    private String buildLabel(List<CalendarSlot> slots) {
        if (slots.isEmpty()) return "No slots configured";

        Set<SlotStatus> distinct = slots.stream()
                .map(CalendarSlot::status)
                .collect(Collectors.toSet());

        if (distinct.size() > 1) return "Partially booked";

        return switch (distinct.iterator().next()) {
            case AVAILABLE   -> "All slots open";
            case IN_PROCESS  -> "All slots in process";
            case BOOKED      -> "All slots booked";
            case OWNER_EVENT -> "Owner's event";
        };
    }

    // ------------------------------------------------------------------
    // HELPERS
    // ------------------------------------------------------------------
    private Booking find(List<Booking> list, BOOKING_STATUS status) {
        return list.stream()
                .filter(b -> b.getBookingStatus() == status)
                .findFirst()
                .orElse(null);
    }

    private int count(List<CalendarDay> days, SlotStatus s) {
        return (int) days.stream().filter(d -> d.status() == s).count();
    }

    // ---- overlap ----
    private boolean overlaps(PackageTime a, PackageTime b) {
        if (a.getId().equals(b.getId())) return true;
        if (!validTimes(a) || !validTimes(b)) return false;   // bad data must not block other slots
        return start(a) < end(b) && start(b) < end(a);
    }

    private boolean validTimes(PackageTime t) {
        if (t.getStartTime() == null || t.getEndTime() == null) return false;
        return end(t) > start(t);
    }

    private int start(PackageTime t) {
        return t.getStartTime().toSecondOfDay() / 60;
    }

    // only 00:00 means "end of day"; any other end time is taken as it is
    private int end(PackageTime t) {
        int e = t.getEndTime().toSecondOfDay() / 60;
        return e == 0 ? 1440 : e;
    }
}