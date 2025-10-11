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
import vn.entity.User;
import vn.repository.OrderDetailRepository;

/**
 * Controller for statistics reports with date filtering support
 */
@Controller
@RequestMapping("/admin")
public class ReportController {

    @Autowired
    private OrderDetailRepository orderDetailRepository;
    
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
                                 @RequestParam(value = "endDate", required = false) String endDateStr) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        
        // Parse dates
        LocalDateTime startDate = parseDate(startDateStr, true);
        LocalDateTime endDate = parseDate(endDateStr, false);
        
        // Get report data with or without date filter
        List<Object[]> reportData;
        if (startDate != null && endDate != null) {
            reportData = orderDetailRepository.getProductSalesStatisticsByDateRange(startDate, endDate);
            model.addAttribute("startDate", startDateStr);
            model.addAttribute("endDate", endDateStr);
        } else {
            reportData = orderDetailRepository.getProductSalesStatistics();
        }
        
        model.addAttribute("reportTitle", "Thống kê theo sản phẩm");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "products");
        
        return "admin/revenue-statistics";
    }

    // Statistics by category sold
    @GetMapping("/report-categories")
    public String reportCategories(HttpSession session, Model model,
                                   @RequestParam(value = "startDate", required = false) String startDateStr,
                                   @RequestParam(value = "endDate", required = false) String endDateStr) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        
        // Parse dates
        LocalDateTime startDate = parseDate(startDateStr, true);
        LocalDateTime endDate = parseDate(endDateStr, false);
        
        // Get report data with or without date filter
        List<Object[]> reportData;
        if (startDate != null && endDate != null) {
            reportData = orderDetailRepository.getCategorySalesStatisticsByDateRange(startDate, endDate);
            model.addAttribute("startDate", startDateStr);
            model.addAttribute("endDate", endDateStr);
        } else {
            reportData = orderDetailRepository.getCategorySalesStatistics();
        }
        
        model.addAttribute("reportTitle", "Thống kê theo danh mục");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "categories");
        
        return "admin/revenue-statistics";
    }

    // Statistics of products sold by year
    @GetMapping("/report-years")
    public String reportYears(HttpSession session, Model model,
                             @RequestParam(value = "startDate", required = false) String startDateStr,
                             @RequestParam(value = "endDate", required = false) String endDateStr) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        
        // Parse dates
        LocalDateTime startDate = parseDate(startDateStr, true);
        LocalDateTime endDate = parseDate(endDateStr, false);
        
        // Get report data with or without date filter
        List<Object[]> reportData;
        if (startDate != null && endDate != null) {
            reportData = orderDetailRepository.getYearlySalesStatisticsByDateRange(startDate, endDate);
            model.addAttribute("startDate", startDateStr);
            model.addAttribute("endDate", endDateStr);
        } else {
            reportData = orderDetailRepository.getYearlySalesStatistics();
        }
        
        model.addAttribute("reportTitle", "Thống kê theo năm");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "years");
        
        return "admin/revenue-statistics";
    }

    // Statistics of products sold by month
    @GetMapping("/report-months")
    public String reportMonths(HttpSession session, Model model,
                              @RequestParam(value = "startDate", required = false) String startDateStr,
                              @RequestParam(value = "endDate", required = false) String endDateStr) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        
        // Parse dates
        LocalDateTime startDate = parseDate(startDateStr, true);
        LocalDateTime endDate = parseDate(endDateStr, false);
        
        // Get report data with or without date filter
        List<Object[]> reportData;
        if (startDate != null && endDate != null) {
            reportData = orderDetailRepository.getMonthlySalesStatisticsByDateRange(startDate, endDate);
            model.addAttribute("startDate", startDateStr);
            model.addAttribute("endDate", endDateStr);
        } else {
            reportData = orderDetailRepository.getMonthlySalesStatistics();
        }
        
        model.addAttribute("reportTitle", "Thống kê theo tháng");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "months");
        
        return "admin/revenue-statistics";
    }

    // Statistics of products sold by quarter
    @GetMapping("/report-quarters")
    public String reportQuarters(HttpSession session, Model model,
                                @RequestParam(value = "startDate", required = false) String startDateStr,
                                @RequestParam(value = "endDate", required = false) String endDateStr) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        
        // Parse dates
        LocalDateTime startDate = parseDate(startDateStr, true);
        LocalDateTime endDate = parseDate(endDateStr, false);
        
        // Get report data with or without date filter
        List<Object[]> reportData;
        if (startDate != null && endDate != null) {
            reportData = orderDetailRepository.getQuarterlySalesStatisticsByDateRange(startDate, endDate);
            model.addAttribute("startDate", startDateStr);
            model.addAttribute("endDate", endDateStr);
        } else {
            reportData = orderDetailRepository.getQuarterlySalesStatistics();
        }
        
        model.addAttribute("reportTitle", "Thống kê theo quý");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "quarters");
        
        return "admin/revenue-statistics";
    }

    // Statistics by user
    @GetMapping("/report-users")
    public String reportUsers(HttpSession session, Model model,
                             @RequestParam(value = "startDate", required = false) String startDateStr,
                             @RequestParam(value = "endDate", required = false) String endDateStr) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        
        // Parse dates
        LocalDateTime startDate = parseDate(startDateStr, true);
        LocalDateTime endDate = parseDate(endDateStr, false);
        
        // Get report data with or without date filter
        List<Object[]> reportData;
        if (startDate != null && endDate != null) {
            reportData = orderDetailRepository.getUserStatisticsByDateRange(startDate, endDate);
            model.addAttribute("startDate", startDateStr);
            model.addAttribute("endDate", endDateStr);
        } else {
            reportData = orderDetailRepository.getUserStatistics();
        }
        
        model.addAttribute("reportTitle", "Thống kê theo khách hàng");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "users");
        
        return "admin/revenue-statistics";
    }

    // Statistics by brand
    @GetMapping("/report-brands")
    public String reportBrands(HttpSession session, Model model,
                              @RequestParam(value = "startDate", required = false) String startDateStr,
                              @RequestParam(value = "endDate", required = false) String endDateStr) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        
        // Parse dates
        LocalDateTime startDate = parseDate(startDateStr, true);
        LocalDateTime endDate = parseDate(endDateStr, false);
        
        // Get report data with or without date filter
        List<Object[]> reportData;
        if (startDate != null && endDate != null) {
            reportData = orderDetailRepository.getBrandSalesStatisticsByDateRange(startDate, endDate);
            model.addAttribute("startDate", startDateStr);
            model.addAttribute("endDate", endDateStr);
        } else {
            reportData = orderDetailRepository.getBrandSalesStatistics();
        }
        
        model.addAttribute("reportTitle", "Thống kê theo thương hiệu");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "brands");
        
        return "admin/revenue-statistics";
    }
}
