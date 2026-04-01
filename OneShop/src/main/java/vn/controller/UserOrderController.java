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
import vn.service.OrderService;

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
    
    @Autowired
    private vn.service.RefundService refundService;
    
    @Autowired
    private vn.service.OneXuService oneXuService;

    @Autowired
    private OrderService orderService;

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
        java.util.Map<Long, vn.entity.Refund> refundsMap = new java.util.HashMap<>();
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
            if (orderDetails != null) {
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
            }
            order.setOrderDetails(orderDetails);
            
            // Load refund information for RETURNED, RETURN_REQUESTED orders or DELIVERED orders with refund request
            if (order.getStatus() == Order.OrderStatus.RETURNED || 
                order.getStatus() == Order.OrderStatus.RETURN_REQUESTED || 
                order.getStatus() == Order.OrderStatus.DELIVERED) {
                try {
                    vn.entity.Refund refund = refundService.getRefundByOrderId(order.getOrderId());
                    if (refund != null) {
                        refundsMap.put(order.getOrderId(), refund);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading refund for order " + order.getOrderId() + ": " + e.getMessage());
                }
            }
        }

        // Thống kê số lượng đơn hàng theo từng trạng thái
        long pendingCount = orderRepository.countByUserAndStatus(user, Order.OrderStatus.PENDING);
        long confirmedCount = orderRepository.countByUserAndStatus(user, Order.OrderStatus.CONFIRMED);
        long shippingCount = orderRepository.countByUserAndStatus(user, Order.OrderStatus.SHIPPING);
        long deliveredCount = orderRepository.countByUserAndStatus(user, Order.OrderStatus.DELIVERED);
        long cancelledCount = orderRepository.countByUserAndStatus(user, Order.OrderStatus.CANCELLED);
        long returnRequestedCount = orderRepository.countByUserAndStatus(user, Order.OrderStatus.RETURN_REQUESTED);
        long returnedCount = orderRepository.countByUserAndStatus(user, Order.OrderStatus.RETURNED);
        
        // Debug: Log số lượng đơn hàng theo từng trạng thái
        System.out.println("Debug - Pending: " + pendingCount + ", Confirmed: " + confirmedCount + 
                          ", Shipping: " + shippingCount + ", Delivered: " + deliveredCount + 
                          ", Cancelled: " + cancelledCount + ", Return Requested: " + returnRequestedCount + 
                          ", Returned: " + returnedCount);

        model.addAttribute("user", user);
        model.addAttribute("orders", ordersPage);
        model.addAttribute("currentStatus", status);
        model.addAttribute("myCommentsMap", myCommentsMap);
        model.addAttribute("refundsMap", refundsMap);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("shippingCount", shippingCount);
        model.addAttribute("deliveredCount", deliveredCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("returnRequestedCount", returnRequestedCount);
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

        // Load refund information if order is RETURNED, RETURN_REQUESTED or DELIVERED (user đã yêu cầu trả hàng)
        vn.entity.Refund refund = null;
        if (order.getStatus() == Order.OrderStatus.RETURNED || 
            order.getStatus() == Order.OrderStatus.RETURN_REQUESTED || 
            order.getStatus() == Order.OrderStatus.DELIVERED) {
            try {
                refund = refundService.getRefundByOrderId(orderId);
            } catch (Exception e) {
                System.err.println("Error loading refund for order " + orderId + ": " + e.getMessage());
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);
        model.addAttribute("actualShippingFee", actualShippingFee);
        model.addAttribute("refund", refund);

        return "web/order-detail-simple";
    }

    /**
     * Hủy đơn hàng (chỉ khi chưa giao: NEW / PENDING / CONFIRMED — đi qua OrderService để mail + audit)
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

        // Chỉ cho phép hủy khi chưa giao (NEW/PENDING: chờ xác nhận; CONFIRMED: đã xác nhận nhưng khách vẫn được hủy theo policy cũ)
        if (order.getStatus() == Order.OrderStatus.NEW
                || order.getStatus() == Order.OrderStatus.PENDING
                || order.getStatus() == Order.OrderStatus.CONFIRMED) {
            order.setCancellationReason("Khách hàng chủ động hủy đơn");
            order.setCancelledDate(LocalDateTime.now());
            orderRepository.save(order);
            orderService.updateOrderStatus(orderId, Order.OrderStatus.CANCELLED, "UserOrderController.cancelOrder");
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
        
        // Tính finalAmount (số tiền khách hàng thực sự đã thanh toán)
        double finalAmount;
        if (order.getFinalAmount() != null && order.getFinalAmount() > 0) {
            finalAmount = order.getFinalAmount();
        } else {
            double totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
            double shippingFee = order.getShippingFee() != null ? order.getShippingFee() : 0.0;
            double discountAmount = order.getDiscountAmount() != null ? order.getDiscountAmount() : 0.0;
            finalAmount = totalAmount + shippingFee - discountAmount;
        }
        
        // Load OneXu balance để hiển thị trong form
        Double currentBalance = oneXuService.getUserBalance(user.getUserId());
        Double balanceAfter = currentBalance + finalAmount; // Dùng finalAmount thay vì totalAmount

        model.addAttribute("order", order);
        model.addAttribute("finalAmount", finalAmount);
        model.addAttribute("currentBalance", currentBalance);
        model.addAttribute("balanceAfter", balanceAfter);
        return "web/return-order-form";
    }

    /**
     * Xử lý yêu cầu trả hàng với lý do và phương thức hoàn tiền
     */
    @PostMapping("/return-order/{orderId}")
    public String processReturnOrder(@PathVariable Long orderId, 
                                   @RequestParam String returnReason,
                                   @RequestParam String refundMethod,
                                   @RequestParam(required = false) String bankName,
                                   @RequestParam(required = false) String bankAccountNumber,
                                   @RequestParam(required = false) String accountHolderName,
                                   @RequestParam(required = false) String bankBranch,
                                   @RequestParam(required = false) String contactPhone,
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
        
        // Kiểm tra phương thức hoàn tiền
        if (refundMethod == null || (!"ONEXU".equals(refundMethod) && !"BANK_TRANSFER".equals(refundMethod))) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn phương thức hoàn tiền");
            return "redirect:/return-order/" + orderId;
        }

        try {
            // Lưu lý do (đơn vẫn DELIVERED) rồi chuyển trạng thái qua OrderService để Observer/Publisher gửi mail
            order.setCancellationReason(returnReason.trim());
            order.setCancelledDate(LocalDateTime.now());
            orderRepository.save(order);
            orderService.updateOrderStatus(orderId, Order.OrderStatus.RETURN_REQUESTED);

            // Tạo refund request với phương thức đã chọn
            refundService.createRefundRequestFromUser(orderId, user.getUserId(), refundMethod,
                    bankName, bankAccountNumber, accountHolderName, bankBranch, contactPhone);

            redirectAttributes.addFlashAttribute("success", "Đã gửi yêu cầu trả hàng thành công. Chúng tôi sẽ xem xét và phản hồi trong thời gian sớm nhất.");
            return "redirect:/user/my-orders?status=return_requested";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xử lý yêu cầu trả hàng: " + e.getMessage());
            return "redirect:/return-order/" + orderId;
        }
    }
}
