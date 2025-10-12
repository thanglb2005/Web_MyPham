package vn.controller.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.entity.Shop;
import vn.entity.User;
import vn.repository.ShopRepository;
import vn.repository.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller quản lý shipper cho shop/admin
 * @author OneShop Team
 */
@Controller
@RequestMapping("/admin")
public class ShipperManagementController {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Hiển thị trang quản lý shipper cho các shop
     */
    @GetMapping("/manage-shippers")
    public String manageShippers(Model model, HttpSession session) {
        // Lấy tất cả shop
        List<Shop> shops = shopRepository.findAll();
        
        // Lấy tất cả shipper (user có role SHIPPER)
        List<User> allShippers = userRepository.findAll().stream()
            .filter(user -> user.getRoles().stream()
                .anyMatch(role -> "ROLE_SHIPPER".equals(role.getName())))
            .collect(Collectors.toList());

        model.addAttribute("shops", shops);
        model.addAttribute("allShippers", allShippers);
        model.addAttribute("pageTitle", "Quản lý Shipper");

        return "admin/manage-shippers";
    }

    /**
     * Gán shipper cho shop
     */
    @PostMapping("/assign-shipper")
    @ResponseBody
    public String assignShipper(@RequestParam Long shopId, 
                                @RequestParam Long shipperId) {
        try {
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
            
            User shipper = userRepository.findById(shipperId)
                .orElseThrow(() -> new RuntimeException("Shipper không tồn tại"));

            // Kiểm tra shipper đã được gán cho shop này chưa
            if (shop.getShippers().contains(shipper)) {
                return "duplicate";
            }

            // Gán shipper cho shop
            shop.getShippers().add(shipper);
            shopRepository.save(shop);

            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Xóa shipper khỏi shop
     */
    @PostMapping("/remove-shipper")
    @ResponseBody
    public String removeShipper(@RequestParam Long shopId, 
                                @RequestParam Long shipperId) {
        try {
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
            
            User shipper = userRepository.findById(shipperId)
                .orElseThrow(() -> new RuntimeException("Shipper không tồn tại"));

            // Xóa shipper khỏi shop
            shop.getShippers().remove(shipper);
            shopRepository.save(shop);

            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Lấy danh sách shipper của shop (API)
     */
    @GetMapping("/api/shop-shippers/{shopId}")
    @ResponseBody
    public Set<User> getShopShippers(@PathVariable Long shopId) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
        return shop.getShippers();
    }
}

