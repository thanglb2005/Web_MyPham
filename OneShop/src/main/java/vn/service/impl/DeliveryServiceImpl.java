package vn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.dto.DeliveryOptionsDTO;
import vn.dto.ShippingVoucherDTO;
import vn.entity.Promotion;
import vn.entity.Shop;
import vn.entity.ShippingProvider;
import vn.entity.User;
import vn.repository.PromotionRepository;
import vn.repository.ShopRepository;
import vn.repository.ShippingProviderRepository;
import vn.service.DeliveryService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ShippingProviderRepository shippingProviderRepository;
    
    @Autowired
    private PromotionRepository promotionRepository;

    @Override
    public DeliveryOptionsDTO checkDeliveryOptions(String customerCity, Long shopId) {
        DeliveryOptionsDTO options = new DeliveryOptionsDTO();

        // Lấy thông tin shop
        Optional<Shop> shopOpt = shopRepository.findById(shopId);
        if (shopOpt.isEmpty()) {
            options.setExpressAvailable(false);
            options.setSameCity(false);
            return options;
        }

        Shop shop = shopOpt.get();
        options.setShopCity(shop.getCity());

        // Kiểm tra có cùng tỉnh/thành không
        boolean sameCity = customerCity != null && 
                          shop.getCity() != null && 
                          customerCity.equalsIgnoreCase(shop.getCity());
        options.setSameCity(sameCity);

        if (sameCity) {
            // CÙNG TỈNH
            
            // Kiểm tra shop có cho phép giao hỏa tốc không
            boolean allowExpress = shop.getAllowExpress() != null && shop.getAllowExpress();
            
            // Kiểm tra shop có shipper riêng không (xử lý lazy loading)
            boolean hasShipper = false;
            try {
                Set<User> shippers = shop.getShippers();
                hasShipper = shippers != null && !shippers.isEmpty();
            } catch (Exception e) {
                // Lazy loading exception - check via query
                hasShipper = shopRepository.countShippersByShop(shopId) > 0;
            }
            
            // Có thể giao hỏa tốc nếu: shop cho phép + có shipper
            if (allowExpress && hasShipper) {
                options.setExpressAvailable(true);
                options.setExpressFee(30000.0);  // Phí hỏa tốc: 30k
                options.setExpressTime("2-4 giờ");
            } else {
                options.setExpressAvailable(false);
            }
            
            // Phí giao thường (cùng tỉnh)
            options.setStandardFee(30000.0);
            options.setStandardTime("1-2 ngày");
            
        } else {
            // KHÁC TỈNH - chỉ giao qua đơn vị vận chuyển
            options.setExpressAvailable(false);
            options.setStandardFee(50000.0);  // Phí xa hơn
            options.setStandardTime("2-3 ngày");
            
            // Lấy danh sách đơn vị vận chuyển
            List<ShippingProvider> providers = shippingProviderRepository.findAllActive();
            options.setShippingProviders(providers);
        }
        
        // Lấy danh sách voucher giảm ship
        List<ShippingVoucherDTO> vouchers = getShippingVouchers(shopId);
        options.setShippingVouchers(vouchers);

        return options;
    }
    
    /**
     * Lấy danh sách voucher giảm ship cho shop
     * Bao gồm: voucher hệ thống (shop_id = NULL) + voucher của shop
     * Lưu ý: Bình thường shop không được phát voucher ship, chỉ hệ thống mới phát
     */
    private List<ShippingVoucherDTO> getShippingVouchers(Long shopId) {
        List<Promotion> promotions = promotionRepository.findShippingVouchersByShop(shopId, LocalDateTime.now());
        
        return promotions.stream()
            .map(this::convertToVoucherDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Chuyển đổi Promotion entity sang ShippingVoucherDTO
     */
    private ShippingVoucherDTO convertToVoucherDTO(Promotion promotion) {
        ShippingVoucherDTO dto = new ShippingVoucherDTO();
        dto.setPromotionId(promotion.getPromotionId());
        dto.setPromotionCode(promotion.getPromotionCode());
        dto.setPromotionName(promotion.getPromotionName());
        dto.setDescription(promotion.getDescription());
        dto.setPromotionType(promotion.getPromotionType().name());
        dto.setDiscountValue(promotion.getDiscountValue());
        dto.setMinimumOrderAmount(promotion.getMinimumOrderAmount());
        dto.setMaximumDiscountAmount(promotion.getMaximumDiscountAmount());
        dto.setRemainingUsage(promotion.getUsageLimit() - promotion.getUsedCount());
        
        return dto;
    }
}

