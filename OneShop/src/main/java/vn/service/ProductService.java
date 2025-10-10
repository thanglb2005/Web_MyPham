package vn.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    
    List<Product> findAll();
    
    Optional<Product> findById(Long id);
    
    Product save(Product product);
    
    void deleteById(Long id);
    
    List<Product> findByStatus(Boolean status);
    
    Page<Product> findByProductNameContainingIgnoreCase(String productName, Pageable pageable);
    
    List<Product> findByCategoryId(Long categoryId);
    
    List<Product> findByBrandId(Long brandId);
    
    List<Product> findActiveProductsOrderByDate();
    
    Optional<Product> findByProductName(String productName);
    
    // Methods from green-shop
    List<Product> listProductByCategory(Long categoryId);
    
    List<Product> listProductByCategory10(Long categoryId);
    
    List<Product> listProductNew20();
    
    List<Product> searchProduct(String productName);
    
    List<Object[]> listCategoryByProductName();
    
    List<Object[]> bestSaleProduct20();
    
    List<Product> findByInventoryIds(List<Long> listProductId);

    List<Product> findByShopId(Long shopId);

    Page<Product> findByShopId(Long shopId, Pageable pageable);

    Optional<Product> findByIdAndShop(Long productId, Long shopId);

    long countByShopId(Long shopId);
}

