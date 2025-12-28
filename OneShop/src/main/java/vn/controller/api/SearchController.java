package vn.controller.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.entity.Category;
import vn.entity.Product;
import vn.repository.CategoryRepository;
import vn.repository.ProductRepository;

/**
 * API Controller for search autocomplete
 * @author OneShop Team
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public SearchController(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Autocomplete endpoint
     * Returns suggestions based on search query
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<List<Map<String, String>>> autocomplete(@RequestParam(required = false, defaultValue = "") String q) {
        String query = q == null ? "" : q.trim();
        if (query.length() < 2) {
            return ResponseEntity.ok(List.of());
        }

        final int limit = 10;
        Set<String> names = new LinkedHashSet<>();

        // 1) Normal keyword-based suggestions (name/brand/category via existing query)
        List<Product> products = productRepository.searchProductsByKeyword(query, limit);
        for (Product product : products) {
            if (product == null) continue;
            String name = product.getProductName();
            if (name == null || name.isBlank()) continue;
            names.add(name);
        }

        // 2) If query matches a category name, also include products in that category
        if (names.size() < limit) {
            List<Category> matchedCategories = categoryRepository
                    .findByCategoryNameContainingIgnoreCase(query, PageRequest.of(0, 3))
                    .getContent();

            if (!matchedCategories.isEmpty()) {
                List<Long> categoryIds = new ArrayList<>();
                for (Category category : matchedCategories) {
                    if (category == null || category.getCategoryId() == null) continue;
                    categoryIds.add(category.getCategoryId());
                }

                if (!categoryIds.isEmpty()) {
                    int remaining = limit - names.size();
                    List<Product> categoryProducts = productRepository.findActiveProductsByCategoryIds(categoryIds, remaining);
                    for (Product product : categoryProducts) {
                        if (product == null) continue;
                        String name = product.getProductName();
                        if (name == null || name.isBlank()) continue;
                        names.add(name);
                        if (names.size() >= limit) break;
                    }
                }
            }
        }

        List<Map<String, String>> suggestions = new ArrayList<>();
        for (String name : names) {
            Map<String, String> suggestion = new HashMap<>();
            suggestion.put("name", name);
            suggestions.add(suggestion);
            if (suggestions.size() >= limit) break;
        }

        return ResponseEntity.ok(suggestions);
    }
}
