<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %> <%@
taglib prefix="c" uri="jakarta.tags.core" %>

<div class="page-header">
  <h2 class="page-title">Chi tiết khuyến mãi</h2>
</div>

<div class="page-inner">
  <div class="card">
    <div class="card-header">
      <i class="fas fa-tags"></i> ${promotion.promotionName}
    </div>
    <div class="card-body">
      <div class="row">
        <div class="col-md-6">
          <p><strong>Mã:</strong> ${promotion.promotionCode}</p>
          <p><strong>Loại:</strong> ${promotion.promotionType}</p>
          <p><strong>Giá trị:</strong> ${promotion.discountValue}</p>
          <p><strong>Mô tả:</strong> ${promotion.description}</p>
        </div>
        <div class="col-md-6">
          <p><strong>Bắt đầu:</strong> ${promotion.startDate}</p>
          <p><strong>Kết thúc:</strong> ${promotion.endDate}</p>
          <p><strong>Tối thiểu:</strong> ${promotion.minimumOrderAmount}</p>
          <p>
            <strong>Giảm tối đa:</strong> ${promotion.maximumDiscountAmount}
          </p>
        </div>
      </div>
      <div class="mt-3">
        <a href="/admin/promotions" class="btn btn-secondary"
          ><i class="fas fa-arrow-left"></i> Quay lại</a
        >
        <a
          href="/admin/promotions/edit/${promotion.promotionId}"
          class="btn btn-warning"
          ><i class="fas fa-edit"></i> Sửa</a
        >
        <a
          href="/admin/promotions/toggle/${promotion.promotionId}"
          class="btn btn-primary"
          ><i class="fas fa-toggle-on"></i> ${promotion.isActive ? 'Tạm dừng' :
          'Kích hoạt'}</a
        >
        <a
          href="/admin/promotions/delete/${promotion.promotionId}"
          class="btn btn-danger"
          onclick="return confirm('Xóa?')"
          ><i class="fas fa-trash"></i> Xóa</a
        >
      </div>
    </div>
  </div>
</div>
