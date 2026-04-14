package vn.dto;

import vn.entity.Order;

public class MomoPaymentRequestDTO {
    private final Order order;
    private final String returnUrl;
    private final String notifyUrl;

    public MomoPaymentRequestDTO(Order order, String returnUrl, String notifyUrl) {
        this.order = order;
        this.returnUrl = returnUrl;
        this.notifyUrl = notifyUrl;
    }

    public Order getOrder() {
        return order;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }
}
