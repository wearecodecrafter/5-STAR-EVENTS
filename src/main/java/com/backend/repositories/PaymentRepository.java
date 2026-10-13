package com.backend.repositories;

import com.backend.models.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {


    @Query("""
        select coalesce(sum(p.amount), 0) from Payment p
        where p.booking.id = :bookingId and p.id <> :excludeId
    """)
    Double sumPaidForBooking(@Param("bookingId") UUID bookingId,
                             @Param("excludeId") UUID excludeId);

    boolean existsByTransactionIdIgnoreCase(String transactionId);

    boolean existsByTransactionIdIgnoreCaseAndIdNot(String transactionId, UUID id);
}
