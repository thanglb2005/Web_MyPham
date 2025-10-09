CREATE DATABASE WebMyPham;
GO
USE WebMyPham;
GO

/* ===============================
   TABLE: categories
   =============================== */
IF OBJECT_ID('dbo.categories', 'U') IS NOT NULL DROP TABLE dbo.categories;
CREATE TABLE dbo.categories (
    category_id     BIGINT IDENTITY(1,1) PRIMARY KEY,
    category_image  NVARCHAR(255),
    category_name   NVARCHAR(255) NOT NULL
);
INSERT INTO dbo.categories(category_image, category_name) VALUES
(NULL, N'Son môi'),
(NULL, N'Kem dưỡng da'),
(NULL, N'Nước hoa'),
(NULL, N'Sữa rửa mặt'),
(NULL, N'Toner'),
(NULL, N'Mặt nạ'),
(NULL, N'Kem chống nắng'),
(NULL, N'Phấn phủ'),
(NULL, N'Tẩy tế bào chết'),
(NULL, N'Serum dưỡng da');
GO

/* ===============================
   TABLE: user
   =============================== */
IF OBJECT_ID('dbo.[user]', 'U') IS NOT NULL DROP TABLE dbo.[user];
CREATE TABLE dbo.[user] (
    user_id        BIGINT IDENTITY(1,1) PRIMARY KEY,
    avatar         NVARCHAR(255),
    email          NVARCHAR(255) UNIQUE NOT NULL,
    name           NVARCHAR(255) NOT NULL,
    password       NVARCHAR(255) NOT NULL,
    register_date  DATE DEFAULT (GETDATE()),
    status         BIT  DEFAULT 1
);
-- 8 user (đã thêm shipper)
INSERT INTO dbo.[user](avatar, email, name, password, register_date, status)
VALUES 
('user.png','chi@gmail.com',N'Trần Thảo Chi','123456','2025-09-04',1),
('user.png','dong@gmail.com',N'Trần Hữu Đồng','123456','2025-09-04',1),
('user.png','user@gmail.com',N'User Demo','123456','2025-09-04',1),
('user.png','admin@mypham.com',N'Admin Mỹ Phẩm','123456','2025-09-04',1),
('user.png','vendor@mypham.com',N'Nguyễn Văn An','123456','2025-10-07',1),
('user.png','vendor1@mypham.com',N'Trần Thị Bình','123456','2025-10-07',1),
('user.png','vendor2@mypham.com',N'Lê Quốc Cường','123456','2025-10-07',1),
('user.png','shipper@mypham.com',N'Phạm Văn Giao','123456','2025-10-08',1);
GO

/* ===============================
   TABLE: role
   =============================== */
IF OBJECT_ID('dbo.role', 'U') IS NOT NULL DROP TABLE dbo.role;
CREATE TABLE dbo.role (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) UNIQUE NOT NULL
);
INSERT INTO dbo.role(name)
VALUES 
('ROLE_USER'),
('ROLE_ADMIN'),
('ROLE_VENDOR'),
('ROLE_SHIPPER'); -- Thêm role mới
GO

/* ===============================
   TABLE: users_roles
   =============================== */
IF OBJECT_ID('dbo.users_roles', 'U') IS NOT NULL DROP TABLE dbo.users_roles;
CREATE TABLE dbo.users_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT PK_users_roles PRIMARY KEY(user_id, role_id),
    CONSTRAINT FK_users_roles_user FOREIGN KEY(user_id) REFERENCES dbo.[user](user_id),
    CONSTRAINT FK_users_roles_role FOREIGN KEY(role_id) REFERENCES dbo.role(id)
);
-- Gán role cho user
INSERT INTO dbo.users_roles(user_id, role_id)
VALUES 
(1,1),  -- user thường
(2,1),
(3,1),
(4,2),  -- admin
(5,3),  -- vendor
(6,3),
(7,3),
(8,4);  -- shipper
GO

/* ===============================
   TABLE: brands
   =============================== */
IF OBJECT_ID('dbo.brands', 'U') IS NOT NULL DROP TABLE dbo.brands;
CREATE TABLE dbo.brands (
    brand_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    brand_name NVARCHAR(255) NOT NULL,
    brand_image NVARCHAR(255),
    description NVARCHAR(1000),
    origin NVARCHAR(255),
    status BIT DEFAULT 1
);

/* ===============================
   TABLE: products
   =============================== */
IF OBJECT_ID('dbo.products', 'U') IS NOT NULL DROP TABLE dbo.products;
CREATE TABLE dbo.products (
    product_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    description NVARCHAR(1000),
    discount INT CHECK (discount >= 0 AND discount <= 90),
    entered_date DATETIME DEFAULT GETDATE(),
    price DECIMAL(18,2) NOT NULL CHECK (price > 0),
    product_image NVARCHAR(255),
    product_name NVARCHAR(255) NOT NULL,
    quantity INT NOT NULL CHECK (quantity >= 0),
    status BIT DEFAULT 1,
    category_id BIGINT,
    brand_id BIGINT,
    manufacture_date DATE,
    expiry_date DATE,
    favorite BIT DEFAULT 0,
    CONSTRAINT FK_products_categories FOREIGN KEY(category_id) REFERENCES dbo.categories(category_id),
    CONSTRAINT FK_products_brands FOREIGN KEY(brand_id) REFERENCES dbo.brands(brand_id)
);


/* ===============================
   TABLE: orders
   =============================== */
IF OBJECT_ID('dbo.orders', 'U') IS NOT NULL DROP TABLE dbo.orders;
CREATE TABLE dbo.orders (
    order_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    customer_name NVARCHAR(255) NOT NULL,
    customer_email NVARCHAR(255) NOT NULL,
    customer_phone NVARCHAR(20) NOT NULL,
    shipping_address NVARCHAR(500) NOT NULL,
    note NVARCHAR(1000),
    status NVARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_method NVARCHAR(50) NOT NULL DEFAULT 'COD',
    total_amount DECIMAL(18,2) NOT NULL CHECK (total_amount > 0),
    order_date DATETIME2 NOT NULL DEFAULT GETDATE(),
    shipped_date DATETIME2,
    delivered_date DATETIME2,
    shipper_id BIGINT NULL, -- shipper giao hàng
    CONSTRAINT FK_orders_user FOREIGN KEY(user_id) REFERENCES dbo.[user](user_id),
    CONSTRAINT FK_orders_shipper FOREIGN KEY(shipper_id) REFERENCES dbo.[user](user_id)
);


/* ===============================
   TABLE: order_details
   =============================== */
IF OBJECT_ID('dbo.order_details', 'U') IS NOT NULL DROP TABLE dbo.order_details;
CREATE TABLE dbo.order_details (
    order_detail_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name NVARCHAR(255) NOT NULL,
    unit_price DECIMAL(18,2) NOT NULL CHECK (unit_price > 0),
    quantity INT NOT NULL CHECK (quantity > 0),
    total_price DECIMAL(18,2) NOT NULL CHECK (total_price > 0),
    CONSTRAINT FK_orderdetails_orders FOREIGN KEY(order_id) REFERENCES dbo.orders(order_id),
    CONSTRAINT FK_orderdetails_products FOREIGN KEY(product_id) REFERENCES dbo.products(product_id)
);

/* ===============================
   TABLE: comments
   =============================== */
IF OBJECT_ID('dbo.comments', 'U') IS NOT NULL DROP TABLE dbo.comments;
CREATE TABLE dbo.comments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    content NVARCHAR(255),
    rate_date DATETIME DEFAULT GETDATE(),
    rating DECIMAL(2,1) CHECK (rating BETWEEN 1 AND 5),
    order_detail_id BIGINT,
    product_id BIGINT,
    user_id BIGINT,
    CONSTRAINT FK_comments_orderdetail FOREIGN KEY(order_detail_id) REFERENCES dbo.order_details(order_detail_id),
    CONSTRAINT FK_comments_product FOREIGN KEY(product_id) REFERENCES dbo.products(product_id),
    CONSTRAINT FK_comments_user FOREIGN KEY(user_id) REFERENCES dbo.[user](user_id)
);


/* ===============================
   TABLE: favorites
   =============================== */
IF OBJECT_ID('dbo.favorites', 'U') IS NOT NULL DROP TABLE dbo.favorites;
CREATE TABLE dbo.favorites (
    favorite_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id BIGINT,
    user_id BIGINT,
    CONSTRAINT FK_favorites_product FOREIGN KEY(product_id) REFERENCES dbo.products(product_id),
    CONSTRAINT FK_favorites_user FOREIGN KEY(user_id) REFERENCES dbo.[user](user_id)
);

-- chat_message table + index
CREATE TABLE chat_message (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  room_id NVARCHAR(100) NOT NULL,
  sender NVARCHAR(255) NOT NULL,
  sender_type NVARCHAR(50) NOT NULL,
  message_type NVARCHAR(20) NOT NULL,
  content NVARCHAR(MAX) NOT NULL,
  sent_at BIGINT NOT NULL,
  customer_name NVARCHAR(255) NULL,
  vendor_name NVARCHAR(255) NULL
);
CREATE INDEX ix_chat_message_room_time ON chat_message(room_id, sent_at DESC);

/* ===============================
   TABLE: shipping_providers
   =============================== */
IF OBJECT_ID('dbo.shipping_providers', 'U') IS NOT NULL DROP TABLE dbo.shipping_providers;
CREATE TABLE dbo.shipping_providers (
    provider_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    provider_name NVARCHAR(255) NOT NULL,
    contact_phone NVARCHAR(15),
    contact_email NVARCHAR(255),
    description NVARCHAR(1000),
    website NVARCHAR(255),
    address NVARCHAR(255),
    shipping_fees DECIMAL(18,2),
    delivery_time_range NVARCHAR(255),
    logo NVARCHAR(255),
    status BIT DEFAULT 1
);
INSERT INTO dbo.shipping_providers (provider_name, contact_phone, contact_email, description, website, address, shipping_fees, delivery_time_range, logo, status)
VALUES
(N'Giao Hàng Nhanh', N'1900636677', N'support@ghn.vn', N'Dịch vụ giao hàng nhanh trên toàn quốc', N'https://ghn.vn', N'Hà Nội, Việt Nam', 30000, N'1-3 ngày', N'ghn-logo.png', 1),
(N'Giao Hàng Tiết Kiệm', N'1900636677', N'support@giaohangtietkiem.vn', N'Dịch vụ giao hàng tiết kiệm toàn quốc', N'https://giaohangtietkiem.vn', N'Hồ Chí Minh, Việt Nam', 25000, N'2-4 ngày', N'ghtk-logo.png', 1),
(N'J&T Express', N'19001088', N'cskh@jtexpress.vn', N'Dịch vụ chuyển phát nhanh J&T Express', N'https://jtexpress.vn', N'Hồ Chí Minh, Việt Nam', 28000, N'1-3 ngày', N'jt-express-logo.png', 1),
(N'Viettel Post', N'1900818820', N'cskh@viettelpost.com.vn', N'Dịch vụ chuyển phát Viettel Post', N'https://viettelpost.com.vn', N'Hà Nội, Việt Nam', 27000, N'2-4 ngày', N'viettel-post-logo.png', 1),
(N'Vietnam Post', N'18006422', N'info@vnpost.vn', N'Bưu điện Việt Nam - Vietnam Post', N'https://www.vnpost.vn', N'Hà Nội, Việt Nam', 26000, N'2-5 ngày', N'vnpost-logo.png', 1);
GO
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
/* ===================== INSERT BRANDS ===================== */
INSERT INTO brands(brand_name, brand_image, description, origin, status)
VALUES
(N'La Roche-Posay', 'laroche.png', N'Dược mỹ phẩm cho da nhạy cảm; nổi bật B5, Effaclar, Cicaplast', N'Pháp', 1),
(N'Olay', 'olay.png', N'Dưỡng ẩm và chống lão hoá (Niacinamide, Retinol, Peptide)', N'Mỹ', 1),
(N'Yves Saint Laurent (YSL)', 'ysl.png', N'Thương hiệu cao cấp Pháp; son Rouge Pur Couture, The Slim', N'Pháp', 1),
(N'ZO Skin Health', 'zo.png', N'Chăm sóc da chuyên sâu do bác sĩ da liễu Zein Obagi phát triển', N'Mỹ', 1),
(N'3CE (3 Concept Eyes)', '3ce.png', N'Thương hiệu makeup Hàn Quốc; son lì thời trang, bảng màu trẻ trung', N'Hàn Quốc', 1),
(N'Avène', 'avene.png', N'Dược mỹ phẩm suối khoáng Avène, dịu nhẹ phục hồi hàng rào da', N'Pháp', 1),
(N'Chanel', 'chanel.png', N'Thương hiệu cao cấp Pháp; son Rouge Coco/Flash, dưỡng môi sang trọng', N'Pháp', 1),
(N'Eucerin', 'eucerin.png', N'Dược mỹ phẩm Đức; phục hồi, dưỡng ẩm, trị liệu cho da nhạy cảm', N'Đức', 1),
(N'Sebamed', 'sebamed.png', N'Dược mỹ phẩm Đức, nổi bật dưỡng ẩm 5% Urea cho da khô', N'Đức', 1),
(N'Aloins', 'aloins.png', N'Kem dưỡng ẩm chiết xuất nha đam Nhật Bản, dùng toàn thân', N'Nhật Bản', 1),
(N'CeraVe', 'cerave.png', N'Thương hiệu dưỡng ẩm của Mỹ với Ceramide & HA, phục hồi hàng rào da', N'Mỹ', 1),
(N'Ailus', 'ailus.png', N'Thương hiệu son bình dân, trẻ trung, giá hợp lý', N'Nhật Bản', 1),
(N'The Ordinary', 'the_ordinary.jpg', N'Mỹ phẩm tối giản, tập trung hoạt chất', N'Canada', 1),
(N'Vichy', 'vichy.jpg', N'Dược mỹ phẩm khoáng núi lửa', N'Pháp', 1),
(N'Skin1004', 'skin1004.jpg', N'Thương hiệu Hàn, nổi bật dòng rau má Madagascar', N'Hàn Quốc', 1),
(N'Some By Mi', 'some_by_mi.jpg', N'Nổi tiếng AHA-BHA-PHA 30 Days', N'Hàn Quốc', 1),
(N'Melano CC', 'melano_cc.jpg', N'Vitamin C trị thâm mờ nám', N'Nhật Bản', 1),
(N'Klairs', 'klairs.jpg', N'Serum Vitamin C dịu nhẹ cho da nhạy cảm', N'Hàn Quốc', 1),
(N'Paula''s Choice', 'paulas_choice.jpg', N'Booster/serum đặc trị, thành phần chuẩn', N'Mỹ', 1),
(N'Hada Labo', 'hada_labo.jpg', N'Dưỡng ẩm HA, có dòng Retinol B3', N'Nhật Bản', 1),
(N'Cocoon', 'cocoon.jpg', N'Vegan – Original Vietnam', N'Việt Nam', 1),
(N'Cetaphil', 'cetaphil.jpg', N'Dược mỹ phẩm cho da nhạy cảm', N'Canada', 1),
(N'Fixderma', 'fixderma.jpg', N'Dược mỹ phẩm Ấn Độ', N'Ấn Độ', 1),
(N'L''Oréal Paris', 'loreal_paris.jpg', N'Thương hiệu Pháp đại chúng', N'Pháp', 1),
(N'Sắc Ngọc Khang', 'sacngockhang.jpg', N'Thuộc Hoa Linh', N'Việt Nam', 1),
(N'Reihaku Hatomugi', 'reihaku_hatomugi.jpg', N'Hatomugi (ý dĩ) dưỡng ẩm', N'Nhật Bản', 1),
(N'Innisfree', 'innisfree.jpg', N'Thien nhien Jeju, lanh tinh', N'Han Quoc', 1),
(N'Catrice', 'catrice.jpg', N'My pham Duc, gia tot', N'Duc', 1),
(N'Eglips', 'eglips.jpg', N'Noi tieng voi phan phu kiem dau', N'Han Quoc', 1),
(N'I''m Meme', 'im_meme.jpg', N'Phong cach tre trung, tien loi', N'Han Quoc', 1),
(N'Lemonade', 'lemonade.jpg', N'Thuan chay, mong nhe', N'Viet Nam', 1),
(N'Silkygirl', 'silkygirl.jpg', N'Gia mem, de dung', N'Malaysia', 1),
(N'Too Cool For School', 'tcfs.jpg', N'Phong cach nghe thuat, tre trung', N'Han Quoc', 1),
(N'Dr.G', 'drg.jpg', N'Dược mỹ phẩm Hàn Quốc, dịu nhẹ cho da', N'Hàn Quốc', 1),
(N'Himalaya', 'himalaya.jpg', N'Thảo mộc Ấn Độ, an toàn lành tính', N'Ấn Độ', 1),
(N'Exclusive Cosmetic', 'exclusivecosmetic.jpg', N'Mỹ phẩm Nga, chiết xuất cà phê', N'Nga', 1),
(N'Meishoku', 'meishoku.jpg', N'Mỹ phẩm nội địa Nhật, chăm sóc da', N'Nhật Bản', 1),
(N'Naruko', 'naruko.jpg', N'Mỹ phẩm tràm trà nổi tiếng của Đài Loan', N'Đài Loan', 1),
(N'Organic Shop', 'organicshop.jpg', N'Mỹ phẩm hữu cơ, thiên nhiên', N'Nga', 1),
(N'Simple', 'simple.png', N'Thương hiệu Anh Quốc nổi tiếng với dòng skincare lành tính, không cồn & hương liệu', N'Anh Quốc', 1),
(N'Pyunkang Yul', 'pyunkangyul.png', N'Thương hiệu Hàn Quốc thuộc viện Y học cổ truyền Pyunkang, nổi bật dưỡng ẩm dịu nhẹ', N'Hàn Quốc', 1),
(N'Bioderma', 'bioderma.png', N'Dược mỹ phẩm Pháp, nổi bật chăm sóc da nhạy cảm và làm sạch dịu nhẹ', N'Pháp', 1),
(N'Colorkey', 'colorkey.png', N'Mỹ phẩm Trung Quốc theo xu hướng makeup + skincare', N'Trung Quốc', 1),
(N'Rwine', 'rwine.png', N'Mỹ phẩm Nhật Bản với các dòng mask & dưỡng da', N'Nhật Bản', 1),
(N'Nature Republic', 'nature_republic.png', N'Thương hiệu mỹ phẩm thiên nhiên Hàn Quốc', N'Hàn Quốc', 1),
(N'Saborino', 'saborino.png', N'Mask nhanh buổi sáng từ Nhật Bản', N'Nhật Bản', 1),
(N'Caryophy', 'caryophy.png', N'Mỹ phẩm Hàn Quốc thiên về điều trị mụn & dịu da', N'Hàn Quốc', 1),
(N'The Face Shop', 'the_face_shop.png', N'Thương hiệu Hàn Quốc với dòng chiết xuất thiên nhiên', N'Hàn Quốc', 1),
(N'Anua', 'anua.png', N'Mỹ phẩm Hàn sử dụng các thành phần thiên nhiên lành tính', N'Hàn Quốc', 1),
(N'SVR', 'svr.png', N'Dược mỹ phẩm Pháp chuyên cho da dầu / mụn', N'Pháp', 1),
(N'Image Skincare', 'image_skincare.png', N'Mỹ phẩm chuyên nghiệp từ Mỹ', N'Mỹ', 1),
(N'Laneige', 'laneige.png', N'Thương hiệu mỹ phẩm Hàn Quốc nổi tiếng', N'Hàn Quốc', 1),
(N'Chanel', 'chanel_perfume.png', N'Nước hoa cao cấp Pháp', N'Pháp', 1),
(N'Dior', 'dior.png', N'Nước hoa sang trọng Pháp', N'Pháp', 1),
(N'Versace', 'versace.png', N'Nước hoa Ý cao cấp', N'Ý', 1),
(N'Calvin Klein', 'calvin_klein.png', N'Nước hoa Mỹ phổ biến', N'Mỹ', 1),
(N'Hugo Boss', 'hugo_boss.png', N'Nước hoa Đức nam tính', N'Đức', 1),
(N'Lacoste', 'lacoste.png', N'Nước hoa Pháp thể thao', N'Pháp', 1),
(N'Montblanc', 'montblanc.png', N'Nước hoa Đức sang trọng', N'Đức', 1),
(N'Burberry', 'burberry.png', N'Nước hoa Anh cổ điển', N'Anh', 1),
(N'Tommy Hilfiger', 'tommy_hilfiger.png', N'Nước hoa Mỹ trẻ trung', N'Mỹ', 1);
GO

/* ===================== INSERT PRODUCTS ===================== */
USE WebMyPham;
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
 '2025-06-15', '2028-06-15', 0),

-- 21. The Ordinary Niacinamide 10% + Zinc 1%
(N'Serum The Ordinary Niacinamide 10% + Zinc 1% giúp kiểm dầu, se lỗ chân lông', 0, '2025-10-07', 250000,
 'theordinary_niacinamide.jpg', N'The Ordinary Niacinamide 10% + Zinc 1%', 120, 1, 10, 5,
 '2025-07-01', '2028-07-01', 0),

-- 22. La Roche-Posay Hyalu B5 Serum
(N'Serum La Roche-Posay Hyaluronic Acid & Vitamin B5 hỗ trợ phục hồi hàng rào da', 5, '2025-10-07', 1600000,
 'larocheposay_hyalu_b5.jpg', N'La Roche-Posay Hyalu B5 Serum', 80, 1, 10, 6,
 '2025-06-01', '2028-06-01', 0),

-- 23. Vichy Minéral 89
(N'Dưỡng chất khoáng cô đặc 89% + HA, cấp ẩm và làm khỏe da', 10, '2025-10-07', 850000,
 'vichy_mineral89.jpg', N'Vichy Minéral 89 Hyaluronic Acid Serum', 100, 1, 10, 7,
 '2025-05-01', '2028-05-01', 0),

-- 24. Skin1004 Madagascar Centella Ampoule
(N'Chiết xuất rau má Madagascar làm dịu & phục hồi da sau mụn', 0, '2025-10-07', 300000,
 'skin1004_centella.jpg', N'Skin1004 Madagascar Centella Ampoule 55ml', 90, 1, 10, 8,
 '2025-07-10', '2028-07-10', 0),

-- 25. Some By Mi AHA-BHA-PHA 30 Days Miracle Serum
(N'AHA-BHA-PHA hỗ trợ làm sạch tế bào chết, sáng da & giảm mụn', 0, '2025-10-07', 350000,
 'somebymi_miracle.jpg', N'Some By Mi 30 Days Miracle Serum', 110, 1, 10, 9,
 '2025-04-01', '2028-04-01', 0),

-- 26. Melano CC Vitamin C Brightening Serum
(N'Vitamin C tinh khiết dưỡng sáng, hỗ trợ mờ thâm', 0, '2025-10-07', 220000,
 'melano_cc_vitc.jpg', N'Melano CC Vitamin C Brightening Serum 20ml', 150, 1, 10, 10,
 '2025-08-01', '2028-08-01', 0),

-- 27. Klairs Freshly Juiced Vitamin C Drop 5%
(N'Serum Vitamin C 5% dịu nhẹ cho da nhạy cảm, làm sáng tone', 0, '2025-10-07', 320000,
 'klairs_vitc_drop.jpeg', N'Klairs Freshly Juiced Vitamin C Drop 35ml', 70, 1, 10, 11,
 '2025-06-15', '2028-06-15', 0),

-- 28. L''Oréal Revitalift 1.5% Hyaluronic Acid Serum
(N'HA 1.5% đa kích thước cấp ẩm sâu, da căng mịn', 5, '2025-10-07', 360000,
 'loreal_revitalift_ha15.jpg', N'L''Oréal Revitalift 1.5% Hyaluronic Acid Serum', 130, 1, 10, 3,
 '2025-05-20', '2028-05-20', 0),

-- 29. Paula''s Choice 10% Niacinamide Booster
(N'Booster 10% Niacinamide hỗ trợ se lỗ chân lông & đều màu', 0, '2025-10-07', 1790000,
 'paulaschoice_niacinamide10.jpg', N'Paula''s Choice 10% Niacinamide Booster 20ml', 40, 1, 10, 12,
 '2025-03-01', '2028-03-01', 0),

-- 30. Hada Labo Retinol B3 Serum (Pro-Aging)
(N'Retinol + Vitamin B3 hỗ trợ cải thiện dấu hiệu lão hóa', 10, '2025-10-07', 300000,
 'hadalabo_retinol_b3.jpg', N'Hada Labo Retinol B3 Pro-Aging Serum 30ml', 85, 1, 10, 13,
 '2025-06-01', '2028-06-01', 0);
GO
/* ===================== KEM CHỐNG NẮNG (KCN) ===================== */
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
SET NOCOUNT ON;

-- Đảm bảo category tồn tại
    INSERT INTO categories(category_name)
    SELECT N'Tay te bao chet'
    WHERE NOT EXISTS (SELECT 1 FROM categories WHERE category_name=N'Tay te bao chet');

INSERT INTO categories(category_name)
SELECT N'Nuoc hoa'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE category_name=N'Nuoc hoa');

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
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Toner'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Simple'),
 DATEFROMPARTS(2025,7,1), DATEFROMPARTS(2027,7,1), 0),

-- 2. Klairs Supple Preparation Facial Toner
(N'Toner Klairs Supple Preparation cân bằng pH, dưỡng ẩm dịu nhẹ cho da nhạy cảm', 0, GETDATE(), 233000,
 N'klairs_supple_toner.png', N'Klairs Supple Preparation Facial Toner 180ml', 90, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Toner'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Klairs'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2027,6,1), 0),

-- 3. Skin1004 Madagascar Centella Toning Toner
(N'Toner chứa rau má Madagascar làm dịu, cấp ẩm và phục hồi da sau mụn', 0, GETDATE(), 312000,
 N'skin1004_centella_toner.png', N'Skin1004 Madagascar Centella Toning Toner 210ml', 100, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Toner'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Skin1004'),
 DATEFROMPARTS(2025,6,15), DATEFROMPARTS(2027,6,15), 0),

-- 4. Some By Mi AHA-BHA-PHA 30 Days Miracle Toner
(N'Toner tẩy tế bào chết nhẹ với AHA-BHA-PHA, hỗ trợ sáng da & giảm mụn trong 30 ngày', 0, GETDATE(), 289000,
 N'somebymi_miracle_toner.png', N'Some By Mi AHA-BHA-PHA 30 Days Miracle Toner 150ml', 130, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Toner'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Some By Mi'),
 DATEFROMPARTS(2025,5,1), DATEFROMPARTS(2027,5,1), 0),

-- 5. Pyunkang Yul Essence Toner
(N'Toner dưỡng ẩm sâu, chiết xuất rễ Hoàng Cầm giúp làm dịu & tăng đàn hồi cho da', 0, GETDATE(), 210000,
 N'pyunkangyul_essence_toner.png', N'Pyunkang Yul Essence Toner 200ml', 120, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Toner'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Pyunkang Yul'),
 DATEFROMPARTS(2025,4,20), DATEFROMPARTS(2027,4,20), 0),

-- 6. Cocoon Sen Hậu Giang Soothing Toner
(N'Toner thuần chay chiết xuất Sen Hậu Giang, cấp ẩm & làm dịu da nhạy cảm', 0, GETDATE(), 173000,
 N'cocoon_sen_toner.png', N'Cocoon Sen Hậu Giang Soothing Toner 140ml', 150, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Toner'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Cocoon'),
 DATEFROMPARTS(2025,7,10), DATEFROMPARTS(2027,7,10), 0),

-- 7. La Roche-Posay Effaclar Clarifying Toner
(N'Toner dược mỹ phẩm chứa BHA & LHA giúp làm sạch sâu, giảm bít tắc lỗ chân lông', 5, GETDATE(), 360000,
 N'larocheposay_effaclar_toner.png', N'La Roche-Posay Effaclar Clarifying Toner 200ml', 80, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Toner'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'La Roche-Posay'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2027,6,1), 0),

-- 8. Vichy Normaderm Purifying Pore-Tightening Toner
(N'Toner khoáng núi lửa Vichy giúp se khít lỗ chân lông, hỗ trợ kiềm dầu cho da mụn', 5, GETDATE(), 380000,
 N'vichy_normaderm_toner.png', N'Vichy Normaderm Purifying Pore-Tightening Toner 200ml', 90, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Toner'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Vichy'),
 DATEFROMPARTS(2025,5,1), DATEFROMPARTS(2027,5,1), 0),

-- 9. Hada Labo Gokujyun Hyaluronic Acid Lotion (Toner)
(N'Toner cấp ẩm chuyên sâu với 3 loại Hyaluronic Acid giúp da căng mượt & ẩm mịn', 0, GETDATE(), 245000,
 N'hadalabo_gokujyun_toner.png', N'Hada Labo Gokujyun Hyaluronic Acid Lotion 170ml', 140, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Toner'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Hada Labo'),
 DATEFROMPARTS(2025,5,15), DATEFROMPARTS(2027,5,15), 0),

-- 10. Bioderma Sensibio Tonique
(N'Toner Bioderma Sensibio dịu nhẹ, không cồn, làm mềm & phục hồi cân bằng cho da nhạy cảm', 0, GETDATE(), 320000,
 N'bioderma_sensibio_tonique.png', N'Bioderma Sensibio Tonique 250ml', 85, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Toner'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Bioderma'),
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
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Mặt nạ'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Innisfree'),
 DATEFROMPARTS(2025,5,1), DATEFROMPARTS(2028,5,1), 0),

-- 2. SKIN1004 Mad Centella Poremizing Clay Stick Mask
(N'Mask đất sét dạng stick SKIN1004 chiết xuất centella giúp làm sạch & dịu da', 0, GETDATE(), 263000,
 N'skin1004_centella_clay_stick.png', N'SKIN1004 Mad Centella Poremizing Clay Stick Mask 55g', 80, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Mặt nạ'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Skin1004'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- 3. Image Skincare Vital C Hydrating Enzyme Masque
(N'Mask enzyme + Vitamin C giúp tẩy tế bào chết nhẹ & cung cấp độ ẩm', 0, GETDATE(), 873499,
 N'image_vitalc_enzyme.png', N'Image Skincare Vital C Hydrating Enzyme Masque 57g', 50, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Mặt nạ'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Image Skincare'),
 DATEFROMPARTS(2025,4,1), DATEFROMPARTS(2028,4,1), 0),

-- 4. Colorkey Luminous B3 Brightening Facial Mask
(N'Mask làm sáng da với Niacinamide B3, giúp đều màu & dưỡng ẩm', 0, GETDATE(), 15000,
 N'colorkey_b3_mask.png', N'Colorkey Luminous B3 Brightening Facial Mask 30ml', 200, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Mặt nạ'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Colorkey'),
 DATEFROMPARTS(2025,7,1), DATEFROMPARTS(2027,7,1), 0),

-- 5. Rwine Placenta Face Mask
(N'Mask nhau thai (placenta) giúp dưỡng ẩm & tái tạo tế bào da', 0, GETDATE(), 11000,
 N'rwine_placenta_mask.png', N'Rwine Placenta Face Mask', 150, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Mặt nạ'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Rwine'),
 DATEFROMPARTS(2025,8,1), DATEFROMPARTS(2027,8,1), 0),

-- 6. Laneige Water Sleeping Mask Ex
(N'Mặt nạ ngủ Laneige giúp cấp ẩm sâu qua đêm, làm da mềm mịn khi ngủ dậy', 0, GETDATE(), 227000,
 N'laneige_water_sleeping_ex.png', N'Laneige Water Sleeping Mask Ex 70ml', 120, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Mặt nạ'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Laneige'),
 DATEFROMPARTS(2025,6,10), DATEFROMPARTS(2028,6,10), 0),

-- 7. Nature Republic Real Nature Orange Sheet Mask
(N'Mặt nạ giấy chiết xuất cam giúp cấp ẩm & sáng nhẹ cho da', 0, GETDATE(), 18000,
 N'nature_orange_sheet.png', N'Nature Republic Real Nature Orange Sheet Mask 23ml', 250, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Mặt nạ'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Nature Republic'),
 DATEFROMPARTS(2025,7,15), DATEFROMPARTS(2028,7,15), 0),

-- 8. Saborino Morning Mask
(N'Mask buổi sáng Saborino giúp tiết kiệm bước chăm da, dưỡng & làm sạch nhẹ', 0, GETDATE(), 364163,
 N'saborino_morning_mask.png', N'Saborino Morning Facial Mask', 90, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Mặt nạ'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Saborino'),
 DATEFROMPARTS(2025,5,20), DATEFROMPARTS(2028,5,20), 0),

-- 9. Caryophy Portulaca Mask Sheet
(N'Mặt nạ Caryophy giúp giảm mụn & làm dịu da với chiết xuất portulaca', 0, GETDATE(), 22950,
 N'caryophy_portulaca_mask.png', N'Caryophy Portulaca Mask Sheet 3-in-1', 180, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Mặt nạ'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Caryophy'),
 DATEFROMPARTS(2025,6,25), DATEFROMPARTS(2028,6,25), 0),

-- 10. Klairs Midnight Blue Calming Sheet Mask
(N'Mặt nạ Klairs Midnight Blue làm dịu da, giảm đỏ & bảo vệ da nhạy cảm', 0, GETDATE(), 44000,
 N'klairs_midnight_blue.png', N'Klairs Midnight Blue Calming Sheet Mask', 140, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Mặt nạ'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Klairs'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0);
GO


/* ==========
   10 NƯỚC HOA
   ========== */

INSERT INTO products(
  description, discount, entered_date, price, product_image, product_name,
  quantity, status, category_id, brand_id, manufacture_date, expiry_date, favorite
)
VALUES
-- 1. Chanel No.5 Eau de Parfum
(N'Nước hoa Chanel No.5 huyền thoại với hương hoa cổ điển, sang trọng và quyến rũ', 0, GETDATE(), 2500000,
 N'chanel_no5_edp.png', N'Chanel No.5 Eau de Parfum 50ml', 50, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Nuoc hoa'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Chanel'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- 2. Dior Sauvage Eau de Toilette
(N'Nước hoa nam Dior Sauvage với hương cay nồng, nam tính và cuốn hút', 0, GETDATE(), 2500000,
 N'dior_sauvage_edt.png', N'Dior Sauvage Eau de Toilette 60ml', 80, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Nuoc hoa'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Dior'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- 3. Versace Eros Eau de Toilette
(N'Nước hoa nam Versace Eros với hương ngọt ngào, quyến rũ và đầy năng lượng', 0, GETDATE(), 1200000,
 N'versace_eros_edt.png', N'Versace Eros Eau de Toilette 50ml', 70, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Nuoc hoa'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Versace'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- 4. Calvin Klein CK One Eau de Toilette
(N'Nước hoa unisex Calvin Klein CK One với hương tươi mát, trẻ trung và phổ biến', 0, GETDATE(), 800000,
 N'ck_one_edt.png', N'Calvin Klein CK One Eau de Toilette 100ml', 120, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Nuoc hoa'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Calvin Klein'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- 5. Hugo Boss Bottled Eau de Toilette
(N'Nước hoa nam Hugo Boss Bottled với hương gỗ ấm áp, nam tính và lịch lãm', 0, GETDATE(), 1300000,
 N'hugo_boss_bottled_edt.png', N'Hugo Boss Bottled Eau de Toilette 75ml', 90, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Nuoc hoa'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Hugo Boss'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- 6. Lacoste L.12.12 Blanc Eau de Toilette
(N'Nước hoa nam Lacoste L.12.12 Blanc với hương tươi mát, thể thao và năng động', 0, GETDATE(), 1000000,
 N'lacoste_l1212_blanc_edt.png', N'Lacoste L.12.12 Blanc Eau de Toilette 75ml', 100, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Nuoc hoa'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Lacoste'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- 7. Montblanc Legend Eau de Toilette
(N'Nước hoa nam Montblanc Legend với hương gỗ sang trọng, nam tính và quyến rũ', 0, GETDATE(), 1200000,
 N'montblanc_legend_edt.png', N'Montblanc Legend Eau de Toilette 50ml', 85, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Nuoc hoa'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Montblanc'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- 8. Burberry Brit Eau de Toilette
(N'Nước hoa nam Burberry Brit với hương cổ điển, sang trọng và lịch lãm', 0, GETDATE(), 1100000,
 N'burberry_brit_edt.png', N'Burberry Brit Eau de Toilette 50ml', 75, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Nuoc hoa'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Burberry'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- 9. Tommy Hilfiger Tommy Eau de Toilette
(N'Nước hoa nam Tommy Hilfiger Tommy với hương tươi mát, trẻ trung và năng động', 0, GETDATE(), 1300000,
 N'tommy_hilfiger_tommy_edt.png', N'Tommy Hilfiger Tommy Eau de Toilette 100ml', 110, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Nuoc hoa'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Tommy Hilfiger'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0),

-- 10. Chanel Coco Mademoiselle Eau de Parfum
(N'Nước hoa nữ Chanel Coco Mademoiselle với hương hoa quyến rũ, sang trọng và nữ tính', 0, GETDATE(), 4400000,
 N'chanel_coco_mademoiselle_edp.png', N'Chanel Coco Mademoiselle Eau de Parfum 50ml', 60, 1,
 (SELECT MIN(category_id) FROM categories WHERE category_name=N'Nuoc hoa'),
 (SELECT MIN(brand_id) FROM brands WHERE brand_name=N'Chanel'),
 DATEFROMPARTS(2025,6,1), DATEFROMPARTS(2028,6,1), 0);
GO
