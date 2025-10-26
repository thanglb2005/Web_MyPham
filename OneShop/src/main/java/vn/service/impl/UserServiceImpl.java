package vn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.entity.User;
import vn.repository.UserRepository;
import vn.repository.RoleRepository;
import vn.entity.Role;
import vn.service.UserService;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;
    
    @Override
    public User save(User user) {
        // Hash password before saving if it's not already hashed
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Override
    public Optional<User> findByEmailWithRoles(String email) {
        return userRepository.findByEmailWithRoles(email);
    }
    
    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    @Override
    public Optional<User> findByIdWithRoles(Long userId) {
        return userRepository.findByIdWithRoles(userId);
    }
    
    @Override
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
    
    @Override
    public Optional<User> findByName(String name) {
        return userRepository.findByName(name);
    }
    
    @Override
    public List<User> searchUsers(String keyword) {
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
    }
    
    @Override
    public boolean toggleUserStatus(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus(!user.getStatus());
            userRepository.save(user);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    @Override
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    @Transactional
    public void updateUserRoles(Long userId, java.util.List<Long> roleIds) {
        User user = userRepository.findByIdWithRoles(userId).orElseThrow();
        java.util.List<Long> safeIds = (roleIds == null) ? java.util.Collections.emptyList()
                : roleIds.stream().filter(java.util.Objects::nonNull).toList();
        java.util.Set<Role> newRoles = new java.util.HashSet<>(roleRepository.findAllById(safeIds));
        user.setRoles(newRoles);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void createUserWithRoles(String name, String email, String password, java.util.List<Long> roleIds) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã tồn tại: " + email);
        }
        
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRegisterDate(new java.util.Date());
        newUser.setStatus(true);
        newUser.setEnabled(true);
        
        java.util.List<Long> safeIds = (roleIds == null) ? java.util.Collections.emptyList()
                : roleIds.stream().filter(java.util.Objects::nonNull).toList();
        java.util.Set<Role> newRoles = new java.util.HashSet<>(roleRepository.findAllById(safeIds));
        newUser.setRoles(newRoles);
        
        userRepository.save(newUser);
    }

    @Override
    @Transactional
    public void deleteById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Không tìm thấy user với ID: " + userId);
        }
        userRepository.deleteById(userId);
    }
}