<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<style>
  .brand-image {
    width: 120px;
    height: 60px;
    object-fit: contain;
    display: block;
    margin: 0 auto;
  }
  .no-image {
    width: 120px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f8f9fa;
    border: 1px dashed #e9ecef;
    color: #6c757d;
  }
</style>

<div class="page-header">
  <h2 class="page-title">Thương hiệu</h2>
</div>

<div class="page-inner">
  <div class="d-flex align-items-center justify-content-between mb-3">
    <form method="get" class="d-flex align-items-center" style="gap:12px">
      <input type="hidden" name="page" value="0" />
      <input type="hidden" name="sortBy" value="${sortBy}" />
      <input type="hidden" name="sortDir" value="${sortDir}" />
      <div class="input-group" style="min-width:300px">
        <span class="input-group-text"><i class="fas fa-search"></i></span>
        <input class="form-control" type="text" name="search" value="${param.search}" placeholder="Tìm kiếm thương hiệu..." />
      </div>
      <select name="size" class="form-control form-control-sm" style="width:auto" onchange="this.form.submit()">
        <option value="5"  ${pageSize==5? 'selected': ''}>5 dòng</option>
        <option value="10" ${pageSize==10? 'selected': ''}>10 dòng</option>
        <option value="20" ${pageSize==20? 'selected': ''}>20 dòng</option>
        <option value="50" ${pageSize==50? 'selected': ''}>50 dòng</option>
      </select>
    </form>
    <button type="button" class="btn btn-primary" data-toggle="modal" data-target="#addBrandModal"><i class="fas fa-plus"></i> Thêm thương hiệu</button>
  </div>

  <div class="card">
    <div class="card-header"><i class="fas fa-list"></i> Danh sách thương hiệu</div>
    <div class="card-body p-0">
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th style="width:120px"><a href="?page=${currentPage}&size=${pageSize}&search=${param.search}&sortBy=brandId&sortDir=${sortBy=='brandId' && sortDir=='asc' ? 'desc' : 'asc'}">ID</a></th>
              <th style="width:120px">Ảnh</th>
              <th>Tên thương hiệu</th>
              <th style="width:220px">Trạng thái</th>
              <th style="width:160px">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="brand" items="${brands}">
              <tr>
                <td><span class="badge badge-primary">${brand.brandId}</span></td>
                <td>
                  <c:choose>
                    <c:when test="${not empty brand.brandImage}">
                      <img src="${fn:startsWith(brand.brandImage, 'http') ? brand.brandImage : '/brands/'.concat(brand.brandImage)}" class="brand-image" alt="Brand Image" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';" />
                      <div class="no-image" style="display:none"><i class="fas fa-image"></i></div>
                    </c:when>
                    <c:otherwise>
                      <div class="no-image"><i class="fas fa-image"></i></div>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td><strong>${brand.brandName}</strong></td>
                <td>
                  <c:choose>
                    <c:when test="${brand.status}"><span class="badge badge-success">Hoạt động</span></c:when>
                    <c:otherwise><span class="badge badge-danger">Tạm dừng</span></c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <div class="d-flex" style="gap:8px">
                    <form method="post" action="/admin/toggleBrandStatus/${brand.brandId}" style="display:inline">
                      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                      <button type="submit" class="btn btn-sm btn-secondary"><i class="fas fa-toggle-on"></i></button>
                    </form>
                    <button type="button" class="btn btn-sm btn-info" data-id="${brand.brandId}" data-name="${fn:escapeXml(brand.brandName)}" onclick="openEditBrand(this.dataset.id, this.dataset.name)"><i class="fas fa-edit"></i></button>
                    <button type="button" class="btn btn-sm btn-danger" data-id="${brand.brandId}" data-name="${fn:escapeXml(brand.brandName)}" onclick="confirmDeleteBrand(this.dataset.id, this.dataset.name)"><i class="fas fa-trash"></i></button>
                  </div>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty brands}">
              <tr>
                <td colspan="5" class="text-center p-4">
                  <i class="fas fa-inbox"></i>
                  <h5 class="mt-2 mb-0">Chưa có thương hiệu nào</h5>
                  <small>Hãy thêm thương hiệu đầu tiên để bắt đầu quản lý</small>
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
        <li class="page-item ${currentPage==0?'disabled':''}"><a class="page-link" href="?page=${currentPage-1}&size=${pageSize}&search=${param.search}&sortBy=${sortBy}&sortDir=${sortDir}">Trước</a></li>
        <c:forEach var="i" begin="0" end="${totalPages-1}">
          <li class="page-item ${i==currentPage?'active':''}"><a class="page-link" href="?page=${i}&size=${pageSize}&search=${param.search}&sortBy=${sortBy}&sortDir=${sortDir}">${i+1}</a></li>
        </c:forEach>
        <li class="page-item ${currentPage==totalPages-1?'disabled':''}"><a class="page-link" href="?page=${currentPage+1}&size=${pageSize}&search=${param.search}&sortBy=${sortBy}&sortDir=${sortDir}">Sau</a></li>
      </ul>
    </nav>
  </c:if>
</div>

<!-- Add Brand Modal -->
<div class="modal fade" id="addBrandModal" tabindex="-1" role="dialog">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title"><i class="fas fa-plus-circle"></i> Thêm thương hiệu</h5>
        <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
      </div>
      <form id="addBrandForm" method="post" action="/admin/addBrand" enctype="multipart/form-data">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <div class="modal-body">
          <div class="form-group">
            <label for="brandName">Tên thương hiệu <span class="text-danger">*</span></label>
            <input type="text" class="form-control" id="brandName" name="brandName" required />
          </div>
          <div class="form-group">
            <label for="brandImage">Logo</label>
            <input type="file" class="form-control-file" id="brandImage" name="brandImageFile" accept="image/*" />
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">Hủy</button>
          <button type="submit" class="btn btn-primary"><i class="fas fa-save"></i> Lưu</button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- Edit Brand Modal -->
<div class="modal fade" id="editBrandModal" tabindex="-1" role="dialog">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title"><i class="fas fa-edit"></i> Chỉnh sửa thương hiệu</h5>
        <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
      </div>
      <form id="editBrandForm" method="post" action="/admin/updateBrand" enctype="multipart/form-data">
        <div class="modal-body">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
          <input type="hidden" name="brandId" id="editBrandId" />
          <div class="form-group">
            <label for="editBrandName">Tên thương hiệu <span class="text-danger">*</span></label>
            <input type="text" class="form-control" id="editBrandName" name="brandName" required />
          </div>
          <div class="form-group">
            <label for="editBrandImage">Logo</label>
            <input type="file" class="form-control-file" id="editBrandImage" name="brandImageFile" accept="image/*" />
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">Hủy</button>
          <button type="submit" class="btn btn-primary"><i class="fas fa-save"></i> Cập nhật</button>
        </div>
      </form>
    </div>
  </div>
</div>

<script>
function openEditBrand(id, name) {
  document.getElementById('editBrandId').value = id;
  document.getElementById('editBrandName').value = name;
  $('#editBrandModal').modal('show');
}

function confirmDeleteBrand(id, name) {
  Swal.fire({
    title: 'Xác nhận xóa',
    html: `Bạn có chắc chắn muốn xóa thương hiệu <strong>"${name}"</strong>?`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#dc3545',
    cancelButtonColor: '#6c757d',
    confirmButtonText: '<i class="fas fa-trash"></i> Xóa',
    cancelButtonText: '<i class="fas fa-times"></i> Hủy'
  }).then((result) => {
    if (result.isConfirmed) {
      const form = document.createElement('form');
      form.method = 'POST';
      form.action = '/admin/deleteBrand/' + id;
      // CSRF token
      const csrf = document.createElement('input');
      csrf.type = 'hidden';
      csrf.name = '${_csrf.parameterName}';
      csrf.value = '${_csrf.token}';
      form.appendChild(csrf);
      document.body.appendChild(form);
      form.submit();
    }
  });
}

// Success toast cleanup (reuse from categories)
const urlParams = new URLSearchParams(window.location.search);
if (urlParams.get('success') || urlParams.get('error')) {
  let title = '';
  if (urlParams.get('success') === 'added') title = 'Thêm thương hiệu thành công!';
  else if (urlParams.get('success') === 'updated') title = 'Cập nhật thương hiệu thành công!';
  else if (urlParams.get('success') === 'deleted') title = 'Xóa thương hiệu thành công!';
  else if (urlParams.get('error')) title = 'Có lỗi xảy ra!';
  if (title) {
    Swal.fire({ icon: title.includes('lỗi') ? 'error' : 'success', title, showConfirmButton: false, timer: 2000 });
  }
  urlParams.delete('success');
  urlParams.delete('error');
  const remaining = urlParams.toString();
  const cleanUrl = window.location.pathname + (remaining ? ('?' + remaining) : '');
  window.history.replaceState({}, document.title, cleanUrl);
}
</script>


