package com.example.Spectacle_phase1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.Spectacle_phase1.Repository.UserRepository;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

	private final UserRepository userRepository;

	public WebSecurityConfig(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(authorize -> authorize
						// Allow public access to static resources, home, login, register, and product
						// pages
						.requestMatchers(
								"/",
								"/login",
								"/register",
								"/about",
								"/contact",
								"/css/**",
								"/javascript/**",
								"/images/**",
								"/product-details", 
								"/image/**",
								"/eyeglasses",
								"/sunglasses",
								"/screen-glasses",
								"/contact-lenses",
								"/kids-glasses"
						).permitAll()
						// Secure admin pages - use hasRole() which automatically adds ROLE_ prefix
						.requestMatchers("/admin/**").hasRole("ADMIN")
						// All other requests require authentication
						.anyRequest().authenticated())
				.formLogin(form -> form
						.loginPage("/login")
						.defaultSuccessUrl("/", true)
						.permitAll())
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login?logout")
						.permitAll());

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return username -> userRepository.findByUsername(username)
				.map(user -> org.springframework.security.core.userdetails.User.builder()
						.username(user.getUsername())
						.password(user.getPassword())
						.authorities("ROLE_" + user.getRole()) // Use authorities to add the ROLE_ prefix
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
	}

}