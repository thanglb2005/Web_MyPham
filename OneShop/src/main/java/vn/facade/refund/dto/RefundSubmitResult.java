package vn.facade.refund.dto;

/**
 * DTO ket qua khi submit form hoan tien.
 */
public class RefundSubmitResult {
    private final boolean success;
    private final String message;
    private final String redirectUrl;

    public RefundSubmitResult(boolean success, String message, String redirectUrl) {
        this.success = success;
        this.message = message;
        this.redirectUrl = redirectUrl;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getRedirectUrl() { return redirectUrl; }
}

