package vn.service;

import vn.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
    User saveUser(User user);
    void deleteUser(Long id);
    User findByEmail(String email);
    Optional<User> findByName(String name);
    List<User> findByStatus(Boolean status);
    boolean existsByEmail(String email);
    
    // Thêm các method cho quản lý khách hàng
    void toggleUserStatus(Long userId);
    List<User> searchUsers(String keyword);
}
