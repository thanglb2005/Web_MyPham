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
    padding: 4px 12px 4px 8px;
    border-radius: 999px;
    font-size: 12px;
    margin-right: 6px;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    position: relative;
  }
  .shop-chip .remove-btn {
    color: #dc3545;
    cursor: pointer;
    font-weight: bold;
    padding: 0 4px;
    border-radius: 50%;
    transition: background 0.2s;
  }
  .shop-chip .remove-btn:hover {
    background: rgba(220, 53, 69, 0.1);
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
          <c:if test="${s.assignedShops != null && !s.assignedShops.isEmpty()}">
            <c:forEach var="shop" items="${s.assignedShops}">
              <span class="shop-chip">
                <span>${shop.shopName}</span>
                <span
                  class="remove-btn"
                  onclick="removeShipperFromShop(${shop.shopId}, ${s.userId}, '${shop.shopName}')"
                  title="Gỡ shipper khỏi shop này"
                >
                  ×
                </span>
              </span>
            </c:forEach>
          </c:if>
          <c:if test="${s.assignedShops == null || s.assignedShops.isEmpty()}">
            <span class="text-muted"><em>Chưa phân công shop nào</em></span>
          </c:if>
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
          <button
            type="button"
            class="btn btn-sm btn-danger"
            onclick="deleteShipper(${s.userId})"
            title="Xóa shipper"
          >
            <i class="fas fa-trash"></i> Xóa
          </button>
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
            <label>Họ tên <span class="text-danger">*</span></label>
            <input
              type="text"
              class="form-control"
              name="name"
              required
              placeholder="Nhập họ tên shipper"
            />
          </div>
          <div class="form-group">
            <label>Email <span class="text-danger">*</span></label>
            <input
              type="email"
              class="form-control"
              name="email"
              required
              placeholder="Nhập email"
            />
            <small class="form-text text-muted"
              >Email sẽ dùng để đăng nhập</small
            >
          </div>
          <div class="form-group">
            <label>Mật khẩu <span class="text-danger">*</span></label>
            <input
              type="password"
              class="form-control"
              name="password"
              required
              placeholder="Nhập mật khẩu"
            />
            <small class="form-text text-muted">Tối thiểu 6 ký tự</small>
          </div>
          <div class="form-group">
            <div class="custom-control custom-checkbox">
              <input
                type="checkbox"
                class="custom-control-input"
                id="autoApproveCheckbox"
                name="autoApprove"
                value="true"
                checked
              />
              <label class="custom-control-label" for="autoApproveCheckbox"
                >Tự động duyệt shipper</label
              >
            </div>
            <small class="form-text text-muted"
              >Nếu không chọn, shipper sẽ ở trạng thái chờ duyệt</small
            >
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">
            <i class="fas fa-times me-1"></i>Hủy
          </button>
          <button type="submit" class="btn btn-primary">
            <i class="fas fa-save me-1"></i>Tạo mới
          </button>
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
    if (!confirm("Bạn có chắc muốn duyệt shipper này?")) {
      return;
    }
    fetch("/admin/approve-shipper", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: "shipperId=" + encodeURIComponent(id),
    })
      .then((r) => r.text())
      .then((t) => {
        if (t === "success") {
          alert("Đã duyệt shipper thành công!");
          location.reload();
        } else {
          alert("Không duyệt được");
        }
      });
  }

  function rejectShipper(id) {
    if (!confirm("Bạn có chắc muốn từ chối shipper này?")) {
      return;
    }
    fetch("/admin/reject-shipper", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: "shipperId=" + encodeURIComponent(id),
    })
      .then((r) => r.text())
      .then((t) => {
        if (t === "success") {
          alert("Đã từ chối shipper!");
          location.reload();
        } else {
          alert("Không từ chối được");
        }
      });
  }

  function deleteShipper(id) {
    if (
      !confirm(
        "Bạn có chắc muốn XÓA VĨNH VIỄN shipper này?\n\nHành động này KHÔNG THỂ hoàn tác!"
      )
    ) {
      return;
    }

    // Double confirm for safety
    if (!confirm("XÁC NHẬN LẦN CUỐI: Bạn thực sự muốn xóa shipper này?")) {
      return;
    }

    fetch("/admin/delete-shipper", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: "shipperId=" + encodeURIComponent(id),
    })
      .then((r) => r.text())
      .then((t) => {
        if (t === "success") {
          alert("Đã xóa shipper thành công!");
          location.reload();
        } else {
          alert("Không thể xóa shipper. Vui lòng thử lại!");
        }
      })
      .catch((err) => {
        console.error(err);
        alert("Lỗi kết nối khi xóa shipper!");
      });
  }

  function removeShipperFromShop(shopId, shipperId, shopName) {
    if (!confirm('Bạn có chắc muốn gỡ shipper khỏi shop "' + shopName + '"?')) {
      return;
    }

    fetch("/admin/remove-shipper", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body:
        "shopId=" +
        encodeURIComponent(shopId) +
        "&shipperId=" +
        encodeURIComponent(shipperId),
    })
      .then((r) => r.text())
      .then((t) => {
        if (t === "success") {
          alert("Đã gỡ shipper khỏi shop thành công!");
          location.reload();
        } else if (t === "duplicate") {
          alert("Shipper không thuộc shop này!");
        } else {
          alert("Không thể gỡ shipper khỏi shop. Vui lòng thử lại!");
        }
      })
      .catch((err) => {
        console.error(err);
        alert("Lỗi kết nối!");
      });
  }

  document
    .getElementById("addShipperForm")
    ?.addEventListener("submit", function (e) {
      e.preventDefault();

      // Get form data
      const formData = new FormData(this);
      const name = formData.get("name");
      const email = formData.get("email");
      const password = formData.get("password");
      const autoApprove = formData.get("autoApprove") === "true";

      // Validate
      if (!name || !email || !password) {
        alert("Vui lòng điền đầy đủ thông tin!");
        return;
      }

      if (password.length < 6) {
        alert("Mật khẩu phải có ít nhất 6 ký tự!");
        return;
      }

      // Create URLSearchParams
      const fd = new URLSearchParams();
      fd.append("name", name);
      fd.append("email", email);
      fd.append("password", password);
      fd.append("autoApprove", autoApprove);

      fetch("/admin/create-shipper", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: fd.toString(),
      })
        .then((r) => r.text())
        .then((res) => {
          if (res === "success") {
            alert("Tạo shipper thành công!");
            $("#addShipperModal").modal("hide");
            this.reset();
            location.reload();
          } else if (res === "email_exists") {
            alert("Email đã tồn tại trong hệ thống!");
          } else if (res === "role_not_found") {
            alert("Không tìm thấy role SHIPPER!");
          } else {
            alert("Lỗi: " + res);
          }
        })
        .catch((err) => {
          console.error(err);
          alert("Lỗi kết nối!");
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
