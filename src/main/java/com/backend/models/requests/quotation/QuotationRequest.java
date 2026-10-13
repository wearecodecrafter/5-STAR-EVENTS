package com.backend.models.requests.quotation;

import com.backend.models.requests.CustomerRequest;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class QuotationRequest {

    private UUID id;

    private CustomerRequest customer;

    private String eventNature;

    private LocalDate eventDate;

    private String eventVenue;

    private List<QuotationLineItemRequest> quotationLineItem;

    private Double subTotal;

    private Double discount;

    private Integer tax;

    private Double total;

    private Double advanceRequired;

    private Double remainingBalance;
}
