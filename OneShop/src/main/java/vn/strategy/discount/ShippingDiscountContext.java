package vn.strategy.discount;

import vn.entity.Promotion;

/**
 * Context cho Strategy pattern trong bài toán tính giảm giá vận chuyển.
 *
 * <p>Context giữ strategy hiện tại và ủy quyền việc tính toán cho strategy đó.
 * CartController chỉ cần chọn strategy phù hợp rồi gọi Context thực thi.</p>
 */
public class ShippingDiscountContext {

    private ShippingDiscountStrategy strategy;

    public ShippingDiscountContext() {
    }

    public ShippingDiscountContext(ShippingDiscountStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(ShippingDiscountStrategy strategy) {
        this.strategy = strategy;
    }

    public double calculateDiscount(Promotion promotion, double shippingFee) {
        if (strategy == null) {
            throw new IllegalStateException("ShippingDiscountStrategy must be set before calculating discount.");
        }
        return strategy.calculate(promotion, shippingFee);
    }
}
