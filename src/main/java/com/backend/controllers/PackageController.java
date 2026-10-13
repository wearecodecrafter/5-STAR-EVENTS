package com.backend.controllers;

import com.backend.models.requests.pacakage.PackageCategoryRequest;
import com.backend.services.PackageCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/package")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PackageController {

    private final PackageCategoryService packageService;

    @PostMapping("/create")
    public ResponseEntity<?> createPackage(@RequestBody PackageCategoryRequest request){
        return new ResponseEntity<>(packageService.cratePackageCategory(request), HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updatePackage(@RequestParam UUID id,  @RequestBody PackageCategoryRequest request){
        return new ResponseEntity<>(packageService.updatePackageCategory(id, request), HttpStatus.OK);
    }

    @GetMapping("/get-by-id")
    public ResponseEntity<?> getById(@RequestParam UUID id){
        return new ResponseEntity<>(packageService.getById(id), HttpStatus.OK);
    }
    @DeleteMapping("/delete-by-id")
    public ResponseEntity<?> deletePackage(@RequestParam UUID id){
        return new ResponseEntity<>(packageService.deletePackage(id), HttpStatus.OK);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllPackages(){
        return new ResponseEntity<>(packageService.getAllPackageCategories(), HttpStatus.OK);
    }
}

