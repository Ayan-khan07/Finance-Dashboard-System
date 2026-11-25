package com.finance.dashboard.dto.response;

import com.finance.dashboard.enums.Role;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String username,
        Role role) {
}
