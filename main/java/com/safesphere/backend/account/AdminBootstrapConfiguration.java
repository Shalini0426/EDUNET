package com.safesphere.backend.account;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminBootstrapConfiguration {

    @Bean
    ApplicationRunner administratorBootstrap(
            UserAccountService accounts,
            @Value("${safesphere.admin.email:}") String email,
            @Value("${safesphere.admin.password:}") String password) {
        return applicationArguments -> accounts.bootstrapAdministrator(email, password);
    }
}
