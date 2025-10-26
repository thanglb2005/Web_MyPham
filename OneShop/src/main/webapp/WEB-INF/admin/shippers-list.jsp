<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %> <%@
taglib prefix="c" uri="jakarta.tags.core" %> <%@ taglib prefix="fn"
uri="jakarta.tags.functions" %>

<style>
  .shipper-card {
    background: #fff;
    border: 1px solid #e9ecef;
    border-radius: 12px;
    padding: 16px;
    margin-bottom: 12px;
  }
  .status-badge {
    padding: 4px 10px;
    border-radius: 999px;
    font-weight: 600;
    font-size: 12px;
  }
  .status-approved {
    background: #d4edda;
    color: #155724;
  }
  .status-pending {
    background: #fff3cd;
    color: #856404;
  }
  .shop-chip {
    background: #e9f5ff;
    color: #0069d9;
    padding: 4px 8px;
    border-radius: 999px;
    font-size: 12px;
    margin-right: 6px;
    display: inline-block;
  }
</style>

<div class="page-header d-flex align-items-center justify-content-between">
  <h2 class="page-title">Danh sách Shipper</h2>
  <div>
    <button
      class="btn btn-success"
      data-toggle="modal"
      data-target="#addShipperModal"
    >
      <i class="fas fa-plus"></i> Thêm Shipper mới
    </button>
  </div>
</div>

<div class="page-inner">
  <div class="row text-center mb-3">
    <div class="col-sm-3">
      <h4>${totalShippers}</h4>
      <div>Tổng Shipper</div>
    </div>
    <div class="col-sm-3">
      <h4>${approvedShippers}</h4>
      <div>Đã duyệt</div>
    </div>
    <div class="col-sm-3">
      <h4>${pendingShippers}</h4>
      <div>Chờ duyệt</div>
    </div>
    <div class="col-sm-3">
      <h4>${assignedShippers}</h4>
      <div>Đã phân công</div>
    </div>
  </div>

  <c:forEach var="s" items="${shippers}">
    <div class="shipper-card d-flex justify-content-between align-items-center">
      <div>
        <h5 class="mb-1">${s.name}</h5>
        <div class="text-muted mb-2">${s.email}</div>
        <div>
          <c:forEach var="shop" items="${s.assignedShops}">
            <span class="shop-chip">${shop.shopName}</span>
          </c:forEach>
        </div>
      </div>
      <div class="text-right">
        <div class="mb-2">
          <span
            class="status-badge ${s.status ? 'status-approved' : 'status-pending'}"
            >${s.status ? 'Đã duyệt' : 'Chờ duyệt'}</span
          >
        </div>
        <div
          class="actions-inline"
          style="display: flex; gap: 8px; justify-content: flex-end"
        >
          <a
            class="btn btn-sm btn-info"
            href="/admin/shipper-detail/${s.userId}"
            ><i class="fas fa-eye"></i> Xem chi tiết</a
          >
          <button
            type="button"
            class="btn btn-sm btn-warning"
            onclick="openAssignModal(${s.userId})"
          >
            <i class="fas fa-tasks"></i> Phân công
          </button>
          <c:if test="${s.status == null || !s.status}">
            <button
              type="button"
              class="btn btn-sm btn-success"
              onclick="approveShipper(${s.userId})"
            >
              <i class="fas fa-check"></i> Duyệt
            </button>
            <button
              type="button"
              class="btn btn-sm btn-danger"
              onclick="rejectShipper(${s.userId})"
            >
              <i class="fas fa-times"></i> Từ chối
            </button>
          </c:if>
        </div>
      </div>
    </div>
  </c:forEach>
</div>

<!-- Add Shipper Modal -->
<div class="modal fade" id="addShipperModal" tabindex="-1" role="dialog">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">
          <i class="fas fa-user-plus"></i> Thêm Shipper mới
        </h5>
        <button type="button" class="close" data-dismiss="modal">
          <span>&times;</span>
        </button>
      </div>
      <form id="addShipperForm">
        <div class="modal-body">
          <div class="form-group">
            <label>Họ tên</label>
            <input type="text" class="form-control" name="name" required />
          </div>
          <div class="form-group">
            <label>Email</label>
            <input type="email" class="form-control" name="email" required />
          </div>
          <div class="form-group">
            <label>Mật khẩu tạm</label>
            <input type="text" class="form-control" name="password" required />
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">
            Hủy
          </button>
          <button type="submit" class="btn btn-primary">Tạo mới</button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- Assign Shipper Modal -->
<div class="modal fade" id="assignShipperModal" tabindex="-1" role="dialog">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">
          <i class="fas fa-user-check"></i> Phân công Shipper cho Shop
        </h5>
        <button type="button" class="close" data-dismiss="modal">
          <span>&times;</span>
        </button>
      </div>
      <form id="assignShipperForm">
        <div class="modal-body">
          <input type="hidden" name="shipperId" id="assignShipperId" />
          <div class="form-group">
            <label>Chọn Shop</label>
            <select name="shopId" class="form-control" required>
              <c:forEach var="shop" items="${shops}">
                <option value="${shop.shopId}">${shop.shopName}</option>
              </c:forEach>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">
            Hủy
          </button>
          <button type="submit" class="btn btn-primary">Phân công</button>
        </div>
      </form>
    </div>
  </div>
</div>

<script>
  function approveShipper(id) {
    fetch("/admin/approve-shipper", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: "shipperId=" + encodeURIComponent(id),
    })
      .then((r) => r.text())
      .then((t) => {
        if (t === "success") {
          location.reload();
        } else {
          alert("Không duyệt được");
        }
      });
  }

  document
    .getElementById("addShipperForm")
    ?.addEventListener("submit", function (e) {
      e.preventDefault();
      const fd = new URLSearchParams(new FormData(this));
      fetch("/admin/add-shipper", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: fd.toString(),
      })
        .then((r) => r.json())
        .then((res) => {
          if (res.success) {
            location.reload();
          } else {
            alert(res.message || "Lỗi");
          }
        });
    });

  function openAssignModal(shipperId) {
    document.getElementById("assignShipperId").value = shipperId;
    $("#assignShipperModal").modal("show");
  }

  document
    .getElementById("assignShipperForm")
    ?.addEventListener("submit", function (e) {
      e.preventDefault();
      const fd = new URLSearchParams(new FormData(this));
      fetch("/admin/assign-shipper", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: fd.toString(),
      })
        .then((r) => r.text())
        .then((t) => {
          if (t === "success") {
            location.reload();
          } else {
            alert("Không phân công được");
          }
        });
    });
</script>
