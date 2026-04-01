package vn.payment;

import vn.entity.Order;
import vn.service.OrderService;

/**
 * Xử lý checkout cho phương thức thanh toán MoMo.
 */
public class MomoPaymentProcessor implements PaymentProcessor {

    private final OrderService orderService;

    public MomoPaymentProcessor(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public String process(CheckoutContext ctx) {
        Order momoOrder = orderService.createOrder(
                ctx.getUser(),
                ctx.getCustomerName(),
                ctx.getCustomerEmail(),
                ctx.getPhone(),
                ctx.getFullAddress(),
                ctx.getNote(),
                ctx.getPaymentMethod(),
                ctx.getCartMap(),
                ctx.getPromotionDescription().isEmpty() ? null : ctx.getPromotionDescription(),
                ctx.getTotalDiscount(),
                ctx.getShippingFee(),
                ctx.getShippingVoucherCode(),
                ctx.getShippingVoucherDiscount(),
                ctx.getDeliveryType(),
                ctx.getShippingInfoId()
        );

        return "redirect:/payment/momo/create?orderId=" + momoOrder.getOrderId();
    }
}
