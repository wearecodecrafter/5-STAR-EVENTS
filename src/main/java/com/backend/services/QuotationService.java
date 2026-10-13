package com.backend.services;

import com.backend.models.requests.quotation.QuotationRequest;
import com.backend.models.response.PaginatedResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface QuotationService {

    QuotationRequest createQuotation(QuotationRequest request);
    QuotationRequest updateQuotation(UUID id, QuotationRequest request);

    PaginatedResponse<QuotationRequest> findAll(int page, int size);

    Boolean deleteQuotation(UUID id);

    PaginatedResponse<QuotationRequest> findByCustomer(UUID id, int page, int size);

    PaginatedResponse<QuotationRequest> search(
            String keyword, int page, int size);
}
