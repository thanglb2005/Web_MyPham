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
import vn.entity.Shop;
import vn.entity.User;
import vn.repository.OrderRepository;
import vn.repository.ShopRepository;
import vn.service.OrderService;
import vn.entity.OrderDetail;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Autowired
    private ShopRepository shopRepository;

    /**
     * Trang chủ shipper - hiển thị dashboard với các đơn hàng được phân công
     */
    @GetMapping("/home")
    public String shipperHome(HttpSession session, Model model) {
        User shipper = ensureShipper(session);
        if (shipper == null) {
            return "redirect:/login";
        }

        // Lấy các shop mà shipper được gán
        List<Shop> assignedShops = shopRepository.findShopsByShipper(shipper);
        
        // Xác định tên hiển thị
        String displayName = "OneShop Shipper";
        if (assignedShops != null && assignedShops.size() == 1 && assignedShops.get(0) != null) {
            Shop shop = assignedShops.get(0);
            if (shop.getShopName() != null) {
                displayName = shop.getShopName() + " - Shipper";
            }
        }

        // Lấy các đơn hàng được phân công cho shipper này
        List<Order> allAssignedOrders = orderRepository.findOrdersByShipper(shipper);
        
        // Debug: Log tất cả đơn hàng được gán
        System.out.println("All assigned orders for shipper: " + allAssignedOrders.size());
        for (Order order : allAssignedOrders) {
            System.out.println("Order #" + order.getOrderId() + " - Status: " + order.getStatus() + 
                             " - Shop: " + (order.getShop() != null ? order.getShop().getShopId() : "null"));
        }
        
        // Lấy các đơn hàng đang giao và đã giao của shipper này 
        List<Order> assignedOrders = allAssignedOrders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.SHIPPING || 
                            order.getStatus() == Order.OrderStatus.DELIVERED ||
                            order.getStatus() == Order.OrderStatus.OVERDUE)
            .collect(Collectors.toList());
        
        // Lấy các đơn hàng đang chờ giao (CONFIRMED) - ChỈ LẤY ĐƠN HỎA TỐC
        // Chỉ lấy đơn hàng từ các shop mà shipper được gán
        List<Order> availableOrders = orderRepository.findAvailableOrdersForShipper(
            shipper,
            Order.OrderStatus.CONFIRMED
        );
        
        // Thêm các đơn hàng CONFIRMED ĐÃ ĐƯỢC GÁN CHO SHIPPER HIỆN TẠI (chỉ hỏa tốc)
        // Chỉ hiển thị đơn đã được vendor gán cho shipper này
        List<Order> confirmedAssignedOrders = allAssignedOrders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.CONFIRMED 
                        && order.getDeliveryType() == Order.DeliveryType.EXPRESS
                        && order.getShipper() != null 
                        && order.getShipper().getUserId().equals(shipper.getUserId()))
            .collect(Collectors.toList());
        availableOrders.addAll(confirmedAssignedOrders);
        
        // Sắp xếp theo ngày đặt hàng (mới nhất trước)
        availableOrders.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));
        
        // Debug: Log để kiểm tra
        System.out.println("Available orders count: " + availableOrders.size());
        System.out.println("Assigned orders count: " + assignedOrders.size());
        for (Order order : availableOrders) {
            System.out.println("Available order #" + order.getOrderId() + " - Status: " + order.getStatus());
        }
        for (Order order : assignedOrders) {
            System.out.println("Assigned order #" + order.getOrderId() + " - Status: " + order.getStatus());
        }

        // Lấy các đơn hàng giao muộn của shipper
        List<Order> overdueOrders = orderService.findOverdueOrdersByShipper(shipper);

        // Thống kê các đơn hàng của shipper - chỉ status liên quan đến giao hàng
        long totalOrders = assignedOrders.size();
        long confirmedOrders = assignedOrders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.CONFIRMED)
            .count();
        long shippingOrders = assignedOrders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.SHIPPING)
            .count();
        long deliveredOrders = assignedOrders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
            .count();
        long overdueOrdersCount = orderService.countOverdueOrdersByShipper(shipper);

        model.addAttribute("shipper", shipper);
        model.addAttribute("displayName", displayName);
        model.addAttribute("assignedShops", assignedShops);
        model.addAttribute("assignedOrders", assignedOrders);
        model.addAttribute("availableOrders", availableOrders);
        model.addAttribute("overdueOrders", overdueOrders);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("confirmedOrders", confirmedOrders);
        model.addAttribute("shippingOrders", shippingOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("overdueOrdersCount", overdueOrdersCount);
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
            // Kiểm tra đơn hàng có tồn tại
            Order order = orderService.getOrderById(orderId);
            
            // Kiểm tra điều kiện: đơn hỏa tốc, đã xác nhận
            if (order != null && 
                order.getStatus() == Order.OrderStatus.CONFIRMED &&
                order.getDeliveryType() == Order.DeliveryType.EXPRESS) {
                
                // Nếu đơn chưa có shipper → Phân công cho shipper hiện tại
                // Nếu đơn đã có shipper → Kiểm tra xem có phải là shipper hiện tại không
                boolean canPickup = false;
                String reason = "";
                
                if (order.getShipper() == null) {
                    // Đơn chưa có shipper → shipper tự nhận
                    canPickup = true;
                } else if (order.getShipper().getUserId().equals(shipper.getUserId())) {
                    // Đơn đã được vendor gán cho shipper này
                    canPickup = true;
                } else {
                    // Đơn đã được gán cho shipper khác
                    canPickup = false;
                    reason = "Đơn đã được gán cho shipper khác";
                }
                
                if (canPickup) {
                    // Nếu chưa có shipper, phân công
                    if (order.getShipper() == null) {
                        orderService.assignShipper(orderId, shipper);
                    }
                    
                    // Cập nhật trạng thái đơn hàng sang SHIPPING
                    orderService.updateOrderStatus(orderId, Order.OrderStatus.SHIPPING);
                    
                    // ===== CODE CŨ (gửi mail trực tiếp — trùng với EmailOrderStatusSubscriber) =====
                    // try {
                    //     Order picked = orderService.getOrderById(orderId);
                    //     if (picked != null) {
                    //         sendOrderPickedUpEmail(picked, shipper);
                    //     }
                    // } catch (Exception ignore) {}
                    // ===== Mail HTML do OrderStatusEmailComposer + Observer (order đã có shipper sau assign) =====
                    
                    model.addAttribute("success", "Đã nhận đơn hàng #" + orderId + " thành công!");
                } else {
                    model.addAttribute("error", reason.isEmpty() ? "Không thể nhận đơn hàng này!" : reason);
                }
            } else {
                model.addAttribute("error", "Chỉ có thể nhận đơn hỏa tốc đã được xác nhận!");
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
                                    @RequestParam Order.OrderStatus status,
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
                
                // Cho phép cập nhật từ OVERDUE về DELIVERED
                if (order.getStatus() == Order.OrderStatus.OVERDUE && status == Order.OrderStatus.DELIVERED) {
                    orderService.updateOrderStatus(orderId, status);
                } else if (order.getStatus() != Order.OrderStatus.OVERDUE) {
                    orderService.updateOrderStatus(orderId, status);
                } else {
                    model.addAttribute("error", "Không thể cập nhật trạng thái đơn hàng này!");
                    return "redirect:/shipper/home";
                }
                
                // ===== CODE CŨ (gửi mail trực tiếp — trùng với EmailOrderStatusSubscriber) =====
                // if (status == Order.OrderStatus.DELIVERED) {
                //     try {
                //         Order delivered = orderService.getOrderById(orderId);
                //         if (delivered != null) {
                //             sendOrderDeliveredEmail(delivered);
                //         }
                //     } catch (Exception ignore) {}
                // }
                
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

        // Lấy các shop mà shipper được gán
        List<Shop> assignedShops = shopRepository.findShopsByShipper(shipper);
        
        // Xác định tên hiển thị
        String displayName = "OneShop Shipper";
        if (assignedShops.size() == 1) {
            displayName = assignedShops.get(0).getShopName() + " - Shipper";
        }
        
        // Tạo mô tả shop
        String shopDescription = "";
        if (assignedShops.isEmpty()) {
            shopDescription = "Chưa được phân công shop nào";
        } else if (assignedShops.size() == 1) {
            shopDescription = "Phụ trách giao hàng cho shop: " + assignedShops.get(0).getShopName();
        } else {
            shopDescription = "Phụ trách giao hàng cho " + assignedShops.size() + " shop";
        }

        List<Order> myOrders = orderRepository.findByShipperOrderByOrderDateDesc(shipper);

        // Tính toán thống kê - chỉ status liên quan đến giao hàng
        long totalOrders = myOrders.size();
        long confirmedOrders = myOrders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.CONFIRMED)
            .count();
        long shippingOrders = myOrders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.SHIPPING)
            .count();
        long deliveredOrders = myOrders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
            .count();
        long overdueOrders = myOrders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.OVERDUE)
            .count();

        // Tạo danh sách đã lọc theo trạng thái
        List<Order> confirmedOrdersList = myOrders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.CONFIRMED)
            .collect(Collectors.toList());
        List<Order> shippingOrdersList = myOrders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.SHIPPING)
            .collect(Collectors.toList());
        List<Order> deliveredOrdersList = myOrders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
            .collect(Collectors.toList());
        List<Order> overdueOrdersList = myOrders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.OVERDUE)
            .collect(Collectors.toList());

        model.addAttribute("shipper", shipper);
        model.addAttribute("displayName", displayName);
        model.addAttribute("assignedShops", assignedShops);
        model.addAttribute("shopDescription", shopDescription);
        model.addAttribute("orders", myOrders);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("confirmedOrders", confirmedOrders);
        model.addAttribute("shippingOrders", shippingOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("overdueOrders", overdueOrders);
        model.addAttribute("confirmedOrdersList", confirmedOrdersList);
        model.addAttribute("shippingOrdersList", shippingOrdersList);
        model.addAttribute("deliveredOrdersList", deliveredOrdersList);
        model.addAttribute("overdueOrdersList", overdueOrdersList);
        model.addAttribute("totalOrdersCount", totalOrders);
        model.addAttribute("shippingOrdersCount", shippingOrders);
        model.addAttribute("deliveredOrdersCount", deliveredOrders);
        model.addAttribute("overdueOrdersCount", overdueOrders);
        model.addAttribute("pageTitle", "Đơn hàng của tôi");

        return "shipper/my-orders";
    }

    /**
     * Trang thống kê chi tiết cho shipper
     */
    @GetMapping("/statistics")
    public String statistics(HttpSession session, Model model,
                            @RequestParam(required = false) Long shopId,
                            @RequestParam(required = false) String fromDate,
                            @RequestParam(required = false) String toDate) {
        User shipper = ensureShipper(session);
        if (shipper == null) {
            return "redirect:/login";
        }

        // Lấy các shop mà shipper được gán
        List<Shop> assignedShops = shopRepository.findShopsByShipper(shipper);
        
        // Xác định tên hiển thị
        String displayName = "OneShop Shipper";
        if (assignedShops.size() == 1) {
            displayName = assignedShops.get(0).getShopName() + " - Shipper";
        }
        
        // Tạo mô tả shop
        String shopDescription = "";
        if (assignedShops.isEmpty()) {
            shopDescription = "Chưa được phân công shop nào";
        } else if (assignedShops.size() == 1) {
            shopDescription = "Phụ trách giao hàng cho shop: " + assignedShops.get(0).getShopName();
        } else {
            shopDescription = "Phụ trách giao hàng cho " + assignedShops.size() + " shop";
        }

        // Parse date strings
        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;
        boolean hasDateFilter = false;

        if (fromDate != null && !fromDate.isEmpty()) {
            try {
                fromDateTime = LocalDateTime.parse(fromDate + "T00:00:00");
                hasDateFilter = true;
            } catch (Exception e) {
                // Invalid date format, ignore
            }
        }

        if (toDate != null && !toDate.isEmpty()) {
            try {
                toDateTime = LocalDateTime.parse(toDate + "T23:59:59");
                hasDateFilter = true;
            } catch (Exception e) {
                // Invalid date format, ignore
            }
        }

        // Kiểm tra nếu có filter
        List<Order> allOrders;
        long totalOrders;
        long shippingOrders;
        long deliveredOrders;
        long cancelledOrders;
        Double totalDeliveredAmount;
        List<Object[]> monthlyStats;

        if (hasDateFilter && shopId != null) {
            // Filter by both shop and date range
            allOrders = orderRepository.findByShipperAndShopAndDateRange(shipper, shopId, fromDateTime, toDateTime);
            shippingOrders = orderRepository.countByShipperAndStatusAndShopAndDateRange(shipper, Order.OrderStatus.SHIPPING, shopId, fromDateTime, toDateTime);
            deliveredOrders = orderRepository.countByShipperAndStatusAndShopAndDateRange(shipper, Order.OrderStatus.DELIVERED, shopId, fromDateTime, toDateTime);
            cancelledOrders = orderRepository.countByShipperAndStatusAndShopAndDateRange(shipper, Order.OrderStatus.CANCELLED, shopId, fromDateTime, toDateTime);
            totalDeliveredAmount = orderRepository.getTotalDeliveredAmountByShipperAndShopAndDateRange(shipper, shopId, fromDateTime, toDateTime);
            monthlyStats = orderRepository.getShipperMonthlyStatisticsByShopAndDateRange(shipper, shopId, fromDateTime, toDateTime);
            totalOrders = allOrders.size();
        } else if (hasDateFilter) {
            // Filter by date range only
            allOrders = orderRepository.findByShipperAndDateRange(shipper, fromDateTime, toDateTime);
            shippingOrders = orderRepository.countByShipperAndStatusAndDateRange(shipper, Order.OrderStatus.SHIPPING, fromDateTime, toDateTime);
            deliveredOrders = orderRepository.countByShipperAndStatusAndDateRange(shipper, Order.OrderStatus.DELIVERED, fromDateTime, toDateTime);
            cancelledOrders = orderRepository.countByShipperAndStatusAndDateRange(shipper, Order.OrderStatus.CANCELLED, fromDateTime, toDateTime);
            totalDeliveredAmount = orderRepository.getTotalDeliveredAmountByShipperAndDateRange(shipper, fromDateTime, toDateTime);
            monthlyStats = orderRepository.getShipperMonthlyStatisticsByDateRange(shipper, fromDateTime, toDateTime);
            totalOrders = allOrders.size();
        } else if (shopId != null) {
            // Filter by shop only
            allOrders = orderRepository.findByShipperAndShop(shipper, shopId);
            shippingOrders = orderRepository.countByShipperAndStatusAndShop(shipper, Order.OrderStatus.SHIPPING, shopId);
            deliveredOrders = orderRepository.countByShipperAndStatusAndShop(shipper, Order.OrderStatus.DELIVERED, shopId);
            cancelledOrders = orderRepository.countByShipperAndStatusAndShop(shipper, Order.OrderStatus.CANCELLED, shopId);
            totalDeliveredAmount = orderRepository.getTotalDeliveredAmountByShipperAndShop(shipper, shopId);
            monthlyStats = orderRepository.getShipperMonthlyStatisticsByShop(shipper, shopId);
            totalOrders = allOrders.size();
        } else {
            // No filter
            allOrders = orderRepository.findByShipperOrderByOrderDateDesc(shipper);
            shippingOrders = orderRepository.countByShipperAndStatus(shipper, Order.OrderStatus.SHIPPING);
            deliveredOrders = orderRepository.countByShipperAndStatus(shipper, Order.OrderStatus.DELIVERED);
            cancelledOrders = orderRepository.countByShipperAndStatus(shipper, Order.OrderStatus.CANCELLED);
            totalDeliveredAmount = orderRepository.getTotalDeliveredAmountByShipper(shipper);
            monthlyStats = orderRepository.getShipperMonthlyStatistics(shipper);
            totalOrders = allOrders.size();
        }

        if (totalDeliveredAmount == null) {
            totalDeliveredAmount = 0.0;
        }

        // Tính tỷ lệ giao hàng thành công
        double successRate = totalOrders > 0 ? (deliveredOrders * 100.0 / totalOrders) : 0.0;
        
        // Thống kê theo trạng thái
        List<Object[]> statusStats = orderRepository.getShipperOrderStatsByStatus(shipper);

        model.addAttribute("shipper", shipper);
        model.addAttribute("displayName", displayName);
        model.addAttribute("assignedShops", assignedShops);
        model.addAttribute("shopDescription", shopDescription);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("shippingOrders", shippingOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("cancelledOrders", cancelledOrders);
        model.addAttribute("totalDeliveredAmount", totalDeliveredAmount);
        model.addAttribute("successRate", String.format("%.1f", successRate));
        model.addAttribute("monthlyStats", monthlyStats);
        model.addAttribute("statusStats", statusStats);
        model.addAttribute("pageTitle", "Thống kê giao hàng");
        model.addAttribute("selectedShopId", shopId);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        return "shipper/statistics";
    }

    /**
     * API lấy thống kê theo tháng (JSON)
     */
    @GetMapping("/api/monthly-stats")
    @ResponseBody
    public Map<String, Object> getMonthlyStats(HttpSession session,
                                                @RequestParam(required = false) Long shopId,
                                                @RequestParam(required = false) String fromDate,
                                                @RequestParam(required = false) String toDate) {
        User shipper = ensureShipper(session);
        Map<String, Object> response = new HashMap<>();
        
        if (shipper == null) {
            response.put("error", "Unauthorized");
            return response;
        }

        // Parse date strings
        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;
        boolean hasDateFilter = false;

        if (fromDate != null && !fromDate.isEmpty()) {
            try {
                fromDateTime = LocalDateTime.parse(fromDate + "T00:00:00");
                hasDateFilter = true;
            } catch (Exception e) {
                // Invalid date format, ignore
            }
        }

        if (toDate != null && !toDate.isEmpty()) {
            try {
                toDateTime = LocalDateTime.parse(toDate + "T23:59:59");
                hasDateFilter = true;
            } catch (Exception e) {
                // Invalid date format, ignore
            }
        }

        List<Object[]> monthlyStats;
        
        if (hasDateFilter && shopId != null) {
            monthlyStats = orderRepository.getShipperMonthlyStatisticsByShopAndDateRange(shipper, shopId, fromDateTime, toDateTime);
        } else if (hasDateFilter) {
            monthlyStats = orderRepository.getShipperMonthlyStatisticsByDateRange(shipper, fromDateTime, toDateTime);
        } else if (shopId != null) {
            monthlyStats = orderRepository.getShipperMonthlyStatisticsByShop(shipper, shopId);
        } else {
            monthlyStats = orderRepository.getShipperMonthlyStatistics(shipper);
        }
        
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
    public Map<String, Object> getStatusStats(HttpSession session,
                                               @RequestParam(required = false) Long shopId,
                                               @RequestParam(required = false) String fromDate,
                                               @RequestParam(required = false) String toDate) {
        User shipper = ensureShipper(session);
        Map<String, Object> response = new HashMap<>();
        
        if (shipper == null) {
            response.put("error", "Unauthorized");
            return response;
        }

        // Parse date strings
        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;
        boolean hasDateFilter = false;

        if (fromDate != null && !fromDate.isEmpty()) {
            try {
                fromDateTime = LocalDateTime.parse(fromDate + "T00:00:00");
                hasDateFilter = true;
            } catch (Exception e) {
                // Invalid date format, ignore
            }
        }

        if (toDate != null && !toDate.isEmpty()) {
            try {
                toDateTime = LocalDateTime.parse(toDate + "T23:59:59");
                hasDateFilter = true;
            } catch (Exception e) {
                // Invalid date format, ignore
            }
        }

        // Get filtered orders
        List<Order> allOrders;
        if (hasDateFilter && shopId != null) {
            allOrders = orderRepository.findByShipperAndShopAndDateRange(shipper, shopId, fromDateTime, toDateTime);
        } else if (hasDateFilter) {
            allOrders = orderRepository.findByShipperAndDateRange(shipper, fromDateTime, toDateTime);
        } else if (shopId != null) {
            allOrders = orderRepository.findByShipperAndShop(shipper, shopId);
        } else {
            allOrders = orderRepository.findByShipperOrderByOrderDateDesc(shipper);
        }

        // Calculate status stats
        Map<String, Long> statusCounts = allOrders.stream()
            .collect(Collectors.groupingBy(
                order -> order.getStatus().name(),
                Collectors.counting()
            ));

        List<Map<String, Object>> formattedStats = new ArrayList<>();
        statusCounts.forEach((status, count) -> {
            Map<String, Object> item = new HashMap<>();
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
            item.put("count", count);
            formattedStats.add(item);
        });

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

        // Lấy các shop mà shipper được gán
        List<Shop> assignedShops = shopRepository.findShopsByShipper(shipper);
        
        // Xác định tên hiển thị
        String displayName = "OneShop Shipper";
        if (assignedShops.size() == 1) {
            displayName = assignedShops.get(0).getShopName() + " - Shipper";
        }
        
        // Tạo mô tả shop
        String shopDescription = "";
        if (assignedShops.isEmpty()) {
            shopDescription = "Chưa được phân công shop nào";
        } else if (assignedShops.size() == 1) {
            shopDescription = "Phụ trách giao hàng cho shop: " + assignedShops.get(0).getShopName();
        } else {
            shopDescription = "Phụ trách giao hàng cho " + assignedShops.size() + " shop";
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
        
        // Lấy thông tin shop của đơn hàng
        Shop orderShop = null;
        if (order.getShop() != null) {
            orderShop = order.getShop();
        } else if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            // Nếu order không có shop, lấy shop từ order details
            orderShop = order.getOrderDetails().get(0).getProduct().getShop();
        }
        
        // Cập nhật pickup address nếu chưa có
        if (order.getPickupAddress() == null && orderShop != null) {
            order.setPickupAddress(orderShop.getAddress());
        }
        
        // Debug: Log order status
        System.out.println("Order ID: " + orderId + ", Status: " + order.getStatus());
        System.out.println("Order Payment Method: " + order.getPaymentMethod());
        
        model.addAttribute("shipper", shipper);
        model.addAttribute("displayName", displayName);
        model.addAttribute("assignedShops", assignedShops);
        model.addAttribute("shopDescription", shopDescription);
        model.addAttribute("order", order);
        model.addAttribute("orderShop", orderShop);
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

    /**
     * API để test dữ liệu thật - kiểm tra đơn hàng giao muộn
     */
    @GetMapping("/test-overdue-data")
    @ResponseBody
    public Map<String, Object> testOverdueData(HttpSession session) {
        User shipper = ensureShipper(session);
        Map<String, Object> response = new HashMap<>();
        
        if (shipper == null) {
            response.put("error", "Unauthorized");
            return response;
        }

        try {
            // Test 1: Lấy tất cả đơn hàng của shipper
            List<Order> allOrders = orderRepository.findOrdersByShipper(shipper);
            
            // Test 2: Lấy đơn hàng giao muộn
            List<Order> overdueOrders = orderService.findOverdueOrdersByShipper(shipper);
            
            // Test 3: Lấy đơn hàng cần đánh dấu giao muộn
            List<Order> ordersToMark = orderService.findOrdersToMarkOverdue();
            
            // Test 4: Thống kê
            long totalOrders = allOrders.size();
            long overdueCount = orderService.countOverdueOrdersByShipper(shipper);
            
            response.put("shipper", shipper.getName());
            response.put("totalOrders", totalOrders);
            response.put("overdueCount", overdueCount);
            response.put("allOrders", allOrders.stream().map(order -> {
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("id", order.getOrderId());
                orderData.put("status", order.getStatus());
                orderData.put("estimatedDeliveryDate", order.getEstimatedDeliveryDate());
                orderData.put("orderDate", order.getOrderDate());
                orderData.put("customerName", order.getCustomerName());
                return orderData;
            }).collect(Collectors.toList()));
            
            response.put("overdueOrders", overdueOrders.stream().map(order -> {
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("id", order.getOrderId());
                orderData.put("status", order.getStatus());
                orderData.put("estimatedDeliveryDate", order.getEstimatedDeliveryDate());
                orderData.put("customerName", order.getCustomerName());
                orderData.put("isOverdue", orderService.isOrderOverdue(order));
                return orderData;
            }).collect(Collectors.toList()));
            
            response.put("ordersToMark", ordersToMark.stream().map(order -> {
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("id", order.getOrderId());
                orderData.put("status", order.getStatus());
                orderData.put("estimatedDeliveryDate", order.getEstimatedDeliveryDate());
                orderData.put("customerName", order.getCustomerName());
                orderData.put("shipper", order.getShipper() != null ? order.getShipper().getName() : "N/A");
                return orderData;
            }).collect(Collectors.toList()));
            
            response.put("currentTime", LocalDateTime.now());
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("error", "Error testing data: " + e.getMessage());
            e.printStackTrace();
        }
        
        return response;
    }

    /**
     * API để force check và đánh dấu đơn hàng giao muộn
     */
    @PostMapping("/force-check-overdue")
    @ResponseBody
    public Map<String, Object> forceCheckOverdue(HttpSession session) {
        User shipper = ensureShipper(session);
        Map<String, Object> response = new HashMap<>();
        
        if (shipper == null) {
            response.put("error", "Unauthorized");
            return response;
        }

        try {
            // Force check và đánh dấu đơn hàng giao muộn
            orderService.markOverdueOrders();
            
            // Lấy dữ liệu sau khi cập nhật
            List<Order> overdueOrders = orderService.findOverdueOrdersByShipper(shipper);
            long overdueCount = orderService.countOverdueOrdersByShipper(shipper);
            
            response.put("success", true);
            response.put("message", "Đã kiểm tra và cập nhật đơn hàng giao muộn");
            response.put("overdueCount", overdueCount);
            response.put("overdueOrders", overdueOrders.size());
            response.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            response.put("error", "Error checking overdue orders: " + e.getMessage());
            e.printStackTrace();
        }
        
        return response;
    }
}
