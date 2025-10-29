<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %> <%@
taglib prefix="c" uri="jakarta.tags.core" %>

<div class="page-header">
  <div class="d-flex align-items-center justify-content-between w-100">
    <h2 class="page-title mb-0">Quản lý tài khoản</h2>
    <div class="ml-auto">
      <button
        class="btn btn-primary"
        data-toggle="modal"
        data-target="#createUserModal"
      >
        <i class="fas fa-user-plus"></i> Tạo tài khoản
      </button>
    </div>
  </div>
</div>

<div class="page-inner">
  <form method="get" class="d-flex align-items-center mb-3" style="gap:12px">
    <input type="hidden" name="page" value="0" />
    <div class="input-group" style="min-width:300px">
      <span class="input-group-text"><i class="fas fa-search"></i></span>
      <input class="form-control" type="text" name="q" value="${searchTerm}" placeholder="Tìm theo tên hoặc email tài khoản..." />
    </div>
    <select name="size" class="form-control form-control-sm" style="width:auto">
      <option value="5"  ${currentSize==5? 'selected': ''}>5 dòng</option>
      <option value="10" ${currentSize==10? 'selected': ''}>10 dòng</option>
      <option value="20" ${currentSize==20? 'selected': ''}>20 dòng</option>
      <option value="50" ${currentSize==50? 'selected': ''}>50 dòng</option>
    </select>
    <button class="btn btn-primary" type="submit"><i class="fas fa-search"></i> Tìm</button>
    <a class="btn btn-outline-secondary" href="/admin/accounts">Đặt lại</a>
  </form>
  <!-- Success/Error Messages -->
  <c:if test="${param.success == 'updated'}">
    <div class="alert alert-success alert-dismissible fade show" role="alert">
      <strong><i class="fas fa-check-circle"></i> Thành công!</strong> Cập nhật
      quyền thành công. <strong>Lưu ý:</strong> User cần đăng xuất và đăng nhập
      lại để các quyền mới có hiệu lực.
      <button
        type="button"
        class="close"
        data-dismiss="alert"
        aria-label="Close"
      >
        <span aria-hidden="true">&times;</span>
      </button>
    </div>
  </c:if>
  <c:if test="${param.success == 'deleted'}">
    <div class="alert alert-success alert-dismissible fade show" role="alert">
      <strong><i class="fas fa-check-circle"></i> Thành công!</strong> Đã xóa
      tài khoản.
      <button
        type="button"
        class="close"
        data-dismiss="alert"
        aria-label="Close"
      >
        <span aria-hidden="true">&times;</span>
      </button>
    </div>
  </c:if>
  <c:if test="${param.error != null}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
      <strong><i class="fas fa-exclamation-triangle"></i> Lỗi!</strong>
      <c:choose>
        <c:when test="${param.error == 'missing_user'}"
          >Thiếu ID người dùng.</c:when
        >
        <c:when test="${param.error == 'update_failed'}"
          >Không thể cập nhật quyền.</c:when
        >
        <c:when test="${param.error == 'cannot_delete_self'}"
          >Không thể xóa tài khoản của chính mình.</c:when
        >
        <c:when test="${param.error == 'delete_failed'}"
          >Không thể xóa tài khoản. Có thể tài khoản có dữ liệu liên
          quan.</c:when
        >
        <c:otherwise>Đã xảy ra lỗi không xác định.</c:otherwise>
      </c:choose>
      <button
        type="button"
        class="close"
        data-dismiss="alert"
        aria-label="Close"
      >
        <span aria-hidden="true">&times;</span>
      </button>
    </div>
  </c:if>
  <div class="card">
    <div class="card-header">
      <i class="fas fa-users-cog"></i> Danh sách tài khoản
    </div>
    <div class="card-body p-0">
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th>ID</th>
              <th>Tên</th>
              <th>Email</th>
              <th>Trạng thái</th>
              <th>Roles</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="u" items="${users}">
              <tr>
                <td><span class="badge badge-primary">${u.userId}</span></td>
                <td><strong>${u.name}</strong></td>
                <td>${u.email}</td>
                <td>
                  <c:choose>
                    <c:when test="${u.status}"
                      ><span class="badge badge-success"
                        >Hoạt động</span
                      ></c:when
                    >
                    <c:otherwise
                      ><span class="badge badge-danger"
                        >Tạm dừng</span
                      ></c:otherwise
                    >
                  </c:choose>
                </td>
                <td>
                  <c:forEach var="r" items="${u.roles}">
                    <span class="badge badge-info" style="margin-right: 4px"
                      >${r.name}</span
                    >
                  </c:forEach>
                </td>
                <td>
                  <div class="d-flex gap-2" style="gap: 8px">
                    <button
                      class="btn btn-sm btn-secondary"
                      data-toggle="modal"
                      data-target="#rolesModal"
                      data-userid="${u.userId}"
                      data-username="${u.name}"
                      onclick="setRolesTarget('${u.userId}','${u.name}')"
                      title="Cấp quyền"
                    >
                      <i class="fas fa-user-shield"></i>
                    </button>
                    <button
                      class="btn btn-sm btn-danger"
                      data-userid="${u.userId}"
                      data-username="${u.name}"
                      onclick="confirmDeleteUser(this)"
                      title="Xóa tài khoản"
                    >
                      <i class="fas fa-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty users}">
              <tr>
                <td colspan="6" class="text-center p-4">
                  <i class="fas fa-inbox"></i>
                  <h5 class="mt-2 mb-0">Chưa có tài khoản</h5>
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
        <li class="page-item ${hasPrev? '': 'disabled'}"><a class="page-link" href="?page=${currentPage-1}&size=${currentSize}&q=${searchTerm}">Trước</a></li>
        <c:forEach var="i" begin="0" end="${totalPages-1}">
          <li class="page-item ${i==currentPage?'active':''}"><a class="page-link" href="?page=${i}&size=${currentSize}&q=${searchTerm}">${i+1}</a></li>
        </c:forEach>
        <li class="page-item ${hasNext? '': 'disabled'}"><a class="page-link" href="?page=${currentPage+1}&size=${currentSize}&q=${searchTerm}">Sau</a></li>
      </ul>
    </nav>
  </c:if>

  <!-- Modal cập nhật role -->
  <div
    class="modal fade"
    id="rolesModal"
    tabindex="-1"
    role="dialog"
    aria-hidden="true"
  >
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">
            Cấp quyền cho: <span id="rolesModalUserName"></span>
          </h5>
          <button
            type="button"
            class="close"
            data-dismiss="modal"
            aria-label="Close"
          >
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <form
          method="post"
          action="/admin/accounts/update-roles"
          onsubmit="return ensureUserId()"
        >
          <input type="hidden" name="_method" value="post" />
          <div class="modal-body">
            <input type="hidden" name="userId" id="rolesModalUserId" />
            <div class="form-group">
              <label>Chọn quyền</label>
              <div>
                <c:forEach var="r" items="${roles}">
                  <label class="mr-3" style="font-weight: 500">
                    <input type="checkbox" name="roleIds" value="${r.id}" />
                    ${r.name}
                  </label>
                </c:forEach>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              data-dismiss="modal"
            >
              Hủy
            </button>
            <button type="submit" class="btn btn-primary">Lưu</button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <!-- Modal tạo tài khoản -->
  <div
    class="modal fade"
    id="createUserModal"
    tabindex="-1"
    role="dialog"
    aria-hidden="true"
  >
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Tạo tài khoản mới</h5>
          <button
            type="button"
            class="close"
            data-dismiss="modal"
            aria-label="Close"
          >
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <form method="post" action="/admin/accounts/create">
          <div class="modal-body">
            <div class="form-group">
              <label>Tên</label>
              <input class="form-control" name="name" required />
            </div>
            <div class="form-group">
              <label>Email</label>
              <input class="form-control" type="email" name="email" required />
            </div>
            <div class="form-group">
              <label>Mật khẩu</label>
              <input
                class="form-control"
                type="password"
                name="password"
                required
              />
            </div>
            <div class="form-group">
              <label>Chọn quyền</label>
              <div>
                <c:forEach var="r" items="${roles}">
                  <label class="mr-3" style="font-weight: 500">
                    <input type="checkbox" name="roleIds" value="${r.id}" />
                    ${r.name}
                  </label>
                </c:forEach>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              data-dismiss="modal"
            >
              Hủy
            </button>
            <button type="submit" class="btn btn-primary">Tạo</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</div>

<script>
  $("#rolesModal").on("show.bs.modal", function (event) {
    var button = $(event.relatedTarget);
    var userId = button.data("userid");
    var userName = button.data("username");
    $(this).find("#rolesModalUserId").val(userId);
    $(this).find("#rolesModalUserName").text(userName);
  });

  // Fallback: set fields immediately on click before modal opens
  $(document).on("click", '[data-target="#rolesModal"]', function () {
    var userId = $(this).data("userid");
    var userName = $(this).data("username");
    $("#rolesModalUserId").val(userId);
    $("#rolesModalUserName").text(userName);
  });

  // Hard set values via inline onclick
  function setRolesTarget(id, name) {
    document.getElementById("rolesModalUserId").value = id;
    document.getElementById("rolesModalUserName").textContent = name;
  }

  function ensureUserId() {
    var id = document.getElementById("rolesModalUserId");
    if (!id || !id.value) {
      alert("Thiếu userId, vui lòng mở lại hộp thoại.");
      return false;
    }
    return true;
  }

  // Delete user function
  function confirmDeleteUser(button) {
    var userId = $(button).data("userid");
    var userName = $(button).data("username");

    if (
      confirm(
        'Bạn có chắc chắn muốn xóa tài khoản "' +
          userName +
          '" (ID: ' +
          userId +
          ")?\n\nLưu ý: Hành động này không thể hoàn tác!"
      )
    ) {
      // Create a hidden form and submit
      var form = document.createElement("form");
      form.method = "POST";
      form.action = "/admin/accounts/delete";

      var userIdInput = document.createElement("input");
      userIdInput.type = "hidden";
      userIdInput.name = "userId";
      userIdInput.value = userId;
      form.appendChild(userIdInput);

      document.body.appendChild(form);
      form.submit();
    }
  }
</script>
