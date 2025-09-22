package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.entity.Order;

import java.util.List;

/**
 * Repository for Order entity
 * @author OneShop Team
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = "SELECT * FROM orders WHERE user_id = ?1", nativeQuery = true)
    List<Order> findOrderByUserId(Long userId);
    
    @Query(value = "SELECT * FROM orders ORDER BY order_date DESC", nativeQuery = true)
    List<Order> findAllOrderByOrderDateDesc();
    
    @Query(value = "SELECT * FROM orders WHERE status = ?1", nativeQuery = true)
    List<Order> findByStatus(Integer status);
}
