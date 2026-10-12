package com.backend.services;

import com.backend.models.requests.booking.BookingRequest;

public interface BookingService {

    BookingRequest createBooking(BookingRequest request);

}
