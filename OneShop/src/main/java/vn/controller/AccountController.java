package vn.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import vn.dto.UserVoucherGroup;
import vn.entity.Promotion;
import vn.entity.Shop;
import vn.entity.User;
import vn.repository.UserRepository;
import vn.service.UserService;
import vn.service.ImageStorageService;
import vn.service.OneXuService;
import vn.service.PromotionService;

import java.io.IOException;
// SimpleDateFormat is referenced with fully qualified name later; remove import to avoid warning
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AccountController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;

    @Autowired
    private ImageStorageService imageStorageService;
    
    @Autowired
    private OneXuService oneXuService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private vn.repository.OrderRepository orderRepository;

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Đồng bộ hóa số dư One Xu trước khi hiển thị profile
        oneXuService.syncUserBalance(user.getUserId());
        
        // Load fresh user data from database
        User freshUser = userRepository.findById(user.getUserId()).orElse(user);
        session.setAttribute("user", freshUser);
        
        // Populate real statistics and fields
        populateProfileModel(model, freshUser);
        return "profile";
    }

    @GetMapping("/user/vouchers")
    public String userVouchers(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Promotion> activePromotions = promotionService.getActivePromotions();
        
        // Separate system vouchers and shop vouchers
        List<UserVoucherGroup.VoucherItem> systemVouchers = new ArrayList<>();
        Map<Long, UserVoucherGroup> grouped = new LinkedHashMap<>();

        for (Promotion promotion : activePromotions) {
            Shop shop = promotion.getShop();
            if (shop == null) {
                // System voucher
                systemVouchers.add(UserVoucherGroup.VoucherItem.fromPromotion(promotion));
            } else {
                // Shop voucher
                grouped.computeIfAbsent(shop.getShopId(), id -> new UserVoucherGroup(shop))
                        .addVoucher(promotion);
            }
        }

        // Sort system vouchers
        Comparator<UserVoucherGroup.VoucherItem> voucherComparator = Comparator
                .comparing((UserVoucherGroup.VoucherItem item) ->
                        item.getEndDate() == null ? LocalDateTime.MAX : item.getEndDate())
                .thenComparing(item -> item.getPromotionName() != null
                        ? item.getPromotionName().toLowerCase()
                        : "");
        systemVouchers.sort(voucherComparator);

        // Sort shop vouchers
        List<UserVoucherGroup> voucherGroups = new ArrayList<>(grouped.values());
        for (UserVoucherGroup group : voucherGroups) {
            group.getVouchers().sort(voucherComparator);
        }
        voucherGroups.sort(Comparator.comparing(UserVoucherGroup::getShopName, String.CASE_INSENSITIVE_ORDER));

        // Calculate statistics
        long systemVoucherCount = systemVouchers.size();
        long shopVoucherCount = voucherGroups.stream()
                .mapToLong(group -> group.getVouchers().size())
                .sum();
        long totalVouchers = systemVoucherCount + shopVoucherCount;
        
        long systemExpiringSoon = systemVouchers.stream()
                .filter(UserVoucherGroup.VoucherItem::isExpiringSoon)
                .count();
        long shopExpiringSoon = voucherGroups.stream()
                .mapToLong(UserVoucherGroup::getExpiringSoonCount)
                .sum();
        long expiringSoon = systemExpiringSoon + shopExpiringSoon;

        model.addAttribute("systemVouchers", systemVouchers);
        model.addAttribute("voucherGroups", voucherGroups);
        model.addAttribute("totalVoucherCount", totalVouchers);
        model.addAttribute("systemVoucherCount", systemVoucherCount);
        model.addAttribute("shopVoucherCount", shopVoucherCount);
        model.addAttribute("expiringSoonCount", expiringSoon);

        return "web/user-vouchers";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam("avatarFile") MultipartFile avatarFile,
                               HttpSession session,
                               Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        user.setName(name);
        user.setEmail(email);

        // Handle file upload
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String fileName = imageStorageService.store(avatarFile, user.getName());
                user.setAvatar(fileName);
            } catch (IOException e) {
                // Log the error and add a message for the user
                model.addAttribute("error", "Lỗi khi tải lên ảnh đại diện. Vui lòng thử lại.");
                // Re-populate model attributes for the view
                populateProfileModel(model, user);
                return "profile";
            }
        }

        userRepository.save(user);
        session.setAttribute("user", user);
        
        model.addAttribute("message", "Cập nhật thông tin thành công!");
        populateProfileModel(model, user);
        
        return "profile";
    }
    
    private void populateProfileModel(Model model, User user) {
        String formattedDate = "";
        if (user.getRegisterDate() != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            formattedDate = sdf.format(user.getRegisterDate());
        }
        model.addAttribute("user", user);
        model.addAttribute("formattedRegisterDate", formattedDate);

        // Calculate real metrics
        try {
            // Total orders (all statuses)
            long totalOrders = 0;
            try {
                java.util.List<vn.entity.Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);
                totalOrders = orders != null ? orders.size() : 0;
            } catch (Exception ignored) {}

            // Total spent (only DELIVERED, using finalAmount if available)
            double totalSpent = 0.0;
            try {
                java.util.List<vn.entity.Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);
                if (orders != null) {
                    for (vn.entity.Order o : orders) {
                        if (o != null && o.getStatus() == vn.entity.Order.OrderStatus.DELIVERED) {
                            Double amount = (o.getFinalAmount() != null && o.getFinalAmount() > 0) ? o.getFinalAmount() : o.getTotalAmount();
                            if (amount != null) totalSpent += amount;
                        }
                    }
                }
            } catch (Exception ignored) {}

            // Voucher count (active, usable vouchers from system + shops)
            long totalVouchers = 0;
            try {
                java.util.List<vn.entity.Promotion> promos = promotionService.getActivePromotions();
                totalVouchers = promos != null ? promos.size() : 0;
            } catch (Exception ignored) {}

            model.addAttribute("totalOrders", totalOrders);
            model.addAttribute("totalSpent", totalSpent);
            model.addAttribute("totalVouchers", totalVouchers);
        } catch (Exception e) {
            // Fallback safe values
            model.addAttribute("totalOrders", 0);
            model.addAttribute("totalSpent", 0);
            model.addAttribute("totalVouchers", 0);
        }
    }

    @GetMapping("/change-password")
    public String changePasswordPage() {
        return "changePassword";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                HttpSession session,
                                Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        if (!userService.verifyPassword(oldPassword, user.getPassword())) {
            model.addAttribute("error", "Mật khẩu cũ không đúng!");
            return "changePassword";
        }
        
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu mới không khớp!");
            return "changePassword";
        }
        
        user.setPassword(newPassword);
        userService.save(user);
        session.setAttribute("user", user);
        
        model.addAttribute("success", "Đổi mật khẩu thành công!");
        return "changePassword";
    }
}
