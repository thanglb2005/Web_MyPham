package vn.controller.vendor;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.entity.Order;
import vn.entity.Shop;
import vn.entity.User;
import vn.repository.OrderDetailRepository;
import vn.service.ProductService;
import vn.service.ShopService;

import java.util.Optional;

@Controller
@RequestMapping("/vendor")
public class VendorDashboardController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @GetMapping({"/dashboard", ""})
    public String dashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        Optional<Shop> shopOpt = shopService.findByVendor(vendor);
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", "Bạn chưa có shop. Hãy đăng ký ngay!");
            return "redirect:/vendor/shop/register";
        }

        Shop shop = shopService.refreshStatistics(shopOpt.get());

        Long shopId = shop.getShopId();
        long productCount = productService.countByShopId(shopId);
        Long pendingOrders = orderDetailRepository.countDistinctOrdersByShopAndStatus(shopId, Order.OrderStatus.PENDING);
        Long confirmedOrders = orderDetailRepository.countDistinctOrdersByShopAndStatus(shopId, Order.OrderStatus.CONFIRMED);
        Long shippingOrders = orderDetailRepository.countDistinctOrdersByShopAndStatus(shopId, Order.OrderStatus.SHIPPING);
        Long deliveredOrders = orderDetailRepository.countDistinctOrdersByShopAndStatus(shopId, Order.OrderStatus.DELIVERED);
        Long cancelledOrders = orderDetailRepository.countDistinctOrdersByShopAndStatus(shopId, Order.OrderStatus.CANCELLED);
        Double totalRevenue = orderDetailRepository.sumRevenueByShop(shopId);

        model.addAttribute("shop", shop);
        model.addAttribute("productCount", productCount);
        model.addAttribute("pendingOrders", defaultZero(pendingOrders));
        model.addAttribute("confirmedOrders", defaultZero(confirmedOrders));
        model.addAttribute("shippingOrders", defaultZero(shippingOrders));
        model.addAttribute("deliveredOrders", defaultZero(deliveredOrders));
        model.addAttribute("cancelledOrders", defaultZero(cancelledOrders));
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        model.addAttribute("pageTitle", "Bảng điều khiển shop");

        return "vendor/dashboard";
    }

    private long defaultZero(Long value) {
        return value != null ? value : 0L;
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

