package vn.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.entity.User;
import vn.repository.UserRepository;
import vn.service.CategoryService;

import java.util.Base64;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home(HttpServletRequest request, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        
        // If no user in session, check for Remember Me cookie
        if (user == null) {
            user = checkRememberMeCookie(request);
            if (user != null) {
                session.setAttribute("user", user);
            }
        }
        
        model.addAttribute("user", user);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "index";
    }
    
    // Helper method to check Remember Me cookie (same as in LoginController)
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
