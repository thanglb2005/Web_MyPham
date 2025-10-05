package vn.controller.admin;

import java.security.Principal;
// removed unused import: ArrayList
import java.util.Collections;
// removed unused import: LinkedHashMap
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vn.entity.User;
import vn.repository.OrderDetailRepository;
import vn.repository.ProductRepository;
import vn.repository.CategoryRepository;
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
    private final CategoryRepository categoryRepository;
    private final ProductStatisticsService productStatisticsService;

    @GetMapping("/statistics")
    public String statistics(
            Model model,
            Principal principal,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        User user = resolveUser(principal);
        model.addAttribute("user", user);

        // Optional legacy stats (kept for compatibility if used elsewhere)
        List<Object[]> productStats = orderDetailRepository.getProductSalesStatistics();
        model.addAttribute("productStats", productStats);

        // Build KPI values on server-side
        Map<String, Number> kpis = productStatisticsService.buildKpis();
        model.addAttribute("bestSellers", kpis.getOrDefault("bestSellers", 0));
        model.addAttribute("newProducts", kpis.getOrDefault("newProducts", 0));
        model.addAttribute("slowMoving", kpis.getOrDefault("slowMoving", 0));
        model.addAttribute("trendingUp", kpis.getOrDefault("trendingUp", 0));
        model.addAttribute("lowStockCount", kpis.getOrDefault("lowStock", 0));
        model.addAttribute("expiringSoonCount", kpis.getOrDefault("expiringSoon", 0));
        model.addAttribute("favoritesCount", kpis.getOrDefault("favorites", 0));
        model.addAttribute("discountedCount", kpis.getOrDefault("discounted", 0));

    // Filters and pagination for inventory table
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 5), 100);
    String normSearch = (search == null || search.trim().isEmpty()) ? null : search.trim();

    var pageable = org.springframework.data.domain.PageRequest.of(safePage, safeSize);
    var pageResult = productRepository.getInventoryDetailsPage(categoryId, normSearch, pageable);

    model.addAttribute("inventoryDetails", pageResult.getContent());
    model.addAttribute("currentPage", pageResult.getNumber());
    model.addAttribute("totalPages", pageResult.getTotalPages());
    model.addAttribute("pageSize", pageResult.getSize());
    model.addAttribute("totalElements", pageResult.getTotalElements());

    // Category dropdown
    model.addAttribute("categories", categoryRepository.findAll());
    model.addAttribute("selectedCategoryId", categoryId);
    model.addAttribute("searchTerm", search);

        return "admin/statistics";
    }

    // Removed JSON APIs as per request: dashboard now renders server-side only

    @GetMapping("/statistics/products")
    public String productStatistics(Model model, Principal principal) {
        return statistics(model, principal, null, null, 0, 10);
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
