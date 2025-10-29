package vn.controller.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.entity.User;
import vn.service.UserService;
import vn.repository.RoleRepository;

import java.util.List;
import java.util.Optional;

/**
 * Controller quản lý khách hàng trong admin
 * @author OneShop Team
 */
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Hiển thị danh sách tất cả khách hàng
     */
    @GetMapping("/admin/users")
    public String users(HttpSession session,
                        @RequestParam(value = "q", required = false) String query,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "10") int size,
                        Model model) {
        User adminUser = (User) session.getAttribute("user");
        if (adminUser == null) {
            return "redirect:/login";
        }
        
        List<User> users;
        String q = (query != null) ? query.trim() : "";
        if (!q.isEmpty()) {
            users = userService.searchUsers(q);
        } else {
            users = userService.findAll();
        }

        // Pagination
        if (size <= 0) size = 10;
        int totalItems = users.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        if (page < 0) page = 0;
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalItems);
        List<User> paginated = users.subList(Math.min(startIndex, totalItems), Math.min(endIndex, totalItems));

        model.addAttribute("users", paginated);
        model.addAttribute("searchTerm", q);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("hasPrev", page > 0);
        model.addAttribute("hasNext", page < totalPages - 1);
        model.addAttribute("startIndex", Math.min(startIndex + 1, totalItems));
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("user", adminUser);
        
        return "admin/users";
    }

    /**
     * Trang quản lý tài khoản: cấp role và tạo tài khoản
     */
    @GetMapping("/admin/accounts")
    public String accounts(HttpSession session,
                           @RequestParam(value = "q", required = false) String query,
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "10") int size,
                           Model model) {
        User adminUser = (User) session.getAttribute("user");
        if (adminUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", adminUser);
        List<User> list = null;
        String q = (query != null) ? query.trim() : "";
        if (!q.isEmpty()) {
            list = userService.searchUsers(q);
        } else {
            list = userService.findAll();
        }

        if (size <= 0) size = 10;
        int totalItems = list.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        if (page < 0) page = 0;
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalItems);
        List<User> paginated = list.subList(Math.min(startIndex, totalItems), Math.min(endIndex, totalItems));

        model.addAttribute("users", paginated);
        model.addAttribute("searchTerm", q);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("hasPrev", page > 0);
        model.addAttribute("hasNext", page < totalPages - 1);
        model.addAttribute("startIndex", Math.min(startIndex + 1, totalItems));
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/accounts";
    }

    /** Cập nhật role cho user */
    @PostMapping("/admin/accounts/update-roles")
    public String updateRoles(@RequestParam(value = "userId", required = false) Long userId,
                              @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
                              HttpSession session) {
        User adminUser = (User) session.getAttribute("user");
        if (adminUser == null) {
            return "redirect:/login";
        }
        if (userId == null) {
            return "redirect:/admin/accounts?error=missing_user";
        }
        try {
            userService.updateUserRoles(userId, roleIds);
            
            // If admin is updating their own roles, refresh session
            if (adminUser.getUserId().equals(userId)) {
                Optional<User> updatedUser = userService.findByIdWithRoles(userId);
                updatedUser.ifPresent(u -> session.setAttribute("user", u));
            }
            
            return "redirect:/admin/accounts?success=updated";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "redirect:/admin/accounts?error=update_failed";
        }
    }

    /** Graceful redirect if someone opens the POST URL directly */
    @GetMapping("/admin/accounts/update-roles")
    public String updateRolesGetFallback() {
        return "redirect:/admin/accounts";
    }

    /** Tạo tài khoản mới + phân role */
    @PostMapping("/admin/accounts/create")
    public String createAccount(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String password,
                                @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
                                HttpSession session) {
        User adminUser = (User) session.getAttribute("user");
        if (adminUser == null) {
            return "redirect:/login";
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setStatus(true);
        if (roleIds != null && !roleIds.isEmpty()) {
            user.setRoles(new java.util.HashSet<>(roleRepository.findAllById(roleIds)));
        }
        userService.save(user);
        return "redirect:/admin/accounts";
    }

    /** Xóa tài khoản */
    @PostMapping("/admin/accounts/delete")
    public String deleteAccount(@RequestParam(value = "userId", required = false) Long userId,
                                HttpSession session) {
        User adminUser = (User) session.getAttribute("user");
        if (adminUser == null) {
            return "redirect:/login";
        }
        if (userId == null) {
            return "redirect:/admin/accounts?error=missing_user";
        }
        
        // Prevent admin from deleting themselves
        if (adminUser.getUserId().equals(userId)) {
            return "redirect:/admin/accounts?error=cannot_delete_self";
        }
        
        try {
            userService.deleteById(userId);
            return "redirect:/admin/accounts?success=deleted";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "redirect:/admin/accounts?error=delete_failed";
        }
    }

    /**
     * Thay đổi trạng thái hoạt động của khách hàng
     */
    @PostMapping("/admin/users/toggle-status/{userId}")
    public String toggleUserStatus(@PathVariable Long userId, HttpSession session) {
        User adminUser = (User) session.getAttribute("user");
        if (adminUser == null) {
            return "redirect:/login";
        }
        
        userService.toggleUserStatus(userId);
        return "redirect:/admin/users";
    }

    /**
     * Xem chi tiết khách hàng
     */
    @GetMapping("/admin/users/detail/{userId}")
    public String userDetail(@PathVariable Long userId, HttpSession session, Model model) {
        User adminUser = (User) session.getAttribute("user");
        if (adminUser == null) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return "redirect:/admin/users";
        }
        
        model.addAttribute("userDetail", userOpt.get());
        model.addAttribute("user", adminUser);
        
        return "admin/userDetail";
    }

    /**
     * Tìm kiếm khách hàng theo tên hoặc email
     */
    @GetMapping("/admin/users/search")
    public String searchUsers(@RequestParam(required = false) String keyword, HttpSession session, Model model) {
        User adminUser = (User) session.getAttribute("user");
        if (adminUser == null) {
            return "redirect:/login";
        }
        
        List<User> users;
        String searchKeyword = (keyword != null) ? keyword.trim() : "";
        
        if (searchKeyword.isEmpty()) {
            // Nếu keyword rỗng, hiển thị thông báo yêu cầu nhập từ khóa
            users = userService.findAll(); // Vẫn hiển thị tất cả users
            model.addAttribute("showEmptyKeywordMessage", true);
        } else if (searchKeyword.length() < 2) {
            // Nếu keyword quá ngắn
            users = userService.findAll();
            model.addAttribute("showShortKeywordMessage", true);
        } else {
            // Tìm kiếm bình thường
            users = userService.searchUsers(searchKeyword);
        }
        
        model.addAttribute("users", users);
        model.addAttribute("user", adminUser);
        model.addAttribute("keyword", searchKeyword);
        
        return "admin/users";
    }

    /**
     * Test page để kiểm tra
     */
    @GetMapping("/admin/test")
    public String test() {
        return "admin/test";
    }

    /**
     * Debug page để kiểm tra CSS và JavaScript
     */
    @GetMapping("/admin/debug")
    public String debug() {
        return "admin/debug";
    }

    /**
     * Test page đơn giản để kiểm tra users
     */
    @GetMapping("/admin/users-simple")
    public String usersSimple(HttpSession session, Model model) {
        User adminUser = (User) session.getAttribute("user");
        if (adminUser == null) {
            return "redirect:/login";
        }
        
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        model.addAttribute("user", adminUser);
        
        return "admin/users-simple";
    }
}
