package vn.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.entity.Provider;

import java.util.List;
import java.util.Optional;

public interface ProviderService {
    
    List<Provider> findAll();
    
    Page<Provider> findAll(Pageable pageable);
    
    Optional<Provider> findById(Long id);
    
    Provider save(Provider provider);
    
    void deleteById(Long id);
    
    List<Provider> findByStatus(Boolean status);
    
    Page<Provider> findByProviderNameContainingIgnoreCase(String providerName, Pageable pageable);
    
    List<Provider> findAllActiveProviders();
    
    boolean existsByProviderName(String providerName);
    
    Optional<Provider> findByProviderName(String providerName);
}
