package com.example.pos_driver.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityFilter {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // Disable CSRF protection
                .cors().and() // Apply CORS settings
                .authorizeRequests()
                .antMatchers(
                        "/auth/**",
                        "/fit/**",
                        "/merchant/**",
                        "/device/**",
                        "/transaction/**",
                        "/terminal/**",
                        "/key/**",
                        "/hsm/**",
                        "/analytics/**",
                        "/switch/**"
                ).permitAll()
                .anyRequest().authenticated()
                .and()
                .httpBasic() // Use basic authentication
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS); // Stateless session
        return http.build();
    }
}
