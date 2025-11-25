package com.g3.parking.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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
                                         .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                                .sessionFixation().migrateSession() // PREVENIR corrupción de sesión
                                                .maximumSessions(1))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                                                .requestMatchers("/", "/publico/**").permitAll() // permite acceso a la página de bienvenida
                                                .requestMatchers("/api/auth/**").permitAll()
                                                .requestMatchers("/api/sites/**").permitAll()   
                                                .requestMatchers("/api/**").authenticated()
                                                .requestMatchers("/owner/**").hasRole("OWNER")
                                                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "OWNER")
                                                .anyRequest().authenticated())
                                                .exceptionHandling(ex -> ex
                                                .defaultAuthenticationEntryPointFor(
                                                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                                        new AntPathRequestMatcher("/api/**")))
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

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }
}
