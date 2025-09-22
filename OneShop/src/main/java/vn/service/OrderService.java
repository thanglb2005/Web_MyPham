package vn.service;

import vn.entity.Order;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Order management
 * @author OneShop Team
 */
public interface OrderService {
    
    List<Order> findAll();
    
    Optional<Order> findById(Long id);
    
    Order save(Order order);
    
    void deleteById(Long id);
    
    List<Order> findOrderByUserId(Long userId);
    
    List<Order> findAllOrderByOrderDateDesc();
    
    List<Order> findByStatus(Integer status);
    
    Order updateOrderStatus(Long orderId, Integer status);
}
