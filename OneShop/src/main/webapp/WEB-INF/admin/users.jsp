<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %> <%@
taglib prefix="c" uri="jakarta.tags.core" %> <%@ taglib prefix="fn"
uri="jakarta.tags.functions" %>

<style>
  .avatar-img {
    width: 48px;
    height: 48px;
    object-fit: cover;
    border-radius: 50%;
    background: #f8f9fa;
    display: block;
    margin: 0 auto;
  }
  .avatar-fallback {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: #f0f2f5;
    color: #6c757d;
  }
</style>

<div class="page-header">
  <h2 class="page-title">Khách hàng</h2>
</div>

<div class="page-inner">
  <form method="get" class="d-flex align-items-center mb-3" style="gap:12px">
    <input type="hidden" name="page" value="0" />
    <div class="input-group" style="min-width:300px">
      <span class="input-group-text"><i class="fas fa-search"></i></span>
      <input class="form-control" type="text" name="q" value="${searchTerm}" placeholder="Tìm theo tên hoặc email..." />
    </div>
    <select name="size" class="form-control form-control-sm" style="width:auto">
      <option value="5"  ${currentSize==5? 'selected': ''}>5 dòng</option>
      <option value="10" ${currentSize==10? 'selected': ''}>10 dòng</option>
      <option value="20" ${currentSize==20? 'selected': ''}>20 dòng</option>
      <option value="50" ${currentSize==50? 'selected': ''}>50 dòng</option>
    </select>
    <button class="btn btn-primary" type="submit"><i class="fas fa-search"></i> Tìm</button>
    <a class="btn btn-outline-secondary" href="/admin/users">Đặt lại</a>
  </form>
  <div class="card">
    <div class="card-header">
      <i class="fas fa-users"></i> Danh sách khách hàng
    </div>
    <div class="card-body p-0">
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th style="width: 100px">ID</th>
              <th style="width: 100px">Avatar</th>
              <th>Tên</th>
              <th>Email</th>
              <th style="width: 160px">Trạng thái</th>
              <th style="width: 160px">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="u" items="${users}">
              <tr>
                <td><span class="badge badge-primary">${u.userId}</span></td>
                <td>
                  <c:choose>
                    <c:when test="${not empty u.avatar}">
                      <img
                        src="${u.avatar}"
                        class="avatar-img"
                        alt="Avatar"
                        onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';"
                      />
                      <div class="avatar-fallback" style="display: none">
                        <i class="fas fa-user"></i>
                      </div>
                    </c:when>
                    <c:otherwise>
                      <div class="avatar-fallback">
                        <i class="fas fa-user"></i>
                      </div>
                    </c:otherwise>
                  </c:choose>
                </td>
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
                  <form
                    method="post"
                    action="/admin/users/toggle-status/${u.userId}"
                    style="display: inline"
                  >
                    <button type="submit" class="btn btn-sm btn-secondary">
                      <i class="fas fa-toggle-on"></i>
                    </button>
                  </form>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty users}">
              <tr>
                <td colspan="6" class="text-center p-4">
                  <i class="fas fa-inbox"></i>
                  <h5 class="mt-2 mb-0">Chưa có khách hàng nào</h5>
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
</div>
