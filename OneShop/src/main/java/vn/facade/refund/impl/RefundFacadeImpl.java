package vn.facade.refund.impl;

import org.springframework.stereotype.Service;
import vn.entity.Order;
import vn.entity.Refund;
import vn.entity.User;
import vn.facade.refund.RefundFacade;
import vn.facade.refund.dto.RefundRequestViewResult;
import vn.facade.refund.dto.RefundSubmitResult;
import vn.repository.OrderRepository;
import vn.service.OneXuService;
import vn.service.RefundService;

@Service
public class RefundFacadeImpl implements RefundFacade {

    private final RefundService refundService;
    private final OrderRepository orderRepository;
    private final OneXuService oneXuService;

    public RefundFacadeImpl(RefundService refundService,
                            OrderRepository orderRepository,
                            OneXuService oneXuService) {
        this.refundService = refundService;
        this.orderRepository = orderRepository;
        this.oneXuService = oneXuService;
    }

    @Override
    public RefundRequestViewResult prepareRefundRequestView(Long orderId, User user) {
        if (user == null) {
            return new RefundRequestViewResult(false, null, null, "/login", null, null, null, null);
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return new RefundRequestViewResult(false, "Khong tim thay don hang", null, "/user/my-orders", null, null, null, null);
        }

        if (!order.getUser().getUserId().equals(user.getUserId())) {
            return new RefundRequestViewResult(false, "Ban khong co quyen truy cap don hang nay", null, "/user/my-orders", null, null, null, null);
        }

        if (order.getStatus() != Order.OrderStatus.RETURNED) {
            return new RefundRequestViewResult(false, "Don hang chua duoc duyet hoan tien", null, "/user/my-orders", null, null, null, null);
        }

        Refund refund = refundService.getRefundByOrderId(orderId);
        if (refund == null) {
            return new RefundRequestViewResult(false, "Khong tim thay yeu cau hoan tien", null, "/user/my-orders", null, null, null, null);
        }

        if (refund.getRefundStatus() != Refund.RefundStatus.PENDING) {
            return new RefundRequestViewResult(false, null, "Yeu cau hoan tien da duoc xu ly", "/order-detail/" + orderId, null, null, null, null);
        }

        Double currentBalance = oneXuService.getUserBalance(user.getUserId());
        Double balanceAfter = currentBalance + refund.getRefundAmount();

        return new RefundRequestViewResult(true, null, null, null, order, refund, currentBalance, balanceAfter);
    }

    @Override
    public RefundSubmitResult submitRefundRequest(Long orderId,
                                                  User user,
                                                  String refundMethod,
                                                  String bankName,
                                                  String bankAccountNumber,
                                                  String accountHolderName,
                                                  String bankBranch,
                                                  String contactPhone) {
        if (user == null) {
            return new RefundSubmitResult(false, null, "/login");
        }

        try {
            Refund refund = refundService.getRefundByOrderId(orderId);
            if (refund == null) {
                return new RefundSubmitResult(false, "Khong tim thay yeu cau hoan tien", "/user/my-orders");
            }

            if (!refund.getUser().getUserId().equals(user.getUserId())) {
                return new RefundSubmitResult(false, "Ban khong co quyen truy cap refund nay", "/user/my-orders");
            }

            if ("ONEXU".equals(refundMethod)) {
                refundService.processRefundToOneXu(refund.getRefundId(), user.getUserId());
                return new RefundSubmitResult(true,
                        "Da hoan tien " + refund.getRefundAmount() + " VND vao OneXu cua ban!",
                        "/order-detail/" + orderId);
            }

            if ("BANK_TRANSFER".equals(refundMethod)) {
                refundService.processRefundToBank(refund.getRefundId(), user.getUserId(),
                        bankName, bankAccountNumber, accountHolderName, bankBranch, contactPhone);
                return new RefundSubmitResult(true,
                        "Da gui thong tin ngan hang. Vendor se xu ly trong 1-3 ngay lam viec.",
                        "/order-detail/" + orderId);
            }

            return new RefundSubmitResult(false, "Phuong thuc hoan tien khong hop le", "/refund-request/" + orderId);
        } catch (Exception e) {
            return new RefundSubmitResult(false, "Loi xu ly: " + e.getMessage(), "/refund-request/" + orderId);
        }
    }
}

