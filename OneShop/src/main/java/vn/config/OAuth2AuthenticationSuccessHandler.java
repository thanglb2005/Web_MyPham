package vn.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import vn.entity.User;
import vn.service.CustomOAuth2User;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();
        
        // Store user in session (compatible with existing login system)
        HttpSession session = request.getSession();
        session.setAttribute("user", user);
        
        // Redirect based on user role
        boolean isAdmin = user.getRoles().stream()
            .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        boolean isCSKH = user.getRoles().stream()
            .anyMatch(role -> role.getName().equals("ROLE_CSKH"));
        boolean isVendor = user.getRoles().stream()
            .anyMatch(role -> role.getName().equals("ROLE_VENDOR"));
        boolean isShipper = user.getRoles().stream()
            .anyMatch(role -> role.getName().equals("ROLE_SHIPPER"));
        
        if (isAdmin) {
            response.sendRedirect("/admin/home");
        } else if (isVendor) {
            response.sendRedirect("/vendor/my-shops");
        } else if (isCSKH) {
            response.sendRedirect("/cskh/chat");
        } else if (isShipper) {
            response.sendRedirect("/shipper/home");
        } else {
            response.sendRedirect("/");
        }
    }
}
