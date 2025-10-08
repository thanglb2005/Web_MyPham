package vn.controller.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.entity.User;
import vn.service.chat.ChatHistoryService;

import java.util.List;
import java.util.Map;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Simple view controller for realtime chat page (no DB)
 */
@Controller
public class ChatViewController {

    @Autowired
    private ChatHistoryService chatHistoryService;

    @GetMapping("/chat")
    public String customerChatPage(HttpServletRequest request, HttpServletResponse response, HttpSession session, Model model) {
        // Stable roomId resolution priority:
        // 1) Cookie 'oneshop_chat_room'
        // 2) Logged-in user specific room 'user-{userId}'
        // 3) New random support room 'support-{random}'
        String cookieRoom = getCookie(request, "oneshop_chat_room");
        String cookieName = getCookie(request, "oneshop_chat_name");

        String roomId;
        String userName;

        Object userObj = session.getAttribute("user");
        if (userObj instanceof User) {
            // FORCE one room per logged-in account
            User u = (User) userObj;
            // If a vendorId is explicitly requested, create a per-pair room (user-seller)
            String vendorParam = request.getParameter("vendorId");
            if (vendorParam != null && !vendorParam.isEmpty()) {
                String vendorIdOnlyDigits = vendorParam.replaceAll("[^0-9]", "");
                if (!vendorIdOnlyDigits.isEmpty()) {
                    roomId = "user-" + u.getUserId() + "-seller-" + vendorIdOnlyDigits;
                } else {
                    roomId = "user-" + u.getUserId();
                }
            } else {
                roomId = "user-" + u.getUserId();
            }
            userName = safeName(u.getName(), u.getEmail());
        } else {
            // Guest: reuse cookie if present, else random
            roomId = (cookieRoom != null && !cookieRoom.isEmpty()) ? cookieRoom : ("support-" + (int)(Math.random() * 1_000_000));
            userName = (cookieName != null && !cookieName.isEmpty()) ? cookieName : ("khach_" + (int)(Math.random() * 1000));
        }

        // Expose to template
        model.addAttribute("chatRoomId", roomId);
        model.addAttribute("chatUserName", userName);
        // Preload last 50 messages so history shows immediately on reload (even if fetch fails)
        List<Map<String, Object>> initMessages = chatHistoryService.getLastMessages(roomId, 50);
        model.addAttribute("chatInitMessages", initMessages);

        // Refresh cookies (30 days)
        addCookie(response, "oneshop_chat_room", roomId, 60 * 60 * 24 * 30);
        addCookie(response, "oneshop_chat_name", userName, 60 * 60 * 24 * 30);

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

    private String getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if (name.equals(c.getName())) {
                try {
                    return URLDecoder.decode(c.getValue(), StandardCharsets.UTF_8);
                } catch (Exception ignored) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
        String safe = value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
        Cookie c = new Cookie(name, safe);
        c.setMaxAge(maxAgeSeconds);
        c.setPath("/");
        // not setting HttpOnly so JS can update if needed
        response.addCookie(c);
    }

    private String safeName(String name, String fallbackEmail) {
        if (name != null && !name.trim().isEmpty()) return name.trim();
        if (fallbackEmail != null && !fallbackEmail.trim().isEmpty()) {
            int idx = fallbackEmail.indexOf('@');
            return idx > 0 ? fallbackEmail.substring(0, idx) : fallbackEmail;
        }
        return "khach_" + (int)(Math.random() * 1000);
    }
}
