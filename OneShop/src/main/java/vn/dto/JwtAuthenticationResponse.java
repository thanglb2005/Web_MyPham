package vn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JWT Authentication Response DTO
 * Contains access token, refresh token and user information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthenticationResponse {
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("token_type")
    private String tokenType = "Bearer";
    
    @JsonProperty("expires_in")
    private Long expiresIn;
    
    @JsonProperty("user_info")
    private UserInfo userInfo;
    
    /**
     * Constructor with tokens and expiration
     */
    public JwtAuthenticationResponse(String accessToken, String refreshToken, Long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
    }
    
    /**
     * Constructor with tokens, expiration and user info
     */
    public JwtAuthenticationResponse(String accessToken, String refreshToken, Long expiresIn, UserInfo userInfo) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.userInfo = userInfo;
    }
    
    /**
     * Nested class for user information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        
        @JsonProperty("user_id")
        private Long userId;
        
        private String email;
        private String name;
        private String avatar;
        private String[] roles;
        
        @JsonProperty("one_xu_balance")
        private Double oneXuBalance;
        
        /**
         * Constructor with basic user info
         */
        public UserInfo(Long userId, String email, String name, String[] roles) {
            this.userId = userId;
            this.email = email;
            this.name = name;
            this.roles = roles;
        }
    }
}
