package vn.payment;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import vn.entity.CartItem;
import vn.entity.Order;
import vn.entity.User;

import java.util.Map;

/**
 * Gom toàn bộ dữ liệu cần thiết cho xử lý checkout.
 * <p>
 * Builder pattern chuẩn: chỉ tạo được qua CheckoutContext.builder()...build().
 * Constructor private nhận Builder – bên ngoài không thể new CheckoutContext(...).
 */
public class CheckoutContext {

    private final User user;
    private final String customerName;
    private final String customerEmail;
    private final String phone;
    private final String fullAddress;
    private final String note;
    private final Order.PaymentMethod paymentMethod;
    private final Map<Long, CartItem> cartMap;
    private final String promotionDescription;
    private final Double totalDiscount;
    private final Double shippingFee;
    private final String shippingVoucherCode;
    private final Double shippingVoucherDiscount;
    private final Order.DeliveryType deliveryType;
    private final HttpServletRequest request;
    private final Model model;

    /**
     * Constructor private – chỉ Builder gọi được. Copy từ builder sang product (chuẩn Builder pattern).
     */
    private CheckoutContext(Builder builder) {
        this.user = builder.user;
        this.customerName = builder.customerName;
        this.customerEmail = builder.customerEmail;
        this.phone = builder.phone;
        this.fullAddress = builder.fullAddress;
        this.note = builder.note;
        this.paymentMethod = builder.paymentMethod;
        this.cartMap = builder.cartMap;
        this.promotionDescription = builder.promotionDescription;
        this.totalDiscount = builder.totalDiscount;
        this.shippingFee = builder.shippingFee;
        this.shippingVoucherCode = builder.shippingVoucherCode;
        this.shippingVoucherDiscount = builder.shippingVoucherDiscount;
        this.deliveryType = builder.deliveryType;
        this.request = builder.request;
        this.model = builder.model;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private User user;
        private String customerName;
        private String customerEmail;
        private String phone;
        private String fullAddress;
        private String note;
        private Order.PaymentMethod paymentMethod;
        private Map<Long, CartItem> cartMap;
        private String promotionDescription;
        private Double totalDiscount;
        private Double shippingFee;
        private String shippingVoucherCode;
        private Double shippingVoucherDiscount;
        private Order.DeliveryType deliveryType;
        private HttpServletRequest request;
        private Model model;

        private Builder() {}

        public Builder user(User user) { this.user = user; return this; }
        public Builder customerName(String customerName) { this.customerName = customerName; return this; }
        public Builder customerEmail(String customerEmail) { this.customerEmail = customerEmail; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder fullAddress(String fullAddress) { this.fullAddress = fullAddress; return this; }
        public Builder note(String note) { this.note = note; return this; }
        public Builder paymentMethod(Order.PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public Builder cartMap(Map<Long, CartItem> cartMap) { this.cartMap = cartMap; return this; }
        public Builder promotionDescription(String promotionDescription) { this.promotionDescription = promotionDescription; return this; }
        public Builder totalDiscount(Double totalDiscount) { this.totalDiscount = totalDiscount; return this; }
        public Builder shippingFee(Double shippingFee) { this.shippingFee = shippingFee; return this; }
        public Builder shippingVoucherCode(String shippingVoucherCode) { this.shippingVoucherCode = shippingVoucherCode; return this; }
        public Builder shippingVoucherDiscount(Double shippingVoucherDiscount) { this.shippingVoucherDiscount = shippingVoucherDiscount; return this; }
        public Builder deliveryType(Order.DeliveryType deliveryType) { this.deliveryType = deliveryType; return this; }
        public Builder request(HttpServletRequest request) { this.request = request; return this; }
        public Builder model(Model model) { this.model = model; return this; }

        /** Chuẩn Builder: build() tạo product bằng cách gọi constructor private nhận this (Builder). */
        public CheckoutContext build() {
            return new CheckoutContext(this);
        }
    }

    public User getUser() { return user; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getPhone() { return phone; }
    public String getFullAddress() { return fullAddress; }
    public String getNote() { return note; }
    public Order.PaymentMethod getPaymentMethod() { return paymentMethod; }
    public Map<Long, CartItem> getCartMap() { return cartMap; }
    public String getPromotionDescription() { return promotionDescription; }
    public Double getTotalDiscount() { return totalDiscount; }
    public Double getShippingFee() { return shippingFee; }
    public String getShippingVoucherCode() { return shippingVoucherCode; }
    public Double getShippingVoucherDiscount() { return shippingVoucherDiscount; }
    public Order.DeliveryType getDeliveryType() { return deliveryType; }
    public HttpServletRequest getRequest() { return request; }
    public Model getModel() { return model; }
}
