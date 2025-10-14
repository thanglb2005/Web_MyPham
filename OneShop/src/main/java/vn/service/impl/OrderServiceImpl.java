package vn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.entity.CartItem;
import vn.entity.Order;
import vn.entity.OrderDetail;
import vn.entity.User;
import vn.repository.OrderDetailRepository;
import vn.repository.OrderRepository;
import vn.repository.ProductRepository;
import vn.service.OrderService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public Order createOrder(User user, String customerName, String customerEmail, String customerPhone,
                             String shippingAddress, String note, Order.PaymentMethod paymentMethod,
                             Map<Long, CartItem> cartItems) {

        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty to create an order.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setCustomerName(customerName);
        order.setCustomerEmail(customerEmail);
        order.setCustomerPhone(customerPhone);
        order.setShippingAddress(shippingAddress);
        order.setNote(note);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());

        double totalAmount = cartItems.values().stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItem cartItem : cartItems.values()) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(savedOrder);
            orderDetail.setProduct(productRepository.findById(cartItem.getId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + cartItem.getId())));
            orderDetail.setProductName(cartItem.getName());
            orderDetail.setUnitPrice(cartItem.getUnitPrice());
            orderDetail.setQuantity(cartItem.getQuantity());
            orderDetail.setTotalPrice(cartItem.getTotalPrice());
            orderDetails.add(orderDetail);
        }
        orderDetailRepository.saveAll(orderDetails);

        savedOrder.setOrderDetails(orderDetails);
        return savedOrder;
    }
    
    @Override
    @Transactional
    public Order createOrder(User user, String customerName, String customerEmail, String customerPhone,
                             String shippingAddress, String note, Order.PaymentMethod paymentMethod,
                             Map<Long, CartItem> cartItems, String promotionCode, Double discountAmount) {

        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty to create an order.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setCustomerName(customerName);
        order.setCustomerEmail(customerEmail);
        order.setCustomerPhone(customerPhone);
        order.setShippingAddress(shippingAddress);
        order.setNote(note);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());

        double totalAmount = cartItems.values().stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
        
        // Áp dụng khuyến mãi nếu có
        if (promotionCode != null && discountAmount != null && discountAmount > 0) {
            totalAmount = Math.max(0, totalAmount - discountAmount);
            order.setNote(note + (note != null && !note.isEmpty() ? "\n" : "") + 
                         "Mã khuyến mãi: " + promotionCode + " - Giảm: " + discountAmount + " VNĐ");
        }
        
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItem cartItem : cartItems.values()) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(savedOrder);
            orderDetail.setProduct(productRepository.findById(cartItem.getId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + cartItem.getId())));
            orderDetail.setProductName(cartItem.getName());
            orderDetail.setUnitPrice(cartItem.getUnitPrice());
            orderDetail.setQuantity(cartItem.getQuantity());
            orderDetail.setTotalPrice(cartItem.getTotalPrice());
            orderDetails.add(orderDetail);
        }
        orderDetailRepository.saveAll(orderDetails);

        savedOrder.setOrderDetails(orderDetails);
        return savedOrder;
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    @Override
    public Collection<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        orderOptional.ifPresent(order -> {
            order.setStatus(newStatus);
            if (newStatus == Order.OrderStatus.SHIPPING && order.getShippedDate() == null) {
                order.setShippedDate(LocalDateTime.now());
            } else if (newStatus == Order.OrderStatus.DELIVERED && order.getDeliveredDate() == null) {
                order.setDeliveredDate(LocalDateTime.now());
            }
            orderRepository.save(order);
        });
    }

    @Override
    @Transactional
    public void assignShipper(Long orderId, User shipper) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        orderOptional.ifPresent(order -> {
            order.setShipper(shipper);
            orderRepository.save(order);
        });
    }

    // ===== VENDOR ORDER MANAGEMENT METHODS =====

    @Override
    public Page<Order> findByShopIdInAndStatus(List<Long> shopIds, Order.OrderStatus status, Pageable pageable) {
        // Tuân thủ logic cũ: lọc theo shop thông qua orderDetails -> product -> shop
        return orderRepository.findByShopIdInAndStatus(shopIds, status, pageable);
    }

    @Override
    public Page<Order> findByShopIdInAndOrderIdContaining(List<Long> shopIds, String search, Pageable pageable) {
        // Tuân thủ logic cũ
        return orderRepository.findByShopIdInAndOrderIdContaining(shopIds, search, pageable);
    }

    @Override
    public Page<Order> findByShopIdInAndStatusAndOrderIdContaining(List<Long> shopIds, Order.OrderStatus status, String search, Pageable pageable) {
        // Tuân thủ logic cũ
        return orderRepository.findByShopIdInAndStatusAndOrderIdContaining(shopIds, status, search, pageable);
    }

    @Override
    public Page<Order> findByShopIdIn(List<Long> shopIds, Pageable pageable) {
        // Tuân thủ logic cũ
        return orderRepository.findByShopIdIn(shopIds, pageable);
    }

    @Override
    public Page<Order> findByShopIdInAndStatusDirect(List<Long> shopIds, Order.OrderStatus status, Pageable pageable) {
        return orderRepository.findByShopIdInAndStatusDirect(shopIds, status, pageable);
    }

    @Override
    public Page<Order> findByShopIdInDirect(List<Long> shopIds, Pageable pageable) {
        return orderRepository.findByShopIdInDirect(shopIds, pageable);
    }

    @Override
    public Optional<Order> findByIdAndShopIdIn(Long orderId, List<Long> shopIds) {
        // Tuân thủ logic cũ
        return orderRepository.findByIdAndShopIdIn(orderId, shopIds);
    }

    @Override
    public Long countByShopIdInAndStatus(List<Long> shopIds, Order.OrderStatus status) {
        // Tuân thủ logic cũ
        return orderRepository.countByShopIdInAndStatus(shopIds, status);
    }

    @Override
    public Long countByShopIdIn(List<Long> shopIds) {
        // Tuân thủ logic cũ
        return orderRepository.countByShopIdIn(shopIds);
    }
}
