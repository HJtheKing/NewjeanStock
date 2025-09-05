package com.sascom.chickenstock.account.security;
import org.springframework.context.annotation.*; import org.springframework.security.config.annotation.web.builders.HttpSecurity; import org.springframework.security.web.SecurityFilterChain; import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration public class SecurityConfig {
  @Bean SecurityFilterChain filterChain(HttpSecurity http, HmacAuthFilter hmac) throws Exception {
    http.csrf(csrf->csrf.disable())
       .authorizeHttpRequests(auth->auth.requestMatchers("/actuator/health").permitAll().anyRequest().authenticated())
       .addFilterBefore(hmac, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
