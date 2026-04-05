package vn.strategy.discount;

import vn.entity.Promotion;

/**
 * Strategy: Giảm theo % phí ship, có cap bởi maximumDiscountAmount.
 */
public class PercentageShippingDiscountStrategy implements ShippingDiscountStrategy {

    @Override
    public double calculate(Promotion promotion, double shippingFee) {
        if (promotion == null || promotion.getDiscountValue() == null) {
            return 0.0;
        }

        double discount = (shippingFee * promotion.getDiscountValue().doubleValue()) / 100.0;
        
        // Bước 1: Chặn trần bằng mức giảm giá tối đa (maximumDiscountAmount) do Admin cấu hình
        if (promotion.getMaximumDiscountAmount() != null
                && promotion.getMaximumDiscountAmount().doubleValue() > 0) {
            discount = Math.min(discount, promotion.getMaximumDiscountAmount().doubleValue());
        }
        
        // Bước 2: Chặn trần tuyệt đối - không bao giờ vượt qua chính phí vận chuyển
        return Math.min(discount, shippingFee);
    }
}
