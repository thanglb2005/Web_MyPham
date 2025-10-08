package vn.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.entity.ShippingProvider;

import java.util.List;
import java.util.Optional;

public interface ShippingProviderService {
    
    List<ShippingProvider> findAll();
    
    Page<ShippingProvider> findAll(Pageable pageable);
    
    Optional<ShippingProvider> findById(Long id);
    
    ShippingProvider save(ShippingProvider shippingProvider);
    
    void deleteById(Long id);
    
    List<ShippingProvider> findByStatus(Boolean status);
    
    Page<ShippingProvider> findByProviderNameContainingIgnoreCase(String providerName, Pageable pageable);
    
    List<ShippingProvider> findAllActiveProviders();
    
    boolean existsByProviderName(String providerName);
    
    Optional<ShippingProvider> findByProviderName(String providerName);
}