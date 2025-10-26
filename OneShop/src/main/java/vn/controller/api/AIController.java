package vn.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.entity.Product;
import vn.repository.ProductRepository;
import vn.service.ai.AIChatService;
import vn.service.ai.GeminiService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

/**
 * API Controller để test và quản lý AI chatbot
 * @author OneShop Team
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private AIChatService aiChatService;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Test kết nối với Gemini API
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isApiKeyValid = geminiService.isApiKeyValid();
            response.put("apiKeyValid", isApiKeyValid);
            
            if (isApiKeyValid) {
                CompletableFuture<Boolean> testResult = geminiService.testConnection();
                boolean connectionOk = testResult.get();
                response.put("connectionOk", connectionOk);
                
                if (connectionOk) {
                    response.put("status", "success");
                    response.put("message", "Gemini API kết nối thành công");
                } else {
                    response.put("status", "error");
                    response.put("message", "Gemini API không phản hồi");
                }
            } else {
                response.put("status", "error");
                response.put("message", "API key không hợp lệ hoặc chưa được cấu hình");
            }
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi test Gemini API: " + e.getMessage());
            response.put("connectionOk", false);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test AI chat với tin nhắn mẫu
     */
    @PostMapping("/test-chat")
    public ResponseEntity<Map<String, Object>> testChat(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String message = request.getOrDefault("message", "Xin chào");
            String roomId = request.getOrDefault("roomId", "test-room");
            
            CompletableFuture<String> aiResponse = aiChatService.processUserMessage(
                roomId, message, "Test User", null
            );
            
            String result = aiResponse.get();
            
            response.put("status", "success");
            response.put("userMessage", message);
            response.put("aiResponse", result);
            response.put("roomId", roomId);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi test AI chat: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Kiểm tra trạng thái AI
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAIStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isReady = aiChatService.isAIReady();
            response.put("isReady", isReady);
            response.put("status", isReady ? "ready" : "not_ready");
            
            if (isReady) {
                response.put("message", "AI chatbot sẵn sàng hoạt động");
            } else {
                response.put("message", "AI chatbot chưa sẵn sàng - kiểm tra API key");
            }
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi kiểm tra trạng thái AI: " + e.getMessage());
            response.put("isReady", false);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test trực tiếp với Gemini API
     */
    @PostMapping("/direct-test")
    public ResponseEntity<Map<String, Object>> directTest(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String message = request.getOrDefault("message", "Xin chào");
            String context = request.getOrDefault("context", "");
            
            CompletableFuture<String> aiResponse = geminiService.generateResponse(message, context);
            String result = aiResponse.get();
            
            response.put("status", "success");
            response.put("userMessage", message);
            response.put("context", context);
            response.put("aiResponse", result);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi test trực tiếp Gemini API: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Tạo tin nhắn chào mừng từ AI
     */
    @PostMapping("/welcome")
    public ResponseEntity<Map<String, Object>> generateWelcomeMessage(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String roomId = request.getOrDefault("roomId", "welcome-room");
            Long shopId = request.containsKey("shopId") ? Long.parseLong(request.get("shopId")) : null;
            
            CompletableFuture<String> welcomeMessage = aiChatService.generateWelcomeMessage(roomId, shopId);
            String result = welcomeMessage.get();
            
            response.put("status", "success");
            response.put("welcomeMessage", result);
            response.put("roomId", roomId);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi tạo tin nhắn chào mừng: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Simple chat endpoint for floating chatbot with conversation history
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String message = request.getOrDefault("message", "").toString();
            String context = request.getOrDefault("context", "OneShop mỹ phẩm").toString();
            
            // Lấy conversation history (có thể null)
            @SuppressWarnings("unchecked")
            List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");
            
            if (message.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Tin nhắn không được để trống");
                return ResponseEntity.badRequest().body(response);
            }
            
            CompletableFuture<String> aiResponse = geminiService.generateResponse(message, context, history);
            String result = aiResponse.get();
            
            response.put("status", "success");
            response.put("message", result);
            response.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            System.err.println("Error in /api/ai/chat: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "error");
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Tìm sản phẩm trong database để chatbot giới thiệu
     */
    @PostMapping("/search-products")
    public ResponseEntity<Map<String, Object>> searchProducts(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String query = request.getOrDefault("query", "");
            
            if (query.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Query không được để trống");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Extract keyword from query (remove stopwords)
            String keyword = extractKeywordFromQuery(query);
            
            // Search products in database using the new method (limit to 10 products for 2 columns x 5 rows)
            List<Product> products = productRepository.searchProductsByKeyword(keyword, 10);
            
            // Convert to response format matching frontend displayProducts() expectation
            List<Map<String, Object>> productData = products.stream().map(p -> {
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("id", p.getProductId());
                productMap.put("name", p.getProductName());
                productMap.put("price", p.getPrice());
                productMap.put("discount", p.getDiscount() != null ? p.getDiscount() : 0);
                productMap.put("image", p.getProductImage());
                
                // Add brand and category for display
                if (p.getBrand() != null) {
                    productMap.put("brand", p.getBrand().getBrandName());
                }
                if (p.getCategory() != null) {
                    productMap.put("category", p.getCategory().getCategoryName());
                }
                
                return productMap;
            }).collect(Collectors.toList());
            
            response.put("status", "success");
            response.put("products", productData);
            response.put("count", productData.size());
            
        } catch (Exception e) {
            System.err.println("Error in /api/ai/search-products: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "error");
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Extract keyword from user query (remove common stopwords)
     */
    private String extractKeywordFromQuery(String query) {
        if (query == null) return "";
        
        String processed = query.toLowerCase()
            .replaceAll("[^\\p{L}\\p{Nd}\\s]", " ")
            .replaceAll("\\b(tim|tìm|cho|mua|giá|sản|phẩm|có|không|loại|nào|giúp|tư\\s*vấn|xem|show|list|danh|sách)\\b", " ")
            .replaceAll("\\s+", " ")
            .trim();
        
        // If empty after processing, return the original query
        return processed.isEmpty() ? query.trim() : processed;
    }

    /**
     * Upload hình ảnh từ chatbot (Base64)
     */
    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== UPLOAD IMAGE REQUEST ===");
            System.out.println("Request keys: " + request.keySet());
            
            String imageData = request.get("imageData");
            String message = request.getOrDefault("message", "");
            
            System.out.println("Message: " + message);
            System.out.println("Image data length: " + (imageData != null ? imageData.length() : 0));
            
            if (imageData == null || imageData.isEmpty()) {
                System.err.println("Image data is empty or null");
                response.put("status", "error");
                response.put("message", "Không có dữ liệu hình ảnh");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Limit image size (max 5MB in base64)
            if (imageData.length() > 7000000) {
                System.err.println("Image size too large: " + imageData.length());
                response.put("status", "error");
                response.put("message", "Kích thước hình ảnh quá lớn (tối đa 5MB)");
                return ResponseEntity.badRequest().body(response);
            }
            
            System.out.println("Calling Gemini API with image...");
            
            // Send image + message to Gemini
            CompletableFuture<String> aiResponse = geminiService.generateResponseWithImage(message, imageData);
            String result = aiResponse.get();
            
            response.put("status", "success");
            response.put("message", result);
            response.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            System.err.println("Error in /api/ai/upload-image: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "error");
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
