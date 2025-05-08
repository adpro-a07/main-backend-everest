package id.ac.ui.cs.advprog.everest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity // Enables @PreAuthorize and friends
public class MethodSecurityConfig {}
