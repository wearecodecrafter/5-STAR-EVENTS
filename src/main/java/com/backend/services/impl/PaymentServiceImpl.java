package com.backend.services.impl;

import com.backend.exception.PackageException;
import com.backend.mapper.PaymentMapper;
import com.backend.models.Booking;
import com.backend.models.enums.BOOKING_STATUS;
import com.backend.models.payment.Payment;
import com.backend.models.requests.payment.PaymentRequest;
import com.backend.models.response.PaginatedResponse;
import com.backend.repositories.BookingRepository;
import com.backend.repositories.PaymentRepository;
import com.backend.services.PaymentService;
import com.backend.specification.PaymentSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final UUID NO_EXCLUDE = new UUID(0L, 0L);

    private static final List<BOOKING_STATUS> NOT_PAYABLE = List.of(
            BOOKING_STATUS.CANCELLED, BOOKING_STATUS.REJECTED, BOOKING_STATUS.OWNER_EVENT);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentMapper paymentMapper;
    private final PaginationResponseImpl paginationResponse;

    @Transactional
    @Override
    public PaymentRequest createPayment(PaymentRequest request) {
        if (request.getBookingId() == null) {
            throw new PackageException("Booking is required.");
        }
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new PackageException("Booking not found."));

        validate(request, booking, NO_EXCLUDE);

        Payment payment = paymentMapper.toEntity(request);
        payment.setBooking(booking);
        normalize(payment);
        if(request.getPaymentDate() == null){
            payment.setPaymentDate(LocalDateTime.now());
        }
        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Transactional
    @Override
    public PaymentRequest updatePayment(UUID id, PaymentRequest request) {
        Payment payment = findPayment(id);

        validate(request, payment.getBooking(), id);

        paymentMapper.update(request, payment);
        normalize(payment);
        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    @Override
    public PaymentRequest getPaymentById(UUID id) {
        return paymentMapper.toDto(findPayment(id));
    }

    @Transactional
    @Override
    public Boolean deletePayment(UUID id) {
        paymentRepository.delete(findPayment(id));
        return true;
    }

    @Transactional(readOnly = true)
    @Override
    public PaginatedResponse<PaymentRequest> searchPayments(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));
        Page<Payment> result = paymentRepository.findAll(PaymentSpecs.keyword(keyword), pageable);
        List<PaymentRequest> list = result.getContent().stream().map(paymentMapper::toDto).toList();
        return paginationResponse.buildPaginatedResponse(list, result);
    }
    // ------------------------------------------------------------------
    // HELPERS
    // ------------------------------------------------------------------
    private Payment findPayment(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    private void validate(PaymentRequest request, Booking booking, UUID excludePaymentId) {
        if (NOT_PAYABLE.contains(booking.getBookingStatus())) {
            throw new PackageException(
                    "Payments cannot be added to a " + booking.getBookingStatus() + " booking.");
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new PackageException("Amount must be greater than 0.");
        }
        if (request.getPaymentType() == null || request.getPaymentType().isBlank()) {
            throw new PackageException("Payment type is required.");
        }
        if (request.getPaymentMethod() == null || request.getPaymentMethod().isBlank()) {
            throw new PackageException("Payment method is required.");
        }

        // transaction id must be unique (when provided)
        String txn = request.getTransactionId();
        if (txn != null && !txn.isBlank()) {
            boolean duplicate = excludePaymentId.equals(NO_EXCLUDE)
                    ? paymentRepository.existsByTransactionIdIgnoreCase(txn.trim())
                    : paymentRepository.existsByTransactionIdIgnoreCaseAndIdNot(txn.trim(), excludePaymentId);
            if (duplicate) {
                throw new PackageException("This transaction id already exists.");
            }
        }

        // total paid must not exceed the package amount
        if (booking.getPackageAmount() != null) {
            double alreadyPaid = paymentRepository.sumPaidForBooking(booking.getId(), excludePaymentId);
            if (alreadyPaid + request.getAmount() > booking.getPackageAmount() + 0.001) {
                throw new PackageException("Total payments (" + (alreadyPaid + request.getAmount())
                        + ") cannot exceed the package amount (" + booking.getPackageAmount() + ").");
            }
        }
    }

    private void normalize(Payment payment) {
        payment.setPaymentType(payment.getPaymentType().trim().toUpperCase());
        payment.setPaymentMethod(payment.getPaymentMethod().trim().toUpperCase());
        if (payment.getTransactionId() != null) {
            String t = payment.getTransactionId().trim();
            payment.setTransactionId(t.isEmpty() ? null : t);   // blank -> null
        }
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }
    }
}