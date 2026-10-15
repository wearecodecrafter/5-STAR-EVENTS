package com.backend.services.impl;

import com.backend.exception.PackageException;
import com.backend.mapper.BookingMapper;
import com.backend.models.Booking;
import com.backend.models.Customer;
import com.backend.models.PackageCategory;
import com.backend.models.PackageTime;
import com.backend.models.enums.BOOKING_STATUS;
import com.backend.models.requests.booking.BookingRequest;
import com.backend.models.response.PaginatedResponse;
import com.backend.repositories.BookingRepository;
import com.backend.repositories.CustomerRepository;
import com.backend.repositories.PackageCategoryRepository;
import com.backend.repositories.PackageTimingRepository;
import com.backend.services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    /** Statuses that fully occupy a slot. Nobody else can inquire/book it. */
    private final PackageCategoryRepository packageCategoryRepository;
    private final CustomerResolver customerResolver;
    private static final List<BOOKING_STATUS> BLOCKING_STATUSES = List.of(
            BOOKING_STATUS.CONFIRMED_BOOKED,
            BOOKING_STATUS.COMPLETED,
            BOOKING_STATUS.OWNER_EVENT);

    /** Statuses that can no longer be edited / revived. */
    private static final List<BOOKING_STATUS> CLOSED_STATUSES = List.of(
            BOOKING_STATUS.CANCELLED,
            BOOKING_STATUS.REJECTED,
            BOOKING_STATUS.COMPLETED);

    private static final UUID NO_EXCLUDE = new UUID(0L, 0L);

    private final CustomerRepository customerRepository;
    private final PackageTimingRepository packageTimingRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final PaginationResponseImpl paginationResponse;

    // ------------------------------------------------------------------
    // CREATE INQUIRY (customer wants a slot) -> IN_PROGRESS
    // ------------------------------------------------------------------
    @Transactional
    @Override
    public BookingRequest createBooking(BookingRequest request) {
        validateEventDate(request.getEventDate());
        PackageTime packageTime = getPackageTime(request);
        PackageCategory packageCategory = getPackageCategory(request);

        List<Booking> slotBookings = bookingRepository
                .findSlotBookingsForUpdate(request.getEventDate(), packageTime.getId());
        if (slotBookings.stream().anyMatch(b -> BLOCKING_STATUSES.contains(b.getBookingStatus()))) {
            throw new PackageException("This slot is already booked for the selected date.");
        }

        Customer customer = customerResolver.resolve(request.getCustomer());

        if (bookingRepository.existsByEventDateAndPackageTime_IdAndCustomer_IdAndBookingStatus(
                request.getEventDate(), packageTime.getId(), customer.getId(), BOOKING_STATUS.IN_PROGRESS)) {
            throw new PackageException("This customer already has an inquiry for this slot.");
        }

        Booking booking = new Booking();
        booking.setEventDate(request.getEventDate());
        booking.setCustomer(customer);
        booking.setPackageTime(packageTime);
        booking.setPackageCategory(packageCategory);               // chosen by the customer, not from the slot
        booking.setVenue(request.getVenue());
        booking.setExpectedGuests(request.getExpectedGuests());
        booking.setIsCateringRequired(Boolean.TRUE.equals(request.getIsCateringRequired()));
        booking.setPackageAmount(request.getPackageAmount() != null
                ? request.getPackageAmount()
                : packageCategory.getPrice());                     // default to the package price
        booking.setAdvanceAmount(request.getAdvanceAmount());
        booking.setBookingStatus(BOOKING_STATUS.IN_PROGRESS);
        booking.setRemarks(request.getRemarks());

        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    // ------------------------------------------------------------------
    // OWNER CONFIRMS ONE INQUIRY -> others on same slot get REJECTED
    // ------------------------------------------------------------------
    @Transactional
    @Override
    public BookingRequest confirmBooking(UUID id) {
        Booking booking = findBooking(id);

        if (booking.getBookingStatus() != BOOKING_STATUS.IN_PROGRESS) {
            throw new PackageException(
                    "Only bookings that are In Progress can be confirmed.");
        }
        validateEventDate(booking.getEventDate());

        UUID timeId = booking.getPackageTime().getId();
        LocalDate date = booking.getEventDate();

        // Lock every booking on this slot so two confirmations can't race
        List<Booking> slotBookings =
                bookingRepository.findSlotBookingsForUpdate(date, timeId);

        boolean blockedByOther = slotBookings.stream()
                .anyMatch(b -> !b.getId().equals(id)
                        && BLOCKING_STATUSES.contains(b.getBookingStatus()));
        if (blockedByOther) {
            throw new PackageException(
                    "This slot has already been booked by someone else.");
        }

        booking.setBookingStatus(BOOKING_STATUS.CONFIRMED_BOOKED);

        // Reject all competing inquiries
        slotBookings.stream()
                .filter(b -> !b.getId().equals(id))
                .filter(b -> b.getBookingStatus() == BOOKING_STATUS.IN_PROGRESS)
                .forEach(b -> b.setBookingStatus(BOOKING_STATUS.REJECTED));

        bookingRepository.saveAll(slotBookings);
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    // ------------------------------------------------------------------
    // OWNER BLOCKS A SLOT FOR HIS OWN EVENT -> OWNER_EVENT
    // ------------------------------------------------------------------
    @Transactional
    @Override
    public BookingRequest createOwnerEvent(BookingRequest request) {
        validateEventDate(request.getEventDate());
        PackageTime packageTime = getPackageTime(request);

        List<Booking> slotBookings = bookingRepository
                .findSlotBookingsForUpdate(request.getEventDate(), packageTime.getId());

        boolean blocked = slotBookings.stream()
                .anyMatch(b -> BLOCKING_STATUSES.contains(b.getBookingStatus()));
        if (blocked) {
            throw new PackageException(
                    "This slot is already booked. Cancel the existing booking first.");
        }

        // Any pending inquiries on this slot can no longer be served
        slotBookings.stream()
                .filter(b -> b.getBookingStatus() == BOOKING_STATUS.IN_PROGRESS)
                .forEach(b -> b.setBookingStatus(BOOKING_STATUS.REJECTED));
        bookingRepository.saveAll(slotBookings);

        Booking event = new Booking();
        event.setEventDate(request.getEventDate());
        event.setPackageTime(packageTime);
        event.setVenue(request.getVenue());
        event.setRemarks(request.getRemarks());          // e.g. "Owner's family function"
        event.setIsCateringRequired(false);
        event.setBookingStatus(BOOKING_STATUS.OWNER_EVENT);
        // customer stays null for owner events

        return bookingMapper.toDto(bookingRepository.save(event));
    }

    @Transactional
    @Override
    public BookingRequest updateBooking(UUID id, BookingRequest request) {

        Booking booking = findBooking(id);
        BOOKING_STATUS status = booking.getBookingStatus();

        if (CLOSED_STATUSES.contains(status)) {
            throw new PackageException("A " + status + " booking cannot be edited.");
        }

        if (request.getEventDate() == null) {
            throw new PackageException("Event date is required.");
        }
        boolean dateChanged = !request.getEventDate().equals(booking.getEventDate());
        if (dateChanged) {
            validateEventDate(request.getEventDate());
        }

        // 3. Slot, plus customer and package for normal bookings (owner events have neither)
        PackageTime packageTime = getPackageTime(request);
        boolean isOwnerEvent = status == BOOKING_STATUS.OWNER_EVENT;

        PackageCategory packageCategory = null;
        if (!isOwnerEvent) {
            packageCategory = getPackageCategory(request);
        }

        // 4. Amounts must make sense
        validateAmounts(request, packageCategory);

        boolean slotChanged = dateChanged
                || !packageTime.getId().equals(booking.getPackageTime().getId());

        // 5. Lock the target slot and check it
        if (slotChanged) {
            List<Booking> targetSlot = bookingRepository
                    .findSlotBookingsForUpdate(request.getEventDate(), packageTime.getId());

            // a confirmed booking or owner event on the target slot blocks the move
            boolean blocked = targetSlot.stream()
                    .anyMatch(b -> !b.getId().equals(id)
                            && BLOCKING_STATUSES.contains(b.getBookingStatus()));
            if (blocked) {
                throw new PackageException("This slot is already booked for the selected date.");
            }

            // a confirmed booking or owner event that moves takes the slot, so competing
            // inquiries there are rejected (same rule as confirm)
            if (status != BOOKING_STATUS.IN_PROGRESS) {
                targetSlot.stream()
                        .filter(b -> !b.getId().equals(id))
                        .filter(b -> b.getBookingStatus() == BOOKING_STATUS.IN_PROGRESS)
                        .forEach(b -> b.setBookingStatus(BOOKING_STATUS.REJECTED));
                bookingRepository.saveAll(targetSlot);
            }
        }

        // 6. Customer (existing or new). Resolved after the slot check so a failed edit creates no customer.
        Customer customer = null;
        if (!isOwnerEvent) {
            customer = customerResolver.resolve(request.getCustomer());

            // an inquiry moved onto a slot where the same customer already has one
            if (status == BOOKING_STATUS.IN_PROGRESS && slotChanged
                    && bookingRepository.existsByEventDateAndPackageTime_IdAndCustomer_IdAndBookingStatus(
                    request.getEventDate(), packageTime.getId(),
                    customer.getId(), BOOKING_STATUS.IN_PROGRESS)) {
                throw new PackageException("This customer already has an inquiry for this slot.");
            }
        }

        // 7. Apply the changes (status is never changed here; use confirm / updateStatus)
        booking.setEventDate(request.getEventDate());
        booking.setPackageTime(packageTime);
        booking.setVenue(request.getVenue());
        booking.setRemarks(request.getRemarks());

        if (!isOwnerEvent) {
            boolean packageChanged = booking.getPackageCategory() == null
                    || !booking.getPackageCategory().getId().equals(packageCategory.getId());

            booking.setCustomer(customer);
            booking.setPackageCategory(packageCategory);
            booking.setExpectedGuests(request.getExpectedGuests());
            booking.setIsCateringRequired(Boolean.TRUE.equals(request.getIsCateringRequired()));
            booking.setAdvanceAmount(request.getAdvanceAmount());

            if (request.getPackageAmount() != null) {
                booking.setPackageAmount(request.getPackageAmount());        // owner's own amount
            } else if (packageChanged || booking.getPackageAmount() == null) {
                booking.setPackageAmount(packageCategory.getPrice());        // default to the package price
            }                                                                // otherwise keep the saved amount
        }

        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    private void validateAmounts(BookingRequest request, PackageCategory category) {
        Double packageAmount = request.getPackageAmount() != null
                ? request.getPackageAmount()
                : (category != null ? category.getPrice() : null);
        Double advance = request.getAdvanceAmount();

        if (request.getPackageAmount() != null && request.getPackageAmount() < 0) {
            throw new PackageException("Package amount cannot be negative.");
        }
        if (advance != null) {
            if (advance < 0) {
                throw new PackageException("Advance amount cannot be negative.");
            }
            if (packageAmount != null && advance > packageAmount) {
                throw new PackageException("Advance amount cannot exceed the package amount.");
            }
        }
        if (request.getExpectedGuests() != null && request.getExpectedGuests() < 0) {
            throw new PackageException("Expected guests cannot be negative.");
        }
    }

    @Override
    public BookingRequest getBookingById(UUID id) {
        return bookingMapper.toDto(findBooking(id));
    }

    @Transactional
    @Override
    public Boolean deleteBooking(UUID id) {
        bookingRepository.delete(findBooking(id));
        return true;
    }


    @Override
    public PaginatedResponse<BookingRequest> getAllBookings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> all = bookingRepository.findAll(pageable);
        List<BookingRequest> list = all.getContent().stream()
                .map(bookingMapper::toDto).toList();
        return paginationResponse.buildPaginatedResponse(list, all);
    }


    // ------------------------------------------------------------------
    // CALENDAR DATA: every booking of a month (frontend derives
    // Available / In Process / Booked / Owner's Event per slot)
    // ------------------------------------------------------------------
    @Override
    public List<BookingRequest> getBookingsByMonth(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return bookingRepository
                .findByEventDateBetweenAndBookingStatusNotIn(
                        ym.atDay(1), ym.atEndOfMonth(),
                        List.of(BOOKING_STATUS.CANCELLED, BOOKING_STATUS.REJECTED))
                .stream().map(bookingMapper::toDto).toList();
    }

    @Override
    public List<BookingRequest> getByCustomerId(UUID id) {
        return bookingRepository.findByCustomer_id(id)
                .stream()
                .sorted(Comparator.comparing(Booking::getCreatedAt).reversed())
                .map(bookingMapper::toDto)
                .toList();
    }
    @Transactional
    @Override
    public BookingRequest updateStatus(UUID id, BOOKING_STATUS status) {
        if (status == BOOKING_STATUS.CONFIRMED_BOOKED) {
            return confirmBooking(id);              // same rules + reject others
        }

        Booking booking = findBooking(id);
        BOOKING_STATUS current = booking.getBookingStatus();

        if (current == status) {
            return bookingMapper.toDto(booking);
        }

        if (current == BOOKING_STATUS.REJECTED
                || current == BOOKING_STATUS.CANCELLED
                || current == BOOKING_STATUS.COMPLETED) {
            throw new PackageException("A " + current + " booking cannot change status.");
        }
        switch (status) {
            case IN_PROGRESS -> throw new PackageException(
                    "A booking cannot be moved back to In Progress.");
            case OWNER_EVENT -> throw new PackageException(
                    "Use the owner event API to block a slot.");
            case COMPLETED -> {
                if (current != BOOKING_STATUS.CONFIRMED_BOOKED) {
                    throw new PackageException(
                            "Only confirmed bookings can be completed.");
                }
            }
            case REJECTED -> {
                if (current != BOOKING_STATUS.IN_PROGRESS) {
                    throw new PackageException(
                            "Only In Progress inquiries can be rejected.");
                }
            }
            default -> { /* CANCELLED: allowed from IN_PROGRESS / CONFIRMED / OWNER_EVENT */ }
        }

        booking.setBookingStatus(status);
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    private Booking findBooking(UUID id) {
        return bookingRepository.findById(id).orElseThrow(
                () -> new PackageException("Booking not found with id: " + id));
    }

    private void validateEventDate(LocalDate date) {
        if (date == null) {
            throw new PackageException("Event date is required.");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new PackageException("Event date cannot be in the past.");
        }
    }

    private Customer getCustomer(BookingRequest request) {
        if (request.getCustomer() == null || request.getCustomer().getId() == null) {
            throw new PackageException("Customer is required.");
        }
        return customerRepository.findById(request.getCustomer().getId())
                .orElseThrow(() -> new PackageException("Customer not found."));
    }

    private PackageTime getPackageTime(BookingRequest request) {
        if (request.getPackageTime() == null || request.getPackageTime().getId() == null) {
            throw new PackageException("Package time is required.");
        }
        return packageTimingRepository.findById(request.getPackageTime().getId())
                .orElseThrow(() -> new PackageException("Package time not found."));
    }

    private PackageCategory getPackageCategory(BookingRequest request) {
        if (request.getPackageCategory() == null || request.getPackageCategory().getId() == null) {
            throw new PackageException("Package is required.");
        }
        return packageCategoryRepository.findById(request.getPackageCategory().getId())
                .orElseThrow(() -> new PackageException("Package not found."));
    }

    private boolean isSlotBlocked(LocalDate date, UUID packageTimeId, UUID excludeId) {
        return bookingRepository.existsBlockingBooking(
                date, packageTimeId, BLOCKING_STATUSES,
                excludeId != null ? excludeId : NO_EXCLUDE);
    }
}