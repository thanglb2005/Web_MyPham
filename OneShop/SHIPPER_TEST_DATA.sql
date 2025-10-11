-- =====================================================
-- SQL SCRIPT ĐỂ TEST CHỨC NĂNG SHIPPER
-- Tạo dữ liệu đơn hàng thật để test các tính năng:
-- 1. Nhận đơn hàng mới (CONFIRMED, chưa có shipper)
-- 2. Quản lý đơn hàng đang giao (SHIPPING)
-- 3. Thống kê đơn hàng đã giao (DELIVERED)
-- =====================================================

USE WebMyPham;
GO

-- =====================================================
-- PHẦN 1: TẠO ĐỢN HÀNG CHỜ NHẬN (CONFIRMED, chưa có shipper)
-- =====================================================

-- Đơn hàng 1: Khách hàng ở Quận 1, HCM
INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, note, status, payment_method, total_amount, order_date, shipper_id)
VALUES 
(1, N'Nguyễn Thị Mai Anh', 'maianh@gmail.com', '0901234567', N'123 Nguyễn Huệ, Q1, TP.HCM', N'Giao giờ hành chính', 'CONFIRMED', 'COD', 1192500, '2025-10-11 09:30:00', NULL);
DECLARE @order1 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
VALUES 
(@order1, 1, N'Son Chanel Rouge Allure Velvet', 850000, 1, 765000),
(@order1, 5, N'Kem Dưỡng Olay Regenerist', 450000, 1, 427500);

-- Đơn hàng 2
INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, note, status, payment_method, total_amount, order_date, shipper_id)
VALUES 
(2, N'Trần Văn Bình', 'vanbinhtran@gmail.com', '0912345678', N'456 Võ Văn Tần, Q3, TP.HCM', N'Gọi trước 15 phút', 'CONFIRMED', 'MOMO', 890000, '2025-10-11 10:15:00', NULL);
DECLARE @order2 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
VALUES (@order2, 10, N'Serum La Roche-Posay Hyalu B5', 890000, 1, 890000);

-- Đơn hàng 3
INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, status, payment_method, total_amount, order_date, shipper_id)
VALUES 
(3, N'Lê Thị Hương', 'huongle123@gmail.com', '0923456789', N'789 Đinh Bộ Lĩnh, Q.Bình Thạnh, TP.HCM', 'CONFIRMED', 'COD', 2930000, '2025-10-11 11:20:00', NULL);
DECLARE @order3 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
VALUES 
(@order3, 1, N'Son Chanel Rouge Allure Velvet', 850000, 2, 1530000),
(@order3, 15, N'Nước Hoa Chanel No.5', 3200000, 1, 2400000);

-- Đơn hàng 4
INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, note, status, payment_method, total_amount, order_date, shipper_id)
VALUES 
(1, N'Phạm Minh Tuấn', 'minhtuan.pham@gmail.com', '0934567890', N'101 Nguyễn Hữu Thọ, Q7, TP.HCM', N'Để ở bảo vệ nếu không có người', 'CONFIRMED', 'BANK_TRANSFER', 1680000, '2025-10-11 14:00:00', NULL);
DECLARE @order4 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
VALUES (@order4, 8, N'Kem Chống Nắng Anessa', 420000, 4, 1680000);

-- =====================================================
-- PHẦN 2: ĐƠN HÀNG ĐANG GIAO (SHIPPING)
-- =====================================================

INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, note, status, payment_method, total_amount, order_date, shipped_date, shipper_id)
VALUES 
(2, N'Võ Thị Lan', 'lanvo@gmail.com', '0945678901', N'234 Lê Văn Sỹ, Q.Tân Bình, TP.HCM', N'Giao buổi sáng', 'SHIPPING', 'COD', 1402500, '2025-10-10 08:00:00', '2025-10-10 09:30:00', 8);
DECLARE @order5 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
VALUES 
(@order5, 3, N'Son YSL Rouge Pur Couture', 950000, 1, 902500),
(@order5, 12, N'Mặt Nạ Innisfree My Real Squeeze', 25000, 20, 500000);

INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, status, payment_method, total_amount, order_date, shipped_date, shipper_id)
VALUES 
(3, N'Đặng Quốc Cường', 'cuongdang@gmail.com', '0956789012', N'567 Cách Mạng Tháng 8, Q3, TP.HCM', 'SHIPPING', 'MOMO', 1980000, '2025-10-11 07:15:00', '2025-10-11 08:00:00', 8);
DECLARE @order6 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
VALUES 
(@order6, 6, N'Sữa Rửa Mặt CeraVe Foaming', 180000, 3, 540000),
(@order6, 9, N'Toner Some By Mi AHA BHA PHA', 320000, 5, 1440000);

INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, note, status, payment_method, total_amount, order_date, shipped_date, shipper_id)
VALUES 
(1, N'Bùi Thị Thu Hà', 'thuha.bui@gmail.com', '0967890123', N'890 Xô Viết Nghệ Tĩnh, Q.Bình Thạnh, TP.HCM', N'Giao sau 18h', 'SHIPPING', 'COD', 2977500, '2025-10-11 09:00:00', '2025-10-11 10:30:00', 8);
DECLARE @order7 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
VALUES 
(@order7, 1, N'Son Chanel Rouge Allure Velvet', 850000, 3, 2167500),
(@order7, 5, N'Kem Dưỡng Olay Regenerist', 450000, 2, 810000);

-- =====================================================
-- PHẦN 3: ĐƠN HÀNG ĐÃ GIAO (DELIVERED) - Tháng 10/2025
-- =====================================================

INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, status, payment_method, total_amount, order_date, shipped_date, delivered_date, shipper_id)
VALUES 
(2, N'Hoàng Văn Nam', 'namhoang@gmail.com', '0978901234', N'123 Pasteur, Q1, TP.HCM', 'DELIVERED', 'COD', 1780000, '2025-10-08 08:00:00', '2025-10-08 09:00:00', '2025-10-08 15:30:00', 8);
DECLARE @order8 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
VALUES 
(@order8, 4, N'Son MAC Retro Matte', 620000, 2, 1240000),
(@order8, 7, N'Phấn Phủ Innisfree No Sebum', 150000, 4, 540000);

INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, note, status, payment_method, total_amount, order_date, shipped_date, delivered_date, shipper_id)
VALUES 
(3, N'Nguyễn Thị Phương', 'phuongnt@gmail.com', '0989012345', N'456 Hai Bà Trưng, Q1, TP.HCM', N'Đã thanh toán', 'DELIVERED', 'BANK_TRANSFER', 2038000, '2025-10-09 10:00:00', '2025-10-09 11:00:00', '2025-10-09 16:45:00', 8);
DECLARE @order9 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
VALUES 
(@order9, 10, N'Serum La Roche-Posay Hyalu B5', 890000, 2, 1691000),
(@order9, 11, N'Tẩy Tế Bào Chết Paula Choice 2% BHA', 720000, 1, 648000);

INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, status, payment_method, total_amount, order_date, shipped_date, delivered_date, shipper_id)
VALUES 
(1, N'Lý Thanh Tùng', 'tunglythanh@gmail.com', '0990123456', N'789 Trần Hưng Đạo, Q1, TP.HCM', 'DELIVERED', 'MOMO', 4005000, '2025-10-10 07:00:00', '2025-10-10 08:30:00', '2025-10-10 17:20:00', 8);
DECLARE @order10 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price)
VALUES 
(@order10, 15, N'Nước Hoa Chanel No.5', 3200000, 1, 2560000),
(@order10, 1, N'Son Chanel Rouge Allure Velvet', 850000, 2, 1445000);

-- =====================================================
-- PHẦN 4: ĐƠN ĐÃ GIAO - Tháng 9/2025 (5 đơn)
-- =====================================================

INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, status, payment_method, total_amount, order_date, shipped_date, delivered_date, shipper_id)
VALUES 
(2, N'Trịnh Văn Đức', 'ductrinhvan@gmail.com', '0901122334', N'100 Điện Biên Phủ, Q1, TP.HCM', 'DELIVERED', 'COD', 1233000, '2025-09-25 09:00:00', '2025-09-25 10:00:00', '2025-09-25 14:30:00', 8);
DECLARE @order11 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price) VALUES (@order11, 5, N'Kem Dưỡng Olay Regenerist', 450000, 2, 855000), (@order11, 8, N'Kem Chống Nắng Anessa', 420000, 1, 378000);

INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, status, payment_method, total_amount, order_date, shipped_date, delivered_date, shipper_id)
VALUES 
(3, N'Phan Thị Mai', 'maiphan@gmail.com', '0912233445', N'200 Nguyễn Văn Cừ, Q5, TP.HCM', 'DELIVERED', 'BANK_TRANSFER', 1955000, '2025-09-22 11:00:00', '2025-09-22 13:00:00', '2025-09-22 18:00:00', 8);
DECLARE @order12 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price) VALUES (@order12, 1, N'Son Chanel Rouge Allure Velvet', 850000, 2, 1530000), (@order12, 12, N'Mặt Nạ Innisfree My Real Squeeze', 25000, 20, 425000);

INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, status, payment_method, total_amount, order_date, shipped_date, delivered_date, shipper_id)
VALUES 
(1, N'Đỗ Minh Quân', 'quandominh@gmail.com', '0923344556', N'300 Lý Thường Kiệt, Q10, TP.HCM', 'DELIVERED', 'COD', 1565000, '2025-09-20 08:30:00', '2025-09-20 09:30:00', '2025-09-20 15:45:00', 8);
DECLARE @order13 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price) VALUES (@order13, 10, N'Serum La Roche-Posay Hyalu B5', 890000, 1, 890000), (@order13, 7, N'Phấn Phủ Innisfree No Sebum', 150000, 5, 675000);

INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, status, payment_method, total_amount, order_date, shipped_date, delivered_date, shipper_id)
VALUES 
(2, N'Vũ Thị Hạnh', 'hanhvu@gmail.com', '0934455667', N'400 Cộng Hòa, Q.Tân Bình, TP.HCM', 'DELIVERED', 'MOMO', 3309000, '2025-09-18 10:00:00', '2025-09-18 11:30:00', '2025-09-18 17:00:00', 8);
DECLARE @order14 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price) VALUES (@order14, 15, N'Nước Hoa Chanel No.5', 3200000, 1, 2720000), (@order14, 4, N'Son MAC Retro Matte', 620000, 1, 589000);

INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, status, payment_method, total_amount, order_date, shipped_date, delivered_date, shipper_id)
VALUES 
(3, N'Lê Văn Hải', 'haile@gmail.com', '0945566778', N'500 Phan Văn Trị, Q.Gò Vấp, TP.HCM', 'DELIVERED', 'COD', 2319000, '2025-09-15 09:00:00', '2025-09-15 10:00:00', '2025-09-15 16:30:00', 8);
DECLARE @order15 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price) VALUES (@order15, 3, N'Son YSL Rouge Pur Couture', 950000, 2, 1710000), (@order15, 9, N'Toner Some By Mi AHA BHA PHA', 320000, 2, 608000);

-- =====================================================
-- PHẦN 5: ĐƠN ĐÃ GIAO - Tháng 8/2025 (3 đơn)
-- =====================================================

INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, status, payment_method, total_amount, order_date, shipped_date, delivered_date, shipper_id)
VALUES 
(1, N'Ngô Thị Lan', 'lanngo@gmail.com', '0956677889', N'600 Âu Cơ, Q.Tân Bình, TP.HCM', 'DELIVERED', 'BANK_TRANSFER', 1350000, '2025-08-28 08:00:00', '2025-08-28 09:00:00', '2025-08-28 14:00:00', 8);
DECLARE @order16 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price) VALUES (@order16, 6, N'Sữa Rửa Mặt CeraVe Foaming', 180000, 5, 900000), (@order16, 12, N'Mặt Nạ Innisfree My Real Squeeze', 25000, 20, 450000);

INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, status, payment_method, total_amount, order_date, shipped_date, delivered_date, shipper_id)
VALUES 
(2, N'Huỳnh Văn Phúc', 'phuchuynhvan@gmail.com', '0967788990', N'700 Hoàng Văn Thụ, Q.Tân Bình, TP.HCM', 'DELIVERED', 'COD', 3137500, '2025-08-20 10:00:00', '2025-08-20 11:00:00', '2025-08-20 16:45:00', 8);
DECLARE @order17 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price) VALUES (@order17, 1, N'Son Chanel Rouge Allure Velvet', 850000, 3, 2417500), (@order17, 11, N'Tẩy Tế Bào Chết Paula Choice 2% BHA', 720000, 1, 720000);

INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, status, payment_method, total_amount, order_date, shipped_date, delivered_date, shipper_id)
VALUES 
(3, N'Châu Thị Nga', 'ngachau@gmail.com', '0978899001', N'800 Ba Tháng Hai, Q10, TP.HCM', 'DELIVERED', 'MOMO', 3307500, '2025-08-15 09:30:00', '2025-08-15 10:30:00', '2025-08-15 17:15:00', 8);
DECLARE @order18 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price) VALUES (@order18, 15, N'Nước Hoa Chanel No.5', 3200000, 1, 2880000), (@order18, 5, N'Kem Dưỡng Olay Regenerist', 450000, 1, 427500);

-- =====================================================
-- PHẦN 6: ĐƠN BỊ HỦY
-- =====================================================

INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, shipping_address, note, status, payment_method, total_amount, order_date, shipper_id)
VALUES 
(2, N'Trần Văn Long', 'longtrvan@gmail.com', '0989900112', N'900 Lê Hồng Phong, Q10, TP.HCM', N'Khách hủy đơn', 'CANCELLED', 'COD', 1240000, '2025-10-05 11:00:00', 8);
DECLARE @order19 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity, total_price) VALUES (@order19, 4, N'Son MAC Retro Matte', 620000, 2, 1240000);

GO

PRINT N'✅ Đã tạo xong 19 đơn hàng test cho Shipper!';
PRINT N'';
PRINT N'📊 THỐNG KÊ:';
PRINT N'  - 4 đơn CONFIRMED (chưa có shipper)';
PRINT N'  - 3 đơn SHIPPING (đang giao)';  
PRINT N'  - 11 đơn DELIVERED (đã giao)';
PRINT N'  - 1 đơn CANCELLED (đã hủy)';
PRINT N'';
PRINT N'🔐 ĐĂNG NHẬP: shipper@mypham.com / 123456';
