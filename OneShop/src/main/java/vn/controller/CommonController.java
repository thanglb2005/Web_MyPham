package vn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ControllerAdvice;

import vn.entity.Category;
import vn.service.CategoryService;

import java.util.List;

@ControllerAdvice
public class CommonController {

    @Autowired
    private CategoryService categoryService;

    @ModelAttribute("categories")
    public List<Category> categories() {
        return categoryService.getAllCategories();
    }

    @ModelAttribute("keyword")
    public String keyword() {
        return "";
    }
}


