package vn.service;

import vn.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User save(User user);
    List<User> findAll();
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailWithRoles(String email);
    Optional<User> findByIdWithRoles(Long userId);
    Optional<User> findById(Long userId);
    Optional<User> findByName(String name);
    List<User> searchUsers(String keyword);
    boolean toggleUserStatus(Long userId);
    boolean verifyPassword(String rawPassword, String encodedPassword);
    String encodePassword(String rawPassword);
    void updateUserRoles(Long userId, java.util.List<Long> roleIds);
    void createUserWithRoles(String name, String email, String password, java.util.List<Long> roleIds);
    void deleteById(Long userId);
}