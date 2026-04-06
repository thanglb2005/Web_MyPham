package vn.controller.admin.template.query;

import org.springframework.stereotype.Component;
import vn.controller.admin.template.AdminReportQuery;
import vn.repository.OrderDetailRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class MonthReportQuery implements AdminReportQuery {
    private final OrderDetailRepository repository;

    public MonthReportQuery(OrderDetailRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Object[]> fetchAll() {
        return repository.getMonthlySalesStatistics();
    }

    @Override
    public List<Object[]> fetchByShop(Long shopId) {
        return repository.getMonthlySalesStatisticsByShop(shopId);
    }

    @Override
    public List<Object[]> fetchByDateRange(LocalDateTime start, LocalDateTime end) {
        return repository.getMonthlySalesStatisticsByDateRange(start, end);
    }

    @Override
    public List<Object[]> fetchByShopAndDateRange(Long shopId, LocalDateTime start, LocalDateTime end) {
        return repository.getMonthlySalesStatisticsByShopAndDateRange(shopId, start, end);
    }
}
