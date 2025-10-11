package vn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.entity.CartItem;
import vn.entity.Shop;

import java.util.List;

/**
 * DTO for representing cart items grouped by shop (like Shopee)
 * @author OneShop Team
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartByShopDTO {
    
    private Shop shop;
    private String shopName;
    private String shopLogo;
    private Boolean shopSelected = true; // Whether all items in this shop are selected
    private List<CartItem> cartItems;
    private Integer totalItems;
    private Double totalPrice;
    private Integer selectedItems;
    private Double selectedPrice;
    
    public CartByShopDTO(Shop shop, List<CartItem> cartItems) {
        this.shop = shop;
        this.shopName = shop != null ? shop.getShopName() : "Shop không xác định";
        this.shopLogo = shop != null ? shop.getShopLogo() : null;
        this.cartItems = cartItems;
        this.totalItems = cartItems.size();
        
        // Calculate totals
        this.totalPrice = cartItems.stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
        
        // Calculate selected totals
        this.selectedItems = (int) cartItems.stream()
                .filter(item -> item.getSelected() != null && item.getSelected())
                .count();
        
        this.selectedPrice = cartItems.stream()
                .filter(item -> item.getSelected() != null && item.getSelected())
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
        
        // Check if shop is selected (all items selected)
        this.shopSelected = selectedItems.equals(totalItems) && totalItems > 0;
    }
    
    /**
     * Check if this shop has any selected items
     */
    public boolean hasSelectedItems() {
        return selectedItems > 0;
    }
    
    /**
     * Check if all items in this shop are selected
     */
    public boolean isAllItemsSelected() {
        return shopSelected;
    }
    
    /**
     * Check if some (but not all) items are selected
     */
    public boolean isPartiallySelected() {
        return selectedItems > 0 && !shopSelected;
    }
}
