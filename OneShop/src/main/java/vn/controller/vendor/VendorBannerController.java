package vn.controller.vendor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.entity.Shop;
import vn.service.ShopService;
import vn.service.StorageService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/vendor/shop/banner")
public class VendorBannerController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private StorageService storageService;

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadBanner(
            @RequestParam("shopId") Long shopId,
            @RequestParam("bannerFile") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Shop> shopOpt = shopService.findById(shopId);
            if (shopOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Shop không tồn tại");
                return ResponseEntity.badRequest().body(response);
            }

            Shop shop = shopOpt.get();
            
            // Upload ảnh
            String imageName = storageService.storeGeneralImage(file);
            
            // Cập nhật shop banner
            shop.setShopBanner(imageName);
            shopService.updateShop(shop);
            
            response.put("success", true);
            response.put("message", "Upload banner thành công");
            response.put("imageName", imageName);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteBanner(@RequestParam("shopId") Long shopId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Shop> shopOpt = shopService.findById(shopId);
            if (shopOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Shop không tồn tại");
                return ResponseEntity.badRequest().body(response);
            }

            Shop shop = shopOpt.get();
            
            // Xóa ảnh cũ nếu có
            if (shop.getShopBanner() != null && !shop.getShopBanner().isEmpty()) {
                storageService.deleteImage(shop.getShopBanner());
            }
            
            // Xóa banner khỏi shop
            shop.setShopBanner(null);
            shopService.updateShop(shop);
            
            response.put("success", true);
            response.put("message", "Xóa banner thành công");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
