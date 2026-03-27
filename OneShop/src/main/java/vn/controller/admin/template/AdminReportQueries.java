package vn.controller.admin.template;

import java.time.LocalDateTime;
import java.util.List;

import vn.repository.OrderDetailRepository;

/**
 * Factory tĩnh cho từng {@link AdminReportQuery} — gom mapping repository theo loại báo cáo.
 * <p>
 * (Báo cáo video) Mỗi method static tương ứng một URL cũ trong {@code ReportController};
 * bên trong là bốn lời gọi {@link OrderDetailRepository} giống khối if/else đã comment trong controller.
 */
public final class AdminReportQueries {

    private AdminReportQueries() {
    }

    public static AdminReportQuery products(OrderDetailRepository r) {
        return new AdminReportQuery() {
            @Override
            public List<Object[]> fetchAll() {
                return r.getProductSalesStatistics();
            }

            @Override
            public List<Object[]> fetchByShop(Long shopId) {
                return r.getProductSalesStatisticsByShop(shopId);
            }

            @Override
            public List<Object[]> fetchByDateRange(LocalDateTime start, LocalDateTime end) {
                return r.getProductSalesStatisticsByDateRange(start, end);
            }

            @Override
            public List<Object[]> fetchByShopAndDateRange(Long shopId, LocalDateTime start, LocalDateTime end) {
                return r.getProductSalesStatisticsByShopAndDateRange(shopId, start, end);
            }
        };
    }

    public static AdminReportQuery categories(OrderDetailRepository r) {
        return new AdminReportQuery() {
            @Override
            public List<Object[]> fetchAll() {
                return r.getCategorySalesStatistics();
            }

            @Override
            public List<Object[]> fetchByShop(Long shopId) {
                return r.getCategorySalesStatisticsByShop(shopId);
            }

            @Override
            public List<Object[]> fetchByDateRange(LocalDateTime start, LocalDateTime end) {
                return r.getCategorySalesStatisticsByDateRange(start, end);
            }

            @Override
            public List<Object[]> fetchByShopAndDateRange(Long shopId, LocalDateTime start, LocalDateTime end) {
                return r.getCategorySalesStatisticsByShopAndDateRange(shopId, start, end);
            }
        };
    }

    public static AdminReportQuery years(OrderDetailRepository r) {
        return new AdminReportQuery() {
            @Override
            public List<Object[]> fetchAll() {
                return r.getYearlySalesStatistics();
            }

            @Override
            public List<Object[]> fetchByShop(Long shopId) {
                return r.getYearlySalesStatisticsByShop(shopId);
            }

            @Override
            public List<Object[]> fetchByDateRange(LocalDateTime start, LocalDateTime end) {
                return r.getYearlySalesStatisticsByDateRange(start, end);
            }

            @Override
            public List<Object[]> fetchByShopAndDateRange(Long shopId, LocalDateTime start, LocalDateTime end) {
                return r.getYearlySalesStatisticsByShopAndDateRange(shopId, start, end);
            }
        };
    }

    public static AdminReportQuery months(OrderDetailRepository r) {
        return new AdminReportQuery() {
            @Override
            public List<Object[]> fetchAll() {
                return r.getMonthlySalesStatistics();
            }

            @Override
            public List<Object[]> fetchByShop(Long shopId) {
                return r.getMonthlySalesStatisticsByShop(shopId);
            }

            @Override
            public List<Object[]> fetchByDateRange(LocalDateTime start, LocalDateTime end) {
                return r.getMonthlySalesStatisticsByDateRange(start, end);
            }

            @Override
            public List<Object[]> fetchByShopAndDateRange(Long shopId, LocalDateTime start, LocalDateTime end) {
                return r.getMonthlySalesStatisticsByShopAndDateRange(shopId, start, end);
            }
        };
    }

    public static AdminReportQuery quarters(OrderDetailRepository r) {
        return new AdminReportQuery() {
            @Override
            public List<Object[]> fetchAll() {
                return r.getQuarterlySalesStatistics();
            }

            @Override
            public List<Object[]> fetchByShop(Long shopId) {
                return r.getQuarterlySalesStatisticsByShop(shopId);
            }

            @Override
            public List<Object[]> fetchByDateRange(LocalDateTime start, LocalDateTime end) {
                return r.getQuarterlySalesStatisticsByDateRange(start, end);
            }

            @Override
            public List<Object[]> fetchByShopAndDateRange(Long shopId, LocalDateTime start, LocalDateTime end) {
                return r.getQuarterlySalesStatisticsByShopAndDateRange(shopId, start, end);
            }
        };
    }

    public static AdminReportQuery users(OrderDetailRepository r) {
        return new AdminReportQuery() {
            @Override
            public List<Object[]> fetchAll() {
                return r.getUserStatistics();
            }

            @Override
            public List<Object[]> fetchByShop(Long shopId) {
                return r.getUserStatisticsByShop(shopId);
            }

            @Override
            public List<Object[]> fetchByDateRange(LocalDateTime start, LocalDateTime end) {
                return r.getUserStatisticsByDateRange(start, end);
            }

            @Override
            public List<Object[]> fetchByShopAndDateRange(Long shopId, LocalDateTime start, LocalDateTime end) {
                return r.getUserStatisticsByShopAndDateRange(shopId, start, end);
            }
        };
    }

    public static AdminReportQuery brands(OrderDetailRepository r) {
        return new AdminReportQuery() {
            @Override
            public List<Object[]> fetchAll() {
                return r.getBrandSalesStatistics();
            }

            @Override
            public List<Object[]> fetchByShop(Long shopId) {
                return r.getBrandSalesStatisticsByShop(shopId);
            }

            @Override
            public List<Object[]> fetchByDateRange(LocalDateTime start, LocalDateTime end) {
                return r.getBrandSalesStatisticsByDateRange(start, end);
            }

            @Override
            public List<Object[]> fetchByShopAndDateRange(Long shopId, LocalDateTime start, LocalDateTime end) {
                return r.getBrandSalesStatisticsByShopAndDateRange(shopId, start, end);
            }
        };
    }
}
