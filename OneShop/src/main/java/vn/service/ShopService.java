package vn.service;

import vn.entity.Shop;
import vn.entity.User;

import java.util.List;
import java.util.Optional;

public interface ShopService {

    Optional<Shop> findById(Long shopId);

    Optional<Shop> findFirstByVendor(User vendor);

    List<Shop> findAllByVendor(User vendor);

    Optional<Shop> findFirstByVendorId(Long vendorId);

    Optional<Shop> findByIdAndVendor(Long shopId, User vendor);

    Optional<Shop> findBySlug(String slug);

    List<Shop> findAllByVendorId(Long vendorId);

    List<Shop> findByStatus(Shop.ShopStatus status);

    List<Shop> findAll();

    Shop registerShop(User vendor, Shop shop);

    Shop updateShop(Shop shop);

    Shop refreshStatistics(Shop shop);

    Shop updateStatus(Long shopId, Shop.ShopStatus status, String rejectionReason);

    boolean isShopNameAvailable(String name, Long excludeId);

    String generateSlug(String shopName, Long excludeId);
}

