package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.entity.ShopSupportAssignment;

import java.util.Optional;

@Repository
public interface ShopSupportAssignmentRepository extends JpaRepository<ShopSupportAssignment, Long> {
    Optional<ShopSupportAssignment> findByShop_ShopId(Long shopId);
}

