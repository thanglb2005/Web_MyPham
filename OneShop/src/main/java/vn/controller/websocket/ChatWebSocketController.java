package vn.controller.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import vn.service.chat.ChatHistoryService;
import vn.entity.ChatMessage;
import vn.repository.ChatMessageRepository;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
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
    
    // In-memory storage for active users (room-based)
    private final Map<String, Map<String, String>> activeUsers = new ConcurrentHashMap<>();
    // Which vendor is assigned to which room (ephemeral, in-memory)
    private final Map<String, String> assignedVendorByRoom = new ConcurrentHashMap<>();
    // Pending customer messages count per room (before a vendor is assigned)
    private final Map<String, Integer> pendingCountByRoom = new ConcurrentHashMap<>();

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
            // For IMAGE messages, keep URL as-is; for TEXT, sanitize
            if ("IMAGE".equalsIgnoreCase(messageType)) {
                message.put("messageContent", content);
            } else {
                message.put("messageContent", sanitizeContent(content));
            }
            message.put("messageType", messageType);
            message.put("sentAt", System.currentTimeMillis());
            message.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            
            // Determine sender type (vendor, customer, system)
            String userTypeOverride = String.valueOf(payload.getOrDefault("userType", "")).trim().toLowerCase();
            String senderType = ("vendor".equals(userTypeOverride) || "customer".equals(userTypeOverride) || "system".equals(userTypeOverride))
                ? userTypeOverride
                : determineSenderType(senderName);
            message.put("senderType", senderType);

            // Broadcast to room
            messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
            // Persist message to history (file/DB service)
            chatHistoryService.appendMessage(roomId, message);
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
            } catch (Exception ignored) {}
            
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
                    messagingTemplate.convertAndSend("/topic/rooms", summary);
                }
            } else if ("vendor".equalsIgnoreCase(senderType)) {
                // First vendor message claims the room if not yet assigned
                assignedVendorByRoom.putIfAbsent(roomId, senderName);
                pendingCountByRoom.put(roomId, 0);
                Map<String, Object> summary = new HashMap<>();
                summary.put("type", "ROOM_UPDATED");
                summary.put("roomId", roomId);
                summary.put("lastMessageBy", senderName);
                summary.put("pendingCount", 0);
                summary.put("sentAt", System.currentTimeMillis());
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

            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/users", joinNotification);

            // Broadcast global room-open event so vendors can see and pick up the conversation
            if (isNewRoom && "customer".equalsIgnoreCase(userType)) {
                Map<String, Object> roomOpenEvent = new HashMap<>();
                roomOpenEvent.put("type", "ROOM_OPEN");
                roomOpenEvent.put("roomId", roomId);
                roomOpenEvent.put("customerName", userName);
                roomOpenEvent.put("createdAt", System.currentTimeMillis());
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
                chatHistoryService.appendMessage(roomId, tagMsg);
            }

            // If a vendor joins and there is no assigned vendor, assign and notify
            if ("vendor".equalsIgnoreCase(userType) && !assignedVendorByRoom.containsKey(roomId)) {
                assignedVendorByRoom.put(roomId, userName);
                pendingCountByRoom.put(roomId, 0);
                Map<String, Object> assignedEvent = new HashMap<>();
                assignedEvent.put("type", "ROOM_ASSIGNED");
                assignedEvent.put("roomId", roomId);
                assignedEvent.put("vendorName", userName);
                assignedEvent.put("assignedAt", System.currentTimeMillis());
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
