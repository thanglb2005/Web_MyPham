<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!-- Page Header -->
<div class="page-header">
  <h2 class="page-title">Danh mục sản phẩm</h2>
</div>

<div class="page-inner">
      <div class="d-flex align-items-center justify-content-between mb-3">
        <form method="get" class="d-flex align-items-center" style="gap:12px">
          <input type="hidden" name="page" value="0" />
          <input type="hidden" name="sortBy" value="${sortBy}" />
          <input type="hidden" name="sortDir" value="${sortDir}" />
          <div class="input-group" style="min-width:300px">
            <span class="input-group-text"><i class="fas fa-search"></i></span>
            <input class="form-control" type="text" name="search" value="${param.search}" placeholder="Tìm kiếm danh mục..." />
          </div>
          <select name="size" class="form-control form-control-sm" style="width:auto" onchange="this.form.submit()">
            <option value="5"  ${pageSize==5? 'selected': ''}>5 dòng</option>
            <option value="10" ${pageSize==10? 'selected': ''}>10 dòng</option>
            <option value="20" ${pageSize==20? 'selected': ''}>20 dòng</option>
            <option value="50" ${pageSize==50? 'selected': ''}>50 dòng</option>
          </select>
        </form>
        <button type="button" class="btn btn-primary" data-toggle="modal" data-target="#addCategoryModal"><i class="fas fa-plus"></i> Thêm danh mục mới</button>
      </div>

      <div class="card">
        <div class="card-header"><i class="fas fa-list"></i> Danh sách danh mục</div>
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-hover mb-0" id="categoriesTable">
              <thead>
                <tr>
                  <th style="width:120px"><a href="?page=${currentPage}&size=${pageSize}&search=${param.search}&sortBy=categoryId&sortDir=${sortBy=='categoryId' && sortDir=='asc' ? 'desc' : 'asc'}">ID</a></th>
                  <th style="width:120px">Ảnh</th>
                  <th>Tên danh mục</th>
                  <th style="width:160px">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="category" items="${categories}">
                  <tr>
                    <td><span class="badge badge-primary">${category.categoryId}</span></td>
                    <td>
                      <c:choose>
                        <c:when test="${not empty category.categoryImage}">
                          <img src="/loadImage?imageName=${category.categoryImage}" class="category-image" alt="Category Image" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';" />
                          <div class="no-image" style="display:none"><i class="fas fa-image"></i></div>
                        </c:when>
                        <c:otherwise>
                          <div class="no-image"><i class="fas fa-image"></i></div>
                        </c:otherwise>
                      </c:choose>
                    </td>
                    <td><strong>${category.categoryName}</strong></td>
                    <td>
                      <div class="d-flex" style="gap:8px">
                        <button type="button" class="btn btn-sm btn-info" data-id="${category.categoryId}" data-name="${fn:escapeXml(category.categoryName)}" onclick="openEditModal(this.dataset.id, this.dataset.name)"><i class="fas fa-edit"></i></button>
                        <button type="button" class="btn btn-sm btn-danger" data-id="${category.categoryId}" data-name="${fn:escapeXml(category.categoryName)}" onclick="confirmDelete(this.dataset.id, this.dataset.name)"><i class="fas fa-trash"></i></button>
                      </div>
                    </td>
                  </tr>
                </c:forEach>
                <c:if test="${empty categories}">
                  <tr>
                    <td colspan="4" class="text-center p-4">
                      <i class="fas fa-inbox"></i>
                      <h5 class="mt-2 mb-0">Chưa có danh mục nào</h5>
                      <small>Hãy thêm danh mục đầu tiên để bắt đầu quản lý</small>
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

<!-- Add Category Modal -->
<div class="modal fade" id="addCategoryModal" tabindex="-1" role="dialog">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title"><i class="fas fa-plus-circle"></i> Thêm danh mục mới</h5>
        <button type="button" class="close" data-dismiss="modal">
          <span>&times;</span>
        </button>
      </div>
      <form id="addCategoryForm" method="post" action="/admin/categories/add" enctype="multipart/form-data">
        <div class="modal-body">
          <input type="hidden" name="page" value="${currentPage}" />
          <input type="hidden" name="size" value="${pageSize}" />
          <input type="hidden" name="sortBy" value="${sortBy}" />
          <input type="hidden" name="sortDir" value="${sortDir}" />
          <input type="hidden" name="search" value="${search}" />
          
          <div class="form-group">
            <label for="categoryName">Tên danh mục <span class="text-danger">*</span></label>
            <input type="text" class="form-control" id="categoryName" name="categoryName" required />
          </div>
          
          <div class="form-group">
            <label for="categoryImage">Ảnh danh mục</label>
            <input type="file" class="form-control-file" id="categoryImage" name="categoryImageFile" accept="image/*" />
            <small class="form-text text-muted">Chọn ảnh đại diện cho danh mục</small>
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

<!-- Edit Category Modal -->
<div class="modal fade" id="editCategoryModal" tabindex="-1" role="dialog">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title"><i class="fas fa-edit"></i> Chỉnh sửa danh mục</h5>
        <button type="button" class="close" data-dismiss="modal">
          <span>&times;</span>
        </button>
      </div>
      <form id="editCategoryForm" method="post" enctype="multipart/form-data">
        <div class="modal-body">
          <input type="hidden" name="page" value="${currentPage}" />
          <input type="hidden" name="size" value="${pageSize}" />
          <input type="hidden" name="sortBy" value="${sortBy}" />
          <input type="hidden" name="sortDir" value="${sortDir}" />
          <input type="hidden" name="search" value="${search}" />
          
          <div class="form-group">
            <label for="editCategoryName">Tên danh mục <span class="text-danger">*</span></label>
            <input type="text" class="form-control" id="editCategoryName" name="categoryName" required />
          </div>
          
          <div class="form-group">
            <label for="editCategoryImage">Ảnh danh mục</label>
            <input type="file" class="form-control-file" id="editCategoryImage" name="categoryImageFile" accept="image/*" />
            <small class="form-text text-muted">Chọn ảnh mới để thay thế ảnh hiện tại</small>
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
function openEditModal(id, name) {
  $('#editCategoryForm').attr('action', '/admin/categories/edit/' + id);
  $('#editCategoryName').val(name);
  $('#editCategoryModal').modal('show');
}

function confirmDelete(id, name) {
  Swal.fire({
    title: 'Xác nhận xóa',
    html: `Bạn có chắc chắn muốn xóa danh mục <strong>"${name}"</strong>?<br/><small class="text-danger">Tất cả sản phẩm trong danh mục này cũng sẽ bị xóa!</small>`,
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
      form.action = '/admin/categories/delete/' + id;

      const urlParams = new URLSearchParams(window.location.search);
      const addHidden = (name, value) => {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = name;
        input.value = value;
        form.appendChild(input);
      };

      if (urlParams.has('page')) addHidden('page', urlParams.get('page'));
      if (urlParams.has('size')) addHidden('size', urlParams.get('size'));
      if (urlParams.has('sortBy')) addHidden('sortBy', urlParams.get('sortBy'));
      if (urlParams.has('sortDir')) addHidden('sortDir', urlParams.get('sortDir'));
      if (urlParams.has('search')) addHidden('search', urlParams.get('search'));

      document.body.appendChild(form);
      form.submit();
    }
  });
}

// Success notification
const urlParams = new URLSearchParams(window.location.search);
if (urlParams.get('success') === 'true') {
  const action = urlParams.get('action');
  let message = '';
  let icon = 'success';

  if (action === 'add') {
    message = 'Thêm danh mục thành công!';
    icon = 'success';
  } else if (action === 'edit') {
    message = 'Cập nhật danh mục thành công!';
    icon = 'success';
  } else if (action === 'delete') {
    message = 'Xóa danh mục thành công!';
    icon = 'success';
  }

  if (message) {
    Swal.fire({
      icon: icon,
      title: message,
      showConfirmButton: false,
      timer: 2000
    });
  }

  // Clean only success-related flags, keep paging/sort/search
  urlParams.delete('success');
  urlParams.delete('action');
  const remaining = urlParams.toString();
  const cleanUrl = window.location.pathname + (remaining ? ('?' + remaining) : '');
  window.history.replaceState({}, document.title, cleanUrl);
}
</script>