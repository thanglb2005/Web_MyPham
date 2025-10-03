package vn.controller.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.entity.User;
import vn.repository.OrderRepository;
import vn.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Customer Statistics Controller for Admin Panel
 * Provides comprehensive customer analytics and insights
 * @author OneShop Team
 */
@Controller
@RequestMapping("/admin")
public class CustomerStatisticsController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Main customer statistics dashboard
     */
    @GetMapping("/customer-statistics")
    public String customerStatistics(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        
        // Get basic statistics
        List<Object[]> activityStats = userRepository.getUserActivityStatistics();
        if (!activityStats.isEmpty()) {
            Object[] stats = activityStats.get(0);
            model.addAttribute("totalUsers", stats[0]);
            model.addAttribute("activeUsers", stats[1]);
            model.addAttribute("inactiveUsers", stats[2]);
            model.addAttribute("newUsersToday", stats[3]);
            model.addAttribute("newUsersThisMonth", stats[4]);
        }
        
        // Get status statistics
        List<Object[]> statusStats = userRepository.getUserStatusStatistics();
        model.addAttribute("statusStats", statusStats);
        
        // Get newest users
        List<Object[]> newestUsers = userRepository.getNewestUsers();
        model.addAttribute("newestUsers", newestUsers);
        
        // Get user distribution
        List<Object[]> distributionStats = userRepository.getUserDistributionByPeriod();
        model.addAttribute("distributionStats", distributionStats);
        
        return "admin/customer-statistics";
    }

    // ========== Customer Analytics JSON APIs ==========

    /**
     * Get user registration statistics by month
     */
    @GetMapping("/customer-statistics/api/registration-by-month")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiRegistrationByMonth() {
        List<Object[]> data = userRepository.getUserRegistrationByMonth();
        return ResponseEntity.ok(data);
    }

    /**
     * Get user registration statistics by year
     */
    @GetMapping("/customer-statistics/api/registration-by-year")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiRegistrationByYear() {
        List<Object[]> data = userRepository.getUserRegistrationByYear();
        return ResponseEntity.ok(data);
    }

    /**
     * Get user registration trend (last 12 months)
     */
    @GetMapping("/customer-statistics/api/registration-trend")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiRegistrationTrend() {
        List<Object[]> data = userRepository.getUserRegistrationTrend();
        return ResponseEntity.ok(data);
    }

    /**
     * Get user registration by day (last 30 days)
     */
    @GetMapping("/customer-statistics/api/registration-last-30-days")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiRegistrationLast30Days() {
        List<Object[]> data = userRepository.getUserRegistrationLast30Days();
        return ResponseEntity.ok(data);
    }

    /**
     * Get user status statistics
     */
    @GetMapping("/customer-statistics/api/status-statistics")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiStatusStatistics() {
        List<Object[]> data = userRepository.getUserStatusStatistics();
        return ResponseEntity.ok(data);
    }

    /**
     * Get user activity statistics
     */
    @GetMapping("/customer-statistics/api/activity-statistics")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiActivityStatistics() {
        List<Object[]> data = userRepository.getUserActivityStatistics();
        return ResponseEntity.ok(data);
    }

    /**
     * Get newest users
     */
    @GetMapping("/customer-statistics/api/newest-users")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiNewestUsers() {
        List<Object[]> data = userRepository.getNewestUsers();
        return ResponseEntity.ok(data);
    }

    /**
     * Get user distribution by period
     */
    @GetMapping("/customer-statistics/api/distribution-by-period")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiDistributionByPeriod() {
        List<Object[]> data = userRepository.getUserDistributionByPeriod();
        return ResponseEntity.ok(data);
    }

    /**
     * Get top customers by order value
     */
    @GetMapping("/customer-statistics/api/top-customers-by-value")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiTopCustomersByValue(@RequestParam(defaultValue = "10") int limit) {
        List<Object[]> data = orderRepository.getTopCustomersByValue();
        if (data.size() > limit) {
            data = data.subList(0, limit);
        }
        return ResponseEntity.ok(data);
    }

    /**
     * Get top customers by order count
     */
    @GetMapping("/customer-statistics/api/top-customers-by-count")
    @ResponseBody
    public ResponseEntity<List<Object[]>> apiTopCustomersByCount(@RequestParam(defaultValue = "10") int limit) {
        List<Object[]> data = orderRepository.getTopCustomersByCount();
        if (data.size() > limit) {
            data = data.subList(0, limit);
        }
        return ResponseEntity.ok(data);
    }

    /**
     * Get customer KPIs
     */
    @GetMapping("/customer-statistics/api/kpis")
    @ResponseBody
    public ResponseEntity<Map<String, Number>> apiCustomerKpis() {
        Map<String, Number> kpis = new HashMap<>();
        
        try {
            // Get activity statistics
            List<Object[]> activityStats = userRepository.getUserActivityStatistics();
            if (!activityStats.isEmpty()) {
                Object[] stats = activityStats.get(0);
                kpis.put("totalUsers", (Number) stats[0]);
                kpis.put("activeUsers", (Number) stats[1]);
                kpis.put("inactiveUsers", (Number) stats[2]);
                kpis.put("newUsersToday", (Number) stats[3]);
                kpis.put("newUsersThisMonth", (Number) stats[4]);
            }
            
            // Get top customers count
            List<Object[]> topCustomers = orderRepository.getTopCustomersByValue();
            kpis.put("topCustomers", topCustomers.size());
            
            // Calculate active user percentage
            if (kpis.containsKey("totalUsers") && kpis.containsKey("activeUsers")) {
                long totalUsers = kpis.get("totalUsers").longValue();
                long activeUsers = kpis.get("activeUsers").longValue();
                if (totalUsers > 0) {
                    double activePercentage = (activeUsers * 100.0) / totalUsers;
                    kpis.put("activeUserPercentage", Math.round(activePercentage * 100.0) / 100.0);
                }
            }
            
        } catch (Exception e) {
            // Set default values if there's an error
            kpis.put("totalUsers", 0);
            kpis.put("activeUsers", 0);
            kpis.put("inactiveUsers", 0);
            kpis.put("newUsersToday", 0);
            kpis.put("newUsersThisMonth", 0);
            kpis.put("topCustomers", 0);
            kpis.put("activeUserPercentage", 0.0);
        }
        
        return ResponseEntity.ok(kpis);
    }

    /**
     * Get customer analytics summary
     */
    @GetMapping("/customer-statistics/api/summary")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiCustomerSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            // Basic stats
            List<Object[]> activityStats = userRepository.getUserActivityStatistics();
            if (!activityStats.isEmpty()) {
                Object[] stats = activityStats.get(0);
                summary.put("totalUsers", stats[0]);
                summary.put("activeUsers", stats[1]);
                summary.put("inactiveUsers", stats[2]);
                summary.put("newUsersToday", stats[3]);
                summary.put("newUsersThisMonth", stats[4]);
            }
            
            // Registration trend
            List<Object[]> trend = userRepository.getUserRegistrationTrend();
            summary.put("registrationTrend", trend);
            
            // Status distribution
            List<Object[]> statusStats = userRepository.getUserStatusStatistics();
            summary.put("statusDistribution", statusStats);
            
            // Top customers
            List<Object[]> topCustomers = orderRepository.getTopCustomersByValue();
            summary.put("topCustomers", topCustomers);
            
            // User distribution
            List<Object[]> distribution = userRepository.getUserDistributionByPeriod();
            summary.put("userDistribution", distribution);
            
        } catch (Exception e) {
            summary.put("error", "Unable to load customer statistics");
        }
        
        return ResponseEntity.ok(summary);
    }
}

