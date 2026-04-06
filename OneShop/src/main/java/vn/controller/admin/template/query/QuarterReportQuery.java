package vn.controller.admin.template.query;

import org.springframework.stereotype.Component;
import vn.controller.admin.template.AdminReportQuery;
import vn.repository.OrderDetailRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class QuarterReportQuery implements AdminReportQuery {
    private final OrderDetailRepository repository;

    public QuarterReportQuery(OrderDetailRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Object[]> fetchAll() {
        return repository.getQuarterlySalesStatistics();
    }

    @Override
    public List<Object[]> fetchByShop(Long shopId) {
        return repository.getQuarterlySalesStatisticsByShop(shopId);
    }

    @Override
    public List<Object[]> fetchByDateRange(LocalDateTime start, LocalDateTime end) {
        return repository.getQuarterlySalesStatisticsByDateRange(start, end);
    }

    @Override
    public List<Object[]> fetchByShopAndDateRange(Long shopId, LocalDateTime start, LocalDateTime end) {
        return repository.getQuarterlySalesStatisticsByShopAndDateRange(shopId, start, end);
    }
}
