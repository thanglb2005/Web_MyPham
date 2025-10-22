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
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/vendor/shop")
public class VendorShopController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private ImageStorageService imageStorageService;

    @GetMapping("/register")
    public String registerForm(HttpSession session, Model model) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        if (!model.containsAttribute("shopForm")) {
            model.addAttribute("shopForm", new VendorShopForm());
        }
        model.addAttribute("vendor", vendor);
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

        model.addAttribute("vendor", vendor);
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
        shop.setDistrict(null); // District không sử dụng trong form mới
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
        return "redirect:/vendor/my-shops";
    }

    @GetMapping("/settings")
    public String settings(@RequestParam(value = "shopId", required = false) Long shopId,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        Optional<Shop> shopOpt = shopId != null
                ? shopService.findByIdAndVendor(shopId, vendor)
                : shopService.findFirstByVendor(vendor);
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", "Bạn chưa có shop nào. Đăng ký shop mới nhé!");
            return "redirect:/vendor/shop/register";
        }

        Shop shop = shopOpt.get();
        if (!model.containsAttribute("shopForm")) {
            model.addAttribute("shopForm", toForm(shop));
        }

        List<Shop> shopList = shopService.findAllByVendor(vendor);
        model.addAttribute("vendor", vendor);
        model.addAttribute("shop", shop);
        model.addAttribute("shopList", shopList);
        model.addAttribute("selectedShopId", shop.getShopId());
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

        Long targetShopId = form.getShopId();
        if (targetShopId == null) {
            bindingResult.reject("shopId", "Thiếu thông tin shop cần cập nhật.");
        }

        Optional<Shop> shopOpt = targetShopId != null
                ? shopService.findByIdAndVendor(targetShopId, vendor)
                : Optional.empty();
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", "Không tìm thấy shop để cập nhật.");
            return "redirect:/vendor/shop/register";
        }

        Shop shop = shopOpt.get();

        if (!shopService.isShopNameAvailable(form.getShopName(), shop.getShopId())) {
            bindingResult.rejectValue("shopName", "duplicate", "Tên shop đã được sử dụng.");
        }

        if (bindingResult.hasErrors()) {
            List<Shop> shopList = shopService.findAllByVendor(vendor);
            model.addAttribute("vendor", vendor);
            model.addAttribute("shop", shop);
            model.addAttribute("shopList", shopList);
            model.addAttribute("selectedShopId", shop.getShopId());
            model.addAttribute("pageTitle", "Cài đặt shop");
            return "vendor/shop-settings";
        }

        shop.setShopName(form.getShopName().trim());
        shop.setShopDescription(form.getShopDescription());
        shop.setPhoneNumber(form.getPhoneNumber());
        shop.setAddress(form.getAddress());
        shop.setCity(form.getCity());
        shop.setDistrict(null); // District không sử dụng trong form mới
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
            List<Shop> shopList = shopService.findAllByVendor(vendor);
            model.addAttribute("vendor", vendor);
            model.addAttribute("shop", shop);
            model.addAttribute("shopList", shopList);
            model.addAttribute("selectedShopId", shop.getShopId());
            model.addAttribute("pageTitle", "Cài đặt shop");
            return "vendor/shop-settings";
        }

        shopService.updateShop(shop);
        redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin shop thành công.");
        return "redirect:/vendor/shop/settings?shopId=" + shop.getShopId();
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
        form.setShopId(shop.getShopId());
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
