package vn.service;

import vn.entity.OrderDetail;

import java.util.List;

public interface OrderDetailService {
    List<OrderDetail> findAll();
    OrderDetail save(OrderDetail orderDetail);
    void deleteById(Long id);
    List<OrderDetail> findByOrderId(Long orderId);
    List<OrderDetail> findByProductId(Long productId);
}