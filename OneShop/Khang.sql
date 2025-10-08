/* ===================== KEM CHỐNG NẮNG (KCN) ===================== */
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
INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Vichy', 'vichy.jpg', N'Dược mỹ phẩm Pháp', N'Pháp', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Vichy');
INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'La Roche-Posay', 'larocheposay.jpg', N'Dược mỹ phẩm Pháp', N'Pháp', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'La Roche-Posay');

/* 2) Lấy sẵn ID (chấp nhận có/không dấu) */
DECLARE @catKCN int =
(SELECT TOP 1 category_id FROM categories WHERE category_name IN (N'Kem chống nắng', N'Kem chong nang'));
IF @catKCN IS NULL
    THROW 51001, N'Không tìm thấy category Kem chống nắng (Kem chống nắng/Kem chong nang).', 1;

DECLARE @bCocoon     int = (SELECT brand_id FROM brands WHERE brand_name=N'Cocoon');
DECLARE @bCetaphil   int = (SELECT brand_id FROM brands WHERE brand_name=N'Cetaphil');
DECLARE @bFixderma   int = (SELECT brand_id FROM brands WHERE brand_name=N'Fixderma');
DECLARE @bLoreal     int = (SELECT brand_id FROM brands WHERE brand_name=N'L''Oréal Paris');
DECLARE @bSNG        int = (SELECT brand_id FROM brands WHERE brand_name=N'Sắc Ngọc Khang');
DECLARE @bSebamed    int = (SELECT brand_id FROM brands WHERE brand_name=N'Sebamed');
DECLARE @bReihaku    int = (SELECT brand_id FROM brands WHERE brand_name=N'Reihaku Hatomugi');
DECLARE @bVichy      int = (SELECT brand_id FROM brands WHERE brand_name=N'Vichy');
DECLARE @bLRP        int = (SELECT brand_id FROM brands WHERE brand_name=N'La Roche-Posay');

/* 3) Chèn 10 SP KCN (dùng biến @catKCN và @brand) */
INSERT INTO products(
  description, discount, entered_date, price, product_image, product_name,
  quantity, status, category_id, brand_id, manufacture_date, expiry_date, favorite
)
VALUES
-- 1) Cocoon Winter Melon Sunscreen
(N'Kem chống nắng bí đao Cocoon SPF50+ PA++++, nhẹ mặt, dùng hằng ngày', 0, GETDATE(), 245000,
 N'cocoon_winter_melon_spf50.png', N'COCOON Winter Melon Sunscreen SPF50+ PA++++ 50ml',
 60, 1, @catKCN, @bCocoon, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- 2) Cetaphil Sun Light Gel SPF50+
(N'Cetaphil Sun Light Gel SPF50+ PA++++, không nhờn rít, dịu cho da nhạy cảm', 0, GETDATE(), 390000,
 N'cetaphil_sun_spf50_light_gel.png', N'Cetaphil Sun Light Gel SPF50+ PA++++ 50ml',
 80, 1, @catKCN, @bCetaphil, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- 3) Vichy Capital Soleil Dry Touch SPF50
(N'Vichy Capital Soleil Dry Touch SPF50 PA++++, kiềm dầu - khô thoáng', 0, GETDATE(), 495000,
 N'vichy_capital_soleil_dry_touch_spf50.png', N'Vichy Capital Soleil Dry Touch SPF50 50ml',
 90, 1, @catKCN, @bVichy, DATEFROMPARTS(2025,5,1), DATEFROMPARTS(2028,5,1), 0),

-- 4) Fixderma Shadow SPF50+
(N'Fixderma Shadow SPF50+ PA+++, chống nắng mạnh, bền nước', 0, GETDATE(), 260000,
 N'fixderma_shadow_spf50_cream.png', N'Fixderma Shadow SPF50+ Cream 75g',
 70, 1, @catKCN, @bFixderma, DATEFROMPARTS(2025,4,1), DATEFROMPARTS(2028,4,1), 0),

-- 5) L'Oréal UV Defender Invisible Fluid SPF50+
(N'L''Oréal UV Defender Invisible Fluid SPF50+ PA++++, thấm nhanh, không để lại vệt trắng', 0, GETDATE(), 330000,
 N'loreal_uv_defender_invisible_fluid_spf50.png', N'L''Oréal UV Defender Invisible Fluid SPF50+ 50ml',
 100, 1, @catKCN, @bLoreal, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- 6) La Roche-Posay Anthelios UVMune 400 Oil Control
(N'La Roche-Posay Anthelios UVMune 400 Oil Control Fluid SPF50+ PA++++, kiểm soát dầu', 0, GETDATE(), 620000,
 N'larocheposay_uvmune400_oil_control_spf50.png', N'La Roche-Posay Anthelios UVMune 400 Oil Control SPF50+ 50ml',
 120, 1, @catKCN, @bLRP, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- 7) La Roche-Posay Anthelios XL Dry Touch
(N'Anthelios XL Dry Touch Gel-Cream SPF50+, chống bóng nhờn, không hương liệu', 0, GETDATE(), 580000,
 N'larocheposay_anthelios_xl_dry_touch_spf50.png', N'La Roche-Posay Anthelios XL Dry Touch SPF50+ 50ml',
 90, 1, @catKCN, @bLRP, DATEFROMPARTS(2025,5,1), DATEFROMPARTS(2028,5,1), 0),

-- 8) Sắc Ngọc Khang Tone Up Sun
(N'Sắc Ngọc Khang Tone Up Sun Gel-Cream SPF50+ PA++++, nâng tone nhẹ', 0, GETDATE(), 155000,
 N'sac_ngoc_khang_tone_up_spf50.png', N'Sắc Ngọc Khang Tone Up Sun SPF50+ 50g',
 110, 1, @catKCN, @bSNG, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- 9) Sebamed Anti-Redness Light Day Care SPF20
(N'Sebamed Anti-Redness Light Day Care SPF20, làm dịu da đỏ - rất nhạy cảm', 0, GETDATE(), 420000,
 N'sebamed_anti_redness_day_spf20.png', N'Sebamed Anti-Redness Light Day Care SPF20 50ml',
 50, 1, @catKCN, @bSebamed, DATEFROMPARTS(2025,5,1), DATEFROMPARTS(2027,5,1), 0),

-- 10) Reihaku Hatomugi UV Milky Gel
(N'Reihaku Hatomugi UV Milky Gel SPF50+ PA++++, gel sữa thấm nhanh, rửa được bằng xà phòng', 0, GETDATE(), 210000,
 N'reihaku_hatomugi_uv_milky_gel_spf50.png', N'Reihaku Hatomugi UV Milky Gel SPF50+ 80g',
 140, 1, @catKCN, @bReihaku, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0);
GO
/* ===================== PHẤN PHỦ ===================== */
USE WebMyPham;
GO


/* 1) Bổ sung thương hiệu nếu còn thiếu */
INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Innisfree', 'innisfree.jpg', N'Thien nhien Jeju, lanh tinh', N'Han Quoc', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Innisfree');
INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Catrice', 'catrice.jpg', N'My pham Duc, gia tot', N'Duc', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Catrice');
INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Eglips', 'eglips.jpg', N'Noi tieng voi phan phu kiem dau', N'Han Quoc', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Eglips');
INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'I''m Meme', 'im_meme.jpg', N'Phong cach tre trung, tien loi', N'Han Quoc', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'I''m Meme');
INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Lemonade', 'lemonade.jpg', N'Thuan chay, mong nhe', N'Viet Nam', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Lemonade');
INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Silkygirl', 'silkygirl.jpg', N'Gia mem, de dung', N'Malaysia', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Silkygirl');
INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Too Cool For School', 'tcfs.jpg', N'Phong cach nghe thuat, tre trung', N'Han Quoc', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Too Cool For School');

-- LẤY SẴN ID (chấp nhận có dấu/không dấu)
DECLARE @catPP int =
(SELECT TOP 1 category_id FROM categories WHERE category_name IN (N'Phan phu', N'Phấn phủ'));
IF @catPP IS NULL
    THROW 50001, N'Không tìm thấy category Phấn phủ (Phan phu/Phấn phủ).', 1;

DECLARE @bInnisfree int = (SELECT brand_id FROM brands WHERE brand_name=N'Innisfree');
DECLARE @bCatrice   int = (SELECT brand_id FROM brands WHERE brand_name=N'Catrice');
DECLARE @bEglips    int = (SELECT brand_id FROM brands WHERE brand_name=N'Eglips');
DECLARE @bImMeme    int = (SELECT brand_id FROM brands WHERE brand_name=N'I''m Meme');
DECLARE @bLemonade  int = (SELECT brand_id FROM brands WHERE brand_name=N'Lemonade');
DECLARE @bSilkygirl int = (SELECT brand_id FROM brands WHERE brand_name=N'Silkygirl');
DECLARE @bTCFS      int = (SELECT brand_id FROM brands WHERE brand_name=N'Too Cool For School');

-- 2) Chèn sản phẩm (DÙNG BIẾN @catPP và @b*)
INSERT INTO products(
  description, discount, entered_date, price, product_image, product_name,
  quantity, status, category_id, brand_id, manufacture_date, expiry_date, favorite
)
VALUES
(N'Phan phu bot kiem dau, hat phan sieu min giup da kho thoang suot ngay.', 0, GETDATE(), 165000,
 'innisfree-no-sebum-mineral-5g.png',
 N'Innisfree No-Sebum Mineral Powder 5g', 120, 1, @catPP, @bInnisfree, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),
(N'Phan nen kiem dau, li min den 12h, kiem soat bong nhon.', 0, GETDATE(), 195000,
 'catrice-all-matt-10g.png',
 N'Catrice All Matt Plus Shine Control Powder 10g', 150, 1, @catPP, @bCatrice, DATEFROMPARTS(2025,5,1), DATEFROMPARTS(2028,5,1), 0),
(N'Phan nen kiem dau Oil Cut, min da va co dinh lop nen lau troi.', 0, GETDATE(), 175000,
 'eglips-oil-cut-pact.png',
 N'Eglips Oil Cut Powder Pact', 140, 1, @catPP, @bEglips, DATEFROMPARTS(2025,4,1), DATEFROMPARTS(2028,4,1), 0),
(N'Phan nen hieu ung glow cang bong, lam muot be mat da, khong kho moc.', 0, GETDATE(), 185000,
 'eglips-glow-pact.png',
 N'Eglips Glow Powder Pact', 120, 1, @catPP, @bEglips, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),
(N'Hat phan sieu min Air Fit giup che mo lo chan long, da muot nhu loc.', 0, GETDATE(), 185000,
 'eglips-air-fit-8g.png',
 N'Eglips Air Fit Powder Pact 8g', 130, 1, @catPP, @bEglips, DATEFROMPARTS(2025,5,1), DATEFROMPARTS(2028,5,1), 0),
(N'Phan nen kiem dau tuc thi, giu nen kho rao va ben mau.', 0, GETDATE(), 210000,
 'im-meme-oil-cut-9-5g.png',
 N'I''m Meme Oil Cut Pact 9.5g', 110, 1, @catPP, @bImMeme, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),
(N'Phan bot thuan chay Supermatte, mong nhe nhu khong, lam mo khuyet diem.', 0, GETDATE(), 245000,
 'lemonade-supermatte-9g.png',
 N'Lemonade Supermatte No Makeup Loose Powder 9g', 100, 1, @catPP, @bLemonade, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),
(N'Phan bot khoang No-Sebum, kiem dau va lam min thoang da.', 0, GETDATE(), 120000,
 'silkygirl-no-sebum-5g.png',
 N'Silkygirl No-Sebum Mineral Powder 5g', 160, 1, @catPP, @bSilkygirl, DATEFROMPARTS(2025,4,1), DATEFROMPARTS(2028,4,1), 0),
(N'Phan nen nang tong Let It Glow, da sang hong tu nhien, min muot.', 0, GETDATE(), 155000,
 'silkygirl-let-it-glow-7g.png',
 N'Silkygirl Let It Glow Tone Up Powder 7g', 140, 1, @catPP, @bSilkygirl, DATEFROMPARTS(2025,5,1), DATEFROMPARTS(2028,5,1), 0),
(N'Phan nen finish/setting giup khoa lop nen, han che bong dau, min da.', 0, GETDATE(), 320000,
 'tcfs-artclass-rodin-4g.png',
 N'Too Cool For School Artclass By Rodin Finish Setting Pact 4g', 90, 1, @catPP, @bTCFS, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0);
GO

/* ===================== TẨY TẾ BÀO CHẾT ===================== */
USE WebMyPham;
GO
SET NOCOUNT ON;

-- Đảm bảo category tồn tại
IF COL_LENGTH('dbo.categories','status') IS NOT NULL
BEGIN
    INSERT INTO categories(category_name, status)
    SELECT N'Tay te bao chet', 1
    WHERE NOT EXISTS (SELECT 1 FROM categories WHERE category_name=N'Tay te bao chet');
END
ELSE
BEGIN
    INSERT INTO categories(category_name)
    SELECT N'Tay te bao chet'
    WHERE NOT EXISTS (SELECT 1 FROM categories WHERE category_name=N'Tay te bao chet');
END

/* 1) Bổ sung thương hiệu nếu còn thiếu */
INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Dr.G', 'drg.jpg', N'Dược mỹ phẩm Hàn Quốc, dịu nhẹ cho da', N'Hàn Quốc', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Dr.G');
INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Eucerin', 'eucerin.jpg', N'Dược mỹ phẩm Đức, chăm sóc da mụn', N'Đức', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Eucerin');
INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Himalaya', 'himalaya.jpg', N'Thảo mộc Ấn Độ, an toàn lành tính', N'Ấn Độ', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Himalaya');
INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Paula''s Choice', 'paulaschoice.jpg', N'Dược mỹ phẩm Hoa Kỳ, BHA/AHA nổi tiếng', N'Mỹ', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Paula''s Choice');
INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Exclusive Cosmetic', 'exclusivecosmetic.jpg', N'Mỹ phẩm Nga, chiết xuất cà phê', N'Nga', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Exclusive Cosmetic');
INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Meishoku', 'meishoku.jpg', N'Mỹ phẩm nội địa Nhật, chăm sóc da', N'Nhật Bản', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Meishoku');
INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Cocoon', 'cocoon.jpg', N'Mỹ phẩm thuần chay Việt Nam', N'Việt Nam', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Cocoon');
INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Naruko', 'naruko.jpg', N'Mỹ phẩm tràm trà nổi tiếng của Đài Loan', N'Đài Loan', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Naruko');
INSERT INTO brands(brand_name, brand_image, description, origin, status)
SELECT N'Organic Shop', 'organicshop.jpg', N'Mỹ phẩm hữu cơ, thiên nhiên', N'Nga', 1
WHERE NOT EXISTS (SELECT 1 FROM brands WHERE brand_name=N'Organic Shop');

-- LẤY SẴN ID
DECLARE @catTBC int =
(SELECT TOP 1 category_id FROM categories WHERE category_name IN (N'Tay te bao chet', N'Tẩy tế bào chết'));
IF @catTBC IS NULL
    THROW 50002, N'Không tìm thấy category Tẩy tế bào chết (Tay te bao chet/Tẩy tế bào chết).', 1;

DECLARE @bDrG        int = (SELECT brand_id FROM brands WHERE brand_name=N'Dr.G');
DECLARE @bEucerin    int = (SELECT brand_id FROM brands WHERE brand_name=N'Eucerin');
DECLARE @bHimalaya   int = (SELECT brand_id FROM brands WHERE brand_name=N'Himalaya');
DECLARE @bPaulas     int = (SELECT brand_id FROM brands WHERE brand_name=N'Paula''s Choice');
DECLARE @bExclusive  int = (SELECT brand_id FROM brands WHERE brand_name=N'Exclusive Cosmetic');
DECLARE @bMeishoku   int = (SELECT brand_id FROM brands WHERE brand_name=N'Meishoku');
DECLARE @bCocoon     int = (SELECT brand_id FROM brands WHERE brand_name=N'Cocoon');
DECLARE @bNaruko     int = (SELECT brand_id FROM brands WHERE brand_name=N'Naruko');
DECLARE @bOrganic    int = (SELECT brand_id FROM brands WHERE brand_name=N'Organic Shop');

-- 2) Chèn sản phẩm (DÙNG BIẾN @catTBC và @b*)
INSERT INTO products(
  description, discount, entered_date, price, product_image, product_name,
  quantity, status, category_id, brand_id, manufacture_date, expiry_date, favorite
)
VALUES
(N'Gel tẩy tế bào chết dịu nhẹ, làm sáng da, phù hợp da nhạy cảm.', 0, GETDATE(), 290000,
 'drg-brightening-peeling-gel-120g.png', N'Dr.G Brightening Peeling Gel 120g', 80, 1, @catTBC, @bDrG, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),
(N'Scrub tẩy tế bào chết hỗ trợ giảm dầu, hạn chế mụn.', 0, GETDATE(), 320000,
 'eucerin-pro-acne-scrub-100ml.png', N'Eucerin Pro Acne Solution Scrub 100ml', 90, 1, @catTBC, @bEucerin, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),
(N'Tẩy da chết chiết xuất neem và mơ, làm sạch sâu, ngừa mụn.', 0, GETDATE(), 120000,
 'himalaya-neem-scrub-100ml.png', N'Himalaya Purifying Neem Scrub 100ml', 150, 1, @catTBC, @bHimalaya, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),
(N'Tẩy tế bào chết hóa học BHA 2%, giảm mụn ẩn, mụn đầu đen.', 0, GETDATE(), 380000,
 'paulaschoice-bha-2-liquid-30ml.png', N'Paula''s Choice 2% BHA Liquid Exfoliant 30ml', 100, 1, @catTBC, @bPaulas, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),
(N'Gel scrub cà phê Nga, giúp da sáng mịn, giảm dầu thừa.', 0, GETDATE(), 95000,
 'exclusive-cosmetic-coffee-scrub-100g.png', N'Exclusive Cosmetic Coffee Gel Scrub 100g', 120, 1, @catTBC, @bExclusive, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),
(N'Gel tẩy tế bào chết Detclear, AHA BHA, ngăn ngừa lão hóa.', 0, GETDATE(), 310000,
 'meishoku-detclear-peeling-jelly-180ml.png', N'Meishoku Detclear Bright & Peel Peeling Jelly 180ml', 110, 1, @catTBC, @bMeishoku, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),
(N'Tẩy tế bào chết môi Cocoon cà phê Đắk Lắk, thuần chay.', 0, GETDATE(), 85000,
 'cocoon-dak-lak-coffee-lip-scrub-5g.png', N'Cocoon Dak Lak Coffee Lip Scrub 5g', 200, 1, @catTBC, @bCocoon, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),
(N'Gel tẩy tế bào chết Naruko tràm trà, kiểm soát nhờn, ngừa mụn.', 0, GETDATE(), 260000,
 'naruko-tea-tree-peeling-gel-120ml.png', N'Naruko Tea Tree Peeling Gel 120ml', 100, 1, @catTBC, @bNaruko, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),
(N'Tẩy tế bào chết hữu cơ Organic Shop chiết xuất cà phê.', 0, GETDATE(), 145000,
 'organicshop-soft-face-gommage-coffee-75ml.png', N'Organic Shop Soft Face Gommage Coffee 75ml', 90, 1, @catTBC, @bOrganic, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),
(N'Tẩy tế bào chết toàn thân Organic Shop Body Desserts, dưỡng ẩm.', 0, GETDATE(), 265000,
 'organicshop-body-desserts-scrub-450ml.png', N'Organic Shop Body Desserts Body Scrub 450ml', 70, 1, @catTBC, @bOrganic, DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0);
GO
