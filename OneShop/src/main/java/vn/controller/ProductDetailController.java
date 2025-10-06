package vn.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vn.entity.Product;
import vn.service.CategoryService;
import vn.service.ProductService;

@Controller
public class ProductDetailController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping(value = "/productDetail")
    public String productDetail(@RequestParam("id") Long id, Model model) {
        Optional<Product> opt = productService.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/products";
        }

        Product product = opt.get();
        model.addAttribute("product", product);

        List<Product> related = productService.listProductByCategory10(product.getCategory().getCategoryId());
        model.addAttribute("productByCategory", related);

        model.addAttribute("categories", categoryService.getAllCategories());
        return "web/productDetail";
    }
}


