package vn.service;

import vn.dto.CartByShopDTO;
import vn.entity.CartItemEntity;
import vn.entity.Product;
import vn.entity.Shop;
import vn.entity.User;

import java.util.List;

/**
 * Service interface for Cart operations
 * @author OneShop Team
 */
public interface CartService {
    
    /**
     * Add product to cart
     */
    CartItemEntity addToCart(User user, Product product, Integer quantity);
    
    /**
     * Update cart item quantity
     */
    CartItemEntity updateCartItemQuantity(User user, Product product, Integer quantity);
    
    /**
     * Remove product from cart
     */
    void removeFromCart(User user, Product product);
    
    /**
     * Get all cart items for user
     */
    List<CartItemEntity> getCartItems(User user);
    
    /**
     * Get cart item count for user
     */
    Integer getCartItemCount(User user);
    
    /**
     * Get total price for user's cart
     */
    Double getCartTotalPrice(User user);
    
    /**
     * Clear user's cart
     */
    void clearCart(User user);
    
    /**
     * Check if product exists in user's cart
     */
    boolean isProductInCart(User user, Product product);
    
    /**
     * Get cart item by user and product
     */
    CartItemEntity getCartItem(User user, Product product);
    
    /**
     * Update selected status of cart item
     */
    void updateCartItemSelected(User user, Product product, Boolean selected);
    
    /**
     * Update selected status of all cart items for user
     */
    void updateAllCartItemsSelected(User user, Boolean selected);
    
    /**
     * Get selected cart items for user
     */
    List<CartItemEntity> getSelectedCartItems(User user);
    
    /**
     * Get total price for selected cart items
     */
    Double getSelectedCartTotalPrice(User user);
    
    /**
     * Get count of selected cart items
     */
    Integer getSelectedCartItemCount(User user);
    
    /**
     * Get cart items grouped by shop (Shopee-like)
     */
    List<CartByShopDTO> getCartItemsByShop(User user);
    
    /**
     * Update selected status for all items in a shop
     */
    void updateShopItemsSelected(User user, Long shopId, Boolean selected);
}
