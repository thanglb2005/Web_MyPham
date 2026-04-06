package vn.payment.creator;

import vn.entity.Order;
import vn.payment.PaymentProcessor;

public interface PaymentCreator {
    boolean supports(Order.PaymentMethod method);
    PaymentProcessor createProcessor();
}
