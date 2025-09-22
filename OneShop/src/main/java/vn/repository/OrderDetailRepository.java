package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.entity.OrderDetail;

import java.util.List;

/**
 * Repository for OrderDetail entity
 * @author OneShop Team
 */
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    @Query(value = "SELECT * FROM order_details WHERE order_id = ?1", nativeQuery = true)
    List<OrderDetail> findByOrderId(Long orderId);
    
    @Query(value = "SELECT * FROM order_details WHERE product_id = ?1", nativeQuery = true)
    List<OrderDetail> findByProductId(Long productId);
}
