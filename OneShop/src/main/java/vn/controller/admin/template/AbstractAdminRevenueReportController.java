package vn.controller.admin.template;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpSession;
import vn.entity.Shop;
import vn.entity.User;
import vn.repository.OrderDetailRepository;
import vn.repository.ShopRepository;

/**
 * Template Method pattern: cố định khung xử lý báo cáo doanh thu admin (auth, filter shop, parse ngày,
 * chọn nhánh query, gán model, view). Phần thay đổi được đưa vào {@link AdminReportQuery} (primitive hook).
 * <p>
 * Trước refactor, toàn bộ khung này bị copy trong từng {@code @GetMapping} của
 * {@link vn.controller.admin.ReportController}; code cũ được giữ trong comment tại các method đó để đối chiếu.
 */
public abstract class AbstractAdminRevenueReportController {

    private static final String VIEW_REVENUE_STATISTICS = "admin/revenue-statistics";

    @Autowired
    protected OrderDetailRepository orderDetailRepository;

    @Autowired
    protected ShopRepository shopRepository;

    protected LocalDateTime parseDate(String dateStr, boolean isStartOfDay) {
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

    /**
     * Template method: thuật toán cố định; bước lấy dữ liệu ủy quyền cho {@code query}.
     */
    protected final String renderRevenueStatisticsReport(
            HttpSession session,
            Model model,
            String startDateStr,
            String endDateStr,
            Long shopId,
            String reportTitle,
            String reportType,
            AdminReportQuery query) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        List<Shop> allShops = shopRepository.findAll();
        model.addAttribute("allShops", allShops);
        model.addAttribute("shopId", shopId);

        LocalDateTime startDate = parseDate(startDateStr, true);
        LocalDateTime endDate = parseDate(endDateStr, false);

        List<Object[]> reportData;
        if (shopId != null && startDate != null && endDate != null) {
            reportData = query.fetchByShopAndDateRange(shopId, startDate, endDate);
        } else if (shopId != null) {
            reportData = query.fetchByShop(shopId);
        } else if (startDate != null && endDate != null) {
            reportData = query.fetchByDateRange(startDate, endDate);
        } else {
            reportData = query.fetchAll();
        }

        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);
        model.addAttribute("reportTitle", reportTitle);
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", reportType);

        return VIEW_REVENUE_STATISTICS;
    }
}
