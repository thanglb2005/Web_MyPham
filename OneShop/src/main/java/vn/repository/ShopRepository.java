package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.Shop;
import vn.entity.User;

import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    Optional<Shop> findByVendor(User vendor);

    Optional<Shop> findByVendor_UserId(Long vendorId);

    boolean existsByShopSlug(String slug);

    @Query("SELECT COUNT(s) > 0 FROM Shop s WHERE s.shopSlug = :slug AND (:excludeId IS NULL OR s.shopId <> :excludeId)")
    boolean existsBySlugExcludingId(@Param("slug") String slug, @Param("excludeId") Long excludeId);
}

