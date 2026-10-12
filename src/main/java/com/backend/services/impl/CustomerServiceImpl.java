package com.backend.services.impl;

import com.backend.exception.CustomerException;
import com.backend.mapper.CustomerMapper;
import com.backend.models.Customer;
import com.backend.models.requests.CustomerRequest;
import com.backend.models.response.PaginatedResponse;
import com.backend.repositories.CustomerRepository;
import com.backend.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final PaginationResponseImpl paginationResponse;

    @Transactional
    @Override
    public CustomerRequest createCustomer(CustomerRequest request) {
        Customer customer = customerMapper.toEntity(request);
        Optional<Customer> byPhoneNumber = customerRepository.findByPhoneNumber(request.getPhoneNumber());
        if(byPhoneNumber.isPresent()){
           throw  new CustomerException("Customer with phone number " + request.getPhoneNumber() + " already exists");
        }
        return customerMapper.toDto(customerRepository.save(customer));
    }

    @Override
    public CustomerRequest getCustomerById(UUID id) {
        return customerRepository.findById(id).map(customerMapper::toDto).orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }

    @Override
    public CustomerRequest updateCustomer(UUID id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        customerMapper.update(request, customer);
        return customerMapper.toDto(customerRepository.save(customer));
    }

    @Override
    public Boolean deleteCustomer(UUID id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        customerRepository.delete(customer);
        return true;
    }

    @Override
    public CustomerRequest searchCustomerByPhoneNumber(String keyword) {
        return customerRepository.findByPhoneNumberContaining(keyword).map(customerMapper::toDto).orElse(null);
    }

    @Override
    public PaginatedResponse<CustomerRequest> getAllCustomers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Customer> all = customerRepository.findAll(pageable);
        List<CustomerRequest> list = all.getContent().stream().map(customerMapper::toDto).toList();
        return paginationResponse.buildPaginatedResponse(list, all);
    }
}
