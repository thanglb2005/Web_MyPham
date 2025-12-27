package vn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.dto.CartByShopDTO;
import vn.entity.CartItem;
import vn.entity.CartItemEntity;
import vn.entity.FlashSaleProduct;
import vn.entity.Product;
import vn.entity.Shop;
import vn.entity.User;
import vn.repository.CartItemRepository;
import vn.service.CartService;
import vn.service.FlashSaleService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for Cart operations
 * @author OneShop Team
 */
@Service
@Transactional
public class CartServiceImpl implements CartService {
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private FlashSaleService flashSaleService;
    
    @Override
    public CartItemEntity addToCart(User user, Product product, Integer quantity) {
        // Validate quantity against stock
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        if (quantity > product.getQuantity()) {
            throw new IllegalArgumentException("Số lượng không được vượt quá tồn kho (" + product.getQuantity() + ")");
        }
        
        // Check if product is in flash sale
        Optional<FlashSaleProduct> flashSaleProductOpt = flashSaleService.getActiveFlashSaleProduct(product.getProductId());
        Double unitPrice = product.getPrice();
        Long flashSaleId = null;
        
        if (flashSaleProductOpt.isPresent()) {
            FlashSaleProduct fsp = flashSaleProductOpt.get();
            flashSaleId = fsp.getFlashSale().getFlashSaleId();
            
            // Validate flash sale quantity and user limit
            if (!flashSaleService.canUserPurchase(flashSaleId, product.getProductId(), user.getUserId(), quantity)) {
                int remaining = fsp.getRemainingQuantity();
                int userPurchased = flashSaleService.getUserPurchasedQuantity(flashSaleId, product.getProductId(), user.getUserId());
                int maxPerUser = fsp.getMaxQuantityPerUser();
                
                if (remaining < quantity) {
                    throw new IllegalArgumentException("Flash sale chỉ còn " + remaining + " sản phẩm");
                }
                if (userPurchased + quantity > maxPerUser) {
                    throw new IllegalArgumentException("Bạn chỉ có thể mua tối đa " + maxPerUser + " sản phẩm trong flash sale này. Đã mua: " + userPurchased);
                }
            }
            
            // Use flash sale price
            unitPrice = fsp.getFlashSalePrice().doubleValue();
        }
        
        Optional<CartItemEntity> existingItem = cartItemRepository.findByUserAndProduct(user, product);
        
        if (existingItem.isPresent()) {
            CartItemEntity item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            
            // Validate total quantity against stock
            if (newQuantity > product.getQuantity()) {
                throw new IllegalArgumentException("Tổng số lượng không được vượt quá tồn kho (" + product.getQuantity() + ")");
            }
            
            // If in flash sale, validate again with new total quantity
            if (flashSaleProductOpt.isPresent() && flashSaleId != null) {
                if (!flashSaleService.canUserPurchase(flashSaleId, product.getProductId(), user.getUserId(), newQuantity)) {
                    FlashSaleProduct fsp = flashSaleProductOpt.get();
                    int remaining = fsp.getRemainingQuantity();
                    int userPurchased = flashSaleService.getUserPurchasedQuantity(flashSaleId, product.getProductId(), user.getUserId());
                    int maxPerUser = fsp.getMaxQuantityPerUser();
                    
                    if (remaining < newQuantity) {
                        throw new IllegalArgumentException("Flash sale chỉ còn " + remaining + " sản phẩm");
                    }
                    if (userPurchased + newQuantity > maxPerUser) {
                        throw new IllegalArgumentException("Bạn chỉ có thể mua tối đa " + maxPerUser + " sản phẩm trong flash sale này");
                    }
                }
                // Update price to flash sale price if changed
                item.setUnitPrice(unitPrice);
            }
            
            item.setQuantity(newQuantity);
            return cartItemRepository.save(item);
        } else {
            CartItemEntity newItem = new CartItemEntity();
            newItem.setUser(user);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setUnitPrice(unitPrice); // Use flash sale price if available
            // Set shop from product
            newItem.setShop(product.getShop());
            newItem.calculateTotalPrice();
            
            return cartItemRepository.save(newItem);
        }
    }
    
    @Override
    public CartItemEntity updateCartItemQuantity(User user, Product product, Integer quantity) {
        // Validate quantity against stock
        if (quantity > product.getQuantity()) {
            throw new IllegalArgumentException("Số lượng không được vượt quá tồn kho (" + product.getQuantity() + ")");
        }
        
        Optional<CartItemEntity> existingItem = cartItemRepository.findByUserAndProduct(user, product);
        
        if (existingItem.isPresent()) {
            CartItemEntity item = existingItem.get();
            if (quantity <= 0) {
                cartItemRepository.delete(item);
                return null;
            } else {
                item.setQuantity(quantity);
                return cartItemRepository.save(item);
            }
        }
        
        return null;
    }
    
    @Override
    public void removeFromCart(User user, Product product) {
        cartItemRepository.deleteByUserAndProduct(user, product);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CartItemEntity> getCartItems(User user) {
        return cartItemRepository.findByUser(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Integer getCartItemCount(User user) {
        return (int) cartItemRepository.countByUser(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Double getCartTotalPrice(User user) {
        List<CartItemEntity> cartItems = getCartItems(user);
        return cartItems.stream()
                .mapToDouble(item -> {
                    // Check if product is in flash sale (use flash sale price if available)
                    Optional<FlashSaleProduct> fspOpt = flashSaleService.getActiveFlashSaleProduct(item.getProduct().getProductId());
                    double unitPrice = fspOpt.isPresent() ? 
                        fspOpt.get().getFlashSalePrice().doubleValue() : item.getUnitPrice();
                    
                    // Apply product discount only if not in flash sale
                    if (!fspOpt.isPresent()) {
                        int discount = item.getProduct().getDiscount() != null ? item.getProduct().getDiscount() : 0;
                        unitPrice = unitPrice * (1.0 - (discount / 100.0));
                    }
                    
                    return unitPrice * item.getQuantity();
                })
                .sum();
    }
    
    @Override
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isProductInCart(User user, Product product) {
        return cartItemRepository.existsByUserAndProduct(user, product);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CartItemEntity getCartItem(User user, Product product) {
        return cartItemRepository.findByUserAndProduct(user, product).orElse(null);
    }
    
    @Override
    public void updateCartItemSelected(User user, Product product, Boolean selected) {
        Optional<CartItemEntity> existingItem = cartItemRepository.findByUserAndProduct(user, product);
        if (existingItem.isPresent()) {
            CartItemEntity item = existingItem.get();
            item.setSelected(selected);
            cartItemRepository.save(item);
        }
    }
    
    @Override
    public void updateAllCartItemsSelected(User user, Boolean selected) {
        List<CartItemEntity> cartItems = getCartItems(user);
        for (CartItemEntity item : cartItems) {
            item.setSelected(selected);
        }
        cartItemRepository.saveAll(cartItems);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CartItemEntity> getSelectedCartItems(User user) {
        return cartItemRepository.findByUserAndSelected(user, true);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Double getSelectedCartTotalPrice(User user) {
        List<CartItemEntity> selectedItems = getSelectedCartItems(user);
        return selectedItems.stream()
                .mapToDouble(item -> {
                    // Check if product is in flash sale (use flash sale price if available)
                    Optional<FlashSaleProduct> fspOpt = flashSaleService.getActiveFlashSaleProduct(item.getProduct().getProductId());
                    double unitPrice = fspOpt.isPresent() ? 
                        fspOpt.get().getFlashSalePrice().doubleValue() : item.getUnitPrice();
                    
                    // Apply product discount only if not in flash sale
                    if (!fspOpt.isPresent()) {
                        int discount = item.getProduct().getDiscount() != null ? item.getProduct().getDiscount() : 0;
                        unitPrice = unitPrice * (1.0 - (discount / 100.0));
                    }
                    
                    return unitPrice * item.getQuantity();
                })
                .sum();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Integer getSelectedCartItemCount(User user) {
        return (int) cartItemRepository.countByUserAndSelected(user, true);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CartByShopDTO> getCartItemsByShop(User user) {
        List<CartItemEntity> cartItemEntities = getCartItems(user);
        
        // Group cart items by shop
        Map<Shop, List<CartItem>> cartItemsByShop = new HashMap<>();
        
        for (CartItemEntity entity : cartItemEntities) {
            Shop shop = entity.getShop();
            
            // Convert to CartItem DTO
            CartItem cartItem = new CartItem();
            cartItem.setId(entity.getProduct().getProductId());
            cartItem.setName(entity.getProduct().getProductName());
            cartItem.setUnitPrice(entity.getUnitPrice());
            cartItem.setDiscount(entity.getProduct().getDiscount());
            cartItem.setQuantity(entity.getQuantity());
            cartItem.setTotalPrice(entity.getTotalPrice());
            cartItem.setProduct(entity.getProduct());
            cartItem.setSelected(entity.getSelected());
            
            // Set additional fields to avoid lazy loading issues
            cartItem.setBrandName(entity.getProduct().getBrand() != null ? 
                entity.getProduct().getBrand().getBrandName() : "");
            cartItem.setCategoryName(entity.getProduct().getCategory() != null ? 
                entity.getProduct().getCategory().getCategoryName() : "");
            cartItem.setImageUrl(entity.getProduct().getProductImage());
            
            // Recalculate total price using discounted price
            cartItem.updateTotalPrice();
            
            // Group by shop
            cartItemsByShop.computeIfAbsent(shop, k -> new ArrayList<>()).add(cartItem);
        }
        
        // Convert to CartByShopDTO list
        List<CartByShopDTO> result = new ArrayList<>();
        for (Map.Entry<Shop, List<CartItem>> entry : cartItemsByShop.entrySet()) {
            result.add(new CartByShopDTO(entry.getKey(), entry.getValue()));
        }
        
        // Sort by shop name (null shops last)
        result.sort((a, b) -> {
            if (a.getShop() == null && b.getShop() == null) return 0;
            if (a.getShop() == null) return 1;
            if (b.getShop() == null) return -1;
            return a.getShopName().compareTo(b.getShopName());
        });
        
        return result;
    }
    
    @Override
    public void updateShopItemsSelected(User user, Long shopId, Boolean selected) {
        List<CartItemEntity> cartItems;
        
        if (shopId == null) {
            // Handle items without shop
            cartItems = cartItemRepository.findByUser(user).stream()
                    .filter(item -> item.getShop() == null)
                    .collect(Collectors.toList());
        } else {
            // Handle items with specific shop
            cartItems = cartItemRepository.findByUser(user).stream()
                    .filter(item -> item.getShop() != null && shopId.equals(item.getShop().getShopId()))
                    .collect(Collectors.toList());
        }
        
        for (CartItemEntity item : cartItems) {
            item.setSelected(selected);
        }
        cartItemRepository.saveAll(cartItems);
    }
}
