USE WebMyPham;
GO

INSERT INTO brands(brand_name, brand_image, description, origin, status) 
VALUES
(N'The Ordinary', 'the_ordinary.jpg', N'Mỹ phẩm tối giản, tập trung hoạt chất', N'Canada', 1),
(N'La Roche-Posay', 'la_roche_posay.jpg', N'Dược mỹ phẩm cho da nhạy cảm', N'Pháp', 1),
(N'Vichy', 'vichy.jpg', N'Dược mỹ phẩm khoáng núi lửa', N'Pháp', 1),
(N'Skin1004', 'skin1004.jpg', N'Thương hiệu Hàn, nổi bật dòng rau má Madagascar', N'Hàn Quốc', 1),
(N'Some By Mi', 'some_by_mi.jpg', N'Nổi tiếng AHA-BHA-PHA 30 Days', N'Hàn Quốc', 1),
(N'Melano CC', 'melano_cc.jpg', N'Vitamin C trị thâm mờ nám', N'Nhật Bản', 1),
(N'Klairs', 'klairs.jpg', N'Serum Vitamin C dịu nhẹ cho da nhạy cảm', N'Hàn Quốc', 1),
(N'Paula''s Choice', 'paulas_choice.jpg', N'Booster/serum đặc trị, thành phần chuẩn', N'Mỹ', 1),
(N'Hada Labo', 'hada_labo.jpg', N'Dưỡng ẩm HA, có dòng Retinol B3', N'Nhật Bản', 1);
GO

INSERT INTO products(
  description, discount, entered_date, price, product_image, product_name,
  quantity, status, category_id, brand_id, manufacture_date, expiry_date, favorite
)
VALUES
-- 1. The Ordinary Niacinamide 10% + Zinc 1%
(N'Serum The Ordinary Niacinamide 10% + Zinc 1% giúp kiểm dầu, se lỗ chân lông', 0, '2025-10-07', 250000,
 'theordinary_niacinamide.jpg', N'The Ordinary Niacinamide 10% + Zinc 1%', 120, 1, 10, 5,
 '2025-07-01', '2028-07-01', 0),

-- 2. La Roche-Posay Hyalu B5 Serum
(N'Serum La Roche-Posay Hyaluronic Acid & Vitamin B5 hỗ trợ phục hồi hàng rào da', 5, '2025-10-07', 1600000,
 'larocheposay_hyalu_b5.jpg', N'La Roche-Posay Hyalu B5 Serum', 80, 1, 10, 6,
 '2025-06-01', '2028-06-01', 0),

-- 3. Vichy Minéral 89
(N'Dưỡng chất khoáng cô đặc 89% + HA, cấp ẩm và làm khỏe da', 10, '2025-10-07', 850000,
 'vichy_mineral89.jpg', N'Vichy Minéral 89 Hyaluronic Acid Serum', 100, 1, 10, 7,
 '2025-05-01', '2028-05-01', 0),

-- 4. Skin1004 Madagascar Centella Ampoule
(N'Chiết xuất rau má Madagascar làm dịu & phục hồi da sau mụn', 0, '2025-10-07', 300000,
 'skin1004_centella.jpg', N'Skin1004 Madagascar Centella Ampoule 55ml', 90, 1, 10, 8,
 '2025-07-10', '2028-07-10', 0),

-- 5. Some By Mi AHA-BHA-PHA 30 Days Miracle Serum
(N'AHA-BHA-PHA hỗ trợ làm sạch tế bào chết, sáng da & giảm mụn', 0, '2025-10-07', 350000,
 'somebymi_miracle.jpg', N'Some By Mi 30 Days Miracle Serum', 110, 1, 10, 9,
 '2025-04-01', '2028-04-01', 0),

-- 6. Melano CC Vitamin C Brightening Serum
(N'Vitamin C tinh khiết dưỡng sáng, hỗ trợ mờ thâm', 0, '2025-10-07', 220000,
 'melano_cc_vitc.jpg', N'Melano CC Vitamin C Brightening Serum 20ml', 150, 1, 10, 10,
 '2025-08-01', '2028-08-01', 0),

-- 7. Klairs Freshly Juiced Vitamin C Drop 5%
(N'Serum Vitamin C 5% dịu nhẹ cho da nhạy cảm, làm sáng tone', 0, '2025-10-07', 320000,
 'klairs_vitc_drop.jpeg', N'Klairs Freshly Juiced Vitamin C Drop 35ml', 70, 1, 10, 11,
 '2025-06-15', '2028-06-15', 0),

-- 8. L''Oréal Revitalift 1.5% Hyaluronic Acid Serum
(N'HA 1.5% đa kích thước cấp ẩm sâu, da căng mịn', 5, '2025-10-07', 360000,
 'loreal_revitalift_ha15.jpg', N'L''Oréal Revitalift 1.5% Hyaluronic Acid Serum', 130, 1, 10, 3,
 '2025-05-20', '2028-05-20', 0),

-- 9. Paula''s Choice 10% Niacinamide Booster
(N'Booster 10% Niacinamide hỗ trợ se lỗ chân lông & đều màu', 0, '2025-10-07', 1790000,
 'paulaschoice_niacinamide10.jpg', N'Paula''s Choice 10% Niacinamide Booster 20ml', 40, 1, 10, 12,
 '2025-03-01', '2028-03-01', 0),

-- 10. Hada Labo Retinol B3 Serum (Pro-Aging)
(N'Retinol + Vitamin B3 hỗ trợ cải thiện dấu hiệu lão hóa', 10, '2025-10-07', 300000,
 'hadalabo_retinol_b3.jpg', N'Hada Labo Retinol B3 Pro-Aging Serum 30ml', 85, 1, 10, 13,
 '2025-06-01', '2028-06-01', 0);
GO
