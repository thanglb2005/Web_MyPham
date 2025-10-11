// Favorite functionality
function toggleFavorite(productId, button) {
    $.ajax({
        url: '/toggleFavorite',
        type: 'POST',
        data: { productId: productId },
        success: function(response) {
            if (response.success) {
                // Toggle heart icon
                const heartIcon = $(button).find('i');
                if (response.isFavorited) {
                    heartIcon.removeClass('far').addClass('fas');
                    $(button).addClass('active');
                    showToast('Đã thêm vào yêu thích', 'success');
                } else {
                    heartIcon.removeClass('fas').addClass('far');
                    $(button).removeClass('active');
                    showToast('Đã xóa khỏi yêu thích', 'info');
                }
            } else {
                showToast(response.message, 'error');
            }
        },
        error: function() {
            showToast('Có lỗi xảy ra', 'error');
        }
    });
}

// Toast notification
function showToast(message, type) {
    const toastClass = type === 'success' ? 'alert-success' : 
                      type === 'error' ? 'alert-danger' : 'alert-info';
    const toast = $(`<div class="alert ${toastClass} alert-dismissible fade show position-fixed" 
                     style="top: 20px; right: 20px; z-index: 9999;">
                     ${message}
                     <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                     </div>`);
    $('body').append(toast);
    setTimeout(() => toast.remove(), 3000);
}

// Load favorite status on page load
$(document).ready(function() {
    // Check favorite status for all favorite buttons
    $('.product-wish, .details-wish').each(function() {
        const productId = $(this).data('product-id');
        if (productId) {
            $.ajax({
                url: '/checkFavorite',
                type: 'GET',
                data: { productId: productId },
                success: function(response) {
                    const heartIcon = $(this).find('i');
                    if (response.isFavorited) {
                        heartIcon.removeClass('far').addClass('fas');
                        $(this).addClass('active');
                    } else {
                        heartIcon.removeClass('fas').addClass('far');
                        $(this).removeClass('active');
                    }
                }.bind(this)
            });
        }
    });
});

