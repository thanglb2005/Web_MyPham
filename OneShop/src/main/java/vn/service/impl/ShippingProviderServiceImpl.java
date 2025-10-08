package vn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.entity.ShippingProvider;
import vn.repository.ShippingProviderRepository;
import vn.service.ShippingProviderService;

import java.util.List;
import java.util.Optional;

@Service
public class ShippingProviderServiceImpl implements ShippingProviderService {
    
    @Autowired
    private ShippingProviderRepository shippingProviderRepository;
    
    @Override
    public List<ShippingProvider> findAll() {
        return shippingProviderRepository.findAll();
    }
    
    @Override
    public Page<ShippingProvider> findAll(Pageable pageable) {
        return shippingProviderRepository.findAll(pageable);
    }
    
    @Override
    public Optional<ShippingProvider> findById(Long id) {
        return shippingProviderRepository.findById(id);
    }
    
    @Override
    public ShippingProvider save(ShippingProvider shippingProvider) {
        return shippingProviderRepository.save(shippingProvider);
    }
    
    @Override
    public void deleteById(Long id) {
        shippingProviderRepository.deleteById(id);
    }
    
    @Override
    public List<ShippingProvider> findByStatus(Boolean status) {
        return shippingProviderRepository.findByStatus(status);
    }
    
    @Override
    public Page<ShippingProvider> findByProviderNameContainingIgnoreCase(String providerName, Pageable pageable) {
        return shippingProviderRepository.findByProviderNameContainingIgnoreCase(providerName, pageable);
    }
    
    @Override
    public List<ShippingProvider> findAllActiveProviders() {
        return shippingProviderRepository.findAllActiveProviders();
    }
    
    @Override
    public boolean existsByProviderName(String providerName) {
        return shippingProviderRepository.existsByProviderName(providerName);
    }
    
    @Override
    public Optional<ShippingProvider> findByProviderName(String providerName) {
        return shippingProviderRepository.findByProviderName(providerName);
    }
}