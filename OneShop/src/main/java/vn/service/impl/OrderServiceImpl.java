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
import vn.service.OneXuService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private OneXuService oneXuService;

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
        order.setPaymentPaid(false); // Set payment status to false initially

        // Set shop_id from first product in cart items
        if (!cartItems.isEmpty()) {
            CartItem firstItem = cartItems.values().iterator().next();
            if (firstItem.getProduct() != null && firstItem.getProduct().getShop() != null) {
                order.setShop(firstItem.getProduct().getShop());
                System.out.println("Order shop set to: " + firstItem.getProduct().getShop().getShopName());
            }
        }

        // Debug logging for order calculation
        System.out.println("=== Order Calculation Debug ===");
        double totalAmount = 0;
        for (CartItem item : cartItems.values()) {
            double itemTotal = item.getQuantity() * item.getUnitPrice();
            totalAmount += itemTotal;
            System.out.println("Order Item: " + item.getName() + 
                " | Qty: " + item.getQuantity() + 
                " | Unit Price: " + item.getUnitPrice() + 
                " | Total: " + itemTotal);
        }
        System.out.println("Final Order Total Amount: " + totalAmount);
        System.out.println("=============================");
        
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItem cartItem : cartItems.values()) {
            try {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrder(savedOrder);
                
                // Debug logging
                System.out.println("Processing cart item: ID=" + cartItem.getId() + ", Name=" + cartItem.getName());
                
                orderDetail.setProduct(productRepository.findById(cartItem.getId())
                        .orElseThrow(() -> new RuntimeException("Product not found: " + cartItem.getId())));
                orderDetail.setProductName(cartItem.getName());
                orderDetail.setUnitPrice(cartItem.getUnitPrice());
                orderDetail.setQuantity(cartItem.getQuantity());
                orderDetail.setTotalPrice(cartItem.getTotalPrice());
                orderDetails.add(orderDetail);
            } catch (Exception e) {
                System.err.println("Error processing cart item " + cartItem.getId() + ": " + e.getMessage());
                throw e;
            }
        }
        orderDetailRepository.saveAll(orderDetails);

        savedOrder.setOrderDetails(orderDetails);
        return savedOrder;
    }
    
    @Override
    @Transactional
    public Order createOrder(User user, String customerName, String customerEmail, String customerPhone,
                             String shippingAddress, String note, Order.PaymentMethod paymentMethod,
                             Map<Long, CartItem> cartItems, String promotionCode, Double discountAmount, 
                             Double shippingFee) {

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

        // Set shop_id from first product in cart items
        if (!cartItems.isEmpty()) {
            CartItem firstItem = cartItems.values().iterator().next();
            if (firstItem.getProduct() != null && firstItem.getProduct().getShop() != null) {
                order.setShop(firstItem.getProduct().getShop());
                System.out.println("Order shop set to: " + firstItem.getProduct().getShop().getShopName());
            }
        }

        double originalAmount = cartItems.values().stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
        
        // Set original total amount first
        order.setTotalAmount(originalAmount);
        
        // Set shipping fee
        order.setShippingFee(shippingFee != null ? shippingFee : 0.0);
        
        // Áp dụng khuyến mãi nếu có
        if (promotionCode != null && discountAmount != null && discountAmount > 0) {
            order.setDiscountAmount(discountAmount);
            double productAmountAfterDiscount = Math.max(0, originalAmount - discountAmount);
            double finalAmount = productAmountAfterDiscount + (shippingFee != null ? shippingFee : 0.0);
            order.setFinalAmount(finalAmount);
            order.setNote(note + (note != null && !note.isEmpty() ? "\n" : "") + 
                         "Mã khuyến mãi: " + promotionCode + " - Giảm: " + discountAmount + " VNĐ");
        } else {
            order.setDiscountAmount(0.0);
            double finalAmount = originalAmount + (shippingFee != null ? shippingFee : 0.0);
            order.setFinalAmount(finalAmount);
        }

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
            Order.OrderStatus oldStatus = order.getStatus();
            order.setStatus(newStatus);
            
            if (newStatus == Order.OrderStatus.SHIPPING && order.getShippedDate() == null) {
                order.setShippedDate(LocalDateTime.now());
            } else if (newStatus == Order.OrderStatus.DELIVERED && order.getDeliveredDate() == null) {
                order.setDeliveredDate(LocalDateTime.now());
                
                // Thưởng One Xu khi đơn hàng được giao thành công (1% giá trị đơn hàng)
                if (oldStatus != Order.OrderStatus.DELIVERED) {
                    try {
                        oneXuService.rewardFromOrder(order.getUser().getUserId(), orderId, order.getTotalAmount());
                    } catch (Exception e) {
                        // Log error but don't fail the order status update
                        System.err.println("Error rewarding One Xu for order " + orderId + ": " + e.getMessage());
                    }
                }
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
        return orderRepository.findByShopShopIdInOrderByOrderDateDesc(shopIds, pageable);
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    @Transactional
    public void confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể xác nhận đơn hàng ở trạng thái 'Chờ xác nhận'.");
        }

        order.setStatus(Order.OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, User vendor) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng #" + orderId));

        // Security Check: Ensure the vendor owns this order
        if (order.getShop() == null || !order.getShop().getVendor().equals(vendor)) {
            throw new IllegalStateException("Bạn không có quyền hủy đơn hàng này.");
        }

        // Business Logic Check: Only PENDING orders can be cancelled by vendor
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể hủy đơn hàng khi ở trạng thái 'Chờ xác nhận'.");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        // Optionally, you can set a reason for cancellation
        // order.setCancellationReason("Hủy bởi người bán");
        order.setCancelledDate(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public List<Object[]> countOrdersByStatus(List<Long> shopIds) {
        return orderRepository.countOrdersByStatus(shopIds);
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
    public Page<Order> findByShopIdInAndOrderIdContainingDirect(List<Long> shopIds, String search, Pageable pageable) {
        return orderRepository.findByShopIdInAndOrderIdContainingDirect(shopIds, search, pageable);
    }

    @Override
    public Page<Order> findByShopIdInAndStatusAndOrderIdContainingDirect(List<Long> shopIds, Order.OrderStatus status, String search, Pageable pageable) {
        return orderRepository.findByShopIdInAndStatusAndOrderIdContainingDirect(shopIds, status, search, pageable);
    }

    @Override
    public Long countByShopIdInAndStatusDirect(List<Long> shopIds, Order.OrderStatus status) {
        return orderRepository.countByShopIdInAndStatusDirect(shopIds, status);
    }

    @Override
    public Optional<Order> findByIdAndShopIdIn(Long orderId, List<Long> shopIds) {
        // Tuân thủ logic cũ
        return orderRepository.findByIdAndShopIdIn(orderId, shopIds);
    }

    @Override
    public Optional<Order> findWithDetailsByIdAndShopIdIn(Long orderId, List<Long> shopIds) {
        return orderRepository.findWithDetailsByIdAndShopIds(orderId, shopIds);
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

    @Override
    public void updateOrder(Order order) {
        orderRepository.save(order);
    }

    @Override
    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }

    // ===== OVERDUE DELIVERY MANAGEMENT METHODS =====

    @Override
    public List<Order> findOverdueOrders() {
        LocalDateTime currentTime = LocalDateTime.now();
        return orderRepository.findOverdueOrders(currentTime);
    }

    @Override
    public List<Order> findOverdueOrdersByShipper(User shipper) {
        LocalDateTime currentTime = LocalDateTime.now();
        return orderRepository.findOverdueOrdersByShipper(shipper, currentTime);
    }

    @Override
    public long countOverdueOrdersByShipper(User shipper) {
        LocalDateTime currentTime = LocalDateTime.now();
        return orderRepository.countOverdueOrdersByShipper(shipper, currentTime);
    }

    @Override
    @Transactional
    public void markOverdueOrders() {
        LocalDateTime currentTime = LocalDateTime.now();
        List<Order> ordersToMark = orderRepository.findOrdersToMarkOverdue(currentTime);
        int updatedCount = 0;
        
        for (Order order : ordersToMark) {
            order.setStatus(Order.OrderStatus.OVERDUE);
            orderRepository.save(order);
            updatedCount++;
            
            System.out.println("Order #" + order.getOrderId() + " marked as OVERDUE - " +
                             "Estimated delivery: " + order.getEstimatedDeliveryDate() +
                             " | Shipper: " + (order.getShipper() != null ? order.getShipper().getName() : "N/A"));
        }
        
        if (updatedCount > 0) {
            System.out.println("Marked " + updatedCount + " orders as OVERDUE");
        }
    }

    @Override
    public boolean isOrderOverdue(Order order) {
        if (order == null || order.getEstimatedDeliveryDate() == null) {
            return false;
        }
        
        return (order.getStatus() == Order.OrderStatus.SHIPPING || order.getStatus() == Order.OrderStatus.OVERDUE) && 
               order.getEstimatedDeliveryDate().isBefore(LocalDateTime.now());
    }

    @Override
    public List<Order> findOrdersToMarkOverdue() {
        LocalDateTime currentTime = LocalDateTime.now();
        return orderRepository.findOrdersToMarkOverdue(currentTime);
    }
}
