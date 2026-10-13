package com.backend.specification;

import com.backend.models.Booking;
import com.backend.models.Customer;
import com.backend.models.payment.Payment;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
public final class PaymentSpecs {

    private PaymentSpecs() {}

    public static Specification<Payment> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();                 // no keyword -> return everything
            }
            String k = keyword.trim();
            String like = "%" + k.toLowerCase() + "%";

            Join<Payment, Booking> booking = root.join("booking", JoinType.LEFT);
            Join<Booking, Customer> cust = booking.join("customer", JoinType.LEFT);

            List<Predicate> any = new ArrayList<>();
            any.add(cb.like(cb.lower(cust.get("fullName")), like));
            any.add(cb.like(cust.get("phoneNumber"), "%" + k + "%"));
            any.add(cb.like(cb.lower(root.get("paymentType")), like));
            any.add(cb.like(cb.lower(root.get("paymentMethod")), like));
            any.add(cb.like(cb.lower(root.get("transactionId")), like));

            // booking id: only an exact match, and only if the keyword is a valid UUID
            try {
                any.add(cb.equal(booking.get("id"), UUID.fromString(k)));
            } catch (IllegalArgumentException ignored) {
                // not a UUID, skip
            }

            return cb.or(any.toArray(new Predicate[0]));   // OR across all fields
        };
    }
}