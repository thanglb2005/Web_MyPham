package vn.payment;

import vn.entity.OneXuTransaction;
import vn.entity.Order;
import vn.entity.User;
import vn.repository.OneXuTransactionRepository;
import vn.repository.UserRepository;
import vn.service.CartService;
import vn.service.OrderService;

/**
 * Xử lý checkout cho phương thức thanh toán COD.
 * Logic được tách ra từ CartController để áp dụng Factory Method.
 */
public class CodPaymentProcessor implements PaymentProcessor {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserRepository userRepository;
    private final OneXuTransactionRepository oneXuTransactionRepository;

    public CodPaymentProcessor(OrderService orderService,
                               CartService cartService,
                               UserRepository userRepository,
                               OneXuTransactionRepository oneXuTransactionRepository) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.userRepository = userRepository;
        this.oneXuTransactionRepository = oneXuTransactionRepository;
    }

    @Override
    public String process(CheckoutContext ctx) {
        User user = ctx.getUser();

        Order order = orderService.createOrder(
                user,
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

        // Deduct xu from user balance if xu was used (logic copy từ CartController)
        Integer xuAmount = (Integer) ctx.getRequest().getSession().getAttribute("xuAmount");
        if (xuAmount != null && xuAmount > 0) {
            Double currentBalance = user.getOneXuBalance() != null ? user.getOneXuBalance() : 0.0;
            Double newBalance = currentBalance - xuAmount;
            if (newBalance < 0) newBalance = 0.0;

            user.setOneXuBalance(newBalance);
            userRepository.save(user);

            OneXuTransaction xuTransaction = new OneXuTransaction(
                    user.getUserId(),
                    OneXuTransaction.TransactionType.PURCHASE,
                    -xuAmount.doubleValue(), // Negative because it's a deduction
                    newBalance,
                    "Sử dụng " + xuAmount + " xu cho đơn hàng #" + order.getOrderId(),
                    order.getOrderId()
            );
            oneXuTransactionRepository.save(xuTransaction);

            // Update user in session
            ctx.getRequest().getSession().setAttribute("user", user);
        }

        // Clear cart after successful COD order
        cartService.clearCart(user);

        // Clear voucher and xu session data
        ctx.getRequest().getSession().removeAttribute("oneVoucher");
        ctx.getRequest().getSession().removeAttribute("oneVoucherDiscount");
        ctx.getRequest().getSession().removeAttribute("shopVoucher");
        ctx.getRequest().getSession().removeAttribute("shopVoucherDiscount");
        ctx.getRequest().getSession().removeAttribute("xuAmount");
        ctx.getRequest().getSession().removeAttribute("xuDiscount");

        ctx.getModel().addAttribute("message", "Đặt hàng thành công! Mã đơn hàng: #" + order.getOrderId());
        ctx.getModel().addAttribute("orderId", order.getOrderId());

        return "redirect:/order-success?orderId=" + order.getOrderId();
    }
}
