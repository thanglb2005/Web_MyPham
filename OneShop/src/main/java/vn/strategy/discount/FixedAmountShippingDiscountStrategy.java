package vn.strategy.discount;

import vn.entity.Promotion;

/**
 * Strategy: Giảm số tiền cố định (discountValue).
 */
public class FixedAmountShippingDiscountStrategy implements ShippingDiscountStrategy {

    @Override
    public double calculate(Promotion promotion, double shippingFee) {
        return promotion.getDiscountValue().doubleValue();
    }
}
