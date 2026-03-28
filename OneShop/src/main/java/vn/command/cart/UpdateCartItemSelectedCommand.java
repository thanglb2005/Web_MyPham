package vn.command.cart;

import vn.entity.Product;
import vn.entity.User;
import vn.service.CartService;

/** ConcreteCommand: bật/tắt chọn một dòng giỏ (AJAX). */
public final class UpdateCartItemSelectedCommand implements CartCommand {

    private final User user;
    private final Product product;
    private final Boolean selected;
    private final CartService cartService;

    public UpdateCartItemSelectedCommand(User user, Product product, Boolean selected, CartService cartService) {
        this.user = user;
        this.product = product;
        this.selected = selected;
        this.cartService = cartService;
    }

    @Override
    public CartCommandResult execute() {
        if (product == null) {
            return CartCommandResult.failure("Sản phẩm không tồn tại");
        }
        cartService.updateCartItemSelected(user, product, selected);
        double total = cartService.getSelectedCartTotalPrice(user);
        int count = cartService.getSelectedCartItemCount(user);
        return CartCommandResult.okWithSelectionTotals(total, count);
    }
}
