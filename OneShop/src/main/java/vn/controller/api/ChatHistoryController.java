package vn.controller.api;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import vn.entity.ChatMessage;
import vn.entity.Role;
import vn.entity.Shop;
import vn.entity.User;
import vn.repository.ChatMessageRepository;
import vn.service.ShopService;
import vn.service.chat.ChatHistoryService;
import vn.service.UserService;
import vn.util.chat.ChatRoomUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class ChatHistoryController {

    @Autowired
    private ChatHistoryService chatHistoryService;
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private ShopService shopService;
    @Autowired
    private UserService userService;

    @GetMapping("/api/chat/conversations")
    public ResponseEntity<List<Map<String, Object>>> getUserConversations(HttpSession session) {
        User user = currentUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
        }

        // Chỉ cho phép user thường (không phải admin/vendor/cskh)
        boolean admin = hasRole(user, "ROLE_ADMIN");
        boolean cskh = hasRole(user, "ROLE_CSKH");
        boolean vendor = hasRole(user, "ROLE_VENDOR");
        
        if (admin || cskh || vendor) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.emptyList());
        }

        try {
            System.out.println("=== CONVERSATIONS API DEBUG ===");
            System.out.println("User: " + user.getName());
            System.out.println("User roles: " + user.getRoles());
            
            // Debug: Lấy tất cả tin nhắn để xem customer_name thực tế
            List<ChatMessage> allMessages = chatMessageRepository.findAll();
            System.out.println("Total messages in database: " + allMessages.size());
            for (ChatMessage msg : allMessages) {
                System.out.println("Message customer_name: '" + msg.getCustomerName() + "' | Room: " + msg.getRoomId() + " | Content: " + msg.getContent().substring(0, Math.min(30, msg.getContent().length())));
            }
            
            // Lấy tất cả tin nhắn của user này với các shop
            // Thử cách 1: Tìm theo tên user
            List<ChatMessage> userMessages = chatMessageRepository.findByCustomerNameAndRoomIdStartingWith(
                user.getName(), "shop-"
            );
            
            // Nếu không tìm thấy, thử cách 2: Tìm theo user ID trong room ID
            if (userMessages.isEmpty()) {
                System.out.println("No messages found by name, trying by user ID...");
                String roomPattern = "shop-%customer-" + user.getUserId();
                userMessages = chatMessageRepository.findByRoomIdStartingWith(roomPattern);
                System.out.println("Found " + userMessages.size() + " messages by user ID pattern: " + roomPattern);
            }
            
            // Nếu vẫn không tìm thấy, thử cách 3: Tìm tất cả shop messages và filter
            if (userMessages.isEmpty()) {
                System.out.println("No messages found by user ID, trying all shop messages...");
                List<ChatMessage> allShopMessages = chatMessageRepository.findByRoomIdStartingWith("shop-%");
                System.out.println("Total shop messages: " + allShopMessages.size());
                
                // Filter messages where room ID contains user ID
                userMessages = allShopMessages.stream()
                    .filter(msg -> msg.getRoomId().contains("customer-" + user.getUserId()))
                    .collect(Collectors.toList());
                System.out.println("Filtered messages for user " + user.getUserId() + ": " + userMessages.size());
            }
            
            System.out.println("Found " + userMessages.size() + " messages for user " + user.getName());
            for (ChatMessage msg : userMessages) {
                System.out.println("Message: " + msg.getRoomId() + " | " + msg.getContent().substring(0, Math.min(50, msg.getContent().length())));
            }

            // Group theo shop và tìm tin nhắn cuối cùng
            Map<Long, ChatMessage> latestMessagesByShop = new HashMap<>();
            Map<Long, Integer> unreadCountByShop = new HashMap<>();
            
            for (ChatMessage message : userMessages) {
                Long shopId = ChatRoomUtils.extractShopId(message.getRoomId()).orElse(null);
                if (shopId == null) continue;
                
                // Cập nhật tin nhắn cuối cùng
                ChatMessage latest = latestMessagesByShop.get(shopId);
                if (latest == null || message.getSentAt() > latest.getSentAt()) {
                    latestMessagesByShop.put(shopId, message);
                }
                
                // Đếm tin nhắn chưa đọc (từ vendor/cskh)
                if ("vendor".equals(message.getSenderType()) || "cskh".equals(message.getSenderType())) {
                    unreadCountByShop.put(shopId, unreadCountByShop.getOrDefault(shopId, 0) + 1);
                }
            }

            System.out.println("Latest messages by shop: " + latestMessagesByShop.size());
            for (Map.Entry<Long, ChatMessage> entry : latestMessagesByShop.entrySet()) {
                System.out.println("Shop " + entry.getKey() + ": " + entry.getValue().getRoomId() + " | " + entry.getValue().getContent().substring(0, Math.min(30, entry.getValue().getContent().length())));
            }

            // Tạo danh sách conversations
            List<Map<String, Object>> conversations = new ArrayList<>();
            for (Map.Entry<Long, ChatMessage> entry : latestMessagesByShop.entrySet()) {
                Long shopId = entry.getKey();
                ChatMessage latestMessage = entry.getValue();
                
                // Lấy thông tin shop
                Optional<Shop> shopOpt = shopService.findById(shopId);
                if (shopOpt.isEmpty()) continue;
                
                Shop shop = shopOpt.get();
                
                Map<String, Object> conversation = new HashMap<>();
                conversation.put("shopId", shopId);
                conversation.put("shopName", shop.getShopName());
                conversation.put("shopLogo", shop.getShopLogo());
                conversation.put("roomId", latestMessage.getRoomId());
                conversation.put("room", latestMessage.getRoomId()); // Thêm room parameter
                conversation.put("user", user.getName()); // Thêm user parameter
                conversation.put("lastMessage", latestMessage.getContent());
                conversation.put("lastMessageTime", latestMessage.getSentAt());
                conversation.put("unreadCount", unreadCountByShop.getOrDefault(shopId, 0));
                
                conversations.add(conversation);
            }

            // Sort theo thời gian tin nhắn cuối
            conversations.sort((a, b) -> {
                Long timeA = (Long) a.get("lastMessageTime");
                Long timeB = (Long) b.get("lastMessageTime");
                return timeB.compareTo(timeA);
            });

            return ResponseEntity.ok(conversations);
            
        } catch (Exception e) {
            System.err.println("Error loading conversations: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/api/chat/history")
    public List<Map<String, Object>> getHistory(
            @RequestParam("roomId") String roomId,
            @RequestParam(value = "limit", required = false) Integer limit,
            HttpSession session
    ) {
        System.out.println("[API] getHistory roomId=" + roomId + ", limit=" + limit);
        if (roomId == null || roomId.isEmpty()) {
            return Collections.emptyList();
        }
        ensureRoomAccess(session, roomId);
        List<Map<String, Object>> out = chatHistoryService.getLastMessages(roomId, limit);
        System.out.println("[API] getHistory size=" + (out!=null?out.size():0));
        return out;
    }

    @GetMapping("/api/chat/rooms")
    public List<Map<String, Object>> getRecentRooms(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "shopId", required = false) Long requestedShopId,
            HttpSession session
    ) {
        User user = currentUser(session);
        if (user == null) {
            return Collections.emptyList();
        }

        boolean admin = hasRole(user, "ROLE_ADMIN");
        boolean cskh = hasRole(user, "ROLE_CSKH");
        boolean vendor = hasRole(user, "ROLE_VENDOR");
        if (!admin && !cskh && !vendor) {
            return Collections.emptyList();
        }

        List<Long> allowedShopIds = resolveAllowedShopIds(user, admin, cskh, vendor);
        if (requestedShopId != null) {
            if (!allowedShopIds.contains(requestedShopId) && !admin && !cskh) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Khong co quyen xem shop nay");
            }
            allowedShopIds = allowedShopIds.stream()
                    .filter(id -> id.equals(requestedShopId))
                    .collect(Collectors.toList());
            if (allowedShopIds.isEmpty() && (admin || cskh)) {
                allowedShopIds = List.of(requestedShopId);
            }
        }

        int max = (limit == null || limit <= 0) ? 50 : Math.min(limit, 500);
        List<ChatMessage> lastPerRoom = chatMessageRepository.findLatestPerRoom(
                PageRequest.of(0, max, Sort.by(Sort.Direction.DESC, "sentAt"))
        );

        Map<Long, Shop> shopCache = new HashMap<>();
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (ChatMessage message : lastPerRoom) {
            String roomId = message.getRoomId();
            Long shopId = ChatRoomUtils.extractShopId(roomId).orElse(null);

            // Handle liaison rooms (CSKH ↔ Vendor) which don't have shopId embedded
            if (shopId == null && roomId != null && roomId.startsWith("liaison-vendor-")) {
                Long vendorId = null;
                try {
                    int startIdx = "liaison-vendor-".length();
                    int endIdx = roomId.indexOf('-', startIdx);
                    if (endIdx > startIdx) vendorId = Long.parseLong(roomId.substring(startIdx, endIdx));
                } catch (Exception ignored) {}
                if (vendorId != null) {
                    if ((admin || cskh) || (vendor && user.getUserId().equals(vendorId))) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("roomId", roomId);
                        map.put("preview", message.getContent());
                        map.put("sentAt", message.getSentAt());
                        String vendorName = null;
                        try {
                            User v = userService.getUserById(vendorId).orElse(null);
                            if (v != null) vendorName = v.getName();
                        } catch (Exception ignored) {}
                        String shopName = null;
                        try {
                            final Long vId = vendorId;
                            shopName = shopService.findAll().stream()
                                    .filter(s -> s.getVendor() != null && s.getVendor().getUserId().equals(vId))
                                    .map(Shop::getShopName)
                                    .findFirst().orElse(null);
                        } catch (Exception ignored) {}
                        if (admin || cskh) {
                            map.put("customerName", (vendorName != null ? vendorName : ("Vendor #" + vendorId)) + (shopName != null ? (" - " + shopName) : ""));
                        } else {
                            map.put("customerName", "CSKH");
                        }
                        map.put("pendingCount", 0);
                        filtered.add(map);
                        continue;
                    }
                }
            }
            
            // CSKH có thể thấy cả support rooms và shop rooms
            if (shopId == null) {                // Liaison-shop rooms (CSKH ? Vendor by shop)
                if (roomId.startsWith("liaison-shop-")) {
                    Long lsId = vn.util.chat.ChatRoomUtils.extractLiaisonShopId(roomId).orElse(null);
                    if (lsId != null && (admin || cskh || (vendor && shopService.findByIdAndVendor(lsId, user).isPresent()))) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("roomId", roomId);
                        map.put("preview", message.getContent());
                        map.put("sentAt", message.getSentAt());
                        map.put("pendingCount", 0);
                        map.put("shopId", lsId);
                        try {
                            Shop s = shopService.findById(lsId).orElse(null);
                            if (s != null) {
                                map.put("shopName", s.getShopName());
                                map.put("customerName", (admin || cskh) ? (s.getVendor() != null ? s.getVendor().getName() : ("Shop #" + lsId)) : "CSKH");
                            }
                        } catch (Exception ignored) {}
                        filtered.add(map);
                        continue;
                    }
                }
                // Đây là support room hoặc room khác không có shopId
                if (roomId.startsWith("support-") && (admin || cskh)) {
                    // CSKH và Admin có thể thấy support rooms
                    Map<String, Object> map = new HashMap<>();
                    map.put("roomId", roomId);
                    map.put("preview", message.getContent());
                    map.put("sentAt", message.getSentAt());

                    // Ưu tiên ánh xạ tên khách dựa trên roomId khi là user đã đăng nhập: support-<userId>
                    String customerName = null;
                    try {
                        String suffix = roomId.substring("support-".length());
                        if (suffix.matches("\\d+")) {
                            Long uid = Long.parseLong(suffix);
                            userService.getUserById(uid).ifPresent(u -> {
                                map.put("customerName", u.getName());
                            });
                            customerName = (String) map.get("customerName");
                        }
                    } catch (Exception ignored) {}

                    // Nếu không xác định được qua roomId (khách vãng lai), fallback theo lịch sử tin nhắn
                    if (customerName == null || customerName.isBlank()) {
                        customerName = message.getCustomerName();
                        if (customerName == null || customerName.isEmpty()) {
                            List<String> customerNames = chatMessageRepository.findLatestCustomerName(roomId, PageRequest.of(0, 1));
                            customerName = customerNames.isEmpty() ? "Khách hàng" : customerNames.get(0);
                        }
                        map.put("customerName", customerName);
                    }

                    map.put("pendingCount", 0);
                    filtered.add(map);
                }
                continue;
            }
            if (!allowedShopIds.isEmpty() && !allowedShopIds.contains(shopId) && !admin && !cskh) {
                continue;
            }
            Shop shop = shopCache.computeIfAbsent(shopId, id ->
                    shopService.findById(id).orElse(null)
            );
            if (shop == null) {
                continue;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("roomId", roomId);
            map.put("shopId", shopId);
            map.put("shopName", shop.getShopName());
            map.put("lastMessageBy", message.getSender());
            map.put("lastMessageType", message.getMessageType());
            map.put("lastMessage", message.getContent());
            map.put("sentAt", message.getSentAt());
            ChatRoomUtils.extractCustomerId(roomId).ifPresent(id -> map.put("customerId", id));
            ChatRoomUtils.extractGuestKey(roomId).ifPresent(key -> map.put("guestKey", key));
            
            // Resolve customer name: ưu tiên từ user table qua customerId, fallback từ message history
            String customerName = null;
            Optional<Long> customerIdOpt = ChatRoomUtils.extractCustomerId(roomId);
            if (customerIdOpt.isPresent()) {
                Long customerId = customerIdOpt.get();
                Optional<User> customerUser = userService.getUserById(customerId);
                if (customerUser.isPresent()) {
                    customerName = customerUser.get().getName();
                }
            }
            
            // Fallback to message history if not found via user ID
            if (customerName == null || customerName.isEmpty()) {
                try {
                    List<String> names = chatMessageRepository.findLatestCustomerName(
                            roomId, PageRequest.of(0, 1)
                    );
                    if (names != null && !names.isEmpty()) {
                        customerName = names.get(0);
                    }
                } catch (Exception ignored) {
                }
            }
            
            if (customerName != null && !customerName.isEmpty()) {
                map.put("customerName", customerName);
            }
            filtered.add(map);
        }

        try {
            System.out.println("[API] getRecentRooms -> count=" + filtered.size());
            for (Map<String, Object> m : filtered) {
                System.out.println("  roomId=" + m.get("roomId") + ", customerName=" + m.get("customerName") + ", preview=" + m.get("preview"));
            }
        } catch (Exception ignored) {}
        return filtered;
    }

    @DeleteMapping("/api/chat/history")
    public ResponseEntity<Map<String, Object>> clearHistory(@RequestParam("roomId") String roomId,
                                                            HttpSession session) {
        if (roomId == null || roomId.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("success", false));
        }
        ensureRoomAccess(session, roomId);
        boolean ok = chatHistoryService.deleteRoomHistory(roomId);
        return ResponseEntity.ok(Collections.singletonMap("success", ok));
    }

    @PostMapping("/api/chat/history/clear")
    public ResponseEntity<Map<String, Object>> clearHistoryPost(@RequestParam("roomId") String roomId,
                                                                HttpSession session) {
        return clearHistory(roomId, session);
    }

    @PostMapping("/api/chat/history/clearByPair")
    public ResponseEntity<Map<String, Object>> clearByPair(
            @RequestParam("shopId") Long shopId,
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestParam(value = "guestKey", required = false) String guestKey,
            HttpSession session
    ) {
        if (shopId == null || (customerId == null && (guestKey == null || guestKey.isBlank()))) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("success", false));
        }
        String roomId = ChatRoomUtils.buildShopRoomId(shopId, customerId, guestKey);
        ensureRoomAccess(session, roomId);
        boolean ok = chatHistoryService.deleteRoomHistory(roomId);
        return ResponseEntity.ok(Collections.singletonMap("success", ok));
    }

    private void ensureRoomAccess(HttpSession session, String roomId) {
        User user = currentUser(session);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        boolean admin = hasRole(user, "ROLE_ADMIN");
        boolean cskh = hasRole(user, "ROLE_CSKH");
        if (admin || cskh) {
            return;
        }
        
        Optional<Long> shopIdOpt = ChatRoomUtils.extractShopId(roomId);
        if (shopIdOpt.isEmpty()) {
            // Non-shop rooms: allow Admin/CSKH; for Vendor allow if liaison room targets them
            boolean isVendor = hasRole(user, "ROLE_VENDOR");
            if (isVendor) {
                String rid = roomId != null ? roomId : "";
                if (rid.startsWith("liaison-vendor-")) {
                    try {
                        int start = "liaison-vendor-".length();
                        int end = rid.indexOf('-', start);
                        Long vendorId = end > start ? Long.parseLong(rid.substring(start, end)) : null;
                        if (vendorId != null && vendorId.equals(user.getUserId())) {
                            return; // vendor can access their liaison rooms
                        }
                    } catch (Exception ignored) {}
                }
                if (rid.startsWith("liaison-shop-")) {
                    try {
                        Long lsId = vn.util.chat.ChatRoomUtils.extractLiaisonShopId(rid).orElse(null);
                        if (lsId != null && shopService.findByIdAndVendor(lsId, user).isPresent()) {
                            return; // vendor can access liaison-shop rooms of their shops
                        }
                    } catch (Exception ignored) {}
                }
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            return;
        }
        
        Long shopId = shopIdOpt.get();
        
        // Check if user is vendor and owns the shop
        boolean ownsShop = hasRole(user, "ROLE_VENDOR")
                && shopService.findByIdAndVendor(shopId, user).isPresent();
        if (ownsShop) {
            return;
        }
        
        // Check if user is customer and has access to this shop room
        boolean isCustomer = !hasRole(user, "ROLE_VENDOR") && !hasRole(user, "ROLE_ADMIN") && !hasRole(user, "ROLE_CSKH");
        if (isCustomer) {
            // For customers, check if roomId contains their user ID
            Optional<Long> customerIdOpt = ChatRoomUtils.extractCustomerId(roomId);
            if (customerIdOpt.isPresent() && customerIdOpt.get().equals(user.getUserId())) {
                return; // Customer can access their own room
            }
        }
        
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    private User currentUser(HttpSession session) {
        Object userObj = session.getAttribute("user");
        if (userObj instanceof User user) {
            return user;
        }
        return null;
    }

    private boolean hasRole(User user, String roleName) {
        if (user.getRoles() == null) {
            return false;
        }
        return user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(roleName::equals);
    }

    private List<Long> resolveAllowedShopIds(User user, boolean admin, boolean cskh, boolean vendor) {
        if (admin || cskh) {
            return shopService.findAll().stream()
                    .map(Shop::getShopId)
                    .collect(Collectors.toList());
        }
        if (vendor) {
            return shopService.findAllByVendor(user).stream()
                    .map(Shop::getShopId)
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}

