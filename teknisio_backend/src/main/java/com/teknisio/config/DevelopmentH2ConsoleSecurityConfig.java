package com.teknisio.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security exception for the local H2 console.
 *
 * <p>This configuration only exists in the {@code development} profile. The
 * same profile binds the complete application to {@code 127.0.0.1}, so the
 * console cannot be exposed to another host.</p>
 */
@Configuration
@Profile("development")
public class DevelopmentH2ConsoleSecurityConfig {

  @Bean
  @Order(1)
  SecurityFilterChain h2ConsoleSecurityFilterChain(HttpSecurity http) throws Exception {
    return http
      .securityMatcher(PathRequest.toH2Console())
      .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
      .csrf(csrf -> csrf.ignoringRequestMatchers(PathRequest.toH2Console()))
      .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
      .build();
  }
}
