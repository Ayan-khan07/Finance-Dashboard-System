package com.finance.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Optional first-run admin. Only used when the users table is empty AND a password is supplied.
 */
@ConfigurationProperties(prefix = "app.bootstrap-admin")
public record BootstrapAdminProperties(String username, String email, String password) {

    @Override
    public String toString() {
        return "BootstrapAdminProperties[username=" + username + ", email=" + email + ", password=***]";
    }
}
