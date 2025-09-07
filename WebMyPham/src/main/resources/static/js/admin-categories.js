// OneShop Admin - Categories Page JavaScript

$(document).ready(function () {
  // Initialize DataTable with custom styling
  $("#add-row").DataTable({
    pageLength: 10,
    lengthMenu: [
      [5, 10, 25, 50],
      [5, 10, 25, 50],
    ],
    language: {
      lengthMenu: "Hiển thị _MENU_ mục",
      zeroRecords: "Không tìm thấy dữ liệu",
      info: "Hiển thị _START_ đến _END_ của _TOTAL_ mục",
      infoEmpty: "Hiển thị 0 đến 0 của 0 mục",
      infoFiltered: "(lọc từ _MAX_ tổng số mục)",
      search: "Tìm kiếm:",
      paginate: {
        first: "Đầu",
        last: "Cuối",
        next: "Sau",
        previous: "Trước",
      },
    },
    dom: '<"row"<"col-sm-6"l><"col-sm-6"f>>t<"row"<"col-sm-6"i><"col-sm-6"p>>',
    columnDefs: [
      { orderable: false, targets: -1 }, // Disable sorting on action column
    ],
  });

  // Image preview for file upload
  $('input[name="imageFile"]').change(function (e) {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = function (e) {
        // Remove existing preview
        $(".image-preview").remove();

        // Add new preview
        const preview = `
                    <div class="image-preview mt-2">
                        <img src="${e.target.result}" alt="Preview" style="max-width: 100px; max-height: 100px; border-radius: 8px;">
                        <small class="d-block text-muted">Xem trước ảnh</small>
                    </div>
                `;
        $(this).parent().append(preview);
      }.bind(this);
      reader.readAsDataURL(file);
    }
  });

  // Form validation
  $("form").on("submit", function (e) {
    const categoryName = $('input[name="categoryName"]').val().trim();

    if (categoryName === "") {
      e.preventDefault();
      showAlert("Vui lòng nhập tên thể loại!", "warning");
      return false;
    }

    if (categoryName.length < 2) {
      e.preventDefault();
      showAlert("Tên thể loại phải có ít nhất 2 ký tự!", "warning");
      return false;
    }

    // Show loading
    const submitBtn = $(this).find('button[type="submit"]');
    const originalText = submitBtn.text();
    submitBtn.html('<span class="loading"></span> Đang xử lý...');
    submitBtn.prop("disabled", true);

    // Reset after 5 seconds if form doesn't redirect
    setTimeout(() => {
      submitBtn.html(originalText);
      submitBtn.prop("disabled", false);
    }, 5000);
  });

  // Confirm delete with better styling
  window.showConfigModalDialog = function (id, name) {
    $("#configmationId .modal-body p").html(
      `Bạn có chắc chắn muốn xóa thể loại <strong>"${name}"</strong> không?`
    );
    $("#yesOption").attr("href", "/admin/delete/" + id);
    $("#configmationId").modal("show");
  };

  // Auto hide alerts after 5 seconds
  $(".alert").each(function () {
    const alert = $(this);
    setTimeout(() => {
      alert.fadeOut();
    }, 5000);
  });

  // Tooltip initialization
  $('[data-toggle="tooltip"]').tooltip();

  // Add loading state to action buttons
  $(".btn-link").on("click", function () {
    const btn = $(this);
    const icon = btn.find("i");
    const originalClass = icon.attr("class");

    icon.attr("class", "fas fa-spinner fa-spin");

    setTimeout(() => {
      icon.attr("class", originalClass);
    }, 1000);
  });
});

// Utility functions
function showAlert(message, type = "info") {
  const alertClass = `alert-${type}`;
  const alertHtml = `
        <div class="alert ${alertClass} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="close" data-dismiss="alert">
                <span aria-hidden="true">&times;</span>
            </button>
        </div>
    `;

  $(".card-body").prepend(alertHtml);

  // Auto hide after 5 seconds
  setTimeout(() => {
    $(".alert").first().fadeOut();
  }, 5000);
}

// Format file size
function formatFileSize(bytes) {
  if (bytes === 0) return "0 Bytes";
  const k = 1024;
  const sizes = ["Bytes", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
}

// Image lazy loading
function lazyLoadImages() {
  const images = document.querySelectorAll("img[data-src]");
  const imageObserver = new IntersectionObserver((entries, observer) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        const img = entry.target;
        img.src = img.dataset.src;
        img.classList.remove("lazy");
        imageObserver.unobserve(img);
      }
    });
  });

  images.forEach((img) => imageObserver.observe(img));
}
