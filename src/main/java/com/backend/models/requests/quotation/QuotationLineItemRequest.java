package com.backend.models.requests.quotation;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Data
public class QuotationLineItemRequest {

    private UUID id;
    private String description;
    private Integer quantity;
    private Double rate;
}
