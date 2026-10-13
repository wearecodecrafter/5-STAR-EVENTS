package com.backend.services.impl;

import com.backend.exception.CustomerException;
import com.backend.mapper.CustomerMapper;
import com.backend.models.Customer;
import com.backend.models.requests.CustomerRequest;
import com.backend.models.response.PaginatedResponse;
import com.backend.repositories.CustomerRepository;
import com.backend.services.CustomerService;
import com.backend.services.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final CustomerResolver customerResolver;

    @Transactional
    @Override
    public CustomerRequest createCustomer(CustomerRequest request) {

        String phone = AppUserDetailsService.normalizePhone(request.getPhoneNumber());

        // explicit "create": an existing number is an error (resolve would silently reuse it)
        if (phone != null && customerRepository.findByPhoneNumber(phone).isPresent()) {
            throw new CustomerException("Customer with phone number " + phone + " already exists");
        }

        // an id here would make resolve() return that customer instead of creating one
        request.setId(null);

        // validates name and phone, creates the customer and assigns the CUSTOMER role
        Customer customer;
        try {
            customer = customerResolver.resolve(request);
        } catch (DataIntegrityViolationException e) {
            throw new CustomerException("Customer with phone number " + phone + " already exists");
        }

        // optional password, set only when the owner provides one
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            customer.setPassword(passwordEncoder.encode(request.getPassword()));
            customer = customerRepository.save(customer);
        }

        return customerMapper.toDto(customer);
    }

    @Override
    public CustomerRequest getCustomerById(UUID id) {
        return customerRepository.findById(id).map(customerMapper::toDto).orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }

    @Transactional
    @Override
    public CustomerRequest updateCustomer(UUID id, CustomerRequest request) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerException("Customer not found with id: " + id));

        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new CustomerException("Full name is required.");
        }

        String phone = AppUserDetailsService.normalizePhone(request.getPhoneNumber());
        if (phone == null || phone.length() < 10) {
            throw new CustomerException("Valid phone number is required.");
        }

        // another customer must not already use this number
        customerRepository.findByPhoneNumber(phone)
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> {
                    throw new CustomerException("Customer with phone number " + phone + " already exists");
                });

        // phone number is the login username: if it changes, the old token/login stops matching
        boolean phoneChanged = !phone.equals(customer.getPhoneNumber());

        customerMapper.update(request, customer);   // id, password, role, bookings are ignored by the mapper
        customer.setFullName(request.getFullName().trim());
        customer.setPhoneNumber(phone);

        try {
            Customer saved = customerRepository.saveAndFlush(customer);
            return customerMapper.toDto(saved);
        } catch (DataIntegrityViolationException e) {
            throw new CustomerException("Customer with phone number " + phone + " already exists");
        }
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
