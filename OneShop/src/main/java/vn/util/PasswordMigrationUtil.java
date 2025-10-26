package vn.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.entity.User;
import vn.repository.UserRepository;

import java.util.List;

/**
 * Utility class to migrate existing plain text passwords to BCrypt hashed passwords
 * Run this once to update all existing users' passwords
 */
@Component
public class PasswordMigrationUtil implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Only run migration if explicitly requested
        if (args.length > 0 && "migrate-passwords".equals(args[0])) {
            migratePasswords();
        }
    }
    
    public void migratePasswords() {
        System.out.println("🔐 Starting password migration...");
        
        List<User> users = userRepository.findAll();
        int migratedCount = 0;
        
        for (User user : users) {
            String currentPassword = user.getPassword();
            
            // Check if password is already hashed (BCrypt starts with $2a$)
            if (currentPassword != null && !currentPassword.startsWith("$2a$")) {
                // Hash the plain text password
                String hashedPassword = passwordEncoder.encode(currentPassword);
                user.setPassword(hashedPassword);
                userRepository.save(user);
                
                System.out.println("✅ Migrated password for user: " + user.getEmail());
                migratedCount++;
            } else {
                System.out.println("⏭️  Password already hashed for user: " + user.getEmail());
            }
        }
        
        System.out.println("🎉 Password migration completed! Migrated " + migratedCount + " users.");
    }
    
    /**
     * Manual method to hash a specific password
     */
    public String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }
}
