package vn.service.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.service.chat.ChatHistoryService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service quản lý AI chat tích hợp với hệ thống chat hiện tại
 *
 * ---------- CODE CŨ (giai đoạn 1 — Spring bean) ----------
 *  @Autowired
 *  private GeminiService geminiService;
 *  // geminiService.generateResponse(...); geminiService.isApiKeyValid();
 *
 * ---------- CODE CŨ (giai đoạn 2 — Singleton, chưa Proxy) ----------
 *  // GeminiService.getInstance().generateResponse(...);
 *  // GeminiService.getInstance().isApiKeyValid();
 *
 * ---------- CODE MỚI (Singleton + Proxy): ----------
 *  {@code @Autowired GeminiClient geminiClient} — {@code @Primary} là {@link GeminiClientProxy} → {@link GeminiService#getInstance()}.
 *  Trong từng method bên dưới có khối comment CODE CŨ để quay video đối chiếu.
 */
@Service
public class AIChatService {

    @Autowired
    private ChatHistoryService chatHistoryService;

    @Autowired
    private GeminiClient geminiClient;

    /**
     * Xử lý tin nhắn từ người dùng và tạo phản hồi AI
     * @param roomId ID của room chat
     * @param userMessage Tin nhắn từ người dùng
     * @param senderName Tên người gửi
     * @param shopId ID của shop (optional)
     * @return Phản hồi từ AI
     */
    public CompletableFuture<String> processUserMessage(String roomId, String userMessage, String senderName, Long shopId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Lấy lịch sử chat gần đây để làm context
                String context = buildContextFromHistory(roomId);
                
                // ===== CODE CŨ (chưa Proxy — gọi trực tiếp Singleton) =====
                // String aiResponse = GeminiService.getInstance()
                //         .generateResponse(userMessage, context).get();
                // ===== HẾT CODE CŨ =====

                // Gọi qua GeminiClient (Proxy có thể cache khi không có conversation history ở tầng API khác)
                String aiResponse = geminiClient.generateResponse(userMessage, context).get();
                
                return aiResponse;
                
            } catch (Exception e) {
                System.err.println("Error processing AI message: " + e.getMessage());
                return "Xin lỗi, có lỗi xảy ra khi xử lý tin nhắn của bạn. Vui lòng thử lại sau.";
            }
        });
    }

    /**
     * Xây dựng context từ lịch sử chat
     * @param roomId ID của room
     * @return Context string
     */
    private String buildContextFromHistory(String roomId) {
        try {
            // Lấy 5 tin nhắn gần nhất
            List<Map<String, Object>> recentMessages = chatHistoryService.getLastMessages(roomId, 5);
            
            if (recentMessages == null || recentMessages.isEmpty()) {
                return "";
            }
            
            StringBuilder context = new StringBuilder();
            context.append("Lịch sử chat gần đây:\n");
            
            for (var message : recentMessages) {
                String content = (String) message.get("messageContent");
                String senderType = (String) message.get("senderType");
                
                if ("customer".equals(senderType)) {
                    context.append("Khách hàng: ").append(content).append("\n");
                } else if ("ai".equals(senderType)) {
                    context.append("AI: ").append(content).append("\n");
                }
            }
            
            return context.toString();
            
        } catch (Exception e) {
            System.err.println("Error building context from history: " + e.getMessage());
            return "";
        }
    }

    /**
     * Tạo tin nhắn chào mừng từ AI
     * @param roomId ID của room
     * @param shopId ID của shop (optional)
     * @return Tin nhắn chào mừng
     */
    public CompletableFuture<String> generateWelcomeMessage(String roomId, Long shopId) {
        // ===== CODE CŨ (chưa Proxy) =====
        // return GeminiService.getInstance().generateWelcomeMessage(roomId, shopId);
        // ===== HẾT CODE CŨ =====
        return geminiClient.generateWelcomeMessage(roomId, shopId);
    }

    /**
     * Kiểm tra xem AI có sẵn sàng không
     * @return true nếu AI sẵn sàng
     */
    public boolean isAIReady() {
        // ===== CODE CŨ (chưa Proxy) =====
        // return GeminiService.getInstance().isApiKeyValid();
        // ===== HẾT CODE CŨ =====
        return geminiClient.isApiKeyValid();
    }

    /**
     * Test AI với tin nhắn mẫu
     * @param testMessage Tin nhắn test
     * @return Kết quả test
     */
    public CompletableFuture<String> testAI(String testMessage) {
        // ===== CODE CŨ (chưa Proxy) =====
        // return GeminiService.getInstance().generateResponse(testMessage, "Test context");
        // ===== HẾT CODE CŨ =====
        return geminiClient.generateResponse(testMessage, "Test context");
    }
}
