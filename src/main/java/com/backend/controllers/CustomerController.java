
package com.backend.controllers;

import com.backend.models.requests.CustomerRequest;
import com.backend.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/create")
    public ResponseEntity<?> createCustomer(@RequestBody CustomerRequest request) {
        return new ResponseEntity<>(customerService.createCustomer(request),HttpStatus.CREATED);
    }

    @GetMapping("/get-by-id")
    public ResponseEntity<?> getCustomerById(@RequestParam UUID id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateCustomer(
            @RequestParam UUID id,
            @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request)
        );
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteCustomer(@RequestParam UUID id) {
        return ResponseEntity.ok(customerService.deleteCustomer(id));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchCustomerByPhoneNumber(@RequestParam String keyword) {
        return ResponseEntity.ok(customerService.searchCustomerByPhoneNumber(keyword)
        );
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllCustomers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(customerService.getAllCustomers(page, size));
    }
}
