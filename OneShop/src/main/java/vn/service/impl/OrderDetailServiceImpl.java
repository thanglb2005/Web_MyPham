package vn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.entity.OrderDetail;
import vn.repository.OrderDetailRepository;
import vn.service.OrderDetailService;

import java.util.List;

/**
 * Service implementation for OrderDetail management
 * @author OneShop Team
 */
@Service
public class OrderDetailServiceImpl implements OrderDetailService {
    
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    
    @Override
    public List<OrderDetail> findAll() {
        return orderDetailRepository.findAll();
    }
    
    @Override
    public OrderDetail save(OrderDetail orderDetail) {
        return orderDetailRepository.save(orderDetail);
    }
    
    @Override
    public void deleteById(Long id) {
        orderDetailRepository.deleteById(id);
    }
    
    @Override
    public List<OrderDetail> findByOrderId(Long orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }
    
    @Override
    public List<OrderDetail> findByProductId(Long productId) {
        return orderDetailRepository.findByProductId(productId);
    }
}
