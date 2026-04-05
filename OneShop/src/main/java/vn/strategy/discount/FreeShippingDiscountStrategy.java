package vn.strategy.discount;

import vn.entity.Promotion;

/**
 * Strategy: Miễn phí ship – giảm bằng đúng phí ship.
 */
public class FreeShippingDiscountStrategy implements ShippingDiscountStrategy {

    @Override
    public double calculate(Promotion promotion, double shippingFee) {
        if (promotion == null) {
            return 0.0;
        }
        return shippingFee;
    }
}
