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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import vn.entity.Product;
import vn.entity.Shop;
import vn.entity.User;
import vn.service.CategoryService;
import vn.service.ProductService;
import vn.service.ShopService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ShopController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ShopService shopService;
    
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

    @GetMapping("/shop/{slug}")
    public String viewShop(@PathVariable String slug,
                           @RequestParam(value = "page") Optional<Integer> page,
                           @RequestParam(value = "size") Optional<Integer> size,
                           @RequestParam(value = "categoryId") Optional<Long> categoryId,
                           @RequestParam(value = "sort", required = false) Optional<String> sort,
                           @RequestParam(value = "minPrice", required = false) Optional<Double> minPrice,
                           @RequestParam(value = "maxPrice", required = false) Optional<Double> maxPrice,
                           HttpSession session,
                           Model model) {
        Shop shop = shopService.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean isActive = shop.getStatus() == Shop.ShopStatus.ACTIVE;

        int currentPage = page.orElse(1);
        int pageSize = size.orElse(12);
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);

        List<Product> activeProducts = isActive
                ? productService.findByShopId(shop.getShopId()).stream()
                    .filter(p -> Boolean.TRUE.equals(p.getStatus()))
                    .collect(Collectors.toList())
                : Collections.emptyList();

        List<Product> filteredProducts = activeProducts;
        if (isActive && categoryId.isPresent()) {
            Long selectedCategoryId = categoryId.get();
            filteredProducts = activeProducts.stream()
                    .filter(p -> p.getCategory() != null && selectedCategoryId.equals(p.getCategory().getCategoryId()))
                    .collect(Collectors.toList());
            model.addAttribute("selectedCategoryId", selectedCategoryId);
        } else {
            model.addAttribute("selectedCategoryId", null);
        }

        // Apply price filtering if provided
        if (isActive && (minPrice.isPresent() || maxPrice.isPresent())) {
            double min = minPrice.orElse(0.0);
            double max = maxPrice.orElse(Double.MAX_VALUE);
            filteredProducts = filteredProducts.stream()
                    .filter(p -> {
                        try {
                            double price = effectivePrice(p);
                            return price >= min && price <= max;
                        } catch (Exception e) { return false; }
                    })
                    .collect(Collectors.toList());
        }

        // Apply sorting if provided
        if (isActive && sort.isPresent()) {
            filteredProducts = sortProducts(filteredProducts, sort.get());
        }

        Page<Product> productPage = isActive
                ? findPaginated(filteredProducts, pageable)
                : new PageImpl<>(Collections.emptyList(), pageable, 0);

        int totalPages = productPage.getTotalPages();
        if (currentPage > totalPages && totalPages > 0) {
            return "redirect:/shop/" + slug + "?page=" + totalPages + "&size=" + pageSize + (categoryId.isPresent() ? ("&categoryId=" + categoryId.get()) : "");
        }
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        } else {
            model.addAttribute("pageNumbers", Collections.emptyList());
        }

        addCartCountToModel(session, model);
        model.addAttribute("products", productPage);
        try {
            java.util.Set<Long> ids = productPage.getContent().stream()
                    .map(Product::getProductId)
                    .collect(java.util.stream.Collectors.toSet());
            java.util.Map<Long, Double> avgMap = productService.getAverageRatings(ids);
            model.addAttribute("avgMap", avgMap);
        } catch (Exception ignored) {}
        populateSidebar(model, isActive ? activeProducts : Collections.emptyList());
        model.addAttribute("pageTitle", shop.getShopName());
        model.addAttribute("shop", shop);
        model.addAttribute("inactiveShop", !isActive);
        model.addAttribute("shopStatusLabel", shop.getStatus());
        // Preselect current shop in sidebar dropdown
        try { model.addAttribute("selectedShopId", shop.getShopId()); } catch (Exception ignored) {}
        return "web/shop";
    }

    @GetMapping(value = "/products")
    public String shop(Model model, Pageable pageable, @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size, 
            @RequestParam(value = "shopId", required = false) Long shopId,
            @RequestParam(value = "sort", required = false) Optional<String> sort,
            @RequestParam(value = "minPrice", required = false) Optional<Double> minPrice,
            @RequestParam(value = "maxPrice", required = false) Optional<Double> maxPrice,
            HttpSession session) {

        int currentPage = page.orElse(1);
        int pageSize = size.orElse(12);

        List<Product> allProducts;
        if (shopId != null) {
            // Filter products by shop
            allProducts = productService.findByShopId(shopId).stream()
                    .filter(p -> Boolean.TRUE.equals(p.getStatus()))
                    .collect(Collectors.toList());
            model.addAttribute("selectedShopId", shopId);
            // Load shop info to show header card like /shop/{slug}
            try {
                shopService.findById(shopId).ifPresent(s -> model.addAttribute("shop", s));
            } catch (Exception ignored) {}
        } else {
            // Show all products
            allProducts = productService.findAll().stream()
                    .filter(p -> Boolean.TRUE.equals(p.getStatus()))
                    .collect(Collectors.toList());
            model.addAttribute("selectedShopId", null);
        }

        // Apply price filtering if provided
        if (minPrice.isPresent() || maxPrice.isPresent()) {
            double min = minPrice.orElse(0.0);
            double max = maxPrice.orElse(Double.MAX_VALUE);
            allProducts = allProducts.stream()
                    .filter(p -> {
                        try {
                            double price = effectivePrice(p);
                            return price >= min && price <= max;
                        } catch (Exception e) { return false; }
                    })
                    .collect(Collectors.toList());
        }

        // Apply sorting if provided
        if (sort.isPresent()) {
            allProducts = sortProducts(allProducts, sort.get());
        }

        Page<Product> productPage = findPaginated(allProducts, PageRequest.of(currentPage - 1, pageSize));

        int totalPages = productPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        } else {
            model.addAttribute("pageNumbers", Collections.emptyList());
        }

        model.addAttribute("products", productPage);
        try {
            java.util.Set<Long> ids = productPage.getContent().stream()
                    .map(Product::getProductId)
                    .collect(java.util.stream.Collectors.toSet());
            java.util.Map<Long, Double> avgMap = productService.getAverageRatings(ids);
            model.addAttribute("avgMap", avgMap);
        } catch (Exception ignored) {}
        populateSidebar(model, null);
        if (shopId == null) {
            model.addAttribute("shop", null);
        }
        model.addAttribute("selectedCategoryId", null);
        
        // Add cart count for header
        addCartCountToModel(session, model);

        return "web/shop";
    }

    // Helper: compute effective price after discount
    private double effectivePrice(Product p) {
        double base = 0.0;
        try {
            base = p.getPrice() != null ? p.getPrice() : 0.0;
        } catch (Exception ignored) {}
        int discount = 0;
        try {
            discount = p.getDiscount() != null ? p.getDiscount() : 0;
        } catch (Exception ignored) {}
        return base * (1.0 - (discount / 100.0));
    }

    // Helper: sort by given key
    private List<Product> sortProducts(List<Product> list, String sortKey) {
        if (sortKey == null || sortKey.isBlank()) return list;
        Comparator<Product> comp = null;
        switch (sortKey) {
            case "name-asc" -> comp = Comparator.comparing(
                    (Product p) -> p.getProductName() != null ? p.getProductName().toLowerCase() : "");
            case "name-desc" -> comp = Comparator.comparing(
                    (Product p) -> p.getProductName() != null ? p.getProductName().toLowerCase() : "")
                    .reversed();
            case "price-asc" -> comp = Comparator.comparingDouble((Product p) -> effectivePrice(p));
            case "price-desc" -> comp = Comparator.comparingDouble((Product p) -> effectivePrice(p)).reversed();
            case "newest" -> comp = Comparator.comparing(
                    (Product p) -> p.getEnteredDate(),
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
            ).reversed();
            case "discount" -> comp = Comparator.<Product>comparingInt(p -> p.getDiscount() != null ? p.getDiscount() : 0)
                    .reversed();
            default -> { /* no-op */ }
        }
        if (comp == null) return list;
        return list.stream().sorted(comp).collect(Collectors.toList());
    }

    public Page<Product> findPaginated(Pageable pageable) {
        List<Product> allProducts = productService.findAll();
        return findPaginated(allProducts, pageable);
    }

    private Page<Product> findPaginated(List<Product> source, Pageable pageable) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int totalElements = source.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        
        // Ensure currentPage is within valid range
        if (currentPage >= totalPages && totalPages > 0) {
            currentPage = totalPages - 1; // Convert to 0-based index
        }
        if (currentPage < 0) {
            currentPage = 0;
        }
        
        int startItem = currentPage * pageSize;
        List<Product> list;

        if (startItem >= totalElements) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, totalElements);
            list = source.subList(startItem, toIndex);
        }

        return new PageImpl<>(list, PageRequest.of(currentPage, pageSize), totalElements);
    }

    @GetMapping(value = "/searchProduct")
    public String showSearch(@RequestParam(value = "productName", required = false) String productName,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "q", required = false) String q,
                             Model model, HttpSession session) {
        String kw = (productName != null) ? productName : (keyword != null ? keyword : (q != null ? q : ""));
        List<Product> results = kw.isEmpty() ? productService.findAll() : productService.searchProduct(kw);
        model.addAttribute("products", new PageImpl<>(results));
        try {
            java.util.Set<Long> ids = results.stream()
                    .map(Product::getProductId)
                    .collect(java.util.stream.Collectors.toSet());
            java.util.Map<Long, Double> avgMap = productService.getAverageRatings(ids);
            model.addAttribute("avgMap", avgMap);
        } catch (Exception ignored) {}
        model.addAttribute("pageNumbers", List.of(1));
        model.addAttribute("keyword", kw);
        model.addAttribute("selectedCategoryId", null);
        
        // Add cart count for header
        addCartCountToModel(session, model);
        populateSidebar(model, null);
        model.addAttribute("shop", null);
        
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
        try {
            java.util.Set<Long> ids = discounted.stream()
                    .map(Product::getProductId)
                    .collect(java.util.stream.Collectors.toSet());
            java.util.Map<Long, Double> avgMap = productService.getAverageRatings(ids);
            model.addAttribute("avgMap", avgMap);
        } catch (Exception ignored) {}
        model.addAttribute("pageNumbers", List.of(1));
        model.addAttribute("selectedCategoryId", null);
        
        // Add cart count for header
        addCartCountToModel(session, model);
        populateSidebar(model, null);
        model.addAttribute("shop", null);
        model.addAttribute("promotionView", true);

        return "web/shop";
    }

    @GetMapping(value = "/productByCategory")
    public String productByCategory(@RequestParam("id") Long categoryId,
                                    @RequestParam(value = "page") Optional<Integer> page,
                                    @RequestParam(value = "size") Optional<Integer> size,
                                    Model model,
                                    HttpSession session) {
        List<Product> productsByCate = productService.listProductByCategory(categoryId);
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(12);
        Pageable pageable = PageRequest.of(Math.max(currentPage - 1, 0), pageSize);

        Page<Product> productPage = findPaginated(productsByCate, pageable);
        model.addAttribute("products", productPage);
        try {
            java.util.Set<Long> ids = productPage.getContent().stream()
                    .map(Product::getProductId)
                    .collect(java.util.stream.Collectors.toSet());
            java.util.Map<Long, Double> avgMap = productService.getAverageRatings(ids);
            model.addAttribute("avgMap", avgMap);
        } catch (Exception ignored) {}

        int totalPages = productPage.getTotalPages();
        if (currentPage > totalPages && totalPages > 0) {
            return "redirect:/productByCategory?id=" + categoryId + "&page=" + totalPages + "&size=" + pageSize;
        }
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        } else {
            model.addAttribute("pageNumbers", Collections.emptyList());
        }
        
        // Add cart count for header
        addCartCountToModel(session, model);
        populateSidebar(model, null);
        model.addAttribute("shop", null);
        model.addAttribute("selectedCategoryId", categoryId);
        
        return "web/shop";
    }

    private void populateSidebar(Model model, List<Product> scopedProducts) {
        model.addAttribute("categories", categoryService.getAllCategories());
        
        // Add shop list for filtering
        List<Shop> activeShops = shopService.findAll().stream()
                .filter(shop -> shop.getStatus() == Shop.ShopStatus.ACTIVE)
                .collect(Collectors.toList());
        model.addAttribute("shopList", activeShops);

        if (scopedProducts != null) {
            java.util.Map<Long, Object[]> stats = new java.util.LinkedHashMap<>();
            for (Product product : scopedProducts) {
                if (product.getCategory() == null) {
                    continue;
                }
                Long categoryId = product.getCategory().getCategoryId();
                String categoryName = product.getCategory().getCategoryName();
                Object[] row = stats.computeIfAbsent(categoryId, id -> new Object[]{id, categoryName, 0L});
                row[2] = ((Long) row[2]) + 1;
            }
            model.addAttribute("coutnProductByCategory", stats.values().stream().collect(Collectors.toList()));
            List<Product> bestSaleProducts = scopedProducts.stream()
                    .sorted(Comparator.comparingInt((Product p) -> p.getQuantity() != null ? p.getQuantity() : 0).reversed())
                    .limit(8)
                    .collect(Collectors.toList());
            model.addAttribute("bestSaleProduct20", bestSaleProducts);
            
            // Add avgMap for sidebar products
            try {
                java.util.Set<Long> sidebarIds = bestSaleProducts.stream()
                        .map(Product::getProductId)
                        .collect(java.util.stream.Collectors.toSet());
                java.util.Map<Long, Double> sidebarAvgMap = productService.getAverageRatings(sidebarIds);
                model.addAttribute("sidebarAvgMap", sidebarAvgMap);
            } catch (Exception ignored) {}
        } else {
            model.addAttribute("coutnProductByCategory", productService.listCategoryByProductName());

            List<Object[]> bestSaleRaw = productService.bestSaleProduct20();
            if (bestSaleRaw != null && !bestSaleRaw.isEmpty()) {
                List<Long> ids = bestSaleRaw.stream()
                        .map(obj -> Long.valueOf(String.valueOf(obj[0])))
                        .collect(Collectors.toList());
                if (!ids.isEmpty()) {
                    List<Product> bestProducts = productService.findByInventoryIds(ids);
                    model.addAttribute("bestSaleProduct20", bestProducts);
                    
                    // Add avgMap for sidebar products
                    try {
                        java.util.Set<Long> sidebarIds = bestProducts.stream()
                                .map(Product::getProductId)
                                .collect(java.util.stream.Collectors.toSet());
                        java.util.Map<Long, Double> sidebarAvgMap = productService.getAverageRatings(sidebarIds);
                        model.addAttribute("sidebarAvgMap", sidebarAvgMap);
                    } catch (Exception ignored) {}
                }
            }
        }
    }
}
