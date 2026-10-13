package com.backend.repositories;

import com.backend.models.Booking;
import com.backend.models.enums.BOOKING_STATUS;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {


    @Query("""
    SELECT COUNT(b)
    FROM Booking b
    WHERE MONTH(b.createdAt) = :month
      AND YEAR(b.createdAt) = :year
      AND b.bookingStatus = :status
""")
    Long countCreatedByMonthAndYear(
            @Param("month") int month,
            @Param("year") int year,
            @Param("status") BOOKING_STATUS status
    );
    @Query("""
    SELECT COUNT(DISTINCT b.eventDate)
    FROM Booking b
    WHERE MONTH(b.eventDate) = :month
      AND YEAR(b.eventDate) = :year
""")
    Long countBookedDatesByMonthAndYear(
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("""
    SELECT b
    FROM Booking b
    JOIN FETCH b.customer
    JOIN FETCH b.packageCategory
    WHERE b.eventDate >= :currentDate
      AND b.eventDate <= :endDate
    ORDER BY b.eventDate ASC
""")
    List<Booking> findUpcomingEvents(
            @Param("currentDate") LocalDate currentDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
    select count(b) > 0 from Booking b
    where b.eventDate = :date
      and b.packageTime.id = :timeId
      and b.bookingStatus in :statuses
      and b.id <> :excludeId
""")
    boolean existsBlockingBooking(@Param("date") LocalDate date,
                                  @Param("timeId") UUID timeId,
                                  @Param("statuses") Collection<BOOKING_STATUS> statuses,
                                  @Param("excludeId") UUID excludeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select b from Booking b
    where b.eventDate = :date and b.packageTime.id = :timeId
""")
    List<Booking> findSlotBookingsForUpdate(@Param("date") LocalDate date,
                                            @Param("timeId") UUID timeId);

    boolean existsByEventDateAndPackageTime_IdAndCustomer_IdAndBookingStatus(
            LocalDate date, UUID timeId, UUID customerId, BOOKING_STATUS status);

    List<Booking> findByEventDateBetweenAndBookingStatusNotIn(
            LocalDate start, LocalDate end, Collection<BOOKING_STATUS> statuses);

    @Query("""
    select b from Booking b
    left join fetch b.customer
    join fetch b.packageTime
    where b.eventDate between :start and :end
      and b.bookingStatus not in :excluded
""")
    List<Booking> findCalendarBookings(@Param("start") LocalDate start,
                                       @Param("end") LocalDate end,
                                       @Param("excluded") Collection<BOOKING_STATUS> excluded);

    boolean existsByPackageTime_Id(UUID id);
}
