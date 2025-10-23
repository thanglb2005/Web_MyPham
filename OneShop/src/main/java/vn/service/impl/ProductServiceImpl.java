package vn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.entity.Product;
import vn.repository.ProductRepository;
import vn.repository.CommentRepository;
import vn.dto.ShopProductStatistics;
import vn.entity.Shop;
import vn.service.ProductService;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CommentRepository commentRepository;
    
    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }
    
    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }
    
    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }
    
    @Override
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public boolean softDelete(Long id) {
        return productRepository.findById(id).map(p -> {
            // Mark product as inactive/unavailable
            p.setStatus(false);
            p.setQuantity(0);
            productRepository.save(p);
            return true;
        }).orElse(false);
    }
    
    @Override
    public List<Product> findByStatus(Boolean status) {
        return productRepository.findByStatus(status);
    }
    
    @Override
    public Page<Product> findByProductNameContainingIgnoreCase(String productName, Pageable pageable) {
        return productRepository.findByProductNameContainingIgnoreCase(productName, pageable);
    }
    
    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
    
    @Override
    public List<Product> findByBrandId(Long brandId) {
        return productRepository.findByBrandId(brandId);
    }
    
    @Override
    public List<Product> findActiveProductsOrderByDate() {
        return productRepository.findActiveProductsOrderByDate();
    }
    
    @Override
    public Optional<Product> findByProductName(String productName) {
        return productRepository.findByProductName(productName);
    }
    
    // Methods from green-shop
    @Override
    public List<Product> listProductByCategory(Long categoryId) {
        return productRepository.listProductByCategory(categoryId);
    }
    
    @Override
    public List<Product> listProductByCategory10(Long categoryId) {
        return productRepository.listProductByCategory10(categoryId);
    }
    
    @Override
    public List<Product> listProductNew20() {
        return productRepository.listProductNew20();
    }
    
    @Override
    public List<Product> searchProduct(String productName) {
        return productRepository.searchProduct(productName);
    }
    
    @Override
    public List<Object[]> listCategoryByProductName() {
        return productRepository.listCategoryByProductName();
    }
    
    @Override
    public List<Object[]> bestSaleProduct20() {
        return productRepository.bestSaleProduct20();
    }
    

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> findByShopAndName(Long shopId, String productName, Pageable pageable) {
        return productRepository.findByShopShopIdAndProductNameContainingIgnoreCase(shopId, productName, pageable);
    }

    @Override
    public List<ShopProductStatistics> getShopProductStatistics() {
        List<Object[]> rows = productRepository.getShopProductStatistics();
        return rows.stream()
                .map(row -> ShopProductStatistics.builder()
                        .shopId(row[0] != null ? ((Number) row[0]).longValue() : null)
                        .shopName((String) row[1])
                        .status(row[2] != null ? Shop.ShopStatus.valueOf(((String) row[2]).toUpperCase()) : Shop.ShopStatus.PENDING)
                        .vendorName(row[3] != null ? (String) row[3] : "Không xác định")
                        .productCount(row[4] != null ? ((Number) row[4]).longValue() : 0L)
                        .totalQuantity(row[5] != null ? ((Number) row[5]).longValue() : 0L)
                        .totalInventoryValue(row[6] != null ? ((Number) row[6]).doubleValue() : 0.0)
                        .build())
                .toList();
    }

    @Override
    public List<Product> findByInventoryIds(List<Long> listProductId) {
        return productRepository.findByInventoryIds(listProductId);
    }

    @Override
    public List<Product> findByShopId(Long shopId) {
        return productRepository.findByShopShopId(shopId);
    }

    @Override
    public Page<Product> findByShopId(Long shopId, Pageable pageable) {
        return productRepository.findByShopShopId(shopId, pageable);
    }

    @Override
    public Optional<Product> findByIdAndShop(Long productId, Long shopId) {
        return productRepository.findByProductIdAndShopShopId(productId, shopId);
    }

    @Override
    public long countByShopId(Long shopId) {
        return productRepository.countByShopShopId(shopId);
    }

    @Override
    public java.util.Map<Long, Double> getAverageRatings(java.util.Collection<Long> productIds) {
        java.util.Map<Long, Double> result = new java.util.HashMap<>();
        if (productIds == null || productIds.isEmpty()) return result;
        for (Long pid : productIds) {
            java.util.List<vn.entity.Comment> cmts = commentRepository.findByProduct(pid);
            double avg = 0.0;
            if (cmts != null && !cmts.isEmpty()) {
                avg = cmts.stream().filter(c -> c.getRating() != null)
                        .mapToDouble(vn.entity.Comment::getRating).average().orElse(0.0);
            }
            result.put(pid, avg);
        }
        return result;
    }
}

