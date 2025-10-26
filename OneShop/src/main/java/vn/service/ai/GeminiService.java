package vn.service.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service để tích hợp Google Gemini AI cho chatbot
 * @author OneShop Team
 */
@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model.name:gemini-1.5-flash}")
    private String modelName;

    @Value("${gemini.max.tokens:1000}")
    private int maxTokens;

    @Value("${gemini.temperature:0.7}")
    private float temperature;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";

    /**
     * Gửi tin nhắn đến Gemini AI và nhận phản hồi với context lịch sử hội thoại
     * @param userMessage Tin nhắn từ người dùng
     * @param context Context về sản phẩm mỹ phẩm (optional)
     * @param conversationHistory Lịch sử hội thoại (optional)
     * @return Phản hồi từ AI
     */
    public CompletableFuture<String> generateResponse(String userMessage, String context, List<Map<String, String>> conversationHistory) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Tạo prompt với context về OneShop
                String systemPrompt = buildSystemPrompt(context);
                
                // Xây dựng nội dung hội thoại
                List<Map<String, Object>> contents = new ArrayList<>();
                
                // Thêm system prompt vào đầu
                Map<String, Object> systemContent = new HashMap<>();
                Map<String, Object> systemPart = new HashMap<>();
                systemPart.put("text", systemPrompt);
                systemContent.put("parts", List.of(systemPart));
                systemContent.put("role", "user");
                contents.add(systemContent);
                
                // Thêm lịch sử hội thoại
                if (conversationHistory != null && !conversationHistory.isEmpty()) {
                    for (Map<String, String> history : conversationHistory) {
                        if (history.containsKey("role") && history.containsKey("content")) {
                            Map<String, Object> contentItem = new HashMap<>();
                            Map<String, Object> part = new HashMap<>();
                            part.put("text", history.get("content"));
                            contentItem.put("parts", List.of(part));
                            contentItem.put("role", history.get("role"));
                            contents.add(contentItem);
                        }
                    }
                }
                
                // Thêm tin nhắn hiện tại
                Map<String, Object> userContent = new HashMap<>();
                Map<String, Object> userPart = new HashMap<>();
                userPart.put("text", userMessage);
                userContent.put("parts", List.of(userPart));
                userContent.put("role", "user");
                contents.add(userContent);
                
                // Tạo request body với conversation history
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("contents", contents);
                
                // Generation config
                Map<String, Object> generationConfig = new HashMap<>();
                generationConfig.put("maxOutputTokens", maxTokens);
                generationConfig.put("temperature", temperature);
                requestBody.put("generationConfig", generationConfig);
                
                // Safety settings
                Map<String, Object> safetySetting = new HashMap<>();
                safetySetting.put("category", "HARM_CATEGORY_HATE_SPEECH");
                safetySetting.put("threshold", "BLOCK_MEDIUM_AND_ABOVE");
                requestBody.put("safetySettings", List.of(safetySetting));
                
                // Headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                // Request entity
                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
                
                // API URL with model and key
                String url = GEMINI_API_URL.replace("{model}", modelName) + "?key=" + apiKey;
                
                // Debug logging
                System.out.println("=== GEMINI API CALL ===");
                System.out.println("URL: " + url);
                System.out.println("Model: " + modelName);
                System.out.println("Message: " + userMessage);
                
                // Make request
                ResponseEntity<Map> response = restTemplate.exchange(
                    url, 
                    HttpMethod.POST, 
                    requestEntity, 
                    Map.class
                );
                
                System.out.println("Response Status: " + response.getStatusCode());
                
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    System.out.println("Response Body Keys: " + responseBody.keySet());
                    
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                    
                    if (candidates != null && !candidates.isEmpty()) {
                        Map<String, Object> firstCandidate = candidates.get(0);
                        
                        // Check for finish reason
                        String finishReason = (String) firstCandidate.get("finishReason");
                        if ("SAFETY" == finishReason || "RECITATION" == finishReason) {
                            System.err.println("Gemini blocked due to: " + finishReason);
                            return "Xin lỗi, nội dung bị chặn bởi bộ lọc an toàn của AI. Vui lòng thử lại với câu hỏi khác.";
                        }
                        
                        Map<String, Object> contentResponse = (Map<String, Object>) firstCandidate.get("content");
                        
                        if (contentResponse != null) {
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) contentResponse.get("parts");
                            
                            if (parts != null && !parts.isEmpty()) {
                                String text = (String) parts.get(0).get("text");
                                System.out.println("AI Response: " + text);
                                return text;
                            }
                        }
                    } else {
                        System.err.println("No candidates in response");
                        System.err.println("Full response: " + responseBody);
                    }
                } else {
                    System.err.println("Invalid response: " + response.getStatusCode());
                }
                
                return "Xin lỗi, tôi không thể tạo phản hồi lúc này. Vui lòng thử lại sau.";
                
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                System.err.println("HTTP Error: " + e.getStatusCode());
                System.err.println("Error Body: " + e.getResponseBodyAsString());
                return "Xin lỗi, có lỗi xảy ra khi kết nối với AI (" + e.getStatusCode() + "). Vui lòng thử lại sau.";
            } catch (Exception e) {
                System.err.println("Error calling Gemini API: " + e.getMessage());
                e.printStackTrace();
                return "Xin lỗi, có lỗi xảy ra khi xử lý tin nhắn của bạn. Vui lòng thử lại sau.";
            }
        });
    }

    /**
     * Xây dựng system prompt cho AI chatbot
     * @param context Context về sản phẩm (optional)
     * @return System prompt
     */
    private String buildSystemPrompt(String context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Bạn là AI chatbot hỗ trợ khách hàng của OneShop - cửa hàng mỹ phẩm trực tuyến hàng đầu Việt Nam. ");
        prompt.append("Nhiệm vụ của bạn là:\n\n");
        
        prompt.append("1. Tư vấn về các sản phẩm mỹ phẩm: son môi, kem dưỡng da, nước hoa, serum, kem chống nắng, v.v.\n");
        prompt.append("2. Hướng dẫn cách sử dụng và chăm sóc da phù hợp\n");
        prompt.append("3. Tư vấn về thương hiệu và xuất xứ sản phẩm\n");
        prompt.append("4. Hỗ trợ về chính sách đổi trả, giao hàng\n");
        prompt.append("5. Giải đáp thắc mắc về đơn hàng và thanh toán\n\n");
        
        prompt.append("Quy tắc ứng xử:\n");
        prompt.append("- Luôn lịch sự, thân thiện và chuyên nghiệp\n");
        prompt.append("- Trả lời bằng tiếng Việt, dễ hiểu\n");
        prompt.append("- Nếu không chắc chắn, hãy đề xuất liên hệ CSKH\n");
        prompt.append("- Không đưa ra lời khuyên y tế chuyên sâu\n");
        prompt.append("- Luôn khuyến khích khách hàng mua sắm tại OneShop\n\n");
        
        if (context != null && !context.trim().isEmpty()) {
            prompt.append("Thông tin về sản phẩm hiện tại: ").append(context).append("\n\n");
        }
        
        prompt.append("Hãy trả lời ngắn gọn, súc tích và hữu ích cho khách hàng.");
        
        return prompt.toString();
    }

    /**
     * Kiểm tra xem API key có hợp lệ không
     * @return true nếu API key hợp lệ
     */
    public boolean isApiKeyValid() {
        return apiKey != null && !apiKey.trim().isEmpty() && !apiKey.equals("YOUR_GEMINI_API_KEY_HERE");
    }

    /**
     * Overload method for backward compatibility (no conversation history)
     */
    public CompletableFuture<String> generateResponse(String userMessage, String context) {
        return generateResponse(userMessage, context, null);
    }

    /**
     * Test kết nối với Gemini API
     * @return true nếu kết nối thành công
     */
    public CompletableFuture<Boolean> testConnection() {
        return generateResponse("Xin chào", null)
                .thenApply(response -> response != null && !response.contains("lỗi"))
                .exceptionally(throwable -> false);
    }

    /**
     * Tạo tin nhắn chào mừng từ AI
     * @param roomId ID của room
     * @param shopId ID của shop (optional)
     * @return Tin nhắn chào mừng
     */
    public CompletableFuture<String> generateWelcomeMessage(String roomId, Long shopId) {
        String welcomePrompt = "Tạo một tin nhắn chào mừng ngắn gọn và thân thiện cho khách hàng của OneShop mỹ phẩm. " +
                              "Tin nhắn nên giới thiệu về khả năng tư vấn sản phẩm mỹ phẩm và hỗ trợ khách hàng.";
        
        return generateResponse(welcomePrompt, null);
    }

    /**
     * Gửi tin nhắn kèm hình ảnh đến Gemini AI
     * @param userMessage Tin nhắn từ người dùng
     * @param imageBase64 Hình ảnh dạng Base64
     * @return Phản hồi từ AI
     */
    public CompletableFuture<String> generateResponseWithImage(String userMessage, String imageBase64) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Remove data URL prefix if present
                String base64Data = imageBase64;
                if (imageBase64.contains(",")) {
                    base64Data = imageBase64.split(",")[1];
                }
                
                // Tạo prompt
                String fullPrompt = buildSystemPrompt(null) + "\n\nKhách hàng: " + userMessage;
                
                // Tạo request body với image
                Map<String, Object> requestBody = new HashMap<>();
                
                // Contents array với text và image
                Map<String, Object> content = new HashMap<>();
                
                // Text part
                Map<String, Object> textPart = new HashMap<>();
                textPart.put("text", fullPrompt);
                
                // Image part
                Map<String, Object> imagePart = new HashMap<>();
                Map<String, Object> inlineData = new HashMap<>();
                inlineData.put("mimeType", "image/jpeg");
                inlineData.put("data", base64Data);
                imagePart.put("inlineData", inlineData);
                
                content.put("parts", List.of(textPart, imagePart));
                requestBody.put("contents", List.of(content));
                
                // Generation config
                Map<String, Object> generationConfig = new HashMap<>();
                generationConfig.put("maxOutputTokens", maxTokens);
                generationConfig.put("temperature", temperature);
                requestBody.put("generationConfig", generationConfig);
                
                // Headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                // Request entity
                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
                
                // API URL with model and key
                String url = GEMINI_API_URL.replace("{model}", modelName) + "?key=" + apiKey;
                
                System.out.println("=== GEMINI API CALL WITH IMAGE ===");
                System.out.println("URL: " + url);
                System.out.println("Message: " + userMessage);
                System.out.println("Image Base64 Length: " + imageBase64.length());
                
                // Make request
                ResponseEntity<Map> response = restTemplate.exchange(
                    url, 
                    HttpMethod.POST, 
                    requestEntity, 
                    Map.class
                );
                
                System.out.println("Response Status: " + response.getStatusCode());
                
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                    
                    if (candidates != null && !candidates.isEmpty()) {
                        Map<String, Object> firstCandidate = candidates.get(0);
                        Map<String, Object> contentResponse = (Map<String, Object>) firstCandidate.get("content");
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) contentResponse.get("parts");
                        
                        if (parts != null && !parts.isEmpty()) {
                            String text = (String) parts.get(0).get("text");
                            System.out.println("AI Response: " + text);
                            return text;
                        }
                    }
                }
                
                return "Xin lỗi, tôi không thể phân tích hình ảnh lúc này. Vui lòng thử lại sau.";
                
            } catch (Exception e) {
                System.err.println("Error calling Gemini API with image: " + e.getMessage());
                e.printStackTrace();
                return "Xin lỗi, có lỗi xảy ra khi xử lý hình ảnh. Vui lòng thử lại sau.";
            }
        });
    }
}
