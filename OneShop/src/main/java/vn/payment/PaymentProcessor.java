package vn.payment;

/**
 * Định nghĩa interface chung cho tất cả bộ xử lý thanh toán.
 */
public interface PaymentProcessor {

    /**
     * Xử lý thanh toán và trả về URL redirect mà controller sẽ return.
     */
    String process(CheckoutContext ctx);
}
