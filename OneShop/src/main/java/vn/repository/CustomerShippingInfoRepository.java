package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.entity.CustomerShippingInfo;

import java.util.List;
import java.util.Optional;

public interface CustomerShippingInfoRepository extends JpaRepository<CustomerShippingInfo, Long> {
    List<CustomerShippingInfo> findByUserUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);
    Optional<CustomerShippingInfo> findByShippingInfoIdAndUserUserId(Long shippingInfoId, Long userId);
}
