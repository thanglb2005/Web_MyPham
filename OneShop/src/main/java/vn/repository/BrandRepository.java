package vn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.entity.Brand;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    
    Optional<Brand> findByBrandName(String brandName);
    
    List<Brand> findByStatus(Boolean status);
    
    Page<Brand> findByBrandNameContainingIgnoreCase(String brandName, Pageable pageable);
    
    @Query("SELECT b FROM Brand b WHERE b.status = true ORDER BY b.brandName")
    List<Brand> findAllActiveBrands();
    
    boolean existsByBrandName(String brandName);
}

