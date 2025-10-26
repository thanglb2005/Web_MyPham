<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<style>
  .badge-status { padding: 6px 10px; border-radius: 999px; font-weight: 600; font-size: 12px; }
  .badge-PENDING { background:#fff3cd; color:#856404; }
  .badge-CONFIRMED { background:#cfe2ff; color:#084298; }
  .badge-SHIPPING { background:#bee5eb; color:#0c5460; }
  .badge-DELIVERED { background:#d4edda; color:#155724; }
  .badge-CANCELLED { background:#f8d7da; color:#721c24; }
  .badge-RETURNED { background:#e2e3e5; color:#383d41; }
  .actions-inline { display:flex; gap:8px; align-items:center; }
</style>

<div class="page-header">
  <h2 class="page-title">Đơn hàng</h2>
</div>

<div class="page-inner">
  <div class="d-flex align-items-center justify-content-between mb-3">
    <form method="get" class="d-flex align-items-center" style="gap:12px">
      <input type="hidden" name="page" value="0" />
      <div class="input-group" style="min-width:300px">
        <span class="input-group-text"><i class="fas fa-search"></i></span>
        <input class="form-control" type="text" name="search" value="${searchTerm}" placeholder="Tìm theo mã đơn / tên KH..." />
      </div>
      <select name="shopId" class="form-control form-control-sm" style="width:auto" onchange="this.form.submit()">
        <option value="">Tất cả shop</option>
        <c:forEach var="shop" items="${allShops}">
          <option value="${shop.shopId}" ${selectedShopId == shop.shopId ? 'selected' : ''}>${shop.shopName}</option>
        </c:forEach>
      </select>
      <select name="orderStatus" class="form-control form-control-sm" style="width:auto" onchange="this.form.submit()">
        <option value="">Tất cả trạng thái</option>
        <c:forEach var="s" items="${allStatuses}">
          <option value="${s}" ${selectedStatus == s ? 'selected' : ''}>${s}</option>
        </c:forEach>
      </select>
      <select name="size" class="form-control form-control-sm" style="width:auto" onchange="this.form.submit()">
        <option value="5"  ${currentSize==5? 'selected': ''}>5 dòng</option>
        <option value="10" ${currentSize==10? 'selected': ''}>10 dòng</option>
        <option value="20" ${currentSize==20? 'selected': ''}>20 dòng</option>
        <option value="50" ${currentSize==50? 'selected': ''}>50 dòng</option>
      </select>
    </form>
  </div>

  <div class="card">
    <div class="card-header"><i class="fas fa-receipt"></i> Danh sách đơn hàng</div>
    <div class="card-body p-0">
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th style="width:120px">Mã đơn</th>
              <th style="width:160px">Khách hàng</th>
              <th>Shop</th>
              <th style="width:140px">Tổng tiền</th>
              <th style="width:140px">Trạng thái</th>
              <th style="width:160px">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="o" items="${orderDetails}">
              <tr>
                <td><span class="badge badge-primary">${o.orderId}</span></td>
                <td>${o.customerName}</td>
                <td><span class="badge badge-info">${o.shop != null ? o.shop.shopName : 'N/A'}</span></td>
                <td>${o.totalAmount} ₫</td>
                <td><span class="badge-status badge-${o.status}">${o.status}</span></td>
                <td>
                  <div class="actions-inline">
                    <a class="btn btn-sm btn-info" href="/admin/order/detail/${o.orderId}"><i class="fas fa-eye"></i></a>
                    <a class="btn btn-sm btn-success" href="/admin/order/confirm/${o.orderId}"><i class="fas fa-check"></i></a>
                    <a class="btn btn-sm btn-primary" href="/admin/order/delivered/${o.orderId}"><i class="fas fa-truck"></i></a>
                    <a class="btn btn-sm btn-danger" href="/admin/order/cancel/${o.orderId}"><i class="fas fa-times"></i></a>
                  </div>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty orders}">
              <tr>
                <td colspan="6" class="text-center p-4">
                  <i class="fas fa-inbox"></i>
                  <h5 class="mt-2 mb-0">Không có đơn hàng</h5>
                </td>
              </tr>
            </c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <c:if test="${totalPages > 1}">
    <nav class="mt-3" aria-label="Pagination">
      <ul class="pagination">
        <li class="page-item ${hasPrev? '': 'disabled'}"><a class="page-link" href="?page=${currentPage-1}&size=${currentSize}&orderStatus=${selectedStatus}&shopId=${selectedShopId}&search=${searchTerm}">Trước</a></li>
        <c:forEach var="i" begin="0" end="${totalPages-1}">
          <li class="page-item ${i==currentPage?'active':''}"><a class="page-link" href="?page=${i}&size=${currentSize}&orderStatus=${selectedStatus}&shopId=${selectedShopId}&search=${searchTerm}">${i+1}</a></li>
        </c:forEach>
        <li class="page-item ${hasNext? '': 'disabled'}"><a class="page-link" href="?page=${currentPage+1}&size=${currentSize}&orderStatus=${selectedStatus}&shopId=${selectedShopId}&search=${searchTerm}">Sau</a></li>
      </ul>
    </nav>
  </c:if>
</div>


