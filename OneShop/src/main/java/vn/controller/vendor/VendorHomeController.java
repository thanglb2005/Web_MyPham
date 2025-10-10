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

    @GetMapping("/home")
    public String vendorHome(HttpSession session, Model model) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        // Lấy tất cả shops của vendor
        List<Shop> shops = shopService.findAllByVendor(vendor);
        
        model.addAttribute("vendor", vendor);
        model.addAttribute("shops", shops);
        model.addAttribute("hasShops", !shops.isEmpty());
        model.addAttribute("totalShops", shops.size());
        model.addAttribute("pageTitle", "Trang chủ Vendor");
        
        return "vendor/home";
    }

    @GetMapping("/my-shops")
    public String myShops(HttpSession session, Model model) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        List<Shop> shops = shopService.findAllByVendor(vendor);
        
        if (shops.isEmpty()) {
            model.addAttribute("error", "Bạn chưa có shop nào. Hãy đăng ký shop mới!");
            return "vendor/home";
        }
        
        model.addAttribute("vendor", vendor);
        model.addAttribute("shops", shops);
        model.addAttribute("totalShops", shops.size());
        model.addAttribute("pageTitle", "Danh sách Shop của tôi");
        
        return "vendor/my-shops";
    }

    @GetMapping("/dashboard/{shopId}")
    public String shopDashboard(@PathVariable Long shopId, HttpSession session, Model model) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        Optional<Shop> shopOpt = shopService.findById(shopId);
        if (shopOpt.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy shop!");
            return "redirect:/vendor/my-shops";
        }

        Shop shop = shopOpt.get();
        
        // Kiểm tra vendor có sở hữu shop này không
        if (!shop.getVendor().getUserId().equals(vendor.getUserId())) {
            model.addAttribute("error", "Bạn không có quyền truy cập shop này!");
            return "redirect:/vendor/my-shops";
        }

        // Lấy thống kê shop
        shop = shopService.refreshStatistics(shop);
        long productCount = productService.countByShopId(shopId);

        model.addAttribute("vendor", vendor);
        model.addAttribute("shop", shop);
        model.addAttribute("productCount", productCount);
        model.addAttribute("pageTitle", "Dashboard - " + shop.getShopName());
        
        return "vendor/shop-dashboard";
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
