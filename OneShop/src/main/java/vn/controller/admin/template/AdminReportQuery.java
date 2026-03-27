package vn.controller.admin.template;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Hook của Template Method {@link AbstractAdminRevenueReportController#renderRevenueStatisticsReport}:
 * mỗi loại báo cáo chỉ khác bộ hàm gọi repository theo cùng tổ hợp filter (shop / khoảng ngày).
 */
public interface AdminReportQuery {

    List<Object[]> fetchAll();

    List<Object[]> fetchByShop(Long shopId);

    List<Object[]> fetchByDateRange(LocalDateTime start, LocalDateTime end);

    List<Object[]> fetchByShopAndDateRange(Long shopId, LocalDateTime start, LocalDateTime end);
}
