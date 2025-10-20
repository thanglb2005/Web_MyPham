package vn.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.entity.Order;
import vn.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeliveryOverdueService {

    @Autowired
    private OrderService orderService;

    @Scheduled(fixedRate = 1800000)
    public void checkAndMarkOverdueOrders() {
        try {
            System.out.println("=== Checking for overdue orders at " + LocalDateTime.now() + " ===");
            
            List<Order> overdueOrders = orderService.findOverdueOrders();
            
            if (overdueOrders.isEmpty()) {
                System.out.println("No overdue orders found.");
                return;
            }
            
            System.out.println("Found " + overdueOrders.size() + " overdue orders:");
            for (Order order : overdueOrders) {
                System.out.println("- Order #" + order.getOrderId() + 
                                 " | Shipper: " + (order.getShipper() != null ? order.getShipper().getName() : "N/A") +
                                 " | Estimated: " + order.getEstimatedDeliveryDate() +
                                 " | Current status: " + order.getStatus());
            }
            
            orderService.markOverdueOrders();
            
        } catch (Exception e) {
            System.err.println("Error checking overdue orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 86400000)
    public void sendOverdueReport() {
        try {
            System.out.println("=== Generating overdue delivery report at " + LocalDateTime.now() + " ===");
            
            List<Order> overdueOrders = orderService.findOverdueOrders();
            
            if (overdueOrders.isEmpty()) {
                System.out.println("No overdue orders to report.");
                return;
            }
            
            System.out.println("=== OVERDUE DELIVERY REPORT ===");
            System.out.println("Total overdue orders: " + overdueOrders.size());
            System.out.println("Report generated at: " + LocalDateTime.now());
            
            overdueOrders.stream()
                .filter(order -> order.getShipper() != null)
                .collect(java.util.stream.Collectors.groupingBy(Order::getShipper))
                .forEach((shipper, orders) -> {
                    System.out.println("\nShipper: " + shipper.getName() + " - " + orders.size() + " overdue orders");
                    for (Order order : orders) {
                        System.out.println("  - Order #" + order.getOrderId() + 
                                         " | Estimated: " + order.getEstimatedDeliveryDate());
                    }
                });
            
            System.out.println("=== END REPORT ===");
            
        } catch (Exception e) {
            System.err.println("Error generating overdue report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Order> getCurrentOverdueOrders() {
        return orderService.findOverdueOrders();
    }

    public void forceCheckOverdueOrders() {
        checkAndMarkOverdueOrders();
    }
}
