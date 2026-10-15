package com.backend.services.impl;

import com.backend.exception.CustomerException;
import com.backend.models.Customer;
import com.backend.models.Role;
import com.backend.models.requests.CustomerRequest;
import com.backend.repositories.CustomerRepository;
import com.backend.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CustomerResolver {

    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * id present      -> use that existing customer
     * id missing      -> create a new customer from fullName + phoneNumber
     *                    (or reuse the customer who already has that phone number)
     */
    @Transactional
    public Customer resolve(CustomerRequest req) {

        if (req == null) {
            throw new CustomerException("Customer is required.");
        }

        // 1. Existing customer picked by the owner
        if (req.getId() != null) {
            return customerRepository.findById(req.getId())
                    .orElseThrow(() -> new CustomerException("Customer not found."));
        }

        // 2. New customer
        if (req.getFullName() == null || req.getFullName().isBlank()) {
            throw new CustomerException("Customer name is required.");
        }

        String phone = AppUserDetailsService.normalizePhone(req.getPhoneNumber());

        if (phone == null || phone.length() < 10) {
            throw new CustomerException("Valid customer phone number is required.");
        }

        // 3. Reuse existing customer if phone number already exists
        return customerRepository.findByPhoneNumber(phone).orElseGet(() -> {

            Customer customer = new Customer();
            customer.setFullName(req.getFullName().trim());
            customer.setPhoneNumber(phone);
            customer.setEmail(req.getEmail());
            customer.setPassword(passwordEncoder.encode(req.getPassword()));

            // 4. Assign CUSTOMER role
            Role customerRole = roleRepository.findByRoleName("CUSTOMER")
                    .orElseThrow(() ->
                            new CustomerException("CUSTOMER role not found. Please create it first.")
                    );

            customer.setRole(customerRole);
            // Save customer with CUSTOMER role
            return customerRepository.saveAndFlush(customer);
        });
    }
}