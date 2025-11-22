package com.g3.parking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/publico/**").permitAll()
                .requestMatchers("/api/image-processing/**").permitAll()
                .requestMatchers("/owner/**").hasRole("OWNER")
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "OWNER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")              // tu login personalizado
                .loginProcessingUrl("/login")     // donde Spring recibe credenciales
                .defaultSuccessUrl("/home", true) // redirección al loguear
                .failureUrl("/login?error=true")
                .permitAll())
            .logout(logout -> logout.permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

