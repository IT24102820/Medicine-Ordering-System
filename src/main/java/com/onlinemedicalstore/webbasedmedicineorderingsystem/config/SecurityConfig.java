package com.onlinemedicalstore.webbasedmedicineorderingsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/register", "/login.html", "/register.html", "/store.html", "/index.html", "/", "/pharmago.css", "/images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/inventory/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/medicine/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reviews").authenticated()

                        .requestMatchers("/prescriptions.html").hasRole("CUSTOMER")
                        .requestMatchers("/api/prescriptions/**").hasRole("CUSTOMER")

                        .requestMatchers("/profile.html", "/checkout.html").authenticated()
                        .requestMatchers("/api/users/me/**").authenticated()

                        .requestMatchers("/my_orders.html").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/orders/my-orders").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/orders").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/orders/**").authenticated()

                        .requestMatchers("/api/users/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/user_management.html").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/finance/**").hasAnyRole("FINANCE_OFFICER", "SUPER_ADMIN")
                        .requestMatchers("/finance_dashboard.html").hasAnyRole("FINANCE_OFFICER", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/orders").hasAnyRole("INVENTORY_MANAGER", "SUPER_ADMIN", "FINANCE_OFFICER", "DELIVERY_COORDINATOR")
                        .requestMatchers("/api/inventory/**").hasAnyRole("INVENTORY_MANAGER", "SUPER_ADMIN")
                        .requestMatchers("/admin_dashboard.html", "/orders.html", "/inventory.html").hasAnyRole(
                                "INVENTORY_MANAGER", "SUPER_ADMIN", "FINANCE_OFFICER",
                                "GENERAL_PHYSICIAN", "PHARMACY_ASSISTANT", "DELIVERY_COORDINATOR"
                        )

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            boolean isAdmin = authentication.getAuthorities().stream()
                                    .anyMatch(r -> r.getAuthority().startsWith("ROLE_") && !r.getAuthority().equals("ROLE_CUSTOMER"));
                            if (isAdmin) {
                                response.sendRedirect("/admin_dashboard.html");
                            } else {
                                response.sendRedirect("/store.html");
                            }
                        })
                        .failureUrl("/login.html?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login.html?logout")
                        .permitAll());

        return http.build();
    }
}
