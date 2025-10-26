package vn.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Utility to generate BCrypt hashes with different strengths
 */
@Component
public class BCryptHashGenerator {
    
    /**
     * Generate BCrypt hash for password "123456" with strength 8
     */
    public String generateHashFor123456() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(8);
        return encoder.encode("123456");
    }
    
    /**
     * Generate BCrypt hash for any password with specified strength
     */
    public String generateHash(String password, int strength) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(strength);
        return encoder.encode(password);
    }
    
    /**
     * Test method to generate hash for "123456" with strength 8
     */
    public static void main(String[] args) {
        BCryptHashGenerator generator = new BCryptHashGenerator();
        String hash = generator.generateHashFor123456();
        System.out.println("BCrypt hash for '123456' with strength 8:");
        System.out.println(hash);
        System.out.println("Length: " + hash.length());
    }
}
