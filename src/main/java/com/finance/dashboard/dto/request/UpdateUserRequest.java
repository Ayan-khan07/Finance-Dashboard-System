package com.finance.dashboard.dto.request;

import com.finance.dashboard.enums.Role;
import com.finance.dashboard.enums.UserStatus;

/** Partial update: supply role, status, or both. At least one is required (checked in the service). */
public record UpdateUserRequest(
        Role role,
        UserStatus status) {
}
