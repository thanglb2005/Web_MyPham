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
            Order.OrderStatus.RETURNED_REFUNDED
    };

    /**
     * Danh sách đơn hàng với tabs theo trạng thái
     */
    @GetMapping
    public String orderList(
            @RequestParam(value = "status", required = false) Order.OrderStatus status,
            @RequestParam(value = "q", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            HttpSession session, Model model) {
        
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        // Lấy shop của vendor
        List<Long> shopIds = getShopIdsByVendor(vendor);
        if (shopIds.isEmpty()) {
            model.addAttribute("error", "Bạn chưa có shop nào.");
            return "vendor/orders/list";
        }

        // Phân trang
        Pageable pageable = PageRequest.of(page, size);
        
        // Lấy danh sách đơn hàng
        String normalizedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        
        Page<Order> orders = getOrdersByShops(shopIds, status, normalizedSearch, pageable);
        
        // Thống kê theo trạng thái
        Map<Order.OrderStatus, Long> statusCounts = getOrderCountsByStatus(shopIds);
        Long totalOrders = orderService.countByShopIdIn(shopIds);
        
        model.addAttribute("orders", orders);
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("currentStatus", status);
        model.addAttribute("totalOrders", totalOrders != null ? totalOrders : 0L);
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("vendor", vendor);
        model.addAttribute("pageTitle", "Quản lý đơn hàng");
        
        return "vendor/orders/list";
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
     * Duyệt yêu cầu trả/hoàn (DELIVERED → RETURNED_REFUNDED)
     */
    @PostMapping("/{orderId}/approve-return")
    public String approveReturn(@PathVariable Long orderId,
                               @RequestParam Double refundAmount,
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
            
            orderService.updateOrderStatus(orderId, Order.OrderStatus.RETURNED_REFUNDED);
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
            return orderService.findByShopIdInAndStatusAndOrderIdContaining(shopIds, status, trimmedSearch, pageable);
        } else if (status != null) {
            return orderService.findByShopIdInAndStatus(shopIds, status, pageable);
        } else if (trimmedSearch != null) {
            return orderService.findByShopIdInAndOrderIdContaining(shopIds, trimmedSearch, pageable);
        } else {
            return orderService.findByShopIdIn(shopIds, pageable);
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
     * Thong ke so luong don hang theo tung trang thai (doc lap voi bo loc hien tai)
     */
    private Map<Order.OrderStatus, Long> getOrderCountsByStatus(List<Long> shopIds) {
        Map<Order.OrderStatus, Long> counts = new LinkedHashMap<>();
        for (Order.OrderStatus tabStatus : STATUS_TABS) {
            Long count = shopIds.isEmpty() ? 0L : orderService.countByShopIdInAndStatus(shopIds, tabStatus);
            counts.put(tabStatus, count != null ? count : 0L);
        }
        return counts;
    }
}
