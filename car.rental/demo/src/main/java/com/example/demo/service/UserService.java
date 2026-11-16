package com.example.demo.service;

import com.example.demo.dto.RegistrationForm;
import com.example.demo.entity.AppUser;
import com.example.demo.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean emailTaken(String email) {
        return userRepository.existsByEmailIgnoreCase(email.trim());
    }

    /** Loob uue kasutaja rolliga USER. */
    @Transactional
    public AppUser register(RegistrationForm form) {
        AppUser user = new AppUser();
        user.setName(form.getName().trim());
        user.setEmail(form.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole(AppUser.ROLE_USER);
        return userRepository.save(user);
    }

    /** Loob esimese administraatori, kui ühtegi veel pole. */
    @Transactional
    public void createAdminIfMissing(String email, String rawPassword) {
        if (userRepository.existsByRole(AppUser.ROLE_ADMIN)) {
            return;
        }
        AppUser admin = new AppUser();
        admin.setName("Administraator");
        admin.setEmail(email.trim().toLowerCase());
        admin.setPassword(passwordEncoder.encode(rawPassword));
        admin.setRole(AppUser.ROLE_ADMIN);
        userRepository.save(admin);
    }

    /** Administraatori klientide nimekiri. */
    public List<AppUser> findCustomers() {
        return userRepository.findByRoleOrderByNameAsc(AppUser.ROLE_USER);
    }

    public Long idFor(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(AppUser::getId)
                .orElseThrow(() -> new NoSuchElementException("Kasutajat ei leitud: " + email));
    }

    public String displayName(String email) {
        return userRepository.findByEmailIgnoreCase(email).map(AppUser::getName).orElse(email);
    }
}
