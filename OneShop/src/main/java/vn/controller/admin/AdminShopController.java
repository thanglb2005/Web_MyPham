package vn.controller.admin;

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
import vn.entity.Shop;
import vn.entity.User;
import vn.service.ShopService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/shops")
public class AdminShopController {

    @Autowired
    private ShopService shopService;

    @GetMapping
    public String listShops(@RequestParam(value = "status", required = false) Shop.ShopStatus status,
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

        model.addAttribute("user", user);
        model.addAttribute("shops", shops);
        model.addAttribute("selectedStatus", Optional.ofNullable(status).map(Enum::name).orElse("ALL"));
        model.addAttribute("statuses", Arrays.asList(Shop.ShopStatus.values()));
        model.addAttribute("pageTitle", "Quản lý shop");
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
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái shop thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy shop cần cập nhật.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật trạng thái shop.");
        }
        return "redirect:/admin/shops";
    }
}
