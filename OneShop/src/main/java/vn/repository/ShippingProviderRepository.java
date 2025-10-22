package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.entity.ShippingProvider;

import java.util.List;

@Repository
public interface ShippingProviderRepository extends JpaRepository<ShippingProvider, Long> {

    /**
     * Tìm tất cả đơn vị vận chuyển đang hoạt động
     */
    @Query("SELECT sp FROM ShippingProvider sp WHERE sp.status = true ORDER BY sp.providerName")
    List<ShippingProvider> findAllActive();

    /**
     * Tìm theo tên
     */
    List<ShippingProvider> findByProviderNameContainingIgnoreCase(String name);
}

