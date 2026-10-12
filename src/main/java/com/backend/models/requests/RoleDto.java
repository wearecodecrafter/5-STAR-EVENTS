package com.backend.models.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDto {

    private UUID roleId;
    private String roleName;
    private String roleDescription;
    private List<PermissionDto> permissions;
}
