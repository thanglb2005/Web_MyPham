package vn.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.service.chat.ChatHistoryService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class ChatHistoryController {

    @Autowired
    private ChatHistoryService chatHistoryService;

    @GetMapping("/api/chat/history")
    public List<Map<String, Object>> getHistory(
            @RequestParam("roomId") String roomId,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        if (roomId == null || roomId.isEmpty()) return Collections.emptyList();
        return chatHistoryService.getLastMessages(roomId, limit);
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
