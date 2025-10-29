package vn.controller.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.entity.Shop;
import vn.entity.User;
import vn.service.ShopService;
import vn.repository.OrderRepository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/shops")
public class AdminShopController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping
    public String listShops(@RequestParam(value = "status", required = false) Shop.ShopStatus status,
                            @RequestParam(value = "q", required = false) String query,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size,
                            HttpSession session,
                            Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Shop> shops;
        if (status != null) {
            shops = shopService.findByStatus(status);
        } else {
            shops = shopService.findAll();
        }

        // Search filter: restrict to shop name only
        if (query != null && !query.trim().isEmpty()) {
            final String q = query.trim().toLowerCase();
            shops = shops.stream()
                    .filter(s -> {
                        try { return s.getShopName() != null && s.getShopName().toLowerCase().contains(q); }
                        catch (Exception ignored) { return false; }
                    })
                    .toList();
        }

        // Sync revenue: use delivered orders only for consistency across views
        for (Shop shop : shops) {
            try {
                Double delivered = orderRepository.sumDeliveredRevenueByShopId(shop.getShopId());
                shop.setTotalRevenue(delivered != null ? delivered : 0.0);
            } catch (Exception ignored) {}
        }

        // Calculate statistics
        long totalShops = shopService.findAll().size();
        long pendingShops = shopService.findByStatus(Shop.ShopStatus.PENDING).size();
        long activeShops = shopService.findByStatus(Shop.ShopStatus.ACTIVE).size();
        long rejectedShops = shopService.findByStatus(Shop.ShopStatus.REJECTED).size();

        model.addAttribute("user", user);
        // Pagination
        if (size <= 0) size = 10;
        int totalItems = shops.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        if (page < 0) page = 0;
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalItems);
        List<Shop> paginated = shops.subList(Math.min(startIndex, totalItems), Math.min(endIndex, totalItems));

        model.addAttribute("shops", paginated);
        model.addAttribute("selectedStatus", Optional.ofNullable(status).map(Enum::name).orElse("ALL"));
        model.addAttribute("statuses", Arrays.asList(Shop.ShopStatus.values()));
        model.addAttribute("pageTitle", "Quản lý shop");
        model.addAttribute("searchTerm", query);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("hasPrev", page > 0);
        model.addAttribute("hasNext", page < totalPages - 1);
        model.addAttribute("startIndex", Math.min(startIndex + 1, totalItems));
        model.addAttribute("endIndex", endIndex);
        
        // Add statistics
        model.addAttribute("totalShops", totalShops);
        model.addAttribute("pendingShops", pendingShops);
        model.addAttribute("activeShops", activeShops);
        model.addAttribute("rejectedShops", rejectedShops);
        
        return "admin/shops";
    }

    @PostMapping("/{shopId}/status")
    public String updateShopStatus(@PathVariable Long shopId,
                                   @RequestParam("status") Shop.ShopStatus status,
                                   @RequestParam(value = "reason", required = false) String reason,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            shopService.updateStatus(shopId, status, reason);
            String statusText = getStatusText(status);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái shop thành công: " + statusText);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy shop cần cập nhật.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật trạng thái shop.");
        }
        return "redirect:/admin/shops";
    }

    @GetMapping("/pending-count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPendingCount() {
        long pendingCount = shopService.findByStatus(Shop.ShopStatus.PENDING).size();
        Map<String, Object> response = new HashMap<>();
        response.put("pendingCount", pendingCount);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{shopId}/quick-approve")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> quickApprove(@PathVariable Long shopId) {
        Map<String, Object> response = new HashMap<>();
        try {
            shopService.updateStatus(shopId, Shop.ShopStatus.ACTIVE, "Duyệt sớm bởi admin");
            response.put("success", true);
            response.put("message", "Duyệt shop thành công");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi duyệt shop");
        }
        return ResponseEntity.ok(response);
    }

    private String getStatusText(Shop.ShopStatus status) {
        switch (status) {
            case ACTIVE:
                return "Hoạt động";
            case PENDING:
                return "Chờ duyệt";
            case SUSPENDED:
                return "Tạm khóa";
            case REJECTED:
                return "Từ chối";
            default:
                return status.name();
        }
    }
}
