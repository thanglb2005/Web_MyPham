package vn.controller.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.entity.User;
import vn.service.statistics.CategoryStatisticsService;

import java.util.Collections;
import java.util.List;

/**
 * Controller for category statistics in admin panel
 * @author OneShop Team
 */
@Controller
@RequestMapping("/admin")
public class CategoryStatisticsController {

    @Autowired
    private CategoryStatisticsService categoryStatisticsService;

    /**
     * Main category statistics page
     */
    @GetMapping("/category-statistics")
    public String categoryStatistics(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Get all statistics data
        List<Object[]> productCountStats = categoryStatisticsService.getProductCountByCategory();
        List<Object[]> revenueStats = categoryStatisticsService.getRevenueByCategory();
        List<Object[]> ratingStats = categoryStatisticsService.getRatingByCategory();
        List<Object[]> favoritesStats = categoryStatisticsService.getFavoritesByCategory();
        List<Object[]> comprehensiveStats = categoryStatisticsService.getComprehensiveCategoryStats();

        // Calculate summary data
        long totalProducts = categoryStatisticsService.getTotalProducts();
        double totalRevenue = categoryStatisticsService.getTotalRevenue();
        long totalFavorites = categoryStatisticsService.getTotalFavorites();
        double overallRating = categoryStatisticsService.getOverallAverageRating();

        // Get top categories
        Object[] topCategoryByProducts = categoryStatisticsService.getTopCategoryByProductCount();
        Object[] topCategoryByRevenue = categoryStatisticsService.getTopCategoryByRevenue();
        Object[] topCategoryByRating = categoryStatisticsService.getTopCategoryByRating();
        Object[] topCategoryByFavorites = categoryStatisticsService.getTopCategoryByFavorites();

        model.addAttribute("user", user);
        model.addAttribute("productCountStats", productCountStats);
        model.addAttribute("revenueStats", revenueStats);
        model.addAttribute("ratingStats", ratingStats);
        model.addAttribute("favoritesStats", favoritesStats);
        model.addAttribute("comprehensiveStats", comprehensiveStats);
        
        // Summary data
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalFavorites", totalFavorites);
        model.addAttribute("overallRating", overallRating);
        
        // Top categories
        model.addAttribute("topCategoryByProducts", topCategoryByProducts);
        model.addAttribute("topCategoryByRevenue", topCategoryByRevenue);
        model.addAttribute("topCategoryByRating", topCategoryByRating);
        model.addAttribute("topCategoryByFavorites", topCategoryByFavorites);

        return "admin/category-statistics";
    }

    /**
     * API endpoint for product count chart data
     */
    @GetMapping("/category-statistics/api/product-count")
    public String productCountApi(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Object[]> stats = categoryStatisticsService.getProductCountByCategory();
        model.addAttribute("stats", stats);
        
        return "admin/fragments/product-count-chart";
    }

    /**
     * API endpoint for revenue chart data
     */
    @GetMapping("/category-statistics/api/revenue")
    public String revenueApi(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Object[]> stats = categoryStatisticsService.getRevenueByCategory();
        model.addAttribute("stats", stats);
        
        return "admin/fragments/revenue-chart";
    }

    /**
     * API endpoint for rating chart data
     */
    @GetMapping("/category-statistics/api/rating")
    public String ratingApi(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Object[]> stats = categoryStatisticsService.getRatingByCategory();
        model.addAttribute("stats", stats);
        
        return "admin/fragments/rating-chart";
    }

    /**
     * API endpoint for favorites chart data
     */
    @GetMapping("/category-statistics/api/favorites")
    public String favoritesApi(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Object[]> stats = categoryStatisticsService.getFavoritesByCategory();
        model.addAttribute("stats", stats);
        
        return "admin/fragments/favorites-chart";
    }

    /**
     * Export category statistics to Excel
     */
    @GetMapping("/category-statistics/export")
    public String exportCategoryStats(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Object[]> comprehensiveStats = categoryStatisticsService.getComprehensiveCategoryStats();
        model.addAttribute("stats", comprehensiveStats);
        
        return "admin/category-stats-export";
    }

    /**
     * Filter statistics by date range
     */
    @GetMapping("/category-statistics/filter")
    public String filterCategoryStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpSession session, Model model) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Get filtered data
        List<Object[]> revenueStats = Collections.emptyList();
        if (startDate != null && endDate != null) {
            revenueStats = categoryStatisticsService.getRevenueByCategoryInDateRange(startDate, endDate);
        } else {
            revenueStats = categoryStatisticsService.getRevenueByCategory();
        }

        // Get other stats (not filtered by date)
        List<Object[]> productCountStats = categoryStatisticsService.getProductCountByCategory();
        List<Object[]> ratingStats = categoryStatisticsService.getRatingByCategory();
        List<Object[]> favoritesStats = categoryStatisticsService.getFavoritesByCategory();
        List<Object[]> comprehensiveStats = categoryStatisticsService.getComprehensiveCategoryStats();

        model.addAttribute("user", user);
        model.addAttribute("productCountStats", productCountStats);
        model.addAttribute("revenueStats", revenueStats);
        model.addAttribute("ratingStats", ratingStats);
        model.addAttribute("favoritesStats", favoritesStats);
        model.addAttribute("comprehensiveStats", comprehensiveStats);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "admin/category-statistics";
    }
}
