package vn.facade.refund;

import vn.entity.User;
import vn.facade.refund.dto.RefundRequestViewResult;
import vn.facade.refund.dto.RefundSubmitResult;

public interface RefundFacade {
    RefundRequestViewResult prepareRefundRequestView(Long orderId, User user);

    RefundSubmitResult submitRefundRequest(Long orderId,
                                           User user,
                                           String refundMethod,
                                           String bankName,
                                           String bankAccountNumber,
                                           String accountHolderName,
                                           String bankBranch,
                                           String contactPhone);
}

