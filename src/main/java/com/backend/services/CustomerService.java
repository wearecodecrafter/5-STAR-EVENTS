package com.backend.services;

import com.backend.models.requests.CustomerRequest;
import com.backend.models.response.PaginatedResponse;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    CustomerRequest createCustomer(CustomerRequest request);

    CustomerRequest getCustomerById(UUID id);

    CustomerRequest updateCustomer(UUID id, CustomerRequest request);

    Boolean deleteCustomer(UUID id);

    CustomerRequest searchCustomerByPhoneNumber(String keyword);

    PaginatedResponse<CustomerRequest> getAllCustomers(int page, int size);
}
