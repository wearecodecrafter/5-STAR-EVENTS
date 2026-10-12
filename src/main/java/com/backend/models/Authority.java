package com.backend.models;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@AllArgsConstructor
@NoArgsConstructor
public class Authority implements GrantedAuthority {
    private Role authority;
    @Override
    public String getAuthority() {
        return this.authority.getRoleName();
    }

    public void setAuthority(Role authority) {
        this.authority = authority;
    }
}
