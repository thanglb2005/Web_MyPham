package vn.command.cart;

import org.springframework.stereotype.Component;

/**
 * ① Invoker (Command Design Pattern — GoF).
 *
 * <p>Giữ tham chiếu đến {@link CartCommand} hiện tại qua field {@code command}.
 * Client gọi {@link #setCommand(CartCommand)} để gán, sau đó {@link #executeCommand()} để thực thi.</p>
 */
@Component
public class CartCommandInvoker {

    /** ① Thuộc tính command — tham chiếu đến Command hiện tại (GoF). */
    private CartCommand command;

    /** ① setCommand(command) — Client gán ConcreteCommand vào Invoker (GoF). */
    public void setCommand(CartCommand command) {
        this.command = command;
    }

    /** ① executeCommand() — Invoker gọi command.execute() (GoF). */
    public CartCommandResult executeCommand() {
        if (this.command == null) {
            throw new IllegalStateException("Chưa có command nào được gán (gọi setCommand trước).");
        }
        return this.command.execute();
    }
}
