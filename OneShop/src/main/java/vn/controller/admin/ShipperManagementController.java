package vn.controller.admin;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.entity.Role;
import vn.entity.Shop;
import vn.entity.User;
import vn.repository.RoleRepository;
import vn.repository.ShopRepository;
import vn.repository.UserRepository;
import vn.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

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
    public String shippersList(Model model,
                               @RequestParam(value = "q", required = false) String query,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "10") int size) {
        // Lấy tất cả shipper (user có role SHIPPER)
        List<User> allShippers = userRepository.findAll().stream()
            .filter(user -> user.getRoles().stream()
                .anyMatch(role -> "ROLE_SHIPPER".equals(role.getName())))
            .collect(Collectors.toList());
        
        // Search filter by name/email/phone
        if (query != null && !query.trim().isEmpty()) {
            final String q = query.trim().toLowerCase();
            allShippers = allShippers.stream().filter(u -> {
                try { if (u.getName() != null && u.getName().toLowerCase().contains(q)) return true; } catch (Exception ignored) {}
                try { if (u.getEmail() != null && u.getEmail().toLowerCase().contains(q)) return true; } catch (Exception ignored) {}
                return false;
            }).collect(Collectors.toList());
        }
        
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
        
        // Pagination
        if (size <= 0) size = 10;
        int totalItems = allShippers.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        if (page < 0) page = 0;
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalItems);
        List<User> paginated = allShippers.subList(Math.min(startIndex, totalItems), Math.min(endIndex, totalItems));
        
        model.addAttribute("shippers", paginated);
        model.addAttribute("totalShippers", allShippers.size());
        model.addAttribute("approvedShippers", approvedShippers);
        model.addAttribute("pendingShippers", pendingShippers);
        model.addAttribute("assignedShippers", assignedShippers);
        model.addAttribute("unassignedShippers", unassignedShippers);
        // Provide all shops for assignment modal
        model.addAttribute("shops", shopRepository.findAll());
        // Search & pagination context
        model.addAttribute("searchTerm", query);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("hasPrev", page > 0);
        model.addAttribute("hasNext", page < totalPages - 1);
        model.addAttribute("startIndex", Math.min(startIndex + 1, totalItems));
        model.addAttribute("endIndex", endIndex);
        
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
            userRepository.save(shipper);
            
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
            userRepository.save(shipper);
            
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Tạo shipper mới
     */
    @PostMapping("/create-shipper")
    @ResponseBody
    @Transactional
    public String createShipper(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String password,
                                @RequestParam(required = false, defaultValue = "false") Boolean autoApprove) {
        try {
            // Kiểm tra email đã tồn tại chưa
            if (userRepository.existsByEmail(email)) {
                return "email_exists";
            }

            // Lấy ROLE_SHIPPER
            Optional<Role> shipperRole = roleRepository.findByName("ROLE_SHIPPER");
            if (!shipperRole.isPresent()) {
                return "role_not_found";
            }

            // Tạo shipper mới sử dụng UserService
            userService.createUserWithRoles(name, email, password, Collections.singletonList(shipperRole.get().getId()));
            
            // Cập nhật thông tin bổ sung cho shipper
            Optional<User> newShipper = userRepository.findByEmail(email);
            if (newShipper.isPresent()) {
                User shipper = newShipper.get();
                
                // Set avatar mặc định nếu chưa có
                if (shipper.getAvatar() == null || shipper.getAvatar().isEmpty()) {
                    shipper.setAvatar("user.png");
                }
                
                // Set oneXuBalance mặc định nếu chưa có
                if (shipper.getOneXuBalance() == null) {
                    shipper.setOneXuBalance(0.0);
                }
                
                // Set status dựa vào autoApprove
                shipper.setStatus(autoApprove);
                
                userRepository.save(shipper);
            }

            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Xóa shipper
     */
    @PostMapping("/delete-shipper")
    @ResponseBody
    @Transactional
    public String deleteShipper(@RequestParam Long shipperId) {
        try {
            User shipper = userRepository.findById(shipperId)
                .orElseThrow(() -> new RuntimeException("Shipper không tồn tại"));
            
            // Kiểm tra xem shipper có đang được gán cho shop nào không
            List<Shop> assignedShops = shopRepository.findShopsByShipper(shipper);
            if (assignedShops != null && !assignedShops.isEmpty()) {
                // Xóa shipper khỏi tất cả các shop trước
                for (Shop shop : assignedShops) {
                    shop.getShippers().size(); // Force initialize
                    shop.getShippers().remove(shipper);
                    shopRepository.save(shop);
                }
            }
            
            // Xóa shipper
            userRepository.delete(shipper);
            
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }
}

