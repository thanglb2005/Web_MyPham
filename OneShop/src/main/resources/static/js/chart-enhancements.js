document.addEventListener('DOMContentLoaded', function() {
    // Enhanced Chart Styling for all charts
    Chart.defaults.font.family = "'Helvetica Neue', 'Helvetica', 'Arial', sans-serif";
    Chart.defaults.color = '#666';
    Chart.defaults.plugins.tooltip.padding = 10;
    Chart.defaults.plugins.tooltip.cornerRadius = 6;
    Chart.defaults.plugins.tooltip.backgroundColor = 'rgba(0, 0, 0, 0.7)';
    Chart.defaults.plugins.tooltip.titleFont = { weight: 'bold', size: 13 };
    Chart.defaults.plugins.tooltip.bodyFont = { size: 12 };
    
    // Enhanced scales
    Chart.defaults.scales.linear.grid = {
        color: 'rgba(0, 0, 0, 0.05)',
        borderDash: [5, 5]
    };
    
    Chart.defaults.scales.linear.ticks = {
        padding: 8,
        font: { size: 11 }
    };
    
    Chart.defaults.scales.category.grid = {
        display: false
    };
    
    // Enhanced animations - faster for better user experience
    Chart.defaults.animation.duration = 800;
    Chart.defaults.animation.easing = 'easeOutCubic';
    
    // Get revenue chart if exists
    var revenueChartEl = document.getElementById('revenueChart');
    if (revenueChartEl) {
        var ctx = revenueChartEl.getContext('2d');
        var gradient = ctx.createLinearGradient(0, 0, 0, 400);
        gradient.addColorStop(0, 'rgba(54, 162, 235, 0.8)');
        gradient.addColorStop(1, 'rgba(54, 162, 235, 0.1)');
        
        // Apply gradient to existing chart if it's already initialized
        if (window.revenueChart) {
            window.revenueChart.data.datasets[0].backgroundColor = gradient;
            window.revenueChart.data.datasets[0].borderColor = 'rgba(54, 162, 235, 1)';
            window.revenueChart.update();
        }
    }
    
    // Get report chart if exists
    var reportChartEl = document.getElementById('reportChart');
    if (reportChartEl) {
        var ctx = reportChartEl.getContext('2d');
        var gradient = ctx.createLinearGradient(0, 0, 0, 400);
        gradient.addColorStop(0, 'rgba(75, 192, 192, 0.8)');
        gradient.addColorStop(1, 'rgba(75, 192, 192, 0.1)');
        
        // Apply gradient to existing chart if it's already initialized
        if (window.reportChartInstance && window.reportChartInstance.config.type !== 'pie') {
            window.reportChartInstance.data.datasets[0].backgroundColor = gradient;
            window.reportChartInstance.data.datasets[0].borderColor = 'rgba(75, 192, 192, 1)';
            window.reportChartInstance.update();
        }
    }
});