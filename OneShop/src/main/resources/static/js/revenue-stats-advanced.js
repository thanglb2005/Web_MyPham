/**
 * Các chức năng nâng cao cho trang thống kê doanh thu
 */

// Định dạng tiền tệ Việt Nam
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', { 
        style: 'currency', 
        currency: 'VND',
        maximumFractionDigits: 0
    }).format(amount);
}

// Định dạng phần trăm
function formatPercentage(value) {
    // Kiểm tra giá trị đặc biệt
    if (value === null || value === undefined || !isFinite(value)) {
        return '0,0%';
    }
    
    // Chuyển đổi từ số thành chuỗi có dấu phẩy thay dấu chấm (định dạng Việt Nam)
    let formatted = value.toFixed(1).replace('.', ',');
    return formatted + '%';
}

document.addEventListener('DOMContentLoaded', function() {
    // Thêm hiệu ứng khi trang tải xong
    animateDashboard();

    // ===== 1. Xử lý filter theo thời gian =====
    const dateFilterForm = document.getElementById('date-filter-form');
    const resetFilterBtn = document.getElementById('reset-filter');
    
    if (resetFilterBtn) {
        resetFilterBtn.addEventListener('click', function() {
            document.getElementById('startDate').value = '';
            document.getElementById('endDate').value = '';
            
            // Submit form với giá trị rỗng
            dateFilterForm.submit();
        });
    }
    
    // Xử lý chuyển đổi loại biểu đồ
    setupChartTypeToggle();
    
    // ===== 1.5. Tạo tóm tắt báo cáo =====
    createReportSummary();
    
    // ===== 2. Xử lý xuất báo cáo sang Excel =====
    const exportExcelBtn = document.getElementById('export-excel');
    if (exportExcelBtn) {
        exportExcelBtn.addEventListener('click', function() {
            exportToExcel();
        });
    }
    
    // ===== 3. Xử lý xuất báo cáo sang PDF =====
    const exportPdfBtn = document.getElementById('export-pdf');
    if (exportPdfBtn) {
        exportPdfBtn.addEventListener('click', function() {
            exportToPDF();
        });
    }
    
    // ===== 4. Xử lý thay đổi loại biểu đồ =====
    const chartTypeSelect = document.getElementById('chart-type');
    if (chartTypeSelect) {
        chartTypeSelect.addEventListener('change', function() {
            // Lấy loại biểu đồ mới
            const newChartType = chartTypeSelect.value;
            
            // Lấy đối tượng biểu đồ nếu đã được khởi tạo
            const reportChart = window.reportChartInstance;
            
            if (reportChart) {
                // Cập nhật loại biểu đồ
                reportChart.config.type = newChartType;
                
                // Nếu chuyển sang biểu đồ tròn, điều chỉnh dữ liệu và màu sắc
                if (newChartType === 'pie') {
                    // Lấy 5 mục có doanh thu cao nhất để hiển thị
                    const top5Data = reportChart.data.labels.map((label, index) => ({
                        label: label,
                        value: reportChart.data.datasets[0].data[index]
                    }))
                    .sort((a, b) => b.value - a.value)
                    .slice(0, 5);
                    
                    // Thêm mục "Khác" cho các dữ liệu còn lại
                    let otherValue = 0;
                    reportChart.data.datasets[0].data.forEach((value, index) => {
                        if (!top5Data.some(item => item.label === reportChart.data.labels[index])) {
                            otherValue += value;
                        }
                    });
                    
                    // Cập nhật dữ liệu biểu đồ
                    reportChart.data.labels = top5Data.map(item => item.label);
                    reportChart.data.datasets[0].data = top5Data.map(item => item.value);
                    
                    // Thêm mục "Khác" nếu có
                    if (otherValue > 0) {
                        reportChart.data.labels.push('Khác');
                        reportChart.data.datasets[0].data.push(otherValue);
                    }
                    
                    // Tạo mảng màu ngẫu nhiên cho biểu đồ tròn
                    const colorArray = [];
                    for (let i = 0; i < reportChart.data.labels.length; i++) {
                        colorArray.push(getRandomColor());
                    }
                    
                    reportChart.data.datasets[0].backgroundColor = colorArray;
                }
                
                // Áp dụng cấu hình dựa trên loại biểu đồ mới
                applyChartTypeConfig(reportChart, newChartType);
                
                // Cập nhật biểu đồ với hiệu ứng
                reportChart.update({
                    duration: 800,
                    easing: 'easeOutQuart'
                });
            }
        });
    }
    
    // ===== 5. Xử lý so sánh doanh thu giữa các kỳ =====
    const comparePeriodsBtn = document.getElementById('compare-periods');
    const comparisonTypeSelect = document.getElementById('comparison-type');
    const periodValueSelect = document.getElementById('period-value');
    
    if (comparisonTypeSelect) {
        // Cập nhật các tùy chọn khi thay đổi loại so sánh
        comparisonTypeSelect.addEventListener('change', function() {
            updatePeriodOptions();
        });
        
        // Khởi tạo các tùy chọn ban đầu
        updatePeriodOptions();
    }
    
    if (comparePeriodsBtn) {
        comparePeriodsBtn.addEventListener('click', function() {
            fetchComparisonData();
        });
    }
});

/**
 * Xuất dữ liệu báo cáo sang Excel
 */
function exportToExcel() {
    const table = document.getElementById('report-table');
    if (!table) return;
    
    // Lấy tiêu đề báo cáo
    const reportTitle = document.querySelector('.card-title').textContent;
    const fileName = 'Bao_Cao_' + reportTitle.replace(/\s+/g, '_') + '_' + formatDate(new Date()) + '.xlsx';
    
    // Tạo workbook
    const ws = XLSX.utils.table_to_sheet(table);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Báo cáo");
    
    // Tự động điều chỉnh độ rộng cột
    const colWidths = [];
    const data = XLSX.utils.sheet_to_json(ws, { header: 1 });
    
    // Xử lý độ rộng cột
    data.forEach(row => {
        row.forEach((cell, i) => {
            const cellValue = String(cell);
            colWidths[i] = Math.max(colWidths[i] || 0, cellValue.length);
        });
    });
    
    ws['!cols'] = colWidths.map(width => ({ wch: width + 2 }));
    
    // Tải xuống file Excel
    XLSX.writeFile(wb, fileName);
}

/**
 * Xuất dữ liệu báo cáo sang PDF
 */
function exportToPDF() {
    const table = document.getElementById('report-table');
    if (!table) return;
    
    // Lấy tiêu đề báo cáo
    const reportTitle = document.querySelector('.card-title').textContent;
    const fileName = 'Bao_Cao_' + reportTitle.replace(/\s+/g, '_') + '_' + formatDate(new Date()) + '.pdf';
    
    // Tạo PDF
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF('l', 'mm', 'a4');
    
    // Thêm tiêu đề
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(16);
    doc.text(reportTitle, doc.internal.pageSize.getWidth() / 2, 20, { align: 'center' });
    
    // Thêm ngày xuất báo cáo
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(10);
    doc.text('Ngày xuất báo cáo: ' + formatDisplayDate(new Date()), doc.internal.pageSize.getWidth() / 2, 27, { align: 'center' });
    
    // Chuyển đổi bảng sang PDF
    doc.autoTable({
        html: '#report-table',
        startY: 35,
        styles: {
            fontSize: 9,
            cellPadding: 2,
            overflow: 'linebreak'
        },
        columnStyles: {
            0: { cellWidth: 'auto' }
        },
        headStyles: {
            fillColor: [41, 128, 185],
            textColor: 255,
            fontStyle: 'bold'
        },
        alternateRowStyles: {
            fillColor: [245, 245, 245]
        }
    });
    
    // Lưu file PDF
    doc.save(fileName);
}

/**
 * Cập nhật các tùy chọn cho select box kỳ so sánh
 */
function updatePeriodOptions() {
    const comparisonType = document.getElementById('comparison-type').value;
    const periodValueSelect = document.getElementById('period-value');
    
    // Xóa tất cả các tùy chọn hiện có
    periodValueSelect.innerHTML = '';
    
    const currentDate = new Date();
    
    if (comparisonType === 'month') {
        // Tạo tùy chọn cho 12 tháng gần nhất
        for (let i = 0; i < 12; i++) {
            const date = new Date(currentDate.getFullYear(), currentDate.getMonth() - i, 1);
            const monthValue = date.getMonth() + 1;
            const yearValue = date.getFullYear();
            const option = document.createElement('option');
            option.value = yearValue + '-' + monthValue.toString().padStart(2, '0');
            option.textContent = 'Tháng ' + monthValue + '/' + yearValue;
            periodValueSelect.appendChild(option);
        }
    } else if (comparisonType === 'quarter') {
        // Tạo tùy chọn cho 8 quý gần nhất
        const currentQuarter = Math.floor(currentDate.getMonth() / 3) + 1;
        const currentYear = currentDate.getFullYear();
        
        for (let i = 0; i < 8; i++) {
            let quarter = currentQuarter - (i % 4);
            let year = currentYear - Math.floor(i / 4);
            
            if (quarter <= 0) {
                quarter += 4;
                year--;
            }
            
            const option = document.createElement('option');
            option.value = year + '-Q' + quarter;
            option.textContent = 'Quý ' + quarter + '/' + year;
            periodValueSelect.appendChild(option);
        }
    } else if (comparisonType === 'year') {
        // Tạo tùy chọn cho 5 năm gần nhất
        const currentYear = currentDate.getFullYear();
        
        for (let i = 0; i < 5; i++) {
            const yearValue = currentYear - i;
            const option = document.createElement('option');
            option.value = yearValue.toString();
            option.textContent = 'Năm ' + yearValue;
            periodValueSelect.appendChild(option);
        }
    }
}

/**
 * Lấy dữ liệu so sánh từ server
 */
function fetchComparisonData() {
    const comparisonType = document.getElementById('comparison-type').value;
    const periodValue = document.getElementById('period-value').value;
    
    // Hiển thị trạng thái đang tải
    document.getElementById('comparison-placeholder').innerHTML = 
        '<div class="text-center py-4"><i class="fas fa-spinner fa-spin"></i> Đang tải dữ liệu so sánh...</div>';
    
    // Gọi API để lấy dữ liệu thực tế từ server
    fetch(`/admin/api/compare-periods?type=${comparisonType}&period=${periodValue}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            console.log('Dữ liệu so sánh từ API:', data);
            
            // Tính toán sự chênh lệch và tỷ lệ phần trăm
            const revenueDiff = data.currentRevenue - data.previousRevenue;
            
            // Xử lý trường hợp đặc biệt để tránh hiển thị -100% khi dữ liệu không hợp lệ
            let revenueRate = 0;
            if (data.previousRevenue > 0) {
                revenueRate = (revenueDiff / data.previousRevenue) * 100;
            } else if (data.currentRevenue > 0) {
                revenueRate = 100; // Tăng trưởng 100% nếu kỳ trước không có doanh thu
            }
            
            const ordersDiff = data.currentOrders - data.previousOrders;
            let ordersRate = 0;
            if (data.previousOrders > 0) {
                ordersRate = (ordersDiff / data.previousOrders) * 100;
            } else if (data.currentOrders > 0) {
                ordersRate = 100; // Tăng trưởng 100% nếu kỳ trước không có đơn hàng
            }
            
            // Tính giá trị trung bình đơn hàng
            const currentAvg = data.currentOrders > 0 ? data.currentRevenue / data.currentOrders : 0;
            const previousAvg = data.previousOrders > 0 ? data.previousRevenue / data.previousOrders : 0;
            const avgDiff = currentAvg - previousAvg;
            
            // Xử lý trường hợp đặc biệt cho tỷ lệ trung bình đơn hàng
            let avgRate = 0;
            if (previousAvg > 0) {
                avgRate = (avgDiff / previousAvg) * 100;
            } else if (currentAvg > 0) {
                avgRate = 100; // Tăng trưởng 100% nếu kỳ trước không có đơn hàng
            }
            
            // Tạo object dữ liệu để hiển thị
            const comparisonData = {
                currentPeriod: data.currentPeriod,
                previousPeriod: data.previousPeriod,
                currentRevenue: data.currentRevenue,
                previousRevenue: data.previousRevenue,
                currentOrders: data.currentOrders || 0, 
                previousOrders: data.previousOrders || 0,
                currentAvg: currentAvg,
                previousAvg: previousAvg,
                revenueDiff: revenueDiff,
                ordersDiff: ordersDiff,
                avgDiff: avgDiff,
                revenueRate: revenueRate,
                ordersRate: ordersRate,
                avgRate: avgRate
            };
            
            displayComparisonData(comparisonData);
        })
        .catch(error => {
            console.error('Lỗi khi lấy dữ liệu so sánh:', error);
            document.getElementById('comparison-placeholder').innerHTML = 
                '<div class="alert alert-danger">Có lỗi xảy ra khi tải dữ liệu. Vui lòng thử lại sau.</div>';
        });
}

/**
 * Tạo dữ liệu mẫu cho demo
 */
function generateSampleComparisonData(comparisonType, periodValue) {
    // Tạo dữ liệu ngẫu nhiên cho kỳ hiện tại
    const currentRevenue = Math.floor(Math.random() * 100000000) + 10000000;
    const currentOrders = Math.floor(Math.random() * 100) + 50;
    const currentAvg = currentRevenue / currentOrders;
    
    // Tạo dữ liệu cho kỳ trước (thấp hơn một chút để có xu hướng tăng)
    const growthFactor = 0.8 + Math.random() * 0.4; // 80-120% so với kỳ hiện tại
    const previousRevenue = Math.floor(currentRevenue * growthFactor);
    const previousOrders = Math.floor(currentOrders * growthFactor);
    const previousAvg = previousRevenue / previousOrders;
    
    // Tính toán chênh lệch
    const revenueDiff = currentRevenue - previousRevenue;
    const ordersDiff = currentOrders - previousOrders;
    const avgDiff = currentAvg - previousAvg;
    
    // Tính tỷ lệ phần trăm
    const revenueRate = (revenueDiff / previousRevenue) * 100;
    const ordersRate = (ordersDiff / previousOrders) * 100;
    const avgRate = (avgDiff / previousAvg) * 100;
    
    // Xác định tên kỳ
    let currentPeriodName, previousPeriodName;
    
    if (comparisonType === 'month') {
        const [year, month] = periodValue.split('-');
        currentPeriodName = 'Tháng ' + parseInt(month) + '/' + year;
        
        // Tính tháng trước
        let prevMonth = parseInt(month) - 1;
        let prevYear = parseInt(year);
        if (prevMonth === 0) {
            prevMonth = 12;
            prevYear--;
        }
        previousPeriodName = 'Tháng ' + prevMonth + '/' + prevYear;
    } else if (comparisonType === 'quarter') {
        const [year, quarter] = periodValue.split('-Q');
        currentPeriodName = 'Quý ' + quarter + '/' + year;
        
        // Tính quý trước
        let prevQuarter = parseInt(quarter) - 1;
        let prevYear = parseInt(year);
        if (prevQuarter === 0) {
            prevQuarter = 4;
            prevYear--;
        }
        previousPeriodName = 'Quý ' + prevQuarter + '/' + prevYear;
    } else if (comparisonType === 'year') {
        currentPeriodName = 'Năm ' + periodValue;
        previousPeriodName = 'Năm ' + (parseInt(periodValue) - 1);
    }
    
    return {
        currentPeriodName,
        previousPeriodName,
        currentRevenue,
        previousRevenue,
        currentOrders,
        previousOrders,
        currentAvg,
        previousAvg,
        revenueDiff,
        ordersDiff,
        avgDiff,
        revenueRate,
        ordersRate,
        avgRate
    };
}

/**
 * Hiển thị dữ liệu so sánh
 */
function displayComparisonData(data) {
    console.log('Hiển thị dữ liệu so sánh:', data);
    
    // Cập nhật tên các kỳ
    document.getElementById('current-period').textContent = data.currentPeriod || data.currentPeriodName;
    document.getElementById('previous-period').textContent = data.previousPeriod || data.previousPeriodName;
    
    // Tạo hiệu ứng chuyển đổi số
    function animateCountUp(elementId, targetValue, prefix = '', suffix = '', duration = 1000) {
        const el = document.getElementById(elementId);
        const start = 0;
        const increment = targetValue / (duration / 16);
        let current = start;
        
        const timer = setInterval(function() {
            current += increment;
            if (current >= targetValue) {
                current = targetValue;
                clearInterval(timer);
            }
            
            if (typeof targetValue === 'number') {
                if (targetValue >= 1000) {
                    // Nếu là giá trị tiền tệ lớn, định dạng với phân cách hàng nghìn
                    el.textContent = prefix + Math.floor(current).toLocaleString('vi-VN') + suffix;
                } else {
                    // Nếu là giá trị nhỏ, giữ 1 số thập phân
                    el.textContent = prefix + current.toFixed(1) + suffix;
                }
            } else {
                el.textContent = prefix + Math.floor(current) + suffix;
            }
        }, 16);
    }
    
    // Cập nhật số liệu doanh thu với hiệu ứng
    document.getElementById('current-revenue').textContent = '';
    document.getElementById('previous-revenue').textContent = '';
    document.getElementById('revenue-diff').textContent = '';
    
    setTimeout(() => {
        animateCountUp('current-revenue', data.currentRevenue, '', ' ₫');
        animateCountUp('previous-revenue', data.previousRevenue, '', ' ₫');
        animateCountUp('revenue-diff', data.revenueDiff, '', ' ₫');
        document.getElementById('revenue-rate').innerHTML = formatRateWithIcon(data.revenueRate);
    }, 200);
    
    // Cập nhật số liệu đơn hàng với hiệu ứng
    document.getElementById('current-orders').textContent = '';
    document.getElementById('previous-orders').textContent = '';
    document.getElementById('orders-diff').textContent = '';
    
    setTimeout(() => {
        animateCountUp('current-orders', data.currentOrders);
        animateCountUp('previous-orders', data.previousOrders);
        animateCountUp('orders-diff', data.ordersDiff);
        document.getElementById('orders-rate').innerHTML = formatRateWithIcon(data.ordersRate);
    }, 400);
    
    // Cập nhật số liệu giá trị trung bình với hiệu ứng
    document.getElementById('current-avg').textContent = '';
    document.getElementById('previous-avg').textContent = '';
    document.getElementById('avg-diff').textContent = '';
    
    setTimeout(() => {
        animateCountUp('current-avg', data.currentAvg, '', ' ₫');
        animateCountUp('previous-avg', data.previousAvg, '', ' ₫');
        animateCountUp('avg-diff', data.avgDiff, '', ' ₫');
        document.getElementById('avg-rate').innerHTML = formatRateWithIcon(data.avgRate);
    }, 600);
    
    // Hiển thị bảng kết quả với hiệu ứng
    document.getElementById('comparison-placeholder').style.display = 'none';
    const resultTable = document.getElementById('comparison-result');
    resultTable.style.display = 'block';
    resultTable.classList.add('animate__animated', 'animate__fadeIn');
    
    // Thêm hiệu ứng highlight cho hàng khi có giá trị tốt
    setTimeout(() => {
        if (data.revenueRate > 0) {
            document.querySelector('.revenue-row').classList.add('table-success');
        }
        if (data.ordersRate > 0) {
            document.querySelector('.orders-row').classList.add('table-success');
        }
        if (data.avgRate > 0) {
            document.querySelector('.avg-row').classList.add('table-success');
        }
    }, 1000);
}

/**
 * Định dạng tỷ lệ với biểu tượng tăng/giảm và hiệu ứng
 */
function formatRateWithIcon(rate) {
    // Xử lý các giá trị đặc biệt
    if (rate === null || rate === undefined || !isFinite(rate)) {
        return '<span class="text-muted">0,0%</span>';
    }
    
    const formattedRate = formatPercentage(rate);
    
    if (rate > 0) {
        return '<span class="text-success animate__animated animate__fadeIn"><i class="fas fa-arrow-up"></i> ' + formattedRate + '</span>';
    } else if (rate < 0) {
        return '<span class="text-danger animate__animated animate__fadeIn"><i class="fas fa-arrow-down"></i> ' + formattedRate + '</span>';
    } else {
        return '<span class="text-muted animate__animated animate__fadeIn">' + formattedRate + '</span>';
    }
}

/**
 * Định dạng ngày tháng cho tên file
 */
function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return year + month + day;
}

/**
 * Tạo màu ngẫu nhiên cho biểu đồ
 */
function getRandomColor() {
    const letters = '0123456789ABCDEF';
    let color = '#';
    for (let i = 0; i < 6; i++) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}

/**
 * Tạo tóm tắt báo cáo dựa trên dữ liệu bảng và khởi tạo biểu đồ
 */
function createReportSummary() {
    const reportTable = document.getElementById('report-table');
    const reportSummary = document.getElementById('report-summary');
    const reportChartCanvas = document.getElementById('reportChart');
    
    if (!reportTable || !reportSummary) return;
    
    // Lấy loại báo cáo hiện tại
    const reportType = document.querySelector('th[th\\:if]')?.getAttribute('th:if')?.match(/reportType == '([^']+)'/)?.[1];
    
    // Lấy tất cả các hàng dữ liệu
    const rows = Array.from(reportTable.querySelectorAll('tbody tr'));
    
    if (rows.length === 0) {
        // Ẩn phần tóm tắt nếu không có dữ liệu
        reportSummary.style.display = 'none';
        return;
    }
    
    // Dữ liệu cho biểu đồ
    const chartLabels = [];
    const chartData = [];
    
    let totalItems = rows.length;
    let totalQuantity = 0;
    let totalRevenue = 0;
    let totalPriceSum = 0;
    
    // Duyệt qua từng hàng để tính toán tổng
    rows.forEach(row => {
        const cells = row.querySelectorAll('td');
        
        if (reportType === 'products') {
            // Đối với báo cáo sản phẩm: cells[2] = số lượng, cells[3] = doanh thu
            const quantity = parseInt(cells[2].textContent.trim(), 10) || 0;
            const revenue = parseFloat(cells[3].textContent.replace(/[^\d]/g, '')) || 0;
            const avgPrice = parseFloat(cells[4].textContent.replace(/[^\d]/g, '')) || 0;
            
            totalQuantity += quantity;
            totalRevenue += revenue;
            totalPriceSum += avgPrice;
        } else if (reportType === 'users') {
            // Đối với báo cáo khách hàng: cells[2] = số đơn hàng, cells[3] = tổng chi tiêu
            const orderCount = parseInt(cells[2].textContent.trim(), 10) || 0;
            const totalSpent = parseFloat(cells[3].textContent.replace(/[^\d]/g, '')) || 0;
            const avgOrderValue = parseFloat(cells[4].textContent.replace(/[^\d]/g, '')) || 0;
            
            totalQuantity += orderCount; // Số đơn hàng thay vì số lượng sản phẩm
            totalRevenue += totalSpent;
            totalPriceSum += avgOrderValue;
        } else {
            // Đối với các loại báo cáo khác: cells[2] = số lượng, cells[3] = doanh thu
            const quantity = parseInt(cells[2].textContent.trim(), 10) || 0;
            const revenue = parseFloat(cells[3].textContent.replace(/[^\d]/g, '')) || 0;
            const avgPrice = parseFloat(cells[4].textContent.replace(/[^\d]/g, '')) || 0;
            
            totalQuantity += quantity;
            totalRevenue += revenue;
            totalPriceSum += avgPrice;
        }
    });
    
    // Tính giá trung bình
    const avgPrice = totalItems > 0 ? totalPriceSum / totalItems : 0;
    
    // Cập nhật các giá trị trong thẻ tóm tắt
    document.getElementById('summary-total-items').textContent = totalItems;
    document.getElementById('summary-total-revenue').textContent = formatCurrency(totalRevenue);
    
    // Cập nhật các tiêu đề tùy thuộc vào loại báo cáo
    if (reportType === 'users') {
        // Tính toán phân bố khách hàng theo trạng thái
        let activeCustomers = 0;
        let occasionalCustomers = 0;
        let inactiveCustomers = 0;
        
        rows.forEach(row => {
            const cells = row.querySelectorAll('td');
            const status = cells[6].textContent.trim();
            
            if (status.includes('thường xuyên')) {
                activeCustomers++;
            } else if (status.includes('thỉnh thoảng')) {
                occasionalCustomers++;
            } else {
                inactiveCustomers++;
            }
        });
        
        // Thay đổi icon và màu sắc cho thẻ thứ 3 và 4
        document.querySelector('#report-summary .col-md-3:nth-child(3) .icon-big i').className = 'fas fa-users';
        document.querySelector('#report-summary .col-md-3:nth-child(4) .icon-big i').className = 'fas fa-user-check';
        
        document.querySelector('#report-summary .col-md-3:nth-child(1) .card-category').textContent = 'Tổng số khách hàng';
        document.querySelector('#report-summary .col-md-3:nth-child(2) .card-category').textContent = 'Tổng chi tiêu';
        document.querySelector('#report-summary .col-md-3:nth-child(3) .card-category').textContent = 'Khách hàng thường xuyên';
        document.querySelector('#report-summary .col-md-3:nth-child(4) .card-category').textContent = 'Tỷ lệ duy trì';
        
        document.getElementById('summary-total-quantity').textContent = activeCustomers.toLocaleString('vi-VN');
        
        // Tính tỷ lệ khách hàng hoạt động (thường xuyên + thỉnh thoảng)
        const retentionRate = totalItems > 0 ? 
            ((activeCustomers + occasionalCustomers) / totalItems * 100).toFixed(1) + '%' : '0%';
        document.getElementById('summary-avg-price').textContent = retentionRate;
    } else {
        document.querySelector('#report-summary .col-md-3:nth-child(1) .card-category').textContent = 'Tổng số mục';
        document.querySelector('#report-summary .col-md-3:nth-child(2) .card-category').textContent = 'Tổng doanh thu';
        document.querySelector('#report-summary .col-md-3:nth-child(3) .card-category').textContent = 'Tổng số lượng';
        document.querySelector('#report-summary .col-md-3:nth-child(4) .card-category').textContent = 'Giá trung bình';
        document.getElementById('summary-total-quantity').textContent = totalQuantity.toLocaleString('vi-VN');
        document.getElementById('summary-avg-price').textContent = formatCurrency(avgPrice);
    }
    
    // Hiển thị phần tóm tắt
    reportSummary.style.display = 'flex';
    
    // Tạo dữ liệu cho biểu đồ
    rows.forEach(row => {
        const cells = row.querySelectorAll('td');
        let label, value;
        
        if (reportType === 'products') {
            label = cells[1].textContent.trim(); // Tên sản phẩm
            value = parseFloat(cells[3].textContent.replace(/[^\d]/g, '')) || 0; // Doanh thu
        } else if (reportType === 'users') {
            label = cells[1].textContent.trim(); // Tên khách hàng
            value = parseFloat(cells[3].textContent.replace(/[^\d]/g, '')) || 0; // Tổng chi tiêu
        } else {
            label = cells[1].textContent.trim(); // Tên danh mục/năm/tháng/quý...
            value = parseFloat(cells[3].textContent.replace(/[^\d]/g, '')) || 0; // Doanh thu
        }
        
        chartLabels.push(label);
        chartData.push(value);
    });
    
    // Nếu có canvas biểu đồ, khởi tạo biểu đồ
    if (reportChartCanvas) {
        // Tạo mảng màu ngẫu nhiên
        const backgroundColors = [];
        const borderColors = [];
        
        for (let i = 0; i < chartLabels.length; i++) {
            const color = getRandomColor();
            backgroundColors.push(color + '80'); // Thêm độ trong suốt
            borderColors.push(color);
        }
        
        // Khởi tạo biểu đồ
        if (window.reportChartInstance) {
            window.reportChartInstance.destroy();
        }
        
        // Lấy loại biểu đồ từ select (nếu đã chọn)
        const chartTypeSelect = document.getElementById('chart-type');
        const chartType = chartTypeSelect ? chartTypeSelect.value : 'bar';
        
        // Đặt nhãn cho dataset dựa trên loại báo cáo
        const datasetLabel = reportType === 'users' ? 'Tổng chi tiêu' : 'Doanh thu';
        
        // Tạo dataset cho biểu đồ
        const dataset = {
            label: datasetLabel,
            data: chartData,
            backgroundColor: backgroundColors,
            borderWidth: 1
        };
        
        // Cấu hình thêm cho doughnut
        if (chartType === 'doughnut') {
            dataset.borderColor = 'white';
            dataset.borderWidth = 2;
            dataset.hoverBorderColor = 'white';
        } else {
            dataset.borderColor = borderColors;
        }
        
        // Tạo đối tượng cấu hình
        const chartOptions = {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return formatCurrency(value).replace(' ₫', ''); // Bỏ ký tự đồng để gọn hơn
                        }
                    },
                    display: chartType !== 'pie' && chartType !== 'doughnut'
                },
                x: {
                    display: chartType !== 'pie' && chartType !== 'doughnut'
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return formatCurrency(context.raw);
                        }
                    },
                    backgroundColor: 'rgba(0, 0, 0, 0.7)',
                    titleFont: {
                        size: 13,
                        weight: 'bold'
                    },
                    bodyFont: {
                        size: 12
                    },
                    padding: 12,
                    cornerRadius: 6,
                    displayColors: true
                },
                legend: {
                    display: chartType === 'pie' || chartType === 'doughnut',
                    position: 'top',
                    labels: {
                        padding: 15,
                        usePointStyle: true,
                        pointStyle: 'circle'
                    }
                }
            }
        };
        
        // Thiết lập đặc biệt cho doughnut
        if (chartType === 'doughnut') {
            // Thiết lập cả hai thuộc tính để tương thích với mọi phiên bản Chart.js
            chartOptions.cutout = '50%';
            chartOptions.cutoutPercentage = 50;
            console.log('Khởi tạo biểu đồ doughnut với cutout:', chartOptions.cutout);
        } else if (chartType === 'pie') {
            // Đảm bảo không có cutout cho biểu đồ pie
            chartOptions.cutout = 0;
            chartOptions.cutoutPercentage = 0;
        }
        
        // Khởi tạo biểu đồ
        window.reportChartInstance = new Chart(reportChartCanvas, {
            type: chartType,
            data: {
                labels: chartLabels,
                datasets: [dataset]
            },
            options: chartOptions
        });
    }
}

/**
 * Thêm hiệu ứng cho dashboard
 */
function animateDashboard() {
    // Thêm hiệu ứng cho các card thống kê
    const cards = document.querySelectorAll('.dashboard-item');
    cards.forEach((card, index) => {
        setTimeout(() => {
            card.classList.add('animate__animated', 'animate__fadeInUp');
        }, 100 * index);
    });
    
    // Thêm hiệu ứng cho các biểu đồ
    const charts = document.querySelectorAll('.chart-container');
    charts.forEach((chart) => {
        setTimeout(() => {
            chart.classList.add('animate__animated', 'animate__fadeIn');
        }, 500);
    });
    
    // Thêm hiệu ứng cho bảng so sánh
    const tables = document.querySelectorAll('.comparison-table');
    tables.forEach((table) => {
        table.classList.add('animate__animated', 'animate__fadeInUp');
    });
    
    // Thêm hiệu ứng cho các nút
    const buttons = document.querySelectorAll('.btn-modern');
    buttons.forEach((button) => {
        button.addEventListener('mouseenter', function() {
            this.classList.add('animate__animated', 'animate__pulse');
        });
        button.addEventListener('mouseleave', function() {
            this.classList.remove('animate__animated', 'animate__pulse');
        });
    });
    
    // Gọi hàm thiết lập các hiệu ứng cuộn
    setupScrollAnimations();
}

/**
 * Áp dụng cấu hình cho biểu đồ dựa trên loại biểu đồ
 */
function applyChartTypeConfig(chartInstance, chartType) {
    // Đảm bảo cấu hình phù hợp với loại biểu đồ
    if (chartType === 'doughnut') {
        // Thiết lập cả hai thuộc tính để tương thích với mọi phiên bản Chart.js
        chartInstance.options.cutout = '50%';
        chartInstance.options.cutoutPercentage = 50;
        
        // Ẩn scales
        if (chartInstance.options.scales && chartInstance.options.scales.x) {
            chartInstance.options.scales.x.display = false;
        }
        if (chartInstance.options.scales && chartInstance.options.scales.y) {
            chartInstance.options.scales.y.display = false;
        }
        
        // Cấu hình bộ dữ liệu
        if (chartInstance.data.datasets && chartInstance.data.datasets.length > 0) {
            chartInstance.data.datasets[0].borderColor = 'white';
            chartInstance.data.datasets[0].borderWidth = 2;
            chartInstance.data.datasets[0].hoverBorderColor = 'white';
        }
        
        // Hiển thị legend
        if (chartInstance.options.plugins && chartInstance.options.plugins.legend) {
            chartInstance.options.plugins.legend.display = true;
        }
        
        console.log('Đã áp dụng cấu hình doughnut:', {
            cutout: chartInstance.options.cutout,
            cutoutPercentage: chartInstance.options.cutoutPercentage
        });
    } else if (chartType === 'pie') {
        // Cấu hình cho biểu đồ tròn
        chartInstance.options.cutout = 0;
        chartInstance.options.cutoutPercentage = 0;
        
        // Ẩn scales
        if (chartInstance.options.scales && chartInstance.options.scales.x) {
            chartInstance.options.scales.x.display = false;
        }
        if (chartInstance.options.scales && chartInstance.options.scales.y) {
            chartInstance.options.scales.y.display = false;
        }
        
        // Hiển thị legend
        if (chartInstance.options.plugins && chartInstance.options.plugins.legend) {
            chartInstance.options.plugins.legend.display = true;
        }
    } else {
        // Cấu hình cho biểu đồ bar/line
        if (chartInstance.options.scales && chartInstance.options.scales.x) {
            chartInstance.options.scales.x.display = true;
        }
        if (chartInstance.options.scales && chartInstance.options.scales.y) {
            chartInstance.options.scales.y.display = true;
        }
        
        // Ẩn legend nếu không cần thiết
        if (chartInstance.options.plugins && chartInstance.options.plugins.legend) {
            chartInstance.options.plugins.legend.display = false;
        }
    }
}

/**
 * Thiết lập chuyển đổi loại biểu đồ
 */
function setupChartTypeToggle() {
    const chartTypeButtons = document.querySelectorAll('.chart-type');
    if (chartTypeButtons.length > 0) {
        chartTypeButtons.forEach(button => {
            // Remove any existing event listeners to prevent double-binding
            button.removeEventListener('click', handleChartTypeClick);
            // Add the event listener
            button.addEventListener('click', handleChartTypeClick);
        });
    }
}

// Separate function to handle chart type clicks
function handleChartTypeClick(event) {
    // Use event.currentTarget instead of 'this' for better reliability
    const button = event.currentTarget;
    
    // Xác định container cha để chỉ tác động đến nhóm nút trong cùng container
    const container = button.closest('.chart-type-container');
    if (container) {
        // Bỏ active class từ tất cả các nút trong cùng container
        container.querySelectorAll('.chart-type').forEach(btn => {
            btn.classList.remove('active');
            btn.classList.remove('btn-primary');
            btn.classList.add('btn-outline-primary');
        });
        
        // Thêm active class và style cho nút được chọn
        button.classList.add('active');
        button.classList.remove('btn-outline-primary');
        button.classList.add('btn-primary');
        
        // Thêm hiệu ứng khi click với animation immediate
        button.classList.add('animate__animated', 'animate__pulse', 'animate__faster');
        setTimeout(() => {
            button.classList.remove('animate__animated', 'animate__pulse', 'animate__faster');
        }, 300);
    }
    
    // Lấy loại biểu đồ từ thuộc tính data-type
    const chartType = button.getAttribute('data-type');
    
    // Xác định biểu đồ cần cập nhật
    // Kiểm tra xem nút này thuộc về biểu đồ nào
    let chartInstance;
    let chartCanvas;
    
    if (button.closest('.card').querySelector('#revenueChart')) {
        chartInstance = window.revenueChart;
        chartCanvas = 'revenueChart';
    } else if (button.closest('.card').querySelector('#reportChart')) {
        chartInstance = window.reportChartInstance;
        chartCanvas = 'reportChart';
    }
                
                // Vẽ lại biểu đồ với loại mới nếu biểu đồ đã được khởi tạo
                if (chartInstance) {
                    chartInstance.config.type = chartType;
                    
                    // Điều chỉnh cấu hình dựa trên loại biểu đồ
                    if (chartType === 'pie' || chartType === 'doughnut') {
                        // Nếu là biểu đồ tròn hoặc vòng
                        const colors = [];
                        for (let i = 0; i < chartInstance.data.labels.length; i++) {
                            colors.push(getRandomColor());
                        }
                        chartInstance.data.datasets[0].backgroundColor = colors;
                        
                        // Sử dụng hàm applyChartTypeConfig để áp dụng cấu hình phù hợp
                        applyChartTypeConfig(chartInstance, chartType);
                    } else {
                        // Đối với biểu đồ khác như line, bar
                        const ctx = document.getElementById(chartCanvas).getContext('2d');
                        const gradientFill = ctx.createLinearGradient(0, 0, 0, 400);
                        
                        if (chartType === 'line') {
                            // Gradient cho biểu đồ đường
                            gradientFill.addColorStop(0, 'rgba(75, 192, 192, 0.8)');
                            gradientFill.addColorStop(1, 'rgba(75, 192, 192, 0.1)');
                            chartInstance.data.datasets[0].borderColor = 'rgba(75, 192, 192, 1)';
                            chartInstance.data.datasets[0].tension = 0.4; // Làm mịn đường
                            chartInstance.data.datasets[0].pointBackgroundColor = 'rgba(75, 192, 192, 1)';
                        } else if (chartType === 'bar') {
                            // Gradient cho biểu đồ cột
                            gradientFill.addColorStop(0, 'rgba(54, 162, 235, 0.8)');
                            gradientFill.addColorStop(1, 'rgba(54, 162, 235, 0.1)');
                            chartInstance.data.datasets[0].borderColor = 'rgba(54, 162, 235, 1)';
                        }
                        
                        chartInstance.data.datasets[0].backgroundColor = gradientFill;
                        
                        // Hiển thị scales cho biểu đồ line và bar
                        chartInstance.options.scales.x.display = true;
                        chartInstance.options.scales.y.display = true;
                    }
                    
                    // Thêm transition trước khi cập nhật biểu đồ
                    const chartContainer = button.closest('.card').querySelector('.chart-container');
                    if (chartContainer) {
                        chartContainer.style.transition = 'opacity 0.3s ease';
                        chartContainer.style.opacity = '0.6';
                        
                        // Delay nhẹ để người dùng thấy hiệu ứng chuyển đổi
                        setTimeout(() => {
                            // Đảm bảo cấu hình được áp dụng đúng trước khi cập nhật
                            applyChartTypeConfig(chartInstance, chartInstance.config.type);
                            
                            // Cập nhật biểu đồ với hiệu ứng mượt mà
                            chartInstance.update({
                                duration: 800,
                                easing: 'easeOutQuart'
                            });
                            
                            // Hiệu ứng fade in mượt mà
                            chartContainer.style.opacity = '1';
                            chartContainer.classList.add('animate__animated', 'animate__fadeIn', 'animate__faster');
                            
                            setTimeout(() => {
                                chartContainer.classList.remove('animate__animated', 'animate__fadeIn', 'animate__faster');
                            }, 500);
                        }, 100);
                    } else {
                        // Nếu không tìm thấy container, vẫn cập nhật biểu đồ
                        chartInstance.update();
                    }
                }
}

/**
 * Định dạng ngày tháng cho hiển thị
 */
function formatDisplayDate(date) {
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return day + '/' + month + '/' + year;
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
    if (typeof ScrollReveal === 'function') {
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
}

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