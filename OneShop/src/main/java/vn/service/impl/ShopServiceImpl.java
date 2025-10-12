package vn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.entity.Shop;
import vn.entity.User;
import vn.repository.OrderDetailRepository;
import vn.repository.ProductRepository;
import vn.repository.ShopRepository;
import vn.service.ShopService;
import vn.util.SlugUtils;

import java.util.List;
import java.util.Optional;

@Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Override
    public Optional<Shop> findById(Long shopId) {
        if (shopId == null) {
            return Optional.empty();
        }
        return shopRepository.findById(shopId);
    }

    @Override
    public Optional<Shop> findFirstByVendor(User vendor) {
        if (vendor == null) {
            return Optional.empty();
        }
        return shopRepository.findFirstByVendorOrderByCreatedAtAsc(vendor);
    }

    @Override
    public List<Shop> findAllByVendor(User vendor) {
        if (vendor == null) {
            return List.of();
        }
        return shopRepository.findAllByVendor(vendor);
    }

    @Override
    public Optional<Shop> findFirstByVendorId(Long vendorId) {
        if (vendorId == null) {
            return Optional.empty();
        }
        return shopRepository.findFirstByVendor_UserIdOrderByCreatedAtAsc(vendorId);
    }

    @Override
    public Optional<Shop> findByIdAndVendor(Long shopId, User vendor) {
        if (shopId == null || vendor == null) {
            return Optional.empty();
        }
        return shopRepository.findByShopIdAndVendor_UserId(shopId, vendor.getUserId());
    }

    @Override
    public List<Shop> findAllByVendorId(Long vendorId) {
        if (vendorId == null) {
            return List.of();
        }
        return shopRepository.findAllByVendor_UserId(vendorId);
    }

    @Override
    public Optional<Shop> findBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }
        return shopRepository.findByShopSlug(slug);
    }

    @Override
    public List<Shop> findByStatus(Shop.ShopStatus status) {
        if (status == null) {
            return List.of();
        }
        return shopRepository.findAllByStatus(status);
    }

    @Override
    public List<Shop> findAll() {
        return shopRepository.findAll();
    }

    @Override
    @Transactional
    public Shop registerShop(User vendor, Shop shop) {
        shop.setVendor(vendor);
        shop.setShopSlug(generateSlug(shop.getShopName(), null));
        shop.setStatus(Shop.ShopStatus.PENDING);
        shop.setApprovedAt(null);
        shop.setRejectionReason(null);
        shop.setTotalProducts(0);
        shop.setTotalOrders(0);
        shop.setTotalRevenue(0.0);
        return shopRepository.save(shop);
    }

    @Override
    @Transactional
    public Shop updateShop(Shop shop) {
        shop.setShopSlug(generateSlug(shop.getShopName(), shop.getShopId()));
        return shopRepository.save(shop);
    }

    @Override
    @Transactional
    public Shop updateStatus(Long shopId, Shop.ShopStatus status, String rejectionReason) {
        Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new IllegalArgumentException("Shop not found"));
        shop.setStatus(status);
        if (status == Shop.ShopStatus.REJECTED) {
            shop.setRejectionReason(rejectionReason);
            shop.setApprovedAt(null);
        } else if (status == Shop.ShopStatus.ACTIVE) {
            shop.setRejectionReason(null);
            shop.setApprovedAt(shop.getApprovedAt() != null ? shop.getApprovedAt() : java.time.LocalDateTime.now());
        }
        return shopRepository.save(shop);
    }

    @Override
    @Transactional
    public Shop refreshStatistics(Shop shop) {
        if (shop == null || shop.getShopId() == null) {
            return shop;
        }

        Long shopId = shop.getShopId();
        long productCount = productRepository.countByShopShopId(shopId);
        Long orderCount = orderDetailRepository.countDistinctOrdersByShop(shopId);
        Double revenue = orderDetailRepository.sumRevenueByShop(shopId);

        shop.setTotalProducts(Math.toIntExact(productCount));
        shop.setTotalOrders(orderCount != null ? orderCount.intValue() : 0);
        shop.setTotalRevenue(revenue != null ? revenue : 0.0);

        return shopRepository.save(shop);
    }

    @Override
    public boolean isShopNameAvailable(String name, Long excludeId) {
        String slug = SlugUtils.toSlug(name);
        if (slug.isEmpty()) {
            return false;
        }
        return !shopRepository.existsBySlugExcludingId(slug, excludeId);
    }

    @Override
    public String generateSlug(String shopName, Long excludeId) {
        String baseSlug = SlugUtils.toSlug(shopName);
        if (baseSlug.isEmpty()) {
            baseSlug = "shop";
        }
        String candidate = baseSlug;
        int suffix = 1;
        while (shopRepository.existsBySlugExcludingId(candidate, excludeId)) {
            candidate = baseSlug + "-" + suffix++;
        }
        return candidate;
    }

    @Override
    public List<Long> findShopIdsByVendor(User vendor) {
        if (vendor == null) {
            return List.of();
        }
        return shopRepository.findShopIdsByVendor(vendor);
    }
}

