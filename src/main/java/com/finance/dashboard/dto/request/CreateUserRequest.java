package com.finance.dashboard.dto.request;

import com.finance.dashboard.enums.Role;
import com.finance.dashboard.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank
        @Size(min = 3, max = 50)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "may contain only letters, digits, '.', '_' and '-'")
        String username,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        /* BCrypt only uses the first 72 bytes, so longer passwords are rejected rather than silently truncated. */
        @NotBlank
        @Size(min = 8, max = 72)
        String password,

        @NotNull
        Role role,

        /* Optional; defaults to ACTIVE. */
        UserStatus status) {

    @Override
    public String toString() {
        return "CreateUserRequest[username=" + username + ", email=" + email
                + ", password=***, role=" + role + ", status=" + status + "]";
    }
}
