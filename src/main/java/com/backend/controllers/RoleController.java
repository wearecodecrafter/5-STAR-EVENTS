package com.backend.controllers;

import com.backend.models.requests.RoleDto;
import com.backend.models.response.ApiResponse;
import com.backend.services.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/role")
@AllArgsConstructor
@CrossOrigin("*")
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/get-all-roles")
    public ResponseEntity<?> getAllRoles(){
        System.out.println("Getting all roles...");
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @PostMapping("/create-role")
    public ResponseEntity<?> createRole(@RequestBody RoleDto role){
        return ResponseEntity.ok(roleService.createRole(role));
    }

    @GetMapping("/get-role-by-role-name")
    public ResponseEntity<?> getRoleByRoleName(@RequestParam String roleName)  {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(roleService.getRoleByRoleName(roleName));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
    }

    @PutMapping("/update-role")
    public ResponseEntity<?> updateRole(@RequestBody RoleDto roleDto) {
        return ResponseEntity.status(HttpStatus.OK).body(roleService.updateRole(roleDto));
    }

    @DeleteMapping("/delete-role")
    public ResponseEntity<?> deleteRole(@RequestParam UUID roleId){
        ApiResponse<?> response = roleService.deleteRole(roleId);
        if(response.isSuccess()){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
