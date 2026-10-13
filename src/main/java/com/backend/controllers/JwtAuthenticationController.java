package com.backend.controllers;

import com.backend.configurations.jwt.JwtUtils;
import com.backend.models.*;
import com.backend.models.requests.JwtRequest;
import com.backend.models.requests.PermissionDto;
import com.backend.models.requests.RoleDto;
import com.backend.models.response.JwtResponse;
import com.backend.repositories.AppUserRepository;
import com.backend.repositories.CustomerRepository;
import com.backend.repositories.PermissionRepository;
import com.backend.repositories.RoleRepository;
import com.backend.services.RoleService;
import com.backend.services.impl.AppUserDetailsService;
import com.backend.services.impl.AppUserServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/service")
@CrossOrigin("*")
public class JwtAuthenticationController {
    private final AppUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleService roleService;
    private final AppUserServiceImpl userService;
    private final CustomerRepository customerRepository;


    @PostConstruct
    public void createAdmin() {
        Optional<AppUser> optionalUser = userRepository.getMyUserByUsername("admin.com");
        if (optionalUser.isEmpty()) {
            Role savedRole = roleRepository.findByRoleName("SUPER_ADMIN")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setRoleName("SUPER_ADMIN");
                        role.setRoleDescription("This is super admin role");
                        return roleRepository.save(role);
                    });

            if (permissionRepository.getPermissionsByRole(savedRole).isEmpty()) {
                Privilege privilege = new Privilege();
                privilege.setWritePermission("WRITE");
                privilege.setReadPermission("READ");
                privilege.setDeletePermission("DELETE");
                privilege.setUpdatePermission("UPDATE");

                Permissions permissions = new Permissions();
                permissions.setUserPermission("ALL_PERMISSIONS");
                permissions.setRole(savedRole);
                permissions.setPrivilege(privilege);
                roleService.createPermissions(List.of(permissions));
            }

            Role savedRole2 = roleRepository.findByRoleName("EMPLOYEE")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setRoleName("EMPLOYEE");
                        role.setRoleDescription("This is employee role");
                        return roleRepository.save(role);
                    });

            if (permissionRepository.getPermissionsByRole(savedRole2).isEmpty()) {
                Privilege privilege = new Privilege();
                privilege.setWritePermission("WRITE");
                privilege.setReadPermission("READ");
                privilege.setDeletePermission("DELETE");
                privilege.setUpdatePermission("UPDATE");

                Permissions permissions = new Permissions();
                permissions.setUserPermission("LIMITED");
                permissions.setRole(savedRole2);
                permissions.setPrivilege(privilege);
                roleService.createPermissions(List.of(permissions));
            }

            AppUser user = new AppUser();
            user.setName("Admin User");
            user.setUsername("admin.com");
            user.setContact("-");
            user.setEmail("fivestarevents@gmail.com");
            user.setIsActive(true);
            user.setRole(savedRole);
            user.setPosition("SUPER_ADMIN");
            user.setEmployeeCode("SUP-EMP-0001");
            user.setPassword(new BCryptPasswordEncoder().encode("admin@123"));
            userRepository.save(user);
        }
    }
    @PostMapping("/login")
    public JwtResponse createAuthenticationToken(@RequestBody JwtRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(request.getUsername());

        JwtResponse response = new JwtResponse();

        response.setJwtToken(
                jwtUtils.generateToken(userDetails.getUsername())
        );

        // 1. Check AppUser first (staff / owner)
        Optional<AppUser> appUserOptional =
                userRepository.getMyUserByUsername(userDetails.getUsername());

        if (appUserOptional.isPresent()) {

            AppUser user = appUserOptional.get();

            response.setUserId(user.getId());
            response.setFcmToken(user.getFcmToken());
            response.setEmail(user.getEmail());
            response.setName(user.getName());
            response.setContact(user.getContact());
            response.setUsername(user.getUsername());
            response.setUpdatedAt(user.getUpdatedAt());
            response.setCreatedAt(user.getCreatedAt());
            response.setIsLoggedIn(user.getIsUserLoggedIn());
            response.setIsMultiFactor(user.getIsMultiFactor());
            response.setImageUrl(user.getImageUrl());
            response.setIsActive(user.getIsActive());
            response.setPosition(user.getPosition());
            response.setEmployeeCode(user.getEmployeeCode());

            response.setRole(buildRoleDto(user.getRole()));

            return response;
        }

        // 2. Otherwise, check Customer using phone number
        String phone = AppUserDetailsService.normalizePhone(
                request.getUsername()
        );

        Customer customer = customerRepository.findByPhoneNumber(phone)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Customer not found.")
                );

        if (customer.getRole() == null) {
            throw new IllegalStateException(
                    "Customer role is not assigned."
            );
        }

        response.setUserId(customer.getId());
        response.setName(customer.getFullName());
        response.setContact(customer.getPhoneNumber());
        response.setUsername(customer.getPhoneNumber());

        response.setRole(buildRoleDto(customer.getRole()));

        return response;
    }


//    @PostMapping("/login")
//    public JwtResponse createAuthenticationToken(@RequestBody JwtRequest request) {
//        try {
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            request.getUsername(),
//                            request.getPassword()
//                    )
//            );
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw e;
//        }
//        UserDetails userDetails =
//                userDetailsService.loadUserByUsername(request.getUsername());
//        AppUser user = userRepository
//                .getMyUserByUsername(userDetails.getUsername())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//        Optional<Role> roleOptional = roleRepository.findById(user.getRole().getId());
//        Role role = roleOptional.get();
//
//        JwtResponse response = new JwtResponse();
//        response.setJwtToken(jwtUtils.generateToken(userDetails.getUsername()));
//        response.setUserId(user.getId());
//        response.setFcmToken(user.getFcmToken());
//        response.setEmail(user.getEmail());
//        response.setName(user.getName());
//        response.setContact(user.getContact());
//        response.setUsername(user.getUsername());
//        response.setUpdatedAt(user.getUpdatedAt());
//        response.setCreatedAt(user.getCreatedAt());
//
//        RoleDto roleDto = new RoleDto();
//        roleDto.setRoleId(role.getId());
//        roleDto.setRoleName(role.getRoleName());
//        roleDto.setRoleDescription(role.getRoleDescription());
//
//        List<PermissionDto> permissionDtos = new ArrayList<>();
//
//        for (Permissions permission : role.getPermissions()) {
//            PermissionDto permissionDto = new PermissionDto();
//            List<String> privileges = new ArrayList<>();
//            permissionDto.setUserPermission(permission.getUserPermission());
//            privileges.add(permission.getPrivilege().getReadPermission());
//            privileges.add(permission.getPrivilege().getWritePermission());
//            privileges.add(permission.getPrivilege().getUpdatePermission());
//            privileges.add(permission.getPrivilege().getDeletePermission());
//            permissionDto.setPrivileges(privileges);
//            permissionDtos.add(permissionDto);
//        }
//
//        roleDto.setPermissions(permissionDtos);
//
//        response.setRole(roleDto);
//        response.setIsLoggedIn(user.getIsUserLoggedIn());
//        response.setIsMultiFactor(user.getIsMultiFactor());
//        response.setImageUrl(user.getImageUrl());
//        response.setUpdatedAt(user.getUpdatedAt());
//        response.setIsActive(user.getIsActive());
//        response.setPosition(user.getPosition());
//        response.setEmployeeCode(user.getEmployeeCode());
//        return response;
//    }

    @PostMapping("/is-token-expired")
    public ResponseEntity<?> idTokenExpired(@RequestBody JwtResponse jwtResponse) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(jwtUtils.isTokenExpired(jwtResponse.getJwtToken()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(true);
        }
    }
    private RoleDto buildRoleDto(Role role) {

        RoleDto roleDto = new RoleDto();

        roleDto.setRoleId(role.getId());
        roleDto.setRoleName(role.getRoleName());
        roleDto.setRoleDescription(role.getRoleDescription());

        List<PermissionDto> permissionDtos = new ArrayList<>();

        for (Permissions permission : role.getPermissions()) {

            PermissionDto permissionDto = new PermissionDto();

            permissionDto.setUserPermission(
                    permission.getUserPermission()
            );

            List<String> privileges = new ArrayList<>();

            if (permission.getPrivilege().getReadPermission() != null) {
                privileges.add(
                        permission.getPrivilege().getReadPermission()
                );
            }

            if (permission.getPrivilege().getWritePermission() != null) {
                privileges.add(
                        permission.getPrivilege().getWritePermission()
                );
            }

            if (permission.getPrivilege().getUpdatePermission() != null) {
                privileges.add(
                        permission.getPrivilege().getUpdatePermission()
                );
            }

            if (permission.getPrivilege().getDeletePermission() != null) {
                privileges.add(
                        permission.getPrivilege().getDeletePermission()
                );
            }

            permissionDto.setPrivileges(privileges);
            permissionDtos.add(permissionDto);
        }

        roleDto.setPermissions(permissionDtos);

        return roleDto;
    }
}
