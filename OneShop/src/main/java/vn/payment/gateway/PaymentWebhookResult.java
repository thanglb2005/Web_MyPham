package vn.payment.gateway;

public class PaymentWebhookResult {
    private boolean isSuccess;
    private boolean isValidSignature;
    private String status; // Ví dụ: PAID, CANCELLED
    private Long orderId;
    private String message;

    public PaymentWebhookResult(boolean isSuccess, boolean isValidSignature, String status, Long orderId, String message) {
        this.isSuccess = isSuccess;
        this.isValidSignature = isValidSignature;
        this.status = status;
        this.orderId = orderId;
        this.message = message;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public boolean isValidSignature() {
        return isValidSignature;
    }

    public String getStatus() {
        return status;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getMessage() {
        return message;
    }
}
