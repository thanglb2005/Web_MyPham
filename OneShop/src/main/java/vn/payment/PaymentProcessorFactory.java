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
        if (method == null) {
            throw new IllegalArgumentException("Phương thức thanh toán không được để trống (null).");
        }

        switch (method) {
            case COD:
                return new CodPaymentProcessor(orderService, cartService, userRepository, oneXuTransactionRepository);
            case MOMO:
                return new MomoPaymentProcessor(orderService);
            case BANK_TRANSFER:
                return new PayOsPaymentProcessor(orderService);
            default:
                throw new IllegalArgumentException("Không hỗ trợ phương thức thanh toán: " + method);
        }
    }
}
