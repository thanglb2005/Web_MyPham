package vn.facade.refund.dto;

import vn.entity.Order;
import vn.entity.Refund;

/**
 * DTO ket qua cho man hinh chon phuong thuc hoan tien.
 */
public class RefundRequestViewResult {
    private final boolean success;
    private final String errorMessage;
    private final String infoMessage;
    private final String redirectUrl;
    private final Order order;
    private final Refund refund;
    private final Double currentBalance;
    private final Double balanceAfter;

    public RefundRequestViewResult(boolean success,
                                   String errorMessage,
                                   String infoMessage,
                                   String redirectUrl,
                                   Order order,
                                   Refund refund,
                                   Double currentBalance,
                                   Double balanceAfter) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.infoMessage = infoMessage;
        this.redirectUrl = redirectUrl;
        this.order = order;
        this.refund = refund;
        this.currentBalance = currentBalance;
        this.balanceAfter = balanceAfter;
    }

    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
    public String getInfoMessage() { return infoMessage; }
    public String getRedirectUrl() { return redirectUrl; }
    public Order getOrder() { return order; }
    public Refund getRefund() { return refund; }
    public Double getCurrentBalance() { return currentBalance; }
    public Double getBalanceAfter() { return balanceAfter; }
}

