package com.backend.repositories;

import com.backend.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID>,
        JpaSpecificationExecutor<Customer> {
    Optional<Customer> findByPhoneNumberContaining(String phoneNumber);

    Optional<Customer> findByPhoneNumber(String phoneNumber);

    // CustomerRepository
    Optional<Customer> findByEmailIgnoreCase(String email);
}
