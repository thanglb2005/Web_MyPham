package vn.controller.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.entity.User;
import vn.repository.CategoryRepository;

/**
 * Test controller for category statistics
 * @author OneShop Team
 */
@Controller
@RequestMapping("/admin")
public class CategoryStatsTestController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/category-stats-test")
    public String categoryStatsTest(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Simple test data
        model.addAttribute("user", user);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("totalCategories", categoryRepository.count());
        model.addAttribute("message", "Category Statistics Test Page");

        return "admin/category-stats-test";
    }
}
