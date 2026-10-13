package com.backend.controllers;

import com.backend.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
@CrossOrigin("*")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/get-data")
    public ResponseEntity<?> getData(@RequestParam int month, @RequestParam int year){
        return new ResponseEntity<>(dashboardService.getDashboard(month,year), HttpStatus.OK);
    }
}
