package vn.strategy.discount;

import vn.entity.Promotion;

/**
 * Strategy: Giảm theo % phí ship, có cap bởi maximumDiscountAmount.
 */
public class PercentageShippingDiscountStrategy implements ShippingDiscountStrategy {

    @Override
    public double calculate(Promotion promotion, double shippingFee) {
        double discount = (shippingFee * promotion.getDiscountValue().doubleValue()) / 100.0;
        if (promotion.getMaximumDiscountAmount() != null
                && promotion.getMaximumDiscountAmount().doubleValue() > 0) {
            discount = Math.min(discount, promotion.getMaximumDiscountAmount().doubleValue());
        }
        return discount;
    }
}
