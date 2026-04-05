package vn.strategy.discount;

import vn.entity.Promotion;

/**
 * Strategy: Giảm số tiền cố định (discountValue).
 */
public class FixedAmountShippingDiscountStrategy implements ShippingDiscountStrategy {

    @Override
    public double calculate(Promotion promotion, double shippingFee) {
        if (promotion == null || promotion.getDiscountValue() == null) {
            return 0.0;
        }
        
        // Mức giảm tĩnh nhưng quan trọng nhất: không được giảm quá số tiền ship thực tế!
        double discount = promotion.getDiscountValue().doubleValue();
        return Math.min(discount, shippingFee);
    }
}
