// Flash Sale integration for product listing pages (shop.html, index.html)
(function() {
  'use strict';
  
  // Format price helper
  function formatPrice(price) {
    if (price === null || price === undefined || isNaN(price)) {
      return '0 đ';
    }
    try {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price);
    } catch (error) {
      return price + ' đ';
    }
  }
  
  // Update product price with flash sale info
  function updateProductPrice(productId, priceElement, badgeContainer) {
    if (!priceElement || !productId) {
      return;
    }
    
    // Check if FlashSaleAPI is available
    if (typeof FlashSaleAPI === 'undefined') {
      console.warn('FlashSaleAPI not available for product', productId);
      return;
    }
    
    FlashSaleAPI.getProductFlashSalePrice(productId).then(function(data) {
      if (data && data.inFlashSale) {
        // Update price display
        const originalPrice = data.originalPrice;
        const flashSalePrice = data.flashSalePrice;
        const discountPercentage = data.discountPercentage;
        
        priceElement.innerHTML = `
          <del style="color: #999; font-size: 0.9rem;">${formatPrice(originalPrice)}</del>
          <span style="color: #ff6b6b; font-weight: bold; margin-left: 5px;">${formatPrice(flashSalePrice)}</span>
        `;
        
        // Add flash sale badge if badge container exists
        if (badgeContainer) {
          // Check if flash sale badge already exists
          let flashBadge = badgeContainer.querySelector('.label-text.flash');
          if (!flashBadge) {
            flashBadge = document.createElement('label');
            flashBadge.className = 'label-text flash';
            flashBadge.style.cssText = 'background: linear-gradient(135deg, #ff6b6b 0%, #ff4757 100%); color: white; font-weight: bold;';
            flashBadge.textContent = 'FLASH SALE';
            badgeContainer.insertBefore(flashBadge, badgeContainer.firstChild);
          }
          
          // Update discount badge
          const discountBadge = badgeContainer.querySelector('.label-text.sale');
          if (discountBadge) {
            discountBadge.textContent = '-' + discountPercentage + '%';
            discountBadge.style.cssText = 'background: #ff6b6b; color: white;';
          }
        }
        
        // Add flash sale class to product card for styling
        const productCard = priceElement.closest('.product-card');
        if (productCard) {
          productCard.classList.add('flash-sale-product');
          productCard.setAttribute('data-flash-sale', 'true');
        }
      }
    }).catch(function(error) {
      console.error('Error fetching flash sale price for product', productId, ':', error);
    });
  }
  
  // Initialize flash sale prices for all products on page
  function initProductListFlashSale() {
    console.log('Flash Sale Product List: Initializing...');
    
    // Wait for DOM to be ready
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', function() {
        setTimeout(processProductList, 500);
      });
    } else {
      setTimeout(processProductList, 500);
    }
  }
  
  function processProductList() {
    // Find all product cards
    const productCards = document.querySelectorAll('.product-card');
    console.log('Flash Sale Product List: Found', productCards.length, 'products');
    
    productCards.forEach(function(card) {
      // Try to get product ID from various sources
      let productId = null;
      
      // Method 1: From data attribute
      productId = card.getAttribute('data-product-id');
      
      // Method 2: From product link
      if (!productId) {
        const productLink = card.querySelector('a.product-image, a[href*="productDetail"]');
        if (productLink) {
          const href = productLink.getAttribute('href');
          const match = href.match(/[?&]id=(\d+)/);
          if (match) {
            productId = match[1];
          }
        }
      }
      
      // Method 3: From favorite button
      if (!productId) {
        const favoriteBtn = card.querySelector('button.product-wish, button.wish');
        if (favoriteBtn) {
          productId = favoriteBtn.getAttribute('data-product-id') || 
                     favoriteBtn.getAttribute('onclick')?.match(/\d+/)?.[0];
        }
      }
      
      if (!productId) {
        console.warn('Flash Sale Product List: Could not find product ID for card', card);
        return;
      }
      
      // Set data attribute for future reference
      card.setAttribute('data-product-id', productId);
      
      // Find price element
      const priceElement = card.querySelector('.product-price, h6.product-price');
      if (!priceElement) {
        console.warn('Flash Sale Product List: Price element not found for product', productId);
        return;
      }
      
      // Find badge container
      const badgeContainer = card.querySelector('.product-label');
      
      // Update price with flash sale info
      updateProductPrice(productId, priceElement, badgeContainer);
    });
  }
  
  // Initialize when script loads
  initProductListFlashSale();
  
  // Re-initialize when new products are loaded (for pagination/AJAX)
  const observer = new MutationObserver(function(mutations) {
    let shouldReinit = false;
    mutations.forEach(function(mutation) {
      if (mutation.addedNodes.length > 0) {
        mutation.addedNodes.forEach(function(node) {
          if (node.nodeType === 1 && (node.classList.contains('product-card') || node.querySelector('.product-card'))) {
            shouldReinit = true;
          }
        });
      }
    });
    if (shouldReinit) {
      setTimeout(processProductList, 300);
    }
  });
  
  // Start observing
  if (document.body) {
    observer.observe(document.body, {
      childList: true,
      subtree: true
    });
  }
})();

