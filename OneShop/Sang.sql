USE WebMyPham;
GO

INSERT INTO brands(brand_name, brand_image, description, origin, status)
VALUES
(N'La Roche-Posay', 'laroche.png', N'Dược mỹ phẩm cho da nhạy cảm; nổi bật B5, Effaclar, Cicaplast', N'Pháp', 1), -- 5
(N'Olay', 'olay.png', N'Dưỡng ẩm và chống lão hoá (Niacinamide, Retinol, Peptide)', N'Mỹ', 1),                 -- 6
(N'Yves Saint Laurent (YSL)', 'ysl.png', N'Thương hiệu cao cấp Pháp; son Rouge Pur Couture, The Slim', N'Pháp', 1), -- 7
(N'ZO Skin Health', 'zo.png', N'Chăm sóc da chuyên sâu do bác sĩ da liễu Zein Obagi phát triển', N'Mỹ', 1),       -- 8
(N'3CE (3 Concept Eyes)', '3ce.png', N'Thương hiệu makeup Hàn Quốc; son lì thời trang, bảng màu trẻ trung', N'Hàn Quốc', 1), -- 9
(N'Avène', 'avene.png', N'Dược mỹ phẩm suối khoáng Avène, dịu nhẹ phục hồi hàng rào da', N'Pháp', 1),            -- 10
(N'Chanel', 'chanel.png', N'Thương hiệu cao cấp Pháp; son Rouge Coco/Flash, dưỡng môi sang trọng', N'Pháp', 1),  -- 11
(N'Eucerin', 'eucerin.png', N'Dược mỹ phẩm Đức; phục hồi, dưỡng ẩm, trị liệu cho da nhạy cảm', N'Đức', 1),       -- 12
(N'Sebamed', 'sebamed.png', N'Dược mỹ phẩm Đức, nổi bật dưỡng ẩm 5% Urea cho da khô', N'Đức', 1),                -- 13
(N'Aloins', 'aloins.png', N'Kem dưỡng ẩm chiết xuất nha đam Nhật Bản, dùng toàn thân', N'Nhật Bản', 1),          -- 14
(N'CeraVe', 'cerave.png', N'Thương hiệu dưỡng ẩm của Mỹ với Ceramide & HA, phục hồi hàng rào da', N'Mỹ', 1),     -- 15
(N'Ailus', 'ailus.png', N'Thương hiệu son bình dân, trẻ trung, giá hợp lý', N'Nhật Bản', 1);                     -- 16

GO

INSERT INTO products(
  description, discount, entered_date, price, product_image, product_name,
  quantity, status, category_id, brand_id, manufacture_date, expiry_date, favorite
)
VALUES
-- 1. YSL Rouge Pur Couture #01 – Đỏ Tươi
(N'Son thỏi YSL Rouge Pur Couture #01 tông đỏ tươi rực rỡ, chất son lì sang trọng', 0, '2025-10-08', 1220000,
 'SonYSLDoTuoi.jpg', N'YSL Rouge Pur Couture #01 Đỏ Tươi', 70, 1, 1, 7,
 '2025-06-01', '2028-06-01', 0),

-- 2. YSL Tatouage Couture Velvet Cream #216 – Hồng Đất
(N'Son kem lì YSL Tatouage Couture Velvet Cream #216 tông hồng đất dịu nhẹ, chất son mềm mịn', 0, '2025-10-08', 1180000,
 'YSLHongDat.jpg', N'YSL Tatouage Couture Velvet Cream #216 Hồng Đất', 80, 1, 1, 7,
 '2025-07-01', '2028-07-01', 0),

-- 3. Ailus Stress Free Lipstick M4V #03 – Cam Cháy
(N'Son thỏi Ailus Stress Free Lipstick M4V #03 tông cam cháy trẻ trung, giá bình dân', 0, '2025-10-08', 150000,
 'aliusCam.jpg', N'Ailus Stress Free Lipstick M4V #03 Cam Cháy', 100, 1, 1, 16,
 '2025-06-20', '2028-06-20', 0),

-- 4. Ailus Stress Free Lipstick M3V #01 – Đỏ Tươi
(N'Son thỏi Ailus Stress Free Lipstick M3V #01 tông đỏ tươi, mềm mượt dễ tán', 0, '2025-10-08', 150000,
 'aliusDo.jpg', N'Ailus Stress Free Lipstick M3V #01 Đỏ Tươi', 110, 1, 1, 16,
 '2025-06-25', '2028-06-25', 0),

-- 5. Chanel Rouge Coco Flash #116 – Cam San Hô
(N'Son thỏi Chanel Rouge Coco Flash #116 tông cam san hô tươi sáng, chất son dưỡng mềm mịn', 0, '2025-10-08', 1150000,
 'SonChanelCam.png', N'Chanel Rouge Coco Flash #116 Cam San Hô', 60, 1, 1, 11,
 '2025-06-10', '2028-06-10', 0),

-- 6. Chanel Rouge Coco Flash #106 – Đỏ Tươi
(N'Son thỏi Chanel Rouge Coco Flash #106 tông đỏ tươi cổ điển, bóng nhẹ tự nhiên', 0, '2025-10-08', 1150000,
 'SonChanelDo.png', N'Chanel Rouge Coco Flash #106 Đỏ Tươi', 55, 1, 1, 11,
 '2025-06-15', '2028-06-15', 0),

-- 7. Chanel Rouge Coco Flash #108 – Đỏ Hồng
(N'Son thỏi Chanel Rouge Coco Flash #108 tông đỏ hồng nữ tính, dưỡng ẩm môi tốt', 0, '2025-10-08', 1150000,
 'SonChannelDoHong.png', N'Chanel Rouge Coco Flash #108 Đỏ Hồng', 65, 1, 1, 11,
 '2025-06-18', '2028-06-18', 0),

-- 8. 3CE Cashmere Hue Lipstick – Đỏ Đất
(N'Son lì 3CE Cashmere Hue Lipstick tông đỏ đất trendy, chất son lì mịn, lâu trôi', 0, '2025-10-08', 380000,
 'sonli3CE.jpg', N'3CE Cashmere Hue Lipstick Đỏ Đất', 90, 1, 1, 9,
 '2025-07-05', '2028-07-05', 0),

-- 9. YSL Rouge Pur Couture The Slim – Đỏ Quyến Rũ
(N'Son thỏi YSL Rouge Pur Couture The Slim tông đỏ quyến rũ, lì mịn sang trọng', 5, '2025-10-08', 1250000,
 'sonSYL_Rouge.png', N'YSL Rouge Pur Couture The Slim Đỏ Quyến Rũ', 75, 1, 1, 7,
 '2025-05-25', '2028-05-25', 0),

-- 10. YSL Tatouage Couture Matte Stain #13 – Đỏ Cam
(N'Son kem lì YSL Tatouage Couture Matte Stain #13 tông đỏ cam trẻ trung, chất son nhẹ môi', 5, '2025-10-08', 1180000,
 'SonYSLDoCam.jpg', N'YSL Tatouage Couture Matte Stain #13 Đỏ Cam', 70, 1, 1, 7,
 '2025-06-05', '2028-06-05', 0),

-- 11. Olay Total Effects 7 in One Day Moisturiser SPF30
(N'Kem dưỡng da Olay Total Effects 7 in One Day Moisturiser SPF30 giúp dưỡng ẩm và chống lão hoá 7 tác dụng', 5, '2025-10-08', 280000,
 'KemOlay.png', N'Olay Total Effects 7 in One Day Moisturiser SPF30', 120, 1, 2, 6,
 '2025-07-01', '2028-07-01', 0),

-- 12. Sebamed Relief Face Cream 5% Urea
(N'Kem dưỡng Sebamed Relief Face Cream 5% Urea dành cho da khô, cấp ẩm và phục hồi da', 0, '2025-10-08', 320000,
 'kemSebamed.png', N'Sebamed Relief Face Cream 5% Urea', 100, 1, 2, 13,
 '2025-06-15', '2028-06-15', 0),

-- 13. ZO Skin Health Retinol Skin Brightener 1%
(N'ZO Skin Health Retinol Skin Brightener 1% giúp cải thiện sắc tố, làm sáng da và chống lão hóa', 5, '2025-10-08', 2500000,
 'kemZO.jpg', N'ZO Skin Health Retinol Skin Brightener 1%', 60, 1, 2, 8,
 '2025-06-01', '2028-06-01', 0),

-- 14. Aloins Eaude Cream S Aloe Extract
(N'Kem dưỡng ẩm Aloins Eaude Cream S chiết xuất nha đam dưỡng da toàn thân, phục hồi da khô', 0, '2025-10-08', 180000,
 'kemALONIS.png', N'Aloins Eaude Cream S Aloe Extract', 150, 1, 2, 14,
 '2025-05-20', '2028-05-20', 0),

-- 15. CeraVe Moisturising Cream (340g)
(N'Kem dưỡng ẩm CeraVe Moisturising Cream chứa Ceramides & Hyaluronic Acid cho da khô tới rất khô', 0, '2025-10-08', 350000,
 'kemCeraVe.png', N'CeraVe Moisturising Cream 340g', 130, 1, 2, 15,
 '2025-05-10', '2028-05-10', 0),

-- 16. Avène XeraCalm A.D Lipid-Replenishing Balm 200ml
(N'Balm dưỡng ẩm Avène XeraCalm A.D dành cho da rất khô và dễ kích ứng, phục hồi hàng rào bảo vệ da', 0, '2025-10-08', 420000,
 'kemEAU.png', N'Avène XeraCalm A.D Lipid-Replenishing Balm 200ml', 90, 1, 2, 10,
 '2025-06-01', '2028-06-01', 0),

-- 17. Eucerin AQUAporin ACTIVE Moisturising Cream
(N'Kem dưỡng ẩm Eucerin AQUAporin ACTIVE cấp nước tức thì, phục hồi độ ẩm cho da nhạy cảm', 0, '2025-10-08', 380000,
 'kemEucerin.png', N'Eucerin AQUAporin ACTIVE Moisturising Cream', 110, 1, 2, 12,
 '2025-07-01', '2028-07-01', 0),

-- 18. Eucerin Q10 ACTIVE Anti-Wrinkle Face Cream
(N'Kem dưỡng Eucerin Q10 ACTIVE giảm nếp nhăn, nuôi dưỡng da săn chắc và mịn màng', 0, '2025-10-08', 450000,
 'kemEucerinSang.png', N'Eucerin Q10 ACTIVE Anti-Wrinkle Face Cream', 100, 1, 2, 12,
 '2025-07-10', '2028-07-10', 0),

-- 19. La Roche-Posay Cicaplast Baume B5
(N'Kem phục hồi La Roche-Posay Cicaplast Baume B5 làm dịu và phục hồi da tổn thương, thích hợp cho da nhạy cảm', 0, '2025-10-08', 280000,
 'kemLaRoche.png', N'La Roche-Posay Cicaplast Baume B5', 150, 1, 2, 5,
 '2025-06-20', '2028-06-20', 0),

-- 20. ZO Skin Health Pigment Control Crème 4% Hydroquinone
(N'Kem dưỡng ZO Skin Health Pigment Control Crème 4% Hydroquinone giúp cải thiện sắc tố, làm sáng da, chỉ định bởi bác sĩ', 5, '2025-10-08', 3200000,
 'kemNamZO.png', N'ZO Skin Health Pigment Control Crème 4% Hydroquinone', 50, 1, 2, 8,
 '2025-06-15', '2028-06-15', 0);
GO
