package com.backend.repositories;

import com.backend.models.Booking;
import com.backend.models.enums.BOOKING_STATUS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @Query("""
    SELECT COUNT(b) > 0
    FROM Booking b
    WHERE b.eventDate = :eventDate
      AND b.packageTime.id = :packageTimeId
      AND b.bookingStatus <> :cancelledStatus
    """)
    boolean existsActiveBooking(
            @Param("eventDate") LocalDate eventDate,
            @Param("packageTimeId") UUID packageTimeId,
            @Param("cancelledStatus") BOOKING_STATUS cancelledStatus);
}
