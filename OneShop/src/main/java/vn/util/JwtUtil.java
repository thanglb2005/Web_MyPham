package vn.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import vn.entity.User;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JWT Utility class for generating, validating and parsing JWT tokens
 * Supports both access tokens and refresh tokens
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.audience}")
    private String audience;

    /**
     * Generate secret key from the configured secret string
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Extract username (email) from JWT token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract user ID from JWT token
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extract user roles from JWT token
     */
    public String extractRoles(String token) {
        return extractClaim(token, claims -> claims.get("roles", String.class));
    }

    /**
     * Extract token type (access/refresh) from JWT token
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("tokenType", String.class));
    }

    /**
     * Extract expiration date from JWT token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from JWT token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from JWT token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if JWT token is expired
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generate access token for user
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("name", user.getName());
        claims.put("roles", user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.joining(",")));
        claims.put("tokenType", "access");
        
        return createToken(claims, user.getEmail(), accessTokenExpiration);
    }

    /**
     * Generate access token from UserDetails (for Spring Security integration)
     */
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));
        claims.put("tokenType", "access");
        
        return createToken(claims, userDetails.getUsername(), accessTokenExpiration);
    }

    /**
     * Generate refresh token for user
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("tokenType", "refresh");
        
        return createToken(claims, user.getEmail(), refreshTokenExpiration);
    }

    /**
     * Generate refresh token from username
     */
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "refresh");
        
        return createToken(claims, username, refreshTokenExpiration);
    }

    /**
     * Create JWT token with claims, subject and expiration
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validate JWT token against user details
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate JWT token against user
     */
    public Boolean validateToken(String token, User user) {
        try {
            final String username = extractUsername(token);
            final Long userId = extractUserId(token);
            return (username.equals(user.getEmail()) && 
                    userId.equals(user.getUserId()) && 
                    !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate JWT token (basic validation)
     */
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if token is access token
     */
    public Boolean isAccessToken(String token) {
        try {
            return "access".equals(extractTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if token is refresh token
     */
    public Boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(extractTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get token expiration time in milliseconds
     */
    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public Long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    /**
     * Parse JWT token and handle exceptions
     */
    public Claims parseToken(String token) throws JwtException {
        try {
            return extractAllClaims(token);
        } catch (ExpiredJwtException e) {
            throw new JwtException("JWT token has expired", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtException("JWT token is unsupported", e);
        } catch (MalformedJwtException e) {
            throw new JwtException("JWT token is malformed", e);
        } catch (IllegalArgumentException e) {
            throw new JwtException("JWT token is invalid", e);
        }
    }
}
