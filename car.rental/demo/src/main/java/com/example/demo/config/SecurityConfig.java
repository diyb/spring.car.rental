package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // avalik osa
                .requestMatchers("/css/**", "/login", "/register", "/error").permitAll()
                // ainult admin: autode haldus ja klientide vaatamine
                .requestMatchers("/cars/**").hasRole("ADMIN")
                .requestMatchers("/customers/**").hasRole("ADMIN")
                // ainult tavakasutaja: uue broneeringu tegemine
                .requestMatchers("/bookings/new", "/bookings/save").hasRole("USER")
                // ülejäänu (broneeringute nimekiri, tühistamine) eeldab sisselogimist
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/bookings", true)
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll());
        return http.build();
    }

    /**
     * Uued paroolid salvestatakse kujul "{bcrypt}...". Prefiksiga "{noop}parool"
     * saab andmebaasi parooli ka käsitsi (nt phpMyAdminis) avatekstina sisestada.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
