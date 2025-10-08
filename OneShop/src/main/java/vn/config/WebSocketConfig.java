package vn.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

/**
 * Cấu hình WebSocket cho hệ thống chat
 * @author OneShop Team
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Kích hoạt simple broker cho các destination /topic và /queue
        config.enableSimpleBroker("/topic", "/queue");
        
        // Đặt prefix cho các message mapping trong @Controller
        config.setApplicationDestinationPrefixes("/app");
        
        // Đặt prefix cho các tin nhắn cá nhân
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đăng ký endpoint /ws cho WebSocket
        registry.addEndpoint("/ws")
                // Cho phép tất cả origins (có thể giới hạn trong production)
                .setAllowedOriginPatterns("*")
                // Sử dụng SockJS để hỗ trợ fallback khi WebSocket không khả dụng
                .withSockJS();
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        // Ensure JSON messages use UTF-8 consistently
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setContentTypeResolver(resolver);
        messageConverters.add(converter);
        return false; // also register default converters
    }
}
