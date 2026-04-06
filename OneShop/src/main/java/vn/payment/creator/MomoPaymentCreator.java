package vn.payment.creator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.entity.Order;
import vn.payment.MomoPaymentProcessor;
import vn.payment.PaymentProcessor;
import vn.service.OrderService;

@Component
public class MomoPaymentCreator implements PaymentCreator {

    private final OrderService orderService;

    @Autowired
    public MomoPaymentCreator(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public boolean supports(Order.PaymentMethod method) {
        return method == Order.PaymentMethod.MOMO;
    }

    @Override
    public PaymentProcessor createProcessor() {
        return new MomoPaymentProcessor(orderService);
    }
}
