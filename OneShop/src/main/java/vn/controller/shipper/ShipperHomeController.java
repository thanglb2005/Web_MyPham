package vn.controller.shipper;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.entity.Order;
import vn.entity.User;
import vn.repository.OrderRepository;
import vn.service.OrderService;
import vn.util.ShippingProviderHelper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller xử lý trang chủ và quản lý đơn hàng cho Shipper
 * @author OneShop Team
 */
@Controller
@RequestMapping("/shipper")
public class ShipperHomeController {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderService orderService;

    /**
     * Trang chủ shipper - hiển thị dashboard với các đơn hàng được phân công
     */
    @GetMapping("/home")
    public String shipperHome(HttpSession session, Model model) {
        User shipper = ensureShipper(session);
        if (shipper == null) {
            return "redirect:/login";
        }

        // Lấy các đơn hàng được phân công cho shipper này
        List<Order> assignedOrders = orderRepository.findByShipperOrderByOrderDateDesc(shipper);
        
        // Lấy các đơn hàng đang chờ giao (CONFIRMED) mà chưa có shipper
        List<Order> availableOrders = orderRepository.findByStatusAndShipperIsNullOrderByOrderDateDesc(
            Order.OrderStatus.CONFIRMED
        );

        // Thống kê các đơn hàng của shipper
        long totalOrders = assignedOrders.size();
        long shippingOrders = assignedOrders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.SHIPPING)
            .count();
        long deliveredOrders = assignedOrders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
            .count();

        // Thêm thông tin nhà vận chuyển
        String providerFullName = shipper.getShippingProvider() != null 
            ? ShippingProviderHelper.getFullName(shipper.getShippingProvider())
            : null;
        String providerLogo = shipper.getShippingProvider() != null
            ? ShippingProviderHelper.getLogoPath(shipper.getShippingProvider())
            : null;

        model.addAttribute("shipper", shipper);
        model.addAttribute("providerFullName", providerFullName);
        model.addAttribute("providerLogo", providerLogo);
        model.addAttribute("assignedOrders", assignedOrders);
        model.addAttribute("availableOrders", availableOrders);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("shippingOrders", shippingOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("pageTitle", "Trang chủ Shipper");

        return "shipper/home";
    }

    /**
     * Nhận đơn hàng - shipper tự nhận đơn hàng có sẵn
     */
    @PostMapping("/pickup-order")
    public String pickupOrder(@RequestParam Long orderId, HttpSession session, Model model) {
        User shipper = ensureShipper(session);
        if (shipper == null) {
            return "redirect:/login";
        }

        try {
            // Kiểm tra đơn hàng có tồn tại và chưa được phân công shipper
            Order order = orderService.getOrderById(orderId);
            if (order != null && order.getShipper() == null && 
                order.getStatus() == Order.OrderStatus.CONFIRMED) {
                
                // Phân công shipper cho đơn hàng
                orderService.assignShipper(orderId, shipper);
                
                // Cập nhật trạng thái đơn hàng sang SHIPPING
                orderService.updateOrderStatus(orderId, Order.OrderStatus.SHIPPING);
                
                model.addAttribute("success", "Đã nhận đơn hàng #" + orderId + " thành công!");
            } else {
                model.addAttribute("error", "Không thể nhận đơn hàng này!");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/shipper/home";
    }

    /**
     * Cập nhật trạng thái đơn hàng
     */
    @PostMapping("/update-status")
    public String updateOrderStatus(@RequestParam Long orderId, 
                                    @RequestParam String status,
                                    HttpSession session, 
                                    Model model) {
        User shipper = ensureShipper(session);
        if (shipper == null) {
            return "redirect:/login";
        }

        try {
            Order order = orderService.getOrderById(orderId);
            
            // Kiểm tra đơn hàng có thuộc về shipper này không
            if (order != null && order.getShipper() != null && 
                order.getShipper().getUserId().equals(shipper.getUserId())) {
                
                Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status);
                orderService.updateOrderStatus(orderId, newStatus);
                
                model.addAttribute("success", "Đã cập nhật trạng thái đơn hàng thành công!");
            } else {
                model.addAttribute("error", "Bạn không có quyền cập nhật đơn hàng này!");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/shipper/home";
    }

    /**
     * Trang danh sách đơn hàng của shipper
     */
    @GetMapping("/my-orders")
    public String myOrders(HttpSession session, Model model) {
        User shipper = ensureShipper(session);
        if (shipper == null) {
            return "redirect:/login";
        }

        List<Order> myOrders = orderRepository.findByShipperOrderByOrderDateDesc(shipper);

        // Tính toán thống kê
        long totalOrders = myOrders.size();
        long shippingOrders = myOrders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.SHIPPING)
            .count();
        long deliveredOrders = myOrders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
            .count();

        // Tạo danh sách đã lọc theo trạng thái
        List<Order> shippingOrdersList = myOrders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.SHIPPING)
            .collect(Collectors.toList());
        List<Order> deliveredOrdersList = myOrders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
            .collect(Collectors.toList());

        model.addAttribute("shipper", shipper);
        model.addAttribute("orders", myOrders);
        model.addAttribute("shippingOrders", shippingOrdersList);
        model.addAttribute("deliveredOrders", deliveredOrdersList);
        model.addAttribute("totalOrdersCount", totalOrders);
        model.addAttribute("shippingOrdersCount", shippingOrders);
        model.addAttribute("deliveredOrdersCount", deliveredOrders);
        model.addAttribute("pageTitle", "Đơn hàng của tôi");

        return "shipper/my-orders";
    }

    /**
     * Trang thống kê chi tiết cho shipper
     */
    @GetMapping("/statistics")
    public String statistics(HttpSession session, Model model) {
        User shipper = ensureShipper(session);
        if (shipper == null) {
            return "redirect:/login";
        }

        // Lấy tất cả đơn hàng của shipper
        List<Order> allOrders = orderRepository.findByShipperOrderByOrderDateDesc(shipper);

        // Thống kê tổng quan
        long totalOrders = allOrders.size();
        long shippingOrders = orderRepository.countByShipperAndStatus(shipper, Order.OrderStatus.SHIPPING);
        long deliveredOrders = orderRepository.countByShipperAndStatus(shipper, Order.OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByShipperAndStatus(shipper, Order.OrderStatus.CANCELLED);
        
        // Tổng giá trị đơn hàng đã giao
        Double totalDeliveredAmount = orderRepository.getTotalDeliveredAmountByShipper(shipper);
        if (totalDeliveredAmount == null) {
            totalDeliveredAmount = 0.0;
        }

        // Tính tỷ lệ giao hàng thành công
        double successRate = totalOrders > 0 ? (deliveredOrders * 100.0 / totalOrders) : 0.0;

        // Thống kê theo tháng
        List<Object[]> monthlyStats = orderRepository.getShipperMonthlyStatistics(shipper);
        
        // Thống kê theo trạng thái
        List<Object[]> statusStats = orderRepository.getShipperOrderStatsByStatus(shipper);

        model.addAttribute("shipper", shipper);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("shippingOrders", shippingOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("cancelledOrders", cancelledOrders);
        model.addAttribute("totalDeliveredAmount", totalDeliveredAmount);
        model.addAttribute("successRate", String.format("%.1f", successRate));
        model.addAttribute("monthlyStats", monthlyStats);
        model.addAttribute("statusStats", statusStats);
        model.addAttribute("pageTitle", "Thống kê giao hàng");

        return "shipper/statistics";
    }

    /**
     * API lấy thống kê theo tháng (JSON)
     */
    @GetMapping("/api/monthly-stats")
    @ResponseBody
    public Map<String, Object> getMonthlyStats(HttpSession session) {
        User shipper = ensureShipper(session);
        Map<String, Object> response = new HashMap<>();
        
        if (shipper == null) {
            response.put("error", "Unauthorized");
            return response;
        }

        List<Object[]> monthlyStats = orderRepository.getShipperMonthlyStatistics(shipper);
        
        List<Map<String, Object>> formattedStats = monthlyStats.stream()
            .map(stat -> {
                Map<String, Object> item = new HashMap<>();
                item.put("year", stat[0]);
                item.put("month", stat[1]);
                item.put("totalOrders", stat[2]);
                item.put("deliveredOrders", stat[3]);
                item.put("totalAmount", stat[4]);
                return item;
            })
            .collect(Collectors.toList());

        response.put("data", formattedStats);
        return response;
    }

    /**
     * API lấy thống kê theo ngày của tháng hiện tại (JSON)
     */
    @GetMapping("/api/daily-stats")
    @ResponseBody
    public Map<String, Object> getDailyStats(HttpSession session) {
        User shipper = ensureShipper(session);
        Map<String, Object> response = new HashMap<>();
        
        if (shipper == null) {
            response.put("error", "Unauthorized");
            return response;
        }

        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        List<Object[]> dailyStats = orderRepository.getShipperDailyStatistics(
            shipper, currentYear, currentMonth
        );
        
        List<Map<String, Object>> formattedStats = dailyStats.stream()
            .map(stat -> {
                Map<String, Object> item = new HashMap<>();
                item.put("day", stat[0]);
                item.put("totalOrders", stat[1]);
                item.put("deliveredOrders", stat[2]);
                return item;
            })
            .collect(Collectors.toList());

        response.put("data", formattedStats);
        response.put("month", currentMonth);
        response.put("year", currentYear);
        return response;
    }

    /**
     * API lấy thống kê theo trạng thái (JSON)
     */
    @GetMapping("/api/status-stats")
    @ResponseBody
    public Map<String, Object> getStatusStats(HttpSession session) {
        User shipper = ensureShipper(session);
        Map<String, Object> response = new HashMap<>();
        
        if (shipper == null) {
            response.put("error", "Unauthorized");
            return response;
        }

        List<Object[]> statusStats = orderRepository.getShipperOrderStatsByStatus(shipper);
        
        List<Map<String, Object>> formattedStats = statusStats.stream()
            .map(stat -> {
                Map<String, Object> item = new HashMap<>();
                String status = stat[0].toString();
                String label;
                
                // Convert status to Vietnamese label
                switch (status) {
                    case "SHIPPING":
                        label = "Đang giao";
                        break;
                    case "DELIVERED":
                        label = "Đã giao";
                        break;
                    case "CANCELLED":
                        label = "Đã hủy";
                        break;
                    default:
                        label = status;
                }
                
                item.put("status", status);
                item.put("label", label);
                item.put("count", stat[1]);
                return item;
            })
            .collect(Collectors.toList());

        response.put("data", formattedStats);
        return response;
    }

    /**
     * Xem chi tiết đơn hàng
     */
    @GetMapping("/order-detail/{orderId}")
    public String viewOrderDetail(@PathVariable Long orderId, HttpSession session, Model model) {
        User shipper = ensureShipper(session);
        if (shipper == null) {
            return "redirect:/login";
        }

        Order order = orderService.getOrderById(orderId);
        
        // Kiểm tra đơn hàng có thuộc về shipper này không
        if (order == null || order.getShipper() == null || 
            !order.getShipper().getUserId().equals(shipper.getUserId())) {
            model.addAttribute("error", "Bạn không có quyền xem đơn hàng này!");
            return "redirect:/shipper/my-orders";
        }

        // Tính tổng khối lượng từ các sản phẩm nếu chưa có
        if (order.getWeight() == null && order.getOrderDetails() != null) {
            double totalWeight = order.getOrderDetails().size() * 0.5; // Giả định mỗi sản phẩm ~0.5kg
            order.setWeight(totalWeight);
        }
        
        model.addAttribute("shipper", shipper);
        model.addAttribute("order", order);
        model.addAttribute("pageTitle", "Chi tiết đơn hàng #" + orderId);

        return "shipper/order-detail";
    }

    /**
     * Kiểm tra user có phải là shipper không
     */
    private User ensureShipper(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return null;
        }
        boolean isShipper = user.getRoles() != null &&
                user.getRoles().stream().anyMatch(role -> "ROLE_SHIPPER".equals(role.getName()));
        return isShipper ? user : null;
    }
}
