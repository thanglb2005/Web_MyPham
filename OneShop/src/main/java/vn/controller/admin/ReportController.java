package vn.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import vn.controller.admin.template.AbstractAdminRevenueReportController;
import vn.controller.admin.template.AdminReportQueries;

/**
 * Controller báo cáo thống kê doanh thu (admin).
 *
 * ---------- CODE CŨ (chưa Template Method) ----------
 * Mỗi {@code @GetMapping} (/report-products, /report-categories, …) lặp lại cùng một khung:
 * kiểm tra session, load allShops, parse start/end date, khối if/else 4 nhánh gọi repository,
 * {@code model.addAttribute}, return {@code admin/revenue-statistics}.
 *
 * ---------- CODE MỚI (Template Method) ----------
 * Khung thuật toán cố định nằm trong
 * {@link vn.controller.admin.template.AbstractAdminRevenueReportController#renderRevenueStatisticsReport};
 * phần khác nhau theo loại báo cáo là {@link vn.controller.admin.template.AdminReportQuery}
 * (tạo qua {@link AdminReportQueries}).
 * Hai method {@code reportProducts} và {@code reportCategories} có khối CODE CŨ comment đầy đủ để quay video;
 * các endpoint còn lại cùng khung — comment ngắn, tránh file quá dài.
 */
@Controller
@RequestMapping("/admin")
public class ReportController extends AbstractAdminRevenueReportController {

    @GetMapping("/report-products")
    public String reportProducts(HttpSession session, Model model,
                                 @RequestParam(value = "startDate", required = false) String startDateStr,
                                 @RequestParam(value = "endDate", required = false) String endDateStr,
                                 @RequestParam(value = "shopId", required = false) Long shopId) {
        // ===== CODE CŨ (đầy đủ — trùng lặp với các report khác trước khi refactor) =====
        // User user = (User) session.getAttribute("user");
        // if (user == null) {
        //     return "redirect:/login";
        // }
        // model.addAttribute("user", user);
        // List<Shop> allShops = shopRepository.findAll();
        // model.addAttribute("allShops", allShops);
        // model.addAttribute("shopId", shopId);
        // LocalDateTime startDate = parseDate(startDateStr, true);
        // LocalDateTime endDate = parseDate(endDateStr, false);
        // List<Object[]> reportData;
        // if (shopId != null && startDate != null && endDate != null) {
        //     reportData = orderDetailRepository.getProductSalesStatisticsByShopAndDateRange(shopId, startDate, endDate);
        // } else if (shopId != null) {
        //     reportData = orderDetailRepository.getProductSalesStatisticsByShop(shopId);
        // } else if (startDate != null && endDate != null) {
        //     reportData = orderDetailRepository.getProductSalesStatisticsByDateRange(startDate, endDate);
        // } else {
        //     reportData = orderDetailRepository.getProductSalesStatistics();
        // }
        // model.addAttribute("startDate", startDateStr);
        // model.addAttribute("endDate", endDateStr);
        // model.addAttribute("reportTitle", "Thống kê theo sản phẩm");
        // model.addAttribute("reportData", reportData);
        // model.addAttribute("reportType", "products");
        // return "admin/revenue-statistics";
        // ===== HẾT CODE CŨ =====

        return renderRevenueStatisticsReport(
                session, model, startDateStr, endDateStr, shopId,
                "Thống kê theo sản phẩm", "products",
                AdminReportQueries.products(orderDetailRepository));
    }

    @GetMapping("/report-categories")
    public String reportCategories(HttpSession session, Model model,
                                   @RequestParam(value = "startDate", required = false) String startDateStr,
                                   @RequestParam(value = "endDate", required = false) String endDateStr,
                                   @RequestParam(value = "shopId", required = false) Long shopId) {
        // ===== CODE CŨ (đầy đủ — cùng khung report-products, chỉ đổi nhóm getCategorySalesStatistics*) =====
        // User user = (User) session.getAttribute("user");
        // if (user == null) {
        //     return "redirect:/login";
        // }
        // model.addAttribute("user", user);
        // List<Shop> allShops = shopRepository.findAll();
        // model.addAttribute("allShops", allShops);
        // model.addAttribute("shopId", shopId);
        // LocalDateTime startDate = parseDate(startDateStr, true);
        // LocalDateTime endDate = parseDate(endDateStr, false);
        // List<Object[]> reportData;
        // if (shopId != null && startDate != null && endDate != null) {
        //     reportData = orderDetailRepository.getCategorySalesStatisticsByShopAndDateRange(shopId, startDate, endDate);
        // } else if (shopId != null) {
        //     reportData = orderDetailRepository.getCategorySalesStatisticsByShop(shopId);
        // } else if (startDate != null && endDate != null) {
        //     reportData = orderDetailRepository.getCategorySalesStatisticsByDateRange(startDate, endDate);
        // } else {
        //     reportData = orderDetailRepository.getCategorySalesStatistics();
        // }
        // model.addAttribute("startDate", startDateStr);
        // model.addAttribute("endDate", endDateStr);
        // model.addAttribute("reportTitle", "Thống kê theo danh mục");
        // model.addAttribute("reportData", reportData);
        // model.addAttribute("reportType", "categories");
        // return "admin/revenue-statistics";
        // ===== HẾT CODE CŨ =====

        return renderRevenueStatisticsReport(
                session, model, startDateStr, endDateStr, shopId,
                "Thống kê theo danh mục", "categories",
                AdminReportQueries.categories(orderDetailRepository));
    }

    @GetMapping("/report-years")
    public String reportYears(HttpSession session, Model model,
                             @RequestParam(value = "startDate", required = false) String startDateStr,
                             @RequestParam(value = "endDate", required = false) String endDateStr,
                             @RequestParam(value = "shopId", required = false) Long shopId) {
        // CODE CŨ: cùng khung như report-products
        return renderRevenueStatisticsReport(
                session, model, startDateStr, endDateStr, shopId,
                "Thống kê theo năm", "years",
                AdminReportQueries.years(orderDetailRepository));
    }

    @GetMapping("/report-months")
    public String reportMonths(HttpSession session, Model model,
                              @RequestParam(value = "startDate", required = false) String startDateStr,
                              @RequestParam(value = "endDate", required = false) String endDateStr,
                              @RequestParam(value = "shopId", required = false) Long shopId) {
        // CODE CŨ: cùng khung như report-products
        return renderRevenueStatisticsReport(
                session, model, startDateStr, endDateStr, shopId,
                "Thống kê theo tháng", "months",
                AdminReportQueries.months(orderDetailRepository));
    }

    @GetMapping("/report-quarters")
    public String reportQuarters(HttpSession session, Model model,
                                @RequestParam(value = "startDate", required = false) String startDateStr,
                                @RequestParam(value = "endDate", required = false) String endDateStr,
                                @RequestParam(value = "shopId", required = false) Long shopId) {
        // CODE CŨ: cùng khung như report-products
        return renderRevenueStatisticsReport(
                session, model, startDateStr, endDateStr, shopId,
                "Thống kê theo quý", "quarters",
                AdminReportQueries.quarters(orderDetailRepository));
    }

    @GetMapping("/report-users")
    public String reportUsers(HttpSession session, Model model,
                             @RequestParam(value = "startDate", required = false) String startDateStr,
                             @RequestParam(value = "endDate", required = false) String endDateStr,
                             @RequestParam(value = "shopId", required = false) Long shopId) {
        // CODE CŨ: cùng khung như report-products
        return renderRevenueStatisticsReport(
                session, model, startDateStr, endDateStr, shopId,
                "Thống kê theo khách hàng", "users",
                AdminReportQueries.users(orderDetailRepository));
    }

    @GetMapping("/report-brands")
    public String reportBrands(HttpSession session, Model model,
                              @RequestParam(value = "startDate", required = false) String startDateStr,
                              @RequestParam(value = "endDate", required = false) String endDateStr,
                              @RequestParam(value = "shopId", required = false) Long shopId) {
        // CODE CŨ: cùng khung như report-products
        return renderRevenueStatisticsReport(
                session, model, startDateStr, endDateStr, shopId,
                "Thống kê theo thương hiệu", "brands",
                AdminReportQueries.brands(orderDetailRepository));
    }
}
