package vn.payment.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import vn.entity.Order;
import vn.service.MoMoPaymentService;

@Component
public class MoMoGatewayAdapter implements PaymentGatewayAdapter {

    private final MoMoPaymentService moMoPaymentService;

    public MoMoGatewayAdapter(MoMoPaymentService moMoPaymentService) {
        this.moMoPaymentService = moMoPaymentService;
    }

    @Override
    public String createPaymentUrl(Order order, String returnUrl, String notifyUrl) {
        return moMoPaymentService.createPaymentRequest(order, returnUrl, notifyUrl);
    }

    @Override
    public PaymentCallbackResult processCallback(HttpServletRequest request) {
        String orderId = request.getParameter("orderId");
        String resultCode = request.getParameter("resultCode");
        String transId = request.getParameter("transId");
        String amountStr = request.getParameter("amount");

        if (orderId == null) {
            return new PaymentCallbackResult(false, false, null, null, null, "Missing params");
        }

        Long orderIdLong = parseMoMoOrderId(orderId);
        if (orderIdLong == null) {
            return new PaymentCallbackResult(false, false, null, null, null, "Invalid orderId format");
        }

        Double amount = amountStr != null ? Double.parseDouble(amountStr) : 0D;

        boolean success = false;
        if (resultCode != null && transId != null && amountStr != null) {
            success = moMoPaymentService.processPaymentCallback(orderIdLong, resultCode, transId, amount);
        }

        return new PaymentCallbackResult(success, !"0".equals(resultCode), orderIdLong, transId, amount, "MoMo result code: " + resultCode);
    }

    @Override
    public PaymentWebhookResult processWebhook(HttpServletRequest request, String payload) {
        String orderId = request.getParameter("orderId");
        String resultCode = request.getParameter("resultCode");
        String transId = request.getParameter("transId");
        String amountStr = request.getParameter("amount");

        if (orderId == null || resultCode == null || transId == null || amountStr == null) {
            return new PaymentWebhookResult(false, false, "ERROR", null, "Missing parameters");
        }

        Long orderIdLong = parseMoMoOrderId(orderId);
        if (orderIdLong == null) {
            return new PaymentWebhookResult(false, false, "ERROR", null, "Invalid orderId format");
        }

        Double amount = Double.parseDouble(amountStr);
        boolean success = moMoPaymentService.processPaymentCallback(orderIdLong, resultCode, transId, amount);

        return new PaymentWebhookResult(success, true, success ? "SUCCESS" : "FAILED", orderIdLong, "Webhook processed");
    }

    private Long parseMoMoOrderId(String orderId) {
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
}
