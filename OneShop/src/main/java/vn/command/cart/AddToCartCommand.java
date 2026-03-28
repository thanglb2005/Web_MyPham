package vn.command.cart;

import vn.entity.Product;
import vn.entity.User;
import vn.service.CartService;

/** ConcreteCommand: thêm sản phẩm vào giỏ (từ trang sản phẩm / listing). */
public final class AddToCartCommand implements CartCommand {

    private final User user;
    private final Product product;
    private final Integer quantity;
    private final CartService cartService;

    public AddToCartCommand(User user, Product product, Integer quantity, CartService cartService) {
        this.user = user;
        this.product = product;
        this.quantity = quantity;
        this.cartService = cartService;
    }

    @Override
    public CartCommandResult execute() {
        if (product == null) {
            return CartCommandResult.failure("Sản phẩm không tồn tại hoặc đã bị gỡ.");
        }
        try {
            cartService.addToCart(user, product, quantity);
            return CartCommandResult.ok();
        } catch (IllegalArgumentException e) {
            return CartCommandResult.failure(e.getMessage());
        } catch (Exception e) {
            return CartCommandResult.failure("Có lỗi xảy ra khi thêm sản phẩm vào giỏ hàng.");
        }
    }
}
