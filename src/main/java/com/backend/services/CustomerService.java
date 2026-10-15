package com.backend.services;

import com.backend.models.Customer;
import com.backend.models.requests.CustomerRequest;
import com.backend.models.response.PaginatedResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    CustomerRequest createCustomer(CustomerRequest request);

    CustomerRequest getCustomerById(UUID id);

    CustomerRequest updateCustomer(UUID id, CustomerRequest request);

    Boolean deleteCustomer(UUID id);

    CustomerRequest searchCustomerByPhoneNumber(String keyword);

    PaginatedResponse<CustomerRequest> getAllCustomers(int page, int size);

    CustomerRequest forgetPassword();

    Page<Customer> searchCustomers(
            String keyword,
            int page,
            int size);
}
