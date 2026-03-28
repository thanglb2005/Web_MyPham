package vn.command.cart;

import vn.entity.Product;
import vn.entity.User;
import vn.service.CartService;

/** ConcreteCommand: xóa một sản phẩm khỏi giỏ. */
public final class RemoveFromCartCommand implements CartCommand {

    private final User user;
    private final Product product;
    private final CartService cartService;

    public RemoveFromCartCommand(User user, Product product, CartService cartService) {
        this.user = user;
        this.product = product;
        this.cartService = cartService;
    }

    @Override
    public CartCommandResult execute() {
        if (product != null) {
            cartService.removeFromCart(user, product);
        }
        return CartCommandResult.ok();
    }
}
