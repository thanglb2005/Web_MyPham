<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<style>
  .shop-logo {
    width: 48px;
    height: 48px;
    object-fit: cover;
    border-radius: 50%;
    background: #f0f2f5;
  }
  .status-badge {
    padding: 6px 10px;
    border-radius: 999px;
    font-weight: 600;
    font-size: 12px;
  }
  .status-PENDING { background: #fff3cd; color: #856404; }
  .status-ACTIVE { background: #d4edda; color: #155724; }
  .status-SUSPENDED { background: #ffe5e5; color: #c82333; }
  .status-REJECTED { background: #f8d7da; color: #721c24; }
</style>

<div class="page-header">
  <h2 class="page-title">Quản lý Shop</h2>
</div>

<div class="page-inner">
  <div class="d-flex align-items-center justify-content-between mb-3">
    <form method="get" class="d-flex align-items-center" style="gap:12px">
      <select name="status" class="form-control form-control-sm" style="width:auto" onchange="this.form.submit()">
        <option value="" ${selectedStatus == 'ALL' ? 'selected' : ''}>Tất cả trạng thái</option>
        <c:forEach var="s" items="${statuses}">
          <option value="${s}" ${selectedStatus == s.name() ? 'selected' : ''}>${s}</option>
        </c:forEach>
      </select>
    </form>
  </div>

  <div class="card">
    <div class="card-header"><i class="fas fa-store"></i> Danh sách shop</div>
    <div class="card-body p-0">
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th style="width:100px">ID</th>
              <th style="width:80px">Logo</th>
              <th>Tên shop</th>
              <th>Địa chỉ</th>
              <th style="width:160px">Trạng thái</th>
              <th style="width:240px">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="shop" items="${shops}">
              <tr>
                <td><span class="badge badge-primary">${shop.shopId}</span></td>
                <td>
                  <c:choose>
                    <c:when test="${not empty shop.shopLogo}">
                      <img src="${shop.shopLogo}" alt="Logo" class="shop-logo" />
                    </c:when>
                    <c:otherwise>
                      <div class="shop-logo d-flex align-items-center justify-content-center"><i class="fas fa-store"></i></div>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td><strong>${shop.shopName}</strong></td>
                <td>${shop.address}</td>
                <td><span class="status-badge status-${shop.status}">${shop.status}</span></td>
                <td>
                  <button type="button"
                          class="btn btn-sm btn-primary"
                          data-id="${shop.shopId}"
                          data-name="${fn:escapeXml(shop.shopName)}"
                          data-status="${shop.status}"
                          onclick="openUpdateStatusModal(this)">
                    <i class="fas fa-edit"></i> Cập nhật
                  </button>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty shops}">
              <tr>
                <td colspan="6" class="text-center p-4">
                  <i class="fas fa-inbox"></i>
                  <h5 class="mt-2 mb-0">Chưa có shop nào</h5>
                </td>
              </tr>
            </c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>

<!-- Update Status Modal -->
<div class="modal fade" id="updateStatusModal" tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title"><i class="fas fa-store"></i> Cập nhật trạng thái shop</h5>
        <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
      </div>
      <form id="updateStatusForm" method="post">
        <div class="modal-body">
          <div class="form-group">
            <label>Shop</label>
            <div id="shopNameDisplay" class="font-weight-bold">&nbsp;</div>
          </div>
          <div class="form-group">
            <label for="statusSelect">Trạng thái</label>
            <select id="statusSelect" name="status" class="form-control">
              <c:forEach var="s" items="${statuses}">
                <option value="${s}">${s}</option>
              </c:forEach>
            </select>
          </div>
          <div class="form-group">
            <label for="reasonInput">Lý do (nếu từ chối / tạm dừng)</label>
            <textarea id="reasonInput" name="reason" class="form-control" rows="3" placeholder="Nhập lý do..."></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">Hủy</button>
          <button type="submit" class="btn btn-primary"><i class="fas fa-save"></i> Lưu thay đổi</button>
        </div>
      </form>
    </div>
  </div>
  
</div>

<script>
function openUpdateStatusModal(btn) {
  var id = btn.dataset.id;
  var name = btn.dataset.name;
  var status = btn.dataset.status;
  var form = document.getElementById('updateStatusForm');
  form.action = '/admin/shops/' + id + '/status';
  document.getElementById('shopNameDisplay').textContent = name;
  var select = document.getElementById('statusSelect');
  select.value = status;
  document.getElementById('reasonInput').value = '';
  $('#updateStatusModal').modal('show');
}
</script>


