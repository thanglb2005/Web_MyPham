package vn.payment;

import vn.entity.Order;
import vn.repository.OneXuTransactionRepository;
import vn.repository.UserRepository;
import vn.service.CartService;
import vn.service.OrderService;

/**
 * Factory tạo ra PaymentProcessor cụ thể dựa trên PaymentMethod.
 * Style giống AnimalFactory trong bài tập CreationalDesignPattern của bạn.
 */
public class PaymentProcessorFactory {

    public static PaymentProcessor createProcessor(Order.PaymentMethod method,
                                                   OrderService orderService,
                                                   CartService cartService,
                                                   UserRepository userRepository,
                                                   OneXuTransactionRepository oneXuTransactionRepository) {
        if (method == Order.PaymentMethod.COD) {
            return new CodPaymentProcessor(orderService, cartService, userRepository, oneXuTransactionRepository);
        } else if (method == Order.PaymentMethod.MOMO) {
            return new MomoPaymentProcessor(orderService);
        } else if (method == Order.PaymentMethod.BANK_TRANSFER) {
            return new PayOsPaymentProcessor(orderService);
        }
        return null;
    }
}
