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
import vn.entity.Product;
import vn.entity.User;
import vn.repository.OrderRepository;
import vn.repository.OrderDetailRepository;
import vn.repository.ProductRepository;
import vn.repository.UserRepository;

import java.io.IOException;
import java.security.Principal;
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
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute(value = "user")
    public User user(Model model, Principal principal, User user) {
        if (principal != null) {
            model.addAttribute("user", new User());
            user = userRepository.findByEmail(principal.getName()).orElse(null);
            model.addAttribute("user", user);
        }
        return user;
    }

    // List all orders with pagination support
    @GetMapping(value = "/orders")
    public String orders(Model model, Principal principal,
                        @RequestParam(value = "status", required = false) Order.OrderStatus status,
                        @RequestParam(value = "search", required = false) String search,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "10") int size) {
        List<Order> orderDetails;
        
        if (status != null) {
            orderDetails = orderRepository.findByStatusOrderByOrderDateDesc(status);
        } else {
            orderDetails = orderRepository.findAll();
        }
        
        // Filter by search term if provided
        if (search != null && !search.trim().isEmpty()) {
            String searchTerm = search.trim().toLowerCase();
            orderDetails = orderDetails.stream()
                .filter(order -> {
                    // Search by customer name
                    boolean matchesName = order.getUser() != null && 
                                        order.getUser().getName() != null && 
                                        order.getUser().getName().toLowerCase().contains(searchTerm);
                    
                    // Search by customer email
                    boolean matchesEmail = order.getUser() != null && 
                                         order.getUser().getEmail() != null && 
                                         order.getUser().getEmail().toLowerCase().contains(searchTerm);
                    
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
        orderDetails = orderDetails.stream()
            .sorted((o1, o2) -> {
                if (o1.getOrderDate() == null && o2.getOrderDate() == null) return 0;
                if (o1.getOrderDate() == null) return 1;
                if (o2.getOrderDate() == null) return -1;
                return o2.getOrderDate().compareTo(o1.getOrderDate());
            })
            .collect(Collectors.toList());
        
        // Calculate pagination
        int totalItems = orderDetails.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        // Ensure page is within bounds
        if (page >= totalPages) page = Math.max(0, totalPages - 1);
        
        // Get paginated results
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalItems);
        
        List<Order> paginatedOrders = orderDetails.subList(startIndex, endIndex);
        
        // Add pagination attributes
        model.addAttribute("orderDetails", paginatedOrders);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("searchTerm", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("startIndex", startIndex + 1);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("hasPrev", page > 0);
        model.addAttribute("hasNext", page < totalPages - 1);
        
        return "admin/orders";
    }

    // Order detail view
    @GetMapping("/order/detail/{order_id}")
    public ModelAndView detail(ModelMap model, @PathVariable("order_id") Long id) {
        List<OrderDetail> listO = orderDetailRepository.findByOrderId(id);
        
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
    @RequestMapping("/order/cancel/{order_id}")
    public ModelAndView cancel(ModelMap model, @PathVariable("order_id") Long id) {
        Optional<Order> o = orderRepository.findById(id);
        if (o.isPresent()) {
            Order oReal = o.get();
            oReal.setStatus(Order.OrderStatus.CANCELLED); // Đã hủy
            orderRepository.save(oReal);
        }
        return new ModelAndView("forward:/admin/orders", model);
    }

    // Confirm order
    @RequestMapping("/order/confirm/{order_id}")
    public ModelAndView confirm(ModelMap model, @PathVariable("order_id") Long id) {
        Optional<Order> o = orderRepository.findById(id);
        if (o.isPresent()) {
            Order oReal = o.get();
            oReal.setStatus(Order.OrderStatus.CONFIRMED); // Đã xác nhận
            orderRepository.save(oReal);
        }
        return new ModelAndView("forward:/admin/orders", model);
    }

    // Mark as delivered
    @RequestMapping("/order/delivered/{order_id}")
    public ModelAndView delivered(ModelMap model, @PathVariable("order_id") Long id) {
        Optional<Order> o = orderRepository.findById(id);
        if (o.isPresent()) {
            Order oReal = o.get();
            oReal.setStatus(Order.OrderStatus.DELIVERED); // Đã giao hàng
            orderRepository.save(oReal);

            // Update product quantities
            Product p = null;
            List<OrderDetail> listDe = orderDetailRepository.findByOrderId(id);
            for (OrderDetail od : listDe) {
                p = od.getProduct();
                p.setQuantity(p.getQuantity() - od.getQuantity());
                productRepository.save(p);
            }
        }
        return new ModelAndView("forward:/admin/orders", model);
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
