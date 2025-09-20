package vn.controller.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.entity.User;
import vn.service.CategoryService;
import vn.service.UserService;

@Controller
public class IndexAdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/admin/home")
    public String adminHome(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Thống kê cơ bản
        long totalUsers = userService.getAllUsers().size();
        long totalCategories = categoryService.getAllCategories().size();
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalCategories", totalCategories);
        
        return "admin/index";
    }
}
