package com.backend.services.impl;

import com.backend.models.response.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaginationResponseImpl {

    public <T> PaginatedResponse<T> getPaginatedResponse(Page<T> page){
        PaginatedResponse<T> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setContent(page.getContent());
        paginatedResponse.setPageNumber(page.getNumber());
        paginatedResponse.setPageSize(page.getSize());
        paginatedResponse.setTotalElements(page.getTotalElements());
        paginatedResponse.setTotalPages(page.getTotalPages());
        paginatedResponse.setLast(page.isLast());
        return paginatedResponse;
    }

    public <T> PaginatedResponse<T> buildPaginatedResponse(List<T> content, Page<?> pageData) {
        PaginatedResponse<T> response = new PaginatedResponse<>();
        response.setContent(content);
        response.setPageNumber(pageData.getNumber());
        response.setPageSize(pageData.getSize());
        response.setTotalElements(pageData.getTotalElements());
        response.setTotalPages(pageData.getTotalPages());
        response.setLast(pageData.isLast());
        return response;
    }


}

