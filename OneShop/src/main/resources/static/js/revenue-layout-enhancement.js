/**
 * Revenue Statistics Layout Enhancement
 * Provides animations and UI enhancements for the revenue statistics page
 */

document.addEventListener('DOMContentLoaded', function() {
    // Animate metric cards on load
    animateMetricCards();
    
    // Initialize tooltip functionality
    initializeTooltips();
    
    // Add hover effects to chart container
    enhanceChartInteraction();
    
    // Optimize layout for various screen sizes
    optimizeResponsiveLayout();
});

/**
 * Animate metric cards with a staggered fade-in effect
 */
function animateMetricCards() {
    const metricCards = document.querySelectorAll('.metric-card');
    
    metricCards.forEach((card, index) => {
        setTimeout(() => {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
            
            setTimeout(() => {
                card.style.transition = 'all 0.5s ease';
                card.style.opacity = '1';
                card.style.transform = 'translateY(0)';
            }, 100);
        }, index * 100);
    });
    
    // Animate summary stats
    const summaryCard = document.querySelector('.summary-stats');
    if (summaryCard) {
        setTimeout(() => {
            const statRows = summaryCard.querySelectorAll('.stat-row');
            statRows.forEach((row, idx) => {
                setTimeout(() => {
                    row.style.opacity = '0';
                    row.style.transform = 'translateX(-10px)';
                    
                    setTimeout(() => {
                        row.style.transition = 'all 0.3s ease';
                        row.style.opacity = '1';
                        row.style.transform = 'translateX(0)';
                    }, 50);
                }, idx * 100);
            });
        }, 400);
    }
}

/**
 * Initialize tooltip functionality for controls and information icons
 */
function initializeTooltips() {
    // Add tooltip functionality if Bootstrap tooltips are available
    if (typeof $().tooltip === 'function') {
        $('[data-toggle="tooltip"]').tooltip();
        
        // Add tooltips to chart controls
        $('.chart-controls button').tooltip();
    }
}

/**
 * Enhance chart interaction with hover effects and transitions
 */
function enhanceChartInteraction() {
    const chartContainer = document.getElementById('chartContainer');
    
    if (chartContainer) {
        // Add highlight effect on hover
        chartContainer.addEventListener('mouseenter', () => {
            chartContainer.style.boxShadow = '0 8px 30px rgba(0, 0, 0, 0.1)';
        });
        
        chartContainer.addEventListener('mouseleave', () => {
            chartContainer.style.boxShadow = 'none';
        });
    }
}

/**
 * Optimize layout for different screen sizes
 */
function optimizeResponsiveLayout() {
    const resizeHandler = () => {
        const width = window.innerWidth;
        const chartColumn = document.querySelector('.col-md-8');
        const summaryColumn = document.querySelector('.col-md-4');
        
        // Adjust layout for mobile devices
        if (width < 768) {
            if (chartColumn) chartColumn.classList.add('mb-4');
            if (summaryColumn) {
                summaryColumn.style.marginTop = '0';
            }
        } else {
            if (chartColumn) chartColumn.classList.remove('mb-4');
        }
    };
    
    // Initial call and add event listener
    resizeHandler();
    window.addEventListener('resize', resizeHandler);
}

/**
 * Show a notification message
 * @param {string} message - The message to display
 * @param {string} type - The type of notification (success, info, warning, error)
 */
function showEnhancedNotification(message, type = 'info') {
    // Use existing notification function if available
    if (typeof showNotification === 'function') {
        showNotification(message, type);
        return;
    }
    
    // Create custom notification if not available
    const notification = document.createElement('div');
    notification.className = `alert alert-${type} alert-dismissible fade show`;
    notification.style.position = 'fixed';
    notification.style.top = '20px';
    notification.style.right = '20px';
    notification.style.zIndex = '9999';
    notification.style.maxWidth = '350px';
    notification.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
    
    notification.innerHTML = `
        <button type="button" class="close" data-dismiss="alert" aria-label="Close">
            <span aria-hidden="true">&times;</span>
        </button>
        <div class="d-flex align-items-center">
            <i class="fas fa-info-circle me-2"></i>
            <span>${message}</span>
        </div>
    `;
    
    document.body.appendChild(notification);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 5000);
    
    // Close button functionality
    const closeBtn = notification.querySelector('.close');
    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            notification.classList.remove('show');
            setTimeout(() => {
                document.body.removeChild(notification);
            }, 300);
        });
    }
}