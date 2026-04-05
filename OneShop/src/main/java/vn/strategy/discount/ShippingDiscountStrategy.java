package vn.strategy.discount;

import vn.entity.Promotion;

/**
 * Strategy: cách tính giảm giá voucher vận chuyển.
 * Mỗi loại khuyến mãi (FREE_SHIPPING, PERCENTAGE, FIXED_AMOUNT) có implementation riêng.
 */
public interface ShippingDiscountStrategy {

    /**
     * Tính số tiền giảm (VNĐ) cho phí ship.
     *
     * @param promotion   Voucher áp dụng
     * @param shippingFee Phí ship hiện tại (VNĐ)
     * @return Số tiền được giảm (đã được tự động giới hạn không vượt quá phí ship hiện tại)
     */
    double calculate(Promotion promotion, double shippingFee);
}
