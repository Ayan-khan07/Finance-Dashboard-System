package com.finance.dashboard.dto.response;

import com.finance.dashboard.enums.Role;
import com.finance.dashboard.enums.UserStatus;

import java.time.Instant;

/** Never contains the password hash. */
public record UserResponse(
        Long id,
        String username,
        String email,
        Role role,
        UserStatus status,
        Instant createdAt) {
}
