package vn.command.cart;

import vn.entity.Product;
import vn.entity.User;
import vn.service.CartService;

/** ConcreteCommand: cập nhật số lượng dòng giỏ. */
public final class UpdateCartQuantityCommand implements CartCommand {

    private final User user;
    private final Product product;
    private final Integer quantity;
    private final CartService cartService;

    public UpdateCartQuantityCommand(User user, Product product, Integer quantity, CartService cartService) {
        this.user = user;
        this.product = product;
        this.quantity = quantity;
        this.cartService = cartService;
    }

    @Override
    public CartCommandResult execute() {
        if (product == null) {
            return CartCommandResult.ok();
        }
        try {
            cartService.updateCartItemQuantity(user, product, quantity);
            return CartCommandResult.ok();
        } catch (IllegalArgumentException e) {
            return CartCommandResult.failure(e.getMessage());
        }
    }
}
