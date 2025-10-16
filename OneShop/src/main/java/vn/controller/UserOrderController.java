package vn.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import vn.entity.Order;
import vn.entity.OrderDetail;
import vn.entity.User;
import vn.entity.Comment;
import vn.repository.OrderRepository;
import vn.repository.OrderDetailRepository;
import vn.repository.UserRepository;
import vn.service.CommentService;

import java.util.List;
import java.util.Optional;

@Controller
public class UserOrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentService commentService;

    /**
     * Hiển thị trang lịch sử đơn hàng của user với các tab theo trạng thái
     */
    @GetMapping("/user/my-orders")
    public String myOrders(HttpSession session, Model model,
                          @RequestParam(value = "status", required = false) String status,
                          @RequestParam(value = "page", defaultValue = "0") int page,
                          @RequestParam(value = "size", defaultValue = "10") int size) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Lấy user từ database để đảm bảo có đầy đủ thông tin
        Optional<User> userOpt = userRepository.findById(user.getUserId());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        user = userOpt.get();

        Pageable pageable = PageRequest.of(page, size);
        Page<Order> ordersPage;

        if (status != null && !status.isEmpty()) {
            try {
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                ordersPage = orderRepository.findByUserAndStatusOrderByOrderDateDesc(user, orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                // Nếu status không hợp lệ, lấy tất cả đơn hàng
                ordersPage = orderRepository.findByUserOrderByOrderDateDescPageable(user, pageable);
            }
        } else {
            ordersPage = orderRepository.findByUserOrderByOrderDateDescPageable(user, pageable);
        }
        
        // Debug: Log số lượng đơn hàng
        System.out.println("Debug - Total orders found: " + ordersPage.getTotalElements());
        System.out.println("Debug - Orders on current page: " + ordersPage.getContent().size());
        System.out.println("Debug - User ID: " + user.getUserId());
        System.out.println("Debug - User email: " + user.getEmail());
        
        // Debug: Log trạng thái của từng đơn hàng
        for (Order order : ordersPage.getContent()) {
            System.out.println("Debug - Order ID: " + order.getOrderId() + ", Status: " + order.getStatus());
        }
        
        // Debug: Log pagination info
        System.out.println("Debug - Current page: " + page + ", Page size: " + size);
        System.out.println("Debug - Total pages: " + ordersPage.getTotalPages());
        System.out.println("Debug - Status filter: " + status);

        // Load orderDetails for each order using repository
        java.util.Map<Long, Comment> myCommentsMap = new java.util.HashMap<>();
        for (Order order : ordersPage.getContent()) {
            // Eagerly load product and shop to avoid lazy issues in view
            List<OrderDetail> orderDetails = orderDetailRepository.findByOrderIdWithProductAndShop(order.getOrderId());
            
            // If this is a cancelled order and has no details (e.g., user cancelled very early),
            // create a lightweight display row so the UI remains consistent.
            if ((orderDetails == null || orderDetails.isEmpty()) && order.getStatus() == Order.OrderStatus.CANCELLED) {
                OrderDetail placeholder = new OrderDetail();
                placeholder.setOrder(order);
                placeholder.setProductName("Sản phẩm đã hủy");
                placeholder.setUnitPrice(order.getTotalAmount());
                placeholder.setQuantity(1);
                placeholder.setTotalPrice(order.getTotalAmount());
                orderDetails = new java.util.ArrayList<>();
                orderDetails.add(placeholder);
            }

            // Set order.shop from first order detail if available so header can display shop name
            if (order.getShop() == null && orderDetails != null && !orderDetails.isEmpty()) {
                OrderDetail first = orderDetails.get(0);
                if (first.getProduct() != null && first.getProduct().getShop() != null) {
                    order.setShop(first.getProduct().getShop());
                }
            }
            // Force load product and shop information for each orderDetail
            for (OrderDetail orderDetail : orderDetails) {
                if (orderDetail.getProduct() != null) {
                    // Access product to trigger lazy loading
                    orderDetail.getProduct().getProductName();
                    // Access shop information to trigger lazy loading
                    if (orderDetail.getProduct().getShop() != null) {
                        orderDetail.getProduct().getShop().getShopName();
                        orderDetail.getProduct().getShop().getShopLogo();
                    }
                }
                
                // Load comment for this orderDetail to check if user has reviewed
                try {
                    java.util.Optional<Comment> cmt = commentService.getUserCommentForOrderDetail(user.getUserId(), orderDetail.getOrderDetailId());
                    if (cmt.isEmpty() && orderDetail.getProduct() != null) {
                        // Fallback: nếu chưa có review theo order detail, lấy review gần nhất theo sản phẩm
                        cmt = commentService.getLatestUserCommentForProduct(user.getUserId(), orderDetail.getProduct().getProductId());
                    }
                    cmt.ifPresent(comment -> myCommentsMap.put(orderDetail.getOrderDetailId(), comment));
                } catch (Exception ignored) {}
            }
            order.setOrderDetails(orderDetails);
        }

        // Thống kê số lượng đơn hàng theo từng trạng thái
        long pendingCount = orderRepository.countByUserAndStatus(user, Order.OrderStatus.PENDING);
        long confirmedCount = orderRepository.countByUserAndStatus(user, Order.OrderStatus.CONFIRMED);
        long shippingCount = orderRepository.countByUserAndStatus(user, Order.OrderStatus.SHIPPING);
        long deliveredCount = orderRepository.countByUserAndStatus(user, Order.OrderStatus.DELIVERED);
        long cancelledCount = orderRepository.countByUserAndStatus(user, Order.OrderStatus.CANCELLED);
        long returnedCount = orderRepository.countByUserAndStatus(user, Order.OrderStatus.RETURNED);
        
        // Debug: Log số lượng đơn hàng theo từng trạng thái
        System.out.println("Debug - Pending: " + pendingCount + ", Confirmed: " + confirmedCount + 
                          ", Shipping: " + shippingCount + ", Delivered: " + deliveredCount + 
                          ", Cancelled: " + cancelledCount + ", Returned: " + returnedCount);

        model.addAttribute("user", user);
        model.addAttribute("orders", ordersPage);
        model.addAttribute("currentStatus", status);
        model.addAttribute("myCommentsMap", myCommentsMap);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("shippingCount", shippingCount);
        model.addAttribute("deliveredCount", deliveredCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("returnedCount", returnedCount);

        return "web/my-orders";
    }

    /**
     * Xem chi tiết đơn hàng
     */
    @GetMapping("/order-detail/{orderId}")
    public String orderDetail(@PathVariable Long orderId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return "redirect:/user/my-orders";
        }

        Order order = orderOpt.get();
        
        // Kiểm tra xem đơn hàng có thuộc về user này không
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            return "redirect:/user/my-orders";
        }

        List<Object[]> orderDetails = orderDetailRepository.findOrderDetailsByOrderId(orderId);

        model.addAttribute("user", user);
        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);

        return "web/order-detail";
    }

    /**
     * Hủy đơn hàng (chỉ được hủy khi đơn hàng ở trạng thái PENDING hoặc CONFIRMED)
     */
    @GetMapping("/cancel-order/{orderId}")
    public String cancelOrder(@PathVariable Long orderId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return "redirect:/user/my-orders";
        }

        Order order = orderOpt.get();
        
        // Kiểm tra xem đơn hàng có thuộc về user này không
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            return "redirect:/user/my-orders";
        }

        // Chỉ cho phép hủy đơn hàng khi ở trạng thái PENDING hoặc CONFIRMED
        if (order.getStatus() == Order.OrderStatus.PENDING || 
            order.getStatus() == Order.OrderStatus.CONFIRMED) {
            order.setStatus(Order.OrderStatus.CANCELLED);
            orderRepository.save(order);
        }

        return "redirect:/user/my-orders?status=cancelled";
    }

    /**
     * Yêu cầu trả hàng (chỉ được yêu cầu khi đơn hàng đã giao)
     */
    @GetMapping("/return-order/{orderId}")
    public String returnOrder(@PathVariable Long orderId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return "redirect:/user/my-orders";
        }

        Order order = orderOpt.get();
        
        // Kiểm tra xem đơn hàng có thuộc về user này không
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            return "redirect:/user/my-orders";
        }

        // Chỉ cho phép yêu cầu trả hàng khi đơn hàng đã giao
        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            order.setStatus(Order.OrderStatus.RETURNED);
            orderRepository.save(order);
        }

        return "redirect:/user/my-orders?status=returned";
    }
}
