package vn.service.impl;

import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.entity.FlashSale;
import vn.entity.FlashSaleOrder;
import vn.entity.FlashSaleProduct;
import vn.entity.Product;
import vn.entity.User;
import vn.repository.FlashSaleOrderRepository;
import vn.repository.FlashSaleProductRepository;
import vn.repository.FlashSaleRepository;
import vn.repository.ProductRepository;
import vn.service.FlashSaleService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Flash Sale management
 * @author OneShop Team
 */
@Service
@Transactional
public class FlashSaleServiceImpl implements FlashSaleService {
    
    @Autowired
    private FlashSaleRepository flashSaleRepository;
    
    @Autowired
    private FlashSaleProductRepository flashSaleProductRepository;
    
    @Autowired
    private FlashSaleOrderRepository flashSaleOrderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private vn.repository.OrderRepository orderRepository;
    
    @Autowired
    private vn.repository.UserRepository userRepository;
    
    // ===== CRUD Operations =====
    
    @Override
    public List<FlashSale> getAllFlashSales() {
        return flashSaleRepository.findAll();
    }
    
    @Override
    public Page<FlashSale> getFlashSalesWithPagination(Pageable pageable) {
        return flashSaleRepository.findAll(pageable);
    }
    
    @Override
    public Optional<FlashSale> getFlashSaleById(Long id) {
        return flashSaleRepository.findById(id);
    }
    
    @Override
    public FlashSale createFlashSale(FlashSale flashSale, User creator) {
        if (flashSale.getStartTime() == null || flashSale.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and end time are required");
        }
        
        if (!validateFlashSaleDates(flashSale.getStartTime(), flashSale.getEndTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        
        flashSale.setCreatedBy(creator);
        flashSale.setStatus(FlashSale.FlashSaleStatus.DRAFT);
        return flashSaleRepository.save(flashSale);
    }
    
    @Override
    public FlashSale updateFlashSale(Long id, FlashSale flashSale) {
        Optional<FlashSale> existingOpt = flashSaleRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new RuntimeException("Flash sale not found with id: " + id);
        }
        
        FlashSale existing = existingOpt.get();
        
        // Only allow update if not active or ended
        if (existing.getStatus() == FlashSale.FlashSaleStatus.ACTIVE) {
            throw new RuntimeException("Cannot update active flash sale");
        }
        
        if (existing.getStatus() == FlashSale.FlashSaleStatus.ENDED) {
            throw new RuntimeException("Cannot update ended flash sale");
        }
        
        existing.setSaleName(flashSale.getSaleName());
        existing.setDescription(flashSale.getDescription());
        existing.setStartTime(flashSale.getStartTime());
        existing.setEndTime(flashSale.getEndTime());
        existing.setBannerImage(flashSale.getBannerImage());
        
        if (!validateFlashSaleDates(existing.getStartTime(), existing.getEndTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        
        return flashSaleRepository.save(existing);
    }
    
    @Override
    public void deleteFlashSale(Long id) {
        Optional<FlashSale> flashSaleOpt = flashSaleRepository.findById(id);
        if (flashSaleOpt.isEmpty()) {
            throw new RuntimeException("Flash sale not found with id: " + id);
        }
        
        FlashSale flashSale = flashSaleOpt.get();
        
        // Only allow delete if not active
        if (flashSale.getStatus() == FlashSale.FlashSaleStatus.ACTIVE) {
            throw new RuntimeException("Cannot delete active flash sale");
        }
        
        flashSaleRepository.delete(flashSale);
    }
    
    // ===== Product Management =====
    
    @Override
    public FlashSaleProduct addProductToFlashSale(Long flashSaleId, Long productId, 
                                                  BigDecimal flashSalePrice, 
                                                  Integer quantityLimit,
                                                  Integer maxQuantityPerUser,
                                                  Integer displayOrder) {
        Optional<FlashSale> flashSaleOpt = flashSaleRepository.findById(flashSaleId);
        if (flashSaleOpt.isEmpty()) {
            throw new RuntimeException("Flash sale not found with id: " + flashSaleId);
        }
        
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found with id: " + productId);
        }
        
        FlashSale flashSale = flashSaleOpt.get();
        Product product = productOpt.get();
        
        // Check if product already in flash sale
        Optional<FlashSaleProduct> existingOpt = flashSaleProductRepository
            .findByFlashSaleIdAndProductId(flashSaleId, productId);
        if (existingOpt.isPresent()) {
            throw new RuntimeException("Product already in flash sale");
        }
        
        // Validate
        if (!validateFlashSaleProduct(productId, flashSalePrice, quantityLimit)) {
            throw new IllegalArgumentException("Invalid flash sale product data");
        }
        
        BigDecimal originalPrice = BigDecimal.valueOf(product.getPrice());
        if (flashSalePrice.compareTo(originalPrice) >= 0) {
            throw new IllegalArgumentException("Flash sale price must be less than original price");
        }
        
        if (quantityLimit > product.getQuantity()) {
            throw new IllegalArgumentException("Quantity limit cannot exceed product stock");
        }
        
        FlashSaleProduct flashSaleProduct = new FlashSaleProduct();
        flashSaleProduct.setFlashSale(flashSale);
        flashSaleProduct.setProduct(product);
        flashSaleProduct.setFlashSalePrice(flashSalePrice);
        flashSaleProduct.setOriginalPrice(originalPrice);
        flashSaleProduct.setQuantityLimit(quantityLimit);
        flashSaleProduct.setMaxQuantityPerUser(maxQuantityPerUser != null ? maxQuantityPerUser : 1);
        flashSaleProduct.setDisplayOrder(displayOrder != null ? displayOrder : 0);
        flashSaleProduct.setIsActive(true);
        
        return flashSaleProductRepository.save(flashSaleProduct);
    }
    
    @Override
    public void removeProductFromFlashSale(Long flashSaleId, Long productId) {
        Optional<FlashSaleProduct> fspOpt = flashSaleProductRepository
            .findByFlashSaleIdAndProductId(flashSaleId, productId);
        
        if (fspOpt.isEmpty()) {
            throw new RuntimeException("Product not found in flash sale");
        }
        
        FlashSaleProduct fsp = fspOpt.get();
        
        // Check if flash sale is active
        if (fsp.getFlashSale().getStatus() == FlashSale.FlashSaleStatus.ACTIVE) {
            throw new RuntimeException("Cannot remove product from active flash sale");
        }
        
        flashSaleProductRepository.delete(fsp);
    }
    
    @Override
    public FlashSaleProduct updateFlashSaleProduct(Long flashSaleProductId, 
                                                   BigDecimal flashSalePrice,
                                                   Integer quantityLimit,
                                                   Integer maxQuantityPerUser,
                                                   Integer displayOrder) {
        Optional<FlashSaleProduct> fspOpt = flashSaleProductRepository.findById(flashSaleProductId);
        if (fspOpt.isEmpty()) {
            throw new RuntimeException("Flash sale product not found");
        }
        
        FlashSaleProduct fsp = fspOpt.get();
        
        // Check if flash sale is active
        if (fsp.getFlashSale().getStatus() == FlashSale.FlashSaleStatus.ACTIVE) {
            throw new RuntimeException("Cannot update product in active flash sale");
        }
        
        if (flashSalePrice != null) {
            if (flashSalePrice.compareTo(fsp.getOriginalPrice()) >= 0) {
                throw new IllegalArgumentException("Flash sale price must be less than original price");
            }
            fsp.setFlashSalePrice(flashSalePrice);
        }
        
        if (quantityLimit != null) {
            if (quantityLimit > fsp.getProduct().getQuantity()) {
                throw new IllegalArgumentException("Quantity limit cannot exceed product stock");
            }
            if (quantityLimit < fsp.getSoldQuantity()) {
                throw new IllegalArgumentException("Quantity limit cannot be less than sold quantity");
            }
            fsp.setQuantityLimit(quantityLimit);
        }
        
        if (maxQuantityPerUser != null) {
            fsp.setMaxQuantityPerUser(maxQuantityPerUser);
        }
        
        if (displayOrder != null) {
            fsp.setDisplayOrder(displayOrder);
        }
        
        return flashSaleProductRepository.save(fsp);
    }
    
    @Override
    public List<FlashSaleProduct> getProductsByFlashSaleId(Long flashSaleId) {
        return flashSaleProductRepository.findByFlashSaleFlashSaleId(flashSaleId);
    }
    
    @Override
    public Page<FlashSaleProduct> getProductsByFlashSaleId(Long flashSaleId, Pageable pageable) {
        return flashSaleProductRepository.findByFlashSaleFlashSaleId(flashSaleId, pageable);
    }
    
    @Override
    public Optional<FlashSaleProduct> getFlashSaleProductById(Long flashSaleProductId) {
        return flashSaleProductRepository.findById(flashSaleProductId);
    }
    
    @Override
    public Optional<FlashSaleProduct> getFlashSaleProduct(Long flashSaleId, Long productId) {
        return flashSaleProductRepository.findByFlashSaleIdAndProductId(flashSaleId, productId);
    }
    
    // ===== Status Management =====
    
    @Override
    public FlashSale activateFlashSale(Long id) {
        Optional<FlashSale> flashSaleOpt = flashSaleRepository.findById(id);
        if (flashSaleOpt.isEmpty()) {
            throw new RuntimeException("Flash sale not found with id: " + id);
        }
        
        FlashSale flashSale = flashSaleOpt.get();
        
        if (flashSale.getStatus() == FlashSale.FlashSaleStatus.ACTIVE) {
            return flashSale; // Already active
        }
        
        if (flashSale.getStatus() == FlashSale.FlashSaleStatus.ENDED) {
            throw new RuntimeException("Cannot activate ended flash sale");
        }
        
        if (flashSale.getStatus() == FlashSale.FlashSaleStatus.CANCELLED) {
            throw new RuntimeException("Cannot activate cancelled flash sale");
        }
        
        flashSale.setStatus(FlashSale.FlashSaleStatus.ACTIVE);
        return flashSaleRepository.save(flashSale);
    }
    
    @Override
    public FlashSale cancelFlashSale(Long id) {
        Optional<FlashSale> flashSaleOpt = flashSaleRepository.findById(id);
        if (flashSaleOpt.isEmpty()) {
            throw new RuntimeException("Flash sale not found with id: " + id);
        }
        
        FlashSale flashSale = flashSaleOpt.get();
        
        if (flashSale.getStatus() == FlashSale.FlashSaleStatus.ACTIVE) {
            throw new RuntimeException("Cannot cancel active flash sale");
        }
        
        if (flashSale.getStatus() == FlashSale.FlashSaleStatus.ENDED) {
            throw new RuntimeException("Cannot cancel ended flash sale");
        }
        
        flashSale.setStatus(FlashSale.FlashSaleStatus.CANCELLED);
        return flashSaleRepository.save(flashSale);
    }
    
    @Override
    public FlashSale endFlashSale(Long id) {
        Optional<FlashSale> flashSaleOpt = flashSaleRepository.findById(id);
        if (flashSaleOpt.isEmpty()) {
            throw new RuntimeException("Flash sale not found with id: " + id);
        }
        
        FlashSale flashSale = flashSaleOpt.get();
        flashSale.setStatus(FlashSale.FlashSaleStatus.ENDED);
        return flashSaleRepository.save(flashSale);
    }
    
    @Override
    public FlashSale scheduleFlashSale(Long id) {
        Optional<FlashSale> flashSaleOpt = flashSaleRepository.findById(id);
        if (flashSaleOpt.isEmpty()) {
            throw new RuntimeException("Flash sale not found with id: " + id);
        }
        
        FlashSale flashSale = flashSaleOpt.get();
        
        if (flashSale.getStatus() == FlashSale.FlashSaleStatus.ACTIVE) {
            throw new RuntimeException("Cannot schedule active flash sale");
        }
        
        if (flashSale.getStatus() == FlashSale.FlashSaleStatus.ENDED) {
            throw new RuntimeException("Cannot schedule ended flash sale");
        }
        
        if (flashSale.getStatus() == FlashSale.FlashSaleStatus.CANCELLED) {
            throw new RuntimeException("Cannot schedule cancelled flash sale");
        }
        
        flashSale.setStatus(FlashSale.FlashSaleStatus.SCHEDULED);
        return flashSaleRepository.save(flashSale);
    }
    
    // ===== Auto Scheduler =====
    
    @Scheduled(cron = "0 * * * * ?") // Run every minute
    public void checkAndActivateScheduledFlashSales() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<FlashSale> toActivate = flashSaleRepository.findFlashSalesToActivate(now);
            
            for (FlashSale flashSale : toActivate) {
                flashSale.setStatus(FlashSale.FlashSaleStatus.ACTIVE);
                flashSaleRepository.save(flashSale);
                System.out.println("Activated flash sale: " + flashSale.getSaleName() + " (ID: " + flashSale.getFlashSaleId() + ")");
            }
        } catch (Exception e) {
            System.err.println("Error activating scheduled flash sales: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Scheduled(cron = "0 * * * * ?") // Run every minute
    public void checkAndEndActiveFlashSales() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<FlashSale> toEnd = flashSaleRepository.findFlashSalesToEnd(now);
            
            for (FlashSale flashSale : toEnd) {
                flashSale.setStatus(FlashSale.FlashSaleStatus.ENDED);
                flashSaleRepository.save(flashSale);
                System.out.println("Ended flash sale: " + flashSale.getSaleName() + " (ID: " + flashSale.getFlashSaleId() + ")");
            }
        } catch (Exception e) {
            System.err.println("Error ending active flash sales: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ===== Query Operations =====
    
    @Override
    public List<FlashSale> getActiveFlashSales() {
        LocalDateTime now = LocalDateTime.now();
        return flashSaleRepository.findActiveFlashSales(now);
    }
    
    @Override
    public Page<FlashSale> getActiveFlashSales(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return flashSaleRepository.findActiveFlashSales(now, pageable);
    }
    
    @Override
    public List<FlashSale> getUpcomingFlashSales() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextHour = now.plusHours(1);
        return flashSaleRepository.findUpcomingFlashSales(now, nextHour);
    }
    
    @Override
    public Page<FlashSale> getUpcomingFlashSales(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextHour = now.plusHours(1);
        return flashSaleRepository.findUpcomingFlashSales(now, nextHour, pageable);
    }
    
    @Override
    public List<FlashSale> getFlashSalesByStatus(FlashSale.FlashSaleStatus status) {
        return flashSaleRepository.findByStatus(status);
    }
    
    @Override
    public Page<FlashSale> getFlashSalesByStatus(FlashSale.FlashSaleStatus status, Pageable pageable) {
        return flashSaleRepository.findByStatus(status, pageable);
    }
    
    @Override
    public Page<FlashSale> searchFlashSalesByName(String name, Pageable pageable) {
        return flashSaleRepository.findByNameContainingIgnoreCase(name, pageable);
    }
    
    @Override
    public List<FlashSale> getFlashSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return flashSaleRepository.findByDateRange(startDate, endDate);
    }
    
    // ===== Validation & Business Logic =====
    
    @Override
    public boolean canUserPurchase(Long flashSaleId, Long productId, Long userId, Integer quantity) {
        Optional<FlashSaleProduct> fspOpt = flashSaleProductRepository
            .findByFlashSaleIdAndProductId(flashSaleId, productId);
        
        if (fspOpt.isEmpty()) {
            return false;
        }
        
        FlashSaleProduct fsp = fspOpt.get();
        
        // Check if available
        if (!fsp.isAvailable()) {
            return false;
        }
        
        // Check quantity limit
        if (quantity > fsp.getRemainingQuantity()) {
            return false;
        }
        
        // Check user purchase limit
        Integer userPurchased = flashSaleOrderRepository
            .countByFlashSaleIdAndUserIdAndProductId(flashSaleId, userId, productId);
        
        if (userPurchased == null) {
            userPurchased = 0;
        }
        
        if (userPurchased + quantity > fsp.getMaxQuantityPerUser()) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean isProductInFlashSale(Long productId) {
        LocalDateTime now = LocalDateTime.now();
        Optional<FlashSaleProduct> fspOpt = flashSaleProductRepository
            .findByProductIdAndActive(productId, now);
        return fspOpt.isPresent();
    }
    
    @Override
    public BigDecimal getFlashSalePrice(Long productId) {
        Optional<FlashSaleProduct> fspOpt = getActiveFlashSaleProduct(productId);
        return fspOpt.map(FlashSaleProduct::getFlashSalePrice).orElse(null);
    }
    
    @Override
    public Optional<FlashSaleProduct> getActiveFlashSaleProduct(Long productId) {
        LocalDateTime now = LocalDateTime.now();
        return flashSaleProductRepository.findByProductIdAndActive(productId, now);
    }
    
    @Override
    public boolean validateFlashSaleDates(LocalDateTime startTime, LocalDateTime endTime) {
        return endTime != null && startTime != null && endTime.isAfter(startTime);
    }
    
    @Override
    public boolean validateFlashSaleProduct(Long productId, BigDecimal flashSalePrice, Integer quantityLimit) {
        if (productId == null || flashSalePrice == null || quantityLimit == null) {
            return false;
        }
        
        if (flashSalePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        if (quantityLimit <= 0) {
            return false;
        }
        
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return false;
        }
        
        Product product = productOpt.get();
        BigDecimal originalPrice = BigDecimal.valueOf(product.getPrice());
        
        if (flashSalePrice.compareTo(originalPrice) >= 0) {
            return false;
        }
        
        if (quantityLimit > product.getQuantity()) {
            return false;
        }
        
        return true;
    }
    
    // ===== Business Operations =====
    
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public void recordFlashSalePurchase(Long flashSaleId, Long orderId, Long productId, 
                                       Long userId, Integer quantity, BigDecimal flashSalePrice) {
        // Lock the flash sale product for update
        Optional<FlashSaleProduct> fspOpt = flashSaleProductRepository
            .findByFlashSaleIdAndProductId(flashSaleId, productId);
        
        if (fspOpt.isEmpty()) {
            throw new RuntimeException("Flash sale product not found");
        }
        
        FlashSaleProduct fsp = fspOpt.get();
        
        // Validate quantity
        if (quantity > fsp.getRemainingQuantity()) {
            throw new RuntimeException("Insufficient quantity in flash sale");
        }
        
        // Update sold quantity
        fsp.setSoldQuantity(fsp.getSoldQuantity() + quantity);
        flashSaleProductRepository.save(fsp);
        
        // Get Order and User entities
        vn.entity.Order orderEntity = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        User userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Create flash sale order record
        FlashSaleOrder flashSaleOrder = new FlashSaleOrder();
        flashSaleOrder.setFlashSaleId(flashSaleId);
        flashSaleOrder.setOrder(orderEntity);
        flashSaleOrder.setUser(userEntity);
        flashSaleOrder.setProductId(productId);
        flashSaleOrder.setQuantity(quantity);
        flashSaleOrder.setFlashSalePrice(flashSalePrice);
        flashSaleOrder.setTotalAmount(flashSalePrice.multiply(BigDecimal.valueOf(quantity)));
        
        flashSaleOrderRepository.save(flashSaleOrder);
    }
    
    @Override
    public Integer getTotalSoldQuantity(Long flashSaleId, Long productId) {
        Integer sold = flashSaleOrderRepository.countTotalQuantitySold(flashSaleId, productId);
        return sold != null ? sold : 0;
    }
    
    @Override
    public Integer getUserPurchasedQuantity(Long flashSaleId, Long productId, Long userId) {
        Integer purchased = flashSaleOrderRepository
            .countByFlashSaleIdAndUserIdAndProductId(flashSaleId, userId, productId);
        return purchased != null ? purchased : 0;
    }
    
    // ===== Statistics =====
    
    @Override
    public long getTotalFlashSaleCount() {
        return flashSaleRepository.count();
    }
    
    @Override
    public long getActiveFlashSaleCount() {
        LocalDateTime now = LocalDateTime.now();
        return flashSaleRepository.countActiveFlashSales(now);
    }
    
    @Override
    public long getFlashSaleCountByStatus(FlashSale.FlashSaleStatus status) {
        return flashSaleRepository.countByStatus(status);
    }
}

