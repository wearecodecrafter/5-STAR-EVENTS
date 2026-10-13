package com.backend.models.quotation;

import com.backend.models.Customer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Quotation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private String eventNature;

    private LocalDate eventDate;

    private String eventVenue;

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuotationLineItem> quotationLineItem;

    private Double subTotal;

    private Double discount;

    private Integer tax;

    private Double total;

    private Double advanceRequired;

    private Double remainingBalance;
}
