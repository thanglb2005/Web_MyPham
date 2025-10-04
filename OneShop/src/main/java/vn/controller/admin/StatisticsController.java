package vn.controller.admin;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import vn.entity.User;
import vn.repository.OrderDetailRepository;
import vn.repository.ProductRepository;
import vn.repository.UserRepository;
import vn.service.statistics.ProductStatisticsService;
import vn.service.statistics.ProductStatisticsService.ProductStatisticsResult;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class StatisticsController {

    private final UserRepository userRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final ProductStatisticsService productStatisticsService;

    @GetMapping("/statistics")
    public String statistics(Model model, Principal principal) {
        User user = resolveUser(principal);
        model.addAttribute("user", user);

        List<Object[]> productStats = orderDetailRepository.getProductSalesStatistics();
        model.addAttribute("productStats", productStats);

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

    @GetMapping("/statistics/api/top-products")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiTopProducts(@RequestParam(defaultValue = "10") int limit) {
        int safeLimit = Math.max(limit, 1);
        return ResponseEntity.ok(productRepository.getTopSellingProducts(safeLimit));
    }

    @GetMapping("/statistics/api/inventory")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiInventory() {
        return ResponseEntity.ok(productRepository.getInventoryStatistics());
    }

    @GetMapping("/statistics/api/low-stock")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiLowStock(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(productStatisticsService.fetchLowStockPreview(Math.max(limit, 1)));
    }

    @GetMapping("/statistics/api/expiring-soon")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiExpiringSoon(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(productStatisticsService.fetchExpiringPreview(Math.max(limit, 1)));
    }

    @GetMapping("/statistics/api/favorites")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiFavorites(@RequestParam(defaultValue = "25") int limit) {
        return ResponseEntity.ok(productStatisticsService.fetchFavoritesPreview(Math.max(limit, 1)));
    }

    @GetMapping("/statistics/api/discounts")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiDiscounts() {
        return ResponseEntity.ok(productRepository.getDiscountStatistics());
    }

    @GetMapping("/statistics/api/discount-products")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiDiscountProducts(@RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(productStatisticsService.fetchDiscountedPreview(Math.max(limit, 1)));
    }

    @GetMapping("/statistics/api/manufacture")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiManufacture() {
        return ResponseEntity.ok(productRepository.getManufactureDateStatistics());
    }

    @GetMapping("/statistics/api/expiry")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiExpiry() {
        return ResponseEntity.ok(productRepository.getExpiryDateStatistics());
    }

    @GetMapping("/statistics/api/kpis")
    @ResponseBody
    public ResponseEntity<Map<String, Number>> apiKpis() {
        Map<String, Number> kpis = productStatisticsService.buildKpis();
        return ResponseEntity.ok(kpis);
    }

    @GetMapping("/statistics/products")
    public String productStatistics(Model model, Principal principal) {
        return statistics(model, principal);
    }





    @GetMapping("/statistics/view")
    public String statisticsListView(
            @RequestParam String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "quantity") String sortBy,
            Model model,
            Principal principal) {

        try {
            System.out.println("=== STATISTICS VIEW REQUEST ===");
            System.out.println("Type: " + type + ", Page: " + page + ", Size: " + size + ", Search: " + search + ", SortBy: " + sortBy);
            
            // Simplified user handling (like debug method)
            User user = resolveUser(principal);
            if (user == null) {
                user = new User();
                user.setEmail("admin@test.com");
                System.out.println("Using dummy user for compatibility");
            }
            model.addAttribute("user", user);

            // Fetch data using the same logic as debug method with sortBy parameter
            ProductStatisticsResult result = productStatisticsService.fetchList(type, page, size, search, sortBy);
            System.out.println("Fetched " + result.rows().size() + " rows for type: " + type + " sorted by: " + sortBy);

            // Set model attributes (same as debug method that works)
            model.addAttribute("type", result.type());
            model.addAttribute("title", result.title());
            model.addAttribute("rows", result.rows());
            model.addAttribute("total", result.totalElements());
            model.addAttribute("currentPage", result.page());
            model.addAttribute("pageSize", result.size());
            model.addAttribute("totalPages", result.totalPages());
            model.addAttribute("search", result.search());
            model.addAttribute("sortBy", sortBy);

            System.out.println("Successfully loaded " + result.rows().size() + " items with sortBy: " + sortBy);
            return "admin/statistics_list2";
            
        } catch (Exception e) {
            System.err.println("ERROR in statisticsListView: " + e.getMessage());
            e.printStackTrace();
            
            // Error handling (same as debug method)
            model.addAttribute("error", "Lỗi khi tải dữ liệu: " + e.getMessage());
            model.addAttribute("type", type);
            model.addAttribute("rows", Collections.emptyList());
            model.addAttribute("total", 0L);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages", 0);
            model.addAttribute("search", search);
            return "admin/statistics_list2";
        }
    }

    private User resolveUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userRepository.findByEmail(principal.getName()).orElse(null);
    }
}
