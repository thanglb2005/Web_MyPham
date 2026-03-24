package vn.service.ai;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Subject interface cho tích hợp Gemini (RealSubject = {@link GeminiService}, Proxy = {@link GeminiClientProxy}).
 */
public interface GeminiClient {

    CompletableFuture<String> generateResponse(String userMessage, String context,
                                            List<Map<String, String>> conversationHistory);

    /**
     * Không có lịch sử hội thoại — có thể cache ở Proxy khi bật cache.
     */
    default CompletableFuture<String> generateResponse(String userMessage, String context) {
        return generateResponse(userMessage, context, null);
    }

    boolean isApiKeyValid();

    CompletableFuture<Boolean> testConnection();

    CompletableFuture<String> generateWelcomeMessage(String roomId, Long shopId);

    CompletableFuture<String> generateResponseWithImage(String userMessage, String imageBase64);
}
