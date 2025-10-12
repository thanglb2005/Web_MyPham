package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.Shop;
import vn.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    Optional<Shop> findFirstByVendorOrderByCreatedAtAsc(User vendor);

    Optional<Shop> findFirstByVendor_UserIdOrderByCreatedAtAsc(Long vendorId);

    Optional<Shop> findByShopIdAndVendor_UserId(Long shopId, Long vendorId);

    List<Shop> findAllByVendor(User vendor);

    List<Shop> findAllByVendor_UserId(Long vendorId);

    List<Shop> findAllByStatus(Shop.ShopStatus status);

    boolean existsByShopSlug(String slug);

    Optional<Shop> findByShopSlug(String slug);

    @Query("SELECT COUNT(s) > 0 FROM Shop s WHERE s.shopSlug = :slug AND (:excludeId IS NULL OR s.shopId <> :excludeId)")
    boolean existsBySlugExcludingId(@Param("slug") String slug, @Param("excludeId") Long excludeId);

    /**
     * Find shop IDs by vendor
     */
    @Query("SELECT s.shopId FROM Shop s WHERE s.vendor = :vendor")
    List<Long> findShopIdsByVendor(@Param("vendor") User vendor);
}

