package vn.util;

import vn.entity.User;

import jakarta.servlet.http.HttpSession;

public class UserUtils {

    /**
     * Get current user from session
     */
    public static User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    /**
     * Check if user is vendor
     */
    public static boolean isVendor(User user) {
        if (user == null || user.getRoles() == null) {
            return false;
        }
        return user.getRoles().stream()
                .anyMatch(role -> "VENDOR".equals(role.getName()));
    }

    /**
     * Check if user is shipper
     */
    public static boolean isShipper(User user) {
        if (user == null || user.getRoles() == null) {
            return false;
        }
        return user.getRoles().stream()
                .anyMatch(role -> "SHIPPER".equals(role.getName()));
    }

    /**
     * Check if user is admin
     */
    public static boolean isAdmin(User user) {
        if (user == null || user.getRoles() == null) {
            return false;
        }
        return user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getName()));
    }
}
