package com.backend.controllers;

import com.backend.models.enums.BOOKING_STATUS;
import com.backend.models.enums.CalendarView;
import com.backend.models.enums.SlotStatus;
import com.backend.models.requests.booking.BookingRequest;
import com.backend.models.response.calender.CalendarResponse;
import com.backend.services.BookingService;
import com.backend.services.CalenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
@CrossOrigin("*")
public class BookingController {

    private final BookingService bookingService;
    private final CalenderService calenderService;

    @PostMapping("/create")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request){
        return new ResponseEntity<>(bookingService.createBooking(request), HttpStatus.CREATED);
    }

    @PostMapping("/create-owner-booking")
    public ResponseEntity<?> createOwnerBooking(@RequestBody BookingRequest request){
        return new ResponseEntity<>(bookingService.createOwnerEvent(request), HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateBooking(@RequestParam UUID id, @RequestBody BookingRequest request){
        return new ResponseEntity<>(bookingService.updateBooking(id,request), HttpStatus.OK);
    }

    @GetMapping("/get-by-id")
    public ResponseEntity<?> getBookingById(@RequestParam UUID id){
        return new ResponseEntity<>(bookingService.getBookingById(id), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteBooking(@RequestParam UUID id){
        return new ResponseEntity<>(bookingService.deleteBooking(id), HttpStatus.OK);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllBookings(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        return new ResponseEntity<>(bookingService.getAllBookings(page,size), HttpStatus.OK);
    }

    @PutMapping("/update-status")
    public ResponseEntity<?> updateStatus(@RequestParam UUID id, @RequestParam BOOKING_STATUS status){
        return new ResponseEntity<>(bookingService.updateStatus(id,status), HttpStatus.OK);
    }

    @GetMapping("/calendar")
    public CalendarResponse calendar(
            @RequestParam(defaultValue = "MONTH") CalendarView view,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) SlotStatus status) {
        return calenderService.getCalendar(view, date, status);
    }
}
