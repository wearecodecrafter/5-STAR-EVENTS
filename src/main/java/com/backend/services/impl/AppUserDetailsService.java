package com.backend.services.impl;


import com.backend.models.AppUser;
import com.backend.models.Customer;
import com.backend.repositories.AppUserRepository;
import com.backend.repositories.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;
    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. staff / owner
        Optional<AppUser> user = userRepository.getMyUserByUsername(username);
        if (user.isPresent()) {
            AppUser u = user.get();
            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + u.getRole().getRoleName());
            return new User(u.getUsername(), u.getPassword(), Collections.singletonList(authority));
        }

        // 2. customer (username = phone number)
        Customer customer = customerRepository.findByPhoneNumber(normalizePhone(username))
                .orElseThrow(() -> new UsernameNotFoundException("No User Found!"));

        if (customer.getPassword() == null) {
            // created by the owner, never registered
            throw new UsernameNotFoundException("No User Found!");
        }

        String roleName = customer.getRole() != null ? customer.getRole().getRoleName() : "CUSTOMER";
        return new User(customer.getPhoneNumber(), customer.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName)));
    }

    public static String normalizePhone(String phone) {
        return phone == null ? null : phone.replaceAll("\\D", "");
    }
}