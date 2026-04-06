package vn.payment.creator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.entity.Order;
import vn.payment.CodPaymentProcessor;
import vn.payment.PaymentProcessor;
import vn.repository.OneXuTransactionRepository;
import vn.repository.UserRepository;
import vn.service.CartService;
import vn.service.OrderService;

@Component
public class CodPaymentCreator implements PaymentCreator {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserRepository userRepository;
    private final OneXuTransactionRepository oneXuTransactionRepository;

    @Autowired
    public CodPaymentCreator(OrderService orderService, CartService cartService, 
                             UserRepository userRepository, OneXuTransactionRepository oneXuTransactionRepository) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.userRepository = userRepository;
        this.oneXuTransactionRepository = oneXuTransactionRepository;
    }

    @Override
    public boolean supports(Order.PaymentMethod method) {
        return method == Order.PaymentMethod.COD;
    }

    @Override
    public PaymentProcessor createProcessor() {
        return new CodPaymentProcessor(orderService, cartService, userRepository, oneXuTransactionRepository);
    }
}
