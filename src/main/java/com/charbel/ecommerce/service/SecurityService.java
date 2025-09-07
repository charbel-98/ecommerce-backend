package com.charbel.ecommerce.service;

import com.charbel.ecommerce.user.entity.User;
import com.charbel.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Current user not found"));
        }
        throw new RuntimeException("No authenticated user found");
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }
}