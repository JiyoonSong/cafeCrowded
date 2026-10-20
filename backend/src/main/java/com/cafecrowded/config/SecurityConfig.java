package com.cafecrowded.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			// Stateless public API: no cookies/sessions, so CSRF tokens do not apply.
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.cors(Customizer.withDefaults())
			.headers(h -> h
				.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
				.frameOptions(f -> f.deny()))
			.authorizeHttpRequests(a -> a
				.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/cafes/*/reports").permitAll()
				.anyRequest().denyAll());
		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") String origins) {
		CorsConfiguration cfg = new CorsConfiguration();
		List<String> allowed = Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
		cfg.setAllowedOrigins(allowed);
		cfg.setAllowedMethods(List.of("GET", "POST"));
		cfg.setAllowedHeaders(List.of("Content-Type"));
		cfg.setMaxAge(3600L);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", cfg);
		return source;
	}
}
