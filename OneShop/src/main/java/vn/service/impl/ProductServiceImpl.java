package vn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.entity.Product;
import vn.repository.ProductRepository;
import vn.service.ProductService;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
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
    public List<Product> findByInventoryIds(List<Long> listProductId) {
        return productRepository.findByInventoryIds(listProductId);
    }
}

