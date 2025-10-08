package vn.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.service.chat.ChatHistoryService;
import vn.entity.ChatMessage;
import vn.repository.ChatMessageRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
public class ChatHistoryController {

    @Autowired
    private ChatHistoryService chatHistoryService;
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @GetMapping("/api/chat/history")
    public List<Map<String, Object>> getHistory(
            @RequestParam("roomId") String roomId,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        if (roomId == null || roomId.isEmpty()) return Collections.emptyList();
        return chatHistoryService.getLastMessages(roomId, limit);
    }

    @GetMapping("/api/chat/rooms")
    public List<Map<String, Object>> getRecentRooms(@RequestParam(value = "limit", required = false) Integer limit) {
        int max = (limit == null || limit <= 0) ? 50 : Math.min(limit, 500);
        List<ChatMessage> lastPerRoom = chatMessageRepository.findLatestPerRoom(PageRequest.of(0, max, Sort.by(Sort.Direction.DESC, "sentAt")));
        return lastPerRoom.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("roomId", m.getRoomId());
            map.put("lastMessageBy", m.getSender());
            map.put("lastMessageType", m.getMessageType());
            map.put("lastMessage", m.getContent());
            map.put("sentAt", m.getSentAt());
            // fetch displayable customer name for room (latest known)
            try {
                List<String> names = chatMessageRepository.findLatestCustomerName(m.getRoomId(), PageRequest.of(0,1));
                if (names != null && !names.isEmpty()) {
                    map.put("customerName", names.get(0));
                }
            } catch (Exception ignored) {}
            return map;
        }).toList();
    }

    @DeleteMapping("/api/chat/history")
    public ResponseEntity<Map<String, Object>> clearHistory(@RequestParam("roomId") String roomId) {
        if (roomId == null || roomId.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("success", false));
        }
        boolean ok = chatHistoryService.deleteRoomHistory(roomId);
        return ResponseEntity.ok(Collections.singletonMap("success", ok));
    }

    // Optional: POST alias in case DELETE is restricted by some proxies
    @PostMapping("/api/chat/history/clear")
    public ResponseEntity<Map<String, Object>> clearHistoryPost(@RequestParam("roomId") String roomId) {
        return clearHistory(roomId);
    }
}
