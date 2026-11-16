package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Registreerimise vorm. */
public class RegistrationForm {

    @NotBlank(message = "Nimi on kohustuslik")
    @Size(max = 100, message = "Nimi võib olla kuni 100 tähemärki")
    private String name;

    @NotBlank(message = "E-post on kohustuslik")
    @Email(message = "E-posti aadress ei ole korrektne")
    @Size(max = 100, message = "E-post võib olla kuni 100 tähemärki")
    private String email;

    @NotBlank(message = "Parool on kohustuslik")
    @Size(min = 6, max = 72, message = "Parool peab olema 6–72 tähemärki")
    private String password;

    @NotBlank(message = "Korda parooli")
    private String confirmPassword;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
