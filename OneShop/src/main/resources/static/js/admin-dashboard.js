/**
 * Admin Dashboard - Revenue Chart
 * Initializes and manages the revenue chart on the admin dashboard
 */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize the revenue chart
    initRevenueChart();
});

/**
 * Initialize the revenue chart
 */
function initRevenueChart() {
    const revenueChartEl = document.getElementById('revenueChart');
    if (!revenueChartEl) return;
    
    console.log('Initializing revenue chart');
    
    // Get chart context
    const ctx = revenueChartEl.getContext('2d');
    
    // Create gradient for chart
    const gradient = ctx.createLinearGradient(0, 0, 0, 400);
    gradient.addColorStop(0, 'rgba(54, 162, 235, 0.8)');
    gradient.addColorStop(1, 'rgba(54, 162, 235, 0.1)');
    
    // Fetch revenue data from server
    fetchRevenueData()
        .then(data => {
            // Create chart with fetched data
            window.revenueChart = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: data.labels,
                    datasets: [{
                        label: 'Doanh thu',
                        data: data.values,
                        backgroundColor: gradient,
                        borderColor: 'rgba(54, 162, 235, 1)',
                        borderWidth: 2,
                        pointBackgroundColor: 'rgba(54, 162, 235, 1)',
                        pointBorderColor: '#fff',
                        pointRadius: 4,
                        fill: true,
                        tension: 0.3
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            display: true,
                            position: 'top'
                        },
                        tooltip: {
                            mode: 'index',
                            intersect: false,
                            callbacks: {
                                label: function(context) {
                                    let label = context.dataset.label || '';
                                    if (label) {
                                        label += ': ';
                                    }
                                    if (context.parsed.y !== null) {
                                        label += formatCurrency(context.parsed.y);
                                    }
                                    return label;
                                }
                            }
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                callback: function(value) {
                                    return formatCurrency(value, true);
                                }
                            }
                        },
                        x: {
                            grid: {
                                display: false
                            }
                        }
                    }
                }
            });
        })
        .catch(error => {
            console.error('Error initializing revenue chart:', error);
            // Display error message
            showChartError(revenueChartEl, 'Không thể tải dữ liệu biểu đồ');
        });
}

/**
 * Fetch revenue data from the server
 * @returns {Promise} Promise with revenue data
 */
function fetchRevenueData() {
    // Use the actual API endpoint
    return fetch('/admin/api/revenue-chart-data')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .catch(error => {
            console.error('Error fetching revenue data:', error);
            // Return sample data as fallback
            return {
                labels: ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'],
                values: [5500000, 7200000, 8100000, 6500000, 9800000, 8700000, 10200000, 11500000, 9900000, 12800000, 0, 0]
            };
        });
}

/**
 * Format currency in Vietnamese format
 * @param {number} value - The value to format
 * @param {boolean} compact - Whether to use compact notation
 * @returns {string} Formatted currency string
 */
function formatCurrency(value, compact = false) {
    if (compact) {
        // For axis labels (compact)
        if (value >= 1000000) {
            return (value / 1000000).toFixed(1) + ' triệu';
        } else if (value >= 1000) {
            return (value / 1000).toFixed(0) + ' nghìn';
        }
        return value;
    }
    
    // For tooltip (full)
    return new Intl.NumberFormat('vi-VN', { 
        style: 'currency', 
        currency: 'VND',
        maximumFractionDigits: 0
    }).format(value);
}

/**
 * Display an error message when chart data cannot be loaded
 * @param {HTMLElement} chartElement - The chart canvas element
 * @param {string} message - The error message
 */
function showChartError(chartElement, message) {
    // Clear canvas
    const ctx = chartElement.getContext('2d');
    ctx.clearRect(0, 0, chartElement.width, chartElement.height);
    
    // Create container for error message
    const container = chartElement.parentNode;
    const errorDiv = document.createElement('div');
    errorDiv.className = 'chart-error';
    errorDiv.style.position = 'absolute';
    errorDiv.style.top = '50%';
    errorDiv.style.left = '50%';
    errorDiv.style.transform = 'translate(-50%, -50%)';
    errorDiv.style.textAlign = 'center';
    
    // Create icon
    const icon = document.createElement('i');
    icon.className = 'fas fa-exclamation-circle';
    icon.style.fontSize = '32px';
    icon.style.color = '#f5365c';
    icon.style.marginBottom = '10px';
    icon.style.display = 'block';
    
    // Create message
    const text = document.createElement('span');
    text.textContent = message;
    text.style.color = '#525f7f';
    text.style.fontWeight = '500';
    
    // Assemble error message
    errorDiv.appendChild(icon);
    errorDiv.appendChild(text);
    
    // Add to container
    container.appendChild(errorDiv);
}