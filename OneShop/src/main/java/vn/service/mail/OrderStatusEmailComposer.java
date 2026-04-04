package vn.service.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.entity.Order;
import vn.entity.OrderDetail;
import vn.entity.User;
import vn.repository.OrderDetailRepository;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Dựng nội dung email HTML cho khách khi đơn hàng đổi trạng thái (dùng chung cho Observer).
 */
@Service
public class OrderStatusEmailComposer {

    private final OrderDetailRepository orderDetailRepository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public OrderStatusEmailComposer(OrderDetailRepository orderDetailRepository) {
        this.orderDetailRepository = orderDetailRepository;
    }

    /**
     * @return empty nếu không có mẫu email cho cặp (old,new)
     */
    public Optional<OrderStatusEmailContent> buildForTransition(Order order,
                                                                Order.OrderStatus oldStatus,
                                                                Order.OrderStatus newStatus) {
        if (order == null || newStatus == null) {
            return Optional.empty();
        }

        if (newStatus == Order.OrderStatus.CONFIRMED
                && (oldStatus == Order.OrderStatus.PENDING || oldStatus == Order.OrderStatus.NEW)) {
            return Optional.of(buildOrderConfirmed(order));
        }

        if (newStatus == Order.OrderStatus.SHIPPING && oldStatus == Order.OrderStatus.CONFIRMED) {
            return Optional.of(buildOrderPickedUp(order));
        }

        if (newStatus == Order.OrderStatus.DELIVERED) {
            return Optional.of(buildOrderDelivered(order));
        }

        if (newStatus == Order.OrderStatus.RETURN_REQUESTED && oldStatus == Order.OrderStatus.DELIVERED) {
            return Optional.of(buildReturnRequested(order));
        }

        if (newStatus == Order.OrderStatus.RETURNED
                && (oldStatus == Order.OrderStatus.RETURN_REQUESTED || oldStatus == Order.OrderStatus.DELIVERED)) {
            return Optional.of(buildOrderReturned(order));
        }

        if (newStatus == Order.OrderStatus.CANCELLED && oldStatus != null && oldStatus != Order.OrderStatus.CANCELLED) {
            return Optional.of(buildOrderCancelled(order, oldStatus));
        }

        return Optional.empty();
    }

    private static String escapeHtml(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private NumberFormat vndFormat() {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
    }

    // ──────────────────────────────────────────────
    // Shared layout: header + footer wrapper
    // ──────────────────────────────────────────────

    private String emailWrapper(String iconEmoji, String headerColor, String headerText, String innerContent) {
        return "" +
                "<div style='max-width:640px;margin:0 auto;font-family:\"Segoe UI\",Arial,Helvetica,sans-serif;color:#1a1a2e;background:#ffffff'>" +

                // ── Header banner ──
                "<div style='background:linear-gradient(135deg," + headerColor + ");padding:28px 32px;border-radius:12px 12px 0 0;text-align:center'>" +
                "<div style='font-size:36px;margin-bottom:8px'>" + iconEmoji + "</div>" +
                "<h1 style='margin:0;font-size:22px;font-weight:700;color:#ffffff;letter-spacing:0.3px'>" + headerText + "</h1>" +
                "</div>" +

                // ── Body ──
                "<div style='padding:28px 32px;border-left:1px solid #e5e7eb;border-right:1px solid #e5e7eb'>" +
                innerContent +
                "</div>" +

                // ── Footer ──
                "<div style='background:#f8fafc;padding:20px 32px;border-radius:0 0 12px 12px;border:1px solid #e5e7eb;border-top:none;text-align:center'>" +
                "<p style='margin:0 0 4px;font-size:13px;color:#64748b'>Bạn nhận được email này vì đã đặt hàng tại OneShop.</p>" +
                "<p style='margin:0;font-size:13px;color:#64748b'>© 2025 OneShop — <a href='" + baseUrl + "' style='color:#6366f1;text-decoration:none'>oneshop.vn</a></p>" +
                "</div>" +

                "</div>";
    }

    // ──────────────────────────────────────────────
    // Shared components
    // ──────────────────────────────────────────────

    private String buildInfoRow(String label, String value) {
        return "<tr>" +
                "<td style='padding:6px 0;color:#64748b;font-size:14px;width:140px'>" + label + "</td>" +
                "<td style='padding:6px 0;font-size:14px;font-weight:600;color:#1e293b'>" + value + "</td>" +
                "</tr>";
    }

    private String buildInfoBox(String borderColor, String bgColor, String... rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='margin:20px 0;padding:16px 20px;background:").append(bgColor)
                .append(";border-left:4px solid ").append(borderColor)
                .append(";border-radius:8px'>");
        sb.append("<table style='width:100%;border-collapse:collapse'>");
        for (String row : rows) {
            sb.append(row);
        }
        sb.append("</table></div>");
        return sb.toString();
    }

    private String buildItemsTableHtml(Order order) {
        NumberFormat vnd = vndFormat();
        StringBuilder itemsHtml = new StringBuilder();
        try {
            List<OrderDetail> details = orderDetailRepository.findByOrderIdWithProductAndShop(order.getOrderId());
            int idx = 0;
            for (OrderDetail d : details) {
                idx++;
                String name = d.getProductName() != null ? d.getProductName() :
                        (d.getProduct() != null ? d.getProduct().getProductName() : "Sản phẩm");
                int qty = d.getQuantity() != null ? d.getQuantity() : 0;
                double unit = d.getUnitPrice() != null ? d.getUnitPrice() : 0.0;
                double line = d.getTotalPrice() != null ? d.getTotalPrice() : unit * qty;
                String bgRow = (idx % 2 == 0) ? "#f8fafc" : "#ffffff";
                itemsHtml.append("<tr style='background:").append(bgRow).append("'>")
                        .append("<td style='padding:10px 14px;border-bottom:1px solid #f1f5f9;font-size:14px;color:#334155'>").append(escapeHtml(name)).append("</td>")
                        .append("<td style='padding:10px 14px;text-align:center;border-bottom:1px solid #f1f5f9;font-size:14px;color:#475569'>").append(qty).append("</td>")
                        .append("<td style='padding:10px 14px;text-align:right;border-bottom:1px solid #f1f5f9;font-size:14px;color:#475569'>").append(vnd.format(unit)).append("</td>")
                        .append("<td style='padding:10px 14px;text-align:right;border-bottom:1px solid #f1f5f9;font-size:14px;font-weight:600;color:#1e293b'>").append(vnd.format(line)).append("</td>")
                        .append("</tr>");
            }
        } catch (Exception ignore) {
        }
        return itemsHtml.toString();
    }

    private String buildProductTable(Order order) {
        NumberFormat vnd = vndFormat();
        String itemsHtml = buildItemsTableHtml(order);

        double totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
        double discountAmount = order.getDiscountAmount() != null ? order.getDiscountAmount() : 0.0;
        double shippingFee = order.getShippingFee() != null ? order.getShippingFee() : 0.0;
        double finalAmount = order.getFinalAmount() != null ? order.getFinalAmount() : totalAmount;

        StringBuilder sb = new StringBuilder();
        sb.append("<table style='width:100%;border-collapse:collapse;margin:20px 0;border-radius:8px;overflow:hidden;border:1px solid #e2e8f0'>");

        // Header
        sb.append("<thead><tr style='background:#f1f5f9'>");
        sb.append("<th style='text-align:left;padding:12px 14px;font-size:13px;font-weight:600;color:#475569;text-transform:uppercase;letter-spacing:0.5px'>Sản phẩm</th>");
        sb.append("<th style='text-align:center;padding:12px 14px;font-size:13px;font-weight:600;color:#475569;text-transform:uppercase;letter-spacing:0.5px'>SL</th>");
        sb.append("<th style='text-align:right;padding:12px 14px;font-size:13px;font-weight:600;color:#475569;text-transform:uppercase;letter-spacing:0.5px'>Đơn giá</th>");
        sb.append("<th style='text-align:right;padding:12px 14px;font-size:13px;font-weight:600;color:#475569;text-transform:uppercase;letter-spacing:0.5px'>Thành tiền</th>");
        sb.append("</tr></thead>");

        // Items
        sb.append("<tbody>").append(itemsHtml).append("</tbody>");

        // Summary rows
        sb.append("<tfoot>");
        sb.append("<tr style='background:#f8fafc'>");
        sb.append("<td colspan='3' style='padding:8px 14px;text-align:right;font-size:14px;color:#64748b'>Tạm tính</td>");
        sb.append("<td style='padding:8px 14px;text-align:right;font-size:14px;color:#334155'>").append(vnd.format(totalAmount)).append("</td></tr>");

        if (discountAmount > 0) {
            sb.append("<tr style='background:#f8fafc'>");
            sb.append("<td colspan='3' style='padding:8px 14px;text-align:right;font-size:14px;color:#16a34a'>Giảm giá</td>");
            sb.append("<td style='padding:8px 14px;text-align:right;font-size:14px;color:#16a34a;font-weight:600'>-").append(vnd.format(discountAmount)).append("</td></tr>");
        }

        if (shippingFee > 0) {
            sb.append("<tr style='background:#f8fafc'>");
            sb.append("<td colspan='3' style='padding:8px 14px;text-align:right;font-size:14px;color:#64748b'>Phí vận chuyển</td>");
            sb.append("<td style='padding:8px 14px;text-align:right;font-size:14px;color:#334155'>").append(vnd.format(shippingFee)).append("</td></tr>");
        }

        // Total
        sb.append("<tr style='background:#eef2ff'>");
        sb.append("<td colspan='3' style='padding:12px 14px;text-align:right;font-size:16px;font-weight:700;color:#4338ca'>Tổng cộng</td>");
        sb.append("<td style='padding:12px 14px;text-align:right;font-size:16px;font-weight:700;color:#4338ca'>").append(vnd.format(finalAmount)).append("</td></tr>");
        sb.append("</tfoot></table>");

        return sb.toString();
    }

    private String buildButton(String url, String bgColor, String text) {
        return "<a href='" + url + "' style='display:inline-block;background:" + bgColor +
                ";color:#ffffff;padding:12px 28px;border-radius:8px;text-decoration:none;font-size:14px;font-weight:600;letter-spacing:0.3px;margin-right:8px'>" +
                text + "</a>";
    }

    // ──────────────────────────────────────────────
    // Email builders
    // ──────────────────────────────────────────────

    private OrderStatusEmailContent buildOrderConfirmed(Order order) {
        String shopName = (order.getShop() != null && order.getShop().getShopName() != null)
                ? order.getShop().getShopName() : "OneShop";
        String subject = "✅ Đơn hàng #" + order.getOrderId() + " đã được xác nhận — " + shopName;

        String payment = order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "COD";
        String eta = order.getEstimatedDeliveryDate() != null ? order.getEstimatedDeliveryDate().toString() : "(sẽ thông báo sau)";
        String customerName = order.getCustomerName() != null ? escapeHtml(order.getCustomerName()) : "bạn";

        String inner = "" +
                "<p style='margin:0 0 16px;font-size:15px;line-height:1.7;color:#334155'>Xin chào <strong>" + customerName + "</strong>,</p>" +
                "<p style='margin:0 0 16px;font-size:15px;line-height:1.7;color:#334155'>Đơn hàng <strong style='color:#4338ca'>#" + order.getOrderId() + "</strong> của bạn tại <strong>" + escapeHtml(shopName) + "</strong> đã được người bán xác nhận và đang được chuẩn bị giao hàng.</p>" +

                buildInfoBox("#6366f1", "#eef2ff",
                        buildInfoRow("📍 Địa chỉ nhận", order.getShippingAddress() != null ? escapeHtml(order.getShippingAddress()) : "(chưa có)"),
                        buildInfoRow("💳 Thanh toán", payment),
                        buildInfoRow("🚚 Dự kiến giao", eta)
                ) +

                buildProductTable(order) +

                "<div style='margin-top:24px;text-align:center'>" +
                buildButton(baseUrl + "/my-orders", "#6366f1", "📦 Theo dõi đơn hàng") +
                "</div>" +

                "<p style='margin:24px 0 0;font-size:14px;color:#64748b;line-height:1.6'>Nếu bạn cần hỗ trợ, hãy phản hồi email này hoặc liên hệ đội ngũ CSKH.</p>";

        String body = emailWrapper("✅", "#6366f1,#818cf8", "Đơn hàng đã được xác nhận", inner);
        return new OrderStatusEmailContent(subject, body);
    }

    private OrderStatusEmailContent buildOrderPickedUp(Order order) {
        String shopName = (order.getShop() != null && order.getShop().getShopName() != null)
                ? order.getShop().getShopName() : "OneShop";
        String subject = "🚚 Đơn #" + order.getOrderId() + " đang được giao — " + shopName;

        String payment = order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "COD";
        String customerName = order.getCustomerName() != null ? escapeHtml(order.getCustomerName()) : "bạn";
        String contactPhone = (order.getShop() != null && order.getShop().getPhoneNumber() != null)
                ? order.getShop().getPhoneNumber() : "(chưa có)";
        User shipperUser = order.getShipper();
        String shipperName = shipperUser != null && shipperUser.getName() != null ? escapeHtml(shipperUser.getName()) : "Shipper OneShop";

        String inner = "" +
                "<p style='margin:0 0 16px;font-size:15px;line-height:1.7;color:#334155'>Xin chào <strong>" + customerName + "</strong>,</p>" +
                "<p style='margin:0 0 16px;font-size:15px;line-height:1.7;color:#334155'>Đơn hàng <strong style='color:#0284c7'>#" + order.getOrderId() + "</strong> của bạn đã được shipper tiếp nhận và đang trên đường giao đến bạn!</p>" +

                buildInfoBox("#0ea5e9", "#f0f9ff",
                        buildInfoRow("🏍️ Shipper", shipperName),
                        buildInfoRow("📞 Liên hệ shop", contactPhone),
                        buildInfoRow("📍 Địa chỉ nhận", order.getShippingAddress() != null ? escapeHtml(order.getShippingAddress()) : "(chưa có)"),
                        buildInfoRow("💳 Thanh toán", payment)
                ) +

                buildProductTable(order) +

                "<div style='margin-top:24px;text-align:center'>" +
                buildButton(baseUrl + "/my-orders", "#0ea5e9", "📦 Theo dõi đơn hàng") +
                "</div>" +

                "<p style='margin:24px 0 0;font-size:14px;color:#64748b;line-height:1.6'>Cảm ơn bạn đã mua sắm tại <strong>" + escapeHtml(shopName) + "</strong>!</p>";

        String body = emailWrapper("🚚", "#0284c7,#38bdf8", "Shipper đang giao hàng", inner);
        return new OrderStatusEmailContent(subject, body);
    }

    private OrderStatusEmailContent buildOrderDelivered(Order order) {
        String shopName = (order.getShop() != null && order.getShop().getShopName() != null)
                ? order.getShop().getShopName() : "OneShop";
        String subject = "🎉 Giao hàng thành công — Đơn #" + order.getOrderId() + " — " + shopName;

        String payment = order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "COD";
        String customerName = order.getCustomerName() != null ? escapeHtml(order.getCustomerName()) : "bạn";
        String tracking = order.getTrackingNumber() != null ? order.getTrackingNumber() : "(chưa có)";
        String deliveredAt = order.getDeliveredDate() != null ? order.getDeliveredDate().toString() : "hôm nay";
        User shipper = order.getShipper();
        String shipperDisplay = shipper != null && shipper.getName() != null && !shipper.getName().isEmpty()
                ? escapeHtml(shipper.getName()) : "Shipper OneShop";

        String inner = "" +
                "<p style='margin:0 0 16px;font-size:15px;line-height:1.7;color:#334155'>Xin chào <strong>" + customerName + "</strong>,</p>" +
                "<p style='margin:0 0 16px;font-size:15px;line-height:1.7;color:#334155'>Đơn hàng <strong style='color:#16a34a'>#" + order.getOrderId() + "</strong> đã được giao thành công vào <strong>" + deliveredAt + "</strong>. Hy vọng bạn hài lòng với sản phẩm!</p>" +

                buildInfoBox("#16a34a", "#f0fdf4",
                        buildInfoRow("🙍 Người giao", shipperDisplay),
                        buildInfoRow("📋 Mã vận đơn", tracking),
                        buildInfoRow("📍 Địa chỉ nhận", order.getShippingAddress() != null ? escapeHtml(order.getShippingAddress()) : "(chưa có)"),
                        buildInfoRow("💳 Thanh toán", payment)
                ) +

                buildProductTable(order) +

                "<div style='margin-top:24px;text-align:center'>" +
                buildButton(baseUrl + "/review?orderId=" + order.getOrderId(), "#f59e0b", "⭐ Đánh giá sản phẩm") +
                " " +
                buildButton(baseUrl + "/my-orders", "#475569", "📦 Xem đơn hàng") +
                "</div>" +

                "<p style='margin:24px 0 0;font-size:14px;color:#64748b;line-height:1.6'>Cảm ơn bạn đã mua sắm tại <strong>" + escapeHtml(shopName) + "</strong>. Hẹn gặp lại!</p>";

        String body = emailWrapper("🎉", "#16a34a,#4ade80", "Giao hàng thành công", inner);
        return new OrderStatusEmailContent(subject, body);
    }

    private OrderStatusEmailContent buildReturnRequested(Order order) {
        String shopName = (order.getShop() != null && order.getShop().getShopName() != null)
                ? order.getShop().getShopName() : "OneShop";
        String subject = "📦 Yêu cầu hoàn trả — Đơn #" + order.getOrderId() + " — " + shopName;
        String reason = escapeHtml(order.getCancellationReason());
        if (reason.isEmpty()) {
            reason = "(không có lý do kèm theo)";
        }
        NumberFormat vnd = vndFormat();
        double finalAmount = order.getFinalAmount() != null ? order.getFinalAmount() :
                (order.getTotalAmount() != null ? order.getTotalAmount() : 0.0);
        String customerName = order.getCustomerName() != null ? escapeHtml(order.getCustomerName()) : "bạn";

        String inner = "" +
                "<p style='margin:0 0 16px;font-size:15px;line-height:1.7;color:#334155'>Xin chào <strong>" + customerName + "</strong>,</p>" +
                "<p style='margin:0 0 16px;font-size:15px;line-height:1.7;color:#334155'>Chúng tôi đã nhận yêu cầu trả hàng / hoàn tiền cho đơn <strong style='color:#d97706'>#" + order.getOrderId() + "</strong> tại <strong>" + escapeHtml(shopName) + "</strong>.</p>" +

                "<div style='margin:20px 0;padding:16px 20px;background:#fffbeb;border-left:4px solid #f59e0b;border-radius:8px'>" +
                "<p style='margin:0 0 8px;font-size:13px;font-weight:600;color:#92400e;text-transform:uppercase;letter-spacing:0.5px'>Lý do bạn cung cấp</p>" +
                "<p style='margin:0;font-size:14px;color:#78350f;white-space:pre-wrap;line-height:1.6'>" + reason + "</p>" +
                "</div>" +

                "<p style='margin:16px 0;font-size:15px;line-height:1.7;color:#334155'>Shop sẽ xem xét và phản hồi trong thời gian sớm nhất. Tổng giá trị đơn (tham khảo): <strong style='color:#4338ca'>" + vnd.format(finalAmount) + "</strong>.</p>" +

                "<div style='margin-top:24px;text-align:center'>" +
                buildButton(baseUrl + "/user/my-orders?status=return_requested", "#d97706", "📋 Xem đơn hàng") +
                "</div>";

        String body = emailWrapper("📦", "#d97706,#fbbf24", "Yêu cầu hoàn trả đã được ghi nhận", inner);
        return new OrderStatusEmailContent(subject, body);
    }

    private OrderStatusEmailContent buildOrderReturned(Order order) {
        String shopName = (order.getShop() != null && order.getShop().getShopName() != null)
                ? order.getShop().getShopName() : "OneShop";
        String subject = "✅ Hoàn trả đã duyệt — Đơn #" + order.getOrderId() + " — " + shopName;
        String customerName = order.getCustomerName() != null ? escapeHtml(order.getCustomerName()) : "bạn";

        String inner = "" +
                "<p style='margin:0 0 16px;font-size:15px;line-height:1.7;color:#334155'>Xin chào <strong>" + customerName + "</strong>,</p>" +
                "<p style='margin:0 0 16px;font-size:15px;line-height:1.7;color:#334155'>Yêu cầu hoàn trả cho đơn <strong style='color:#15803d'>#" + order.getOrderId() + "</strong> tại <strong>" + escapeHtml(shopName) + "</strong> đã được <strong style='color:#16a34a'>duyệt</strong>.</p>" +
                "<p style='margin:0 0 16px;font-size:15px;line-height:1.7;color:#334155'>Tiền hoàn sẽ được chuyển theo phương thức bạn đã chọn (OneXu hoặc chuyển khoản) trong thời gian xử lý của hệ thống và ngân hàng.</p>" +

                buildProductTable(order) +

                "<div style='margin-top:24px;text-align:center'>" +
                buildButton(baseUrl + "/user/my-orders", "#15803d", "📋 Theo dõi đơn hàng") +
                "</div>";

        String body = emailWrapper("💸", "#15803d,#4ade80", "Hoàn trả / hoàn tiền đã được xử lý", inner);
        return new OrderStatusEmailContent(subject, body);
    }

    private OrderStatusEmailContent buildOrderCancelled(Order order, Order.OrderStatus oldStatus) {
        String shopName = (order.getShop() != null && order.getShop().getShopName() != null)
                ? order.getShop().getShopName() : "OneShop";
        String subject = "❌ Đơn hàng #" + order.getOrderId() + " đã bị hủy — " + shopName;
        String reason = escapeHtml(order.getCancellationReason());
        if (reason.isEmpty()) {
            reason = "(không có ghi chú từ người bán)";
        }
        String customerName = order.getCustomerName() != null ? escapeHtml(order.getCustomerName()) : "bạn";

        String inner = "" +
                "<p style='margin:0 0 16px;font-size:15px;line-height:1.7;color:#334155'>Xin chào <strong>" + customerName + "</strong>,</p>" +
                "<p style='margin:0 0 16px;font-size:15px;line-height:1.7;color:#334155'>Đơn hàng <strong style='color:#dc2626'>#" + order.getOrderId() + "</strong> tại <strong>" + escapeHtml(shopName) + "</strong> đã chuyển sang trạng thái <strong style='color:#dc2626'>Đã hủy</strong> (trước đó: <strong>" + oldStatus.name() + "</strong>).</p>" +

                "<div style='margin:20px 0;padding:16px 20px;background:#fef2f2;border-left:4px solid #ef4444;border-radius:8px'>" +
                "<p style='margin:0 0 8px;font-size:13px;font-weight:600;color:#991b1b;text-transform:uppercase;letter-spacing:0.5px'>Ghi chú / Lý do</p>" +
                "<p style='margin:0;font-size:14px;color:#7f1d1d;white-space:pre-wrap;line-height:1.6'>" + reason + "</p>" +
                "</div>" +

                buildProductTable(order) +

                "<div style='margin-top:24px;text-align:center'>" +
                buildButton(baseUrl + "/user/my-orders", "#475569", "📋 Xem đơn hàng") +
                "</div>" +

                "<p style='margin:24px 0 0;font-size:14px;color:#64748b;line-height:1.6'>Nếu bạn có thắc mắc, vui lòng liên hệ đội ngũ CSKH.</p>";

        String body = emailWrapper("❌", "#dc2626,#f87171", "Đơn hàng đã bị hủy", inner);
        return new OrderStatusEmailContent(subject, body);
    }

    public record OrderStatusEmailContent(String subject, String htmlBody) {}
}
