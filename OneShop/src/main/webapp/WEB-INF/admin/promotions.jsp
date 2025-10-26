<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="page-header">
  <div class="d-flex align-items-center justify-content-between w-100">
    <h2 class="page-title mb-0">Quản lý khuyến mãi</h2>
    <div class="ml-auto">
      <a href="/admin/promotions/add" class="btn btn-primary">
        <i class="fas fa-plus"></i> Thêm khuyến mãi mới
      </a>
    </div>
  </div>
</div>

<div class="page-inner">
  <div class="d-flex align-items-center justify-content-between mb-3">
    <form method="get" class="d-flex align-items-center" style="gap:12px">
      <input type="hidden" name="page" value="0" />
      <input type="hidden" name="sortBy" value="${sortBy}" />
      <input type="hidden" name="sortDir" value="${sortDir}" />
      <div class="input-group" style="min-width:300px">
        <span class="input-group-text"><i class="fas fa-search"></i></span>
        <input class="form-control" type="text" name="search" value="${search}" placeholder="Tìm kiếm khuyến mãi..." />
      </div>
      <select name="size" class="form-control form-control-sm" style="width:auto" onchange="this.form.submit()">
        <option value="5"  ${size==5? 'selected': ''}>5 dòng</option>
        <option value="10" ${size==10? 'selected': ''}>10 dòng</option>
        <option value="20" ${size==20? 'selected': ''}>20 dòng</option>
        <option value="50" ${size==50? 'selected': ''}>50 dòng</option>
      </select>
    </form>
  </div>

  <div class="card">
    <div class="card-header"><i class="fas fa-tags"></i> Danh sách khuyến mãi</div>
    <div class="card-body p-0">
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th>ID</th>
              <th>Tên khuyến mãi</th>
              <th>Mã</th>
              <th>Loại</th>
              <th>Giá trị</th>
              <th>Bắt đầu</th>
              <th>Kết thúc</th>
              <th>Trạng thái</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="p" items="${promotions.content}">
              <tr>
                <td><span class="badge badge-primary">${p.promotionId}</span></td>
                <td><strong>${p.promotionName}</strong><br/><small class="text-muted">${p.description}</small></td>
                <td><span class="badge badge-info">${p.promotionCode}</span></td>
                <td><span class="badge badge-secondary">${p.promotionType}</span></td>
                <td>${p.discountValue}</td>
                <td>${p.startDate}</td>
                <td>${p.endDate}</td>
                <td>
                  <c:choose>
                    <c:when test="${p.isActive}"><span class="badge badge-success">Hoạt động</span></c:when>
                    <c:otherwise><span class="badge badge-danger">Tạm dừng</span></c:otherwise>
                  </c:choose>
                </td>
                <td class="actions-inline" style="display:flex; gap:6px">
                  <a href="/admin/promotions/view/${p.promotionId}" class="btn btn-sm btn-info" title="Xem"><i class="fas fa-eye"></i></a>
                  <a href="/admin/promotions/edit/${p.promotionId}" class="btn btn-sm btn-warning" title="Sửa"><i class="fas fa-edit"></i></a>
                  <a href="/admin/promotions/toggle/${p.promotionId}" class="btn btn-sm btn-secondary" title="Bật/Tắt"><i class="fas fa-toggle-on"></i></a>
                  <a href="/admin/promotions/delete/${p.promotionId}" class="btn btn-sm btn-danger" onclick="return confirm('Xóa?')" title="Xóa"><i class="fas fa-trash"></i></a>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${promotions == null || empty promotions.content}">
              <tr>
                <td colspan="9" class="text-center p-4">
                  <i class="fas fa-inbox"></i>
                  <h5 class="mt-2 mb-0">Chưa có khuyến mãi</h5>
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
        <li class="page-item ${currentPage==0?'disabled':''}"><a class="page-link" href="?page=${currentPage-1}&size=${size}&search=${search}&sortBy=${sortBy}&sortDir=${sortDir}">Trước</a></li>
        <c:forEach var="i" begin="0" end="${totalPages-1}">
          <li class="page-item ${i==currentPage?'active':''}"><a class="page-link" href="?page=${i}&size=${size}&search=${search}&sortBy=${sortBy}&sortDir=${sortDir}">${i+1}</a></li>
        </c:forEach>
        <li class="page-item ${currentPage==totalPages-1?'disabled':''}"><a class="page-link" href="?page=${currentPage+1}&size=${size}&search=${search}&sortBy=${sortBy}&sortDir=${sortDir}">Sau</a></li>
      </ul>
    </nav>
  </c:if>
</div>


