package vn.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vn.entity.Product;
import vn.entity.Comment;
import vn.service.CategoryService;
import vn.service.ProductService;
import vn.service.CommentService;

@Controller
public class ProductDetailController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CommentService commentService;

    @GetMapping(value = "/productDetail")
    public String productDetail(@RequestParam(value = "id", required = false) Long id, Model model) {
        if (id == null) {
            return "redirect:/"; // fallback về trang chủ nếu thiếu id
        }
        Optional<Product> opt = productService.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/";
        }

        Product product = opt.get();
        model.addAttribute("product", product);

        List<Product> related = productService.listProductByCategory10(product.getCategory().getCategoryId());
        model.addAttribute("productByCategory", related);
        
        // Tính avgMap cho sản phẩm cùng loại
        try {
            java.util.Set<Long> relatedIds = related.stream()
                    .map(Product::getProductId)
                    .collect(java.util.stream.Collectors.toSet());
            java.util.Map<Long, Double> relatedAvgMap = productService.getAverageRatings(relatedIds);
            model.addAttribute("relatedAvgMap", relatedAvgMap);
        } catch (Exception ignored) {}

        // Reviews for product
        List<Comment> reviews = commentService.getCommentsByProduct(product.getProductId());
        model.addAttribute("reviews", reviews);
        int count = reviews != null ? reviews.size() : 0;
        model.addAttribute("reviewCount", count);
        double avg = 0.0;
        if (count > 0) {
            avg = reviews.stream()
                    .filter(r -> r.getRating() != null)
                    .mapToDouble(Comment::getRating)
                    .average()
                    .orElse(0.0);
        }
        model.addAttribute("avgRating", avg);

        model.addAttribute("categories", categoryService.getAllCategories());
        return "web/productDetail";
    }
}


