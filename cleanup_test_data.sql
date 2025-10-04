-- ===================================================================
-- SCRIPT XÓA DỮ LIỆU TEST - CLEANUP
-- Sử dụng khi muốn reset về trạng thái ban đầu
-- ===================================================================

USE WebMyPham;
GO

PRINT 'Bắt đầu xóa dữ liệu test...';

-- Xóa comments (không có FK dependencies)
DELETE FROM comments WHERE comment_id > 2;
PRINT 'Đã xóa comments test';

-- Xóa favorites (không có FK dependencies) 
DELETE FROM favorites;
PRINT 'Đã xóa favorites test';

-- Xóa order_details trước (có FK đến orders)
DELETE FROM order_details WHERE order_id > 2;
PRINT 'Đã xóa order_details test';

-- Xóa orders (sau khi đã xóa order_details)
DELETE FROM orders WHERE order_id > 2;
PRINT 'Đã xóa orders test';

-- Xóa products test (giữ lại 3 sản phẩm gốc)
DELETE FROM products WHERE product_id > 3;
PRINT 'Đã xóa products test';

-- Reset lại expiry_date cho sản phẩm gốc về giá trị xa
UPDATE products SET expiry_date = '2028-01-15' WHERE product_id = 1; -- Son đỏ Ruby
UPDATE products SET expiry_date = '2027-03-20' WHERE product_id = 2; -- Kem dưỡng ban đêm  
UPDATE products SET expiry_date = '2030-05-10' WHERE product_id = 3; -- Nước hoa Rose
PRINT 'Đã reset expiry_date cho sản phẩm gốc';

-- Reset favorite flag cho sản phẩm gốc
UPDATE products SET favorite = 0 WHERE product_id IN (1, 2, 3);
PRINT 'Đã reset favorite flag';

-- Reset IDENTITY nếu cần (tùy chọn)
-- DBCC CHECKIDENT ('orders', RESEED, 2);  
-- DBCC CHECKIDENT ('products', RESEED, 3);

PRINT 'Hoàn thành cleanup! Database đã được reset về trạng thái ban đầu.';

-- Kiểm tra kết quả
SELECT 'Products count' as Info, COUNT(*) as Value FROM products
UNION ALL
SELECT 'Orders count' as Info, COUNT(*) as Value FROM orders  
UNION ALL
SELECT 'Order_details count' as Info, COUNT(*) as Value FROM order_details
UNION ALL
SELECT 'Favorites count' as Info, COUNT(*) as Value FROM favorites
UNION ALL 
SELECT 'Comments count' as Info, COUNT(*) as Value FROM comments;

GO