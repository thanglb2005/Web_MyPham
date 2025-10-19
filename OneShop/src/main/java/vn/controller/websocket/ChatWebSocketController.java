package vn.controller.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import vn.service.chat.ChatHistoryService;
import vn.entity.ChatMessage;
import vn.entity.Shop;
import vn.entity.User;
import vn.repository.ChatMessageRepository;
import vn.service.ShopService;
import vn.service.UserService;
import vn.util.chat.ChatRoomUtils;
import vn.service.chat.SupportAssignmentService;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced WebSocket controller for vendor-customer real-time chat
 */
@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ChatHistoryService chatHistoryService;
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private ShopService shopService;
    @Autowired
    private UserService userService;
    @Autowired
    private SupportAssignmentService supportAssignmentService;
    
    // In-memory storage for active users (room-based)
    private final Map<String, Map<String, String>> activeUsers = new ConcurrentHashMap<>();
    // Which vendor is assigned to which room (ephemeral, in-memory)
    private final Map<String, String> assignedVendorByRoom = new ConcurrentHashMap<>();
    // Pending customer messages count per room (before a vendor is assigned)
    private final Map<String, Integer> pendingCountByRoom = new ConcurrentHashMap<>();
    // Cache room -> shopId resolution
    private final Map<String, Long> shopIdByRoom = new ConcurrentHashMap<>();
    // Cache shop names to avoid repeated DB hits
    private final Map<Long, String> shopNameCache = new ConcurrentHashMap<>();
    
    /**
     * Check if vendor has permission to access the shop
     */
    private boolean hasVendorShopAccess(String vendorName, Long shopId) {
        try {
            // Tìm user bằng tên hoặc email
            Optional<User> userOpt = userService.findByName(vendorName);
            if (userOpt.isEmpty()) {
                // Thử tìm bằng email nếu không tìm thấy bằng tên
                try {
                    User userByEmail = userService.findByEmail(vendorName);
                    if (userByEmail != null) {
                        userOpt = Optional.of(userByEmail);
                    }
                } catch (Exception e) {
                    // User not found by email
                }
            }
            
            if (userOpt.isEmpty()) {
                System.err.println("User not found for vendorName: " + vendorName);
                return false;
            }
            
            User user = userOpt.get();
            
            // Check if user has ROLE_ADMIN or ROLE_CSKH (can access all shops)
            boolean isAdminOrCskh = user.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()) || "ROLE_CSKH".equals(role.getName()));
            
            if (isAdminOrCskh) {
                System.out.println("Admin/CSKH access granted for user: " + user.getName());
                return true;
            }
            
            // Check if user is vendor and owns this shop
            boolean isVendor = user.getRoles().stream()
                .anyMatch(role -> "ROLE_VENDOR".equals(role.getName()));
            
            if (isVendor) {
                boolean hasAccess = shopService.findAllByVendor(user).stream()
                    .anyMatch(shop -> shop.getShopId().equals(shopId));
                System.out.println("Vendor access check for user: " + user.getName() + ", shopId: " + shopId + ", hasAccess: " + hasAccess);
                return hasAccess;
            }
            
            System.err.println("User is not vendor/admin/cskh: " + user.getName());
            return false;
        } catch (Exception e) {
            System.err.println("Error checking vendor shop access: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Map<String, Object> payload, Principal principal) {
        try {
            String roomId = String.valueOf(payload.getOrDefault("roomId", "public"));
            String content = String.valueOf(payload.getOrDefault("messageContent", ""));
            String messageType = String.valueOf(payload.getOrDefault("messageType", "TEXT"));
            // Always prefer senderName from payload to avoid sticky HTTP session principal
            String senderName = String.valueOf(payload.getOrDefault("senderName", "")).trim();
            if (senderName.isEmpty()) {
                senderName = "guest_" + (System.currentTimeMillis() % 100000);
            }

            // Enhanced message object
            Map<String, Object> message = new HashMap<>();
            message.put("roomId", roomId);
            message.put("sender", senderName);
            Long shopId = ChatRoomUtils.extractShopId(roomId).orElse(null);
            if (shopId == null) {
                shopId = shopIdByRoom.get(roomId);
            }
            if (shopId != null) {
                shopIdByRoom.putIfAbsent(roomId, shopId);
                message.put("shopId", shopId);
                String shopName = resolveShopName(shopId);
                if (shopName != null) {
                    message.put("shopName", shopName);
                }
            }
            // For IMAGE messages, keep URL as-is; for TEXT, sanitize
            if ("IMAGE".equalsIgnoreCase(messageType)) {
                message.put("messageContent", content);
            } else {
                message.put("messageContent", sanitizeContent(content));
            }
            message.put("messageType", messageType);
            message.put("sentAt", System.currentTimeMillis());
            message.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            
            // Determine sender type (vendor, customer, system, cskh)
            String userTypeOverride = String.valueOf(payload.getOrDefault("userType", "")).trim().toLowerCase();
            String senderTypeOverride = String.valueOf(payload.getOrDefault("senderType", "")).trim().toLowerCase();
            String senderType = ("vendor".equals(userTypeOverride) || "customer".equals(userTypeOverride) || "system".equals(userTypeOverride) || "cskh".equals(userTypeOverride))
                ? userTypeOverride
                : ("vendor".equals(senderTypeOverride) || "customer".equals(senderTypeOverride) || "system".equals(senderTypeOverride) || "cskh".equals(senderTypeOverride))
                ? senderTypeOverride
                : determineSenderType(senderName);
            message.put("senderType", senderType);

            // Broadcast to room
            messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
            // Persist message to database for durable history
            try {
                ChatMessage m = new ChatMessage();
                m.setRoomId(roomId);
                m.setSender(senderName);
                m.setSenderType(senderType);
                m.setMessageType(messageType);
                m.setContent(String.valueOf(message.getOrDefault("messageContent", "")));
                Object sentAtObj = message.get("sentAt");
                long sentAt = sentAtObj instanceof Number ? ((Number) sentAtObj).longValue() : System.currentTimeMillis();
                m.setSentAt(sentAt);
                if ("customer".equalsIgnoreCase(senderType)) {
                    m.setCustomerName(senderName);
                }
                String vendor = assignedVendorByRoom.get(roomId);
                if (vendor != null && !vendor.isEmpty()) {
                    m.setVendorName(vendor);
                }
                chatMessageRepository.save(m);
                
                // Debug log để kiểm tra tin nhắn được lưu
                System.out.println("=== MESSAGE SAVED ===");
                System.out.println("Room ID: " + roomId);
                System.out.println("Sender: " + senderName);
                System.out.println("Sender Type: " + senderType);
                System.out.println("Customer Name: " + m.getCustomerName());
                System.out.println("Vendor Name: " + m.getVendorName());
                System.out.println("Content: " + message.getOrDefault("messageContent", ""));
                System.out.println("=====================");
            } catch (Exception e) {
                System.err.println("Error saving message: " + e.getMessage());
            }
            
            // Update vendor dashboard room summary events for real-world workflow
            if ("customer".equalsIgnoreCase(senderType)) {
                // If no vendor assigned yet, increase pending and notify vendors
                if (!assignedVendorByRoom.containsKey(roomId)) {
                    int pending = pendingCountByRoom.merge(roomId, 1, Integer::sum);
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("type", "ROOM_MESSAGE");
                    summary.put("roomId", roomId);
                    summary.put("from", senderName);
                    summary.put("preview", content.length() > 100 ? content.substring(0, 100) + "..." : content);
                    summary.put("pendingCount", pending);
                    summary.put("sentAt", System.currentTimeMillis());
                    if (shopId != null) {
                        summary.put("shopId", shopId);
                        String shopName = resolveShopName(shopId);
                        if (shopName != null) {
                            summary.put("shopName", shopName);
                        }
                    }
                    messagingTemplate.convertAndSend("/topic/rooms", summary);
                }
            } else if ("vendor".equalsIgnoreCase(senderType)) {
                // Check if vendor has permission to send messages to this shop
                System.out.println("=== VENDOR ACCESS CHECK ===");
                System.out.println("senderName: " + senderName);
                System.out.println("shopId: " + shopId);
                System.out.println("roomId: " + roomId);
                if (shopId != null && !hasVendorShopAccess(senderName, shopId)) {
                    // Send error message to vendor
                    Map<String, Object> errorMsg = new HashMap<>();
                    errorMsg.put("type", "ACCESS_DENIED");
                    errorMsg.put("message", "Bạn không có quyền gửi tin nhắn trong shop này");
                    errorMsg.put("roomId", roomId);
                    messagingTemplate.convertAndSendToUser(senderName, "/queue/errors", errorMsg);
                    return; // Exit without sending message
                }
            } else if ("cskh".equalsIgnoreCase(senderType)) {
                // CSKH can send messages to any room (no access control needed)
                System.out.println("=== CSKH MESSAGE ===");
                System.out.println("senderName: " + senderName);
                System.out.println("roomId: " + roomId);
                System.out.println("CSKH can access all rooms");
            }
            
            // First vendor message claims the room if not yet assigned
            if ("vendor".equalsIgnoreCase(senderType)) {
                assignedVendorByRoom.putIfAbsent(roomId, senderName);
                pendingCountByRoom.put(roomId, 0);
                Map<String, Object> summary = new HashMap<>();
                summary.put("type", "ROOM_UPDATED");
                summary.put("roomId", roomId);
                summary.put("lastMessageBy", senderName);
                summary.put("pendingCount", 0);
                summary.put("sentAt", System.currentTimeMillis());
                if (shopId != null) {
                    summary.put("shopId", shopId);
                    String shopName = resolveShopName(shopId);
                    if (shopName != null) {
                        summary.put("shopName", shopName);
                    }
                }
                messagingTemplate.convertAndSend("/topic/rooms", summary);
            }
            
            // Log message for debugging
            System.out.println(String.format("[CHAT] Room: %s, Sender: %s (%s), Message: %s", 
                roomId, senderName, senderType, content.substring(0, Math.min(content.length(), 50))));
                
        } catch (Exception e) {
            handleError(e, principal, "Error sending message");
        }
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload Map<String, Object> payload, Principal principal) {
        try {
            String roomId = String.valueOf(payload.getOrDefault("roomId", "public"));
            boolean isTyping = Boolean.parseBoolean(String.valueOf(payload.getOrDefault("isTyping", false)));
            // Prefer senderName from payload to avoid sticky principal during testing
            String userName = String.valueOf(payload.getOrDefault("senderName", "")).trim();
            if (userName.isEmpty()) {
                userName = "guest";
            }

            Map<String, Object> typingNotification = new HashMap<>();
            typingNotification.put("userName", userName);
            typingNotification.put("isTyping", isTyping);
            String userTypeOverride = String.valueOf(payload.getOrDefault("userType", "")).trim().toLowerCase();
            String senderType = ("vendor".equals(userTypeOverride) || "customer".equals(userTypeOverride) || "system".equals(userTypeOverride))
                ? userTypeOverride
                : determineSenderType(userName);
            typingNotification.put("senderType", senderType);
            typingNotification.put("timestamp", System.currentTimeMillis());

            // Broadcast typing indicator to room
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/typing", typingNotification);
            
        } catch (Exception e) {
            handleError(e, principal, "Error sending typing indicator");
        }
    }
    
    @MessageMapping("/chat.join")
    public void joinRoom(@Payload Map<String, Object> payload, Principal principal) {
        try {
            String roomId = String.valueOf(payload.getOrDefault("roomId", "public"));
            // Always take userName/userType from payload for deterministic roles across reloads
            String userName = String.valueOf(payload.getOrDefault("senderName", "")).trim();
            if (userName.isEmpty()) userName = "guest";
            String userType = String.valueOf(payload.getOrDefault("userType", "customer"));
            Long shopId = ChatRoomUtils.extractShopId(roomId).orElse(null);
            if (shopId == null) {
                shopId = shopIdByRoom.get(roomId);
            }
            if (shopId != null) {
                shopIdByRoom.putIfAbsent(roomId, shopId);
            }
            String shopName = shopId != null ? resolveShopName(shopId) : null;

            // Detect if this is a brand new room before adding user
            boolean isNewRoom = !activeUsers.containsKey(roomId);

            // Add user to active users
            Map<String, String> roomUsers = activeUsers.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());
            roomUsers.put(userName, userType);

            // Notify room about new user
            Map<String, Object> joinNotification = new HashMap<>();
            joinNotification.put("type", "USER_JOINED");
            joinNotification.put("userName", userName);
            joinNotification.put("userType", userType);
            joinNotification.put("roomId", roomId);
            joinNotification.put("timestamp", System.currentTimeMillis());
            joinNotification.put("activeUsers", roomUsers);
            if (shopId != null) {
                joinNotification.put("shopId", shopId);
                if (shopName != null) {
                    joinNotification.put("shopName", shopName);
                }
            }

            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/users", joinNotification);

            // Broadcast global room-open event so vendors/CSKH see and pick up the conversation
            if (isNewRoom && "customer".equalsIgnoreCase(userType)) {
                Map<String, Object> roomOpenEvent = new HashMap<>();
                roomOpenEvent.put("type", "ROOM_OPEN");
                roomOpenEvent.put("roomId", roomId);
                roomOpenEvent.put("customerName", userName);
                roomOpenEvent.put("createdAt", System.currentTimeMillis());
                if (shopId != null) {
                    roomOpenEvent.put("shopId", shopId);
                    if (shopName != null) {
                        roomOpenEvent.put("shopName", shopName);
                    }
                }
                messagingTemplate.convertAndSend("/topic/rooms", roomOpenEvent);

                // Persist a lightweight system message to tag this room with customer name for vendor lists
                Map<String, Object> tagMsg = new HashMap<>();
                tagMsg.put("roomId", roomId);
                tagMsg.put("sender", "system");
                tagMsg.put("senderType", "system");
                tagMsg.put("messageType", "SYSTEM");
                tagMsg.put("messageContent", "Phòng được tạo bởi " + userName);
                tagMsg.put("customerName", userName);
                tagMsg.put("sentAt", System.currentTimeMillis());
                if (shopId != null) {
                    tagMsg.put("shopId", shopId);
                    if (shopName != null) {
                        tagMsg.put("shopName", shopName);
                    }
                }
                chatHistoryService.appendMessage(roomId, tagMsg);
            }

            // Enforce assignment for liaison-shop rooms: one CSKH per shop
            if ("cskh".equalsIgnoreCase(userType) && roomId != null && roomId.startsWith("liaison-shop-")) {
                Long liaisonShopId = vn.util.chat.ChatRoomUtils.extractLiaisonShopId(roomId).orElse(null);
                Long liaisonCskhId = vn.util.chat.ChatRoomUtils.extractLiaisonCskhId(roomId).orElse(null);
                if (liaisonShopId != null && liaisonCskhId != null) {
                    boolean ok = supportAssignmentService.ensureAssignment(liaisonShopId, liaisonCskhId);
                    if (!ok) {
                        Map<String, Object> errorMsg = new HashMap<>();
                        errorMsg.put("type", "ACCESS_DENIED");
                        errorMsg.put("message", "Shop này đã được một CSKH khác phụ trách");
                        errorMsg.put("roomId", roomId);
                        messagingTemplate.convertAndSendToUser(userName, "/queue/errors", errorMsg);
                        return;
                    }
                }
            }

            // Liaison room open by CSKH
            if (isNewRoom && "cskh".equalsIgnoreCase(userType) && roomId != null && roomId.startsWith("liaison-")) {
                Long vendorId = null;
                try {
                    int a = roomId.indexOf("liaison-vendor-");
                    if (a == 0) {
                        int start = "liaison-vendor-".length();
                        int end = roomId.indexOf('-', start);
                        if (end > start) {
                            vendorId = Long.parseLong(roomId.substring(start, end));
                        }
                    }
                } catch (Exception ignored) {}
                if (vendorId == null && roomId.startsWith("liaison-shop-")) {
                    try {
                        Long lsId = vn.util.chat.ChatRoomUtils.extractLiaisonShopId(roomId).orElse(null);
                        if (lsId != null) {
                            Shop s = shopService.findById(lsId).orElse(null);
                            if (s != null && s.getVendor() != null) {
                                vendorId = s.getVendor().getUserId();
                            }
                        }
                    } catch (Exception ignored) {}
                }
                Map<String, Object> liaisonOpen = new HashMap<>();
                liaisonOpen.put("type", "LIAISON_OPEN");
                liaisonOpen.put("roomId", roomId);
                liaisonOpen.put("cskhName", userName);
                if (vendorId != null) {
                    liaisonOpen.put("vendorId", vendorId);
                    try {
                        User v = userService.getUserById(vendorId).orElse(null);
                        if (v != null) liaisonOpen.put("vendorName", v.getName());
                    } catch (Exception ignored) {}
                }
                messagingTemplate.convertAndSend("/topic/rooms", liaisonOpen);

                // Persist a system tag so room appears in recent list after reload
                try {
                    Map<String, Object> tag = new HashMap<>();
                    tag.put("roomId", roomId);
                    tag.put("sender", "system");
                    tag.put("senderType", "system");
                    tag.put("messageType", "SYSTEM");
                    tag.put("messageContent", "Liên hệ với vendor" + (liaisonOpen.get("vendorName") != null ? (": " + liaisonOpen.get("vendorName")) : ""));
                    tag.put("cskhName", userName);
                    tag.put("sentAt", System.currentTimeMillis());
                    chatHistoryService.appendMessage(roomId, tag);
                } catch (Exception ignored) {}
            }

            // If a vendor joins and there is no assigned vendor, check permissions first
            if ("vendor".equalsIgnoreCase(userType) && !assignedVendorByRoom.containsKey(roomId)) {
                // Check if vendor has permission to access this shop
                System.out.println("=== VENDOR JOIN ACCESS CHECK ===");
                System.out.println("userName: " + userName);
                System.out.println("shopId: " + shopId);
                System.out.println("roomId: " + roomId);
                if (shopId != null && !hasVendorShopAccess(userName, shopId)) {
                    // Send error message to vendor
                    Map<String, Object> errorMsg = new HashMap<>();
                    errorMsg.put("type", "ACCESS_DENIED");
                    errorMsg.put("message", "Bạn không có quyền truy cập shop này");
                    errorMsg.put("roomId", roomId);
                    messagingTemplate.convertAndSendToUser(userName, "/queue/errors", errorMsg);
                    return; // Exit without joining
                }
                
                assignedVendorByRoom.put(roomId, userName);
                pendingCountByRoom.put(roomId, 0);
                Map<String, Object> assignedEvent = new HashMap<>();
                assignedEvent.put("type", "ROOM_ASSIGNED");
                assignedEvent.put("roomId", roomId);
                assignedEvent.put("vendorName", userName);
                assignedEvent.put("assignedAt", System.currentTimeMillis());
                if (shopId != null) {
                    assignedEvent.put("shopId", shopId);
                    if (shopName != null) {
                        assignedEvent.put("shopName", shopName);
                    }
                }
                messagingTemplate.convertAndSend("/topic/rooms", assignedEvent);

                // Announce inside the room so customer knows a vendor joined
                Map<String, Object> systemMsg = new HashMap<>();
                systemMsg.put("roomId", roomId);
                systemMsg.put("sender", "system");
                systemMsg.put("messageType", "SYSTEM");
                systemMsg.put("messageContent", "Tư vấn viên " + userName + " đã tham gia hỗ trợ");
                systemMsg.put("sentAt", System.currentTimeMillis());
                messagingTemplate.convertAndSend("/topic/room/" + roomId, systemMsg);
                chatHistoryService.appendMessage(roomId, systemMsg);
                try {
                    ChatMessage m = new ChatMessage();
                    m.setRoomId(roomId);
                    m.setSender("system");
                    m.setSenderType("system");
                    m.setMessageType("SYSTEM");
                    m.setContent(String.valueOf(systemMsg.getOrDefault("messageContent", "")));
                    Object sentAtObj = systemMsg.get("sentAt");
                    long sentAt = sentAtObj instanceof Number ? ((Number) sentAtObj).longValue() : System.currentTimeMillis();
                    m.setSentAt(sentAt);
                    m.setVendorName(userName);
                    chatMessageRepository.save(m);
                } catch (Exception ignored) {}
            }

            System.out.println(String.format("[JOIN] User: %s (%s) joined room: %s (newRoom=%s)", userName, userType, roomId, isNewRoom));

        } catch (Exception e) {
            handleError(e, principal, "Error joining room");
        }
    }
    
    @MessageMapping("/chat.leave")
    public void leaveRoom(@Payload Map<String, Object> payload, Principal principal) {
        try {
            String roomId = String.valueOf(payload.getOrDefault("roomId", "public"));
            // Prefer payload senderName when available
            String userName = String.valueOf(payload.getOrDefault("senderName", "")).trim();
            if (userName.isEmpty()) userName = "guest";
            
            // Remove user from active users
            Map<String, String> roomUsers = activeUsers.get(roomId);
            if (roomUsers != null) {
                String userType = roomUsers.remove(userName);
                
                // Clean up empty rooms
                if (roomUsers.isEmpty()) {
                    activeUsers.remove(roomId);
                }
                
                // Notify room about user leaving
                Map<String, Object> leaveNotification = new HashMap<>();
                leaveNotification.put("type", "USER_LEFT");
                leaveNotification.put("userName", userName);
                leaveNotification.put("userType", userType);
                leaveNotification.put("roomId", roomId);
                leaveNotification.put("timestamp", System.currentTimeMillis());
                leaveNotification.put("activeUsers", roomUsers);
                
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/users", leaveNotification);
                
                System.out.println(String.format("[LEAVE] User: %s left room: %s", userName, roomId));
            }
            
        } catch (Exception e) {
            handleError(e, principal, "Error leaving room");
        }
    }
    
    private String resolveShopName(Long shopId) {
        if (shopId == null) {
            return null;
        }
        return shopNameCache.computeIfAbsent(shopId, id ->
                shopService.findById(id).map(Shop::getShopName).orElse(null)
        );
    }

    private String determineSenderType(String userName) {
        if (userName == null) return "guest";
        
        if (userName.startsWith("vendor") || userName.contains("@mypham.com")) {
            return "vendor";
        } else if (userName.equals("system")) {
            return "system";
        } else {
            return "customer";
        }
    }
    
    /**
     * Sanitize message content to prevent XSS
     */
    private String sanitizeContent(String content) {
        if (content == null) return "";
        
        return content
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;")
            .trim();
    }
    
    /**
     * Handle errors and send to user
     */
    private void handleError(Exception e, Principal principal, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("details", e.getMessage());
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        if (principal != null) {
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/errors", errorResponse);
        }
        
        System.err.println(String.format("[ERROR] %s: %s", message, e.getMessage()));
        e.printStackTrace();
    }
}
