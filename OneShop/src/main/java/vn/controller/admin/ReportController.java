package vn.controller.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import vn.entity.Shop;
import vn.entity.User;
import vn.repository.OrderDetailRepository;
import vn.repository.ShopRepository;

/**
 * Controller for statistics reports with date filtering support
 */
@Controller
@RequestMapping("/admin")
public class ReportController {

    @Autowired
    private OrderDetailRepository orderDetailRepository;
    
    @Autowired
    private ShopRepository shopRepository;
    
    /**
     * Parse date string to LocalDateTime
     */
    private LocalDateTime parseDate(String dateStr, boolean isStartOfDay) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return isStartOfDay ? date.atStartOfDay() : date.atTime(LocalTime.MAX);
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing date: " + dateStr + " - " + e.getMessage());
            return null;
        }
    }

    // Statistics by product sold
    @GetMapping("/report-products")
    public String reportProducts(HttpSession session, Model model,
                                 @RequestParam(value = "startDate", required = false) String startDateStr,
                                 @RequestParam(value = "endDate", required = false) String endDateStr,
                                 @RequestParam(value = "shopId", required = false) Long shopId) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        // Load shops for filter dropdown
        List<Shop> allShops = shopRepository.findAll();
        model.addAttribute("allShops", allShops);
        model.addAttribute("shopId", shopId);
        
        // Parse dates
        LocalDateTime startDate = parseDate(startDateStr, true);
        LocalDateTime endDate = parseDate(endDateStr, false);
        
        // Get report data with shop/date filters
        List<Object[]> reportData;
        if (shopId != null && startDate != null && endDate != null) {
            reportData = orderDetailRepository.getProductSalesStatisticsByShopAndDateRange(shopId, startDate, endDate);
        } else if (shopId != null) {
            reportData = orderDetailRepository.getProductSalesStatisticsByShop(shopId);
        } else if (startDate != null && endDate != null) {
            reportData = orderDetailRepository.getProductSalesStatisticsByDateRange(startDate, endDate);
        } else {
            reportData = orderDetailRepository.getProductSalesStatistics();
        }
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);
        
        model.addAttribute("reportTitle", "Thống kê theo sản phẩm");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "products");
        
        return "admin/revenue-statistics";
    }

    // Statistics by category sold
    @GetMapping("/report-categories")
    public String reportCategories(HttpSession session, Model model,
                                   @RequestParam(value = "startDate", required = false) String startDateStr,
                                   @RequestParam(value = "endDate", required = false) String endDateStr,
                                   @RequestParam(value = "shopId", required = false) Long shopId) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        List<Shop> allShops = shopRepository.findAll();
        model.addAttribute("allShops", allShops);
        model.addAttribute("shopId", shopId);
        
        // Parse dates
        LocalDateTime startDate = parseDate(startDateStr, true);
        LocalDateTime endDate = parseDate(endDateStr, false);
        
        // Get report data with shop/date filters
        List<Object[]> reportData;
        if (shopId != null && startDate != null && endDate != null) {
            reportData = orderDetailRepository.getCategorySalesStatisticsByShopAndDateRange(shopId, startDate, endDate);
        } else if (shopId != null) {
            reportData = orderDetailRepository.getCategorySalesStatisticsByShop(shopId);
        } else if (startDate != null && endDate != null) {
            reportData = orderDetailRepository.getCategorySalesStatisticsByDateRange(startDate, endDate);
        } else {
            reportData = orderDetailRepository.getCategorySalesStatistics();
        }
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);
        
        model.addAttribute("reportTitle", "Thống kê theo danh mục");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "categories");
        
        return "admin/revenue-statistics";
    }

    // Statistics of products sold by year
    @GetMapping("/report-years")
    public String reportYears(HttpSession session, Model model,
                             @RequestParam(value = "startDate", required = false) String startDateStr,
                             @RequestParam(value = "endDate", required = false) String endDateStr,
                             @RequestParam(value = "shopId", required = false) Long shopId) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        List<Shop> allShops = shopRepository.findAll();
        model.addAttribute("allShops", allShops);
        model.addAttribute("shopId", shopId);
        
        // Parse dates
        LocalDateTime startDate = parseDate(startDateStr, true);
        LocalDateTime endDate = parseDate(endDateStr, false);
        
        // Get report data with shop/date filters
        List<Object[]> reportData;
        if (shopId != null && startDate != null && endDate != null) {
            reportData = orderDetailRepository.getYearlySalesStatisticsByShopAndDateRange(shopId, startDate, endDate);
        } else if (shopId != null) {
            reportData = orderDetailRepository.getYearlySalesStatisticsByShop(shopId);
        } else if (startDate != null && endDate != null) {
            reportData = orderDetailRepository.getYearlySalesStatisticsByDateRange(startDate, endDate);
        } else {
            reportData = orderDetailRepository.getYearlySalesStatistics();
        }
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);
        
        model.addAttribute("reportTitle", "Thống kê theo năm");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "years");
        
        return "admin/revenue-statistics";
    }

    // Statistics of products sold by month
    @GetMapping("/report-months")
    public String reportMonths(HttpSession session, Model model,
                              @RequestParam(value = "startDate", required = false) String startDateStr,
                              @RequestParam(value = "endDate", required = false) String endDateStr,
                              @RequestParam(value = "shopId", required = false) Long shopId) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        List<Shop> allShops = shopRepository.findAll();
        model.addAttribute("allShops", allShops);
        model.addAttribute("shopId", shopId);
        
        // Parse dates
        LocalDateTime startDate = parseDate(startDateStr, true);
        LocalDateTime endDate = parseDate(endDateStr, false);
        
        // Get report data with shop/date filters
        List<Object[]> reportData;
        if (shopId != null && startDate != null && endDate != null) {
            reportData = orderDetailRepository.getMonthlySalesStatisticsByShopAndDateRange(shopId, startDate, endDate);
        } else if (shopId != null) {
            reportData = orderDetailRepository.getMonthlySalesStatisticsByShop(shopId);
        } else if (startDate != null && endDate != null) {
            reportData = orderDetailRepository.getMonthlySalesStatisticsByDateRange(startDate, endDate);
        } else {
            reportData = orderDetailRepository.getMonthlySalesStatistics();
        }
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);
        
        model.addAttribute("reportTitle", "Thống kê theo tháng");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "months");
        
        return "admin/revenue-statistics";
    }

    // Statistics of products sold by quarter
    @GetMapping("/report-quarters")
    public String reportQuarters(HttpSession session, Model model,
                                @RequestParam(value = "startDate", required = false) String startDateStr,
                                @RequestParam(value = "endDate", required = false) String endDateStr,
                                @RequestParam(value = "shopId", required = false) Long shopId) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        List<Shop> allShops = shopRepository.findAll();
        model.addAttribute("allShops", allShops);
        model.addAttribute("shopId", shopId);
        
        // Parse dates
        LocalDateTime startDate = parseDate(startDateStr, true);
        LocalDateTime endDate = parseDate(endDateStr, false);
        
        // Get report data with shop/date filters
        List<Object[]> reportData;
        if (shopId != null && startDate != null && endDate != null) {
            reportData = orderDetailRepository.getQuarterlySalesStatisticsByShopAndDateRange(shopId, startDate, endDate);
        } else if (shopId != null) {
            reportData = orderDetailRepository.getQuarterlySalesStatisticsByShop(shopId);
        } else if (startDate != null && endDate != null) {
            reportData = orderDetailRepository.getQuarterlySalesStatisticsByDateRange(startDate, endDate);
        } else {
            reportData = orderDetailRepository.getQuarterlySalesStatistics();
        }
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);
        
        model.addAttribute("reportTitle", "Thống kê theo quý");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "quarters");
        
        return "admin/revenue-statistics";
    }

    // Statistics by user
    @GetMapping("/report-users")
    public String reportUsers(HttpSession session, Model model,
                             @RequestParam(value = "startDate", required = false) String startDateStr,
                             @RequestParam(value = "endDate", required = false) String endDateStr,
                             @RequestParam(value = "shopId", required = false) Long shopId) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        List<Shop> allShops = shopRepository.findAll();
        model.addAttribute("allShops", allShops);
        model.addAttribute("shopId", shopId);
        
        // Parse dates
        LocalDateTime startDate = parseDate(startDateStr, true);
        LocalDateTime endDate = parseDate(endDateStr, false);
        
        // Get report data with shop/date filters
        List<Object[]> reportData;
        if (shopId != null && startDate != null && endDate != null) {
            reportData = orderDetailRepository.getUserStatisticsByShopAndDateRange(shopId, startDate, endDate);
        } else if (shopId != null) {
            reportData = orderDetailRepository.getUserStatisticsByShop(shopId);
        } else if (startDate != null && endDate != null) {
            reportData = orderDetailRepository.getUserStatisticsByDateRange(startDate, endDate);
        } else {
            reportData = orderDetailRepository.getUserStatistics();
        }
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);
        
        model.addAttribute("reportTitle", "Thống kê theo khách hàng");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "users");
        
        return "admin/revenue-statistics";
    }

    // Statistics by brand
    @GetMapping("/report-brands")
    public String reportBrands(HttpSession session, Model model,
                              @RequestParam(value = "startDate", required = false) String startDateStr,
                              @RequestParam(value = "endDate", required = false) String endDateStr,
                              @RequestParam(value = "shopId", required = false) Long shopId) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        List<Shop> allShops = shopRepository.findAll();
        model.addAttribute("allShops", allShops);
        model.addAttribute("shopId", shopId);
        
        // Parse dates
        LocalDateTime startDate = parseDate(startDateStr, true);
        LocalDateTime endDate = parseDate(endDateStr, false);
        
        // Get report data with shop/date filters
        List<Object[]> reportData;
        if (shopId != null && startDate != null && endDate != null) {
            reportData = orderDetailRepository.getBrandSalesStatisticsByShopAndDateRange(shopId, startDate, endDate);
        } else if (shopId != null) {
            reportData = orderDetailRepository.getBrandSalesStatisticsByShop(shopId);
        } else if (startDate != null && endDate != null) {
            reportData = orderDetailRepository.getBrandSalesStatisticsByDateRange(startDate, endDate);
        } else {
            reportData = orderDetailRepository.getBrandSalesStatistics();
        }
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);
        
        model.addAttribute("reportTitle", "Thống kê theo thương hiệu");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "brands");
        
        return "admin/revenue-statistics";
    }
}
