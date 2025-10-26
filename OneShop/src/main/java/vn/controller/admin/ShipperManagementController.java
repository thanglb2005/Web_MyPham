package vn.controller.admin;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
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
    @Transactional
    public String manageShippers(Model model, HttpSession session) {
        // Lấy tất cả shop
        List<Shop> shops = shopRepository.findAll();
        
        // Force load shippers for each shop
        shops.forEach(shop -> {
            shop.getShippers().size(); // Force initialization
        });
        
        // Lấy tất cả shipper đã được duyệt (status = true và có role SHIPPER)
        List<User> allShippers = userRepository.findAll().stream()
            .filter(user -> user.getRoles().stream()
                .anyMatch(role -> "ROLE_SHIPPER".equals(role.getName())))
            .filter(user -> user.getStatus() != null && user.getStatus()) // Chỉ lấy shipper đã duyệt
            .collect(Collectors.toList());

        model.addAttribute("shops", shops);
        model.addAttribute("allShippers", allShippers);
        model.addAttribute("pageTitle", "Quản lý Shipper");

        return "admin/manage-shippers";
    }

    /**
     * Hiển thị danh sách tất cả shipper
     */
    @GetMapping("/shippers-list")
    @Transactional
    public String shippersList(Model model) {
        // Lấy tất cả shipper (user có role SHIPPER)
        List<User> allShippers = userRepository.findAll().stream()
            .filter(user -> user.getRoles().stream()
                .anyMatch(role -> "ROLE_SHIPPER".equals(role.getName())))
            .collect(Collectors.toList());
        
        // Đếm shipper theo trạng thái
        long approvedShippers = allShippers.stream()
            .filter(user -> user.getStatus() != null && user.getStatus())
            .count();
        
        long pendingShippers = allShippers.stream()
            .filter(user -> user.getStatus() == null || !user.getStatus())
            .count();
        
        long assignedShippers = allShippers.stream()
            .filter(shipper -> shipper.getStatus() != null && shipper.getStatus() && !shopRepository.findShopsByShipper(shipper).isEmpty())
            .count();
        
        long unassignedShippers = approvedShippers - assignedShippers;
        
        // Thêm thông tin shop cho mỗi shipper
        allShippers.forEach(shipper -> {
            List<Shop> shops = shopRepository.findShopsByShipper(shipper);
            shipper.setAssignedShops(shops.stream().collect(Collectors.toSet()));
        });
        
        model.addAttribute("shippers", allShippers);
        model.addAttribute("totalShippers", allShippers.size());
        model.addAttribute("approvedShippers", approvedShippers);
        model.addAttribute("pendingShippers", pendingShippers);
        model.addAttribute("assignedShippers", assignedShippers);
        model.addAttribute("unassignedShippers", unassignedShippers);
        // Provide all shops for assignment modal
        model.addAttribute("shops", shopRepository.findAll());
        
        return "admin/shippers-list";
    }

    /**
     * Xem chi tiết shipper
     */
    @GetMapping("/shipper-detail/{shipperId}")
    @Transactional
    public String shipperDetail(@PathVariable Long shipperId, Model model) {
        try {
            User shipper = userRepository.findById(shipperId).orElse(null);
            
            if (shipper == null) {
                model.addAttribute("error", "Shipper không tồn tại");
                return "redirect:/admin/shippers-list";
            }
            
            // Lấy danh sách shop được phân công
            List<Shop> assignedShops = shopRepository.findShopsByShipper(shipper);
            if (assignedShops == null) {
                assignedShops = new java.util.ArrayList<>();
            }
            
            model.addAttribute("shipper", shipper);
            model.addAttribute("assignedShops", assignedShops);
            
            return "admin/shipper-detail";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/shippers-list";
        }
    }

    /**
     * Gán shipper cho shop
     */
    @PostMapping("/assign-shipper")
    @ResponseBody
    @Transactional
    public String assignShipper(@RequestParam Long shopId, 
                                @RequestParam Long shipperId) {
        try {
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
            
            User shipper = userRepository.findById(shipperId)
                .orElseThrow(() -> new RuntimeException("Shipper không tồn tại"));

            // Force initialize shippers collection
            shop.getShippers().size();
            
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
    @Transactional
    public String removeShipper(@RequestParam Long shopId, 
                                @RequestParam Long shipperId) {
        try {
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
            
            User shipper = userRepository.findById(shipperId)
                .orElseThrow(() -> new RuntimeException("Shipper không tồn tại"));

            // Force initialize shippers collection
            shop.getShippers().size();
            
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
    @Transactional
    public Set<User> getShopShippers(@PathVariable Long shopId) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
        // Force initialize shippers collection
        shop.getShippers().size();
        return shop.getShippers();
    }

    /**
     * Duyệt shipper
     */
    @PostMapping("/approve-shipper")
    @ResponseBody
    @Transactional
    public String approveShipper(@RequestParam Long shipperId) {
        try {
            User shipper = userRepository.findById(shipperId)
                .orElseThrow(() -> new RuntimeException("Shipper không tồn tại"));
            
            shipper.setStatus(true);
            userService.save(shipper);
            
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Từ chối shipper
     */
    @PostMapping("/reject-shipper")
    @ResponseBody
    @Transactional
    public String rejectShipper(@RequestParam Long shipperId) {
        try {
            User shipper = userRepository.findById(shipperId)
                .orElseThrow(() -> new RuntimeException("Shipper không tồn tại"));
            
            shipper.setStatus(false);
            userService.save(shipper);
            
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }
}

