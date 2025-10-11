package vn.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.entity.User;
import vn.repository.UserRepository;

import java.util.Base64;
import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String loginPage(HttpServletRequest request, HttpSession session, Model model) {
        // Check if user is already logged in via cookie
        User user = checkRememberMeCookie(request);
        if (user != null) {
            session.setAttribute("user", user);
            
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
            } else if (isCSKH) {
                return "redirect:/cskh/chat";
            } else if (isVendor) {
                return "redirect:/vendor/home";
            } else if (isShipper) {
                return "redirect:/shipper/home";
            } else {
                return "redirect:/";
            }
        }
        
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, 
                       @RequestParam String password,
                       @RequestParam(required = false) Boolean rememberMe,
                       HttpSession session, 
                       HttpServletResponse response,
                       Model model) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            User user = userOpt.get();
            session.setAttribute("user", user);
            
            // Handle Remember Me cookie
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
            } else if (isCSKH) {
                return "redirect:/cskh/chat";
            } else if (isVendor) {
                return "redirect:/vendor/home";
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
    public String logout(HttpSession session, HttpServletResponse response) {
        session.removeAttribute("user");
        
        // Clear Remember Me cookie
        clearRememberMeCookie(response);
        
        return "redirect:/login";
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
                                Optional<User> userOpt = userRepository.findById(userId);
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
