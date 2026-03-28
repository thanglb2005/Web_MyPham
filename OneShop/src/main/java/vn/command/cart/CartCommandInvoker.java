package vn.command.cart;

import org.springframework.stereotype.Component;

/**
 * Invoker: điểm thống nhất gọi {@link CartCommand#execute()} (có thể mở rộng log, queue, undo sau này).
 */
@Component
public class CartCommandInvoker {

    public CartCommandResult invoke(CartCommand command) {
        return command.execute();
    }
}
