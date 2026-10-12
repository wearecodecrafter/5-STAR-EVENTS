package com.backend.controllers;

import com.backend.models.AppUser;
import com.backend.models.requests.UserRequest;
import com.backend.repositories.RoleRepository;
import com.backend.services.AppUserServices;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
@CrossOrigin("*")
public class AppUserController {
    private final AppUserServices appUserServices;
    private final RoleRepository roleRepository;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createUser(@RequestParam("user") String userJson,
                                        @RequestParam(required = false) MultipartFile imageFile) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        UserRequest request = objectMapper.readValue(userJson, UserRequest.class);

        AppUser user = new AppUser();
        user.setName(request.getName());
        user.setPosition(request.getPosition());
        user.setUsername(request.getUsername());
        user.setPassword(new BCryptPasswordEncoder().encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setContact(request.getContact());
        user.setContact(request.getContact());
        user.setIsUserLoggedIn(false);
        user.setIsMultiFactor(false);
        user.setIsActive(request.getIsActive());

        user.setRole(roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role Not Found")));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appUserServices.saveUser(user, imageFile));
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateUser(@RequestParam("user") String userJson,
                                        @RequestParam(required = false) MultipartFile imageFile) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        UserRequest request = objectMapper.readValue(userJson, UserRequest.class);

        AppUser user = new AppUser();
        user.setId(request.getId());
        user.setName(request.getName());
        user.setPosition(request.getPosition());
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setContact(request.getContact());
        user.setContact(request.getContact());
        user.setIsUserLoggedIn(false);
        user.setIsMultiFactor(false);
        user.setIsActive(request.getIsActive());

        user.setRole(roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role Not Found")));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appUserServices.updateAppUser(user, imageFile));
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllUsers(@RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(appUserServices.getAllAppUsers(page, size));
    }

    @GetMapping("/get-by-id")
    public ResponseEntity<?> getUserById(@RequestParam UUID id) {
        return ResponseEntity.ok(appUserServices.getAppUser(id));
    }

    @GetMapping("/get-employee-by-code")
    public ResponseEntity<?> getEmployeeByCode(@RequestParam String employeeCode) {
        return ResponseEntity.ok(appUserServices.getUsersByEmployeeCode(employeeCode));
    }

    @GetMapping("/get-total-active-employee")
    public ResponseEntity<?> getTotalActiveEmployee() {
        return ResponseEntity.ok(appUserServices.getTotalActiveEmployeeCount());
    }
}
