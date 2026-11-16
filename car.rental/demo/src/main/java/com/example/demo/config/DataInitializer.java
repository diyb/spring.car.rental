package com.example.demo.config;

import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Loob rakenduse käivitamisel esimese administraatori, kui ühtegi pole. */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(UserService userService,
                           @Value("${app.admin.email}") String adminEmail,
                           @Value("${app.admin.password}") String adminPassword) {
        this.userService = userService;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        userService.createAdminIfMissing(adminEmail, adminPassword);
    }
}
