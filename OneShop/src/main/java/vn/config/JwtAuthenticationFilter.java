package vn.config;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.entity.User;
import vn.repository.UserRepository;
import vn.util.JwtUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter
 * Intercepts requests and validates JWT tokens
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // Skip JWT validation for certain paths
        String requestPath = request.getRequestURI();
        if (shouldSkipJwtValidation(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwt = null;

        // Extract JWT token from Authorization header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (JwtException e) {
                logger.warn("JWT token validation failed: " + e.getMessage());
                // Continue without authentication - let Spring Security handle it
            }
        }
        
        // If no JWT in header, try to get from cookies
        if (jwt == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt_access_token".equals(cookie.getName())) {
                        jwt = cookie.getValue();
                        try {
                            username = jwtUtil.extractUsername(jwt);
                        } catch (JwtException e) {
                            logger.warn("Cookie JWT token validation failed: " + e.getMessage());
                            jwt = null; // Invalid token
                        }
                        break;
                    }
                }
            }
        }

        // If we have a valid JWT token and no authentication is set
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Validate token and set authentication
            if (jwtUtil.validateToken(jwt)) {
                try {
                    // Extract user information from token
                    Long userId = jwtUtil.extractUserId(jwt);
                    String rolesString = jwtUtil.extractRoles(jwt);
                    
                    // Convert roles string to GrantedAuthority list
                    List<GrantedAuthority> authorities = Arrays.stream(rolesString.split(","))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    // Store user in session for web page access (Hybrid JWT + Session)
                    if (userId != null) {
                        Optional<User> userOpt = userRepository.findByIdWithRoles(userId);
                        if (userOpt.isPresent()) {
                            User user = userOpt.get();
                            request.setAttribute("jwtUser", user);
                            
                            // Store in session for web pages (Hybrid approach)
                            HttpSession session = request.getSession(true);
                            session.setAttribute("user", user);
                        }
                    }
                    
                } catch (Exception e) {
                    logger.warn("Error setting authentication from JWT: " + e.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Determine if JWT validation should be skipped for certain paths
     */
    private boolean shouldSkipJwtValidation(String requestPath) {
        // Skip JWT validation for:
        // - Static resources
        // - Public API endpoints
        // - OAuth2 endpoints
        // - Login/register pages
        return requestPath.startsWith("/css/") ||
               requestPath.startsWith("/js/") ||
               requestPath.startsWith("/images/") ||
               requestPath.startsWith("/fonts/") ||
               requestPath.startsWith("/static/") ||
               requestPath.startsWith("/webjars/") ||
               requestPath.startsWith("/upload/") ||
               requestPath.startsWith("/brands/") ||
               requestPath.startsWith("/providers/") ||
               requestPath.startsWith("/vendor/bootstrap/") ||
               requestPath.startsWith("/vendor/slickslider/") ||
               requestPath.startsWith("/vendor/venobox/") ||
               requestPath.startsWith("/vendor/niceselect/") ||
               requestPath.startsWith("/vendor/countdown/") ||
               requestPath.startsWith("/oauth2/") ||
               requestPath.startsWith("/login") ||
               requestPath.startsWith("/register") ||
               requestPath.startsWith("/forgotPassword") ||
               requestPath.startsWith("/resetPassword") ||
               requestPath.equals("/") ||
               requestPath.equals("/privacy") ||
               requestPath.equals("/delete-data") ||
               requestPath.equals("/delete-data-callback") ||
               requestPath.startsWith("/api/auth/"); // Skip for auth endpoints
    }
}
