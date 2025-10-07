/**
 * Script debug để kiểm tra chức năng xuất file
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('=== DEBUGGING EXPORT FUNCTIONALITY ===');
    
    // Kiểm tra các thư viện cần thiết
    console.log('XLSX library loaded:', typeof XLSX !== 'undefined');
    console.log('jsPDF library loaded:', typeof window.jspdf !== 'undefined');
    console.log('AutoTable plugin loaded:', typeof window.jspdf?.jsPDF?.API?.autoTable !== 'undefined');
    
    // Kiểm tra các phần tử HTML
    const exportExcelBtn = document.getElementById('export-excel');
    const exportPdfBtn = document.getElementById('export-pdf');
    const reportTable = document.getElementById('report-table');
    
    console.log('Export Excel button found:', exportExcelBtn !== null);
    console.log('Export PDF button found:', exportPdfBtn !== null);
    console.log('Report table found:', reportTable !== null);
    
    // Thêm event listeners để debug
    if (exportExcelBtn) {
        exportExcelBtn.addEventListener('click', function(e) {
            console.log('Excel button clicked!');
            e.preventDefault();
            
            try {
                exportToExcel();
            } catch (error) {
                console.error('Error in Excel export:', error);
                alert('Lỗi xuất Excel: ' + error.message);
            }
        });
    }
    
    if (exportPdfBtn) {
        exportPdfBtn.addEventListener('click', function(e) {
            console.log('PDF button clicked!');
            e.preventDefault();
            
            try {
                exportToPDF();
            } catch (error) {
                console.error('Error in PDF export:', error);
                alert('Lỗi xuất PDF: ' + error.message);
            }
        });
    }
    
    // Test các chức năng cơ bản
    setTimeout(function() {
        console.log('=== TESTING BASIC FUNCTIONS ===');
        console.log('formatDate function exists:', typeof formatDate === 'function');
        console.log('formatDisplayDate function exists:', typeof formatDisplayDate === 'function');
        
        if (typeof formatDate === 'function') {
            try {
                const testDate = formatDate(new Date());
                console.log('formatDate test result:', testDate);
            } catch (e) {
                console.error('formatDate test failed:', e);
            }
        }
        
        if (typeof formatDisplayDate === 'function') {
            try {
                const testDisplayDate = formatDisplayDate(new Date());
                console.log('formatDisplayDate test result:', testDisplayDate);
            } catch (e) {
                console.error('formatDisplayDate test failed:', e);
            }
        }
    }, 1000);
});