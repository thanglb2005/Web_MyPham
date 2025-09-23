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

    /**
     * Hiển thị danh sách tất cả khách hàng
     */
    @GetMapping("/admin/users")
    public String users(HttpSession session, Model model) {
        User adminUser = (User) session.getAttribute("user");
        if (adminUser == null) {
            return "redirect:/login";
        }
        
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("user", adminUser);
        
        return "admin/users";
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
        
        Optional<User> userOpt = userService.getUserById(userId);
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
            users = userService.getAllUsers(); // Vẫn hiển thị tất cả users
            model.addAttribute("showEmptyKeywordMessage", true);
        } else if (searchKeyword.length() < 2) {
            // Nếu keyword quá ngắn
            users = userService.getAllUsers();
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
        
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("user", adminUser);
        
        return "admin/users-simple";
    }
}
