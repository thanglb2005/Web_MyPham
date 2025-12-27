// Flash Sale section loader for index page
(function() {
  'use strict';
  
  function initFlashSale() {
    try {
      console.log('Flash Sale: DOM loaded, initializing...');
      
      const container = document.getElementById('flash-sale-container');
      if (!container) {
        console.warn('Flash Sale: Container not found');
        return;
      }
      
      // Check if FlashSaleAPI is available
      if (typeof FlashSaleAPI === 'undefined') {
        console.error('Flash Sale: FlashSaleAPI is not defined. Make sure flash-sale-api.js is loaded.');
        container.innerHTML = `
          <div class="text-center" style="padding: 40px; color: #dc3545;">
            <i class="fas fa-exclamation-triangle" style="font-size: 2rem; margin-bottom: 15px;"></i>
            <p>Lỗi: Không thể tải Flash Sale API</p>
          </div>
        `;
        return;
      }
      
      loadFlashSales(container);
    } catch (error) {
      console.error('Flash Sale: Error in initFlashSale:', error);
      console.error('Flash Sale: Error stack:', error.stack);
    }
  }
  
  function loadFlashSales(container) {
    try {
  
      console.log('Flash Sale: Fetching active flash sales from /flash-sale/api/active...');
      FlashSaleAPI.getActiveFlashSales().then(function(sales) {
    console.log('Flash Sale: Received sales:', sales);
    console.log('Flash Sale: Sales type:', typeof sales);
    console.log('Flash Sale: Is array?', Array.isArray(sales));
    console.log('Flash Sale: Sales count:', sales && Array.isArray(sales) ? sales.length : 0);
    
    // Validate sales is an array
    if (!sales) {
      console.warn('Flash Sale: Sales is null or undefined');
      const section = document.getElementById('flash-sale-section');
      if (section) {
        section.style.display = 'none';
      }
      return;
    }
    
    if (!Array.isArray(sales)) {
      console.error('Flash Sale: Sales is not an array:', sales);
      container.innerHTML = `
        <div class="text-center" style="padding: 40px; color: #dc3545;">
          <i class="fas fa-exclamation-triangle" style="font-size: 2rem; margin-bottom: 15px;"></i>
          <p>Lỗi: Dữ liệu Flash Sale không hợp lệ</p>
        </div>
      `;
      return;
    }
    
    if (sales.length === 0) {
      console.log('Flash Sale: No active sales found - hiding section');
      // Hide the entire section instead of showing message
      const section = document.getElementById('flash-sale-section');
      if (section) {
        section.style.display = 'none';
      } else {
        container.innerHTML = `
          <div class="text-center" style="padding: 40px;">
            <i class="fas fa-fire" style="font-size: 3rem; color: #ddd; margin-bottom: 15px;"></i>
            <p style="color: #999;">Hiện tại chưa có Flash Sale nào</p>
            <a href="/flash-sale" class="btn btn-outline" style="margin-top: 15px;">
              Xem tất cả Flash Sale
            </a>
          </div>
        `;
      }
      return;
    }
    
    // Show first active flash sale - with safety check
    const firstSale = sales[0];
    if (!firstSale) {
      console.error('Flash Sale: First sale is null or undefined');
      container.innerHTML = `
        <div class="text-center" style="padding: 40px; color: #dc3545;">
          <i class="fas fa-exclamation-triangle" style="font-size: 2rem; margin-bottom: 15px;"></i>
          <p>Lỗi: Không thể tải thông tin Flash Sale</p>
        </div>
      `;
      return;
    }
    
    if (!firstSale.flashSaleId) {
      console.error('Flash Sale: First sale missing flashSaleId:', firstSale);
      container.innerHTML = `
        <div class="text-center" style="padding: 40px; color: #dc3545;">
          <i class="fas fa-exclamation-triangle" style="font-size: 2rem; margin-bottom: 15px;"></i>
          <p>Lỗi: Flash Sale ID không hợp lệ</p>
        </div>
      `;
      return;
    }
    
    console.log('Flash Sale: Loading products for sale ID:', firstSale.flashSaleId);
    console.log('Flash Sale: Sale details:', {
      id: firstSale.flashSaleId,
      name: firstSale.saleName,
      startTime: firstSale.startTime,
      endTime: firstSale.endTime,
      status: firstSale.status
    });
    
    FlashSaleAPI.getFlashSaleProducts(firstSale.flashSaleId).then(function(data) {
      console.log('Flash Sale: Received products data:', data);
      console.log('Flash Sale: Success:', data.success);
      console.log('Flash Sale: Products count:', data.products ? data.products.length : 0);
      
      if (data.success && data.products && data.products.length > 0) {
        console.log('Flash Sale: Rendering', data.products.length, 'products');
        renderFlashSalePreview(container, firstSale, data.products.slice(0, 8));
      } else {
        console.log('Flash Sale: No products available or error:', data.message || 'Unknown error');
        container.innerHTML = `
          <div class="text-center" style="padding: 40px; color: #999;">
            <i class="fas fa-info-circle" style="font-size: 2rem; margin-bottom: 15px;"></i>
            <p>Chưa có sản phẩm trong Flash Sale này</p>
            <a href="/flash-sale" class="btn btn-outline" style="margin-top: 15px;">
              Xem tất cả Flash Sale
            </a>
          </div>
        `;
      }
    }).catch(function(error) {
      console.error('Flash Sale: Error loading products:', error);
      container.innerHTML = `
        <div class="text-center" style="padding: 40px; color: #dc3545;">
          <i class="fas fa-exclamation-triangle" style="font-size: 2rem; margin-bottom: 15px;"></i>
          <p>Lỗi khi tải sản phẩm Flash Sale</p>
          <p style="font-size: 0.9rem; margin-top: 10px;">${error.message || 'Unknown error'}</p>
        </div>
      `;
    });
  }).catch(function(error) {
    console.error('Flash Sale: Error fetching active sales:', error);
    console.error('Flash Sale: Error details:', error.stack);
    container.innerHTML = `
      <div class="text-center" style="padding: 40px; color: #dc3545;">
        <i class="fas fa-exclamation-triangle" style="font-size: 2rem; margin-bottom: 15px;"></i>
        <p>Lỗi khi tải Flash Sale</p>
        <p style="font-size: 0.9rem; margin-top: 10px;">${error.message || 'Unknown error'}</p>
        <p style="font-size: 0.8rem; margin-top: 5px; color: #666;">Vui lòng kiểm tra console để xem chi tiết</p>
      </div>
    `;
      }).catch(function(error) {
        console.error('Flash Sale: Error in loadFlashSales promise:', error);
        console.error('Flash Sale: Error stack:', error.stack);
      });
    } catch (error) {
      console.error('Flash Sale: Error in loadFlashSales function:', error);
      console.error('Flash Sale: Error stack:', error.stack);
      container.innerHTML = `
        <div class="text-center" style="padding: 40px; color: #dc3545;">
          <i class="fas fa-exclamation-triangle" style="font-size: 2rem; margin-bottom: 15px;"></i>
          <p>Lỗi khi khởi tạo Flash Sale</p>
          <p style="font-size: 0.9rem; margin-top: 10px;">${error.message || 'Unknown error'}</p>
        </div>
      `;
    }
  }
  
  // Wait for DOM to be ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initFlashSale);
  } else {
    // DOM is already ready
    setTimeout(initFlashSale, 100); // Small delay to ensure all scripts are loaded
  }
})();

function renderFlashSalePreview(container, sale, products) {
  console.log('Flash Sale: Rendering preview for sale:', sale);
  
  if (!sale) {
    console.error('Flash Sale: Sale object is null or undefined');
    return;
  }
  
  if (!sale.saleName) {
    console.error('Flash Sale: Sale name is missing');
    return;
  }
  
  if (!sale.endTime) {
    console.error('Flash Sale: Sale endTime is missing');
    return;
  }
  
  if (!Array.isArray(products)) {
    console.error('Flash Sale: Products is not an array:', products);
    return;
  }
  
  const endTimeSeconds = FlashSaleTimer.dateToSeconds(sale.endTime);
  const countdownId = 'flash-sale-preview-countdown';
  
  console.log('Flash Sale: End time seconds:', endTimeSeconds);
  
  let html = `
    <div class="flash-sale-preview">
      <div class="flash-sale-preview-header">
        <div>
          <h3 style="color: #ff6b6b; margin-bottom: 5px;">
            <i class="fas fa-fire"></i> ${sale.saleName}
          </h3>
          <p style="color: #666; margin: 0;">${sale.description || ''}</p>
        </div>
        <div class="flash-sale-preview-countdown">
          <div class="countdown" id="${countdownId}"></div>
        </div>
      </div>
      <div class="row">
  `;
  
  products.forEach(function(product) {
    if (!product) {
      console.warn('Flash Sale: Skipping null/undefined product');
      return;
    }
    
    if (!product.isAvailable) {
      console.log('Flash Sale: Skipping unavailable product:', product.productId);
      return;
    }
    
    // Validate required fields
    if (!product.productId || !product.productName) {
      console.warn('Flash Sale: Skipping product with missing required fields:', product);
      return;
    }
    
    const discountPercentage = product.discountPercentage || 0;
    const soldQuantity = product.soldQuantity || 0;
    const quantityLimit = product.quantityLimit || 1;
    const remainingQuantity = product.remainingQuantity || 0;
    const progressPercent = quantityLimit > 0 ? (soldQuantity / quantityLimit) * 100 : 0;
    
    html += `
      <div class="col-lg-3 col-md-4 col-sm-6 mb-3">
        <div class="product-card flash-sale-product" data-product-id="${product.productId}">
          <div class="product-media">
            <div class="product-label">
              <label class="label-text flash">FLASH SALE</label>
              <label class="label-text sale">-${discountPercentage}%</label>
            </div>
            <a class="product-image" href="/productDetail?id=${product.productId}">
              <img src="${product.productImage || '/assets/img/examples/product1.jpg'}" alt="${product.productName || 'Product'}" />
            </a>
          </div>
          <div class="product-content">
            <h6 class="product-name">
              <a href="/productDetail?id=${product.productId}">${product.productName || 'Sản phẩm'}</a>
            </h6>
            <div class="product-price">
              <span class="flash-price">${formatPrice(product.flashSalePrice)}</span>
              <del class="original-price">${formatPrice(product.originalPrice)}</del>
            </div>
            <div class="flash-sale-progress">
              <div class="progress-bar">
                <div class="progress-fill" style="width: ${progressPercent}%"></div>
              </div>
              <small style="color: #666;">Còn lại: ${remainingQuantity}</small>
            </div>
          </div>
        </div>
      </div>
    `;
  });
  
  html += `
      </div>
      <div class="text-center mt-4">
        <a href="/flash-sale" class="btn btn-inline">
          <i class="fas fa-fire"></i> Xem tất cả Flash Sale
        </a>
      </div>
    </div>
  `;
  
  container.innerHTML = html;
  
  // Initialize countdown only if FlashSaleTimer is available
  if (typeof FlashSaleTimer !== 'undefined' && FlashSaleTimer.initCountdown) {
    FlashSaleTimer.initCountdown(countdownId, endTimeSeconds);
  } else {
    console.warn('Flash Sale: FlashSaleTimer is not available');
  }
}

function formatPrice(price) {
  if (price === null || price === undefined || isNaN(price)) {
    console.warn('formatPrice: Invalid price value:', price);
    return '0 đ';
  }
  
  try {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price);
  } catch (error) {
    console.error('formatPrice: Error formatting price:', error, price);
    return price + ' đ';
  }
}

