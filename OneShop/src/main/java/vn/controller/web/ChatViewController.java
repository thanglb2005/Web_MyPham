package vn.controller.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.entity.User;

/**
 * Simple view controller for realtime chat page (no DB)
 */
@Controller
public class ChatViewController {

    @GetMapping("/chat")
    public String customerChatPage(Model model) {
        return "web/chat";
    }
    
    @GetMapping("/admin/vendor-chat")
    public String vendorChatPage(HttpSession session, Model model) {
        Object userObj = session.getAttribute("user");
        if (userObj instanceof User) {
            User user = (User) userObj;
            boolean isVendorOrAdmin = user.getRoles().stream().anyMatch(r ->
                "ROLE_VENDOR".equals(r.getName()) || "ROLE_ADMIN".equals(r.getName())
            );
            if (isVendorOrAdmin) {
                return "admin/vendor-chat";
            }
        }
        // Not vendor/admin: redirect to customer chat
        return "redirect:/chat";
    }
}
