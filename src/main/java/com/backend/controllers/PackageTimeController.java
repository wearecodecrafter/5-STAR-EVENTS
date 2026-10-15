package com.backend.controllers;

import com.backend.models.requests.pacakage.PackageTimeRequest;
import com.backend.services.PackageTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/package-slot")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PackageTimeController {

    private final PackageTimeService packageTimeService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody PackageTimeRequest request)
    {
        return new ResponseEntity<>(packageTimeService.create(request), HttpStatus.CREATED    );
    }
    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestParam UUID id, @RequestBody PackageTimeRequest request) {
        return new ResponseEntity<>(packageTimeService.update(id, request),HttpStatus.OK);
    }
    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam UUID id) {
        return new ResponseEntity<>(packageTimeService.delete(id),HttpStatus.OK);
    }
    @GetMapping("/get-all")
    public ResponseEntity<?> getAllPackageTimes() {
        return new ResponseEntity<>( packageTimeService.getAll(),HttpStatus.OK);
    }
}