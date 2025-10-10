package vn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.entity.CartItemEntity;
import vn.entity.Product;
import vn.entity.User;
import vn.repository.CartItemRepository;
import vn.service.CartService;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Cart operations
 * @author OneShop Team
 */
@Service
@Transactional
public class CartServiceImpl implements CartService {
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Override
    public CartItemEntity addToCart(User user, Product product, Integer quantity) {
        Optional<CartItemEntity> existingItem = cartItemRepository.findByUserAndProduct(user, product);
        
        if (existingItem.isPresent()) {
            CartItemEntity item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            return cartItemRepository.save(item);
        } else {
            CartItemEntity newItem = new CartItemEntity();
            newItem.setUser(user);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setUnitPrice(product.getPrice());
            newItem.calculateTotalPrice();
            
            return cartItemRepository.save(newItem);
        }
    }
    
    @Override
    public CartItemEntity updateCartItemQuantity(User user, Product product, Integer quantity) {
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
                .mapToDouble(CartItemEntity::getTotalPrice)
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
}
