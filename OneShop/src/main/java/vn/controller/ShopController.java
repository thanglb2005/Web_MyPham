package vn.controller;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vn.entity.Product;
import vn.entity.User;
import vn.service.CategoryService;
import vn.service.ProductService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ShopController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;
    
    private void addCartCountToModel(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            @SuppressWarnings("unchecked")
            java.util.Map<Long, vn.entity.CartItem> cartMap = (java.util.Map<Long, vn.entity.CartItem>) session.getAttribute("cartMap");
            int cartCount = (cartMap != null) ? cartMap.size() : 0;
            model.addAttribute("totalCartItems", cartCount);
        } else {
            model.addAttribute("totalCartItems", 0);
        }
    }

    @GetMapping(value = "/products")
    public String shop(Model model, Pageable pageable, @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size, HttpSession session) {

        int currentPage = page.orElse(1);
        int pageSize = size.orElse(12);

        Page<Product> productPage = findPaginated(PageRequest.of(currentPage - 1, pageSize));

        int totalPages = productPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        model.addAttribute("products", productPage);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("coutnProductByCategory", productService.listCategoryByProductName());
        
        // Add cart count for header
        addCartCountToModel(session, model);

        // Add best selling products for sidebar widget
        List<Object[]> bestSaleRaw = productService.bestSaleProduct20();
        if (bestSaleRaw != null && !bestSaleRaw.isEmpty()) {
            List<Long> ids = bestSaleRaw.stream()
                    .map(obj -> Long.valueOf(String.valueOf(obj[0])))
                    .collect(Collectors.toList());
            if (!ids.isEmpty()) {
                List<Product> bestProducts = productService.findByInventoryIds(ids);
                model.addAttribute("bestSaleProduct20", bestProducts);
            }
        }

        return "web/shop";
    }

    public Page<Product> findPaginated(Pageable pageable) {
        List<Product> allProducts = productService.findAll();

        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<Product> list;

        if (allProducts.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, allProducts.size());
            list = allProducts.subList(startItem, toIndex);
        }

        return new PageImpl<>(list, PageRequest.of(currentPage, pageSize), allProducts.size());
    }

    @GetMapping(value = "/searchProduct")
    public String showSearch(@RequestParam(value = "productName", required = false) String productName,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "q", required = false) String q,
                             Model model, HttpSession session) {
        String kw = (productName != null) ? productName : (keyword != null ? keyword : (q != null ? q : ""));
        List<Product> results = kw.isEmpty() ? productService.findAll() : productService.searchProduct(kw);
        model.addAttribute("products", new PageImpl<>(results));
        model.addAttribute("pageNumbers", List.of(1));
        model.addAttribute("keyword", kw);
        
        // Add cart count for header
        addCartCountToModel(session, model);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("coutnProductByCategory", productService.listCategoryByProductName());
        
        // Add best selling products
        List<Object[]> bestSaleRaw = productService.bestSaleProduct20();
        if (bestSaleRaw != null && !bestSaleRaw.isEmpty()) {
            List<Long> ids = bestSaleRaw.stream()
                    .map(obj -> Long.valueOf(String.valueOf(obj[0])))
                    .collect(Collectors.toList());
            if (!ids.isEmpty()) {
                List<Product> bestProducts = productService.findByInventoryIds(ids);
                model.addAttribute("bestSaleProduct20", bestProducts);
            }
        }
        
        return "web/shop";
    }

    @GetMapping(value = "/promotions")
    public String promotions(Model model, HttpSession session) {
        // Filter products with discount > 0 and sort by discount desc
        List<Product> discounted = productService.findAll().stream()
                .filter(p -> p.getDiscount() != null && p.getDiscount() > 0)
                .sorted(Comparator.comparingInt(Product::getDiscount).reversed())
                .collect(Collectors.toList());

        model.addAttribute("products", new PageImpl<>(discounted));
        model.addAttribute("pageNumbers", List.of(1));
        model.addAttribute("categories", categoryService.getAllCategories());
        
        // Add cart count for header
        addCartCountToModel(session, model);
        model.addAttribute("coutnProductByCategory", productService.listCategoryByProductName());
        model.addAttribute("promotionView", true);

        // Add best selling products for sidebar widget
        List<Object[]> bestSaleRaw = productService.bestSaleProduct20();
        if (bestSaleRaw != null && !bestSaleRaw.isEmpty()) {
            List<Long> ids = bestSaleRaw.stream()
                    .map(obj -> Long.valueOf(String.valueOf(obj[0])))
                    .collect(Collectors.toList());
            if (!ids.isEmpty()) {
                List<Product> bestProducts = productService.findByInventoryIds(ids);
                model.addAttribute("bestSaleProduct20", bestProducts);
            }
        }

        return "web/shop";
    }

    @GetMapping(value = "/productByCategory")
    public String productByCategory(@RequestParam("id") Long categoryId, Model model, HttpSession session) {
        List<Product> productsByCate = productService.listProductByCategory(categoryId);
        model.addAttribute("products", new PageImpl<>(productsByCate));
        model.addAttribute("pageNumbers", List.of(1));
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("coutnProductByCategory", productService.listCategoryByProductName());
        
        // Add cart count for header
        addCartCountToModel(session, model);
        
        // Add best selling products
        List<Object[]> bestSaleRaw = productService.bestSaleProduct20();
        if (bestSaleRaw != null && !bestSaleRaw.isEmpty()) {
            List<Long> ids = bestSaleRaw.stream()
                    .map(obj -> Long.valueOf(String.valueOf(obj[0])))
                    .collect(Collectors.toList());
            if (!ids.isEmpty()) {
                List<Product> bestProducts = productService.findByInventoryIds(ids);
                model.addAttribute("bestSaleProduct20", bestProducts);
            }
        }
        
        return "web/shop";
    }
}


