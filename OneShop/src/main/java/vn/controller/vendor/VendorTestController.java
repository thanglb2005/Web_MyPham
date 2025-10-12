package vn.controller.vendor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import vn.entity.User;
import vn.util.UserUtils;

@Controller
@RequestMapping("/vendor/test")
public class VendorTestController {

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        User vendor = UserUtils.getCurrentUser(session);
        if (vendor == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("vendor", vendor);
        model.addAttribute("pageTitle", "Vendor Dashboard");
        return "vendor/home";
    }

    @GetMapping("/orders")
    public String orders(HttpSession session, Model model) {
        User vendor = UserUtils.getCurrentUser(session);
        if (vendor == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("vendor", vendor);
        model.addAttribute("pageTitle", "Quản lý đơn hàng");
        return "vendor/orders/list";
    }
}
