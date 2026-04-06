package vn.controller.admin.template.query;

import org.springframework.stereotype.Component;
import vn.controller.admin.template.AdminReportQuery;
import vn.repository.OrderDetailRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CategoryReportQuery implements AdminReportQuery {
    private final OrderDetailRepository repository;

    public CategoryReportQuery(OrderDetailRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Object[]> fetchAll() {
        return repository.getCategorySalesStatistics();
    }

    @Override
    public List<Object[]> fetchByShop(Long shopId) {
        return repository.getCategorySalesStatisticsByShop(shopId);
    }

    @Override
    public List<Object[]> fetchByDateRange(LocalDateTime start, LocalDateTime end) {
        return repository.getCategorySalesStatisticsByDateRange(start, end);
    }

    @Override
    public List<Object[]> fetchByShopAndDateRange(Long shopId, LocalDateTime start, LocalDateTime end) {
        return repository.getCategorySalesStatisticsByShopAndDateRange(shopId, start, end);
    }
}
