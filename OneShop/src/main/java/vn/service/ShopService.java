package vn.service;

import vn.entity.Shop;
import vn.entity.User;

import java.util.Optional;

public interface ShopService {

    Optional<Shop> findByVendor(User vendor);

    Optional<Shop> findByVendorId(Long vendorId);

    Shop registerShop(User vendor, Shop shop);

    Shop updateShop(Shop shop);

    Shop refreshStatistics(Shop shop);

    boolean isShopNameAvailable(String name, Long excludeId);

    String generateSlug(String shopName, Long excludeId);
}

