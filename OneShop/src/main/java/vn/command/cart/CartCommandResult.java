package vn.command.cart;

/**
 * Kết quả thực thi {@link CartCommand}: dùng cho flash message (redirect) hoặc JSON (selected totals).
 */
public final class CartCommandResult {

    private final boolean success;
    private final String message;
    private final Double selectedTotal;
    private final Integer selectedCount;

    private CartCommandResult(boolean success, String message, Double selectedTotal, Integer selectedCount) {
        this.success = success;
        this.message = message;
        this.selectedTotal = selectedTotal;
        this.selectedCount = selectedCount;
    }

    public static CartCommandResult ok() {
        return new CartCommandResult(true, null, null, null);
    }

    public static CartCommandResult okWithSelectionTotals(double selectedTotal, int selectedCount) {
        return new CartCommandResult(true, null, selectedTotal, selectedCount);
    }

    public static CartCommandResult failure(String message) {
        return new CartCommandResult(false, message, null, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Double getSelectedTotal() {
        return selectedTotal;
    }

    public Integer getSelectedCount() {
        return selectedCount;
    }
}
