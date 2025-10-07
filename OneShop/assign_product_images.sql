-- Gán tên file ảnh vào cột product_image cho sản phẩm (SQL Server)
-- Chạy sau khi đã copy ảnh vào thư mục OneShop/upload/images
-- và dữ liệu sản phẩm đã tồn tại

USE WebMyPham;
GO

-- Tham chiếu các tên file có sẵn trong OneShop/upload/images
-- Son (5 ảnh)
DECLARE @img_son_1 NVARCHAR(255) = N'SonChanelCam.png';
DECLARE @img_son_2 NVARCHAR(255) = N'SonChanelDo.png';
DECLARE @img_son_3 NVARCHAR(255) = N'SonChannelDoHong.png';
DECLARE @img_son_4 NVARCHAR(255) = N'SonYSLDoCam.jpg';
DECLARE @img_son_5 NVARCHAR(255) = N'SonYSLDoTuoi.jpg';

-- Kem (5 ảnh)
DECLARE @img_kem_1 NVARCHAR(255) = N'KemOlay.png';
DECLARE @img_kem_2 NVARCHAR(255) = N'kemCeraVe.png';
DECLARE @img_kem_3 NVARCHAR(255) = N'kemLaRoche.png';
DECLARE @img_kem_4 NVARCHAR(255) = N'kemEucerin.png';
DECLARE @img_kem_5 NVARCHAR(255) = N'kemSebamed.png';

-- 1) Cập nhật ảnh cho các sản phẩm SON (ưu tiên tên có chứa 'son')
;WITH lipstick AS (
    SELECT TOP 5 p.product_id
    FROM products p
    WHERE p.status = 1 AND p.product_name LIKE N'%son%'
    ORDER BY p.entered_date DESC, p.product_id DESC
)
UPDATE p
SET p.product_image = v.img
FROM (
    SELECT product_id, ROW_NUMBER() OVER (ORDER BY product_id) AS rn
    FROM lipstick
) x
JOIN products p ON p.product_id = x.product_id
JOIN (
    SELECT 1 AS rn, @img_son_1 AS img UNION ALL
    SELECT 2, @img_son_2 UNION ALL
    SELECT 3, @img_son_3 UNION ALL
    SELECT 4, @img_son_4 UNION ALL
    SELECT 5, @img_son_5
) v ON v.rn = x.rn;

-- 2) Cập nhật ảnh cho các sản phẩm KEM DƯỠNG (ưu tiên tên có chứa 'kem' hoặc 'dưỡng')
;WITH cream AS (
    SELECT TOP 5 p.product_id
    FROM products p
    WHERE p.status = 1 AND (p.product_name LIKE N'%kem%' OR p.product_name LIKE N'%dưỡng%' OR p.product_name LIKE N'%duong%')
    ORDER BY p.entered_date DESC, p.product_id DESC
)
UPDATE p
SET p.product_image = v.img
FROM (
    SELECT product_id, ROW_NUMBER() OVER (ORDER BY product_id) AS rn
    FROM cream
) x
JOIN products p ON p.product_id = x.product_id
JOIN (
    SELECT 1 AS rn, @img_kem_1 AS img UNION ALL
    SELECT 2, @img_kem_2 UNION ALL
    SELECT 3, @img_kem_3 UNION ALL
    SELECT 4, @img_kem_4 UNION ALL
    SELECT 5, @img_kem_5
) v ON v.rn = x.rn;

-- 3) Cập nhật fallback: nếu vẫn chưa có ảnh, gán theo loại gần đúng
UPDATE p SET p.product_image = @img_son_1
WHERE p.status = 1 AND p.product_image IS NULL AND p.product_name LIKE N'%son%';

UPDATE p SET p.product_image = @img_kem_1
WHERE p.status = 1 AND p.product_image IS NULL AND (p.product_name LIKE N'%kem%' OR p.product_name LIKE N'%dưỡng%' OR p.product_name LIKE N'%duong%');

PRINT 'Đã gán tên ảnh cho sản phẩm Son và Kem dưỡng (tối đa 5 mục mỗi nhóm).';
GO


