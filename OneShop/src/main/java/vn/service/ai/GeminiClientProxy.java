package vn.service.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proxy pattern: lớp đại diện đứng trước {@link GeminiService} (Singleton – RealSubject).
 * <p>
 * Virtual / caching proxy: với {@code generateResponse} khi không có {@code conversationHistory},
 * kết quả được cache theo (message, context) trong TTL để giảm gọi API trùng lặp.
 * <p>
 * Các lệnh kiểm tra key, test kết nối, ảnh, welcome — luôn ủy quyền thẳng cho RealSubject (không cache).
 */
@Component
@Primary
public class GeminiClientProxy implements GeminiClient {

    private final boolean cacheEnabled;
    private final long ttlMs;
    private final int maxEntries;
    private final GeminiClient realSubject;

    private final Map<String, CacheEntry> responseCache = new ConcurrentHashMap<>();

    public GeminiClientProxy(
            @Value("${gemini.proxy.cache.enabled:true}") boolean cacheEnabled,
            @Value("${gemini.proxy.cache.ttl-seconds:120}") long ttlSeconds,
            @Value("${gemini.proxy.cache.max-entries:256}") int maxEntries) {
        this.cacheEnabled = cacheEnabled;
        this.ttlMs = Math.max(1, ttlSeconds) * 1000L;
        this.maxEntries = Math.max(16, maxEntries);
        // UML Proxy association: giữ tham chiếu RealSubject
        this.realSubject = GeminiService.getInstance();
    }

    @Override
    public CompletableFuture<String> generateResponse(String userMessage, String context,
                                                      List<Map<String, String>> conversationHistory) {
        boolean canCache = cacheEnabled
                && (conversationHistory == null || conversationHistory.isEmpty())
                && userMessage != null;

        if (canCache) {
            String key = buildCacheKey(userMessage, context);
            CacheEntry hit = responseCache.get(key);
            if (hit != null && hit.expiresAtMillis > System.currentTimeMillis()) {
                System.out.println("[GeminiClientProxy] CACHE HIT key=" + key);
                return CompletableFuture.completedFuture(hit.text);
            }

            return realSubject.generateResponse(userMessage, context, conversationHistory)
                    .thenApply(text -> {
                        putCache(key, text);
                        return text;
                    });
        }

        return realSubject.generateResponse(userMessage, context, conversationHistory);
    }

    @Override
    public boolean isApiKeyValid() {
        return realSubject.isApiKeyValid();
    }

    @Override
    public CompletableFuture<Boolean> testConnection() {
        return realSubject.testConnection();
    }

    @Override
    public CompletableFuture<String> generateWelcomeMessage(String roomId, Long shopId) {
        return realSubject.generateWelcomeMessage(roomId, shopId);
    }

    @Override
    public CompletableFuture<String> generateResponseWithImage(String userMessage, String imageBase64) {
        return realSubject.generateResponseWithImage(userMessage, imageBase64);
    }

    private String buildCacheKey(String message, String context) {
        String ctx = context != null ? context : "";
        return "r:" + message.hashCode() + ":" + ctx.hashCode() + ":" + message.length() + ":" + ctx.length();
    }

    private void putCache(String key, String text) {
        if (text == null) {
            return;
        }
        if (responseCache.size() >= maxEntries) {
            responseCache.clear();
        }
        responseCache.put(key, new CacheEntry(text, System.currentTimeMillis() + ttlMs));
    }

    private static final class CacheEntry {
        final String text;
        final long expiresAtMillis;

        CacheEntry(String text, long expiresAtMillis) {
            this.text = text;
            this.expiresAtMillis = expiresAtMillis;
        }
    }
}
