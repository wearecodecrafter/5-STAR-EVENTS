package com.backend.services;

import com.backend.models.enums.BOOKING_STATUS;
import com.backend.models.requests.booking.BookingRequest;
import com.backend.models.response.PaginatedResponse;

import java.util.List;
import java.util.UUID;

public interface BookingService {

    BookingRequest createBooking(BookingRequest request);

    BookingRequest updateBooking(UUID id, BookingRequest request);

    BookingRequest getBookingById(UUID id);

    Boolean deleteBooking(UUID id);

    PaginatedResponse<BookingRequest> getAllBookings(int page, int size);

    BookingRequest updateStatus(UUID id, BOOKING_STATUS status);

    BookingRequest confirmBooking(UUID id);
    BookingRequest createOwnerEvent(BookingRequest request);
    List<BookingRequest> getBookingsByMonth(int year, int month);

}
