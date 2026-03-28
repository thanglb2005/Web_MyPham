package vn.command.cart;

/**
 * Command pattern: đóng gói một thao tác giỏ hàng thành object có {@link #execute()}.
 * Receiver thực tế là {@link vn.service.CartService} (gọi bên trong từng lệnh cụ thể).
 */
@FunctionalInterface
public interface CartCommand {

    CartCommandResult execute();
}
