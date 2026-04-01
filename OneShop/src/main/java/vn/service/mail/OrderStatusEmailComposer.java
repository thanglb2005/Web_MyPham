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

    private String buildItemsTableHtml(Order order) {
        NumberFormat vnd = vndFormat();
        StringBuilder itemsHtml = new StringBuilder();
        try {
            List<OrderDetail> details = orderDetailRepository.findByOrderIdWithProductAndShop(order.getOrderId());
            for (OrderDetail d : details) {
                String name = d.getProductName() != null ? d.getProductName() :
                        (d.getProduct() != null ? d.getProduct().getProductName() : "Sản phẩm");
                int qty = d.getQuantity() != null ? d.getQuantity() : 0;
                double unit = d.getUnitPrice() != null ? d.getUnitPrice() : 0.0;
                double line = d.getTotalPrice() != null ? d.getTotalPrice() : unit * qty;
                itemsHtml.append("<tr>")
                        .append("<td style='padding:8px 12px;border-bottom:1px solid #eee'>").append(name).append("</td>")
                        .append("<td style='padding:8px 12px;text-align:center;border-bottom:1px solid #eee'>").append(qty).append("</td>")
                        .append("<td style='padding:8px 12px;text-align:right;border-bottom:1px solid #eee'>").append(vnd.format(unit)).append("</td>")
                        .append("<td style='padding:8px 12px;text-align:right;border-bottom:1px solid #eee'>").append(vnd.format(line)).append("</td>")
                        .append("</tr>");
            }
        } catch (Exception ignore) {
        }
        return itemsHtml.toString();
    }

    private OrderStatusEmailContent buildOrderConfirmed(Order order) {
        String shopName = (order.getShop() != null && order.getShop().getShopName() != null)
                ? order.getShop().getShopName() : "OneShop";
        String subject = "Xác nhận đơn hàng #" + order.getOrderId() + " - " + shopName;

        NumberFormat vnd = vndFormat();
        double total = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
        String payment = order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "COD";
        String itemsHtml = buildItemsTableHtml(order);

        String eta = order.getEstimatedDeliveryDate() != null ? order.getEstimatedDeliveryDate().toString() : "(sẽ thông báo sau)";
        String body = "" +
                "<div style='font-family:Arial,Helvetica,sans-serif;line-height:1.6;color:#111'>" +
                "<h2 style='color:#0a7cff;margin:0 0 12px'>Đơn hàng đã được xác nhận ✅</h2>" +
                "<p>Chào " + (order.getCustomerName() != null ? order.getCustomerName() : "bạn") + ",</p>" +
                "<p>Đơn hàng <strong>#" + order.getOrderId() + "</strong> của bạn tại <strong>" + shopName + "</strong> đã được người bán xác nhận và đang được chuẩn bị giao.</p>" +
                "<div style='margin:16px 0;padding:12px;background:#f6f9ff;border:1px solid #e3efff;border-radius:8px'>" +
                "<p style='margin:0'><strong>Địa chỉ nhận:</strong> " + (order.getShippingAddress() != null ? order.getShippingAddress() : "(chưa có)") + "</p>" +
                "<p style='margin:4px 0 0'><strong>Thanh toán:</strong> " + payment + "</p>" +
                "<p style='margin:4px 0 0'><strong>Dự kiến giao:</strong> " + eta + "</p>" +
                "</div>" +
                "<table style='width:100%;border-collapse:collapse;margin-top:8px'>" +
                "<thead><tr>" +
                "<th style='text-align:left;padding:8px 12px;border-bottom:2px solid #ddd'>Sản phẩm</th>" +
                "<th style='text-align:center;padding:8px 12px;border-bottom:2px solid #ddd'>SL</th>" +
                "<th style='text-align:right;padding:8px 12px;border-bottom:2px solid #ddd'>Đơn giá</th>" +
                "<th style='text-align:right;padding:8px 12px;border-bottom:2px solid #ddd'>Thành tiền</th>" +
                "</tr></thead><tbody>" + itemsHtml + "</tbody></table>" +
                "<p style='text-align:right;margin:12px 0;font-size:16px'><strong>Tổng cộng: " + vnd.format(total) + "</strong></p>" +
                "<div style='margin-top:16px'>" +
                "<a href='" + baseUrl + "/my-orders' style='display:inline-block;background:#0a7cff;color:#fff;padding:10px 16px;border-radius:6px;text-decoration:none'>Theo dõi đơn hàng</a>" +
                "</div>" +
                "<p>Nếu bạn cần hỗ trợ, hãy phản hồi email này hoặc liên hệ CSKH.</p>" +
                "<p style='margin-top:16px'>Trân trọng,<br/>Đội ngũ OneShop</p>" +
                "</div>";

        return new OrderStatusEmailContent(subject, body);
    }

    private OrderStatusEmailContent buildOrderPickedUp(Order order) {
        String shopName = (order.getShop() != null && order.getShop().getShopName() != null)
                ? order.getShop().getShopName() : "OneShop";
        String subject = "Shipper đã nhận đơn - #" + order.getOrderId() + " - " + shopName;

        NumberFormat vnd = vndFormat();
        double total = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
        String payment = order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "COD";
        String itemsHtml = buildItemsTableHtml(order);

        String contactPhone = (order.getShop() != null && order.getShop().getPhoneNumber() != null)
                ? order.getShop().getPhoneNumber() : "(chưa có)";
        User shipperUser = order.getShipper();
        String shipperName = shipperUser != null && shipperUser.getName() != null ? shipperUser.getName() : "Shipper OneShop";

        String body = "" +
                "<div style='font-family:Arial,Helvetica,sans-serif;line-height:1.6;color:#111'>" +
                "<h2 style='color:#0ea5e9;margin:0 0 12px'>Shipper đã nhận đơn 🚚</h2>" +
                "<p>Chào " + (order.getCustomerName() != null ? order.getCustomerName() : "bạn") + ",</p>" +
                "<p>Đơn hàng <strong>#" + order.getOrderId() + "</strong> của bạn đã được shipper tiếp nhận và sẽ sớm giao đến bạn.</p>" +
                "<div style='margin:16px 0;padding:12px;background:#eff6ff;border:1px solid #bfdbfe;border-radius:8px'>" +
                "<p style='margin:0'><strong>Shipper phụ trách:</strong> " + shipperName + "</p>" +
                "<p style='margin:4px 0 0'><strong>Liên hệ shop:</strong> " + contactPhone + "</p>" +
                "<p style='margin:4px 0 0'><strong>Địa chỉ nhận:</strong> " + (order.getShippingAddress() != null ? order.getShippingAddress() : "(chưa có)") + "</p>" +
                "<p style='margin:4px 0 0'><strong>Thanh toán:</strong> " + payment + "</p>" +
                "</div>" +
                "<table style='width:100%;border-collapse:collapse;margin-top:8px'>" +
                "<thead><tr>" +
                "<th style='text-align:left;padding:8px 12px;border-bottom:2px solid #ddd'>Sản phẩm</th>" +
                "<th style='text-align:center;padding:8px 12px;border-bottom:2px solid #ddd'>SL</th>" +
                "<th style='text-align:right;padding:8px 12px;border-bottom:2px solid #ddd'>Đơn giá</th>" +
                "<th style='text-align:right;padding:8px 12px;border-bottom:2px solid #ddd'>Thành tiền</th>" +
                "</tr></thead><tbody>" + itemsHtml + "</tbody></table>" +
                "<p style='text-align:right;margin:12px 0;font-size:16px'><strong>Tổng cộng: " + vnd.format(total) + "</strong></p>" +
                "<div style='margin-top:16px'>" +
                "<a href='" + baseUrl + "/my-orders' style='display:inline-block;background:#0a7cff;color:#fff;padding:10px 16px;border-radius:6px;text-decoration:none'>Theo dõi đơn hàng</a>" +
                "</div>" +
                "<p style='margin-top:16px'>Cảm ơn bạn đã mua sắm tại <strong>" + shopName + "</strong>.</p>" +
                "<p style='margin-top:16px'>Trân trọng,<br/>Đội ngũ OneShop</p>" +
                "</div>";

        return new OrderStatusEmailContent(subject, body);
    }

    private OrderStatusEmailContent buildOrderDelivered(Order order) {
        String shopName = (order.getShop() != null && order.getShop().getShopName() != null)
                ? order.getShop().getShopName() : "OneShop";
        String subject = "Giao hàng thành công - Đơn #" + order.getOrderId() + " - " + shopName;

        NumberFormat vnd = vndFormat();
        double total = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
        String payment = order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "COD";
        String itemsHtml = buildItemsTableHtml(order);

            String tracking = order.getTrackingNumber() != null ? order.getTrackingNumber() : "(chưa có)";
            String deliveredAt = order.getDeliveredDate() != null ? order.getDeliveredDate().toString() : "hôm nay";
            User shipper = order.getShipper();
            String shipperDisplay = shipper != null && shipper.getName() != null && !shipper.getName().isEmpty()
                    ? shipper.getName() : "Shipper OneShop";

            String body = "" +
                    "<div style='font-family:Arial,Helvetica,sans-serif;line-height:1.6;color:#111'>" +
                    "<h2 style='color:#16a34a;margin:0 0 12px'>Giao hàng thành công ✅</h2>" +
                    "<p>Chào " + (order.getCustomerName() != null ? order.getCustomerName() : "bạn") + ",</p>" +
                    "<p>Đơn hàng <strong>#" + order.getOrderId() + "</strong> của bạn đã được giao thành công vào <strong>" + deliveredAt + "</strong>.</p>" +
                    "<div style='margin:16px 0;padding:12px;background:#ecfdf5;border:1px solid #86efac;border-radius:8px'>" +
                    "<p style='margin:0'><strong>Người giao:</strong> " + shipperDisplay + "</p>" +
                "<p style='margin:4px 0 0'><strong>Mã vận đơn:</strong> " + tracking + "</p>" +
                "<p style='margin:4px 0 0'><strong>Địa chỉ nhận:</strong> " + (order.getShippingAddress() != null ? order.getShippingAddress() : "(chưa có)") + "</p>" +
                "<p style='margin:4px 0 0'><strong>Thanh toán:</strong> " + payment + "</p>" +
                "</div>" +
                "<table style='width:100%;border-collapse:collapse;margin-top:8px'>" +
                "<thead><tr>" +
                "<th style='text-align:left;padding:8px 12px;border-bottom:2px solid #ddd'>Sản phẩm</th>" +
                "<th style='text-align:center;padding:8px 12px;border-bottom:2px solid #ddd'>SL</th>" +
                "<th style='text-align:right;padding:8px 12px;border-bottom:2px solid #ddd'>Đơn giá</th>" +
                "<th style='text-align:right;padding:8px 12px;border-bottom:2px solid #ddd'>Thành tiền</th>" +
                "</tr></thead><tbody>" + itemsHtml + "</tbody></table>" +
                "<p style='text-align:right;margin:12px 0;font-size:16px'><strong>Tổng cộng: " + vnd.format(total) + "</strong></p>" +
                "<div style='margin-top:16px'>" +
                "<a href='" + baseUrl + "/review?orderId=" + order.getOrderId() + "' style='display:inline-block;background:#0ea5e9;color:#fff;padding:10px 16px;border-radius:6px;text-decoration:none'>Đánh giá sản phẩm</a>" +
                " <a href='" + baseUrl + "/my-orders' style='display:inline-block;margin-left:8px;background:#374151;color:#fff;padding:10px 16px;border-radius:6px;text-decoration:none'>Xem đơn hàng</a>" +
                "</div>" +
                "<p style='margin-top:16px'>Cảm ơn bạn đã mua sắm tại <strong>" + shopName + "</strong>. Hẹn gặp lại bạn trong những lần sau!</p>" +
                "<p style='margin-top:16px'>Trân trọng,<br/>Đội ngũ OneShop</p>" +
                "</div>";

        return new OrderStatusEmailContent(subject, body);
    }

    private OrderStatusEmailContent buildReturnRequested(Order order) {
        String shopName = (order.getShop() != null && order.getShop().getShopName() != null)
                ? order.getShop().getShopName() : "OneShop";
        String subject = "Đã nhận yêu cầu hoàn trả - Đơn #" + order.getOrderId() + " - " + shopName;
        String reason = escapeHtml(order.getCancellationReason());
        if (reason.isEmpty()) {
            reason = "(không có lý do kèm theo)";
        }
        NumberFormat vnd = vndFormat();
        double total = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;

        String body = ""
                + "<div style='font-family:Arial,Helvetica,sans-serif;line-height:1.6;color:#111'>"
                + "<h2 style='color:#b45309;margin:0 0 12px'>Yêu cầu hoàn trả đã được ghi nhận</h2>"
                + "<p>Chào " + (order.getCustomerName() != null ? escapeHtml(order.getCustomerName()) : "bạn") + ",</p>"
                + "<p>Chúng tôi đã nhận yêu cầu trả hàng / hoàn tiền cho đơn <strong>#" + order.getOrderId()
                + "</strong> tại <strong>" + escapeHtml(shopName) + "</strong>.</p>"
                + "<div style='margin:16px 0;padding:12px;background:#fffbeb;border:1px solid #fcd34d;border-radius:8px'>"
                + "<p style='margin:0'><strong>Lý do bạn cung cấp:</strong></p>"
                + "<p style='margin:8px 0 0;white-space:pre-wrap'>" + reason + "</p>"
                + "</div>"
                + "<p>Shop sẽ xem xét và phản hồi trong thời gian sớm nhất. Tổng giá trị đơn (tham khảo): <strong>"
                + vnd.format(total) + "</strong>.</p>"
                + "<div style='margin-top:16px'>"
                + "<a href='" + baseUrl + "/user/my-orders?status=return_requested' "
                + "style='display:inline-block;background:#b45309;color:#fff;padding:10px 16px;border-radius:6px;text-decoration:none'>"
                + "Xem đơn hàng</a>"
                + "</div>"
                + "<p style='margin-top:16px'>Trân trọng,<br/>Đội ngũ OneShop</p>"
                + "</div>";

        return new OrderStatusEmailContent(subject, body);
    }

    private OrderStatusEmailContent buildOrderReturned(Order order) {
        String shopName = (order.getShop() != null && order.getShop().getShopName() != null)
                ? order.getShop().getShopName() : "OneShop";
        String subject = "Hoàn trả đã được duyệt - Đơn #" + order.getOrderId() + " - " + shopName;
        NumberFormat vnd = vndFormat();
        double total = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
        String itemsHtml = buildItemsTableHtml(order);

        String body = ""
                + "<div style='font-family:Arial,Helvetica,sans-serif;line-height:1.6;color:#111'>"
                + "<h2 style='color:#15803d;margin:0 0 12px'>Hoàn trả / hoàn tiền đã được xử lý</h2>"
                + "<p>Chào " + (order.getCustomerName() != null ? escapeHtml(order.getCustomerName()) : "bạn") + ",</p>"
                + "<p>Yêu cầu hoàn trả cho đơn <strong>#" + order.getOrderId() + "</strong> tại <strong>"
                + escapeHtml(shopName) + "</strong> đã được <strong>duyệt</strong>. "
                + "Tiền hoàn sẽ được chuyển theo phương thức bạn đã chọn (OneXu hoặc chuyển khoản) trong thời gian xử lý của hệ thống và ngân hàng.</p>"
                + "<table style='width:100%;border-collapse:collapse;margin-top:8px'>"
                + "<thead><tr>"
                + "<th style='text-align:left;padding:8px 12px;border-bottom:2px solid #ddd'>Sản phẩm</th>"
                + "<th style='text-align:center;padding:8px 12px;border-bottom:2px solid #ddd'>SL</th>"
                + "<th style='text-align:right;padding:8px 12px;border-bottom:2px solid #ddd'>Đơn giá</th>"
                + "<th style='text-align:right;padding:8px 12px;border-bottom:2px solid #ddd'>Thành tiền</th>"
                + "</tr></thead><tbody>" + itemsHtml + "</tbody></table>"
                + "<p style='text-align:right;margin:12px 0;font-size:16px'><strong>Tổng đơn (tham khảo): " + vnd.format(total) + "</strong></p>"
                + "<div style='margin-top:16px'>"
                + "<a href='" + baseUrl + "/user/my-orders' style='display:inline-block;background:#15803d;color:#fff;padding:10px 16px;border-radius:6px;text-decoration:none'>"
                + "Theo dõi đơn hàng</a>"
                + "</div>"
                + "<p style='margin-top:16px'>Trân trọng,<br/>Đội ngũ OneShop</p>"
                + "</div>";

        return new OrderStatusEmailContent(subject, body);
    }

    private OrderStatusEmailContent buildOrderCancelled(Order order, Order.OrderStatus oldStatus) {
        String shopName = (order.getShop() != null && order.getShop().getShopName() != null)
                ? order.getShop().getShopName() : "OneShop";
        String subject = "Đơn hàng đã hủy - #" + order.getOrderId() + " - " + shopName;
        String reason = escapeHtml(order.getCancellationReason());
        if (reason.isEmpty()) {
            reason = "(không có ghi chú từ người bán)";
        }
        NumberFormat vnd = vndFormat();
        double total = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
        String itemsHtml = buildItemsTableHtml(order);

        String body = ""
                + "<div style='font-family:Arial,Helvetica,sans-serif;line-height:1.6;color:#111'>"
                + "<h2 style='color:#b91c1c;margin:0 0 12px'>Đơn hàng đã bị hủy</h2>"
                + "<p>Chào " + (order.getCustomerName() != null ? escapeHtml(order.getCustomerName()) : "bạn") + ",</p>"
                + "<p>Đơn hàng <strong>#" + order.getOrderId() + "</strong> tại <strong>" + escapeHtml(shopName)
                + "</strong> đã chuyển sang trạng thái <strong>Đã hủy</strong> "
                + "(trước đó: <strong>" + oldStatus.name() + "</strong>).</p>"
                + "<div style='margin:16px 0;padding:12px;background:#fef2f2;border:1px solid #fecaca;border-radius:8px'>"
                + "<p style='margin:0'><strong>Ghi chú / lý do:</strong></p>"
                + "<p style='margin:8px 0 0;white-space:pre-wrap'>" + reason + "</p>"
                + "</div>"
                + "<table style='width:100%;border-collapse:collapse;margin-top:8px'>"
                + "<thead><tr>"
                + "<th style='text-align:left;padding:8px 12px;border-bottom:2px solid #ddd'>Sản phẩm</th>"
                + "<th style='text-align:center;padding:8px 12px;border-bottom:2px solid #ddd'>SL</th>"
                + "<th style='text-align:right;padding:8px 12px;border-bottom:2px solid #ddd'>Đơn giá</th>"
                + "<th style='text-align:right;padding:8px 12px;border-bottom:2px solid #ddd'>Thành tiền</th>"
                + "</tr></thead><tbody>" + itemsHtml + "</tbody></table>"
                + "<p style='text-align:right;margin:12px 0;font-size:16px'><strong>Tổng đơn (tham khảo): " + vnd.format(total) + "</strong></p>"
                + "<div style='margin-top:16px'>"
                + "<a href='" + baseUrl + "/user/my-orders' style='display:inline-block;background:#374151;color:#fff;padding:10px 16px;border-radius:6px;text-decoration:none'>"
                + "Xem đơn hàng</a>"
                + "</div>"
                + "<p style='margin-top:16px'>Trân trọng,<br/>Đội ngũ OneShop</p>"
                + "</div>";

        return new OrderStatusEmailContent(subject, body);
    }

    public record OrderStatusEmailContent(String subject, String htmlBody) {}
}
