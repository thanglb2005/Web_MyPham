// Flash Sale Notification Bar (like Shopee)
(function() {
  'use strict';
  
  function createNotificationBar(flashSale) {
    // Remove existing notification bar if any
    const existing = document.getElementById('flash-sale-notification-bar');
    if (existing) {
      existing.remove();
    }
    
    // Create notification bar
    const notificationBar = document.createElement('div');
    notificationBar.id = 'flash-sale-notification-bar';
    notificationBar.className = 'flash-sale-notification-bar';
    notificationBar.style.cssText = `
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      background: linear-gradient(135deg, #ff6b6b 0%, #ff4757 50%, #ff6b6b 100%);
      background-size: 200% 200%;
      animation: gradientShift 3s ease infinite;
      color: white;
      padding: 18px 20px;
      z-index: 9999;
      box-shadow: 0 4px 20px rgba(255, 107, 107, 0.4);
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 20px;
      font-size: 18px;
      font-weight: 700;
      animation: slideDown 0.3s ease-out, gradientShift 3s ease infinite;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    `;
    
    // Add animation
    const style = document.createElement('style');
    style.textContent = `
      @keyframes slideDown {
        from {
          transform: translateY(-100%);
          opacity: 0;
        }
        to {
          transform: translateY(0);
          opacity: 1;
        }
      }
      @keyframes gradientShift {
        0% {
          background-position: 0% 50%;
        }
        50% {
          background-position: 100% 50%;
        }
        100% {
          background-position: 0% 50%;
        }
      }
      @keyframes pulse {
        0%, 100% {
          transform: scale(1);
        }
        50% {
          transform: scale(1.1);
        }
      }
      .flash-sale-notification-bar .countdown {
        display: inline-flex;
        gap: 8px;
        align-items: center;
        background: rgba(255, 255, 255, 0.25);
        padding: 8px 16px;
        border-radius: 8px;
        font-weight: bold;
        backdrop-filter: blur(10px);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
      }
      .flash-sale-notification-bar .countdown-item {
        background: rgba(255, 255, 255, 0.35);
        padding: 6px 10px;
        border-radius: 6px;
        min-width: 40px;
        text-align: center;
        font-size: 16px;
        font-weight: 800;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
      }
      .flash-sale-notification-bar .countdown-value {
        font-size: 20px;
        font-weight: 900;
        color: white;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
      }
      .flash-sale-notification-bar .countdown-label {
        font-size: 11px;
        font-weight: 600;
        opacity: 0.95;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }
      body.has-flash-sale-notification {
        padding-top: 60px;
      }
    `;
    document.head.appendChild(style);
    
    // Content
    const icon = document.createElement('i');
    icon.className = 'fas fa-fire';
    icon.style.cssText = 'font-size: 28px; animation: pulse 1.5s ease-in-out infinite; color: #ffd700; text-shadow: 0 0 10px rgba(255, 215, 0, 0.8);';
    
    const text = document.createElement('span');
    text.textContent = flashSale.saleName || 'Flash Sale đang diễn ra!';
    text.style.cssText = 'font-size: 20px; font-weight: 800; letter-spacing: 0.5px; text-transform: uppercase;';
    
    const countdownContainer = document.createElement('div');
    countdownContainer.className = 'countdown';
    countdownContainer.id = 'flash-sale-notification-countdown';
    
    const link = document.createElement('a');
    link.href = '/flash-sale';
    link.textContent = 'XEM NGAY →';
    link.style.cssText = `
      color: white;
      text-decoration: none;
      font-weight: 800;
      font-size: 18px;
      margin-left: 15px;
      padding: 8px 20px;
      background: rgba(255, 255, 255, 0.2);
      border: 2px solid rgba(255, 255, 255, 0.5);
      border-radius: 25px;
      transition: all 0.3s ease;
      text-transform: uppercase;
      letter-spacing: 1px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    `;
    link.onmouseover = function() { 
      this.style.background = 'rgba(255, 255, 255, 0.35)';
      this.style.transform = 'scale(1.05)';
      this.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.3)';
    };
    link.onmouseout = function() { 
      this.style.background = 'rgba(255, 255, 255, 0.2)';
      this.style.transform = 'scale(1)';
      this.style.boxShadow = '0 2px 8px rgba(0, 0, 0, 0.2)';
    };
    
    // Close button
    const closeBtn = document.createElement('button');
    closeBtn.innerHTML = '&times;';
    closeBtn.style.cssText = `
      position: absolute;
      right: 15px;
      background: transparent;
      border: none;
      color: white;
      font-size: 24px;
      cursor: pointer;
      padding: 0;
      width: 30px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      transition: background 0.2s;
    `;
    closeBtn.onmouseover = function() { this.style.background = 'rgba(255, 255, 255, 0.2)'; };
    closeBtn.onmouseout = function() { this.style.background = 'transparent'; };
    closeBtn.onclick = function(e) {
      e.preventDefault();
      notificationBar.style.animation = 'slideDown 0.3s ease-out reverse';
      setTimeout(function() {
        notificationBar.remove();
        document.body.classList.remove('has-flash-sale-notification');
        // Save to localStorage to not show again in this session
        sessionStorage.setItem('flash-sale-notification-closed', 'true');
      }, 300);
    };
    
    notificationBar.appendChild(icon);
    notificationBar.appendChild(text);
    notificationBar.appendChild(countdownContainer);
    notificationBar.appendChild(link);
    notificationBar.appendChild(closeBtn);
    
    // Insert at the beginning of body
    document.body.insertBefore(notificationBar, document.body.firstChild);
    
    // Add padding to body to prevent content from being hidden
    document.body.classList.add('has-flash-sale-notification');
    
    // Initialize countdown if FlashSaleTimer is available
    if (typeof FlashSaleTimer !== 'undefined') {
      let endTimeSeconds = 0;
      
      // Method 1: Use endTime directly (preferred - more accurate)
      if (flashSale.endTime) {
        endTimeSeconds = FlashSaleTimer.dateToSeconds(flashSale.endTime);
        console.log('Flash Sale Notification: Using endTime:', flashSale.endTime, '->', endTimeSeconds);
      }
      // Method 2: Calculate from remainingSeconds if endTime not available
      else if (flashSale.remainingSeconds && flashSale.remainingSeconds > 0) {
        const now = Math.floor(Date.now() / 1000);
        endTimeSeconds = now + flashSale.remainingSeconds;
        console.log('Flash Sale Notification: Using remainingSeconds:', flashSale.remainingSeconds, '-> endTime:', endTimeSeconds);
      }
      
      if (endTimeSeconds > 0) {
        // Verify the end time is in the future
        const now = Math.floor(Date.now() / 1000);
        if (endTimeSeconds > now) {
          FlashSaleTimer.initCountdown('flash-sale-notification-countdown', endTimeSeconds);
        } else {
          console.warn('Flash Sale Notification: End time is in the past, flash sale may have ended');
          countdownContainer.innerHTML = '<span>Đã kết thúc</span>';
        }
      } else {
        console.warn('Flash Sale Notification: Could not determine end time');
        countdownContainer.innerHTML = '<span>Đang diễn ra</span>';
      }
    }
  }
  
  function initNotificationBar() {
    // Check if user closed it in this session
    if (sessionStorage.getItem('flash-sale-notification-closed') === 'true') {
      return;
    }
    
    // Check if FlashSaleAPI is available
    if (typeof FlashSaleAPI === 'undefined') {
      console.warn('FlashSaleAPI not available for notification bar');
      return;
    }
    
    // Wait for DOM
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', function() {
        setTimeout(checkAndShowNotification, 500);
      });
    } else {
      setTimeout(checkAndShowNotification, 500);
    }
  }
  
  function checkAndShowNotification() {
    FlashSaleAPI.getActiveFlashSales().then(function(sales) {
      console.log('Flash Sale Notification: Received sales:', sales);
      if (sales && Array.isArray(sales) && sales.length > 0) {
        // Show notification for first active flash sale
        const firstSale = sales[0];
        console.log('Flash Sale Notification: Using first sale:', firstSale);
        console.log('Flash Sale Notification: endTime:', firstSale.endTime, 'type:', typeof firstSale.endTime);
        console.log('Flash Sale Notification: remainingSeconds:', firstSale.remainingSeconds);
        createNotificationBar(firstSale);
      } else {
        console.log('Flash Sale Notification: No active flash sales found');
      }
    }).catch(function(error) {
      console.error('Error loading flash sale for notification:', error);
    });
  }
  
  // Initialize
  initNotificationBar();
})();

