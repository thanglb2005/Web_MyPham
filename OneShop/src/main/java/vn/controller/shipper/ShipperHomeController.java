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
import vn.repository.OrderDetailRepository;
import vn.repository.ShopRepository;
import vn.service.OrderService;
import vn.service.SendMailService;
import vn.entity.OrderDetail;
import java.text.NumberFormat;
import java.util.Locale;

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

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private SendMailService sendMailService;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    /**
     * Kiểm tra địa chỉ có phải là TP.HCM không
     */
    private boolean isHoChiMinhCity(String address) {
        if (address == null) return false;
        String lowerAddress = address.toLowerCase();
        return lowerAddress.contains("hồ chí minh") || 
               lowerAddress.contains("ho chi minh") ||
               lowerAddress.contains("tp.hcm") ||
               lowerAddress.contains("tphcm") ||
               lowerAddress.contains("sài gòn") ||
               lowerAddress.contains("sai gon") ||
               lowerAddress.contains("quận 1") ||
               lowerAddress.contains("quận 2") ||
               lowerAddress.contains("quận 3") ||
               lowerAddress.contains("quận 4") ||
               lowerAddress.contains("quận 5") ||
               lowerAddress.contains("quận 6") ||
               lowerAddress.contains("quận 7") ||
               lowerAddress.contains("quận 8") ||
               lowerAddress.contains("quận 9") ||
               lowerAddress.contains("quận 10") ||
               lowerAddress.contains("quận 11") ||
               lowerAddress.contains("quận 12") ||
               lowerAddress.contains("thủ đức") ||
               lowerAddress.contains("bình thạnh") ||
               lowerAddress.contains("tân bình") ||
               lowerAddress.contains("tân phú") ||
               lowerAddress.contains("phú nhuận") ||
               lowerAddress.contains("gò vấp") ||
               lowerAddress.contains("bình tân") ||
               lowerAddress.contains("hóc môn") ||
               lowerAddress.contains("củ chi") ||
               lowerAddress.contains("bình chánh") ||
               lowerAddress.contains("nhà bè") ||
               lowerAddress.contains("cần giờ");
    }

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
        List<Order> assignedOrders = orderRepository.findOrdersByShipper(shipper);
        
        // Lấy các đơn hàng đang chờ giao (CONFIRMED) mà chưa có shipper
        // Chỉ lấy đơn hàng từ các shop mà shipper được gán
        List<Order> availableOrders = orderRepository.findAvailableOrdersForShipper(
            shipper,
            Order.OrderStatus.CONFIRMED
        );

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
            // Kiểm tra đơn hàng có tồn tại và chưa được phân công shipper
            Order order = orderService.getOrderById(orderId);
            if (order != null && order.getShipper() == null && 
                order.getStatus() == Order.OrderStatus.CONFIRMED) {
                
                // Phân công shipper cho đơn hàng
                orderService.assignShipper(orderId, shipper);
                
                // Cập nhật trạng thái đơn hàng sang SHIPPING
                orderService.updateOrderStatus(orderId, Order.OrderStatus.SHIPPING);
                
                // Gửi email thông báo đã nhận đơn tới khách hàng
                try {
                    Order picked = orderService.getOrderById(orderId);
                    if (picked != null) {
                        sendOrderPickedUpEmail(picked, shipper);
                    }
                } catch (Exception ignore) {}
                
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
                
                if (status == Order.OrderStatus.DELIVERED) {
                    try {
                        Order delivered = orderService.getOrderById(orderId);
                        if (delivered != null) {
                            sendOrderDeliveredEmail(delivered);
                        }
                    } catch (Exception ignore) {}
                }
                
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
    public String statistics(HttpSession session, Model model) {
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

    private void sendOrderDeliveredEmail(Order order) {
        try {
            String to = order.getCustomerEmail() != null ? order.getCustomerEmail() :
                    (order.getUser() != null ? order.getUser().getEmail() : null);
            if (to == null || to.isEmpty()) return;

            String shopName = (order.getShop() != null && order.getShop().getShopName() != null)
                    ? order.getShop().getShopName() : "OneShop";
            String subject = "Giao hàng thành công - Đơn #" + order.getOrderId() + " - " + shopName;

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

            String tracking = order.getTrackingNumber() != null ? order.getTrackingNumber() : "(chưa có)";
            String deliveredAt = order.getDeliveredDate() != null ? order.getDeliveredDate().toString() : "hôm nay";

            String body = "" +
                    "<div style='font-family:Arial,Helvetica,sans-serif;line-height:1.6;color:#111'>" +
                    "<h2 style='color:#16a34a;margin:0 0 12px'>Giao hàng thành công ✅</h2>" +
                    "<p>Chào " + (order.getCustomerName() != null ? order.getCustomerName() : "bạn") + ",</p>" +
                    "<p>Đơn hàng <strong>#" + order.getOrderId() + "</strong> của bạn đã được giao thành công vào <strong>" + deliveredAt + "</strong>.</p>" +
                    "<div style='margin:16px 0;padding:12px;background:#ecfdf5;border:1px solid #86efac;border-radius:8px'>" +
                    "<p style='margin:0'><strong>Người giao:</strong> Shipper OneShop</p>" +
                    "<p style='margin:4px 0 0'><strong>Mã vận đơn:</strong> " + tracking + "</p>" +
                    "<p style='margin:4px 0 0'><strong>Địa chỉ nhận:</strong> " + (order.getShippingAddress() != null ? order.getShippingAddress() : "(chưa có)") + "</p>" +
                    "<p style='margin:4px 0 0'><strong>Thanh toán:</strong> " + payment + "</p>" +
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
                    "<a href='http://localhost:8080/review?orderId=" + order.getOrderId() + "' style='display:inline-block;background:#0ea5e9;color:#fff;padding:10px 16px;border-radius:6px;text-decoration:none'>Đánh giá sản phẩm</a>" +
                    " <a href='http://localhost:8080/my-orders' style='display:inline-block;margin-left:8px;background:#374151;color:#fff;padding:10px 16px;border-radius:6px;text-decoration:none'>Xem đơn hàng</a>" +
                    "</div>" +
                    "<p style='margin-top:16px'>Cảm ơn bạn đã mua sắm tại <strong>" + shopName + "</strong>. Hẹn gặp lại bạn trong những lần sau!</p>" +
                    "<p style='margin-top:16px'>Trân trọng,<br/>Đội ngũ OneShop</p>" +
                    "</div>";

            sendMailService.queue(to, subject, body);
        } catch (Exception ignored) { }
    }

    private void sendOrderPickedUpEmail(Order order, User shipper) {
        try {
            String to = order.getCustomerEmail() != null ? order.getCustomerEmail() :
                    (order.getUser() != null ? order.getUser().getEmail() : null);
            if (to == null || to.isEmpty()) return;

            String shopName = (order.getShop() != null && order.getShop().getShopName() != null)
                    ? order.getShop().getShopName() : "OneShop";
            String subject = "Shipper đã nhận đơn - #" + order.getOrderId() + " - " + shopName;

            NumberFormat vnd = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            double total = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
            String payment = order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "COD";

            String contactPhone = (order.getShop() != null && order.getShop().getPhoneNumber() != null)
                    ? order.getShop().getPhoneNumber() : "(chưa có)";
            String shipperName = shipper != null && shipper.getName() != null ? shipper.getName() : "Shipper OneShop";

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

            String body = "" +
                    "<div style='font-family:Arial,Helvetica,sans-serif;line-height:1.6;color:#111'>" +
                    "<h2 style='color:#0ea5e9;margin:0 0 12px'>Shipper đã nhận đơn 🚚</h2>" +
                    "<p>Chào " + (order.getCustomerName() != null ? order.getCustomerName() : "bạn") + ",</p>" +
                    "<p>Đơn hàng <strong>#" + order.getOrderId() + "</strong> của bạn đã được shipper tiếp nhận và sẽ sớm giao đến bạn.</p>" +
                    "<div style='margin:16px 0;padding:12px;background:#eff6ff;border:1px solid #bfdbfe;border-radius:8px'>" +
                    "<p style='margin:0'><strong>Shipper phụ trách:</strong> " + shipperName + "</p>" +
                    "<p style='margin:4px 0 0'><strong>Liên hệ shop:</strong> " + contactPhone + "</p>" +
                    "<p style='margin:4px 0 0'><strong>Địa chỉ nhận:</strong> " + (order.getShippingAddress() != null ? order.getShippingAddress() : "(chưa có)") + "</p>" +
                    "<p style='margin:4px 0 0'><strong>Thanh toán:</strong> " + payment + "</p>" +
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
                    "<p style='margin-top:16px'>Cảm ơn bạn đã mua sắm tại <strong>" + shopName + "</strong>.</p>" +
                    "<p style='margin-top:16px'>Trân trọng,<br/>Đội ngũ OneShop</p>" +
                    "</div>";

            sendMailService.queue(to, subject, body);
        } catch (Exception ignored) { }
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
