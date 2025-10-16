package vn.controller.admin;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.ModelMap;
import vn.entity.Order;
import vn.entity.OrderDetail;
import vn.entity.Shop;
import vn.entity.User;
import vn.repository.OrderRepository;
import vn.repository.OrderDetailRepository;
import vn.repository.ShopRepository;
import vn.repository.UserRepository;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for Order management in admin panel
 * @author OneShop Team
 */
@Controller
@RequestMapping("/admin")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute(value = "user")
    public User user(Model model, Principal principal) {
        User user = null;
        if (principal != null) {
            user = userRepository.findByEmail(principal.getName()).orElse(null);
            model.addAttribute("user", user);
        }
        return user;
    }

    // List all orders with pagination support
    @GetMapping(value = "/orders")
    public String orders(Model model, Principal principal,
                        @RequestParam(value = "orderStatus", required = false) Order.OrderStatus orderStatus,
                        @RequestParam(value = "shopId", required = false) Long shopId,
                        @RequestParam(value = "search", required = false) String search,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "10") int size) {
        
        // Validate size parameter
        if (size <= 0 || size > 100) {
            size = 10;
        }
        
        List<Order> allOrders = orderRepository.findAll();
        
        // Filter by status if provided
        if (orderStatus != null) {
            allOrders = allOrders.stream()
                .filter(order -> order.getStatus() == orderStatus)
                .collect(Collectors.toList());
        }
        
        // Filter by shop if provided
        if (shopId != null) {
            allOrders = allOrders.stream()
                .filter(order -> order.getShop() != null && order.getShop().getShopId().equals(shopId))
                .collect(Collectors.toList());
        }
        
        // Filter by search term if provided
        if (search != null && !search.trim().isEmpty()) {
            String searchTerm = search.trim().toLowerCase();
            allOrders = allOrders.stream()
                .filter(order -> {
                    // Search by customer name
                    boolean matchesName = order.getCustomerName() != null &&
                                            order.getCustomerName().toLowerCase().contains(searchTerm);

                    // Search by customer email
                    boolean matchesEmail = order.getCustomerEmail() != null &&
                                             order.getCustomerEmail().toLowerCase().contains(searchTerm);
                    
                    // Search by phone number
                    boolean matchesPhone = order.getCustomerPhone() != null && 
                                         order.getCustomerPhone().contains(search);
                    
                    // Search by order ID
                    boolean matchesOrderId = order.getOrderId().toString().contains(search);
                    
                    // Search by address
                    boolean matchesAddress = order.getShippingAddress() != null && 
                                           order.getShippingAddress().toLowerCase().contains(searchTerm);
                    
                    return matchesName || matchesEmail || matchesPhone || matchesOrderId || matchesAddress;
                })
                .collect(Collectors.toList());
        }
        
        // Sort orders by order date descending
        allOrders = allOrders.stream()
            .sorted((o1, o2) -> {
                if (o1.getOrderDate() == null && o2.getOrderDate() == null) return 0;
                if (o1.getOrderDate() == null) return 1;
                if (o2.getOrderDate() == null) return -1;
                return o2.getOrderDate().compareTo(o1.getOrderDate());
            })
            .collect(Collectors.toList());
        
        // Calculate pagination
        int totalItems = allOrders.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        // Ensure page is within bounds
        if (page >= totalPages && totalPages > 0) {
            page = totalPages - 1;
        }
        if (page < 0) {
            page = 0;
        }
        
        // Get paginated results
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalItems);
        
        List<Order> paginatedOrders = allOrders.subList(startIndex, endIndex);
        
        // Calculate simple status statistics (on filtered set before pagination)
        long pendingCount = allOrders.stream()
            .filter(o -> o.getStatus() != null && o.getStatus() == Order.OrderStatus.PENDING)
            .count();
        long shippingCount = allOrders.stream()
            .filter(o -> o.getStatus() != null && o.getStatus() == Order.OrderStatus.SHIPPING)
            .count();
        long deliveredCount = allOrders.stream()
            .filter(o -> o.getStatus() != null && o.getStatus() == Order.OrderStatus.DELIVERED)
            .count();
        
        // Get all shops for filter dropdown
        List<Shop> allShops = shopRepository.findAll();
        
        // Add pagination attributes
        model.addAttribute("orderDetails", paginatedOrders);
        model.addAttribute("selectedStatus", orderStatus);
        model.addAttribute("selectedShopId", shopId);
        model.addAttribute("allShops", allShops);
        model.addAttribute("searchTerm", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("startIndex", startIndex + 1);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("hasPrev", page > 0);
        model.addAttribute("hasNext", page < totalPages - 1);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("shippingCount", shippingCount);
        model.addAttribute("deliveredCount", deliveredCount);
        
        return "admin/orders";
    }

    // Debug endpoint to check order details data
    @GetMapping("/debug/order/{order_id}")
    @ResponseBody
    public String debugOrderDetails(@PathVariable("order_id") Long id) {
        List<OrderDetail> listO = orderDetailRepository.findByOrderIdWithProductAndShop(id);
        StringBuilder result = new StringBuilder();
        result.append("Order ID: ").append(id).append("<br>");
        result.append("Order Details Count: ").append(listO.size()).append("<br><br>");
        
        for (OrderDetail detail : listO) {
            result.append("Detail ID: ").append(detail.getOrderDetailId()).append("<br>");
            result.append("Product Name: ").append(detail.getProductName()).append("<br>");
            result.append("Unit Price: ").append(detail.getUnitPrice()).append("<br>");
            result.append("Quantity: ").append(detail.getQuantity()).append("<br>");
            result.append("Total Price: ").append(detail.getTotalPrice()).append("<br>");
            if (detail.getProduct() != null) {
                result.append("Product Image: ").append(detail.getProduct().getProductImage()).append("<br>");
            }
            result.append("---<br>");
        }
        
        return result.toString();
    }

    // Order detail view
    @GetMapping("/order/detail/{order_id}")
    public ModelAndView detail(ModelMap model, @PathVariable("order_id") Long id) {
        // Use eager loading to avoid LazyInitializationException
        List<OrderDetail> listO = orderDetailRepository.findByOrderIdWithProductAndShop(id);
        
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            model.addAttribute("amount", orderOpt.get().getTotalAmount());
        }
        
        model.addAttribute("orderDetail", listO);
        model.addAttribute("orderId", id);
        model.addAttribute("menuO", "menu");
        return new ModelAndView("admin/editOrder", model);
    }

    // Cancel order
    @GetMapping("/order/cancel/{order_id}")
    public String cancelOrder(@PathVariable("order_id") Long orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        orderOptional.ifPresent(order -> {
            order.setStatus(Order.OrderStatus.CANCELLED);
            order.setCancelledDate(LocalDateTime.now());
            orderRepository.save(order);
        });
        return "redirect:/admin/orders?success=cancelled";
    }

    // Confirm order
    @GetMapping("/order/confirm/{order_id}")
    public String confirmOrder(@PathVariable("order_id") Long orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        orderOptional.ifPresent(order -> {
            order.setStatus(Order.OrderStatus.CONFIRMED);
            orderRepository.save(order);
        });
        return "redirect:/admin/orders?success=confirmed";
    }

    // Mark order as delivered
    @GetMapping("/order/delivered/{order_id}")
    public String deliveredOrder(@PathVariable("order_id") Long orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        orderOptional.ifPresent(order -> {
            order.setStatus(Order.OrderStatus.DELIVERED);
            order.setDeliveredDate(LocalDateTime.now());
            orderRepository.save(order);
        });
        return "redirect:/admin/orders?success=delivered";
    }

    // Export orders to Excel
    @GetMapping(value = "/export")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        List<Order> listOrders = orderRepository.findAll();
        
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=orders.csv");
        
        // Create Excel-compatible content with proper formatting
        StringBuilder excelContent = new StringBuilder();
        
        // Add BOM for UTF-8 to ensure proper encoding
        excelContent.append("\uFEFF");
        
        // Header row with proper CSV formatting
        excelContent.append("ID,Ngày đặt hàng,Khách hàng,Số điện thoại,Địa chỉ,Trạng thái,Tổng tiền\n");
        
        for (Order order : listOrders) {
            excelContent.append(order.getOrderId()).append(",");
            excelContent.append(order.getOrderDate()).append(",");
            
            // Escape customer name if it contains commas
            String customerName = order.getUser() != null ? order.getUser().getName() : "N/A";
            if (customerName.contains(",")) {
                excelContent.append("\"").append(customerName).append("\"");
            } else {
                excelContent.append(customerName);
            }
            excelContent.append(",");
            
            excelContent.append(order.getCustomerPhone()).append(",");
            
            // Escape address if it contains commas
            String address = order.getShippingAddress();
            if (address.contains(",")) {
                excelContent.append("\"").append(address).append("\"");
            } else {
                excelContent.append(address);
            }
            excelContent.append(",");
            
            String status = "";
            switch (order.getStatus()) {
                case PENDING: status = "Chờ xác nhận"; break;
                case CONFIRMED: status = "Đã xác nhận"; break;
                case DELIVERED: status = "Đã giao hàng"; break;
                case CANCELLED: status = "Đã hủy"; break;
                default: status = "Không xác định"; break;
            }
            excelContent.append(status).append(",");
            excelContent.append(order.getTotalAmount()).append("\n");
        }
        
        // Write Excel content to response
        response.getWriter().write(excelContent.toString());
        response.getWriter().flush();
    }

    // Order statistics
    @GetMapping(value = "/order-stats")
    public String orderStats(Model model, Principal principal) {
        List<Order> allOrders = orderRepository.findAll();
        
        // Calculate statistics - Admin chỉ cần thống kê tổng quan
        long totalOrders = allOrders.size();
        long pendingOrders = allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.PENDING).count();
        long confirmedOrders = allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.CONFIRMED).count();
        long deliveredOrders = allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED).count();
        long cancelledOrders = allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED).count();
        
        double totalRevenue = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED) // Only delivered orders
            .mapToDouble(Order::getTotalAmount)
            .sum();
        
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("confirmedOrders", confirmedOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("cancelledOrders", cancelledOrders);
        model.addAttribute("totalRevenue", totalRevenue);
        
        return "admin/order-stats";
    }
}
