/**
 * Tổng Quan - Biểu đồ doanh thu
 * File này xử lý biểu đồ doanh thu trong phần tổng quan của trang admin
 */

document.addEventListener('DOMContentLoaded', function() {
    // Khởi tạo biểu đồ tổng quan
    initializeOverviewChart();
});

/**
 * Khởi tạo biểu đồ doanh thu tổng quan
 */
function initializeOverviewChart() {
    // Tìm canvas biểu đồ
    const overviewChartCanvas = document.getElementById('overviewRevenueChart');
    
    // Nếu không tìm thấy, return
    if (!overviewChartCanvas) {
        console.log('Không tìm thấy canvas biểu đồ tổng quan');
        return;
    }
    
    console.log('Khởi tạo biểu đồ doanh thu tổng quan...');
    
    // Lấy dữ liệu doanh thu từ API
    fetch('/admin/api/monthly-revenue-data')
        .then(response => response.json())
        .then(data => {
            console.log('Dữ liệu tổng quan nhận được:', data);
            
            // Kiểm tra dữ liệu có đúng định dạng
            if (data && data.revenues) {
                createOverviewChart(overviewChartCanvas, data.revenues, data.months);
            } else {
                console.error('Dữ liệu API không đúng định dạng:', data);
                // Dữ liệu mẫu nếu API không trả về đúng định dạng
                createOverviewChart(overviewChartCanvas, 
                    [5000000, 6200000, 7500000, 5800000, 8300000, 7200000, 9100000, 10500000, 9200000, 11200000, 8900000, 12000000],
                    ["Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", 
                     "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"]);
            }
        })
        .catch(error => {
            console.error('Lỗi khi lấy dữ liệu tổng quan:', error);
            // Dữ liệu mẫu nếu API bị lỗi
            createOverviewChart(overviewChartCanvas, 
                [5000000, 6200000, 7500000, 5800000, 8300000, 7200000, 9100000, 10500000, 9200000, 11200000, 8900000, 12000000],
                ["Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", 
                 "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"]);
        });
}

/**
 * Tạo biểu đồ doanh thu
 * @param {HTMLCanvasElement} canvas - Canvas để vẽ biểu đồ
 * @param {Array} revenueData - Dữ liệu doanh thu
 * @param {Array} labels - Nhãn cho trục x
 */
function createOverviewChart(canvas, revenueData, labels) {
    const ctx = canvas.getContext('2d');
    
    // Tạo gradient cho biểu đồ
    const gradient = ctx.createLinearGradient(0, 0, 0, 300);
    gradient.addColorStop(0, 'rgba(54, 162, 235, 0.8)');
    gradient.addColorStop(1, 'rgba(54, 162, 235, 0.1)');
    
    // Tạo biểu đồ với Chart.js
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Doanh thu',
                data: revenueData,
                backgroundColor: gradient,
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(0, 0, 0, 0.05)',
                        borderDash: [5, 5]
                    },
                    ticks: {
                        callback: function(value) {
                            if (value >= 1000000) {
                                return (value / 1000000).toFixed(1) + ' triệu';
                            }
                            return value.toLocaleString('vi-VN');
                        }
                    }
                },
                x: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        maxRotation: 45,
                        minRotation: 45
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return 'Doanh thu: ' + context.raw.toLocaleString('vi-VN') + ' ₫';
                        }
                    }
                },
                legend: {
                    display: true,
                    position: 'top'
                }
            }
        }
    });
    
    console.log('Biểu đồ tổng quan được tạo thành công');
}