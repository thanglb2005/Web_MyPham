package vn.service.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.service.chat.ChatHistoryService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service quản lý AI chat tích hợp với hệ thống chat hiện tại
 * @author OneShop Team
 */
@Service
public class AIChatService {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private ChatHistoryService chatHistoryService;

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
                
                // Gọi GeminiService để lấy phản hồi AI
                String aiResponse = geminiService.generateResponse(userMessage, context).get();
                
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
        return geminiService.generateWelcomeMessage(roomId, shopId);
    }

    /**
     * Kiểm tra xem AI có sẵn sàng không
     * @return true nếu AI sẵn sàng
     */
    public boolean isAIReady() {
        return geminiService.isApiKeyValid();
    }

    /**
     * Test AI với tin nhắn mẫu
     * @param testMessage Tin nhắn test
     * @return Kết quả test
     */
    public CompletableFuture<String> testAI(String testMessage) {
        return geminiService.generateResponse(testMessage, "Test context");
    }
}
