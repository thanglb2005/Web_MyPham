package vn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.entity.Order;
import vn.entity.Shop;
import vn.entity.User;
import vn.repository.OrderDetailRepository;
import vn.repository.ProductRepository;
import vn.repository.ShopRepository;
import vn.service.ShopService;
import vn.util.SlugUtils;

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
    public Optional<Shop> findByVendor(User vendor) {
        if (vendor == null) {
            return Optional.empty();
        }
        return shopRepository.findByVendor(vendor);
    }

    @Override
    public Optional<Shop> findByVendorId(Long vendorId) {
        if (vendorId == null) {
            return Optional.empty();
        }
        return shopRepository.findByVendor_UserId(vendorId);
    }

    @Override
    @Transactional
    public Shop registerShop(User vendor, Shop shop) {
        shop.setVendor(vendor);
        shop.setShopSlug(generateSlug(shop.getShopName(), null));
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
}
