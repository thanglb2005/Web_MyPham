package vn.controller.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.entity.User;
import vn.service.CategoryService;
import vn.service.ProductService;
import vn.repository.OrderRepository;
import vn.repository.BlogPostRepository;
import vn.service.UserService;

@Controller
public class IndexAdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private BlogPostRepository blogPostRepository;

    @GetMapping("/admin")
    public String admin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        return "redirect:/admin/home";
    }

    @GetMapping("/admin/home")
    public String adminHome(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Thống kê cơ bản
        long totalUsers = userService.findAll().size();
        long totalCategories = categoryService.getAllCategories().size();
        long totalProducts = 0L;
        long totalOrders = 0L;
        long totalBlogs = 0L;

        try {
            totalProducts = productService.findAll().size();
        } catch (Exception ignored) {}

        try {
            totalOrders = orderRepository.count();
        } catch (Exception ignored) {}
        
        try {
            totalBlogs = blogPostRepository.count();
        } catch (Exception ignored) {}
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalBlogs", totalBlogs);
        
        return "admin/index";
    }
}
