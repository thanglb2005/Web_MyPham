package vn.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem implements Serializable {

    private Long id;
    private String name;
    private Double unitPrice;
    private Integer quantity;
    private Double totalPrice;
    private Product product;

    // Store these directly to avoid LazyInitializationException
    private String brandName;
    private String categoryName;
    private String imageUrl;
    private Boolean selected = true;

    public CartItem(Product product, Integer quantity) {
        this.id = product.getProductId();
        this.name = product.getProductName();
        this.unitPrice = product.getPrice();
        this.quantity = quantity;
        this.totalPrice = product.getPrice() * quantity;
        this.product = product;

        // Populate these fields directly to avoid lazy loading issues
        this.brandName = product.getBrand() != null ? product.getBrand().getBrandName() : "";
        this.categoryName = product.getCategory() != null ? product.getCategory().getCategoryName() : "";
        this.imageUrl = product.getProductImage();
    }

    public void updateTotalPrice() {
        this.totalPrice = this.unitPrice * this.quantity;
    }
}