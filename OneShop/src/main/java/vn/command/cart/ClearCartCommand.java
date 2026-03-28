package vn.command.cart;

import vn.entity.User;
import vn.service.CartService;

/** ConcreteCommand: xóa toàn bộ giỏ. */
public final class ClearCartCommand implements CartCommand {

    private final User user;
    private final CartService cartService;

    public ClearCartCommand(User user, CartService cartService) {
        this.user = user;
        this.cartService = cartService;
    }

    @Override
    public CartCommandResult execute() {
        cartService.clearCart(user);
        return CartCommandResult.ok();
    }
}
