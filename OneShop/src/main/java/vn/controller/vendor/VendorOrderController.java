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
import vn.service.ShopService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
                model.addAttribute("statusCounts", new LinkedHashMap<>());
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
            
            // Thống kê theo trạng thái
            Map<Order.OrderStatus, Long> statusCounts = getOrderCountsByStatus(targetShopIds);
            System.out.println("DEBUG: Status counts: " + statusCounts);
            Long totalOrders = orderService.countByShopIdIn(targetShopIds);
            System.out.println("DEBUG: Total orders: " + totalOrders);
            
            model.addAttribute("orders", orders);
            model.addAttribute("statusCounts", statusCounts);
            model.addAttribute("currentStatus", status != null ? status.name() : null);
            model.addAttribute("totalOrders", totalOrders != null ? totalOrders : 0L);
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
            model.addAttribute("statusCounts", new LinkedHashMap<>());
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

    /**
     * Helper: Lấy số lượng đơn hàng theo từng trạng thái
     */
    private Map<Order.OrderStatus, Long> getOrderCountsByStatus(List<Long> shopIds) {
        Map<Order.OrderStatus, Long> statusCounts = new LinkedHashMap<>();
        // Khởi tạo tất cả các trạng thái với giá trị 0
        for (Order.OrderStatus status : Order.OrderStatus.values()) {
            statusCounts.put(status, 0L);
        }
        
        // Lấy số lượng từ repository và cập nhật map
        List<Object[]> results = orderService.countOrdersByStatus(shopIds);
        for (Object[] result : results) {
            Order.OrderStatus status = (Order.OrderStatus) result[0];
            Long count = (Long) result[1];
            statusCounts.put(status, count);
        }
        return statusCounts;
    }
}
