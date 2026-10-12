package com.backend.services;

import com.backend.models.Permissions;
import com.backend.models.Privilege;
import com.backend.models.Role;
import com.backend.models.requests.RoleDto;
import com.backend.models.response.ApiResponse;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    Role createRole(RoleDto roleDto);
    List<RoleDto> getAllRoles();

    List<Permissions> createPermissions(List<Permissions> permissions);

    Privilege createPrivilege(Privilege privilege);

    RoleDto getRoleByRoleName(String roleName);

    Role updateRole(RoleDto roleDto) ;

    Role findById(UUID roleId);

    ApiResponse<?> deleteRole(UUID roleId);
}
