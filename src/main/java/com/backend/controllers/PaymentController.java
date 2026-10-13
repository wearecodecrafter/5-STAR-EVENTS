package com.backend.controllers;

import com.backend.models.requests.payment.PaymentRequest;
import com.backend.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody PaymentRequest request){
        return new ResponseEntity<>(paymentService.createPayment(request), HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updatePayment(@RequestParam UUID id, @RequestBody PaymentRequest request){
        return new ResponseEntity<>(paymentService.updatePayment(id,request),HttpStatus.OK);
    }

    @GetMapping("/get-by-id")
    public ResponseEntity<?> getById(@RequestParam UUID id){
        return new ResponseEntity<>(paymentService.getPaymentById(id),HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam UUID id){
        return new ResponseEntity<>(paymentService.deletePayment(id),HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchPayment(@RequestParam String keyword,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size
                                           ){
        return new ResponseEntity<>(paymentService.searchPayments(keyword,page,size ),HttpStatus.OK );
    }

}
