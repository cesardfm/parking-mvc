package com.g3.parking.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        /*
         * private final CustomUserDetailsService customUserDetailsService;
         * 
         * public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
         * this.customUserDetailsService = customUserDetailsService;
         * }
         */

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionFixation().migrateSession() // PREVENIR corrupción de sesión
                                                .maximumSessions(1))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                                                .requestMatchers("/publico/**").permitAll()
                                                .requestMatchers("/owner/**").hasRole("OWNER")
                                                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "OWNER")
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login") // login personalizado
                                                .loginProcessingUrl("/login") // donde Spring recibe credenciales
                                                .defaultSuccessUrl("/dashboard", true) // redirección al loguear
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
        public ModelMapper modelMapper() {
                ModelMapper modelMapper = new ModelMapper();

                // Configuraciones opcionales para mejor mapeo
                modelMapper.getConfiguration()
                                .setMatchingStrategy(MatchingStrategies.STRICT)
                                .setSkipNullEnabled(true);

                return modelMapper;
        }

        /*
         * @Bean
         * public DaoAuthenticationProvider authenticationProvider() {
         * DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
         * authProvider.setUserDetailsService(customUserDetailsService);
         * authProvider.setPasswordEncoder(passwordEncoder());
         * return authProvider;
         * }
         * 
         * @Bean
         * public AuthenticationManager
         * authenticationManager(AuthenticationConfiguration config) throws Exception {
         * return config.getAuthenticationManager();
         * }
         */
}
