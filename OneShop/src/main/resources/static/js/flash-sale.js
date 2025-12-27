/**
 * Flash Sale Countdown Timer and UI Functions
 */

const FlashSaleTimer = {
    timers: new Map(), // Store active timers

    /**
     * Initialize countdown timer for a flash sale
     * @param {string} elementId - ID of the countdown element
     * @param {number} endTimeSeconds - End time in seconds (timestamp)
     * @param {Function} onComplete - Callback when timer completes
     */
    initCountdown: function(elementId, endTimeSeconds, onComplete) {
        const element = document.getElementById(elementId);
        if (!element) {
            console.warn(`Countdown element ${elementId} not found`);
            return;
        }

        // Clear existing timer if any
        if (this.timers.has(elementId)) {
            clearInterval(this.timers.get(elementId));
        }

        const updateTimer = () => {
            const now = Math.floor(Date.now() / 1000);
            const remaining = endTimeSeconds - now;

            if (remaining <= 0) {
                element.innerHTML = '<span class="countdown-expired">Đã kết thúc</span>';
                if (this.timers.has(elementId)) {
                    clearInterval(this.timers.get(elementId));
                    this.timers.delete(elementId);
                }
                if (onComplete) {
                    onComplete();
                }
                return;
            }

            const days = Math.floor(remaining / 86400);
            const hours = Math.floor((remaining % 86400) / 3600);
            const minutes = Math.floor((remaining % 3600) / 60);
            const seconds = remaining % 60;

            let html = '';
            if (days > 0) {
                html += `<span class="countdown-item"><span class="countdown-value">${this.padZero(days)}</span><span class="countdown-label">Ngày</span></span>`;
            }
            html += `<span class="countdown-item"><span class="countdown-value">${this.padZero(hours)}</span><span class="countdown-label">Giờ</span></span>`;
            html += `<span class="countdown-item"><span class="countdown-value">${this.padZero(minutes)}</span><span class="countdown-label">Phút</span></span>`;
            html += `<span class="countdown-item"><span class="countdown-value">${this.padZero(seconds)}</span><span class="countdown-label">Giây</span></span>`;

            element.innerHTML = html;
        };

        // Update immediately
        updateTimer();

        // Update every second
        const intervalId = setInterval(updateTimer, 1000);
        this.timers.set(elementId, intervalId);
    },

    /**
     * Initialize countdown until start time
     * @param {string} elementId - ID of the countdown element
     * @param {number} startTimeSeconds - Start time in seconds (timestamp)
     * @param {Function} onStart - Callback when flash sale starts
     */
    initStartCountdown: function(elementId, startTimeSeconds, onStart) {
        const element = document.getElementById(elementId);
        if (!element) {
            console.warn(`Countdown element ${elementId} not found`);
            return;
        }

        // Clear existing timer if any
        if (this.timers.has(elementId)) {
            clearInterval(this.timers.get(elementId));
        }

        const updateTimer = () => {
            const now = Math.floor(Date.now() / 1000);
            const remaining = startTimeSeconds - now;

            if (remaining <= 0) {
                element.innerHTML = '<span class="countdown-starting">Sắp bắt đầu...</span>';
                if (this.timers.has(elementId)) {
                    clearInterval(this.timers.get(elementId));
                    this.timers.delete(elementId);
                }
                if (onStart) {
                    onStart();
                }
                return;
            }

            const days = Math.floor(remaining / 86400);
            const hours = Math.floor((remaining % 86400) / 3600);
            const minutes = Math.floor((remaining % 3600) / 60);
            const seconds = remaining % 60;

            let html = '<span class="countdown-label">Bắt đầu sau: </span>';
            if (days > 0) {
                html += `<span class="countdown-item"><span class="countdown-value">${this.padZero(days)}</span><span class="countdown-label">Ngày</span></span>`;
            }
            html += `<span class="countdown-item"><span class="countdown-value">${this.padZero(hours)}</span><span class="countdown-label">Giờ</span></span>`;
            html += `<span class="countdown-item"><span class="countdown-value">${this.padZero(minutes)}</span><span class="countdown-label">Phút</span></span>`;
            html += `<span class="countdown-item"><span class="countdown-value">${this.padZero(seconds)}</span><span class="countdown-label">Giây</span></span>`;

            element.innerHTML = html;
        };

        // Update immediately
        updateTimer();

        // Update every second
        const intervalId = setInterval(updateTimer, 1000);
        this.timers.set(elementId, intervalId);
    },

    /**
     * Pad number with leading zero
     * @param {number} num - Number to pad
     * @returns {string} Padded string
     */
    padZero: function(num) {
        return num.toString().padStart(2, '0');
    },

    /**
     * Convert ISO date string to seconds timestamp
     * @param {string} dateString - ISO date string
     * @returns {number} Seconds timestamp
     */
    dateToSeconds: function(dateString) {
        if (!dateString) {
            console.warn('FlashSaleTimer: dateString is empty');
            return 0;
        }
        
        try {
            // Handle different date formats
            let date;
            
            // If it's already a timestamp (number)
            if (typeof dateString === 'number') {
                date = new Date(dateString * 1000); // Convert seconds to milliseconds
            }
            // If it's an ISO string (e.g., "2025-01-15T10:30:00")
            else if (typeof dateString === 'string') {
                // Java LocalDateTime.toString() format: "2025-01-15T10:30:00" (no timezone)
                // Server is in Vietnam timezone (UTC+7), so we need to append timezone
                if (dateString.includes('T') && !dateString.includes('Z') && !dateString.includes('+') && !dateString.includes('-', dateString.indexOf('T'))) {
                    // LocalDateTime format without timezone - append Vietnam timezone (UTC+7)
                    // Format: "2025-01-15T10:30:00" -> "2025-01-15T10:30:00+07:00"
                    date = new Date(dateString + '+07:00');
                    console.log('FlashSaleTimer: Parsed LocalDateTime as Vietnam time:', dateString, '->', dateString + '+07:00');
                } else {
                    // ISO string with timezone or other format
                    date = new Date(dateString);
                }
            } else {
                console.error('FlashSaleTimer: Unsupported date format:', typeof dateString, dateString);
                return 0;
            }
            
            if (isNaN(date.getTime())) {
                console.error('FlashSaleTimer: Invalid date string:', dateString);
                return 0;
            }
            
            const seconds = Math.floor(date.getTime() / 1000);
            const now = Math.floor(Date.now() / 1000);
            const diff = seconds - now;
            const hours = Math.floor(Math.abs(diff) / 3600);
            const minutes = Math.floor((Math.abs(diff) % 3600) / 60);
            console.log('FlashSaleTimer: Converted', dateString, 'to', seconds, 'seconds');
            console.log('FlashSaleTimer: Current time:', now, '(', new Date().toLocaleString('vi-VN', {timeZone: 'Asia/Ho_Chi_Minh'}), ')');
            console.log('FlashSaleTimer: End time:', seconds, '(', new Date(date.getTime()).toLocaleString('vi-VN', {timeZone: 'Asia/Ho_Chi_Minh'}), ')');
            console.log('FlashSaleTimer: Time difference:', diff > 0 ? '+' : '', hours, 'hours', minutes, 'minutes');
            return seconds;
        } catch (error) {
            console.error('FlashSaleTimer: Error converting date:', error, dateString);
            return 0;
        }
    },

    /**
     * Clear all timers
     */
    clearAll: function() {
        this.timers.forEach((timerId) => clearInterval(timerId));
        this.timers.clear();
    }
};

/**
 * Flash Sale UI Helper Functions
 */
const FlashSaleUI = {
    /**
     * Show flash sale badge on product
     * @param {string} productId - Product ID
     * @param {Object} flashSaleInfo - Flash sale information
     */
    showFlashSaleBadge: function(productId, flashSaleInfo) {
        if (!flashSaleInfo || !flashSaleInfo.inFlashSale) {
            return;
        }

        // Find product element
        const productElement = document.querySelector(`[data-product-id="${productId}"]`);
        if (!productElement) {
            return;
        }

        // Create badge
        const badge = document.createElement('div');
        badge.className = 'flash-sale-badge';
        badge.innerHTML = `
            <span class="badge-text">FLASH SALE</span>
            <span class="badge-discount">-${flashSaleInfo.discountPercentage}%</span>
        `;

        // Add badge to product
        const productImage = productElement.querySelector('.product-image, img');
        if (productImage) {
            productImage.parentElement.style.position = 'relative';
            productImage.parentElement.appendChild(badge);
        }

        // Update price if exists
        const priceElement = productElement.querySelector('.product-price, .price');
        if (priceElement && flashSaleInfo.flashSalePrice) {
            const originalPrice = flashSaleInfo.originalPrice;
            const flashPrice = flashSaleInfo.flashSalePrice;
            
            priceElement.innerHTML = `
                <span class="flash-price">${this.formatPrice(flashPrice)}</span>
                <span class="original-price">${this.formatPrice(originalPrice)}</span>
            `;
        }

        // Show remaining quantity if exists
        if (flashSaleInfo.remainingQuantity !== undefined) {
            const quantityInfo = document.createElement('div');
            quantityInfo.className = 'flash-sale-quantity';
            quantityInfo.innerHTML = `Còn lại: ${flashSaleInfo.remainingQuantity} sản phẩm`;
            productElement.appendChild(quantityInfo);
        }
    },

    /**
     * Format price to Vietnamese currency
     * @param {number} price - Price value
     * @returns {string} Formatted price string
     */
    formatPrice: function(price) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(price);
    },

    /**
     * Update product price in product detail page
     * @param {Object} flashSaleInfo - Flash sale information
     */
    updateProductDetailPrice: function(flashSaleInfo) {
        if (!flashSaleInfo || !flashSaleInfo.inFlashSale) {
            return;
        }

        const priceElement = document.querySelector('.details-price, .product-price');
        if (priceElement && flashSaleInfo.flashSalePrice) {
            const originalPrice = flashSaleInfo.originalPrice;
            const flashPrice = flashSaleInfo.flashSalePrice;
            
            priceElement.innerHTML = `
                <span class="flash-price">${this.formatPrice(flashPrice)}</span>
                <span class="original-price">${this.formatPrice(originalPrice)}</span>
                <span class="discount-badge">-${flashSaleInfo.discountPercentage}%</span>
            `;
        }

        // Show countdown timer
        if (flashSaleInfo.remainingSeconds) {
            const countdownElement = document.getElementById('flash-sale-countdown');
            if (countdownElement) {
                const endTime = Math.floor(Date.now() / 1000) + flashSaleInfo.remainingSeconds;
                FlashSaleTimer.initCountdown('flash-sale-countdown', endTime);
            }
        }
    }
};

