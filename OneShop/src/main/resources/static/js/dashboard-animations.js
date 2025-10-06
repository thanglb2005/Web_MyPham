// ScrollReveal Configuration
document.addEventListener('DOMContentLoaded', function() {
    // Initialize animations
    animateDashboard();
    setupChartTypeToggle();
    
    // Add custom gradients to charts
    addChartGradients();
});

/**
 * Add gradient backgrounds to charts
 */
function addChartGradients() {
    // For revenue chart
    if (window.revenueChart) {
        const ctx = document.getElementById('revenueChart').getContext('2d');
        const gradientFill = ctx.createLinearGradient(0, 0, 0, 400);
        gradientFill.addColorStop(0, 'rgba(54, 162, 235, 0.8)');
        gradientFill.addColorStop(1, 'rgba(54, 162, 235, 0.1)');
        
        window.revenueChart.data.datasets[0].backgroundColor = gradientFill;
        window.revenueChart.data.datasets[0].borderColor = 'rgba(54, 162, 235, 1)';
        window.revenueChart.update();
    }
    
    // For report chart if it exists
    if (window.reportChartInstance) {
        const ctx = document.getElementById('reportChart').getContext('2d');
        const gradientFill = ctx.createLinearGradient(0, 0, 0, 400);
        gradientFill.addColorStop(0, 'rgba(75, 192, 192, 0.8)');
        gradientFill.addColorStop(1, 'rgba(75, 192, 192, 0.1)');
        
        if (window.reportChartInstance.config.type !== 'pie') {
            window.reportChartInstance.data.datasets[0].backgroundColor = gradientFill;
            window.reportChartInstance.data.datasets[0].borderColor = 'rgba(75, 192, 192, 1)';
            window.reportChartInstance.update();
        }
    }
}

/**
 * Enhanced percentage formatting with consistent sign handling
 */
function formatPercentage(value) {
    if (value === null || value === undefined || !isFinite(value)) {
        return '0,0%';
    }
    
    // Format with comma as decimal separator (Vietnamese format)
    const absValue = Math.abs(value).toFixed(1).replace('.', ',');
    
    // Only show minus sign for negative values, never show plus sign
    return value < 0 ? '-' + absValue + '%' : absValue + '%';
}

/**
 * Add scroll animations to dashboard elements
 */
function setupScrollAnimations() {
    ScrollReveal().reveal('.card', {
        duration: 1000,
        distance: '20px',
        origin: 'bottom',
        interval: 100,
        easing: 'cubic-bezier(0.5, 0, 0, 1)',
        reset: false
    });
    
    ScrollReveal().reveal('.chart-container', {
        duration: 1200,
        delay: 300,
        distance: '30px',
        origin: 'bottom',
        easing: 'cubic-bezier(0.5, 0, 0, 1)',
        reset: false
    });
    
    ScrollReveal().reveal('.table', {
        duration: 1000,
        delay: 200,
        distance: '20px',
        origin: 'bottom',
        easing: 'cubic-bezier(0.5, 0, 0, 1)',
        reset: false
    });
}