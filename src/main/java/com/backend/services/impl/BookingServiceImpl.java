package com.backend.services.impl;

import com.backend.exception.PackageException;
import com.backend.mapper.BookingMapper;
import com.backend.models.Booking;
import com.backend.models.Customer;
import com.backend.models.PackageTime;
import com.backend.models.enums.BOOKING_STATUS;
import com.backend.models.requests.booking.BookingRequest;
import com.backend.repositories.BookingRepository;
import com.backend.repositories.CustomerRepository;
import com.backend.repositories.PackageTimingRepository;
import com.backend.services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final CustomerRepository customerRepository;
    private final PackageTimingRepository packageTimingRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    @Transactional
    @Override
    public BookingRequest createBooking(BookingRequest request) {

        if (request.getEventDate() == null) {
            throw new PackageException("Event date is required.");
        }

        if (request.getCustomer() == null
                || request.getCustomer().getId() == null) {
            throw new PackageException("Customer is required.");
        }

        if (request.getPackageTime() == null
                || request.getPackageTime().getId() == null) {
            throw new PackageException("Package time is required.");
        }

        // Validate customer
        Customer customer = customerRepository
                .findById(request.getCustomer().getId())
                .orElseThrow(() -> new PackageException(
                        "Customer not found."));

        // Validate package time
        PackageTime packageTime = packageTimingRepository
                .findById(request.getPackageTime().getId())
                .orElseThrow(() -> new PackageException(
                        "Package time not found."));

        // Check whether this package time is already booked on this date
        boolean alreadyBooked = bookingRepository.existsActiveBooking(
                request.getEventDate(),
                packageTime.getId(),
                BOOKING_STATUS.CANCELLED);

        if (alreadyBooked) {
            throw new PackageException(
                    "This package time is already booked for the selected date.");
        }
        Booking booking = new Booking();

        booking.setEventDate(request.getEventDate());
        booking.setCustomer(customer);
        booking.setPackageTime(packageTime);
        booking.setPackageCategory(packageTime.getPackageCategory());
        booking.setVenue(request.getVenue());
        booking.setExpectedGuests(request.getExpectedGuests());
        booking.setIsCateringRequired(
                request.getIsCateringRequired() != null
                        ? request.getIsCateringRequired()
                        : false);
        booking.setPackageAmount(request.getPackageAmount());
        booking.setAdvanceAmount(request.getAdvanceAmount());
        booking.setBookingStatus(BOOKING_STATUS.IN_PROGRESS);
        booking.setRemarks(request.getRemarks());
        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toDto(savedBooking);
    }
}
