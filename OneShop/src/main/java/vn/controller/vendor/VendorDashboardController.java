package vn.controller.vendor;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.entity.Order;
import vn.entity.Product;
import vn.entity.Shop;
import vn.entity.User;
import vn.repository.OrderDetailRepository;
import vn.service.ProductService;
import vn.service.ShopService;

import java.util.List;
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
    public String dashboard(@RequestParam(value = "shopId", required = false) Long shopId,
                           HttpSession session, 
                           Model model, 
                           RedirectAttributes redirectAttributes) {
        return handleDashboard(shopId, session, model, redirectAttributes);
    }

    @GetMapping("/dashboard/{shopId}")
    public String dashboardWithPath(@PathVariable Long shopId,
                                   HttpSession session, 
                                   Model model, 
                                   RedirectAttributes redirectAttributes) {
        return handleDashboard(shopId, session, model, redirectAttributes);
    }

    private String handleDashboard(Long shopId,
                                  HttpSession session, 
                                  Model model, 
                                  RedirectAttributes redirectAttributes) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        List<Shop> shopList = shopService.findAllByVendor(vendor);
        if (shopList.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", "Bạn chưa có shop. Hãy đăng ký ngay!");
            return "redirect:/vendor/shop/register";
        }

        Shop shop = resolveShopFromList(shopList, shopId);
        if (shop == null) {
            redirectAttributes.addFlashAttribute("warning", "Không tìm thấy shop cần quản lý.");
            return "redirect:/vendor/my-shops";
        }

        shop = shopService.refreshStatistics(shop);

        Long shopIdFinal = shop.getShopId();
        long productCount = productService.countByShopId(shopIdFinal);
        long pendingOrders = defaultZero(orderDetailRepository.countDistinctOrdersByShopAndStatus(shopIdFinal, Order.OrderStatus.NEW))
                + defaultZero(orderDetailRepository.countDistinctOrdersByShopAndStatus(shopIdFinal, Order.OrderStatus.PENDING));
        Long confirmedOrders = orderDetailRepository.countDistinctOrdersByShopAndStatus(shopIdFinal, Order.OrderStatus.CONFIRMED);
        Long shippingOrders = orderDetailRepository.countDistinctOrdersByShopAndStatus(shopIdFinal, Order.OrderStatus.SHIPPING);
        Long deliveredOrders = orderDetailRepository.countDistinctOrdersByShopAndStatus(shopIdFinal, Order.OrderStatus.DELIVERED);
        Long cancelledOrders = orderDetailRepository.countDistinctOrdersByShopAndStatus(shopIdFinal, Order.OrderStatus.CANCELLED);
        Double totalRevenue = orderDetailRepository.sumRevenueByShop(shopIdFinal);

        // Lấy danh sách sản phẩm để hiển thị trong dashboard
        List<Product> products = productService.findByShopId(shopIdFinal);

        model.addAttribute("vendor", vendor);
        model.addAttribute("shop", shop);
        model.addAttribute("shopList", shopList);
        model.addAttribute("selectedShopId", shop.getShopId());
        model.addAttribute("products", products);
        model.addAttribute("shopStatus", shop.getStatus());
        model.addAttribute("productCount", productCount);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("confirmedOrders", defaultZero(confirmedOrders));
        model.addAttribute("shippingOrders", defaultZero(shippingOrders));
        model.addAttribute("deliveredOrders", defaultZero(deliveredOrders));
        model.addAttribute("cancelledOrders", defaultZero(cancelledOrders));
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        model.addAttribute("pageTitle", "Bảng điều khiển shop");

        return "vendor/dashboard";
    }

    @PostMapping("/dashboard/{id}/delete")
    public String deleteProduct(@PathVariable("id") Long productId,
                                @RequestParam(value = "shopId", required = false) Long shopId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        Optional<Product> productOpt = findVendorProduct(vendor, productId);
        if (productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm.");
            return "redirect:/vendor/dashboard" + (shopId != null ? "?shopId=" + shopId : "");
        }

        Product product = productOpt.get();
        Shop shop = product.getShop();

        productService.deleteById(productId);
        shopService.refreshStatistics(shop);
        redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm thành công.");
        return "redirect:/vendor/dashboard" + (shopId != null ? "?shopId=" + shopId : "");
    }

    private long defaultZero(Long value) {
        return value != null ? value : 0L;
    }

    private Shop resolveShopFromList(List<Shop> shopList, Long requestedShopId) {
        if (shopList == null || shopList.isEmpty()) {
            return null;
        }
        if (requestedShopId != null) {
            return shopList.stream()
                    .filter(shop -> shop.getShopId().equals(requestedShopId))
                    .findFirst()
                    .orElse(null);
        }
        return shopList.get(0);
    }

    private Optional<Product> findVendorProduct(User vendor, Long productId) {
        return productService.findById(productId)
                .filter(product -> product.getShop() != null
                        && product.getShop().getVendor() != null
                        && product.getShop().getVendor().getUserId().equals(vendor.getUserId()));
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

