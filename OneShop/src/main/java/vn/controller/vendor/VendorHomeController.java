package vn.controller.vendor;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.entity.Shop;
import vn.entity.User;
import vn.service.ShopService;
import vn.service.ProductService;
import vn.repository.OrderRepository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/vendor")
public class VendorHomeController {

    @Autowired
    private ShopService shopService;
    
    @Autowired
    private ProductService productService;

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/home")
    public String vendorHome(HttpSession session, Model model) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        List<Shop> shops = shopService.findAllByVendor(vendor);
        // Cập nhật lại tổng doanh thu theo đơn DELIVERED cho từng shop
        for (Shop shop : shops) {
            try {
                Double delivered = orderRepository.sumDeliveredRevenueByShopId(shop.getShopId());
                shop.setTotalRevenue(delivered != null ? delivered : 0.0);
            } catch (Exception ignored) {}
        }

        int totalProductCount = shops.stream()
                .mapToInt(shop -> shop.getTotalProducts() != null ? shop.getTotalProducts() : 0)
                .sum();
        int totalOrderCount = shops.stream()
                .mapToInt(shop -> shop.getTotalOrders() != null ? shop.getTotalOrders() : 0)
                .sum();
        double totalRevenueAmount = shops.stream()
                .mapToDouble(shop -> shop.getTotalRevenue() != null ? shop.getTotalRevenue() : 0.0)
                .sum();

        model.addAttribute("vendor", vendor);
        model.addAttribute("shops", shops);
        model.addAttribute("hasShops", !shops.isEmpty());
        model.addAttribute("totalShops", shops.size());
        model.addAttribute("totalProductCount", totalProductCount);
        model.addAttribute("totalOrderCount", totalOrderCount);
        model.addAttribute("totalRevenueAmount", totalRevenueAmount);
        model.addAttribute("pageTitle", "Trang chủ nhà bán");

        return "vendor/home";
    }

    @GetMapping("/my-shops")
    public String myShops(HttpSession session, Model model) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        List<Shop> shops = shopService.findAllByVendor(vendor);
        // Sync revenue to delivered orders for each shop
        for (Shop shop : shops) {
            try {
                Double delivered = orderRepository.sumDeliveredRevenueByShopId(shop.getShopId());
                shop.setTotalRevenue(delivered != null ? delivered : 0.0);
            } catch (Exception ignored) {}
        }
        // Aggregate total revenue across all shops
        double totalRevenueAmount = shops.stream()
                .mapToDouble(s -> s.getTotalRevenue() != null ? s.getTotalRevenue() : 0.0)
                .sum();
        
        if (shops.isEmpty()) {
            model.addAttribute("error", "Bạn chưa có shop nào. Hãy đăng ký shop mới!");
            return "vendor/home";
        }
        
        model.addAttribute("vendor", vendor);
        model.addAttribute("shops", shops);
        model.addAttribute("totalShops", shops.size());
        model.addAttribute("totalRevenueAmount", totalRevenueAmount);
        model.addAttribute("pageTitle", "Danh sách shop của tôi");
        
        return "vendor/my-shops";
    }


    private User ensureVendor(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return null;
        }
        boolean isVendor = user.getRoles() != null &&
                user.getRoles().stream().anyMatch(role -> "ROLE_VENDOR".equals(role.getName()));
        return isVendor ? user : null;
    }
}
