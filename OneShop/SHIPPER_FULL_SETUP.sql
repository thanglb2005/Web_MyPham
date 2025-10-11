-- =====================================================
-- SHIPPER FEATURE - FULL SETUP SCRIPT
-- =====================================================
-- Script đầy đủ để thiết lập tính năng Shipper
-- Bao gồm:
--   1. Cập nhật schema cho bảng [user] (shipping_provider)
--   2. Cập nhật schema cho bảng orders (pickup_address, package_type, weight)
--   3. Dữ liệu test cho shipper
-- =====================================================

USE WebMyPham;
GO

PRINT '================================================';
PRINT 'BẮT ĐẦU CÀI ĐẶT TÍNH NĂNG SHIPPER';
PRINT '================================================';
PRINT '';

-- =====================================================
-- PHẦN 1: CẬP NHẬT SCHEMA CHO BẢNG [USER]
-- =====================================================

PRINT '--- PHẦN 1: Cập nhật bảng [user] ---';

-- Thêm cột shipping_provider
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[user]') AND name = 'shipping_provider')
BEGIN
    ALTER TABLE [user] ADD shipping_provider NVARCHAR(50) NULL;
    PRINT '✓ Đã thêm cột shipping_provider vào bảng [user]';
END
ELSE
BEGIN
    PRINT '⚠ Cột shipping_provider đã tồn tại trong bảng [user]';
END
GO

-- Cập nhật shipping_provider cho shipper hiện có
UPDATE [user]
SET shipping_provider = 'GHN'
WHERE user_id IN (
    SELECT ur.user_id 
    FROM users_roles ur
    INNER JOIN role r ON ur.role_id = r.id
    WHERE r.name = 'ROLE_SHIPPER'
);
PRINT '✓ Đã cập nhật shipping_provider = GHN cho các shipper';
GO

-- =====================================================
-- PHẦN 2: CẬP NHẬT SCHEMA CHO BẢNG ORDERS
-- =====================================================

PRINT '';
PRINT '--- PHẦN 2: Cập nhật bảng orders ---';

-- Thêm cột pickup_address (Địa chỉ lấy hàng)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'pickup_address')
BEGIN
    ALTER TABLE orders ADD pickup_address NVARCHAR(500) NULL;
    PRINT '✓ Đã thêm cột pickup_address';
END
ELSE
BEGIN
    PRINT '⚠ Cột pickup_address đã tồn tại';
END
GO

-- Thêm cột package_type (Loại hàng)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'package_type')
BEGIN
    ALTER TABLE orders ADD package_type NVARCHAR(100) NULL;
    PRINT '✓ Đã thêm cột package_type';
END
ELSE
BEGIN
    PRINT '⚠ Cột package_type đã tồn tại';
END
GO

-- Thêm cột weight (Khối lượng)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'weight')
BEGIN
    ALTER TABLE orders ADD weight FLOAT NULL;
    PRINT '✓ Đã thêm cột weight';
END
ELSE
BEGIN
    PRINT '⚠ Cột weight đã tồn tại';
END
GO

-- =====================================================
-- PHẦN 3: DỮ LIỆU TEST
-- =====================================================

PRINT '';
PRINT '--- PHẦN 3: Tạo dữ liệu test ---';

-- =====================================================
-- 3.1: ĐƠN HÀNG CHỜ NHẬN (CONFIRMED, chưa có shipper)
-- =====================================================

PRINT 'Tạo đơn hàng CONFIRMED (chờ shipper nhận)...';

-- Đơn hàng 1: Khách hàng ở Q1, TP.HCM
INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, note, status, payment_method, total_amount, order_date, shipper_id, pickup_address, package_type, weight)
VALUES 
(1, N'Nguyễn Thị Mai Anh', 'maianh@gmail.com', '0901234567', N'123 Nguyễn Huệ, Q1, TP.HCM', N'Giao giờ hành chính', 'CONFIRMED', 'COD', 1192500, '2025-10-11 09:30:00', NULL, N'Cửa hàng OneShop, 234 Lý Thường Kiệt, Q.10, TP.HCM', N'Hàng thường', 1.5);
DECLARE @order1 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
VALUES 
(@order1, 1, N'Son Chanel Rouge Allure Velvet', 850000, 1, 765000),
(@order1, 5, N'Kem Dưỡng Olay Regenerist', 450000, 1, 427500);

-- Đơn hàng 2
INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, note, status, payment_method, total_amount, order_date, shipper_id, pickup_address, package_type, weight)
VALUES 
(2, N'Trần Văn Bình', 'vanbinhtran@gmail.com', '0912345678', N'456 Võ Văn Tần, Q3, TP.HCM', N'Gọi trước 15 phút', 'CONFIRMED', 'MOMO', 890000, '2025-10-11 10:15:00', NULL, N'Kho trung tâm, 567 Âu Cơ, Q.Tân Bình, TP.HCM', N'Hàng dễ vỡ', 2.0);
DECLARE @order2 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
VALUES (@order2, 10, N'Serum La Roche-Posay Hyalu B5', 890000, 1, 890000);

-- Đơn hàng 3
INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, status, payment_method, total_amount, order_date, shipper_id, pickup_address, package_type, weight)
VALUES 
(3, N'Lê Thị Hương', 'huongle123@gmail.com', '0923456789', N'789 Đinh Bộ Lĩnh, Q.Bình Thạnh, TP.HCM', 'CONFIRMED', 'COD', 2930000, '2025-10-11 11:20:00', NULL, N'Cửa hàng OneShop, 234 Lý Thường Kiệt, Q.10, TP.HCM', N'Hàng thường', 3.0);
DECLARE @order3 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
VALUES 
(@order3, 15, N'Kem Chống Nắng Anessa Perfect UV', 650000, 2, 1170000),
(@order3, 20, N'Phấn Phủ Innisfree No Sebum', 220000, 3, 594000),
(@order3, 25, N'Sữa Rửa Mặt CeraVe Foaming', 350000, 3, 945000);

PRINT '✓ Đã tạo 3 đơn hàng CONFIRMED (chưa có shipper)';

-- =====================================================
-- 3.2: ĐƠN HÀNG ĐANG GIAO (SHIPPING, có shipper)
-- =====================================================

PRINT 'Tạo đơn hàng SHIPPING (đang giao)...';

-- Lấy user_id của shipper (giả sử là user_id = 2)
DECLARE @shipperId BIGINT;
SELECT TOP 1 @shipperId = ur.user_id 
FROM users_roles ur
INNER JOIN role r ON ur.role_id = r.id
WHERE r.name = 'ROLE_SHIPPER';

IF @shipperId IS NULL
BEGIN
    PRINT '⚠ Không tìm thấy user với role SHIPPER. Vui lòng tạo user shipper trước.';
    PRINT '⚠ Bỏ qua phần tạo đơn hàng SHIPPING và DELIVERED.';
END
ELSE
BEGIN
    -- Đơn hàng 4: Đang giao
    INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, note, status, payment_method, total_amount, order_date, shipped_date, shipper_id, pickup_address, package_type, weight)
    VALUES 
    (1, N'Phạm Minh Tuấn', 'tuanpham@gmail.com', '0934567890', N'321 Lê Văn Sỹ, Q3, TP.HCM', N'Giao sau 17h', 'SHIPPING', 'COD', 1560000, '2025-10-10 14:00:00', '2025-10-11 08:00:00', @shipperId, N'Cửa hàng OneShop, 234 Lý Thường Kiệt, Q.10, TP.HCM', N'Hàng thường', 2.5);
    DECLARE @order4 BIGINT = SCOPE_IDENTITY();
    INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
    VALUES 
    (@order4, 8, N'Kem Dưỡng Eucerin UreaRepair', 520000, 2, 936000),
    (@order4, 12, N'Toner Some By Mi AHA BHA PHA', 350000, 2, 624000);

    -- Đơn hàng 5: Đang giao
    INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, status, payment_method, total_amount, order_date, shipped_date, shipper_id, pickup_address, package_type, weight)
    VALUES 
    (2, N'Hoàng Thu Hà', 'hathoang@yahoo.com', '0945678901', N'555 Phan Xích Long, Q.Phú Nhuận, TP.HCM', 'SHIPPING', 'BANK_TRANSFER', 780000, '2025-10-10 16:30:00', '2025-10-11 09:00:00', @shipperId, N'Kho trung tâm, 567 Âu Cơ, Q.Tân Bình, TP.HCM', N'Hàng dễ vỡ', 1.8);
    DECLARE @order5 BIGINT = SCOPE_IDENTITY();
    INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
    VALUES (@order5, 18, N'Mặt Nạ Mediheal N.M.F Aquaring', 390000, 2, 780000);

    PRINT '✓ Đã tạo 2 đơn hàng SHIPPING (đang giao)';

    -- =====================================================
    -- 3.3: ĐƠN HÀNG ĐÃ GIAO (DELIVERED)
    -- =====================================================

    PRINT 'Tạo đơn hàng DELIVERED (đã giao - cho thống kê)...';

    -- Đơn hàng tháng 10/2025
    INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, status, payment_method, total_amount, order_date, shipped_date, delivered_date, shipper_id, pickup_address, package_type, weight)
    VALUES 
    (1, N'Vũ Đức Anh', 'anhvu@gmail.com', '0956789012', N'888 Cách Mạng Tháng 8, Q.Tân Bình, TP.HCM', 'DELIVERED', 'COD', 1250000, '2025-10-09 10:00:00', '2025-10-09 14:00:00', '2025-10-09 18:30:00', @shipperId, N'Cửa hàng OneShop, 234 Lý Thường Kiệt, Q.10, TP.HCM', N'Hàng thường', 2.0);
    DECLARE @order6 BIGINT = SCOPE_IDENTITY();
    INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
    VALUES (@order6, 3, N'Cushion Laneige Neo Cushion', 625000, 2, 1250000);

    INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, status, payment_method, total_amount, order_date, shipped_date, delivered_date, shipper_id, pickup_address, package_type, weight)
    VALUES 
    (2, N'Đỗ Minh Châu', 'chaudo@outlook.com', '0967890123', N'222 Hoàng Văn Thụ, Q.Tân Bình, TP.HCM', 'DELIVERED', 'MOMO', 950000, '2025-10-08 11:00:00', '2025-10-08 15:00:00', '2025-10-08 19:00:00', @shipperId, N'Kho trung tâm, 567 Âu Cơ, Q.Tân Bình, TP.HCM', N'Hàng thường', 1.5);
    DECLARE @order7 BIGINT = SCOPE_IDENTITY();
    INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
    VALUES (@order7, 7, N'Sữa Dưỡng Thể Vaseline', 190000, 5, 950000);

    -- Đơn hàng tháng 9/2025
    INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, status, payment_method, total_amount, order_date, shipped_date, delivered_date, shipper_id, pickup_address, package_type, weight)
    VALUES 
    (3, N'Bùi Thanh Tâm', 'tambui@gmail.com', '0978901234', N'111 Trường Chinh, Q.Tân Bình, TP.HCM', 'DELIVERED', 'COD', 1680000, '2025-09-25 09:00:00', '2025-09-25 13:00:00', '2025-09-25 17:30:00', @shipperId, N'Cửa hàng OneShop, 234 Lý Thường Kiệt, Q.10, TP.HCM', N'Hàng dễ vỡ', 3.5);
    DECLARE @order8 BIGINT = SCOPE_IDENTITY();
    INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
    VALUES 
    (@order8, 14, N'Tinh Chất Estée Lauder Advanced Night Repair', 2800000, 1, 2520000),
    (@order8, 9, N'Xịt Khoáng Avène Thermal Spring Water', 220000, 3, 594000);

    INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, status, payment_method, total_amount, order_date, shipped_date, delivered_date, shipper_id, pickup_address, package_type, weight)
    VALUES 
    (1, N'Ngô Quốc Khánh', 'khanhqngo@hotmail.com', '0989012345', N'444 Nguyễn Thái Sơn, Q.Gò Vấp, TP.HCM', 'DELIVERED', 'VIETQR', 2150000, '2025-09-20 10:30:00', '2025-09-20 14:30:00', '2025-09-20 18:00:00', @shipperId, N'Cửa hàng OneShop, 234 Lý Thường Kiệt, Q.10, TP.HCM', N'Hàng thường', 2.8);
    DECLARE @order9 BIGINT = SCOPE_IDENTITY();
    INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
    VALUES (@order9, 11, N'Kem Chống Nắng La Roche-Posay Anthelios', 430000, 5, 2150000);

    -- Đơn hàng tháng 8/2025
    INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, status, payment_method, total_amount, order_date, shipped_date, delivered_date, shipper_id, pickup_address, package_type, weight)
    VALUES 
    (2, N'Trịnh Văn Long', 'longtrinh@gmail.com', '0990123456', N'777 Lê Hồng Phong, Q.10, TP.HCM', 'DELIVERED', 'COD', 1920000, '2025-08-15 08:00:00', '2025-08-15 12:00:00', '2025-08-15 16:30:00', @shipperId, N'Kho trung tâm, 567 Âu Cơ, Q.Tân Bình, TP.HCM', N'Hàng thường', 2.2);
    DECLARE @order10 BIGINT = SCOPE_IDENTITY();
    INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
    VALUES (@order10, 16, N'Sữa Rửa Mặt Senka Perfect Whip', 120000, 16, 1920000);

    INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, status, payment_method, total_amount, order_date, shipped_date, delivered_date, shipper_id, pickup_address, package_type, weight)
    VALUES 
    (3, N'Lý Minh Hiếu', 'hieuly@yahoo.com', '0901111222', N'999 Xô Viết Nghệ Tĩnh, Q.Bình Thạnh, TP.HCM', 'DELIVERED', 'MOMO', 3250000, '2025-08-10 09:30:00', '2025-08-10 13:30:00', '2025-08-10 17:45:00', @shipperId, N'Cửa hàng OneShop, 234 Lý Thường Kiệt, Q.10, TP.HCM', N'Hàng dễ vỡ', 4.0);
    DECLARE @order11 BIGINT = SCOPE_IDENTITY();
    INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
    VALUES 
    (@order11, 4, N'Phấn Nước Missha M Magic Cushion', 390000, 3, 1053000),
    (@order11, 6, N'Son Kem 3CE Velvet Lip Tint', 350000, 4, 1260000),
    (@order11, 13, N'Mặt Nạ Innisfree My Real Squeeze', 25000, 20, 450000);

    PRINT '✓ Đã tạo 6 đơn hàng DELIVERED (đã giao thành công)';
    PRINT '  - Tháng 10/2025: 2 đơn';
    PRINT '  - Tháng 9/2025: 2 đơn';
    PRINT '  - Tháng 8/2025: 2 đơn';
END
GO

-- =====================================================
-- PHẦN 4: KIỂM TRA KẾT QUẢ
-- =====================================================

PRINT '';
PRINT '================================================';
PRINT 'KIỂM TRA KẾT QUẢ';
PRINT '================================================';
PRINT '';

-- Kiểm tra user shipper
PRINT '--- User Shipper ---';
SELECT 
    u.user_id AS 'ID',
    u.name AS 'Tên Shipper',
    u.email AS 'Email',
    u.shipping_provider AS 'Nhà vận chuyển',
    r.name AS 'Role'
FROM [user] u
INNER JOIN users_roles ur ON u.user_id = ur.user_id
INNER JOIN role r ON ur.role_id = r.id
WHERE r.name = 'ROLE_SHIPPER';

PRINT '';
PRINT '--- Thống kê đơn hàng ---';
SELECT 
    status AS 'Trạng thái',
    COUNT(*) AS 'Số lượng',
    SUM(total_amount) AS 'Tổng giá trị'
FROM orders
WHERE shipper_id IS NOT NULL OR status = 'CONFIRMED'
GROUP BY status
ORDER BY 
    CASE status
        WHEN 'CONFIRMED' THEN 1
        WHEN 'SHIPPING' THEN 2
        WHEN 'DELIVERED' THEN 3
        ELSE 4
    END;

PRINT '';
PRINT '--- Mẫu đơn hàng chi tiết ---';
SELECT TOP 5
    order_id AS 'Mã đơn',
    customer_name AS 'Khách hàng',
    pickup_address AS 'Địa chỉ lấy',
    shipping_address AS 'Địa chỉ giao',
    package_type AS 'Loại hàng',
    weight AS 'KL(kg)',
    status AS 'Trạng thái',
    total_amount AS 'Giá trị'
FROM orders
WHERE shipper_id IS NOT NULL OR status = 'CONFIRMED'
ORDER BY order_date DESC;

PRINT '';
PRINT '================================================';
PRINT 'HOÀN TẤT CÀI ĐẶT TÍNH NĂNG SHIPPER!';
PRINT '================================================';
PRINT '';
PRINT 'ĐÃ HOÀN THÀNH:';
PRINT '  ✓ Cập nhật schema bảng [user]';
PRINT '  ✓ Cập nhật schema bảng orders';
PRINT '  ✓ Tạo dữ liệu test đầy đủ';
PRINT '';
PRINT 'TÍNH NĂNG SHIPPER BAO GỒM:';
PRINT '  ✓ Quản lý đơn hàng được phân công';
PRINT '  ✓ Nhận đơn hàng mới (CONFIRMED)';
PRINT '  ✓ Cập nhật trạng thái (SHIPPING → DELIVERED)';
PRINT '  ✓ Xem chi tiết đơn hàng (địa chỉ lấy/giao, loại hàng, khối lượng)';
PRINT '  ✓ Thống kê theo tháng và trạng thái';
PRINT '  ✓ Logo nhà vận chuyển';
PRINT '';
PRINT 'RESTART SERVER ĐỂ ÁP DỤNG THAY ĐỔI!';
PRINT '================================================';
GO

