package vn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.entity.Brand;
import vn.repository.BrandRepository;
import vn.service.BrandService;

import java.util.List;
import java.util.Optional;

@Service
public class BrandServiceImpl implements BrandService {
    
    @Autowired
    private BrandRepository brandRepository;
    
    @Override
    public List<Brand> findAll() {
        return brandRepository.findAll();
    }
    
    @Override
    public Optional<Brand> findById(Long id) {
        return brandRepository.findById(id);
    }
    
    @Override
    public Brand save(Brand brand) {
        return brandRepository.save(brand);
    }
    
    @Override
    public void deleteById(Long id) {
        brandRepository.deleteById(id);
    }
    
    @Override
    public List<Brand> findByStatus(Boolean status) {
        return brandRepository.findByStatus(status);
    }
    
    @Override
    public Page<Brand> findByBrandNameContainingIgnoreCase(String brandName, Pageable pageable) {
        return brandRepository.findByBrandNameContainingIgnoreCase(brandName, pageable);
    }
    
    @Override
    public List<Brand> findAllActiveBrands() {
        return brandRepository.findAllActiveBrands();
    }
    
    @Override
    public boolean existsByBrandName(String brandName) {
        return brandRepository.existsByBrandName(brandName);
    }
    
    @Override
    public Optional<Brand> findByBrandName(String brandName) {
        return brandRepository.findByBrandName(brandName);
    }
}

