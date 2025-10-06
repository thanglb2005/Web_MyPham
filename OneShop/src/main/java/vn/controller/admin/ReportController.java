package vn.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import vn.entity.User;
import vn.repository.OrderDetailRepository;

/**
 * Controller for statistics reports
 */
@Controller
@RequestMapping("/admin")
public class ReportController {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    // Statistics by product sold
    @GetMapping("/report-products")
    public String reportProducts(HttpSession session, Model model) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        List<Object[]> reportData = orderDetailRepository.getProductSalesStatistics();
        model.addAttribute("reportTitle", "Thống kê theo sản phẩm");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "products");
        
        return "admin/revenue-statistics";
    }

    // Statistics by category sold
    @GetMapping("/report-categories")
    public String reportCategories(HttpSession session, Model model) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        List<Object[]> reportData = orderDetailRepository.getCategorySalesStatistics();
        model.addAttribute("reportTitle", "Thống kê theo danh mục");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "categories");
        
        return "admin/revenue-statistics";
    }

    // Statistics of products sold by year
    @GetMapping("/report-years")
    public String reportYears(HttpSession session, Model model) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        List<Object[]> reportData = orderDetailRepository.getYearlySalesStatistics();
        model.addAttribute("reportTitle", "Thống kê theo năm");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "years");
        
        return "admin/revenue-statistics";
    }

    // Statistics of products sold by month
    @GetMapping("/report-months")
    public String reportMonths(HttpSession session, Model model) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        List<Object[]> reportData = orderDetailRepository.getMonthlySalesStatistics();
        model.addAttribute("reportTitle", "Thống kê theo tháng");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "months");
        
        return "admin/revenue-statistics";
    }

    // Statistics of products sold by quarter
    @GetMapping("/report-quarters")
    public String reportQuarters(HttpSession session, Model model) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        List<Object[]> reportData = orderDetailRepository.getQuarterlySalesStatistics();
        model.addAttribute("reportTitle", "Thống kê theo quý");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "quarters");
        
        return "admin/revenue-statistics";
    }

    // Statistics by user
    @GetMapping("/report-users")
    public String reportUsers(HttpSession session, Model model) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        List<Object[]> reportData = orderDetailRepository.getUserStatistics();
        model.addAttribute("reportTitle", "Thống kê theo khách hàng");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "users");
        
        return "admin/revenue-statistics";
    }

    // Statistics by brand
    @GetMapping("/report-brands")
    public String reportBrands(HttpSession session, Model model) {
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        List<Object[]> reportData = orderDetailRepository.getBrandSalesStatistics();
        model.addAttribute("reportTitle", "Thống kê theo thương hiệu");
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "brands");
        
        return "admin/revenue-statistics";
    }
}