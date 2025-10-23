package vn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.entity.Provider;
import vn.repository.ProviderRepository;
import vn.service.ProviderService;

import java.util.List;
import java.util.Optional;

@Service
public class ProviderServiceImpl implements ProviderService {
    
    @Autowired
    private ProviderRepository providerRepository;
    
    @Override
    public List<Provider> findAll() {
        return providerRepository.findAll();
    }
    
    @Override
    public Page<Provider> findAll(Pageable pageable) {
        return providerRepository.findAll(pageable);
    }
    
    @Override
    public Optional<Provider> findById(Long id) {
        return providerRepository.findById(id);
    }
    
    @Override
    public Provider save(Provider provider) {
        return providerRepository.save(provider);
    }
    
    @Override
    public void deleteById(Long id) {
        providerRepository.deleteById(id);
    }
    
    @Override
    public List<Provider> findByStatus(Boolean status) {
        return providerRepository.findByStatus(status);
    }
    
    @Override
    public Page<Provider> findByProviderNameContainingIgnoreCase(String providerName, Pageable pageable) {
        return providerRepository.findByProviderNameContainingIgnoreCase(providerName, pageable);
    }
    
    @Override
    public List<Provider> findAllActiveProviders() {
        return providerRepository.findAllActiveProviders();
    }
    
    @Override
    public boolean existsByProviderName(String providerName) {
        return providerRepository.existsByProviderName(providerName);
    }
    
    @Override
    public Optional<Provider> findByProviderName(String providerName) {
        return providerRepository.findByProviderName(providerName);
    }
}
