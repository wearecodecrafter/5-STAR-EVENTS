
package com.backend.controllers;

import com.backend.models.requests.CustomerRequest;
import com.backend.models.requests.password.ForgetPasswordRequest;
import com.backend.models.requests.password.ResetPasswordRequest;
import com.backend.services.CustomerService;
import com.backend.services.impl.CustomerPasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@CrossOrigin("*")
@Tag(name = "CUSTOMER", description = "Customer management APIs")
public class CustomerController {
    private final CustomerService customerService;
    private final CustomerPasswordResetService resetService;

    @PostMapping("/create")
    @Operation(
            summary = "Create customer",
            description = "CUSTOMER API - Used by Android customer app and customer frontend",
            tags = {"CUSTOMER"}
    )
    public ResponseEntity<?> createCustomer(
            @RequestBody CustomerRequest request) {

        return new ResponseEntity<>(
                customerService.createCustomer(request),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/get-by-id")
    @Operation(
            summary = "Get customer by ID",
            description = "ADMIN API - Used by admin panel and authorized staff",
            tags = {"ADMIN"}
    )
    public ResponseEntity<?> getCustomerById(
            @RequestParam UUID id) {

        return ResponseEntity.ok(
                customerService.getCustomerById(id)
        );
    }

    @PutMapping("/update")
    @Operation(
            summary = "Update customer",
            description = "CUSTOMER API - Used by customer frontend and Android app",
            tags = {"CUSTOMER"}
    )
    public ResponseEntity<?> updateCustomer(
            @RequestParam UUID id,
            @RequestBody CustomerRequest request) {

        return ResponseEntity.ok(
                customerService.updateCustomer(id, request)
        );
    }

    @DeleteMapping("/delete")
    @Operation(
            summary = "Delete customer",
            description = "ADMIN API - Used by admin panel and authorized staff",
            tags = {"ADMIN"}
    )
    public ResponseEntity<?> deleteCustomer(
            @RequestParam UUID id) {

        return ResponseEntity.ok(
                customerService.deleteCustomer(id)
        );
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search customers",
            description = "ADMIN API - Search by name, email, or phone number",
            tags = {"ADMIN"}
    )
    public ResponseEntity<?> searchCustomers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                customerService.searchCustomers(keyword, page, size)
        );
    }
    @GetMapping("/get-all")
    @Operation(
            summary = "Get all customers",
            description = "ADMIN API - Get customers with pagination",
            tags = {"ADMIN"}
    )
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                customerService.getAllCustomers(page, size)
        );
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Forgot password",
            description = "CUSTOMER API - Request a password reset OTP",
            tags = {"CUSTOMER"}
    )
    public ResponseEntity<?> forgot(
            @RequestBody ForgetPasswordRequest req) {
        System.out.println("inside controller");
        resetService.requestOtp(req.getEmail());
        System.out.println("otp send");

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "If this email is registered, a code has been sent."
                )
        );
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset password",
            description = "CUSTOMER API - Reset password using the OTP",
            tags = {"CUSTOMER"}
    )
    public ResponseEntity<Map<String, String>> reset(
            @RequestBody ResetPasswordRequest req) {

        resetService.resetPassword(req);

        return ResponseEntity.ok(
                Map.of("message", "Password updated. Please log in.")
        );
    }
}
