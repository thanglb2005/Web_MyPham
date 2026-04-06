package vn.controller.admin.template.query;

import org.springframework.stereotype.Component;
import vn.controller.admin.template.AdminReportQuery;
import vn.repository.OrderDetailRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ProductReportQuery implements AdminReportQuery {
    private final OrderDetailRepository repository;

    public ProductReportQuery(OrderDetailRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Object[]> fetchAll() {
        return repository.getProductSalesStatistics();
    }

    @Override
    public List<Object[]> fetchByShop(Long shopId) {
        return repository.getProductSalesStatisticsByShop(shopId);
    }

    @Override
    public List<Object[]> fetchByDateRange(LocalDateTime start, LocalDateTime end) {
        return repository.getProductSalesStatisticsByDateRange(start, end);
    }

    @Override
    public List<Object[]> fetchByShopAndDateRange(Long shopId, LocalDateTime start, LocalDateTime end) {
        return repository.getProductSalesStatisticsByShopAndDateRange(shopId, start, end);
    }
}
