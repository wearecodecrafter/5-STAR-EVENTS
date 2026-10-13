package com.backend.controllers;

import com.backend.models.requests.quotation.QuotationRequest;
import com.backend.models.response.PaginatedResponse;
import com.backend.services.QuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quotation")
@CrossOrigin("*")
public class QuotationController {

    private final QuotationService quotationService;
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody QuotationRequest request) {
        return new ResponseEntity<>(quotationService.createQuotation(request), HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestParam UUID id, @RequestBody QuotationRequest request) {
        return new ResponseEntity<>(quotationService.updateQuotation(id, request),HttpStatus.OK);
    }
    @GetMapping("/get-all")
    public ResponseEntity<?> getAllQuotation(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        return new ResponseEntity<>(quotationService.findAll(page,size),HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteQuotation(@RequestParam UUID id){
        return new ResponseEntity<>(quotationService.deleteQuotation(id),HttpStatus.OK);
    }

    @GetMapping("/get-by-customer")
    public ResponseEntity<?> findByCustomer(@RequestParam UUID id,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size){
        return new ResponseEntity<>(quotationService.findByCustomer(id,page,size),HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<QuotationRequest>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                quotationService.search(keyword, page, size)
        );
    }
}
