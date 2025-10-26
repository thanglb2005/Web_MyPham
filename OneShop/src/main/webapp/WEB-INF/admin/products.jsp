<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<style>
  .product-image {
    width: 60px;
    height: 60px;
    object-fit: contain;
    background: #f8f9fa;
    border-radius: 6px;
  }
</style>

<div class="page-header">
  <h2 class="page-title">Sản phẩm</h2>
  
</div>

<div class="page-inner">
  <div class="d-flex align-items-center justify-content-between mb-3">
    <form method="get" class="d-flex align-items-center" style="gap:12px">
      <input type="hidden" name="page" value="0" />
      <input type="hidden" name="sortBy" value="${sortBy}" />
      <input type="hidden" name="sortDir" value="${sortDir}" />
      <div class="input-group" style="min-width:300px">
        <span class="input-group-text"><i class="fas fa-search"></i></span>
        <input class="form-control" type="text" name="search" value="${param.search}" placeholder="Tìm kiếm sản phẩm..." />
      </div>
      <select name="shopId" class="form-control form-control-sm" style="width:auto" onchange="this.form.submit()">
        <option value="">Tất cả shop</option>
        <c:forEach var="shop" items="${shopList}">
          <option value="${shop.shopId}" ${selectedShopId == shop.shopId ? 'selected' : ''}>${shop.shopName}</option>
        </c:forEach>
      </select>
      <select name="size" class="form-control form-control-sm" style="width:auto" onchange="this.form.submit()">
        <option value="5"  ${pageSize==5? 'selected': ''}>5 dòng</option>
        <option value="10" ${pageSize==10? 'selected': ''}>10 dòng</option>
        <option value="20" ${pageSize==20? 'selected': ''}>20 dòng</option>
        <option value="50" ${pageSize==50? 'selected': ''}>50 dòng</option>
      </select>
    </form>
    <button class="btn btn-primary" data-toggle="modal" data-target="#addProductModal"><i class="fas fa-plus"></i> Thêm sản phẩm</button>
  </div>

  <div class="card">
    <div class="card-header"><i class="fas fa-list"></i> Danh sách sản phẩm</div>
    <div class="card-body p-0">
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th style="width:80px"><a href="?page=${currentPage}&size=${pageSize}&search=${param.search}&shopId=${selectedShopId}&sortBy=productId&sortDir=${sortBy=='productId' && sortDir=='asc' ? 'desc' : 'asc'}">ID</a></th>
              <th style="width:100px">Ảnh</th>
              <th>Tên</th>
              <th style="width:140px">Giá</th>
              <th style="width:120px">Danh mục</th>
              <th style="width:120px">Thương hiệu</th>
              <th style="width:160px">Shop</th>
              <th style="width:160px">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="p" items="${products}">
              <tr>
                <td><span class="badge badge-primary">${p.productId}</span></td>
                <td>
                  <c:choose>
                    <c:when test="${not empty p.productImage}">
                      <img src="/loadImage?imageName=${p.productImage}" class="product-image" alt="Image" />
                    </c:when>
                    <c:otherwise>
                      <div class="product-image d-flex align-items-center justify-content-center"><i class="fas fa-image"></i></div>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td><strong>${p.productName}</strong></td>
                <td>${p.price} ₫</td>
                <td>${p.category != null ? p.category.categoryName : 'N/A'}</td>
                <td>${p.brand != null ? p.brand.brandName : 'N/A'}</td>
                <td>${p.shop != null ? p.shop.shopName : 'N/A'}</td>
                <td class="actions-inline" style="display:flex; gap:6px">
                  <a href="/admin/editProduct/${p.productId}" class="btn btn-sm btn-info" title="Sửa"><i class="fas fa-edit"></i></a>
                  <a href="/admin/deleteProduct/${p.productId}" class="btn btn-sm btn-danger" onclick="return confirm('Xóa sản phẩm này?')" title="Xóa"><i class="fas fa-trash"></i></a>
                  <button type="button" class="btn btn-sm btn-warning" data-id="${p.productId}" onclick="openAssignProduct(this.dataset.id)" title="Phân công shop"><i class="fas fa-store"></i></button>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty products}">
              <tr>
                <td colspan="7" class="text-center p-4">
                  <i class="fas fa-inbox"></i>
                  <h5 class="mt-2 mb-0">Không có sản phẩm</h5>
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
        <li class="page-item ${currentPage==0?'disabled':''}"><a class="page-link" href="?page=${currentPage-1}&size=${pageSize}&search=${param.search}&shopId=${selectedShopId}&sortBy=${sortBy}&sortDir=${sortDir}">Trước</a></li>
        <c:forEach var="i" begin="0" end="${totalPages-1}">
          <li class="page-item ${i==currentPage?'active':''}"><a class="page-link" href="?page=${i}&size=${pageSize}&search=${param.search}&shopId=${selectedShopId}&sortBy=${sortBy}&sortDir=${sortDir}">${i+1}</a></li>
        </c:forEach>
        <li class="page-item ${currentPage==totalPages-1?'disabled':''}"><a class="page-link" href="?page=${currentPage+1}&size=${pageSize}&search=${param.search}&shopId=${selectedShopId}&sortBy=${sortBy}&sortDir=${sortDir}">Sau</a></li>
      </ul>
    </nav>
  </c:if>
</div>

<!-- Add Product Modal -->
<div class="modal fade" id="addProductModal" tabindex="-1" role="dialog">
  <div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title"><i class="fas fa-plus"></i> Thêm sản phẩm</h5>
        <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
      </div>
      <form method="post" action="/admin/addProduct" enctype="multipart/form-data">
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group col-md-6">
              <label>Tên sản phẩm</label>
              <input type="text" class="form-control" name="productName" required />
            </div>
            <div class="form-group col-md-6">
              <label>Giá</label>
              <input type="number" step="0.01" class="form-control" name="price" required />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group col-md-6">
              <label>Số lượng</label>
              <input type="number" min="0" class="form-control" name="quantity" required />
            </div>
            <div class="form-group col-md-6">
              <label>Giảm giá (%)</label>
              <input type="number" min="0" max="100" class="form-control" name="discount" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group col-md-6">
              <label>Danh mục</label>
              <select name="categoryId" class="form-control" required>
                <c:forEach var="c" items="${categoryList}"><option value="${c.categoryId}">${c.categoryName}</option></c:forEach>
              </select>
            </div>
            <div class="form-group col-md-6">
              <label>Thương hiệu</label>
              <select name="brandId" class="form-control" required>
                <c:forEach var="b" items="${brandList}"><option value="${b.brandId}">${b.brandName}</option></c:forEach>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group col-md-6">
              <label>Shop</label>
              <select name="shopId" class="form-control" required>
                <c:forEach var="s" items="${shopList}"><option value="${s.shopId}">${s.shopName}</option></c:forEach>
              </select>
            </div>
            <div class="form-group col-md-6">
              <label>Ảnh</label>
              <input type="file" class="form-control-file" name="file" accept="image/*" />
            </div>
          </div>
          <div class="form-group">
            <label>Mô tả</label>
            <textarea class="form-control" name="description" rows="3"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">Hủy</button>
          <button type="submit" class="btn btn-primary">Lưu</button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- Assign Product Modal -->
<div class="modal fade" id="assignProductModal" tabindex="-1" role="dialog">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header"><h5 class="modal-title"><i class="fas fa-store"></i> Phân công sản phẩm cho Shop</h5><button type="button" class="close" data-dismiss="modal"><span>&times;</span></button></div>
      <form method="post" action="/admin/editProduct/assign">
        <div class="modal-body">
          <input type="hidden" name="productId" id="assignProductId" />
          <div class="form-group">
            <label>Chọn Shop</label>
            <select name="shopId" class="form-control" required>
              <c:forEach var="s" items="${shopList}"><option value="${s.shopId}">${s.shopName}</option></c:forEach>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">Hủy</button>
          <button type="submit" class="btn btn-primary">Phân công</button>
        </div>
      </form>
    </div>
  </div>
</div>

<script>
function openAssignProduct(id){
  document.getElementById('assignProductId').value = id;
  $('#assignProductModal').modal('show');
}
</script>


