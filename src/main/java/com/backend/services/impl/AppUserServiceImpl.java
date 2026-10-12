package com.backend.services.impl;


import com.backend.models.AppUser;
import com.backend.models.requests.VerifyOTP;
import com.backend.models.response.ApiResponse;
import com.backend.models.response.PaginatedResponse;
import com.backend.models.response.UserResponse;
import com.backend.repositories.AppUserRepository;
import com.backend.services.AppUserServices;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AppUserServiceImpl implements AppUserServices {
    private final AppUserRepository appUserRepository;
    private final ImageUploadServiceImpl imageUploadService;
    private final PaginationResponseImpl paginationResponse;
    private final RoleServiceImpl roleService;
    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();


    @Transactional
    @Override
    public Boolean saveUser(AppUser user, MultipartFile imageFile) throws IOException {
        if (appUserRepository.getMyUserByUsername(user.getUsername()).isPresent())
            throw new RuntimeException("User already present with this username");
        if (imageFile != null && !imageFile.isEmpty()) {
            user.setImageUrl(imageUploadService.uploadFile(imageFile));
        }
        user.setEmployeeCode(generateEmployeeCode());
        AppUser saved = appUserRepository.save(user);
        return saved != null;
    }

    public String generateEmployeeCode() {
        String prefix = "EMP-";
        long count = appUserRepository.count() + 1;
        return prefix + String.format("%05d", count);
    }

    public UserResponse userResponseHelper(AppUser user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setName(user.getName());
        userResponse.setPosition(user.getPosition());
        userResponse.setEmployeeCode(user.getEmployeeCode());
        userResponse.setEmail(user.getEmail());
        userResponse.setSecretKey(user.getSecretKey());
        userResponse.setIsUserLoggedIn(user.getIsUserLoggedIn());
        userResponse.setUsername(user.getUsername());
        userResponse.setContact(user.getContact());
        userResponse.setPassword(user.getPassword());
        userResponse.setImageUrl(user.getImageUrl());
        userResponse.setIsMultiFactor(user.getIsMultiFactor());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());
        userResponse.setIsActive(user.getIsActive());
        userResponse.setRoleDto(user.getRole() != null ? roleService.roleResponse(user.getRole()) : null);

        return userResponse;
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        Optional<AppUser> myUserByUsername = appUserRepository.getMyUserByUsername(username);
        return myUserByUsername.map(this::userResponseHelper).orElse(null);
    }

    @Override
    public List<UserResponse> getUsersByRoleName(String roleName) {
        return appUserRepository.findAll()
                .stream().filter(user -> user.getRole() != null && user.getRole().getRoleName().equalsIgnoreCase(roleName))
                .map(this::userResponseHelper).toList();
    }

    @Override
    public UserResponse getUsersByEmployeeCode(String employeeCode) {
        Optional<AppUser> appUserByEmployeeCode = appUserRepository.getAppUserByEmployeeCode(employeeCode);
        return appUserByEmployeeCode.map(this::userResponseHelper).orElse(null);
    }

    @Override
    public List<UserResponse> getAllHrs() {
        List<AppUser> hr = appUserRepository.getAllByRoleRoleName("HR");
        return hr.stream().map(this::userResponseHelper).toList();
    }

    @Override
    public UserResponse getAppUser(UUID id) {
        Optional<AppUser> myUserByUsername = appUserRepository.findById(id);
        return myUserByUsername.map(this::userResponseHelper).orElse(null);
    }

    @Override
    public List<UserResponse> searchUser(String keyword) {
        Specification<AppUser> specification = (root, query, criteriaBuilder) -> {
            String likePattern = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("contact")), likePattern)

            );
        };
        return appUserRepository.findAll(specification).stream().map(this::userResponseHelper).toList();
    }

    @Override
    public PaginatedResponse<UserResponse> getAllAppUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AppUser> userPage = appUserRepository.findAll(pageable);
        List<UserResponse> userList = userPage.getContent().stream().map(this::userResponseHelper).toList();
        return paginationResponse.buildPaginatedResponse(userList, userPage);
    }

    @Transactional
    @Override
    public Boolean updateAppUser(AppUser user, MultipartFile imageFile) throws IOException {
        Optional<AppUser> userOptional = appUserRepository.findById(user.getId());
        if (userOptional.isPresent()) {
            AppUser appUser = userOptional.get();
            appUser.setId(user.getId());
            appUser.setName(user.getName());
            appUser.setEmail(user.getEmail());
            appUser.setUsername(user.getUsername());
            appUser.setIsActive(user.getIsActive());
            if (user.getPassword() != null && !user.getPassword().isEmpty())
                appUser.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
            appUser.setContact(user.getContact());
            appUser.setPosition(user.getPosition());
            appUser.setRole(user.getRole() != null ? user.getRole() : appUser.getRole());
            if (imageFile != null && !imageFile.isEmpty())
                appUser.setImageUrl(imageUploadService.uploadFile(imageFile));
            appUserRepository.save(appUser);
            return true;
        }
        return false;
    }

    @Transactional
    @Override
    public Boolean updateAppEmpUser(AppUser user, MultipartFile imageFile) throws IOException {
        Optional<AppUser> userOptional = appUserRepository.findById(user.getId());
        if (userOptional.isPresent()) {
            AppUser appUser = userOptional.get();

            appUser.setId(user.getId());
            appUser.setName(user.getName());
            appUser.setEmail(user.getEmail());
            appUser.setContact(user.getContact());

            if (imageFile != null && !imageFile.isEmpty())
                appUser.setImageUrl(imageUploadService.uploadFile(imageFile));
            appUserRepository.updateEmpUser(appUser.getName(), appUser.getEmail(), appUser.getContact(), appUser.getId());
            return true;
        }
        return false;
    }

    @Override
    public ApiResponse<?> activeUser(UUID id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("user not found"));
        if (user.getIsActive()) {
            user.setIsActive(false);
            appUserRepository.save(user);
            return new ApiResponse<>(true, "user activated", true, HttpStatus.OK);
        } else {
            user.setIsActive(true);
            appUserRepository.save(user);
            return new ApiResponse<>(true, "user de-activated", true, HttpStatus.OK);
        }
    }

    @Override
    public ApiResponse<?> diActive(UUID id) {
        AppUser user = appUserRepository.findById(id).orElseThrow(() -> new RuntimeException("user not found"));
        if (user.getIsActive()) {
            user.setIsActive(false);
            appUserRepository.save(user);
            return new ApiResponse<>(true, "user de-activated", true, HttpStatus.OK);
        }
        return new ApiResponse<>(false, "user not de-activated", null, HttpStatus.BAD_REQUEST);
    }


    // ======= generate Authentication Key.==============


    @Override
    public Boolean isOtpVerify(VerifyOTP verifyOTP) {
        Optional<AppUser> user = appUserRepository.getMyUserByUsername(verifyOTP.getUsername());
        String secreteKey = user.get().getSecretKey();
        return googleAuthenticator.authorize(secreteKey, verifyOTP.getOtp());
    }

    @Override
    public String disableTwoFactorAuthentication(String username) {
        Optional<AppUser> user = appUserRepository.getMyUserByUsername(username);

        if (user.isPresent()) {
            user.get().setSecretKey(null);
            user.get().setIsMultiFactor(false);
            user.get().setQrCodeImage(null);
            appUserRepository.save(user.get());
            return "Two Factor Authentication Disabled";
        } else {
            return "User Not Found";
        }
    }

    @Override
    public List<UserResponse> getUserByShift(UUID shiftId) {
        return null;
    }

    @Override
    public Integer totalEmployeeCount() {
        return appUserRepository.countAllExceptSuperAdmin();
    }

    @Override
    public long getTotalActiveEmployeeCount() {
        return appUserRepository.findAll().stream().filter(appUser -> appUser.getIsActive().equals(true)).count();
    }
}
