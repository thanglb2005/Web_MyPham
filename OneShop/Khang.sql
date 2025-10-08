USE WebMyPham;
GO

/* 1) Bổ sung thương hiệu nếu còn thiếu */
INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Cocoon', 'cocoon.jpg', N'Vegan – Original Vietnam', N'Việt Nam', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Cocoon');

INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Cetaphil', 'cetaphil.jpg', N'Dược mỹ phẩm cho da nhạy cảm', N'Canada', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Cetaphil');

INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Fixderma', 'fixderma.jpg', N'Dược mỹ phẩm Ấn Độ', N'Ấn Độ', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Fixderma');

INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'L''Oréal Paris', 'loreal_paris.jpg', N'Thương hiệu Pháp đại chúng', N'Pháp', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'L''Oréal Paris');

INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Sắc Ngọc Khang', 'sacngockhang.jpg', N'Thuộc Hoa Linh', N'Việt Nam', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Sắc Ngọc Khang');

INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Sebamed', 'sebamed.jpg', N'Độ pH 5.5 dịu nhẹ', N'Đức', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Sebamed');

INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Reihaku Hatomugi', 'reihaku_hatomugi.jpg', N'Hatomugi (ý dĩ) dưỡng ẩm', N'Nhật Bản', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Reihaku Hatomugi');

/* 2) Thêm 10 SP kem chống nắng với ảnh (đặt đúng tên file ảnh bạn đã có) */
INSERT INTO products(
  description, discount, entered_date, price, product_image, product_name,
  quantity, status, category_id, brand_id, manufacture_date, expiry_date, favorite
)
VALUES
-- Cocoon Winter Melon Sunscreen
(N'Kem chống nắng bí đao Cocoon SPF50+ PA++++, nhẹ mặt, dùng hằng ngày', 0, GETDATE(), 245000,
 N'cocoon_winter_melon_spf50.png', N'COCOON Winter Melon Sunscreen SPF50+ PA++++ 50ml', 60, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Kem chống nắng'),
 (SELECT brand_id    FROM brands    WHERE brand_name=N'Cocoon'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- Cetaphil Sun Light Gel SPF50+
(N'Cetaphil Sun Light Gel SPF50+ PA++++, không nhờn rít, dịu cho da nhạy cảm', 0, GETDATE(), 390000,
 N'cetaphil_sun_spf50_light_gel.png', N'Cetaphil Sun Light Gel SPF50+ PA++++ 50ml', 80, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Kem chống nắng'),
 (SELECT brand_id    FROM brands    WHERE brand_name=N'Cetaphil'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- Vichy Capital Soleil Dry Touch SPF50
(N'Vichy Capital Soleil Dry Touch SPF50 PA++++, kiềm dầu - khô thoáng', 0, GETDATE(), 495000,
 N'vichy_capital_soleil_dry_touch_spf50.png', N'Vichy Capital Soleil Dry Touch SPF50 50ml', 90, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Kem chống nắng'),
 (SELECT brand_id    FROM brands    WHERE brand_name=N'Vichy'),
 DATEFROMPARTS(2025,5,1), DATEFROMPARTS(2028,5,1), 0),

-- Fixderma Shadow SPF50+
(N'Fixderma Shadow SPF50+ PA+++, chống nắng mạnh, bền nước', 0, GETDATE(), 260000,
 N'fixderma_shadow_spf50_cream.png', N'Fixderma Shadow SPF50+ Cream 75g', 70, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Kem chống nắng'),
 (SELECT brand_id    FROM brands    WHERE brand_name=N'Fixderma'),
 DATEFROMPARTS(2025,4,1), DATEFROMPARTS(2028,4,1), 0),

-- L''Oréal UV Defender Invisible Fluid SPF50+
(N'L''Oréal UV Defender Invisible Fluid SPF50+ PA++++, thấm nhanh, không để lại vệt trắng', 0, GETDATE(), 330000,
 N'loreal_uv_defender_invisible_fluid_spf50.jpg', N'L''Oréal UV Defender Invisible Fluid SPF50+ 50ml', 100, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Kem chống nắng'),
 (SELECT brand_id    FROM brands    WHERE brand_name=N'L''Oréal Paris'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- La Roche-Posay Anthelios UVMune 400 Oil Control
(N'La Roche-Posay Anthelios UVMune 400 Oil Control Fluid SPF50+ PA++++, kiểm soát dầu', 0, GETDATE(), 620000,
 N'larocheposay_uvmune400_oil_control_spf50.png', N'La Roche-Posay Anthelios UVMune 400 Oil Control SPF50+ 50ml', 120, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Kem chống nắng'),
 (SELECT brand_id    FROM brands    WHERE brand_name=N'La Roche-Posay'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- La Roche-Posay Anthelios XL Dry Touch
(N'Anthelios XL Dry Touch Gel-Cream SPF50+, chống bóng nhờn, không hương liệu', 0, GETDATE(), 580000,
 N'larocheposay_anthelios_xl_dry_touch_spf50.png', N'La Roche-Posay Anthelios XL Dry Touch SPF50+ 50ml', 90, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Kem chống nắng'),
 (SELECT brand_id    FROM brands    WHERE brand_name=N'La Roche-Posay'),
 DATEFROMPARTS(2025,5,1), DATEFROMPARTS(2028,5,1), 0),

-- Sắc Ngọc Khang Tone Up Sun
(N'Sắc Ngọc Khang Tone Up Sun Gel-Cream SPF50+ PA++++, nâng tone nhẹ', 0, GETDATE(), 155000,
 N'sac_ngoc_khang_tone_up_spf50.png', N'Sắc Ngọc Khang Tone Up Sun SPF50+ 50g', 110, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Kem chống nắng'),
 (SELECT brand_id    FROM brands    WHERE brand_name=N'Sắc Ngọc Khang'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- Sebamed Anti-Redness Light Day Care SPF20  (kem ngày có SPF)
(N'Sebamed Anti-Redness Light Day Care SPF20, làm dịu da đỏ - rất nhạy cảm', 0, GETDATE(), 420000,
 N'sebamed_anti_redness_day_spf20.png', N'Sebamed Anti-Redness Light Day Care SPF20 50ml', 50, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Kem chống nắng'),
 (SELECT brand_id    FROM brands    WHERE brand_name=N'Sebamed'),
 DATEFROMPARTS(2025,5,1), DATEFROMPARTS(2027,5,1), 0),

-- Reihaku Hatomugi UV Milky Gel
(N'Reihaku Hatomugi UV Milky Gel SPF50+ PA++++, gel sữa thấm nhanh, rửa được bằng xà phòng', 0, GETDATE(), 210000,
 N'reihaku_hatomugi_uv_milky_gel_spf50.png', N'Reihaku Hatomugi UV Milky Gel SPF50+ 80g', 140, 1,
 (SELECT category_id FROM categories WHERE category_name=N'Kem chống nắng'),
 (SELECT brand_id    FROM brands    WHERE brand_name=N'Reihaku Hatomugi'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0);
GO
