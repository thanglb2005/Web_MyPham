package vn.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.entity.User;
import vn.repository.OrderDetailRepository;
import vn.repository.OrderRepository;
import vn.repository.ProductRepository;
import vn.repository.UserRepository;

import java.security.Principal;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Statistics Controller for Admin Panel
 * Provides comprehensive product and sales analytics
 * @author OneShop Team
 */
@Controller
@RequestMapping("/admin")
public class StatisticsController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Main statistics dashboard
     */
    @GetMapping("/statistics")
    public String statistics(Model model, Principal principal) {
        User user = null;
        if (principal != null) {
            user = userRepository.findByEmail(principal.getName()).orElse(null);
        }
        model.addAttribute("user", user);

        // Product-centric analytics
        List<Object[]> productStats = orderDetailRepository.getProductSalesStatistics();
        model.addAttribute("productStats", productStats);

        // Provide placeholders
        model.addAttribute("categoryStats", Collections.emptyList());
        model.addAttribute("brandStats", Collections.emptyList());
        model.addAttribute("monthlyStats", Collections.emptyList());
        model.addAttribute("yearlyStats", Collections.emptyList());
        model.addAttribute("quarterlyStats", Collections.emptyList());
        model.addAttribute("userStats", Collections.emptyList());
        model.addAttribute("favoriteStats", Collections.emptyList());
        model.addAttribute("inventoryStats", Collections.emptyList());
        model.addAttribute("lowStock", Collections.emptyList());
        model.addAttribute("expiringSoon", Collections.emptyList());
        model.addAttribute("priceRangeStats", Collections.emptyList());
        model.addAttribute("discountStats", Collections.emptyList());
        model.addAttribute("manufactureStats", Collections.emptyList());
        model.addAttribute("expiryStats", Collections.emptyList());

        return "admin/statistics";
    }

    // ========== Product analytics JSON APIs (lazy-load) ==========

    @GetMapping("/statistics/api/top-products")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiTopProducts(@RequestParam(defaultValue = "10") int limit) {
        List<Object[]> data = productRepository.getTopSellingProducts(limit);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/statistics/api/inventory")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiInventory() {
        List<Object[]> data = productRepository.getInventoryStatistics();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/statistics/api/low-stock")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiLowStock() {
        List<Object[]> raw = productRepository.getLowStockProducts();
        // Transform to [product_name, quantity] to match UI expectations
        List<Object[]> data = new ArrayList<>();
        for (Object[] r : raw) {
            Object name = r.length > 0 ? r[0] : null;
            Object qty = r.length > 3 ? r[3] : null;
            data.add(new Object[]{ name, qty });
        }
        return ResponseEntity.ok(data);
    }

    @GetMapping("/statistics/api/expiring-soon")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiExpiringSoon() {
        List<Object[]> raw = productRepository.getProductsExpiringSoon();
        // Transform to [product_name, expiry_date]
        List<Object[]> data = new ArrayList<>();
        for (Object[] r : raw) {
            Object name = r.length > 0 ? r[0] : null;
            Object expiry = r.length > 3 ? r[3] : null;
            data.add(new Object[]{ name, expiry });
        }
        return ResponseEntity.ok(data);
    }

    @GetMapping("/statistics/api/favorites")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiFavorites() {
        List<Object[]> data = productRepository.getFavoriteProductsStatistics();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/statistics/api/discounts")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiDiscounts() {
        List<Object[]> data = productRepository.getDiscountStatistics();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/statistics/api/discount-products")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiDiscountProducts() {
        // Returns: product_name, category_name, brand_name, discount
        List<Object[]> data = productRepository.getDiscountedProducts();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/statistics/api/manufacture")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiManufacture() {
        List<Object[]> data = productRepository.getManufactureDateStatistics();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/statistics/api/expiry")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiExpiry() {
        List<Object[]> data = productRepository.getExpiryDateStatistics();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/statistics/api/kpis")
    @ResponseBody
    public ResponseEntity<Map<String, Number>> apiKpis() {
        Map<String, Number> kpis = new HashMap<>();

        int lowStock = 0;
        try { lowStock = productRepository.getLowStockProducts().size(); } catch (Exception ignored) {}

        int expiringSoon = 0;
        try { expiringSoon = productRepository.getProductsExpiringSoon().size(); } catch (Exception ignored) {}

        int favoriteCount = 0;
        try {
            for (Object[] r : productRepository.getFavoriteProductsStatistics()) {
                if (r[2] instanceof Number) favoriteCount += ((Number) r[2]).intValue();
            }
        } catch (Exception ignored) {}

        int discountedProducts = 0;
        try {
            for (Object[] r : productRepository.getDiscountStatistics()) {
                if (r[4] instanceof Number) discountedProducts += ((Number) r[4]).intValue();
            }
        } catch (Exception ignored) {}

        kpis.put("lowStock", lowStock);
        kpis.put("expiringSoon", expiringSoon);
        kpis.put("favorites", favoriteCount);
        kpis.put("discounted", discountedProducts);
        return ResponseEntity.ok(kpis);
    }

    /**
     * Statistics by product
     */
    @GetMapping("/statistics/products")
    public String productStatistics(Model model, Principal principal) {
        return statistics(model, principal);
    }

    // All other statistics endpoints are removed to focus on product analytics only
}
