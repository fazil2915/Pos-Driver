package com.example.pos_driver.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
public class SecurityFilter extends WebSecurityConfigurerAdapter {

    @Value("${cors.allowedOrigins}")
    private String allowedOrigins;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // Disable CSRF protection
                .cors().configurationSource(corsConfigurationSource()) // Apply CORS settings
                .and()
                .authorizeRequests()
                .antMatchers(
                        "/ws/**",
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
                .anyRequest().permitAll()
                .and()
                .httpBasic() // Use basic authentication
                .and()
                .headers().frameOptions().disable()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> origins = Stream.of(allowedOrigins.split(","))
                .map(String::trim)
                .collect(Collectors.toList());




        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Arrays.asList("*")); // Allow all origins
//        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true); // Set to true if sending cookies or auth headers

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
