package vn.service.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.entity.ChatMessage;
import vn.repository.ChatMessageRepository;

import java.util.*;

@Service
public class ChatHistoryService {
    private static final int DEFAULT_LIMIT = 50;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    /**
     * Persist a chat message into database.
     * Expected keys in message map: roomId, sender, senderType, messageType, messageContent, sentAt
     */
    public void appendMessage(String roomId, Map<String, Object> message) {
        if (roomId == null || roomId.isEmpty() || message == null) return;
        try {
            ChatMessage cm = new ChatMessage();
            cm.setRoomId(roomId);
            cm.setSender(Objects.toString(message.getOrDefault("sender", "system"), "system"));
            cm.setSenderType(Objects.toString(message.getOrDefault("senderType", "customer"), "customer"));
            cm.setMessageType(Objects.toString(message.getOrDefault("messageType", "TEXT"), "TEXT"));
            cm.setContent(Objects.toString(message.getOrDefault("messageContent", ""), ""));
            Long ts = asLongObj(message.get("sentAt"));
            cm.setSentAt(ts != null ? ts : System.currentTimeMillis());
            if (message.containsKey("customerName")) cm.setCustomerName(String.valueOf(message.get("customerName")));
            if (message.containsKey("vendorName")) cm.setVendorName(String.valueOf(message.get("vendorName")));
            chatMessageRepository.save(cm);
        } catch (Exception ignored) {}
    }

    /**
     * Read last N messages of a room from DB, returned in ascending time for UI rendering.
     */
    public List<Map<String, Object>> getLastMessages(String roomId, Integer limit) {
        if (roomId == null || roomId.isEmpty()) return Collections.emptyList();
        int max = (limit == null || limit <= 0) ? DEFAULT_LIMIT : Math.min(limit, 500);
        Pageable pageable = PageRequest.of(0, max, Sort.by(Sort.Direction.DESC, "sentAt"));
        List<ChatMessage> list = chatMessageRepository.findLatestByRoomId(roomId, pageable);
        // convert to map and then order ascending
        List<Map<String, Object>> out = new ArrayList<>(list.size());
        for (ChatMessage m : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("roomId", m.getRoomId());
            map.put("sender", m.getSender());
            map.put("senderType", m.getSenderType());
            map.put("messageType", m.getMessageType());
            map.put("messageContent", m.getContent());
            map.put("sentAt", m.getSentAt());
            out.add(map);
        }
        out.sort(Comparator.comparingLong(o -> asLong(o.get("sentAt"))));
        return out;
    }

    /**
     * Delete all messages of a room.
     */
    @Transactional
    public boolean deleteRoomHistory(String roomId) {
        if (roomId == null || roomId.isEmpty()) return false;
        try {
            long n = chatMessageRepository.deleteByRoomId(roomId);
            // Idempotent: consider success even if there was nothing to delete
            return n >= 0;
        } catch (Exception e) {
            return false;
        }
    }

    private long asLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return 0L; }
    }

    private Long asLongObj(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return null; }
    }
}

