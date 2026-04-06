package vn.payment.creator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.entity.Order;
import vn.payment.PayOsPaymentProcessor;
import vn.payment.PaymentProcessor;
import vn.service.OrderService;

@Component
public class PayOsPaymentCreator implements PaymentCreator {

    private final OrderService orderService;

    @Autowired
    public PayOsPaymentCreator(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public boolean supports(Order.PaymentMethod method) {
        return method == Order.PaymentMethod.BANK_TRANSFER;
    }

    @Override
    public PaymentProcessor createProcessor() {
        return new PayOsPaymentProcessor(orderService);
    }
}
