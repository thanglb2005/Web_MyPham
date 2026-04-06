package vn.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.entity.Order;
import vn.payment.creator.PaymentCreator;

import java.util.List;

@Component
public class PaymentProcessorFactory {

    private final List<PaymentCreator> creators;

    @Autowired
    public PaymentProcessorFactory(List<PaymentCreator> creators) {
        this.creators = creators;
    }

    public PaymentProcessor getProcessor(Order.PaymentMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("Phương thức thanh toán không được để trống (null).");
        }

        for (PaymentCreator creator : creators) {
            if (creator.supports(method)) {
                return creator.createProcessor();
            }
        }

        throw new IllegalArgumentException("Không hỗ trợ phương thức thanh toán: " + method);
    }
}
