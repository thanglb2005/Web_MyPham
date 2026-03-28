package vn.command.cart;

import vn.entity.User;
import vn.service.CartService;

/** ConcreteCommand: chọn / bỏ chọn theo shop trong giỏ. */
public final class SelectShopCartItemsCommand implements CartCommand {

    private final User user;
    private final Long shopId;
    private final Boolean selected;
    private final CartService cartService;

    public SelectShopCartItemsCommand(User user, Long shopId, Boolean selected, CartService cartService) {
        this.user = user;
        this.shopId = shopId;
        this.selected = selected;
        this.cartService = cartService;
    }

    @Override
    public CartCommandResult execute() {
        cartService.updateShopItemsSelected(user, shopId, selected);
        double total = cartService.getSelectedCartTotalPrice(user);
        int count = cartService.getSelectedCartItemCount(user);
        return CartCommandResult.okWithSelectionTotals(total, count);
    }
}
