package com.backend.services.impl;

import com.backend.exception.PackageException;
import com.backend.mapper.QuotationMapper;
import com.backend.models.Customer;
import com.backend.models.quotation.Quotation;
import com.backend.models.quotation.QuotationLineItem;
import com.backend.models.requests.quotation.QuotationLineItemRequest;
import com.backend.models.requests.quotation.QuotationRequest;
import com.backend.models.response.PaginatedResponse;
import com.backend.repositories.CustomerRepository;
import com.backend.repositories.QuotationRepository;
import com.backend.services.QuotationService;
import com.backend.specification.QuotationSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuotationServiceImpl implements QuotationService {

    private final QuotationRepository quotationRepository;
    private final CustomerRepository customerRepository;
    private final QuotationMapper quotationMapper;
    private final PaginationResponseImpl paginationResponse;

    @Transactional
    @Override
    public QuotationRequest createQuotation(QuotationRequest request) {
        validate(request);
        Customer customer = getCustomer(request);
        Quotation quotation = quotationMapper.toEntity(request);
        quotation.setCustomer(customer);
        quotation.setQuotationLineItem(new ArrayList<>());

        applyLineItemsAndTotals(quotation,request);

        return quotationMapper.toDto(quotationRepository.save(quotation));
    }

    @Transactional
    @Override
    public QuotationRequest updateQuotation(UUID id, QuotationRequest request) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new PackageException("Quotation not found with id: " + id));

        validate(request);
        Customer customer = getCustomer(request);

        quotationMapper.update(request, quotation);
        quotation.setCustomer(customer);

        applyLineItemsAndTotals(quotation, request);

        return quotationMapper.toDto(quotationRepository.save(quotation));
    }

    @Override
    public PaginatedResponse<QuotationRequest> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        Page<Quotation> all = quotationRepository.findAll(pageable);
        List<QuotationRequest> list = all.getContent().stream().map(quotationMapper::toDto).toList();
        return paginationResponse.buildPaginatedResponse(list,all);
    }

    @Override
    public Boolean deleteQuotation(UUID id) {
        Quotation quotation = quotationRepository.findById(id).orElseThrow(() -> new RuntimeException("Quotation not found with id: " + id));
        quotationRepository.delete(quotation);
        return true;
    }

    @Override
    public PaginatedResponse<QuotationRequest> findByCustomer(UUID id, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Quotation> all = quotationRepository.findByCustomer_Id(id, pageable);

        List<QuotationRequest> list = all.getContent()
                .stream()
                .map(quotationMapper::toDto)
                .toList();

        return paginationResponse.buildPaginatedResponse(list, all);
    }

    @Override
    public PaginatedResponse<QuotationRequest> search(
            String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Specification<Quotation> specification =
                QuotationSpecification.search(keyword);

        Page<Quotation> all =
                quotationRepository.findAll(specification, pageable);

        List<QuotationRequest> list = all.getContent()
                .stream()
                .map(quotationMapper::toDto)
                .toList();

        return paginationResponse.buildPaginatedResponse(list, all);
    }

    private void validate(QuotationRequest request) {
        if (request.getEventDate() == null) {
            throw new PackageException("Event date is required.");
        }
        if (request.getEventNature() == null || request.getEventNature().isBlank()) {
            throw new PackageException("Event nature is required.");
        }
        if (request.getQuotationLineItem() == null || request.getQuotationLineItem().isEmpty()) {
            throw new PackageException("At least one line item is required.");
        }
        for (QuotationLineItemRequest item : request.getQuotationLineItem()) {
            if (item.getDescription() == null || item.getDescription().isBlank()) {
                throw new PackageException("Line item description is required.");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new PackageException("Line item quantity must be greater than 0.");
            }
            if (item.getRate() == null || item.getRate() < 0) {
                throw new PackageException("Line item rate cannot be negative.");
            }
        }
    }

    private Customer getCustomer(QuotationRequest request) {
        if (request.getCustomer() == null || request.getCustomer().getId() == null) {
            throw new PackageException("Customer is required.");
        }
        return customerRepository.findById(request.getCustomer().getId())
                .orElseThrow(() -> new RuntimeException("Customer not found."));
    }

    /**
     * Rebuilds the line items IN PLACE (required for orphanRemoval) and
     * recalculates all amounts on the server.
     *
     * Assumes: tax = percentage applied after discount.
     *   subTotal = sum(quantity * rate)
     *   total    = (subTotal - discount) + tax% of (subTotal - discount)
     *   remaining = total - advanceRequired
     */
    private void applyLineItemsAndTotals(Quotation quotation, QuotationRequest request) {
        List<QuotationLineItem> items = quotation.getQuotationLineItem();
        if (items == null) {
            items = new ArrayList<>();
            quotation.setQuotationLineItem(items);
        }
        items.clear();   // old rows removed by orphanRemoval

        double subTotal = 0;
        for (QuotationLineItemRequest dto : request.getQuotationLineItem()) {
            QuotationLineItem item = quotationMapper.toLineItem(dto);
            item.setQuotation(quotation);              // set the owning side
            items.add(item);
            subTotal += item.getQuantity() * item.getRate();
        }
        subTotal = round(subTotal);

        double discount = request.getDiscount() != null ? request.getDiscount() : 0;
        int taxPercent = request.getTax() != null ? request.getTax() : 0;
        double advance = request.getAdvanceRequired() != null ? request.getAdvanceRequired() : 0;

        if (discount < 0 || discount > subTotal) {
            throw new PackageException("Discount must be between 0 and the sub total.");
        }
        if (taxPercent < 0 || taxPercent > 100) {
            throw new PackageException("Tax must be between 0 and 100.");
        }

        double taxable = subTotal - discount;
        double total = round(taxable + taxable * taxPercent / 100.0);

        if (advance < 0 || advance > total) {
            throw new PackageException("Advance must be between 0 and the total.");
        }

        quotation.setSubTotal(subTotal);
        quotation.setDiscount(discount);
        quotation.setTax(taxPercent);
        quotation.setTotal(total);
        quotation.setAdvanceRequired(advance);
        quotation.setRemainingBalance(round(total - advance));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
