package vn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.entity.Order;
import vn.repository.OrderRepository;
import vn.service.OrderService;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Order management
 * @author OneShop Team
 */
@Service
public class OrderServiceImpl implements OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }
    
    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }
    
    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }
    
    @Override
    public void deleteById(Long id) {
        orderRepository.deleteById(id);
    }
    
    @Override
    public List<Order> findOrderByUserId(Long userId) {
        return orderRepository.findOrderByUserId(userId);
    }
    
    @Override
    public List<Order> findAllOrderByOrderDateDesc() {
        return orderRepository.findAllOrderByOrderDateDesc();
    }
    
    @Override
    public List<Order> findByStatus(Integer status) {
        return orderRepository.findByStatus(status);
    }
    
    @Override
    public Order updateOrderStatus(Long orderId, Integer status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            return orderRepository.save(order);
        }
        return null;
    }
}
