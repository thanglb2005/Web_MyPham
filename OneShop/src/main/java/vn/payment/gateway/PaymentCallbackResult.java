package vn.payment.gateway;

public class PaymentCallbackResult {
    private boolean isSuccess;
    private boolean isCancel;
    private Long orderId;
    private String transactionId;
    private Double amount;
    private String message;

    public PaymentCallbackResult(boolean isSuccess, boolean isCancel, Long orderId, String transactionId, Double amount, String message) {
        this.isSuccess = isSuccess;
        this.isCancel = isCancel;
        this.orderId = orderId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.message = message;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public boolean isCancel() {
        return isCancel;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Double getAmount() {
        return amount;
    }

    public String getMessage() {
        return message;
    }
}
