package vn.dto;

public class PayOSPaymentRequestDTO {
    private final long orderCode;
    private final int amount;
    private final String description;
    private final String returnUrl;
    private final String cancelUrl;

    public PayOSPaymentRequestDTO(long orderCode, int amount, String description, String returnUrl, String cancelUrl) {
        this.orderCode = orderCode;
        this.amount = amount;
        this.description = description;
        this.returnUrl = returnUrl;
        this.cancelUrl = cancelUrl;
    }

    public long getOrderCode() {
        return orderCode;
    }

    public int getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }
}
