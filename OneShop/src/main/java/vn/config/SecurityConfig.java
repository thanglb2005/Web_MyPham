package vn.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.service.CustomOAuth2UserService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService oAuth2UserService;

    @Autowired
    private AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); 
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Static resources - must be first and most specific
                .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/static/**", "/webjars/**", "/upload/**", "/brands/**", "/providers/**").permitAll()
                .requestMatchers("/vendor/bootstrap/**", "/vendor/slickslider/**", "/vendor/venobox/**", "/vendor/niceselect/**", "/vendor/countdown/**").permitAll()
                // Public pages
                .requestMatchers("/oauth2/**", "/login/**", "/register/**", "/", "/forgotPassword/**", "/resetPassword/**", "/privacy", "/delete-data", "/delete-data-callback").permitAll()
                // JWT API endpoints
                .requestMatchers("/api/auth/**").permitAll()
                // Role-based access
                // Allow CSKH and Vendor to access the vendor chat console as well
                .requestMatchers("/admin/vendor-chat").hasAnyRole("ADMIN", "CSKH", "VENDOR")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/vendor/**").hasRole("VENDOR")
                .requestMatchers("/cskh/**").hasRole("CSKH")
                .requestMatchers("/shipper/**").hasRole("SHIPPER")
                .anyRequest().permitAll()
            )
            // Configure session management for JWT + Session hybrid
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2UserService)
                )
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler((request, response, exception) -> {
                    System.err.println("OAuth2 Error: " + exception.getMessage());
                    exception.printStackTrace();
                    try {
                        String encodedMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
                        response.sendRedirect("/login?error=oauth2_error&message=" + encodedMessage);
                    } catch (Exception e) {
                        response.sendRedirect("/login?error=oauth2_error");
                    }
                })
            )
            // DISABLE Spring Security logout - using custom LoginController.logout() instead
            .logout(logout -> logout.disable());

        return http.build();
    }
}
