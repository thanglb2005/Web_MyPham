USE WebMyPham;
GO
insert into categories(

INSERT INTO brands(brand_name, brand_image, description, origin, status) 
VALUES
-- 1. Simple
(N'Simple', 'simple.png', N'Thương hiệu Anh Quốc nổi tiếng với dòng skincare lành tính, không cồn & hương liệu', N'Anh Quốc', 1),

-- 2. Pyunkang Yul
(N'Pyunkang Yul', 'pyunkangyul.png', N'Thương hiệu Hàn Quốc thuộc viện Y học cổ truyền Pyunkang, nổi bật dưỡng ẩm dịu nhẹ', N'Hàn Quốc', 1),

-- 3. Cocoon
(N'Cocoon', 'cocoon.png', N'Thương hiệu mỹ phẩm thuần chay Việt Nam, chiết xuất từ thiên nhiên, thân thiện môi trường', N'Việt Nam', 1),

-- 4. Bioderma
(N'Bioderma', 'bioderma.png', N'Dược mỹ phẩm Pháp, nổi bật chăm sóc da nhạy cảm và làm sạch dịu nhẹ', N'Pháp', 1);
GO
--Brands mặt nạ
INSERT INTO brands(brand_name, brand_image, description, origin, status)
VALUES
(N'Colorkey', 'colorkey.png', N'Mỹ phẩm Trung Quốc theo xu hướng makeup + skincare', N'Trung Quốc', 1),
(N'Rwine', 'rwine.png', N'Mỹ phẩm Nhật Bản với các dòng mask & dưỡng da', N'Nhật Bản', 1),
(N'Nature Republic', 'nature_republic.png', N'Thương hiệu mỹ phẩm thiên nhiên Hàn Quốc', N'Hàn Quốc', 1),
(N'Saborino', 'saborino.png', N'Mask nhanh buổi sáng từ Nhật Bản', N'Nhật Bản', 1),
(N'Caryophy', 'caryophy.png', N'Mỹ phẩm Hàn Quốc thiên về điều trị mụn & dịu da', N'Hàn Quốc', 1),
(N'Klairs', 'klairs.png', N'Thương hiệu Hàn dịu nhẹ, thân thiện da nhạy cảm', N'Hàn Quốc', 1);
GO
--Brands 
INSERT INTO brands(brand_name, brand_image, description, origin, status)
VALUES
(N'CeraVe', 'cerave.png', N'Dược mỹ phẩm Mỹ, nổi tiếng với ceramide và công thức dịu nhẹ', N'Mỹ', 1),
(N'The Face Shop', 'the_face_shop.png', N'Thương hiệu Hàn Quốc với dòng chiết xuất thiên nhiên', N'Hàn Quốc', 1),
(N'Anua', 'anua.png', N'Mỹ phẩm Hàn sử dụng các thành phần thiên nhiên lành tính', N'Hàn Quốc', 1),
(N'SVR', 'svr.png', N'Dược mỹ phẩm Pháp chuyên cho da dầu / mụn', N'Pháp', 1),
(N'Cetaphil', 'cetaphil.png', N'Dược mỹ phẩm dịu nhẹ cho da nhạy cảm', N'Mỹ', 1),
(N'La Roche-Posay', 'la_roche_posay.png', N'Dược mỹ phẩm cho da nhạy cảm', N'Pháp', 1);
GO


/* =========================
   10 TONER / NƯỚC HOA HỒNG
   ========================= */

INSERT INTO products(
  description, discount, entered_date, price, product_image, product_name,
  quantity, status, category_id, brand_id, manufacture_date, expiry_date, favorite
)
VALUES
-- 1. Simple Kind To Skin Soothing Facial Toner
(N'Toner nhẹ dịu, không cồn & hương liệu, giúp làm sạch và cân bằng pH da nhạy cảm', 0, GETDATE(), 107000,
 N'simple_soothing_toner.png', N'Simple Kind To Skin Soothing Facial Toner 200ml', 120, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Toner'),
 (SELECT brand_id FROM brands WHERE brand_name=N'Simple'),
 DATEFROMPARTS(2025,7,1), DATEFROMPARTS(2027,7,1), 0),

-- 2. Klairs Supple Preparation Facial Toner
(N'Toner Klairs Supple Preparation cân bằng pH, dưỡng ẩm dịu nhẹ cho da nhạy cảm', 0, GETDATE(), 233000,
 N'klairs_supple_toner.png', N'Klairs Supple Preparation Facial Toner 180ml', 90, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Toner'),
 (SELECT brand_id FROM brands WHERE brand_name=N'Klairs'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2027,6,1), 0),

-- 3. Skin1004 Madagascar Centella Toning Toner
(N'Toner chứa rau má Madagascar làm dịu, cấp ẩm và phục hồi da sau mụn', 0, GETDATE(), 312000,
 N'skin1004_centella_toner.png', N'Skin1004 Madagascar Centella Toning Toner 210ml', 100, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Toner'),
 (SELECT brand_id FROM brands WHERE brand_name=N'Skin1004'),
 DATEFROMPARTS(2025,6,15), DATEFROMPARTS(2027,6,15), 0),

-- 4. Some By Mi AHA-BHA-PHA 30 Days Miracle Toner
(N'Toner tẩy tế bào chết nhẹ với AHA-BHA-PHA, hỗ trợ sáng da & giảm mụn trong 30 ngày', 0, GETDATE(), 289000,
 N'somebymi_miracle_toner.png', N'Some By Mi AHA-BHA-PHA 30 Days Miracle Toner 150ml', 130, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Toner'),
 (SELECT brand_id FROM brands WHERE brand_name=N'Some By Mi'),
 DATEFROMPARTS(2025,5,1), DATEFROMPARTS(2027,5,1), 0),

-- 5. Pyunkang Yul Essence Toner
(N'Toner dưỡng ẩm sâu, chiết xuất rễ Hoàng Cầm giúp làm dịu & tăng đàn hồi cho da', 0, GETDATE(), 210000,
 N'pyunkangyul_essence_toner.png', N'Pyunkang Yul Essence Toner 200ml', 120, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Toner'),
 (SELECT brand_id FROM brands WHERE brand_name=N'Pyunkang Yul'),
 DATEFROMPARTS(2025,4,20), DATEFROMPARTS(2027,4,20), 0),

-- 6. Cocoon Sen Hậu Giang Soothing Toner
(N'Toner thuần chay chiết xuất Sen Hậu Giang, cấp ẩm & làm dịu da nhạy cảm', 0, GETDATE(), 173000,
 N'cocoon_sen_toner.png', N'Cocoon Sen Hậu Giang Soothing Toner 140ml', 150, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Toner'),
 (SELECT brand_id FROM brands WHERE brand_name=N'Cocoon'),
 DATEFROMPARTS(2025,7,10), DATEFROMPARTS(2027,7,10), 0),

-- 7. La Roche-Posay Effaclar Clarifying Toner
(N'Toner dược mỹ phẩm chứa BHA & LHA giúp làm sạch sâu, giảm bít tắc lỗ chân lông', 5, GETDATE(), 360000,
 N'larocheposay_effaclar_toner.png', N'La Roche-Posay Effaclar Clarifying Toner 200ml', 80, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Toner'),
 (SELECT brand_id FROM brands WHERE brand_name=N'La Roche-Posay'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2027,6,1), 0),

-- 8. Vichy Normaderm Purifying Pore-Tightening Toner
(N'Toner khoáng núi lửa Vichy giúp se khít lỗ chân lông, hỗ trợ kiềm dầu cho da mụn', 5, GETDATE(), 380000,
 N'vichy_normaderm_toner.png', N'Vichy Normaderm Purifying Pore-Tightening Toner 200ml', 90, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Toner'),
 (SELECT brand_id FROM brands WHERE brand_name=N'Vichy'),
 DATEFROMPARTS(2025,5,1), DATEFROMPARTS(2027,5,1), 0),

-- 9. Hada Labo Gokujyun Hyaluronic Acid Lotion (Toner)
(N'Toner cấp ẩm chuyên sâu với 3 loại Hyaluronic Acid giúp da căng mượt & ẩm mịn', 0, GETDATE(), 245000,
 N'hadalabo_gokujyun_toner.png', N'Hada Labo Gokujyun Hyaluronic Acid Lotion 170ml', 140, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Toner'),
 (SELECT brand_id FROM brands WHERE brand_name=N'Hada Labo'),
 DATEFROMPARTS(2025,5,15), DATEFROMPARTS(2027,5,15), 0),

-- 10. Bioderma Sensibio Tonique
(N'Toner Bioderma Sensibio dịu nhẹ, không cồn, làm mềm & phục hồi cân bằng cho da nhạy cảm', 0, GETDATE(), 320000,
 N'bioderma_sensibio_tonique.png', N'Bioderma Sensibio Tonique 250ml', 85, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Toner'),
 (SELECT brand_id FROM brands WHERE brand_name=N'Bioderma'),
 DATEFROMPARTS(2025,4,10), DATEFROMPARTS(2027,4,10), 0);
GO



/* =================
   10 SỮA RỬA MẶT (FIX)
   ================= */
INSERT INTO products(
  description, discount, entered_date, price, product_image, product_name,
  quantity, status, category_id, brand_id, manufacture_date, expiry_date, favorite
)
VALUES
-- 1. CeraVe Foaming Facial Cleanser
(N'Sữa rửa mặt CeraVe Foaming giúp làm sạch sâu mà không làm khô da', 0, GETDATE(), 280000,
 N'cerave_foaming.png', N'CeraVe Foaming Facial Cleanser 236ml', 100, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Sữa rửa mặt'),
 (SELECT MIN(brand_id)    FROM brands    WHERE brand_name =N'CeraVe'),
 DATEFROMPARTS(2025,5,1), DATEFROMPARTS(2028,5,1), 0),

-- 2. Simple Kind To Skin Refreshing Facial Wash
(N'Sữa rửa mặt Simple làm sạch dịu nhẹ, không gây khô căng, phù hợp da nhạy cảm', 0, GETDATE(), 91000,
 N'simple_refreshing_wash.png', N'Simple Kind To Skin Refreshing Facial Wash 150ml', 150, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Sữa rửa mặt'),
 (SELECT MIN(brand_id)    FROM brands    WHERE brand_name =N'Simple'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2027,6,1), 0),

-- 3. The Face Shop Rice Water Bright Cleanser
(N'Sữa rửa mặt chiết xuất nước gạo giúp làm sáng và làm sạch nhẹ nhàng', 0, GETDATE(), 30000,
 N'tfs_rice_water.png', N'The Face Shop Rice Water Bright Cleanser 150ml', 200, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Sữa rửa mặt'),
 (SELECT MIN(brand_id)    FROM brands    WHERE brand_name =N'The Face Shop'),
 DATEFROMPARTS(2025,7,1), DATEFROMPARTS(2028,7,1), 0),

-- 4. Anua Heartleaf Pore Deep Cleansing Foam
(N'Sữa rửa mặt Anua chứa heartleaf giúp kiểm soát dầu & se khít lỗ chân lông', 0, GETDATE(), 220000,
 N'anua_heartleaf.png', N'Anua Heartleaf Pore Deep Cleansing Foam 150ml', 120, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Sữa rửa mặt'),
 (SELECT MIN(brand_id)    FROM brands    WHERE brand_name =N'Anua'),
 DATEFROMPARTS(2025,6,15), DATEFROMPARTS(2028,6,15), 0),

-- 5. Cocoon Winter Melon Cleanser
(N'Gel rửa mặt Cocoon bí đao thuần chay, dịu nhẹ cho da nhạy cảm', 0, GETDATE(), 105000,
 N'cocoon_winter_melon.png', N'Cocoon Winter Melon Cleanser 140ml', 180, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Sữa rửa mặt'),
 (SELECT MIN(brand_id)    FROM brands    WHERE brand_name =N'Cocoon'),
 DATEFROMPARTS(2025,5,10), DATEFROMPARTS(2027,5,10), 0),

-- 6. SVR Sebiaclear Gel Moussant
(N'Gel rửa mặt SVR làm sạch dầu, giảm mụn nhẹ nhàng', 0, GETDATE(), 180000,
 N'svr_sebiaclear.png', N'SVR Sebiaclear Gel Moussant 200ml', 90, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Sữa rửa mặt'),
 (SELECT MIN(brand_id)    FROM brands    WHERE brand_name =N'SVR'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- 7. Cetaphil Gentle Skin Cleanser
(N'Sữa rửa mặt Cetaphil dịu nhẹ, không tạo bọt mạnh, phù hợp da nhạy cảm', 0, GETDATE(), 150000,
 N'cetaphil_gentle.png', N'Cetaphil Gentle Skin Cleanser 236ml', 140, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Sữa rửa mặt'),
 (SELECT MIN(brand_id)    FROM brands    WHERE brand_name =N'Cetaphil'),
 DATEFROMPARTS(2025,7,1), DATEFROMPARTS(2028,7,1), 0),

-- 8. La Roche-Posay Toleriane Hydrating Gentle Cleanser
(N'Sữa rửa mặt La Roche-Posay dịu nhẹ, bảo vệ hàng rào da', 0, GETDATE(), 320000,
 N'laroche_toleriane.png', N'La Roche-Posay Toleriane Hydrating Gentle Cleanser 200ml', 95, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Sữa rửa mặt'),
 (SELECT MIN(brand_id)    FROM brands    WHERE brand_name =N'La Roche-Posay'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- 9. Bioderma Sébium Gel Moussant
(N'Gel rửa mặt Bioderma giúp kiểm soát dầu & giảm mụn nhẹ nhàng', 0, GETDATE(), 210000,
 N'bioderma_sebium.png', N'Bioderma Sébium Gel Moussant 200ml', 100, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Sữa rửa mặt'),
 (SELECT MIN(brand_id)    FROM brands    WHERE brand_name =N'Bioderma'),
 DATEFROMPARTS(2025,5,15), DATEFROMPARTS(2028,5,15), 0),

-- 10. La Roche-Posay Effaclar Purifying Foaming Gel
(N'Sữa rửa mặt Effaclar giúp làm sạch sâu dành cho da dầu, hỗ trợ giảm mụn', 0, GETDATE(), 350000,
 N'laroche_effaclar.png', N'La Roche-Posay Effaclar Purifying Foaming Gel 200ml', 80, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Sữa rửa mặt'),
 (SELECT MIN(brand_id)    FROM brands    WHERE brand_name =N'La Roche-Posay'),
 DATEFROMPARTS(2025,6,10), DATEFROMPARTS(2028,6,10), 0);
GO



/* ==========
   10 MẶT NẠ
   ========== */

INSERT INTO products(
  description, discount, entered_date, price, product_image, product_name,
  quantity, status, category_id, brand_id, manufacture_date, expiry_date, favorite
)
VALUES
-- 1. Innisfree Super Volcanic Pore Clay Mask
(N'Mask đất sét Innisfree Super Volcanic giúp hút dầu, làm sạch lỗ chân lông sâu', 0, GETDATE(), 270000,
 N'innisfree_super_volcanic.png', N'Innisfree Super Volcanic Pore Clay Mask 100ml', 100, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Mặt nạ'),
 (SELECT brand_id FROM brands WHERE brand_name=N'Innisfree'),
 DATEFROMPARTS(2025,5,1), DATEFROMPARTS(2028,5,1), 0),

-- 2. SKIN1004 Mad Centella Poremizing Clay Stick Mask
(N'Mask đất sét dạng stick SKIN1004 chiết xuất centella giúp làm sạch & dịu da', 0, GETDATE(), 263000,
 N'skin1004_centella_clay_stick.png', N'SKIN1004 Mad Centella Poremizing Clay Stick Mask 55g', 80, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Mặt nạ'),
 (SELECT brand_id FROM brands WHERE brand_name=N'Skin1004'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- 3. Image Skincare Vital C Hydrating Enzyme Masque
(N'Mask enzyme + Vitamin C giúp tẩy tế bào chết nhẹ & cung cấp độ ẩm', 0, GETDATE(), 873499,
 N'image_vitalc_enzyme.png', N'Image Skincare Vital C Hydrating Enzyme Masque 57g', 50, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Mặt nạ'),
 (SELECT brand_id FROM brands WHERE brand_name=N'Image Skincare'),
 DATEFROMPARTS(2025,4,1), DATEFROMPARTS(2028,4,1), 0),

-- 4. Colorkey Luminous B3 Brightening Facial Mask
(N'Mask làm sáng da với Niacinamide B3, giúp đều màu & dưỡng ẩm', 0, GETDATE(), 15000,
 N'colorkey_b3_mask.png', N'Colorkey Luminous B3 Brightening Facial Mask 30ml', 200, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Mặt nạ'),
 (SELECT brand_id FROM brands WHERE brand_name=N'Colorkey'),
 DATEFROMPARTS(2025,7,1), DATEFROMPARTS(2027,7,1), 0),

-- 5. Rwine Placenta Face Mask
(N'Mask nhau thai (placenta) giúp dưỡng ẩm & tái tạo tế bào da', 0, GETDATE(), 11000,
 N'rwine_placenta_mask.png', N'Rwine Placenta Face Mask', 150, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Mặt nạ'),
 (SELECT brand_id FROM brands WHERE brand_name=N'Rwine'),
 DATEFROMPARTS(2025,8,1), DATEFROMPARTS(2027,8,1), 0),

-- 6. Laneige Water Sleeping Mask Ex
(N'Mặt nạ ngủ Laneige giúp cấp ẩm sâu qua đêm, làm da mềm mịn khi ngủ dậy', 0, GETDATE(), 227000,
 N'laneige_water_sleeping_ex.png', N'Laneige Water Sleeping Mask Ex 70ml', 120, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Mặt nạ'),
 (SELECT brand_id FROM brands WHERE brand_name=N'Laneige'),
 DATEFROMPARTS(2025,6,10), DATEFROMPARTS(2028,6,10), 0),

-- 7. Nature Republic Real Nature Orange Sheet Mask
(N'Mặt nạ giấy chiết xuất cam giúp cấp ẩm & sáng nhẹ cho da', 0, GETDATE(), 18000,
 N'nature_orange_sheet.png', N'Nature Republic Real Nature Orange Sheet Mask 23ml', 250, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Mặt nạ'),
 (SELECT brand_id FROM brands WHERE brand_name=N'Nature Republic'),
 DATEFROMPARTS(2025,7,15), DATEFROMPARTS(2028,7,15), 0),

-- 8. Saborino Morning Mask
(N'Mask buổi sáng Saborino giúp tiết kiệm bước chăm da, dưỡng & làm sạch nhẹ', 0, GETDATE(), 364163,
 N'saborino_morning_mask.png', N'Saborino Morning Facial Mask', 90, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Mặt nạ'),
 (SELECT brand_id FROM brands WHERE brand_name=N'Saborino'),
 DATEFROMPARTS(2025,5,20), DATEFROMPARTS(2028,5,20), 0),

-- 9. Caryophy Portulaca Mask Sheet
(N'Mặt nạ Caryophy giúp giảm mụn & làm dịu da với chiết xuất portulaca', 0, GETDATE(), 22950,
 N'caryophy_portulaca_mask.png', N'Caryophy Portulaca Mask Sheet 3-in-1', 180, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Mặt nạ'),
 (SELECT brand_id FROM brands WHERE brand_name=N'Caryophy'),
 DATEFROMPARTS(2025,6,25), DATEFROMPARTS(2028,6,25), 0),

-- 10. Klairs Midnight Blue Calming Sheet Mask
(N'Mặt nạ Klairs Midnight Blue làm dịu da, giảm đỏ & bảo vệ da nhạy cảm', 0, GETDATE(), 44000,
 N'klairs_midnight_blue.png', N'Klairs Midnight Blue Calming Sheet Mask', 140, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Mặt nạ'),
 (SELECT brand_id FROM brands WHERE brand_name=N'Klairs'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0);
GO


SET NOCOUNT ON;
SET XACT_ABORT ON;

-----------------------------
-- 0) KHẢO SÁT TRÙNG HIỆN TẠI
-----------------------------
-- Brand trùng
SELECT brand_name, COUNT(*) AS cnt
FROM dbo.brands
GROUP BY brand_name
HAVING COUNT(*) > 1;

-- Category trùng
SELECT category_name, COUNT(*) AS cnt
FROM dbo.categories
GROUP BY category_name
HAVING COUNT(*) > 1;
--Product trùng
SELECT product_name, COUNT(*) AS cnt
FROM dbo.products
GROUP BY product_name
HAVING COUNT(*) > 1;
------------------------------------
-- 1) CHUẨN HOÁ TÊN (TRIM khoảng trắng)
------------------------------------
UPDATE dbo.brands
SET brand_name = LTRIM(RTRIM(brand_name));

UPDATE dbo.categories
SET category_name = LTRIM(RTRIM(category_name));

UPDATE dbo.products
SET product_name = LTRIM(RTRIM(product_name));

--------------------------------------------
-- 2) GỘP TRÙNG BRANDS (giữ brand_id nhỏ nhất)
--------------------------------------------
BEGIN TRAN;

-- Liệt kê cặp (brand_id, keep_id) cho các bản ghi trùng
IF OBJECT_ID('tempdb..#brand_dups') IS NOT NULL DROP TABLE #brand_dups;
SELECT b.brand_id,
       b.brand_name,
       MIN(b2.brand_id) OVER (PARTITION BY b.brand_name) AS keep_id
INTO #brand_dups
FROM dbo.brands b
JOIN dbo.brands b2
  ON b.brand_name = b2.brand_name;

-- Chỉ lấy những bản ghi cần xoá (brand_id <> keep_id)
IF OBJECT_ID('tempdb..#brand_to_delete') IS NOT NULL DROP TABLE #brand_to_delete;
SELECT brand_id, brand_name, keep_id
INTO #brand_to_delete
FROM #brand_dups
WHERE brand_id <> keep_id;

-- Remap products -> keep_id
UPDATE p
SET p.brand_id = d.keep_id
FROM dbo.products p
JOIN #brand_to_delete d
  ON p.brand_id = d.brand_id;

-- Xoá bản ghi trùng
DELETE b
FROM dbo.brands b
JOIN #brand_to_delete d
  ON b.brand_id = d.brand_id;

COMMIT;
--2) GỘP TRÙNG PRODUCTS (giữ product_id nhỏ nhất)
BEGIN TRAN;

-- Liệt kê cặp (product_id, keep_id) cho các bản ghi trùng
IF OBJECT_ID('tempdb..#prod_dups') IS NOT NULL DROP TABLE #prod_dups;
SELECT p.product_id,
       p.product_name,
       MIN(p2.product_id) OVER (PARTITION BY p.product_name) AS keep_id
INTO #prod_dups
FROM dbo.products p
JOIN dbo.products p2
  ON p.product_name = p2.product_name;

-- Chỉ lấy những bản ghi cần xoá (product_id <> keep_id)
IF OBJECT_ID('tempdb..#prod_to_delete') IS NOT NULL DROP TABLE #prod_to_delete;
SELECT product_id, product_name, keep_id
INTO #prod_to_delete
FROM #prod_dups
WHERE product_id <> keep_id;

-- Nếu có bảng con liên kết FK tới products thì cần update chúng về keep_id trước khi xoá
-- Ví dụ: (nếu có bảng order_details)
-- UPDATE od
-- SET od.product_id = d.keep_id
-- FROM order_details od
-- JOIN #prod_to_delete d ON od.product_id = d.product_id;

-- Xoá bản ghi trùng
DELETE p
FROM dbo.products p
JOIN #prod_to_delete d
  ON p.product_id = d.product_id;

COMMIT;
-----------------------------------------------
-- 3) GỘP TRÙNG CATEGORIES (giữ category_id nhỏ)
-----------------------------------------------
BEGIN TRAN;

IF OBJECT_ID('tempdb..#cat_dups') IS NOT NULL DROP TABLE #cat_dups;
SELECT c.category_id,
       c.category_name,
       MIN(c2.category_id) OVER (PARTITION BY c.category_name) AS keep_id
INTO #cat_dups
FROM dbo.categories c
JOIN dbo.categories c2
  ON c.category_name = c2.category_name;

IF OBJECT_ID('tempdb..#cat_to_delete') IS NOT NULL DROP TABLE #cat_to_delete;
SELECT category_id, category_name, keep_id
INTO #cat_to_delete
FROM #cat_dups
WHERE category_id <> keep_id;

-- Remap products -> keep_id
UPDATE p
SET p.category_id = d.keep_id
FROM dbo.products p
JOIN #cat_to_delete d
  ON p.category_id = d.category_id;

-- Xoá bản ghi trùng
DELETE c
FROM dbo.categories c
JOIN #cat_to_delete d
  ON c.category_id = d.category_id;

COMMIT;

---------------------------------------
-- 4) TẠO UNIQUE TRÊN TÊN ĐÃ CHUẨN HOÁ
--    (dùng computed column để chống lệch khoảng trắng / chữ hoa-thường)
---------------------------------------
-- Brands
IF COL_LENGTH('dbo.brands', 'brand_name_norm') IS NULL
BEGIN
  ALTER TABLE dbo.brands
    ADD brand_name_norm AS UPPER(LTRIM(RTRIM(brand_name))) PERSISTED;
END;

IF NOT EXISTS (
  SELECT 1 FROM sys.indexes
  WHERE name = N'UQ_brands_brand_name_norm'
    AND object_id = OBJECT_ID(N'dbo.brands')
)
BEGIN
  CREATE UNIQUE INDEX UQ_brands_brand_name_norm
  ON dbo.brands(brand_name_norm);
END;

-- Categories
IF COL_LENGTH('dbo.categories', 'category_name_norm') IS NULL
BEGIN
  ALTER TABLE dbo.categories
    ADD category_name_norm AS UPPER(LTRIM(RTRIM(category_name))) PERSISTED;
END;

IF NOT EXISTS (
  SELECT 1 FROM sys.indexes
  WHERE name = N'UQ_categories_category_name_norm'
    AND object_id = OBJECT_ID(N'dbo.categories')
)
BEGIN
  CREATE UNIQUE INDEX UQ_categories_category_name_norm
  ON dbo.categories(category_name_norm);
END;

IF COL_LENGTH('dbo.products', 'product_name_norm') IS NULL
BEGIN
  ALTER TABLE dbo.products
    ADD product_name_norm AS UPPER(LTRIM(RTRIM(product_name))) PERSISTED;
END;

IF NOT EXISTS (
  SELECT 1 FROM sys.indexes
  WHERE name = N'UQ_products_product_name_norm'
    AND object_id = OBJECT_ID(N'dbo.products')
)
BEGIN
  CREATE UNIQUE INDEX UQ_products_product_name_norm
  ON dbo.products(product_name_norm);
END;
---------------------------------------
-- 5) KIỂM TRA LẠI (PHẢI = 0 DÒNG)
---------------------------------------
SELECT brand_name, COUNT(*) AS cnt
FROM dbo.brands
GROUP BY brand_name
HAVING COUNT(*) > 1;

SELECT category_name, COUNT(*) AS cnt
FROM dbo.categories
GROUP BY category_name
HAVING COUNT(*) > 1;

SELECT product_name, COUNT(*) AS cnt
FROM dbo.products
GROUP BY product_name
HAVING COUNT(*) > 1;