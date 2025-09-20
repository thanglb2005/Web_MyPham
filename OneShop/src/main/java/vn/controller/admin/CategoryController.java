package vn.controller.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.entity.Category;
import vn.entity.User;
import vn.service.CategoryService;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/categories")
    public String categories(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "admin/categories";
    }

    @PostMapping("/categories/add")
    public String addCategory(@RequestParam String categoryName, 
                             @RequestParam(required = false) String categoryImage, 
                             HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Category category = new Category();
        category.setCategoryName(categoryName);
        category.setCategoryImage(categoryImage);
        categoryService.saveCategory(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategoryForm(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Category category = categoryService.getCategoryById(id).orElse(null);
        if (category != null) {
            model.addAttribute("category", category);
            return "admin/editCategory";
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/edit/{id}")
    public String editCategory(@PathVariable Long id, 
                              @RequestParam String categoryName,
                              @RequestParam(required = false) String categoryImage,
                              HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Category category = categoryService.getCategoryById(id).orElse(null);
        if (category != null) {
            category.setCategoryName(categoryName);
            category.setCategoryImage(categoryImage);
            categoryService.saveCategory(category);
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Xóa luôn tất cả products liên quan
        categoryService.deleteCategory(id);
        return "redirect:/admin/categories";
    }
}
