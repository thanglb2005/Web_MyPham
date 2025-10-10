package vn.controller.vendor;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.dto.VendorShopForm;
import vn.entity.Shop;
import vn.entity.User;
import vn.service.ImageStorageService;
import vn.service.ShopService;

import java.io.IOException;
import java.util.Optional;

@Controller
@RequestMapping("/vendor/shop")
public class VendorShopController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private ImageStorageService imageStorageService;

    @GetMapping("/register")
    public String registerForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        // Vendor có thể tạo nhiều shop

        if (!model.containsAttribute("shopForm")) {
            model.addAttribute("shopForm", new VendorShopForm());
        }
        model.addAttribute("pageTitle", "Đăng ký shop");
        return "vendor/shop-register";
    }

    @PostMapping("/register")
    public String registerShop(@Valid @ModelAttribute("shopForm") VendorShopForm form,
                               BindingResult bindingResult,
                               @RequestParam(value = "logo", required = false) MultipartFile logoFile,
                               @RequestParam(value = "banner", required = false) MultipartFile bannerFile,
                               HttpSession session,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        if (shopService.findByVendor(vendor).isPresent()) {
            redirectAttributes.addFlashAttribute("info", "Bạn đã có shop. Có thể cập nhật thông tin tại đây.");
            return "redirect:/vendor/shop/settings";
        }

        if (!shopService.isShopNameAvailable(form.getShopName(), null)) {
            bindingResult.rejectValue("shopName", "duplicate", "Tên shop đã tồn tại.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Đăng ký shop");
            return "vendor/shop-register";
        }

        Shop shop = new Shop();
        shop.setShopName(form.getShopName().trim());
        shop.setShopDescription(form.getShopDescription());
        shop.setPhoneNumber(form.getPhoneNumber());
        shop.setAddress(form.getAddress());
        shop.setCity(form.getCity());
        shop.setDistrict(form.getDistrict());
        shop.setWard(form.getWard());
        shop.setAllowCod(form.getAllowCod());
        shop.setPreparationDays(form.getPreparationDays());

        try {
            if (logoFile != null && !logoFile.isEmpty()) {
                String logoName = imageStorageService.store(logoFile, form.getShopName() + "-logo");
                shop.setShopLogo(logoName);
            }
            if (bannerFile != null && !bannerFile.isEmpty()) {
                String bannerName = imageStorageService.store(bannerFile, form.getShopName() + "-banner");
                shop.setShopBanner(bannerName);
            }
        } catch (IOException e) {
            bindingResult.reject("upload", "Không thể lưu hình ảnh. Vui lòng thử lại.");
            model.addAttribute("pageTitle", "Đăng ký shop");
            return "vendor/shop-register";
        }

        shopService.registerShop(vendor, shop);
        redirectAttributes.addFlashAttribute("success", "Đăng ký shop thành công! Shop sẽ được duyệt trong thời gian sớm nhất.");
        return "redirect:/vendor/home";
    }

    @GetMapping("/settings")
    public String settings(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        Optional<Shop> shopOpt = shopService.findByVendor(vendor);
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", "Bạn chưa có shop. Đăng ký shop mới nhé!");
            return "redirect:/vendor/shop/register";
        }

        if (!model.containsAttribute("shopForm")) {
            VendorShopForm form = toForm(shopOpt.get());
            model.addAttribute("shopForm", form);
        }
        model.addAttribute("shop", shopOpt.get());
        model.addAttribute("pageTitle", "Cài đặt shop");
        return "vendor/shop-settings";
    }

    @PostMapping("/settings")
    public String updateSettings(@Valid @ModelAttribute("shopForm") VendorShopForm form,
                                 BindingResult bindingResult,
                                 @RequestParam(value = "logo", required = false) MultipartFile logoFile,
                                 @RequestParam(value = "banner", required = false) MultipartFile bannerFile,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        Optional<Shop> shopOpt = shopService.findByVendor(vendor);
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", "Bạn chưa có shop. Đăng ký shop mới nhé!");
            return "redirect:/vendor/shop/register";
        }

        Shop shop = shopOpt.get();

        if (!shopService.isShopNameAvailable(form.getShopName(), shop.getShopId())) {
            bindingResult.rejectValue("shopName", "duplicate", "Tên shop đã được sử dụng.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("shop", shop);
            model.addAttribute("pageTitle", "Cài đặt shop");
            return "vendor/shop-settings";
        }

        shop.setShopName(form.getShopName().trim());
        shop.setShopDescription(form.getShopDescription());
        shop.setPhoneNumber(form.getPhoneNumber());
        shop.setAddress(form.getAddress());
        shop.setCity(form.getCity());
        shop.setDistrict(form.getDistrict());
        shop.setWard(form.getWard());
        shop.setAllowCod(form.getAllowCod());
        shop.setPreparationDays(form.getPreparationDays());

        try {
            if (logoFile != null && !logoFile.isEmpty()) {
                String logoName = imageStorageService.store(logoFile, form.getShopName() + "-logo");
                shop.setShopLogo(logoName);
            }
            if (bannerFile != null && !bannerFile.isEmpty()) {
                String bannerName = imageStorageService.store(bannerFile, form.getShopName() + "-banner");
                shop.setShopBanner(bannerName);
            }
        } catch (IOException e) {
            bindingResult.reject("upload", "Không thể lưu hình ảnh. Vui lòng thử lại.");
            model.addAttribute("shop", shop);
            model.addAttribute("pageTitle", "Cài đặt shop");
            return "vendor/shop-settings";
        }

        shopService.updateShop(shop);
        redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin shop thành công.");
        return "redirect:/vendor/shop/settings";
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

    private VendorShopForm toForm(Shop shop) {
        VendorShopForm form = new VendorShopForm();
        form.setShopName(shop.getShopName());
        form.setShopDescription(shop.getShopDescription());
        form.setPhoneNumber(shop.getPhoneNumber());
        form.setAddress(shop.getAddress());
        form.setCity(shop.getCity());
        form.setDistrict(shop.getDistrict());
        form.setWard(shop.getWard());
        form.setAllowCod(shop.getAllowCod());
        form.setPreparationDays(shop.getPreparationDays());
        return form;
    }
}

