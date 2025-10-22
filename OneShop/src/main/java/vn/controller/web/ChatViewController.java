package vn.controller.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.entity.Role;
import vn.entity.Shop;
import vn.entity.User;
import vn.service.ShopService;
import vn.service.chat.ChatHistoryService;
import vn.util.chat.ChatRoomUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * View controller for realtime chat pages (customer, vendor, CSKH)
 */
@Controller
public class ChatViewController {

    private static final String COOKIE_ROOM = "oneshop_chat_room";
    private static final String COOKIE_NAME = "oneshop_chat_name";
    private static final String COOKIE_GUEST_KEY = "oneshop_chat_guest_key";

    @Autowired
    private ChatHistoryService chatHistoryService;
    @Autowired
    private ShopService shopService;

    @GetMapping("/chat")
    public String customerChatPage(HttpServletRequest request,
                                   HttpServletResponse response,
                                   HttpSession session,
                                   Model model) {
        Shop targetShop = resolveShop(request);
        boolean shopSpecific = targetShop != null;

        // Debug log để kiểm tra shop được resolve
        System.out.println("=== CHAT DEBUG ===");
        System.out.println("Request parameters:");
        request.getParameterMap().forEach((key, values) -> 
            System.out.println("  " + key + " = " + String.join(", ", values)));
        
        if (targetShop != null) {
            System.out.println("Resolved shop: " + targetShop.getShopName() + " (ID: " + targetShop.getShopId() + ")");
        } else {
            System.out.println("No shop resolved - using general chat");
        }
        System.out.println("==================");

        String cookieRoom = getCookie(request, COOKIE_ROOM);
        String cookieName = getCookie(request, COOKIE_NAME);
        String roomId;
        String userName;

        Object userObj = session.getAttribute("user");
        if (userObj instanceof User user) {
            if (shopSpecific && targetShop != null) {
                // Room cho shop cụ thể
                roomId = "shop-" + targetShop.getShopId() + "-customer-" + user.getUserId();
            } else {
                // LUÔN dùng phòng chuẩn theo userId cho người dùng đã đăng nhập (bỏ qua cookieRoom)
                roomId = "support-" + user.getUserId();
                System.out.println("=== CSKH CHAT ROOM ===");
                System.out.println("Generated roomId: " + roomId);
                System.out.println("User ID: " + user.getUserId());
                System.out.println("======================");
            }
            userName = safeName(user.getName(), user.getEmail());
        } else {
            String guestKey = getCookie(request, COOKIE_GUEST_KEY);
            if (guestKey == null || guestKey.isBlank()) {
                guestKey = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
                addCookie(response, COOKIE_GUEST_KEY, guestKey, 60 * 60 * 24 * 365);
            }
            if (shopSpecific && targetShop != null) {
                // Tạo room ID ngay lập tức cho shop chat để có thể load history
                roomId = "shop-" + targetShop.getShopId() + "-guest-" + guestKey;
            } else if (cookieRoom != null && !cookieRoom.isEmpty() && !cookieRoom.startsWith("shop-") && !cookieRoom.startsWith("user-")) {
                // Chỉ sử dụng cookie nếu không phải shop room và không phải user room cũ
                roomId = cookieRoom;
            } else {
                roomId = "support-" + guestKey.substring(0, 8);
            }
            userName = (cookieName != null && !cookieName.isEmpty())
                    ? cookieName
                    : ("khach_" + guestKey.substring(0, Math.min(guestKey.length(), 6)));
        }

        // Debug log cho roomId
        System.out.println("Generated roomId: " + roomId);
        System.out.println("UserName: " + userName);
        System.out.println("Shop chat enabled: " + shopSpecific);
        System.out.println("==================");

        model.addAttribute("chatRoomId", roomId);
        model.addAttribute("chatUserName", userName);
        model.addAttribute("shopChatEnabled", shopSpecific);
        model.addAttribute("chatShopInfo", buildShopInfo(targetShop));
        if (targetShop != null) {
            model.addAttribute("chatShopId", targetShop.getShopId());
        }

        List<Map<String, Object>> initMessages = chatHistoryService.getLastMessages(roomId, 50);
        model.addAttribute("chatInitMessages", initMessages);

        addCookie(response, COOKIE_ROOM, roomId, 60 * 60 * 24 * 30);
        addCookie(response, COOKIE_NAME, userName, 60 * 60 * 24 * 30);

        return "web/chat";
    }

    @GetMapping("/admin/vendor-chat")
    public String vendorChatPage(HttpSession session, Model model) {
        Object userObj = session.getAttribute("user");
        if (userObj instanceof User user) {
            if (user.getRoles() == null) {
                return "redirect:/login";
            }
            boolean vendor = hasRole(user, "ROLE_VENDOR");
            boolean admin = hasRole(user, "ROLE_ADMIN");
            boolean cskh = hasRole(user, "ROLE_CSKH");
            // If this is a CSKH user, prefer the dedicated CSKH route
            if (cskh && !vendor && !admin) {
                return "redirect:/cskh/vendor-chat";
            }
            if (vendor || admin || cskh) {
                java.util.Map<String,Object> ctx = buildVendorContext(user, vendor, admin, cskh);
                if (admin || cskh) {
                    // Build vendor list + their ACTIVE shops for liaison chat
                    try {
                        java.util.Map<Long, java.util.Map<String, Object>> vendors = new java.util.LinkedHashMap<>();
                        java.util.Map<Long, java.util.List<java.util.Map<String,Object>>> vendorShops = new java.util.LinkedHashMap<>();
                        for (Shop s : shopService.findAll()) {
                            if (s.getVendor() == null) continue;
                            vn.entity.User v = s.getVendor();
                            if (!vendors.containsKey(v.getUserId())) {
                                java.util.Map<String, Object> m = new java.util.HashMap<>();
                                m.put("id", v.getUserId());
                                m.put("name", safeName(v.getName(), v.getEmail()));
                                m.put("email", v.getEmail());
                                vendors.put(v.getUserId(), m);
                            }
                            if (s.getStatus() == Shop.ShopStatus.ACTIVE) {
                                java.util.Map<String,Object> sm = new java.util.HashMap<>();
                                sm.put("id", s.getShopId());
                                sm.put("name", s.getShopName());
                                sm.put("status", s.getStatus().name());
                                vendorShops.computeIfAbsent(v.getUserId(), id -> new java.util.ArrayList<>()).add(sm);
                            }
                        }
                        ctx.put("vendors", new java.util.ArrayList<>(vendors.values()));
                        ctx.put("vendorShops", vendorShops);
                    } catch (Exception ignored) {}
                }
                model.addAttribute("chatVendorContext", ctx);
                return "admin/vendor-chat";
            }
        }
        return "redirect:/chat";
    }

    @GetMapping("/cskh/chat")
    public String cskhChatPage(HttpSession session, Model model) {
        Object userObj = session.getAttribute("user");
        if (userObj instanceof User user) {
            if (hasRole(user, "ROLE_CSKH") || hasRole(user, "ROLE_ADMIN")) {
                model.addAttribute("chatVendorContext", buildVendorContext(user, false, true, true));
                return "cskh/chat";
            }
            model.addAttribute("chatVendorContext", buildVendorContext(user, false, false, false));
            return "admin/vendor-chat";
        }
        return "redirect:/login";
    }

    // CSKH liaison view: reuse the vendor chat dashboard so CSKH có thể liên hệ Vendor
    @GetMapping("/cskh/vendor-chat")
    public String cskhVendorChat(HttpSession session, Model model) {
        Object userObj = session.getAttribute("user");
        if (userObj instanceof User user) {
            if (hasRole(user, "ROLE_CSKH") || hasRole(user, "ROLE_ADMIN")) {
                java.util.Map<String,Object> ctx = buildVendorContext(user, false, true, true);
                try {
                    java.util.Map<Long, java.util.Map<String, Object>> vendors = new java.util.LinkedHashMap<>();
                    java.util.Map<Long, java.util.List<java.util.Map<String,Object>>> vendorShops = new java.util.LinkedHashMap<>();
                    for (Shop s : shopService.findAll()) {
                        if (s.getVendor() == null) continue;
                        vn.entity.User v = s.getVendor();
                        vendors.computeIfAbsent(v.getUserId(), id -> {
                            java.util.Map<String, Object> m = new java.util.HashMap<>();
                            m.put("id", v.getUserId());
                            m.put("name", safeName(v.getName(), v.getEmail()));
                            m.put("email", v.getEmail());
                            return m;
                        });
                        // Only expose ACTIVE shops to CSKH liaison
                        if (s.getStatus() == Shop.ShopStatus.ACTIVE) {
                            java.util.Map<String,Object> sm = new java.util.HashMap<>();
                            sm.put("id", s.getShopId());
                            sm.put("name", s.getShopName());
                            sm.put("status", s.getStatus().name());
                            vendorShops.computeIfAbsent(v.getUserId(), id -> new java.util.ArrayList<>()).add(sm);
                        }
                    }
                    ctx.put("vendors", new java.util.ArrayList<>(vendors.values()));
                    ctx.put("vendorShops", vendorShops);
                } catch (Exception ignored) {}
                model.addAttribute("chatVendorContext", ctx);
                return "admin/vendor-chat";
            }
            return "redirect:/admin/vendor-chat"; // fallback
        }
        return "redirect:/login";
    }

    private String getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
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
        response.addCookie(c);
    }

    private String safeName(String name, String fallbackEmail) {
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        if (fallbackEmail != null && !fallbackEmail.trim().isEmpty()) {
            int idx = fallbackEmail.indexOf('@');
            return idx > 0 ? fallbackEmail.substring(0, idx) : fallbackEmail;
        }
        return "khach_" + (int) (Math.random() * 1000);
    }

    private Shop resolveShop(HttpServletRequest request) {
        String shopIdParam = request.getParameter("shopId");
        if (shopIdParam != null && !shopIdParam.isBlank()) {
            try {
                Long shopId = Long.valueOf(shopIdParam.replaceAll("[^0-9]", ""));
                Optional<Shop> shopOpt = shopService.findById(shopId);
                if (shopOpt.isPresent()) {
                    return shopOpt.get();
                }
            } catch (NumberFormatException ignored) {
            }
        }

        String slugParam = request.getParameter("shopSlug");
        if (slugParam != null && !slugParam.isBlank()) {
            return shopService.findBySlug(slugParam.trim()).orElse(null);
        }

        String vendorParam = request.getParameter("vendorId");
        if (vendorParam != null && !vendorParam.isBlank()) {
            try {
                Long vendorId = Long.valueOf(vendorParam.replaceAll("[^0-9]", ""));
                return shopService.findFirstByVendorId(vendorId).orElse(null);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Map<String, Object> buildShopInfo(Shop shop) {
        if (shop == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> info = new HashMap<>();
        info.put("id", shop.getShopId());
        info.put("name", shop.getShopName());
        info.put("slug", shop.getShopSlug());
        info.put("logo", shop.getShopLogo());
        return info;
    }

    private boolean hasRole(User user, String roleName) {
        if (user.getRoles() == null) {
            return false;
        }
        return user.getRoles().stream().map(Role::getName).anyMatch(roleName::equals);
    }

    private Map<String, Object> buildVendorContext(User user, boolean isVendor, boolean isAdmin, boolean isCskh) {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("displayName", safeName(user.getName(), user.getEmail()));
        ctx.put("userId", user.getUserId());
        ctx.put("roles", user.getRoles() == null
                ? List.of()
                : user.getRoles().stream().map(Role::getName).toList());
        ctx.put("isVendor", isVendor);
        ctx.put("isAdmin", isAdmin);
        ctx.put("isCskh", isCskh);
        
        if (isAdmin || isCskh) {
            // CSKH và Admin có thể chat với tất cả khách hàng
            ctx.put("shops", shopService.findAll().stream().map(this::buildShopInfo).toList());
            ctx.put("canChatAllCustomers", true);
        } else if (isVendor) {
            // Vendor chỉ có thể chat với khách hàng của shop mình
            ctx.put("shops", shopService.findAllByVendor(user).stream().map(this::buildShopInfo).toList());
            ctx.put("canChatAllCustomers", false);
        } else {
            ctx.put("shops", List.of());
            ctx.put("canChatAllCustomers", false);
        }
        return ctx;
    }
}
