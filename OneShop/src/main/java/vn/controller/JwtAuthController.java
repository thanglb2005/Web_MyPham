package vn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.dto.JwtAuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;
import vn.dto.JwtRefreshRequest;
import vn.dto.LoginRequest;
import vn.entity.User;
import vn.repository.UserRepository;
import vn.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT Authentication Controller
 * Provides REST API endpoints for JWT-based authentication
 */
@RestController
@RequestMapping("/api/auth")
@Validated
public class JwtAuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Authenticate user and return JWT tokens
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            // Find user by email
            Optional<User> userOpt = userRepository.findByEmailWithRoles(loginRequest.getEmail());
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
            }
            
            User user = userOpt.get();
            
            // Validate password (in production, use proper password encoding)
            if (!user.getPassword().equals(loginRequest.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
            }
            
            // Check if user is enabled
            if (!user.getEnabled()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Account is disabled"));
            }
            
            // Generate JWT tokens
            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);
            
            // Create user info
            String[] roles = user.getRoles().stream()
                .map(role -> role.getName())
                .toArray(String[]::new);
                
            // Use email as name if name is null or "guest"
            String displayName = user.getName();
            if (displayName == null || displayName.trim().isEmpty() || "guest".equalsIgnoreCase(displayName)) {
                displayName = user.getEmail();
            }
            
            JwtAuthenticationResponse.UserInfo userInfo = new JwtAuthenticationResponse.UserInfo(
                user.getUserId(),
                user.getEmail(),
                displayName,
                user.getAvatar(),
                roles,
                user.getOneXuBalance()
            );
            
            // Store JWT tokens in HTTP cookies (Pure JWT approach)
            Cookie accessTokenCookie = new Cookie("jwt_access_token", accessToken);
            accessTokenCookie.setHttpOnly(true); // Bảo mật - JavaScript không thể đọc
            accessTokenCookie.setSecure(false); // Set true nếu dùng HTTPS
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(3600); // 1 hour
            response.addCookie(accessTokenCookie);
            
            Cookie refreshTokenCookie = new Cookie("jwt_refresh_token", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(86400); // 24 hours
            response.addCookie(refreshTokenCookie);
            
            // Store user in session for web page access (Hybrid approach)
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            
            // Create response
            JwtAuthenticationResponse authResponse = new JwtAuthenticationResponse(
                accessToken,
                refreshToken,
                jwtUtil.getAccessTokenExpiration(),
                userInfo
            );
            
            return ResponseEntity.ok(authResponse);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Authentication failed: " + e.getMessage()));
        }
    }

    /**
     * Refresh access token using refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody JwtRefreshRequest refreshRequest) {
        try {
            String refreshToken = refreshRequest.getRefreshToken();
            
            // Validate refresh token
            if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid refresh token"));
            }
            
            // Extract username from refresh token
            String username = jwtUtil.extractUsername(refreshToken);
            
            // Find user
            Optional<User> userOpt = userRepository.findByEmailWithRoles(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
            }
            
            User user = userOpt.get();
            
            // Check if user is still enabled
            if (!user.getEnabled()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Account is disabled"));
            }
            
            // Generate new access token
            String newAccessToken = jwtUtil.generateAccessToken(user);
            
            // Optionally generate new refresh token (for token rotation)
            String newRefreshToken = jwtUtil.generateRefreshToken(user);
            
            // Create user info for response
            String[] roles = user.getRoles().stream()
                .map(role -> role.getName())
                .toArray(String[]::new);
                
            // Use email as name if name is null or "guest"
            String displayName = user.getName();
            if (displayName == null || displayName.trim().isEmpty() || "guest".equalsIgnoreCase(displayName)) {
                displayName = user.getEmail();
            }
            
            JwtAuthenticationResponse.UserInfo userInfo = new JwtAuthenticationResponse.UserInfo(
                user.getUserId(),
                user.getEmail(),
                displayName,
                user.getAvatar(),
                roles,
                user.getOneXuBalance()
            );
            
            // Create response
            JwtAuthenticationResponse response = new JwtAuthenticationResponse(
                newAccessToken,
                newRefreshToken,
                jwtUtil.getAccessTokenExpiration(),
                userInfo
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Token refresh failed: " + e.getMessage()));
        }
    }

    /**
     * Get current user information from JWT token
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            // Get user from JWT token (set by JwtAuthenticationFilter)
            User user = (User) request.getAttribute("jwtUser");
            
            if (user == null) {
                // Fallback: extract from security context
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                Optional<User> userOpt = userRepository.findByEmailWithRoles(username);
                
                if (userOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not found"));
                }
                user = userOpt.get();
            }
            
            // Create user info response
            String[] roles = user.getRoles().stream()
                .map(role -> role.getName())
                .toArray(String[]::new);
                
            JwtAuthenticationResponse.UserInfo userInfo = new JwtAuthenticationResponse.UserInfo(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getAvatar(),
                roles,
                user.getOneXuBalance()
            );
            
            return ResponseEntity.ok(userInfo);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get user info: " + e.getMessage()));
        }
    }

    /**
     * Logout user (client-side token removal)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Clear JWT cookies (Hybrid approach)
        Cookie accessTokenCookie = new Cookie("jwt_access_token", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0); // Delete cookie
        response.addCookie(accessTokenCookie);
        
        Cookie refreshTokenCookie = new Cookie("jwt_refresh_token", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // Delete cookie
        response.addCookie(refreshTokenCookie);
        
        // Clear session (Hybrid approach)
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        // Clear security context
        SecurityContextHolder.clearContext();
        
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * Validate JWT token
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("valid", false, "error", "Token is required"));
            }
            
            boolean isValid = jwtUtil.validateToken(token);
            
            if (isValid) {
                // Extract basic info from token
                String username = jwtUtil.extractUsername(token);
                String tokenType = jwtUtil.extractTokenType(token);
                Date expiration = jwtUtil.extractExpiration(token);
                
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "username", username,
                    "tokenType", tokenType,
                    "expiration", expiration
                ));
            } else {
                return ResponseEntity.ok(Map.of("valid", false));
            }
            
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "valid", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Set JWT authentication context for web pages
     * This endpoint allows frontend to authenticate for web page access
     */
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticateForWeb(@RequestBody Map<String, String> request, 
                                              HttpServletRequest httpRequest) {
        try {
            String token = request.get("token");
            
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Token is required"));
            }
            
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", "Invalid token"));
            }
            
            // Extract user information from token
            String username = jwtUtil.extractUsername(token);
            Long userId = jwtUtil.extractUserId(token);
            String rolesString = jwtUtil.extractRoles(token);
            
            // Find user from database
            Optional<User> userOpt = userRepository.findByEmailWithRoles(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", "User not found"));
            }
            
            User user = userOpt.get();
            
            // Set authentication context
            List<GrantedAuthority> authorities = Arrays.stream(rolesString.split(","))
                    .map(role -> new SimpleGrantedAuthority(role))
                    .collect(Collectors.toList());
            
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(username, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
            
            SecurityContextHolder.getContext().setAuthentication(authToken);
            
            // Set user in request attribute for controllers
            httpRequest.setAttribute("jwtUser", user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Authentication successful",
                "user", Map.of(
                    "userId", user.getUserId(),
                    "email", user.getEmail(),
                    "name", user.getName() != null ? user.getName() : user.getEmail(),
                    "roles", rolesString.split(",")
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Authentication failed: " + e.getMessage()));
        }
    }
}
