package com.backend.services;


import com.backend.models.AppUser;
import com.backend.models.requests.VerifyOTP;
import com.backend.models.response.ApiResponse;
import com.backend.models.response.PaginatedResponse;
import com.backend.models.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface AppUserServices {

    Boolean saveUser(AppUser user, MultipartFile imageFile ) throws IOException;

    UserResponse getUserByUsername(String username);

    List<UserResponse> getUsersByRoleName(String roleName);

    UserResponse getUsersByEmployeeCode(String employeeCode);

    List<UserResponse> getAllHrs();

    List<UserResponse> searchUser(String keyword);

    UserResponse getAppUser(UUID id);

    PaginatedResponse<UserResponse> getAllAppUsers(int page , int size);

    Boolean updateAppUser(AppUser user, MultipartFile imageFile) throws IOException;
    Boolean updateAppEmpUser(AppUser user, MultipartFile imageFile) throws IOException;


    Boolean isOtpVerify(VerifyOTP verifyOTP);


    String disableTwoFactorAuthentication(String username);

    ApiResponse<?> activeUser(UUID id);

    ApiResponse<?> diActive(UUID id);

    List<UserResponse> getUserByShift(UUID shiftId);

    Integer totalEmployeeCount();

    long getTotalActiveEmployeeCount();
}
