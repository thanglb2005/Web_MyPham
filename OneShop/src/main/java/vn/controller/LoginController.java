package vn.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.entity.User;
import vn.repository.UserRepository;
import vn.util.JwtUtil;

import java.util.Base64;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/login")
    public String loginPage(HttpServletRequest request, Model model) {
        // Pure JWT: Check if user is already authenticated via JWT
        // JwtAuthenticationFilter will handle authentication from cookies
        // No session-based auto-login needed
        
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, 
                       @RequestParam String password,
                       @RequestParam(required = false) Boolean rememberMe,
                       HttpServletResponse response,
                       Model model) {
        // Pure JWT: This method is now only used as fallback for traditional login
        // Main authentication should be handled via /api/auth/login endpoint
        
        Optional<User> userOpt = userRepository.findByEmailWithRoles(email);
        
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            User user = userOpt.get();
            
            // Handle Remember Me cookie (optional for JWT)
            if (rememberMe != null && rememberMe) {
                createRememberMeCookie(user, response);
            }
            
            // Check user role for routing
            boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
            boolean isCSKH = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_CSKH"));
            boolean isVendor = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_VENDOR"));
            boolean isShipper = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_SHIPPER"));
            
            if (isAdmin) {
                return "redirect:/admin/home";
            } else if (isVendor) {
                return "redirect:/vendor/my-shops";
            } else if (isCSKH) {
                return "redirect:/cskh/chat";
            } else if (isShipper) {
                return "redirect:/shipper/home";
            } else {
                return "redirect:/";
            }
        } else {
            model.addAttribute("error", "Email hoặc mật khẩu không đúng!");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        // Hybrid JWT + Session: Clear session
        // Clear Spring Security context
        SecurityContextHolder.clearContext();
        
        // Clear Remember Me cookie
        clearRememberMeCookie(response);
        
        return "redirect:/login";
    }
    
    // Helper method to set Spring Security authentication context (Pure JWT - not used)
    // This method is kept for compatibility but not used in Pure JWT approach
    private void setAuthenticationContext(User user, HttpSession session) {
        // Pure JWT: Authentication context is set by JwtAuthenticationFilter
        // No manual session management needed
    }
    
    // Helper methods for Remember Me functionality
    private void createRememberMeCookie(User user, HttpServletResponse response) {
        try {
            // Create a simple token: userId + timestamp + some random string
            String token = user.getUserId() + ":" + System.currentTimeMillis() + ":" + "oneshop_remember";
            String encodedToken = Base64.getEncoder().encodeToString(token.getBytes());
            
            Cookie cookie = new Cookie("rememberMe", encodedToken);
            cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
            cookie.setPath("/");
            cookie.setHttpOnly(true); // Security: prevent XSS
            response.addCookie(cookie);
        } catch (Exception e) {
            // Log error but don't break login
            System.err.println("Error creating remember me cookie: " + e.getMessage());
        }
    }
    
    private void clearRememberMeCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("rememberMe", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
    
    private User checkRememberMeCookie(HttpServletRequest request) {
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("rememberMe".equals(cookie.getName())) {
                        String encodedToken = cookie.getValue();
                        String token = new String(Base64.getDecoder().decode(encodedToken));
                        String[] parts = token.split(":");
                        
                        if (parts.length == 3 && "oneshop_remember".equals(parts[2])) {
                            Long userId = Long.parseLong(parts[0]);
                            long timestamp = Long.parseLong(parts[1]);
                            
                            // Check if token is not too old (30 days)
                            long thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000;
                            if (System.currentTimeMillis() - timestamp < thirtyDaysInMillis) {
                                Optional<User> userOpt = userRepository.findByIdWithRoles(userId);
                                if (userOpt.isPresent()) {
                                    return userOpt.get();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Log error but don't break the app
            System.err.println("Error checking remember me cookie: " + e.getMessage());
        }
        return null;
    }
}
