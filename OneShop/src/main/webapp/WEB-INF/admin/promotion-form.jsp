<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="page-header">
  <h2 class="page-title">${promotion != null && promotion.promotionId != null ? 'Chỉnh sửa khuyến mãi' : 'Thêm khuyến mãi mới'}</h2>
</div>

<div class="page-inner">
  <div class="card">
    <div class="card-header"><i class="fas fa-tags"></i> Thông tin khuyến mãi</div>
    <div class="card-body">
      <c:choose>
        <c:when test="${promotion != null && promotion.promotionId != null}">
          <c:set var="formAction" value="/admin/promotions/edit/${promotion.promotionId}" />
        </c:when>
        <c:otherwise>
          <c:set var="formAction" value="/admin/promotions/add" />
        </c:otherwise>
      </c:choose>

      <c:if test="${not empty success}">
        <div class="alert alert-success">${success}</div>
      </c:if>
      <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
      </c:if>

      <form method="post" action="${formAction}">
        <div class="form-group">
          <label>Tên khuyến mãi</label>
          <input type="text" class="form-control" name="promotionName" value="${promotion.promotionName}" required />
        </div>
        <div class="form-group">
          <label>Mô tả</label>
          <textarea class="form-control" rows="3" name="description">${promotion.description}</textarea>
        </div>
        <div class="form-row">
          <div class="form-group col-md-6">
            <label>Mã khuyến mãi</label>
            <input type="text" class="form-control" name="promotionCode" value="${promotion.promotionCode}" required />
          </div>
          <div class="form-group col-md-6">
            <label>Loại khuyến mãi</label>
            <select name="promotionType" class="form-control" required>
              <c:forEach var="t" items="${promotionTypes}">
                <option value="${t}" ${promotion.promotionType == t ? 'selected' : ''}>${t}</option>
              </c:forEach>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group col-md-6">
            <label>Giá trị giảm</label>
            <input type="number" step="0.01" class="form-control" name="discountValue" value="${promotion.discountValue}" required />
          </div>
          <div class="form-group col-md-6">
            <label>Đơn hàng tối thiểu</label>
            <input type="number" step="0.01" class="form-control" name="minimumOrderAmount" value="${promotion.minimumOrderAmount}" required />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group col-md-6">
            <label>Giảm tối đa</label>
            <input type="number" step="0.01" class="form-control" name="maximumDiscountAmount" value="${promotion.maximumDiscountAmount}" required />
          </div>
          <div class="form-group col-md-6">
            <label>Giới hạn sử dụng</label>
            <input type="number" class="form-control" name="usageLimit" value="${promotion.usageLimit}" required />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group col-md-6">
            <label>Ngày bắt đầu</label>
            <input type="datetime-local" class="form-control" name="startDate" value="${promotion.startDate}" required />
          </div>
          <div class="form-group col-md-6">
            <label>Ngày kết thúc</label>
            <input type="datetime-local" class="form-control" name="endDate" value="${promotion.endDate}" required />
          </div>
        </div>
        <c:if test="${promotion != null && promotion.promotionId != null}">
          <div class="form-group">
            <label>Kích hoạt</label>
            <div class="form-check">
              <input type="checkbox" class="form-check-input" id="isActive" name="isActive" ${promotion.isActive ? 'checked' : ''} />
              <label for="isActive" class="form-check-label">Hoạt động</label>
            </div>
          </div>
        </c:if>
        <div class="text-right">
          <a href="/admin/promotions" class="btn btn-secondary">Quay lại</a>
          <button type="submit" class="btn btn-primary"><i class="fas fa-save"></i> Lưu</button>
        </div>
      </form>
    </div>
  </div>
</div>


