package vn.payment;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import vn.entity.CartItem;
import vn.entity.Order;
import vn.entity.User;

import java.util.Map;

/**
 * Gom toàn bộ dữ liệu cần thiết cho xử lý checkout.
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

    public CheckoutContext(User user,
                           String customerName,
                           String customerEmail,
                           String phone,
                           String fullAddress,
                           String note,
                           Order.PaymentMethod paymentMethod,
                           Map<Long, CartItem> cartMap,
                           String promotionDescription,
                           Double totalDiscount,
                           Double shippingFee,
                           String shippingVoucherCode,
                           Double shippingVoucherDiscount,
                           Order.DeliveryType deliveryType,
                           HttpServletRequest request,
                           Model model) {
        this.user = user;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.phone = phone;
        this.fullAddress = fullAddress;
        this.note = note;
        this.paymentMethod = paymentMethod;
        this.cartMap = cartMap;
        this.promotionDescription = promotionDescription;
        this.totalDiscount = totalDiscount;
        this.shippingFee = shippingFee;
        this.shippingVoucherCode = shippingVoucherCode;
        this.shippingVoucherDiscount = shippingVoucherDiscount;
        this.deliveryType = deliveryType;
        this.request = request;
        this.model = model;
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
