-- ===================================================================
-- SCRIPT THÊM DỮ LIỆU TEST HOÀN CHỈNH CHO CÁC KPI CARDS STATISTICS  
-- Phiên bản cuối cùng - Đã test thành công
-- Chạy sau khi đã có database cơ bản với 3 sản phẩm và 2 orders
-- ===================================================================

USE WebMyPham;
GO

-- ===================================================================
-- PHẦN 1: THÊM SẢN PHẨM ĐỂ TEST CÁC TRƯỜNG HỢP KHÁC NHAU
-- ===================================================================

INSERT INTO products(description, discount, entered_date, price, product_image, product_name, quantity, status, category_id, brand_id, manufacture_date, expiry_date, favorite)
VALUES
-- Sản phẩm SẮP HẾT HẠN (expiring soon) - HẾT HẠN TRONG 30 NGÀY
(N'Kem dưỡng ẩm sắp hết hạn', 15, '2025-08-01', 180000, 'kem2.jpg', N'Kem dưỡng ẩm Nivea', 25, 1, 2, 4, '2024-11-01', DATEADD(day, 20, GETDATE()), 1),
(N'Son bóng sắp hết hạn', 20, '2025-07-15', 120000, 'son2.jpg', N'Son bóng LOreal', 8, 1, 1, 3, '2024-10-15', DATEADD(day, 8, GETDATE()), 0),

-- Sản phẩm SẮP HẾT KHOẢNG (low stock) - SỐ LƯỢNG < 10
(N'Toner cân bằng da', 0, '2025-08-20', 280000, 'toner1.jpg', N'Toner Innisfree', 5, 1, 5, 2, '2025-02-01', '2027-02-01', 1),
(N'Mặt nạ giấy dưỡng ẩm', 25, '2025-09-01', 45000, 'mask1.jpg', N'Mặt nạ Innisfree', 3, 1, 6, 2, '2025-01-10', '2026-01-10', 0),

-- Sản phẩm MỚI NHẬP (entered trong 30 ngày)
(N'Kem chống nắng SPF 50', 10, DATEADD(day, -15, GETDATE()), 220000, 'sunscreen1.jpg', N'Kem chống nắng Nivea', 30, 1, 2, 4, '2025-05-01', '2027-08-01', 0),
(N'Son lì lâu trôi', 0, DATEADD(day, -10, GETDATE()), 150000, 'lipstick2.jpg', N'Son lì Maybelline', 20, 1, 1, 3, '2025-03-15', '2026-02-15', 1),
(N'Phấn phủ kiềm dầu', 15, DATEADD(day, -5, GETDATE()), 180000, 'powder1.jpg', N'Phấn phủ Maybelline', 15, 1, 1, 3, '2025-04-01', '2026-07-15', 0),
(N'Tẩy da chết mặt', 20, DATEADD(day, -25, GETDATE()), 95000, 'scrub1.jpg', N'Tẩy da chết Innisfree', 12, 1, 5, 2, '2025-01-15', '2026-08-10', 1),

-- Sản phẩm GIẢM GIÁ (discount > 0) và YÊU THÍCH
(N'Serum Vitamin C dưỡng da', 30, '2025-07-10', 350000, 'serum1.jpg', N'Serum Vitamin C', 18, 1, 5, 2, '2025-03-01', '2027-03-01', 1),
(N'Nước hoa mini size', 25, '2025-08-10', 800000, 'perfume2.jpg', N'Nước hoa L''Oreal Mini', 25, 1, 3, 3, '2025-06-01', '2029-06-01', 1);
GO

-- ===================================================================
-- PHẦN 2: CẬP NHẬT SẢN PHẨM GỐC ĐỂ CÓ NGÀY HẾT HẠN SẮP TỚI
-- ===================================================================

-- Cập nhật sản phẩm gốc để có sản phẩm sắp hết hạn
UPDATE products SET expiry_date = DATEADD(day, 15, GETDATE()) WHERE product_id = 1; -- Son đỏ Ruby
UPDATE products SET expiry_date = DATEADD(day, 25, GETDATE()) WHERE product_id = 2; -- Kem dưỡng ban đêm
GO

-- ===================================================================
-- PHẦN 3: THÊM ĐƠN HÀNG ĐỂ TEST TĂNG TRƯỞNG VÀ SẢN PHẨM BÁN CHẠY  
-- ===================================================================

-- ĐƠN HÀNG THÁNG HIỆN TẠI (THÁNG 10/2025) - 4 đơn
INSERT INTO orders(address, amount, order_date, phone, status, user_id)
VALUES
('Hồ Chí Minh', 750000, '2025-10-01', '0933333333', 2, 3),
('Cần Thơ', 850000, '2025-10-02', '0944444444', 2, 1), 
('Hải Phòng', 620000, '2025-10-03', '0955555555', 2, 2),
('Đà Lạt', 1100000, '2025-10-04', '0966666666', 2, 3);
GO

-- ĐƠN HÀNG THÁNG TRƯỚC (THÁNG 9/2025) - 4 đơn để so sánh tăng trưởng
INSERT INTO orders(address, amount, order_date, phone, status, user_id)
VALUES
('Nha Trang', 450000, '2025-09-15', '0977777777', 2, 1),
('Vũng Tàu', 680000, '2025-09-20', '0988888888', 2, 2),
('Biên Hòa', 520000, '2025-09-25', '0911111111', 2, 3),
('Long An', 480000, '2025-09-28', '0922222222', 2, 1);
GO

-- ===================================================================
-- PHẦN 4: THÊM CHI TIẾT ĐƠN HÀNG (ORDER_DETAILS)
-- Lưu ý: Phải lấy đúng order_id sau khi INSERT orders
-- ===================================================================

-- Lấy order_id của các đơn hàng vừa tạo
DECLARE @order_oct_1 BIGINT = (SELECT TOP 1 order_id FROM orders WHERE order_date = '2025-10-01');
DECLARE @order_oct_2 BIGINT = (SELECT TOP 1 order_id FROM orders WHERE order_date = '2025-10-02');
DECLARE @order_oct_3 BIGINT = (SELECT TOP 1 order_id FROM orders WHERE order_date = '2025-10-03');
DECLARE @order_oct_4 BIGINT = (SELECT TOP 1 order_id FROM orders WHERE order_date = '2025-10-04');

DECLARE @order_sep_1 BIGINT = (SELECT TOP 1 order_id FROM orders WHERE order_date = '2025-09-15');
DECLARE @order_sep_2 BIGINT = (SELECT TOP 1 order_id FROM orders WHERE order_date = '2025-09-20');
DECLARE @order_sep_3 BIGINT = (SELECT TOP 1 order_id FROM orders WHERE order_date = '2025-09-25');
DECLARE @order_sep_4 BIGINT = (SELECT TOP 1 order_id FROM orders WHERE order_date = '2025-09-28');

-- CHI TIẾT ĐƠN HÀNG THÁNG 10/2025 (THÁNG HIỆN TẠI)
INSERT INTO order_details(price, quantity, order_id, product_id)
VALUES
-- Đơn 1 tháng 10: 750k total
(250000, 2, @order_oct_1, 1), -- 2 x Son đỏ Ruby = 500k  
(250000, 1, @order_oct_1, 2), -- 1 x Kem dưỡng ban đêm = 250k

-- Đơn 2 tháng 10: 850k total
(250000, 1, @order_oct_2, 1), -- 1 x Son đỏ Ruby = 250k
(300000, 2, @order_oct_2, 2), -- 2 x Kem dưỡng ban đêm = 600k

-- Đơn 3 tháng 10: 620k total  
(250000, 1, @order_oct_3, 1), -- 1 x Son đỏ Ruby = 250k
(350000, 1, @order_oct_3, 2), -- 1 x Kem dưỡng ban đêm = 370k

-- Đơn 4 tháng 10: 1100k total
(250000, 1, @order_oct_4, 1), -- 1 x Son đỏ Ruby = 250k  
(350000, 1, @order_oct_4, 2), -- 1 x Kem dưỡng ban đêm = 350k
(500000, 1, @order_oct_4, 3), -- 1 x Nước hoa Rose = 500k

-- CHI TIẾT ĐƠN HÀNG THÁNG 9/2025 (THÁNG TRƯỚC - ĐỂ TÍNH TĂNG TRƯỞNG)
(250000, 1, @order_sep_1, 1), -- 1 x Son đỏ Ruby = 250k
(200000, 1, @order_sep_1, 2), -- 1 x Kem dưỡng ban đêm = 200k

(250000, 1, @order_sep_2, 1), -- 1 x Son đỏ Ruby = 250k  
(430000, 1, @order_sep_2, 3), -- 1 x Nước hoa Rose = 430k

(250000, 2, @order_sep_3, 1), -- 2 x Son đỏ Ruby = 500k
(20000, 1, @order_sep_3, 2),  -- 1 x Kem dưỡng ban đêm = 20k

(250000, 1, @order_sep_4, 1), -- 1 x Son đỏ Ruby = 250k
(230000, 1, @order_sep_4, 2); -- 1 x Kem dưỡng ban đêm = 230k
GO

-- ===================================================================  
-- PHẦN 5: THÊM YÊU THÍCH (FAVORITES) - ĐỂ TEST KPI YÊU THÍCH
-- ===================================================================

INSERT INTO favorites(product_id, user_id)
SELECT product_id, user_id FROM (
VALUES
(1, 3), -- user 3 thích Son đỏ Ruby
(2, 1), -- user 1 thích Kem dưỡng ban đêm  
(3, 2), -- user 2 thích Nước hoa Rose
(1, 2), -- user 2 cũng thích Son đỏ Ruby
(2, 3)  -- user 3 cũng thích Kem dưỡng ban đêm
) AS t(product_id, user_id)
WHERE EXISTS (SELECT 1 FROM products p WHERE p.product_id = t.product_id)
AND EXISTS (SELECT 1 FROM [user] u WHERE u.user_id = t.user_id);
GO

-- ===================================================================
-- PHẦN 6: THÊM BÌNH LUẬN VÀ ĐÁNH GIÁ (COMMENTS)
-- ===================================================================

INSERT INTO comments(content, rate_date, rating, order_detail_id, product_id, user_id)
SELECT content, rate_date, rating, order_detail_id, product_id, user_id FROM (
VALUES
(N'Son đỏ này màu rất đẹp và lâu trôi!', DATEADD(day, -7, GETDATE()), 4.5, 1, 1, 1),
(N'Kem dưỡng này làm da mịn màng hơn nhiều!', DATEADD(day, -5, GETDATE()), 5, 2, 2, 2),  
(N'Nước hoa này mùi hương rất thơm và nhẹ nhàng', DATEADD(day, -3, GETDATE()), 4, 3, 3, 3),
(N'Màu son này cực kỳ phù hợp với da tôi', DATEADD(day, -1, GETDATE()), 5, 1, 1, 2),
(N'Kem dưỡng không gây nhờn, thấm nhanh', GETDATE(), 4, 2, 2, 3),
(N'Đã dùng 1 tháng, mùi hương rất thích', DATEADD(day, -10, GETDATE()), 5, 3, 3, 1)
) AS t(content, rate_date, rating, order_detail_id, product_id, user_id)
WHERE EXISTS (SELECT 1 FROM products p WHERE p.product_id = t.product_id)
AND EXISTS (SELECT 1 FROM [user] u WHERE u.user_id = t.user_id);
GO

-- ===================================================================
-- PHẦN 7: CẬP NHẬT CỘT FAVORITE TRONG PRODUCTS (ĐỂ KPI ĐẾM ĐÚNG)
-- ===================================================================

-- Cập nhật cột favorite cho các sản phẩm được yêu thích
UPDATE products SET favorite = 1 WHERE product_id IN (1, 2, 3);
GO

-- ===================================================================
-- PHẦN 8: KIỂM TRA KẾT QUẢ VÀ THỐNG KÊ
-- ===================================================================

PRINT 'Đã thêm dữ liệu test thành công!';
PRINT 'Bây giờ có thể test các KPI cards:';
PRINT '1. Sản phẩm bán chạy: Son đỏ Ruby, Kem dưỡng ban đêm';
PRINT '2. Sản phẩm mới nhập: Có sản phẩm entered trong 30 ngày';  
PRINT '3. Sản phẩm ít bán/chậm: Toner (5 sp), Mặt nạ (3 sp)';
PRINT '4. Sản phẩm tăng trưởng: So sánh tháng 10 vs tháng 9';
PRINT '5. Sản phẩm sắp hết kho: Các sản phẩm có quantity < 10';
PRINT '6. Sản phẩm sắp hết hạn: Có sản phẩm hết hạn trong 30 ngày';
PRINT '7. Sản phẩm yêu thích: Các sản phẩm được đánh dấu favorite';
PRINT '8. Sản phẩm giảm giá: Các sản phẩm có discount > 0';

-- Thống kê tổng quan
SELECT 
    'Tổng sản phẩm' as KPI,
    COUNT(*) as Value
FROM products p
WHERE p.status = 1

UNION ALL

SELECT 
    'Sắp hết kho' as KPI,
    COUNT(*) as Value  
FROM products p
WHERE p.quantity < 10 AND p.status = 1

UNION ALL

SELECT 
    'Sắp hết hạn' as KPI,
    COUNT(*) as Value
FROM products p  
WHERE p.expiry_date BETWEEN CAST(GETDATE() AS DATE) 
    AND CAST(DATEADD(day, 30, GETDATE()) AS DATE)
    AND p.status = 1

UNION ALL

SELECT 
    'Yêu thích' as KPI,
    COUNT(*) as Value
FROM products p
WHERE p.favorite = 1 AND p.status = 1

UNION ALL

SELECT 
    'Có giảm giá' as KPI, 
    COUNT(*) as Value
FROM products p
WHERE p.discount > 0 AND p.status = 1

UNION ALL

SELECT 
    'Mới nhập (30 ngày)' as KPI,
    COUNT(*) as Value  
FROM products p
WHERE p.status = 1 
    AND p.entered_date >= CAST(DATEADD(day, -30, GETDATE()) AS DATE);

PRINT 'Script hoàn thành! Có thể truy cập Statistics Dashboard để xem kết quả.';
GO