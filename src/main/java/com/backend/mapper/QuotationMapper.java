package com.backend.mapper;

import com.backend.models.quotation.Quotation;
import com.backend.models.quotation.QuotationLineItem;
import com.backend.models.requests.quotation.QuotationLineItemRequest;
import com.backend.models.requests.quotation.QuotationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring",uses = {CustomerMapper.class})
public interface QuotationMapper {
    QuotationRequest toDto(Quotation entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "quotationLineItem", ignore = true)
    @Mapping(target = "subTotal", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "remainingBalance", ignore = true)
    Quotation toEntity(QuotationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "quotationLineItem", ignore = true)
    @Mapping(target = "subTotal", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "remainingBalance", ignore = true)
    void update(QuotationRequest request, @MappingTarget Quotation entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "quotation", ignore = true)
    QuotationLineItem toLineItem(QuotationLineItemRequest request);
}
