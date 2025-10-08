package vn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.entity.ShippingProvider;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingProviderRepository extends JpaRepository<ShippingProvider, Long> {
    
    Optional<ShippingProvider> findByProviderName(String providerName);
    
    List<ShippingProvider> findByStatus(Boolean status);
    
    Page<ShippingProvider> findByProviderNameContainingIgnoreCase(String providerName, Pageable pageable);
    
    @Query("SELECT s FROM ShippingProvider s WHERE s.status = true ORDER BY s.providerName")
    List<ShippingProvider> findAllActiveProviders();
    
    boolean existsByProviderName(String providerName);
}