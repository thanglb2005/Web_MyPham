package vn.payment.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import vn.dto.MomoPaymentRequestDTO;
import vn.entity.Order;
import vn.service.MoMoPaymentService;

@Component
public class MoMoGatewayAdapter implements PaymentGatewayPort {

    private final MoMoPaymentService adaptee;

    public MoMoGatewayAdapter(MoMoPaymentService moMoPaymentService) {
        this.adaptee = moMoPaymentService;
    }

    @Override
    public String createPaymentUrl(Order order, String returnUrl, String notifyUrl) {
        MomoPaymentRequestDTO requestDto = convertToServiceFormat(order, returnUrl, notifyUrl);
        return adaptee.createPaymentRequest(requestDto);
    }

    @Override
    public PaymentCallbackResult processCallback(HttpServletRequest request) {
        CallbackData callbackData = convertCallbackToServiceFormat(request);

        if (callbackData.orderId() == null) {
            return new PaymentCallbackResult(false, false, null, null, null, "Missing params");
        }

        Long orderIdLong = parseOrderId(callbackData.orderId());
        if (orderIdLong == null) {
            return new PaymentCallbackResult(false, false, null, null, null, "Invalid orderId format");
        }

        Double amount = callbackData.amount() != null ? Double.parseDouble(callbackData.amount()) : 0D;

        boolean success = false;
        if (callbackData.resultCode() != null && callbackData.transId() != null && callbackData.amount() != null) {
            success = processByAdaptee(orderIdLong, callbackData.resultCode(), callbackData.transId(), amount);
        }

        return new PaymentCallbackResult(
                success,
                !"0".equals(callbackData.resultCode()),
                orderIdLong,
                callbackData.transId(),
                amount,
                "MoMo result code: " + callbackData.resultCode()
        );
    }

    @Override
    public PaymentWebhookResult processWebhook(HttpServletRequest request, String payload) {
        CallbackData webhookData = convertWebhookToServiceFormat(request, payload);

        if (webhookData.orderId() == null || webhookData.resultCode() == null || webhookData.transId() == null || webhookData.amount() == null) {
            return new PaymentWebhookResult(false, false, "ERROR", null, "Missing parameters");
        }

        Long orderIdLong = parseOrderId(webhookData.orderId());
        if (orderIdLong == null) {
            return new PaymentWebhookResult(false, false, "ERROR", null, "Invalid orderId format");
        }

        Double amount = Double.parseDouble(webhookData.amount());
        boolean success = processByAdaptee(orderIdLong, webhookData.resultCode(), webhookData.transId(), amount);

        return new PaymentWebhookResult(success, true, success ? "SUCCESS" : "FAILED", orderIdLong, "Webhook processed");
    }

    private Long parseOrderId(String orderId) {
        try {
            if (orderId.startsWith("MOMO_")) {
                String[] parts = orderId.split("_");
                return Long.parseLong(parts.length >= 2 ? parts[1] : orderId);
            }
            return Long.parseLong(orderId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private MomoPaymentRequestDTO convertToServiceFormat(Order order, String returnUrl, String notifyUrl) {
        return new MomoPaymentRequestDTO(order, returnUrl, notifyUrl);
    }

    private CallbackData convertCallbackToServiceFormat(HttpServletRequest request) {
        return new CallbackData(
                request.getParameter("orderId"),
                request.getParameter("resultCode"),
                request.getParameter("transId"),
                request.getParameter("amount")
        );
    }

    private CallbackData convertWebhookToServiceFormat(HttpServletRequest request, String payload) {
        return convertCallbackToServiceFormat(request);
    }

    private boolean processByAdaptee(Long orderId, String resultCode, String transId, Double amount) {
        return adaptee.processPaymentCallback(orderId, resultCode, transId, amount);
    }

    private record CallbackData(String orderId, String resultCode, String transId, String amount) {
    }
}
