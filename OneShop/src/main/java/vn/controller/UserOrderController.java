package vn.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.entity.Order;
import vn.entity.OrderDetail;
import vn.entity.User;
import vn.entity.Comment;
import vn.repository.OrderRepository;
import vn.repository.OrderDetailRepository;
import vn.repository.UserRepository;
import vn.service.CommentService;

import java.time.LocalDateTime;
import java.util.*;

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

        // Load shop information to avoid lazy loading issues
        if (order.getShop() != null) {
            order.getShop().getShopName();
            System.out.println("Debug - Order shop: " + order.getShop().getShopName());
        } else {
            System.out.println("Debug - Order shop is null");
        }

        List<Object[]> orderDetails = orderDetailRepository.findOrderDetailsByOrderId(orderId);

        // Tính toán phí vận chuyển thực tế (đã trừ shipping voucher)
        double actualShippingFee = order.getShippingFee() != null ? order.getShippingFee() : 0.0;
        
        // Kiểm tra xem có shipping voucher discount trong note không
        if (order.getNote() != null && order.getNote().contains("Voucher ship:")) {
            // Parse shipping voucher discount từ note
            String note = order.getNote();
            String[] lines = note.split("\n");
            for (String line : lines) {
                if (line.contains("Voucher ship:") && line.contains("- Giảm:")) {
                    try {
                        // Extract discount amount từ line như "Voucher ship: WEEKEND - Giảm: 30000.0 VNĐ"
                        String discountPart = line.substring(line.indexOf("- Giảm:") + 8);
                        String discountStr = discountPart.split(" ")[0]; // Lấy số trước "VNĐ"
                        double shippingDiscount = Double.parseDouble(discountStr);
                        actualShippingFee = Math.max(0, actualShippingFee - shippingDiscount);
                        break;
                    } catch (Exception e) {
                        // Nếu parse lỗi, giữ nguyên shipping fee
                        System.out.println("Error parsing shipping voucher discount: " + e.getMessage());
                    }
                }
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);
        model.addAttribute("actualShippingFee", actualShippingFee);

        return "web/order-detail-simple";
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
     * Hiển thị form yêu cầu trả hàng
     */
    @GetMapping("/return-order/{orderId}")
    public String showReturnOrderForm(@PathVariable Long orderId, HttpSession session, Model model) {
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
        if (order.getStatus() != Order.OrderStatus.DELIVERED) {
            return "redirect:/user/my-orders?error=invalid_status";
        }

        model.addAttribute("order", order);
        return "web/return-order-form";
    }

    /**
     * Xử lý yêu cầu trả hàng với lý do
     */
    @PostMapping("/return-order/{orderId}")
    public String processReturnOrder(@PathVariable Long orderId, 
                                   @RequestParam String returnReason,
                                   HttpSession session, 
                                   RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng");
            return "redirect:/user/my-orders";
        }

        Order order = orderOpt.get();
        
        // Kiểm tra xem đơn hàng có thuộc về user này không
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập đơn hàng này");
            return "redirect:/user/my-orders";
        }

        // Kiểm tra trạng thái đơn hàng
        if (order.getStatus() != Order.OrderStatus.DELIVERED) {
            redirectAttributes.addFlashAttribute("error", "Chỉ có thể yêu cầu trả hàng khi đơn hàng đã được giao");
            return "redirect:/user/my-orders";
        }

        // Kiểm tra lý do trả hàng
        if (returnReason == null || returnReason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập lý do trả hàng");
            return "redirect:/return-order/" + orderId;
        }

        try {
            // Cập nhật trạng thái đơn hàng và lý do trả hàng
            order.setStatus(Order.OrderStatus.RETURNED);
            order.setCancellationReason(returnReason.trim());
            order.setCancelledDate(LocalDateTime.now());
            orderRepository.save(order);

            redirectAttributes.addFlashAttribute("success", "Đã gửi yêu cầu trả hàng thành công. Chúng tôi sẽ xem xét và phản hồi trong thời gian sớm nhất.");
            return "redirect:/user/my-orders?status=returned";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xử lý yêu cầu trả hàng: " + e.getMessage());
            return "redirect:/return-order/" + orderId;
        }
    }
}
