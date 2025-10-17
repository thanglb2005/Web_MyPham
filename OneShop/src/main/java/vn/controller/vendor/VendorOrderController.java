package vn.controller.vendor;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.entity.Order;
import vn.entity.User;
import vn.service.OrderService;
import vn.service.SendMailService;
import vn.repository.OrderDetailRepository;
import vn.entity.OrderDetail;
import java.text.NumberFormat;
import java.util.Locale;
import vn.service.ShopService;

import java.util.List;

/**
 * Controller quản lý đơn hàng cho Vendor
 * @author OneShop Team
 */
@Controller
@RequestMapping("/vendor/orders")
public class VendorOrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ShopService shopService;

    @Autowired
    private SendMailService sendMailService;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    private static final Order.OrderStatus[] STATUS_TABS = {
            Order.OrderStatus.PENDING,
            Order.OrderStatus.CONFIRMED,
            Order.OrderStatus.SHIPPING,
            Order.OrderStatus.DELIVERED,
            Order.OrderStatus.CANCELLED,
            Order.OrderStatus.RETURNED
    };

    /**
     * Danh sách đơn hàng với tabs theo trạng thái
     */
    @GetMapping
    public String orderList(
            @RequestParam(value = "shopId", required = false) Long shopId,
            @RequestParam(value = "status", required = false) Order.OrderStatus status,
            @RequestParam(value = "q", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            HttpSession session, Model model) {
        
        try {
            User vendor = ensureVendor(session);
            if (vendor == null) {
                return "redirect:/login";
            }

            // Lấy shop của vendor
            List<Long> shopIds = getShopIdsByVendor(vendor);
            System.out.println("DEBUG: Vendor shop IDs: " + shopIds);
            if (shopIds.isEmpty()) {
                model.addAttribute("error", "Bạn chưa có shop nào.");
                model.addAttribute("orders", Page.empty());
                model.addAttribute("totalOrders", 0L);
                model.addAttribute("pendingCount", 0L);
                model.addAttribute("confirmedCount", 0L);
                model.addAttribute("shippingCount", 0L);
                model.addAttribute("deliveredCount", 0L);
                model.addAttribute("cancelledCount", 0L);
                model.addAttribute("returnedCount", 0L);
                return "vendor/orders/list";
            }

            // Nếu có shopId cụ thể, filter theo shop đó
            List<Long> targetShopIds;
            if (shopId != null && shopIds.contains(shopId)) {
                targetShopIds = List.of(shopId);
                System.out.println("DEBUG: Filtering by specific shopId: " + shopId);
            } else {
                targetShopIds = shopIds;
                System.out.println("DEBUG: Using all vendor shops: " + shopIds);
            }

            // Phân trang
            Pageable pageable = PageRequest.of(page, size);
            
            // Lấy danh sách đơn hàng
            String normalizedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
            
            Page<Order> orders;
            try {
                orders = getOrdersByShops(targetShopIds, status, normalizedSearch, pageable);
                System.out.println("DEBUG: Orders found: " + orders.getTotalElements() + " orders");
                System.out.println("DEBUG: Orders content size: " + orders.getContent().size());
            } catch (Exception e) {
                System.err.println("ERROR: Failed to get orders: " + e.getMessage());
                e.printStackTrace();
                model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách đơn hàng.");
                return "vendor/orders/list";
            }
            
            // Thống kê theo trạng thái - sử dụng cách đếm riêng biệt như UserOrderController
            Long totalOrders = orderService.countByShopIdIn(targetShopIds);
            Long pendingCount = orderService.countByShopIdInAndStatus(targetShopIds, Order.OrderStatus.PENDING);
            Long confirmedCount = orderService.countByShopIdInAndStatus(targetShopIds, Order.OrderStatus.CONFIRMED);
            Long shippingCount = orderService.countByShopIdInAndStatus(targetShopIds, Order.OrderStatus.SHIPPING);
            Long deliveredCount = orderService.countByShopIdInAndStatus(targetShopIds, Order.OrderStatus.DELIVERED);
            Long cancelledCount = orderService.countByShopIdInAndStatus(targetShopIds, Order.OrderStatus.CANCELLED);
            Long returnedCount = orderService.countByShopIdInAndStatus(targetShopIds, Order.OrderStatus.RETURNED);
            
            // Debug: Log số lượng đơn hàng theo từng trạng thái
            System.out.println("DEBUG - Total: " + totalOrders + ", Pending: " + pendingCount + ", Confirmed: " + confirmedCount + 
                              ", Shipping: " + shippingCount + ", Delivered: " + deliveredCount + 
                              ", Cancelled: " + cancelledCount + ", Returned: " + returnedCount);
            
            model.addAttribute("orders", orders);
            model.addAttribute("currentStatus", status != null ? status.name() : null);
            model.addAttribute("totalOrders", totalOrders != null ? totalOrders : 0L);
            model.addAttribute("pendingCount", pendingCount != null ? pendingCount : 0L);
            model.addAttribute("confirmedCount", confirmedCount != null ? confirmedCount : 0L);
            model.addAttribute("shippingCount", shippingCount != null ? shippingCount : 0L);
            model.addAttribute("deliveredCount", deliveredCount != null ? deliveredCount : 0L);
            model.addAttribute("cancelledCount", cancelledCount != null ? cancelledCount : 0L);
            model.addAttribute("returnedCount", returnedCount != null ? returnedCount : 0L);
            model.addAttribute("search", search != null ? search : "");
            model.addAttribute("vendor", vendor);
            model.addAttribute("shopId", shopId);
            model.addAttribute("pageTitle", "Quản lý đơn hàng");
            
            return "vendor/orders/list";

        } catch (Exception e) {
            System.err.println("ERROR: Lỗi nghiêm trọng trong orderList: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Đã có lỗi xảy ra khi tải danh sách đơn hàng. Vui lòng thử lại.");
            model.addAttribute("orders", Page.empty());
            model.addAttribute("totalOrders", 0L);
            model.addAttribute("pendingCount", 0L);
            model.addAttribute("confirmedCount", 0L);
            model.addAttribute("shippingCount", 0L);
            model.addAttribute("deliveredCount", 0L);
            model.addAttribute("cancelledCount", 0L);
            model.addAttribute("returnedCount", 0L);
            return "vendor/orders/list";
        }
    }

    @PostMapping("/confirm")
    public String confirmOrder(@RequestParam("orderId") Long orderId,
                               @RequestParam(value = "shopId", required = false) Long shopId,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        try {
            Order order = orderService.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng #" + orderId));

            List<Long> vendorShopIds = getShopIdsByVendor(vendor);
            if (order.getShop() == null || !vendorShopIds.contains(order.getShop().getShopId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xác nhận đơn hàng này.");
                return "redirect:/vendor/orders" + (shopId != null ? "?shopId=" + shopId : "");
            }

            orderService.confirmOrder(orderId);
            try {
                Order confirmed = orderService.findById(orderId).orElse(null);
                if (confirmed != null) {
                    sendOrderConfirmedEmail(confirmed);
                }
            } catch (Exception ignore) {}
            redirectAttributes.addFlashAttribute("success", "Đã xác nhận đơn hàng #" + orderId + " thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xác nhận đơn hàng: " + e.getMessage());
        }

        String redirectUrl = "/vendor/orders";
        if (shopId != null) {
            redirectUrl += "?shopId=" + shopId;
        }
        return "redirect:" + redirectUrl;
    }

    @PostMapping("/cancel")
    public String cancelOrder(@RequestParam("orderId") Long orderId,
                              @RequestParam(value = "shopId", required = false) Long shopId,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {

        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        try {
            orderService.cancelOrder(orderId, vendor);
            redirectAttributes.addFlashAttribute("success", "Đã hủy đơn hàng #" + orderId + " thành công.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi hủy đơn hàng: " + e.getMessage());
        }

        String redirectUrl = "/vendor/orders";
        if (shopId != null) {
            redirectUrl += "?shopId=" + shopId;
        }
        return "redirect:" + redirectUrl;
    }

    /**
     * Chi tiết đơn hàng
     */
    @GetMapping("/{orderId}")
    public String orderDetail(@PathVariable Long orderId, HttpSession session, Model model) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        // Kiểm tra quyền truy cập đơn hàng
        Order order = getOrderForVendor(orderId, vendor);
        if (order == null) {
            model.addAttribute("error", "Không tìm thấy đơn hàng hoặc không có quyền truy cập.");
            return "redirect:/vendor/orders";
        }
        
        model.addAttribute("order", order);
        model.addAttribute("vendor", vendor);
        model.addAttribute("pageTitle", "Chi tiết đơn hàng #" + orderId);
        
        return "vendor/orders/detail";
    }

    /**
     * Xác nhận đơn hàng (NEW → CONFIRMED)
     */
    @PostMapping("/{orderId}/confirm")
    public String confirmOrder(@PathVariable Long orderId, HttpSession session, RedirectAttributes redirectAttributes) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        try {
            Order order = getOrderForVendor(orderId, vendor);
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng hoặc không có quyền truy cập");
                return "redirect:/vendor/orders";
            }
            
            if (order.getStatus() != Order.OrderStatus.PENDING) {
                redirectAttributes.addFlashAttribute("error", "Chỉ có thể xác nhận khi trạng thái là PENDING");
                return "redirect:/vendor/orders/" + orderId;
            }
            
            orderService.updateOrderStatus(orderId, Order.OrderStatus.CONFIRMED);
            try {
                Order confirmed = orderService.findById(orderId).orElse(null);
                if (confirmed != null) {
                    sendOrderConfirmedEmail(confirmed);
                }
            } catch (Exception ignore) {}
            redirectAttributes.addFlashAttribute("success", "Đã xác nhận đơn hàng #" + orderId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi xác nhận đơn hàng: " + e.getMessage());
        }
        
        return "redirect:/vendor/orders/" + orderId;
    }

    /**
     * Cập nhật trạng thái đơn hàng
     */
    @PostMapping("/{orderId}/update-status")
    public String updateOrderStatus(@PathVariable Long orderId, 
                                   @RequestParam String newStatus,
                                   HttpSession session, RedirectAttributes redirectAttributes) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        try {
            Order order = getOrderForVendor(orderId, vendor);
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng hoặc không có quyền truy cập");
                return "redirect:/vendor/orders";
            }
            
            Order.OrderStatus status = Order.OrderStatus.valueOf(newStatus);
            orderService.updateOrderStatus(orderId, status);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái đơn hàng #" + orderId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi cập nhật trạng thái: " + e.getMessage());
        }
        
        return "redirect:/vendor/orders/" + orderId;
    }

    /**
     * Hủy đơn hàng
     */
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId, 
                             @RequestParam String reason,
                             HttpSession session, RedirectAttributes redirectAttributes) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        try {
            Order order = getOrderForVendor(orderId, vendor);
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng hoặc không có quyền truy cập");
                return "redirect:/vendor/orders";
            }
            
            orderService.updateOrderStatus(orderId, Order.OrderStatus.CANCELLED);
            redirectAttributes.addFlashAttribute("success", "Đã hủy đơn hàng #" + orderId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hủy đơn hàng: " + e.getMessage());
        }
        
        return "redirect:/vendor/orders/" + orderId;
    }

    /**
     * Duyệt yêu cầu trả/hoàn (DELIVERED → RETURNED)
     */
    @PostMapping("/{orderId}/approve-return")
    public String approveReturn(@PathVariable Long orderId,
                               @RequestParam Double refundAmount,
                               @RequestParam String returnReason,
                               HttpSession session, RedirectAttributes redirectAttributes) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        try {
            Order order = getOrderForVendor(orderId, vendor);
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng hoặc không có quyền truy cập");
                return "redirect:/vendor/orders";
            }
            
            if (order.getStatus() != Order.OrderStatus.DELIVERED) {
                redirectAttributes.addFlashAttribute("error", "Chỉ có thể duyệt hoàn khi đơn hàng đã được giao");
                return "redirect:/vendor/orders/" + orderId;
            }
            
            if (refundAmount <= 0 || refundAmount > order.getTotalAmount()) {
                redirectAttributes.addFlashAttribute("error", "Số tiền hoàn không hợp lệ");
                return "redirect:/vendor/orders/" + orderId;
            }
            
            orderService.updateOrderStatus(orderId, Order.OrderStatus.RETURNED);
            redirectAttributes.addFlashAttribute("success", "Đã duyệt hoàn tiền cho đơn hàng #" + orderId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi duyệt hoàn tiền: " + e.getMessage());
        }
        
        return "redirect:/vendor/orders/" + orderId;
    }

    /**
     * Kiểm tra quyền vendor
     */
    private User ensureVendor(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRoles().stream().anyMatch(role -> "ROLE_VENDOR".equals(role.getName()))) {
            return null;
        }
        return user;
    }

    /**
     * Lấy shop IDs của vendor
     */
    private List<Long> getShopIdsByVendor(User vendor) {
        return shopService.findShopIdsByVendor(vendor);
    }

    /**
     * Lấy đơn hàng theo shop IDs
     */
    private Page<Order> getOrdersByShops(List<Long> shopIds, Order.OrderStatus status, String search, Pageable pageable) {
        if (shopIds.isEmpty()) {
            return Page.empty();
        }

        String trimmedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        if (status != null && trimmedSearch != null) {
            return orderService.findByShopIdInAndStatusAndOrderIdContainingDirect(shopIds, status, trimmedSearch, pageable);
        } else if (status != null) {
            return orderService.findByShopIdInAndStatusDirect(shopIds, status, pageable);
        } else if (trimmedSearch != null) {
            return orderService.findByShopIdInAndOrderIdContainingDirect(shopIds, trimmedSearch, pageable);
        } else {
            return orderService.findByShopIdInDirect(shopIds, pageable);
        }
    }

    /**
     * Lấy đơn hàng cho vendor (kiểm tra quyền)
     */
    private Order getOrderForVendor(Long orderId, User vendor) {
        List<Long> shopIds = getShopIdsByVendor(vendor);
        if (shopIds.isEmpty()) {
            return null;
        }
        return orderService.findByIdAndShopIdIn(orderId, shopIds).orElse(null);
    }

    private void sendOrderConfirmedEmail(Order order) {
        try {
            String to = order.getCustomerEmail() != null ? order.getCustomerEmail() :
                    (order.getUser() != null ? order.getUser().getEmail() : null);
            if (to == null || to.isEmpty()) return;

            String shopName = (order.getShop() != null && order.getShop().getShopName() != null)
                    ? order.getShop().getShopName() : "OneShop";
            String subject = "Xác nhận đơn hàng #" + order.getOrderId() + " - " + shopName;

            NumberFormat vnd = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            double total = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
            String payment = order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "COD";

            StringBuilder itemsHtml = new StringBuilder();
            try {
                java.util.List<OrderDetail> details = orderDetailRepository.findByOrderIdWithProductAndShop(order.getOrderId());
                for (OrderDetail d : details) {
                    String name = d.getProductName() != null ? d.getProductName() :
                            (d.getProduct() != null ? d.getProduct().getProductName() : "Sản phẩm");
                    int qty = d.getQuantity() != null ? d.getQuantity() : 0;
                    double unit = d.getUnitPrice() != null ? d.getUnitPrice() : 0.0;
                    double line = d.getTotalPrice() != null ? d.getTotalPrice() : unit * qty;
                    itemsHtml.append("<tr>")
                            .append("<td style='padding:8px 12px;border-bottom:1px solid #eee'>").append(name).append("</td>")
                            .append("<td style='padding:8px 12px;text-align:center;border-bottom:1px solid #eee'>").append(qty).append("</td>")
                            .append("<td style='padding:8px 12px;text-align:right;border-bottom:1px solid #eee'>").append(vnd.format(unit)).append("</td>")
                            .append("<td style='padding:8px 12px;text-align:right;border-bottom:1px solid #eee'>").append(vnd.format(line)).append("</td>")
                            .append("</tr>");
                }
            } catch (Exception ignore) {}

            String eta = order.getEstimatedDeliveryDate() != null ? order.getEstimatedDeliveryDate().toString() : "(sẽ thông báo sau)";
            String body = "" +
                    "<div style='font-family:Arial,Helvetica,sans-serif;line-height:1.6;color:#111'>" +
                    "<h2 style='color:#0a7cff;margin:0 0 12px'>Đơn hàng đã được xác nhận ✅</h2>" +
                    "<p>Chào " + (order.getCustomerName() != null ? order.getCustomerName() : "bạn") + ",</p>" +
                    "<p>Đơn hàng <strong>#" + order.getOrderId() + "</strong> của bạn tại <strong>" + shopName + "</strong> đã được người bán xác nhận và đang được chuẩn bị giao.</p>" +
                    "<div style='margin:16px 0;padding:12px;background:#f6f9ff;border:1px solid #e3efff;border-radius:8px'>" +
                    "<p style='margin:0'><strong>Địa chỉ nhận:</strong> " + (order.getShippingAddress() != null ? order.getShippingAddress() : "(chưa có)") + "</p>" +
                    "<p style='margin:4px 0 0'><strong>Thanh toán:</strong> " + payment + "</p>" +
                    "<p style='margin:4px 0 0'><strong>Dự kiến giao:</strong> " + eta + "</p>" +
                    "</div>" +
                    "<table style='width:100%;border-collapse:collapse;margin-top:8px'>" +
                    "<thead><tr>" +
                    "<th style='text-align:left;padding:8px 12px;border-bottom:2px solid #ddd'>Sản phẩm</th>" +
                    "<th style='text-align:center;padding:8px 12px;border-bottom:2px solid #ddd'>SL</th>" +
                    "<th style='text-align:right;padding:8px 12px;border-bottom:2px solid #ddd'>Đơn giá</th>" +
                    "<th style='text-align:right;padding:8px 12px;border-bottom:2px solid #ddd'>Thành tiền</th>" +
                    "</tr></thead><tbody>" + itemsHtml + "</tbody></table>" +
                    "<p style='text-align:right;margin:12px 0;font-size:16px'><strong>Tổng cộng: " + vnd.format(total) + "</strong></p>" +
                    "<div style='margin-top:16px'>" +
                    "<a href='http://localhost:8080/my-orders' style='display:inline-block;background:#0a7cff;color:#fff;padding:10px 16px;border-radius:6px;text-decoration:none'>Theo dõi đơn hàng</a>" +
                    "</div>" +
                    "<p>Nếu bạn cần hỗ trợ, hãy phản hồi email này hoặc liên hệ CSKH.</p>" +
                    "<p style='margin-top:16px'>Trân trọng,<br/>Đội ngũ OneShop</p>" +
                    "</div>";

            sendMailService.queue(to, subject, body);
        } catch (Exception ignored) { }
    }

}
