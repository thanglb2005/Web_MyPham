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

    @GetMapping("/api/chat/history")
    public List<Map<String, Object>> getHistory(
            @RequestParam("roomId") String roomId,
            @RequestParam(value = "limit", required = false) Integer limit,
            HttpSession session
    ) {
        if (roomId == null || roomId.isEmpty()) {
            return Collections.emptyList();
        }
        ensureRoomAccess(session, roomId);
        return chatHistoryService.getLastMessages(roomId, limit);
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
            
            // CSKH có thể thấy cả support rooms và shop rooms
            if (shopId == null) {
                // Đây là support room hoặc room khác không có shopId
                if (roomId.startsWith("support-") && (admin || cskh)) {
                    // CSKH và Admin có thể thấy support rooms
                    Map<String, Object> map = new HashMap<>();
                    map.put("roomId", roomId);
                    map.put("preview", message.getContent());
                    map.put("sentAt", message.getSentAt());
                    
                    // Lấy customerName từ tin nhắn cuối cùng có customerName
                    String customerName = message.getCustomerName();
                    System.out.println("=== API ROOMS DEBUG ===");
                    System.out.println("Room ID: " + roomId);
                    System.out.println("Latest message customerName: " + customerName);
                    
                    if (customerName == null || customerName.isEmpty()) {
                        // Nếu tin nhắn cuối cùng không có customerName, tìm tin nhắn cuối cùng có customerName
                        List<String> customerNames = chatMessageRepository.findLatestCustomerName(roomId, PageRequest.of(0, 1));
                        customerName = customerNames.isEmpty() ? "Khách hàng" : customerNames.get(0);
                        System.out.println("Found customerName from history: " + customerName);
                    }
                    
                    System.out.println("Final customerName: " + customerName);
                    System.out.println("=======================");
                    
                    map.put("customerName", customerName);
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
            try {
                List<String> names = chatMessageRepository.findLatestCustomerName(
                        roomId, PageRequest.of(0, 1)
                );
                if (names != null && !names.isEmpty()) {
                    map.put("customerName", names.get(0));
                }
            } catch (Exception ignored) {
            }
            filtered.add(map);
        }

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
            if (hasRole(user, "ROLE_VENDOR")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            return;
        }
        Long shopId = shopIdOpt.get();
        boolean ownsShop = hasRole(user, "ROLE_VENDOR")
                && shopService.findByIdAndVendor(shopId, user).isPresent();
        if (!ownsShop) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
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
