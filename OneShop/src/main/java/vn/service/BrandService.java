package vn.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.entity.Brand;

import java.util.List;
import java.util.Optional;

public interface BrandService {
    
    List<Brand> findAll();
    
    Page<Brand> findAll(Pageable pageable);
    
    Optional<Brand> findById(Long id);
    
    Brand save(Brand brand);
    
    void deleteById(Long id);
    
    List<Brand> findByStatus(Boolean status);
    
    Page<Brand> findByBrandNameContainingIgnoreCase(String brandName, Pageable pageable);
    
    List<Brand> findAllActiveBrands();
    
    boolean existsByBrandName(String brandName);
    
    Optional<Brand> findByBrandName(String brandName);
}

