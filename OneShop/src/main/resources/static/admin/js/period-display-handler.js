/**
 * Dynamic Period Display Handler
 * 
 * Chịu trách nhiệm cập nhật thông tin hiển thị kỳ (tháng/quý/năm) dựa trên 
 * bộ lọc người dùng chọn trong giao diện thống kê doanh thu
 */
(function() {
    // Chờ DOM tải xong
    document.addEventListener('DOMContentLoaded', function() {
        console.log('Period Display Handler initialized');
        
        // Lấy các phần tử bộ lọc
        const yearSelect = document.getElementById('yearSelect');
        const monthSelect = document.getElementById('monthSelect');
        const quarterSelect = document.getElementById('quarterSelect');
        const viewByButtons = document.querySelectorAll('.view-by-btn');
        
        // Lấy các phần tử hiển thị
        const selectedPeriodTitle = document.getElementById('selectedPeriodTitle');
        
        // Biến theo dõi loại bộ lọc hiện tại
        let currentFilterType = 'month'; // Mặc định xem theo tháng
        
        // Hàm cập nhật tiêu đề kỳ và thẻ hiển thị
        function updatePeriodDisplay() {
            if (!yearSelect || !selectedPeriodTitle) {
                console.error('Required elements not found');
                return;
            }
            
            const year = yearSelect.value;
            let periodText = '';
            
            // Xác định tiêu đề dựa vào loại bộ lọc
            if (currentFilterType === 'month' && monthSelect) {
                const month = monthSelect.value;
                periodText = `Tháng ${month}/${year}`;
            } else if (currentFilterType === 'quarter' && quarterSelect) {
                const quarter = quarterSelect.value;
                periodText = `Quý ${quarter}/${year}`;
            } else if (currentFilterType === 'year') {
                periodText = `Năm ${year}`;
            }
            
            // Cập nhật tiêu đề
            selectedPeriodTitle.textContent = periodText;
            
            // Thêm class cho CSS dựa theo loại
            selectedPeriodTitle.classList.remove('month-period', 'quarter-period', 'year-period');
            selectedPeriodTitle.classList.add(currentFilterType + '-period');
            
            // Thêm hiệu ứng animation
            selectedPeriodTitle.classList.remove('period-update-animation');
            void selectedPeriodTitle.offsetWidth; // Trick để reset animation
            selectedPeriodTitle.classList.add('period-update-animation');
            
            console.log('Period title updated to:', periodText);
            
            // Kích hoạt sự kiện để các thành phần khác có thể phản ứng
            const event = new CustomEvent('periodChanged', {
                detail: {
                    type: currentFilterType,
                    year: year,
                    month: currentFilterType === 'month' ? monthSelect.value : null,
                    quarter: currentFilterType === 'quarter' ? quarterSelect.value : null,
                    displayText: periodText
                }
            });
            document.dispatchEvent(event);
        }
        
        // Thêm người nghe sự kiện cho các phần tử bộ lọc
        if (yearSelect) {
            yearSelect.addEventListener('change', updatePeriodDisplay);
        }
        
        if (monthSelect) {
            monthSelect.addEventListener('change', updatePeriodDisplay);
        }
        
        if (quarterSelect) {
            quarterSelect.addEventListener('change', updatePeriodDisplay);
        }
        
        // Xử lý sự kiện khi người dùng chuyển đổi giữa các chế độ xem
        viewByButtons.forEach(button => {
            button.addEventListener('click', function() {
                // Lấy loại từ thuộc tính data-type
                const type = this.getAttribute('data-type');
                if (!type) return;
                
                // Cập nhật loại bộ lọc hiện tại
                currentFilterType = type;
                
                // Hiển thị/ẩn các bộ chọn phù hợp
                if (type === 'month') {
                    document.getElementById('monthSelect').parentElement.style.display = 'block';
                    document.getElementById('quarterSelector').style.display = 'none';
                } else if (type === 'quarter') {
                    document.getElementById('monthSelect').parentElement.style.display = 'none';
                    document.getElementById('quarterSelector').style.display = 'block';
                } else if (type === 'year') {
                    document.getElementById('monthSelect').parentElement.style.display = 'none';
                    document.getElementById('quarterSelector').style.display = 'none';
                }
                
                // Cập nhật hiển thị sau khi thay đổi loại
                updatePeriodDisplay();
            });
        });
        
        // Lắng nghe sự kiện từ mã JavaScript chính
        document.addEventListener('filterChanged', function(event) {
            console.log('Filter changed event received:', event.detail);
            
            // Cập nhật loại bộ lọc hiện tại từ sự kiện
            if (event.detail && event.detail.type) {
                currentFilterType = event.detail.type;
                
                // Cập nhật hiển thị sau khi nhận sự kiện
                updatePeriodDisplay();
            }
        });
        
        // Cập nhật hiển thị ban đầu
        setTimeout(updatePeriodDisplay, 100);
    });
})();