package vn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.entity.FlashSale;
import vn.entity.FlashSaleProduct;
import vn.service.FlashSaleService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Public controller for Flash Sale
 * @author OneShop Team
 */
@Controller
@RequestMapping("/flash-sale")
public class FlashSaleController {
    
    @Autowired
    private FlashSaleService flashSaleService;
    
    /**
     * Display flash sale page
     */
    @GetMapping
    public String flashSalePage(Model model) {
        List<FlashSale> activeSales = flashSaleService.getActiveFlashSales();
        List<FlashSale> upcomingSales = flashSaleService.getUpcomingFlashSales();
        
        model.addAttribute("activeSales", activeSales);
        model.addAttribute("upcomingSales", upcomingSales);
        
        return "web/flash-sale";
    }
    
    /**
     * API: Get active flash sales (JSON)
     */
    @GetMapping("/api/active")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getActiveFlashSales() {
        List<FlashSale> activeSales = flashSaleService.getActiveFlashSales();
        
        // Debug logging
        System.out.println("=== Flash Sale API Debug ===");
        System.out.println("Active sales count: " + activeSales.size());
        activeSales.forEach(sale -> {
            System.out.println("Flash Sale ID: " + sale.getFlashSaleId() + 
                             ", Name: " + sale.getSaleName() + 
                             ", Status: " + sale.getStatus() +
                             ", Start: " + sale.getStartTime() +
                             ", End: " + sale.getEndTime());
        });
        
        List<Map<String, Object>> result = activeSales.stream()
            .map(this::toFlashSaleDTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * API: Get upcoming flash sales (JSON)
     */
    @GetMapping("/api/upcoming")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getUpcomingFlashSales() {
        List<FlashSale> upcomingSales = flashSaleService.getUpcomingFlashSales();
        
        List<Map<String, Object>> result = upcomingSales.stream()
            .map(this::toFlashSaleDTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * API: Check if product is in flash sale and get price
     */
    @GetMapping("/api/product/{productId}/price")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFlashSalePrice(@PathVariable Long productId) {
        Map<String, Object> result = new HashMap<>();
        
        System.out.println("=== Flash Sale Product Price API ===");
        System.out.println("Product ID: " + productId);
        
        Optional<FlashSaleProduct> fspOpt = flashSaleService.getActiveFlashSaleProduct(productId);
        
        if (fspOpt.isPresent()) {
            FlashSaleProduct fsp = fspOpt.get();
            System.out.println("Product found in flash sale: " + fsp.getProduct().getProductName());
            System.out.println("Flash Sale: " + fsp.getFlashSale().getSaleName());
            System.out.println("Status: " + fsp.getFlashSale().getStatus());
            
            result.put("inFlashSale", true);
            result.put("flashSalePrice", fsp.getFlashSalePrice());
            result.put("originalPrice", fsp.getOriginalPrice());
            result.put("discountPercentage", fsp.getDiscountPercentage());
            result.put("remainingQuantity", fsp.getRemainingQuantity());
            result.put("maxQuantityPerUser", fsp.getMaxQuantityPerUser());
            result.put("flashSaleId", fsp.getFlashSale().getFlashSaleId());
            result.put("flashSaleName", fsp.getFlashSale().getSaleName());
            result.put("remainingSeconds", fsp.getFlashSale().getRemainingSeconds());
        } else {
            System.out.println("Product NOT found in any active flash sale");
            result.put("inFlashSale", false);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * API: Get products in flash sale
     */
    @GetMapping("/api/{id}/products")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFlashSaleProducts(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<FlashSale> flashSaleOpt = flashSaleService.getFlashSaleById(id);
        if (flashSaleOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Flash sale not found");
            return ResponseEntity.ok(result);
        }
        
        FlashSale flashSale = flashSaleOpt.get();
        List<FlashSaleProduct> products = flashSaleService.getProductsByFlashSaleId(id);
        
        result.put("success", true);
        result.put("flashSale", toFlashSaleDTO(flashSale));
        result.put("products", products.stream()
            .map(this::toFlashSaleProductDTO)
            .collect(Collectors.toList()));
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Convert FlashSale to DTO
     */
    private Map<String, Object> toFlashSaleDTO(FlashSale flashSale) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("flashSaleId", flashSale.getFlashSaleId());
        dto.put("saleName", flashSale.getSaleName());
        dto.put("description", flashSale.getDescription());
        dto.put("startTime", flashSale.getStartTime());
        dto.put("endTime", flashSale.getEndTime());
        dto.put("status", flashSale.getStatus().name());
        dto.put("bannerImage", flashSale.getBannerImage());
        dto.put("remainingSeconds", flashSale.getRemainingSeconds());
        dto.put("secondsUntilStart", flashSale.getSecondsUntilStart());
        dto.put("isActive", flashSale.isActive());
        return dto;
    }
    
    /**
     * Convert FlashSaleProduct to DTO
     */
    private Map<String, Object> toFlashSaleProductDTO(FlashSaleProduct fsp) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("flashSaleProductId", fsp.getFlashSaleProductId());
        dto.put("productId", fsp.getProduct().getProductId());
        dto.put("productName", fsp.getProduct().getProductName());
        dto.put("productImage", fsp.getProduct().getProductImage());
        dto.put("flashSalePrice", fsp.getFlashSalePrice());
        dto.put("originalPrice", fsp.getOriginalPrice());
        dto.put("discountPercentage", fsp.getDiscountPercentage());
        dto.put("quantityLimit", fsp.getQuantityLimit());
        dto.put("soldQuantity", fsp.getSoldQuantity());
        dto.put("remainingQuantity", fsp.getRemainingQuantity());
        dto.put("maxQuantityPerUser", fsp.getMaxQuantityPerUser());
        dto.put("isAvailable", fsp.isAvailable());
        return dto;
    }
}

