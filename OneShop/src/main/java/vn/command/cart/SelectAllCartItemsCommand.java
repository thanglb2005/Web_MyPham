package vn.command.cart;

import vn.entity.User;
import vn.service.CartService;

/** ConcreteCommand: chọn / bỏ chọn toàn bộ giỏ. */
public final class SelectAllCartItemsCommand implements CartCommand {

    private final User user;
    private final Boolean selected;
    private final CartService cartService;

    public SelectAllCartItemsCommand(User user, Boolean selected, CartService cartService) {
        this.user = user;
        this.selected = selected;
        this.cartService = cartService;
    }

    @Override
    public CartCommandResult execute() {
        cartService.updateAllCartItemsSelected(user, selected);
        double total = cartService.getSelectedCartTotalPrice(user);
        int count = cartService.getSelectedCartItemCount(user);
        return CartCommandResult.okWithSelectionTotals(total, count);
    }
}
