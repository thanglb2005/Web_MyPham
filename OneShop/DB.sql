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
-- 9 user (đã thêm shipper và CSKH)
INSERT INTO dbo.[user](avatar, email, name, password, register_date, status)
VALUES 
('user.png','chi@gmail.com',N'Trần Thảo Chi','123456','2025-09-04',1),
('user.png','dong@gmail.com',N'Trần Hữu Đồng','123456','2025-09-04',1),
('user.png','user@gmail.com',N'User Demo','123456','2025-09-04',1),
('user.png','admin@mypham.com',N'Admin Mỹ Phẩm','123456','2025-09-04',1),
('user.png','vendor@mypham.com',N'Nguyễn Văn An','123456','2025-10-07',1),
('user.png','vendor1@mypham.com',N'Trần Thị Bình','123456','2025-10-07',1),
('user.png','vendor2@mypham.com',N'Lê Quốc Cường','123456','2025-10-07',1),
('user.png','shipper@mypham.com',N'Phạm Văn Giao','123456','2025-10-08',1),
('user.png','cskh@mypham.com',N'Nguyễn Thị Linh - CSKH','123456','2025-10-10',1);
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
('ROLE_SHIPPER'),
('ROLE_CSKH');
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
(1,1),  -- chi: user thường
(2,1),  -- dong: user thường  
(3,1),  -- user: user thường
(4,2),  -- admin: ADMIN
(5,1),(5,3),  -- vendor An: USER + VENDOR
(6,1),(6,3),  -- vendor Bình: USER + VENDOR
(7,1),(7,3),  -- vendor Cường: USER + VENDOR
(8,4),  -- shipper
(9,5);  -- cskh: CSKH
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
GO

/* ===============================
   TABLE: shops (MỚI - THÊM TRƯỚC PRODUCTS)
   =============================== */
IF OBJECT_ID('dbo.shops', 'U') IS NOT NULL DROP TABLE dbo.shops;
CREATE TABLE dbo.shops (
    shop_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    
    -- Thông tin cơ bản
    shop_name NVARCHAR(255) NOT NULL UNIQUE,
    shop_slug NVARCHAR(100) UNIQUE,
    shop_description NVARCHAR(2000),
    shop_logo NVARCHAR(255),
    shop_banner NVARCHAR(255),
    
    -- Liên kết vendor (1 vendor có thể có nhiều shop)
    vendor_id BIGINT NOT NULL,
    
    -- Thông tin liên hệ
    phone_number NVARCHAR(15),
    address NVARCHAR(500),
    city NVARCHAR(100),
    district NVARCHAR(100),
    ward NVARCHAR(100),
    
    -- Trạng thái: PENDING, ACTIVE, SUSPENDED, REJECTED
    status NVARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME2 DEFAULT GETDATE(),
    approved_at DATETIME2,
    rejection_reason NVARCHAR(500),
    
    -- Thống kê
    total_products INT DEFAULT 0,
    total_orders INT DEFAULT 0,
    total_revenue DECIMAL(18,2) DEFAULT 0,
    
    -- Cài đặt
    allow_cod BIT DEFAULT 1,
    preparation_days INT DEFAULT 2,
    
    CONSTRAINT FK_shops_vendor FOREIGN KEY(vendor_id) REFERENCES dbo.[user](user_id)
);

-- Insert 3 shop mẫu
INSERT INTO dbo.shops(
    shop_name, shop_slug, shop_description, shop_logo, shop_banner,
    vendor_id, phone_number, address, city, district, ward,
    status, created_at, approved_at,
    total_products, total_orders, total_revenue,
    allow_cod, preparation_days
)
VALUES
-- Shop 1: Vendor An (user_id=5) - ACTIVE
(N'Mỹ Phẩm An Nguyễn', 'my-pham-an-nguyen',
 N'Chuyên cung cấp mỹ phẩm chính hãng từ Hàn Quốc, Nhật Bản. Cam kết 100% hàng chính hãng.',
 'shop_an_logo.png', 'shop_an_banner.jpg',
 5, '0901234567', N'123 Nguyễn Huệ', N'TP. Hồ Chí Minh', N'Quận 1', N'Phường Bến Nghé',
 'ACTIVE', '2025-09-10', '2025-09-12',
 0, 0, 0, 1, 2),

-- Shop 2: Vendor Bình (user_id=6) - ACTIVE
(N'Cosmetic House Bình', 'cosmetic-house-binh',
 N'Chuyên dược mỹ phẩm cao cấp: La Roche-Posay, Vichy, Eucerin. Uy tín - Chất lượng.',
 'shop_binh_logo.png', 'shop_binh_banner.jpg',
 6, '0912345678', N'456 Lê Lợi', N'TP. Hồ Chí Minh', N'Quận 1', N'Phường Phạm Ngũ Lão',
 'ACTIVE', '2025-09-15', '2025-09-17',
 0, 0, 0, 1, 1),

-- Shop 3: Vendor Cường (user_id=7) - PENDING (chờ duyệt)
(N'Shop Mỹ Phẩm Cường', 'shop-my-pham-cuong',
 N'Mỹ phẩm thiên nhiên Việt Nam: Cocoon, Lemonade. Thuần chay - An toàn.',
 'shop_cuong_logo.png', 'shop_cuong_banner.jpg',
 7, '0923456789', N'789 Trần Hưng Đạo', N'TP. Hồ Chí Minh', N'Quận 1', N'Phường Cầu Kho',
 'PENDING', '2025-10-05', NULL,
 0, 0, 0, 1, 3);
GO

/* ===============================
   TABLE: products (ĐÃ THÊM shop_id)
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
    shop_id BIGINT NULL, -- NULL = sản phẩm của admin/platform
    manufacture_date DATE,
    expiry_date DATE,
    favorite BIT DEFAULT 0,
    CONSTRAINT FK_products_categories FOREIGN KEY(category_id) REFERENCES dbo.categories(category_id),
    CONSTRAINT FK_products_brands FOREIGN KEY(brand_id) REFERENCES dbo.brands(brand_id),
    CONSTRAINT FK_products_shops FOREIGN KEY(shop_id) REFERENCES dbo.shops(shop_id)
);
CREATE INDEX IX_products_shop ON dbo.products(shop_id);
GO
/* ===============================
   TABLE: favorites
   =============================== */
IF OBJECT_ID('dbo.favorites', 'U') IS NOT NULL DROP TABLE dbo.favorites;
CREATE TABLE dbo.favorites (
    favorite_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    CONSTRAINT FK_favorites_user FOREIGN KEY(user_id) REFERENCES dbo.[user](user_id),
    CONSTRAINT FK_favorites_product FOREIGN KEY(product_id) REFERENCES dbo.products(product_id),
    CONSTRAINT UK_favorites_user_product UNIQUE(user_id, product_id)
);
CREATE INDEX IX_favorites_user ON dbo.favorites(user_id);
CREATE INDEX IX_favorites_product ON dbo.favorites(product_id);
GO

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
    cancelled_date DATETIME2,
    cancellation_reason NVARCHAR(MAX),
    tracking_number NVARCHAR(100),
    shipping_fee DECIMAL(10,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    final_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    payment_status BIT NOT NULL DEFAULT 0,
    payment_date DATETIME2,
    estimated_delivery_date DATETIME2,
    shipper_id BIGINT NULL,
    shop_id BIGINT NULL,
    CONSTRAINT FK_orders_user FOREIGN KEY(user_id) REFERENCES dbo.[user](user_id),
    CONSTRAINT FK_orders_shipper FOREIGN KEY(shipper_id) REFERENCES dbo.[user](user_id),
    CONSTRAINT FK_orders_shop FOREIGN KEY(shop_id) REFERENCES dbo.shops(shop_id)
);

-- Create indexes for better performance
CREATE INDEX IX_orders_status ON orders(status);
CREATE INDEX IX_orders_user_id ON orders(user_id);
CREATE INDEX IX_orders_shop_id ON orders(shop_id);
CREATE INDEX IX_orders_order_date ON orders(order_date);
CREATE INDEX IX_orders_payment_status ON orders(payment_status);
CREATE INDEX IX_orders_payment_method ON orders(payment_method);
GO

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
GO

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
GO

/* duplicate favorites table removed to keep the earlier definition with UNIQUE constraint */

/* ===============================
   TABLE: chat_message
   =============================== */
IF OBJECT_ID('dbo.chat_message', 'U') IS NOT NULL DROP TABLE dbo.chat_message;
CREATE TABLE dbo.chat_message (
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
CREATE INDEX ix_chat_message_room_time ON dbo.chat_message(room_id, sent_at DESC);
GO

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

/* ===================== INSERT BRANDS ===================== */
INSERT INTO brands(brand_name, brand_image, description, origin, status)
VALUES
(N'La Roche-Posay', 'laroche.png', N'Dược mỹ phẩm cho da nhạy cảm', N'Pháp', 1),
(N'Olay', 'olay.png', N'Dưỡng ẩm và chống lão hoá', N'Mỹ', 1),
(N'Yves Saint Laurent (YSL)', 'ysl.png', N'Thương hiệu cao cấp Pháp', N'Pháp', 1),
(N'ZO Skin Health', 'zo.png', N'Chăm sóc da chuyên sâu', N'Mỹ', 1),
(N'3CE (3 Concept Eyes)', '3ce.png', N'Makeup Hàn Quốc', N'Hàn Quốc', 1),
(N'Avène', 'avene.png', N'Dược mỹ phẩm suối khoáng', N'Pháp', 1),
(N'Chanel', 'chanel.png', N'Thương hiệu cao cấp Pháp', N'Pháp', 1),
(N'Eucerin', 'eucerin.png', N'Dược mỹ phẩm Đức', N'Đức', 1),
(N'Sebamed', 'sebamed.png', N'pH 5.5 dịu nhẹ', N'Đức', 1),
(N'Aloins', 'aloins.png', N'Kem dưỡng nha đam Nhật', N'Nhật Bản', 1),
(N'CeraVe', 'cerave.png', N'Ceramide & HA', N'Mỹ', 1),
(N'Ailus', 'ailus.png', N'Son bình dân Nhật', N'Nhật Bản', 1),
(N'The Ordinary', 'the_ordinary.jpg', N'Mỹ phẩm tối giản', N'Canada', 1),
(N'Vichy', 'vichy.jpg', N'Khoáng núi lửa', N'Pháp', 1),
(N'Skin1004', 'skin1004.jpg', N'Rau má Madagascar', N'Hàn Quốc', 1),
(N'Some By Mi', 'some_by_mi.jpg', N'AHA-BHA-PHA', N'Hàn Quốc', 1),
(N'Melano CC', 'melano_cc.jpg', N'Vitamin C Nhật', N'Nhật Bản', 1),
(N'Klairs', 'klairs.jpg', N'Vitamin C dịu nhẹ', N'Hàn Quốc', 1),
(N'Paula''s Choice', 'paulas_choice.jpg', N'Booster chuyên trị', N'Mỹ', 1),
(N'Hada Labo', 'hada_labo.jpg', N'Dưỡng ẩm HA', N'Nhật Bản', 1),
(N'Cocoon', 'cocoon.jpg', N'Vegan Việt Nam', N'Việt Nam', 1),
(N'Cetaphil', 'cetaphil.jpg', N'Dược mỹ phẩm Canada', N'Canada', 1),
(N'Fixderma', 'fixderma.jpg', N'Dược mỹ phẩm Ấn Độ', N'Ấn Độ', 1),
(N'L''Oréal Paris', 'loreal_paris.jpg', N'Thương hiệu Pháp', N'Pháp', 1),
(N'Sắc Ngọc Khang', 'sacngockhang.jpg', N'Thuộc Hoa Linh', N'Việt Nam', 1),
(N'Reihaku Hatomugi', 'reihaku_hatomugi.jpg', N'Hatomugi dưỡng ẩm', N'Nhật Bản', 1),
(N'Innisfree', 'innisfree.jpg', N'Thiên nhiên Jeju', N'Hàn Quốc', 1),
(N'Catrice', 'catrice.jpg', N'Mỹ phẩm Đức', N'Đức', 1),
(N'Eglips', 'eglips.jpg', N'Phấn phủ kiềm dầu', N'Hàn Quốc', 1),
(N'I''m Meme', 'im_meme.jpg', N'Phong cách trẻ trung', N'Hàn Quốc', 1),
(N'Lemonade', 'lemonade.jpg', N'Thuần chay VN', N'Việt Nam', 1),
(N'Silkygirl', 'silkygirl.jpg', N'Giá mềm Malaysia', N'Malaysia', 1),
(N'Too Cool For School', 'tcfs.jpg', N'Nghệ thuật Hàn', N'Hàn Quốc', 1),
(N'Dr.G', 'drg.jpg', N'Dược mỹ phẩm Hàn', N'Hàn Quốc', 1),
(N'Himalaya', 'himalaya.jpg', N'Thảo mộc Ấn Độ', N'Ấn Độ', 1),
(N'Exclusive Cosmetic', 'exclusivecosmetic.jpg', N'Mỹ phẩm Nga', N'Nga', 1),
(N'Meishoku', 'meishoku.jpg', N'Nội địa Nhật', N'Nhật Bản', 1),
(N'Naruko', 'naruko.jpg', N'Tràm trà Đài Loan', N'Đài Loan', 1),
(N'Organic Shop', 'organicshop.jpg', N'Hữu cơ thiên nhiên', N'Nga', 1),
(N'Simple', 'simple.png', N'Lành tính Anh Quốc', N'Anh Quốc', 1),
(N'Pyunkang Yul', 'pyunkangyul.png', N'Y học cổ truyền Hàn', N'Hàn Quốc', 1),
(N'Bioderma', 'bioderma.png', N'Dược mỹ phẩm Pháp', N'Pháp', 1),
(N'Colorkey', 'colorkey.png', N'Makeup Trung Quốc', N'Trung Quốc', 1),
(N'Rwine', 'rwine.png', N'Mask Nhật Bản', N'Nhật Bản', 1),
(N'Nature Republic', 'nature_republic.png', N'Thiên nhiên Hàn', N'Hàn Quốc', 1),
(N'Saborino', 'saborino.png', N'Mask buổi sáng', N'Nhật Bản', 1),
(N'Caryophy', 'caryophy.png', N'Trị mụn Hàn Quốc', N'Hàn Quốc', 1),
(N'The Face Shop', 'the_face_shop.png', N'Thiên nhiên Hàn', N'Hàn Quốc', 1),
(N'Anua', 'anua.png', N'Lành tính Hàn Quốc', N'Hàn Quốc', 1),
(N'SVR', 'svr.png', N'Dược mỹ phẩm Pháp', N'Pháp', 1),
(N'Image Skincare', 'image_skincare.png', N'Chuyên nghiệp Mỹ', N'Mỹ', 1),
(N'Laneige', 'laneige.png', N'Mỹ phẩm cao cấp Hàn', N'Hàn Quốc', 1),
(N'Dior', 'dior.png', N'Nước hoa Pháp', N'Pháp', 1),
(N'Versace', 'versace.png', N'Nước hoa Ý', N'Ý', 1),
(N'Calvin Klein', 'calvin_klein.png', N'Nước hoa Mỹ', N'Mỹ', 1),
(N'Hugo Boss', 'hugo_boss.png', N'Nước hoa Đức', N'Đức', 1),
(N'Lacoste', 'lacoste.png', N'Nước hoa thể thao', N'Pháp', 1),
(N'Montblanc', 'montblanc.png', N'Nước hoa Đức', N'Đức', 1),
(N'Burberry', 'burberry.png', N'Nước hoa Anh', N'Anh', 1),
(N'Tommy Hilfiger', 'tommy_hilfiger.png', N'Nước hoa Mỹ', N'Mỹ', 1);
GO

/* =====================================================================
   INSERT PRODUCTS
   - shop_id = NULL → Sản phẩm của ADMIN/PLATFORM (bán chính hãng)
   - shop_id = 1 → Sản phẩm của Shop "Mỹ Phẩm An Nguyễn" (vendor An)
   - shop_id = 2 → Sản phẩm của Shop "Cosmetic House Bình" (vendor Bình)
   
   PHÂN BỔ LOGIC:
   - Son môi (YSL, Chanel, 3CE, Ailus) → Shop 1 (An Nguyễn - chuyên makeup)
   - Dược mỹ phẩm (La Roche, Eucerin, Vichy, Avène) → Shop 2 (Bình - dược)
   - Sản phẩm cao cấp (ZO, Olay...) → Admin (platform bán chính hãng)
   - Sản phẩm bình dân → Vendor shops
   ===================================================================== */

INSERT INTO products(
  description, discount, entered_date, price, product_image, product_name,
  quantity, status, category_id, brand_id, shop_id, manufacture_date, expiry_date, favorite
)
VALUES
-- ═══════ SON MÔI - SHOP 1 (An Nguyễn) ═══════
(N'Son thỏi YSL Rouge Pur Couture #01 tông đỏ tươi rực rỡ, chất son lì sang trọng', 0, '2025-10-08', 1220000,
 'SonYSLDoTuoi.jpg', N'YSL Rouge Pur Couture #01 Đỏ Tươi', 70, 1, 1, 3, 1, '2025-06-01', '2028-06-01', 0),

(N'Son kem lì YSL Tatouage Couture Velvet Cream #216 tông hồng đất dịu nhẹ', 0, '2025-10-08', 1180000,
 'YSLHongDat.jpg', N'YSL Tatouage Couture Velvet Cream #216 Hồng Đất', 80, 1, 1, 3, 1, '2025-07-01', '2028-07-01', 0),

(N'Son thỏi Ailus M4V #03 tông cam cháy trẻ trung, giá bình dân', 0, '2025-10-08', 150000,
 'aliusCam.jpg', N'Ailus Stress Free Lipstick M4V #03 Cam Cháy', 100, 1, 1, 12, 1, '2025-06-20', '2028-06-20', 0),

(N'Son thỏi Ailus M3V #01 tông đỏ tươi, mềm mượt dễ tán', 0, '2025-10-08', 150000,
 'aliusDo.jpg', N'Ailus Stress Free Lipstick M3V #01 Đỏ Tươi', 110, 1, 1, 12, 1, '2025-06-25', '2028-06-25', 0),

(N'Son thỏi Chanel Rouge Coco Flash #116 tông cam san hô tươi sáng', 0, '2025-10-08', 1150000,
 'SonChanelCam.png', N'Chanel Rouge Coco Flash #116 Cam San Hô', 60, 1, 1, 7, 1, '2025-06-10', '2028-06-10', 0),

(N'Son thỏi Chanel Rouge Coco Flash #106 tông đỏ tươi cổ điển', 0, '2025-10-08', 1150000,
 'SonChanelDo.png', N'Chanel Rouge Coco Flash #106 Đỏ Tươi', 55, 1, 1, 7, 1, '2025-06-15', '2028-06-15', 0),

(N'Son thỏi Chanel Rouge Coco Flash #108 tông đỏ hồng nữ tính', 0, '2025-10-08', 1150000,
 'SonChannelDoHong.png', N'Chanel Rouge Coco Flash #108 Đỏ Hồng', 65, 1, 1, 7, 1, '2025-06-18', '2028-06-18', 0),

(N'Son lì 3CE Cashmere Hue Lipstick tông đỏ đất trendy', 0, '2025-10-08', 380000,
 'sonli3CE.jpg', N'3CE Cashmere Hue Lipstick Đỏ Đất', 90, 1, 1, 5, 1, '2025-07-05', '2028-07-05', 0),

(N'Son thỏi YSL Rouge Pur Couture The Slim tông đỏ quyến rũ', 5, '2025-10-08', 1250000,
 'sonSYL_Rouge.png', N'YSL Rouge Pur Couture The Slim Đỏ Quyến Rũ', 75, 1, 1, 3, 1, '2025-05-25', '2028-05-25', 0),

(N'Son kem lì YSL Tatouage Couture Matte Stain #13 tông đỏ cam', 5, '2025-10-08', 1180000,
 'SonYSLDoCam.jpg', N'YSL Tatouage Couture Matte Stain #13 Đỏ Cam', 70, 1, 1, 3, 1, '2025-06-05', '2028-06-05', 0),

-- ═══════ KEM DƯỠNG DA - SHOP 2 (Bình - Dược) & ADMIN ═══════
(N'Kem dưỡng Olay Total Effects 7 in One Day Moisturiser SPF30', 5, '2025-10-08', 280000,
 'KemOlay.png', N'Olay Total Effects 7 in One Day Moisturiser SPF30', 120, 1, 2, 2, NULL, '2025-07-01', '2028-07-01', 0),

(N'Kem dưỡng Sebamed Relief Face Cream 5% Urea dành cho da khô', 0, '2025-10-08', 320000,
 'kemSebamed.png', N'Sebamed Relief Face Cream 5% Urea', 100, 1, 2, 9, 2, '2025-06-15', '2028-06-15', 0),

(N'ZO Skin Health Retinol Skin Brightener 1% cải thiện sắc tố', 5, '2025-10-08', 2500000,
 'kemZO.jpg', N'ZO Skin Health Retinol Skin Brightener 1%', 60, 1, 2, 4, NULL, '2025-06-01', '2028-06-01', 0),

(N'Kem dưỡng ẩm Aloins Eaude Cream S chiết xuất nha đam', 0, '2025-10-08', 180000,
 'kemALONIS.png', N'Aloins Eaude Cream S Aloe Extract', 150, 1, 2, 10, 1, '2025-05-20', '2028-05-20', 0),

(N'Kem dưỡng ẩm CeraVe Moisturising Cream chứa Ceramides & HA', 0, '2025-10-08', 350000,
 'kemCeraVe.png', N'CeraVe Moisturising Cream 340g', 130, 1, 2, 11, 2, '2025-05-10', '2028-05-10', 0),

(N'Balm dưỡng ẩm Avène XeraCalm A.D dành cho da rất khô', 0, '2025-10-08', 420000,
 'kemEAU.png', N'Avène XeraCalm A.D Lipid-Replenishing Balm 200ml', 90, 1, 2, 6, 2, '2025-06-01', '2028-06-01', 0),

(N'Kem dưỡng ẩm Eucerin AQUAporin ACTIVE cấp nước tức thì', 0, '2025-10-08', 380000,
 'kemEucerin.png', N'Eucerin AQUAporin ACTIVE Moisturising Cream', 110, 1, 2, 8, 2, '2025-07-01', '2028-07-01', 0),

(N'Kem dưỡng Eucerin Q10 ACTIVE giảm nếp nhăn', 0, '2025-10-08', 450000,
 'kemEucerinSang.png', N'Eucerin Q10 ACTIVE Anti-Wrinkle Face Cream', 100, 1, 2, 8, 2, '2025-07-10', '2028-07-10', 0),

(N'Kem phục hồi La Roche-Posay Cicaplast Baume B5 làm dịu da', 0, '2025-10-08', 280000,
 'kemLaRoche.png', N'La Roche-Posay Cicaplast Baume B5', 150, 1, 2, 1, 2, '2025-06-20', '2028-06-20', 0),

(N'Kem dưỡng ZO Skin Health Pigment Control 4% Hydroquinone', 5, '2025-10-08', 3200000,
 'kemNamZO.png', N'ZO Skin Health Pigment Control Crème 4% Hydroquinone', 50, 1, 2, 4, NULL, '2025-06-15', '2028-06-15', 0),

-- ═══════ SERUM - SHOP 2 (Bình) & ADMIN ═══════
(N'Serum The Ordinary Niacinamide 10% + Zinc 1% kiểm dầu, se lỗ chân lông', 0, '2025-10-07', 250000,
 'theordinary_niacinamide.jpg', N'The Ordinary Niacinamide 10% + Zinc 1%', 120, 1, 10, 13, NULL, '2025-07-01', '2028-07-01', 0),

(N'Serum La Roche-Posay Hyalu B5 phục hồi hàng rào da', 5, '2025-10-07', 1600000,
 'larocheposay_hyalu_b5.jpg', N'La Roche-Posay Hyalu B5 Serum', 80, 1, 10, 1, 2, '2025-06-01', '2028-06-01', 0),

(N'Dưỡng chất khoáng Vichy Minéral 89% + HA', 10, '2025-10-07', 850000,
 'vichy_mineral89.jpg', N'Vichy Minéral 89 Hyaluronic Acid Serum', 100, 1, 10, 14, 2, '2025-05-01', '2028-05-01', 0),

(N'Chiết xuất rau má Madagascar làm dịu da', 0, '2025-10-07', 300000,
 'skin1004_centella.jpg', N'Skin1004 Madagascar Centella Ampoule 55ml', 90, 1, 10, 15, 2, '2025-07-10', '2028-07-10', 0),

(N'Some By Mi AHA-BHA-PHA hỗ trợ sáng da & giảm mụn', 0, '2025-10-07', 350000,
 'somebymi_miracle.jpg', N'Some By Mi 30 Days Miracle Serum', 110, 1, 10, 16, 2, '2025-04-01', '2028-04-01', 0),

(N'Melano CC Vitamin C tinh khiết dưỡng sáng', 0, '2025-10-07', 220000,
 'melano_cc_vitc.jpg', N'Melano CC Vitamin C Brightening Serum 20ml', 150, 1, 10, 17, 1, '2025-08-01', '2028-08-01', 0),

(N'Serum Klairs Vitamin C 5% dịu nhẹ cho da nhạy cảm', 0, '2025-10-07', 320000,
 'klairs_vitc_drop.jpeg', N'Klairs Freshly Juiced Vitamin C Drop 35ml', 70, 1, 10, 18, 2, '2025-06-15', '2028-06-15', 0),

(N'L''Oréal Revitalift HA 1.5% đa kích thước cấp ẩm sâu', 5, '2025-10-07', 360000,
 'loreal_revitalift_ha15.jpg', N'L''Oréal Revitalift 1.5% Hyaluronic Acid Serum', 130, 1, 10, 24, NULL, '2025-05-20', '2028-05-20', 0),

(N'Paula''s Choice 10% Niacinamide Booster se lỗ chân lông', 0, '2025-10-07', 1790000,
 'paulaschoice_niacinamide10.jpg', N'Paula''s Choice 10% Niacinamide Booster 20ml', 40, 1, 10, 19, NULL, '2025-03-01', '2028-03-01', 0),

(N'Hada Labo Retinol B3 Serum cải thiện dấu hiệu lão hóa', 10, '2025-10-07', 300000,
 'hadalabo_retinol_b3.jpg', N'Hada Labo Retinol B3 Pro-Aging Serum 30ml', 85, 1, 10, 20, 1, '2025-06-01', '2028-06-01', 0);
GO

/* ===================== KEM CHỐNG NẮNG - SHOP 1 & 2 & ADMIN ===================== */
DECLARE @catKCN int = (SELECT category_id FROM categories WHERE category_name=N'Kem chống nắng');

INSERT INTO products(
  description, discount, entered_date, price, product_image, product_name,
  quantity, status, category_id, brand_id, shop_id, manufacture_date, expiry_date, favorite
)
VALUES
(N'Kem chống nắng bí đao Cocoon SPF50+ PA++++, nhẹ mặt', 0, GETDATE(), 245000,
 N'cocoon_winter_melon_spf50.png', N'COCOON Winter Melon Sunscreen SPF50+ PA++++ 50ml',
 60, 1, @catKCN, (SELECT brand_id FROM brands WHERE brand_name=N'Cocoon'), 1, '2025-06-01', '2028-06-01', 0),

(N'Cetaphil Sun Light Gel SPF50+ PA++++, không nhờn rít', 0, GETDATE(), 390000,
 N'cetaphil_sun_spf50_light_gel.png', N'Cetaphil Sun Light Gel SPF50+ PA++++ 50ml',
 80, 1, @catKCN, (SELECT brand_id FROM brands WHERE brand_name=N'Cetaphil'), 2, '2025-06-01', '2028-06-01', 0),

(N'Vichy Capital Soleil Dry Touch SPF50 PA++++, kiềm dầu', 0, GETDATE(), 495000,
 N'vichy_capital_soleil_dry_touch_spf50.png', N'Vichy Capital Soleil Dry Touch SPF50 50ml',
 90, 1, @catKCN, (SELECT brand_id FROM brands WHERE brand_name=N'Vichy'), 2, '2025-05-01', '2028-05-01', 0),

(N'Fixderma Shadow SPF50+ PA+++, chống nắng mạnh', 0, GETDATE(), 260000,
 N'fixderma_shadow_spf50_cream.png', N'Fixderma Shadow SPF50+ Cream 75g',
 70, 1, @catKCN, (SELECT brand_id FROM brands WHERE brand_name=N'Fixderma'), NULL, '2025-04-01', '2028-04-01', 0),

(N'L''Oréal UV Defender Invisible Fluid SPF50+ PA++++, thấm nhanh', 0, GETDATE(), 330000,
 N'loreal_uv_defender_invisible_fluid_spf50.png', N'L''Oréal UV Defender Invisible Fluid SPF50+ 50ml',
 100, 1, @catKCN, (SELECT brand_id FROM brands WHERE brand_name=N'L''Oréal Paris'), NULL, '2025-06-01', '2028-06-01', 0),

(N'La Roche-Posay Anthelios UVMune 400 Oil Control Fluid SPF50+', 0, GETDATE(), 620000,
 N'larocheposay_uvmune400_oil_control_spf50.png', N'La Roche-Posay Anthelios UVMune 400 Oil Control SPF50+ 50ml',
 120, 1, @catKCN, (SELECT brand_id FROM brands WHERE brand_name=N'La Roche-Posay'), 2, '2025-06-01', '2028-06-01', 0),

(N'Anthelios XL Dry Touch Gel-Cream SPF50+, chống bóng nhờn', 0, GETDATE(), 580000,
 N'larocheposay_anthelios_xl_dry_touch_spf50.png', N'La Roche-Posay Anthelios XL Dry Touch SPF50+ 50ml',
 90, 1, @catKCN, (SELECT brand_id FROM brands WHERE brand_name=N'La Roche-Posay'), 2, '2025-05-01', '2028-05-01', 0),

(N'Sắc Ngọc Khang Tone Up Sun Gel-Cream SPF50+ PA++++', 0, GETDATE(), 155000,
 N'sac_ngoc_khang_tone_up_spf50.png', N'Sắc Ngọc Khang Tone Up Sun SPF50+ 50g',
 110, 1, @catKCN, (SELECT brand_id FROM brands WHERE brand_name=N'Sắc Ngọc Khang'), 1, '2025-06-01', '2028-06-01', 0),

(N'Sebamed Anti-Redness Light Day Care SPF20', 0, GETDATE(), 420000,
 N'sebamed_anti_redness_day_spf20.png', N'Sebamed Anti-Redness Light Day Care SPF20 50ml',
 50, 1, @catKCN, (SELECT brand_id FROM brands WHERE brand_name=N'Sebamed'), 2, '2025-05-01', '2027-05-01', 0),

(N'Reihaku Hatomugi UV Milky Gel SPF50+ PA++++', 0, GETDATE(), 210000,
 N'reihaku_hatomugi_uv_milky_gel_spf50.png', N'Reihaku Hatomugi UV Milky Gel SPF50+ 80g',
 140, 1, @catKCN, (SELECT brand_id FROM brands WHERE brand_name=N'Reihaku Hatomugi'), 1, '2025-06-01', '2028-06-01', 0);
GO

/* ===================== PHẤN PHỦ - SHOP 1 (An) ===================== */
DECLARE @catPP int = (SELECT category_id FROM categories WHERE category_name=N'Phấn phủ');

INSERT INTO products(
  description, discount, entered_date, price, product_image, product_name,
  quantity, status, category_id, brand_id, shop_id, manufacture_date, expiry_date, favorite
)
VALUES
(N'Phấn phủ bột kiềm dầu Innisfree No-Sebum', 0, GETDATE(), 165000,
 'innisfree-no-sebum-mineral-5g.png', N'Innisfree No-Sebum Mineral Powder 5g', 
 120, 1, @catPP, (SELECT brand_id FROM brands WHERE brand_name=N'Innisfree'), 1, '2025-06-01', '2028-06-01', 0),

(N'Phấn nền kiềm dầu Catrice All Matt Plus', 0, GETDATE(), 195000,
 'catrice-all-matt-10g.png', N'Catrice All Matt Plus Shine Control Powder 10g', 
 150, 1, @catPP, (SELECT brand_id FROM brands WHERE brand_name=N'Catrice'), 1, '2025-05-01', '2028-05-01', 0),

(N'Phấn nền Eglips Oil Cut Powder Pact', 0, GETDATE(), 175000,
 'eglips-oil-cut-pact.png', N'Eglips Oil Cut Powder Pact', 
 140, 1, @catPP, (SELECT brand_id FROM brands WHERE brand_name=N'Eglips'), 1, '2025-04-01', '2028-04-01', 0),

(N'Phấn nền Eglips Glow Powder Pact', 0, GETDATE(), 185000,
 'eglips-glow-pact.png', N'Eglips Glow Powder Pact', 
 120, 1, @catPP, (SELECT brand_id FROM brands WHERE brand_name=N'Eglips'), 1, '2025-06-01', '2028-06-01', 0),

(N'Phấn Eglips Air Fit Powder Pact 8g', 0, GETDATE(), 185000,
 'eglips-air-fit-8g.png', N'Eglips Air Fit Powder Pact 8g', 
 130, 1, @catPP, (SELECT brand_id FROM brands WHERE brand_name=N'Eglips'), 1, '2025-05-01', '2028-05-01', 0),

(N'Phấn I''m Meme Oil Cut Pact 9.5g', 0, GETDATE(), 210000,
 'im-meme-oil-cut-9-5g.png', N'I''m Meme Oil Cut Pact 9.5g', 
 110, 1, @catPP, (SELECT brand_id FROM brands WHERE brand_name=N'I''m Meme'), 1, '2025-06-01', '2028-06-01', 0),

(N'Phấn bột Lemonade Supermatte thuần chay', 0, GETDATE(), 245000,
 'lemonade-supermatte-9g.png', N'Lemonade Supermatte No Makeup Loose Powder 9g', 
 100, 1, @catPP, (SELECT brand_id FROM brands WHERE brand_name=N'Lemonade'), 1, '2025-06-01', '2028-06-01', 0),

(N'Phấn bột Silkygirl No-Sebum Mineral Powder', 0, GETDATE(), 120000,
 'silkygirl-no-sebum-5g.png', N'Silkygirl No-Sebum Mineral Powder 5g', 
 160, 1, @catPP, (SELECT brand_id FROM brands WHERE brand_name=N'Silkygirl'), 1, '2025-04-01', '2028-04-01', 0),

(N'Phấn Silkygirl Let It Glow Tone Up Powder', 0, GETDATE(), 155000,
 'silkygirl-let-it-glow-7g.png', N'Silkygirl Let It Glow Tone Up Powder 7g', 
 140, 1, @catPP, (SELECT brand_id FROM brands WHERE brand_name=N'Silkygirl'), 1, '2025-05-01', '2028-05-01', 0),

(N'Phấn Too Cool For School Artclass By Rodin', 0, GETDATE(), 320000,
 'tcfs-artclass-rodin-4g.png', N'Too Cool For School Artclass By Rodin Finish Setting Pact 4g', 
 90, 1, @catPP, (SELECT brand_id FROM brands WHERE brand_name=N'Too Cool For School'), 1, '2025-06-01', '2028-06-01', 0);
GO

/* ===================== TẨY TẾ BÀO CHẾT - ADMIN & SHOP ===================== */
DECLARE @catTBC int = (SELECT category_id FROM categories WHERE category_name=N'Tẩy tế bào chết');

INSERT INTO products(
  description, discount, entered_date, price, product_image, product_name,
  quantity, status, category_id, brand_id, shop_id, manufacture_date, expiry_date, favorite
)
VALUES
(N'Gel tẩy tế bào chết Dr.G dịu nhẹ, làm sáng da', 0, GETDATE(), 290000,
 'drg-brightening-peeling-gel-120g.png', N'Dr.G Brightening Peeling Gel 120g', 
 80, 1, @catTBC, (SELECT brand_id FROM brands WHERE brand_name=N'Dr.G'), 2, '2025-06-01', '2028-06-01', 0),

(N'Scrub Eucerin Pro Acne hỗ trợ giảm dầu, hạn chế mụn', 0, GETDATE(), 320000,
 'eucerin-pro-acne-scrub-100ml.png', N'Eucerin Pro Acne Solution Scrub 100ml', 
 90, 1, @catTBC, (SELECT brand_id FROM brands WHERE brand_name=N'Eucerin'), 2, '2025-06-01', '2028-06-01', 0),

(N'Tẩy da chết Himalaya neem và mơ, ngừa mụn', 0, GETDATE(), 120000,
 'himalaya-neem-scrub-100ml.png', N'Himalaya Purifying Neem Scrub 100ml', 
 150, 1, @catTBC, (SELECT brand_id FROM brands WHERE brand_name=N'Himalaya'), NULL, '2025-06-01', '2028-06-01', 0),

(N'Tẩy tế bào chết hóa học Paula''s Choice BHA 2%', 0, GETDATE(), 380000,
 'paulaschoice-bha-2-liquid-30ml.png', N'Paula''s Choice 2% BHA Liquid Exfoliant 30ml', 
 100, 1, @catTBC, (SELECT brand_id FROM brands WHERE brand_name=N'Paula''s Choice'), NULL, '2025-06-01', '2028-06-01', 0),

(N'Gel scrub cà phê Exclusive Cosmetic Nga', 0, GETDATE(), 95000,
 'exclusive-cosmetic-coffee-scrub-100g.png', N'Exclusive Cosmetic Coffee Gel Scrub 100g', 
 120, 1, @catTBC, (SELECT brand_id FROM brands WHERE brand_name=N'Exclusive Cosmetic'), 1, '2025-06-01', '2028-06-01', 0),

(N'Gel tẩy tế bào chết Meishoku Detclear AHA BHA', 0, GETDATE(), 310000,
 'meishoku-detclear-peeling-jelly-180ml.png', N'Meishoku Detclear Bright & Peel Peeling Jelly 180ml', 
 110, 1, @catTBC, (SELECT brand_id FROM brands WHERE brand_name=N'Meishoku'), 1, '2025-06-01', '2028-06-01', 0),

(N'Tẩy tế bào chết môi Cocoon cà phê Đắk Lắk', 0, GETDATE(), 85000,
 'cocoon-dak-lak-coffee-lip-scrub-5g.png', N'Cocoon Dak Lak Coffee Lip Scrub 5g', 
 200, 1, @catTBC, (SELECT brand_id FROM brands WHERE brand_name=N'Cocoon'), 1, '2025-06-01', '2028-06-01', 0),

(N'Gel tẩy tế bào chết Naruko tràm trà', 0, GETDATE(), 260000,
 'naruko-tea-tree-peeling-gel-120ml.png', N'Naruko Tea Tree Peeling Gel 120ml', 
 100, 1, @catTBC, (SELECT brand_id FROM brands WHERE brand_name=N'Naruko'), 2, '2025-06-01', '2028-06-01', 0),

(N'Tẩy tế bào chết Organic Shop chiết xuất cà phê', 0, GETDATE(), 145000,
 'organicshop-soft-face-gommage-coffee-75ml.png', N'Organic Shop Soft Face Gommage Coffee 75ml', 
 90, 1, @catTBC, (SELECT brand_id FROM brands WHERE brand_name=N'Organic Shop'), 1, '2025-06-01', '2028-06-01', 0),

(N'Tẩy tế bào chết toàn thân Organic Shop Body Desserts', 0, GETDATE(), 265000,
 'organicshop-body-desserts-scrub-450ml.png', N'Organic Shop Body Desserts Body Scrub 450ml', 
 70, 1, @catTBC, (SELECT brand_id FROM brands WHERE brand_name=N'Organic Shop'), 1, '2025-06-01', '2028-06-01', 0);
GO

/* ===================== TONER - SHOP 2 (Bình) & ADMIN ===================== */
DECLARE @catToner int = (SELECT category_id FROM categories WHERE category_name=N'Toner');

INSERT INTO products(
  description, discount, entered_date, price, product_image, product_name,
  quantity, status, category_id, brand_id, shop_id, manufacture_date, expiry_date, favorite
)
VALUES
(N'Toner Simple dịu nhẹ, không cồn', 0, GETDATE(), 107000,
 N'simple_soothing_toner.png', N'Simple Kind To Skin Soothing Facial Toner 200ml', 
 120, 1, @catToner, (SELECT brand_id FROM brands WHERE brand_name=N'Simple'), NULL, '2025-07-01', '2027-07-01', 0),

(N'Toner Klairs Supple Preparation cân bằng pH', 0, GETDATE(), 233000,
 N'klairs_supple_toner.png', N'Klairs Supple Preparation Facial Toner 180ml', 
 90, 1, @catToner, (SELECT brand_id FROM brands WHERE brand_name=N'Klairs'), 2, '2025-06-01', '2027-06-01', 0),

(N'Toner Skin1004 rau má Madagascar làm dịu da', 0, GETDATE(), 312000,
 N'skin1004_centella_toner.png', N'Skin1004 Madagascar Centella Toning Toner 210ml', 
 100, 1, @catToner, (SELECT brand_id FROM brands WHERE brand_name=N'Skin1004'), 2, '2025-06-15', '2027-06-15', 0),

(N'Toner Some By Mi AHA-BHA-PHA 30 Days Miracle', 0, GETDATE(), 289000,
 N'somebymi_miracle_toner.png', N'Some By Mi AHA-BHA-PHA 30 Days Miracle Toner 150ml', 
 130, 1, @catToner, (SELECT brand_id FROM brands WHERE brand_name=N'Some By Mi'), 2, '2025-05-01', '2027-05-01', 0),

(N'Toner Pyunkang Yul Essence dưỡng ẩm sâu', 0, GETDATE(), 210000,
 N'pyunkangyul_essence_toner.png', N'Pyunkang Yul Essence Toner 200ml', 
 120, 1, @catToner, (SELECT brand_id FROM brands WHERE brand_name=N'Pyunkang Yul'), 2, '2025-04-20', '2027-04-20', 0),

(N'Toner Cocoon Sen Hậu Giang thuần chay', 0, GETDATE(), 173000,
 N'cocoon_sen_toner.png', N'Cocoon Sen Hậu Giang Soothing Toner 140ml', 
 150, 1, @catToner, (SELECT brand_id FROM brands WHERE brand_name=N'Cocoon'), 1, '2025-07-10', '2027-07-10', 0),

(N'Toner La Roche-Posay Effaclar chứa BHA & LHA', 5, GETDATE(), 360000,
 N'larocheposay_effaclar_toner.png', N'La Roche-Posay Effaclar Clarifying Toner 200ml', 
 80, 1, @catToner, (SELECT brand_id FROM brands WHERE brand_name=N'La Roche-Posay'), 2, '2025-06-01', '2027-06-01', 0),

(N'Toner Vichy Normaderm se khít lỗ chân lông', 5, GETDATE(), 380000,
 N'vichy_normaderm_toner.png', N'Vichy Normaderm Purifying Pore-Tightening Toner 200ml', 
 90, 1, @catToner, (SELECT brand_id FROM brands WHERE brand_name=N'Vichy'), 2, '2025-05-01', '2027-05-01', 0),

(N'Toner Hada Labo Gokujyun với 3 loại HA', 0, GETDATE(), 245000,
 N'hadalabo_gokujyun_toner.png', N'Hada Labo Gokujyun Hyaluronic Acid Lotion 170ml', 
 140, 1, @catToner, (SELECT brand_id FROM brands WHERE brand_name=N'Hada Labo'), 1, '2025-05-15', '2027-05-15', 0),

(N'Toner Bioderma Sensibio dịu nhẹ, không cồn', 0, GETDATE(), 320000,
 N'bioderma_sensibio_tonique.png', N'Bioderma Sensibio Tonique 250ml', 
 85, 1, @catToner, (SELECT brand_id FROM brands WHERE brand_name=N'Bioderma'), 2, '2025-04-10', '2027-04-10', 0);
GO

/* ===================== SỮA RỬA MẶT - SHOP 2 & ADMIN ===================== */
DECLARE @catSRM int = (SELECT category_id FROM categories WHERE category_name=N'Sữa rửa mặt');

INSERT INTO products(
  description, discount, entered_date, price, product_image, product_name,
  quantity, status, category_id, brand_id, shop_id, manufacture_date, expiry_date, favorite
)
VALUES
(N'Sữa rửa mặt CeraVe Foaming làm sạch sâu', 0, GETDATE(), 280000,
 N'cerave_foaming.png', N'CeraVe Foaming Facial Cleanser 236ml', 
 100, 1, @catSRM, (SELECT brand_id FROM brands WHERE brand_name=N'CeraVe'), 2, '2025-05-01', '2028-05-01', 0),

(N'Sữa rửa mặt Simple làm sạch dịu nhẹ', 0, GETDATE(), 91000,
 N'simple_refreshing_wash.png', N'Simple Kind To Skin Refreshing Facial Wash 150ml', 
 150, 1, @catSRM, (SELECT brand_id FROM brands WHERE brand_name=N'Simple'), NULL, '2025-06-01', '2027-06-01', 0),

(N'Sữa rửa mặt The Face Shop chiết xuất nước gạo', 0, GETDATE(), 30000,
 N'tfs_rice_water.png', N'The Face Shop Rice Water Bright Cleanser 150ml', 
 200, 1, @catSRM, (SELECT brand_id FROM brands WHERE brand_name=N'The Face Shop'), NULL, '2025-07-01', '2028-07-01', 0),

(N'Sữa rửa mặt Anua Heartleaf kiểm soát dầu', 0, GETDATE(), 220000,
 N'anua_heartleaf.png', N'Anua Heartleaf Pore Deep Cleansing Foam 150ml', 
 120, 1, @catSRM, (SELECT brand_id FROM brands WHERE brand_name=N'Anua'), 2, '2025-06-15', '2028-06-15', 0),

(N'Gel rửa mặt Cocoon bí đao thuần chay', 0, GETDATE(), 105000,
 N'cocoon_winter_melon.png', N'Cocoon Winter Melon Cleanser 140ml', 
 180, 1, @catSRM, (SELECT brand_id FROM brands WHERE brand_name=N'Cocoon'), 1, '2025-05-10', '2027-05-10', 0),

(N'Gel rửa mặt SVR Sebiaclear làm sạch dầu', 0, GETDATE(), 180000,
 N'svr_sebiaclear.png', N'SVR Sebiaclear Gel Moussant 200ml', 
 90, 1, @catSRM, (SELECT brand_id FROM brands WHERE brand_name=N'SVR'), 2, '2025-06-01', '2028-06-01', 0),

(N'Sữa rửa mặt Cetaphil Gentle Skin Cleanser', 0, GETDATE(), 150000,
 N'cetaphil_gentle.png', N'Cetaphil Gentle Skin Cleanser 236ml', 
 140, 1, @catSRM, (SELECT brand_id FROM brands WHERE brand_name=N'Cetaphil'), 2, '2025-07-01', '2028-07-01', 0),

(N'Sữa rửa mặt La Roche-Posay Toleriane dịu nhẹ', 0, GETDATE(), 320000,
 N'laroche_toleriane.png', N'La Roche-Posay Toleriane Hydrating Gentle Cleanser 200ml', 
 95, 1, @catSRM, (SELECT brand_id FROM brands WHERE brand_name=N'La Roche-Posay'), 2, '2025-06-01', '2028-06-01', 0),

(N'Gel rửa mặt Bioderma Sébium kiểm soát dầu', 0, GETDATE(), 210000,
 N'bioderma_sebium.png', N'Bioderma Sébium Gel Moussant 200ml', 
 100, 1, @catSRM, (SELECT brand_id FROM brands WHERE brand_name=N'Bioderma'), 2, '2025-05-15', '2028-05-15', 0),

(N'Sữa rửa mặt La Roche-Posay Effaclar dành cho da dầu', 0, GETDATE(), 350000,
 N'laroche_effaclar.png', N'La Roche-Posay Effaclar Purifying Foaming Gel 200ml', 
 80, 1, @catSRM, (SELECT brand_id FROM brands WHERE brand_name=N'La Roche-Posay'), 2, '2025-06-10', '2028-06-10', 0);
GO

/* ===================== MẶT NẠ - SHOP 1 & ADMIN ===================== */
DECLARE @catMask int = (SELECT category_id FROM categories WHERE category_name=N'Mặt nạ');

INSERT INTO products(
  description, discount, entered_date, price, product_image, product_name,
  quantity, status, category_id, brand_id, shop_id, manufacture_date, expiry_date, favorite
)
VALUES
(N'Mask đất sét Innisfree Super Volcanic hút dầu', 0, GETDATE(), 270000,
 N'innisfree_super_volcanic.png', N'Innisfree Super Volcanic Pore Clay Mask 100ml', 
 100, 1, @catMask, (SELECT brand_id FROM brands WHERE brand_name=N'Innisfree'), 1, '2025-05-01', '2028-05-01', 0),

(N'Mask đất sét SKIN1004 Centella Stick', 0, GETDATE(), 263000,
 N'skin1004_centella_clay_stick.png', N'SKIN1004 Mad Centella Poremizing Clay Stick Mask 55g', 
 80, 1, @catMask, (SELECT brand_id FROM brands WHERE brand_name=N'Skin1004'), 2, '2025-06-01', '2028-06-01', 0),

(N'Mask enzyme Image Skincare Vital C', 0, GETDATE(), 873499,
 N'image_vitalc_enzyme.png', N'Image Skincare Vital C Hydrating Enzyme Masque 57g', 
 50, 1, @catMask, (SELECT brand_id FROM brands WHERE brand_name=N'Image Skincare'), NULL, '2025-04-01', '2028-04-01', 0),

(N'Mask Colorkey Luminous B3 Brightening', 0, GETDATE(), 15000,
 N'colorkey_b3_mask.png', N'Colorkey Luminous B3 Brightening Facial Mask 30ml', 
 200, 1, @catMask, (SELECT brand_id FROM brands WHERE brand_name=N'Colorkey'), NULL, '2025-07-01', '2027-07-01', 0),

(N'Mask Rwine Placenta Face Mask', 0, GETDATE(), 11000,
 N'rwine_placenta_mask.png', N'Rwine Placenta Face Mask', 
 150, 1, @catMask, (SELECT brand_id FROM brands WHERE brand_name=N'Rwine'), NULL, '2025-08-01', '2027-08-01', 0),

(N'Mặt nạ ngủ Laneige Water Sleeping Mask', 0, GETDATE(), 227000,
 N'laneige_water_sleeping_ex.png', N'Laneige Water Sleeping Mask Ex 70ml', 
 120, 1, @catMask, (SELECT brand_id FROM brands WHERE brand_name=N'Laneige'), 1, '2025-06-10', '2028-06-10', 0),

(N'Mặt nạ giấy Nature Republic chiết xuất cam', 0, GETDATE(), 18000,
 N'nature_orange_sheet.png', N'Nature Republic Real Nature Orange Sheet Mask 23ml', 
 250, 1, @catMask, (SELECT brand_id FROM brands WHERE brand_name=N'Nature Republic'), NULL, '2025-07-15', '2028-07-15', 0),

(N'Mask buổi sáng Saborino', 0, GETDATE(), 364163,
 N'saborino_morning_mask.png', N'Saborino Morning Facial Mask', 
 90, 1, @catMask, (SELECT brand_id FROM brands WHERE brand_name=N'Saborino'), NULL, '2025-05-20', '2028-05-20', 0),

(N'Mặt nạ Caryophy Portulaca giảm mụn', 0, GETDATE(), 22950,
 N'caryophy_portulaca_mask.png', N'Caryophy Portulaca Mask Sheet 3-in-1', 
 180, 1, @catMask, (SELECT brand_id FROM brands WHERE brand_name=N'Caryophy'), 1, '2025-06-25', '2028-06-25', 0),

(N'Mặt nạ Klairs Midnight Blue làm dịu da', 0, GETDATE(), 44000,
 N'klairs_midnight_blue.png', N'Klairs Midnight Blue Calming Sheet Mask', 
 140, 1, @catMask, (SELECT brand_id FROM brands WHERE brand_name=N'Klairs'), 2, '2025-06-01', '2028-06-01', 0);
GO

/* ===================== NƯỚC HOA - ADMIN (Platform bán chính hãng) ===================== */
DECLARE @catNH int = (SELECT category_id FROM categories WHERE category_name=N'Nước hoa');

INSERT INTO products(
  description, discount, entered_date, price, product_image, product_name,
  quantity, status, category_id, brand_id, shop_id, manufacture_date, expiry_date, favorite
)
VALUES
(N'Nước hoa Chanel No.5 huyền thoại', 0, GETDATE(), 2500000,
 N'chanel_no5_edp.png', N'Chanel No.5 Eau de Parfum 50ml', 
 50, 1, @catNH, (SELECT brand_id FROM brands WHERE brand_name=N'Chanel'), NULL, '2025-06-01', '2028-06-01', 0),

(N'Nước hoa nam Dior Sauvage cay nồng', 0, GETDATE(), 2500000,
 N'dior_sauvage_edt.png', N'Dior Sauvage Eau de Toilette 60ml', 
 80, 1, @catNH, (SELECT brand_id FROM brands WHERE brand_name=N'Dior'), NULL, '2025-06-01', '2028-06-01', 0),

(N'Nước hoa nam Versace Eros quyến rũ', 0, GETDATE(), 1200000,
 N'versace_eros_edt.png', N'Versace Eros Eau de Toilette 50ml', 
 70, 1, @catNH, (SELECT brand_id FROM brands WHERE brand_name=N'Versace'), NULL, '2025-06-01', '2028-06-01', 0),

(N'Nước hoa unisex Calvin Klein CK One', 0, GETDATE(), 800000,
 N'ck_one_edt.png', N'Calvin Klein CK One Eau de Toilette 100ml', 
 120, 1, @catNH, (SELECT brand_id FROM brands WHERE brand_name=N'Calvin Klein'), NULL, '2025-06-01', '2028-06-01', 0),

(N'Nước hoa nam Hugo Boss Bottled gỗ ấm', 0, GETDATE(), 1300000,
 N'hugo_boss_bottled_edt.png', N'Hugo Boss Bottled Eau de Toilette 75ml', 
 90, 1, @catNH, (SELECT brand_id FROM brands WHERE brand_name=N'Hugo Boss'), NULL, '2025-06-01', '2028-06-01', 0),

(N'Nước hoa nam Lacoste L.12.12 Blanc thể thao', 0, GETDATE(), 1000000,
 N'lacoste_l1212_blanc_edt.png', N'Lacoste L.12.12 Blanc Eau de Toilette 75ml', 
 100, 1, @catNH, (SELECT brand_id FROM brands WHERE brand_name=N'Lacoste'), NULL, '2025-06-01', '2028-06-01', 0),

(N'Nước hoa nam Montblanc Legend gỗ sang trọng', 0, GETDATE(), 1200000,
 N'montblanc_legend_edt.png', N'Montblanc Legend Eau de Toilette 50ml', 
 85, 1, @catNH, (SELECT brand_id FROM brands WHERE brand_name=N'Montblanc'), NULL, '2025-06-01', '2028-06-01', 0),

(N'Nước hoa nam Burberry Brit cổ điển', 0, GETDATE(), 1100000,
 N'burberry_brit_edt.png', N'Burberry Brit Eau de Toilette 50ml', 
 75, 1, @catNH, (SELECT brand_id FROM brands WHERE brand_name=N'Burberry'), NULL, '2025-06-01', '2028-06-01', 0),

(N'Nước hoa nam Tommy Hilfiger Tommy trẻ trung', 0, GETDATE(), 1300000,
 N'tommy_hilfiger_tommy_edt.png', N'Tommy Hilfiger Tommy Eau de Toilette 100ml', 
 110, 1, @catNH, (SELECT brand_id FROM brands WHERE brand_name=N'Tommy Hilfiger'), NULL, '2025-06-01', '2028-06-01', 0),

(N'Nước hoa nữ Chanel Coco Mademoiselle quyến rũ', 0, GETDATE(), 4400000,
 N'chanel_coco_mademoiselle_edp.png', N'Chanel Coco Mademoiselle Eau de Parfum 50ml', 
 60, 1, @catNH, (SELECT brand_id FROM brands WHERE brand_name=N'Chanel'), NULL, '2025-06-01', '2028-06-01', 0);
GO

/* =====================================================================
   INSERT SAMPLE ORDERS FOR TESTING
   ===================================================================== */
INSERT INTO orders(
    user_id, customer_name, customer_email, customer_phone, shipping_address, note,
    status, payment_method, total_amount, final_amount, shipping_fee, discount_amount,
    order_date, estimated_delivery_date, shop_id, payment_status
)
VALUES
-- Order 1: User Chi đặt hàng từ Shop 1 (An Nguyễn)
(1, N'Trần Thảo Chi', 'chi@gmail.com', '0901234567', 
 N'123 Nguyễn Huệ, Quận 1, TP.HCM', N'Giao hàng vào buổi chiều',
 'PENDING', 'COD', 150000, 150000, 0, 0,
 '2025-10-10 10:30:00', '2025-10-14 10:30:00', 1, 0),

-- Order 2: User Đồng đặt hàng từ Shop 2 (Bình)
(2, N'Trần Hữu Đồng', 'dong@gmail.com', '0912345678',
 N'456 Lê Lợi, Quận 1, TP.HCM', N'Cần giao nhanh',
 'CONFIRMED', 'MOMO', 320000, 320000, 0, 0,
 '2025-10-09 14:20:00', '2025-10-13 14:20:00', 2, 1),

-- Order 3: User Demo đặt hàng từ Admin/Platform
(3, N'User Demo', 'user@gmail.com', '0923456789',
 N'789 Trần Hưng Đạo, Quận 1, TP.HCM', N'Giao hàng cuối tuần',
 'SHIPPING', 'BANK_TRANSFER', 2500000, 2500000, 0, 0,
 '2025-10-08 09:15:00', '2025-10-12 09:15:00', NULL, 1);

-- Insert order details
INSERT INTO order_details(order_id, product_id, product_name, unit_price, quantity, total_price)
VALUES
-- Order 1 details (Ailus lipstick)
(1, (SELECT TOP 1 product_id FROM products WHERE product_name LIKE '%Ailus%' AND product_name LIKE '%Cam%'), 
 N'Ailus Stress Free Lipstick M4V #03 Cam Cháy', 150000, 1, 150000),

-- Order 2 details (Sebamed cream)
(2, (SELECT TOP 1 product_id FROM products WHERE product_name LIKE '%Sebamed%'), 
 N'Sebamed Relief Face Cream 5% Urea', 320000, 1, 320000),

-- Order 3 details (Chanel perfume)
(3, (SELECT TOP 1 product_id FROM products WHERE product_name LIKE '%Chanel%' AND product_name LIKE '%No.5%'), 
 N'Chanel No.5 Eau de Parfum 50ml', 2500000, 1, 2500000);
GO

/* =====================================================================
   CẬP NHẬT THỐNG KÊ CHO SHOPS
   (Đếm số sản phẩm thực tế đã insert)
   ===================================================================== */
UPDATE shops SET total_products = (
    SELECT COUNT(*) FROM products WHERE shop_id = shops.shop_id
);
GO

/* =====================================================================
   TẠO BẢNG CART_ITEMS CHO GIỎ HÀNG
   ===================================================================== */
CREATE TABLE cart_items (
    cart_item_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    shop_id BIGINT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    selected BIT NOT NULL CONSTRAINT DF_cart_items_selected DEFAULT 1,
    created_date DATETIME2 DEFAULT GETDATE(),
    updated_date DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES [user](user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (shop_id) REFERENCES shops(shop_id) ON DELETE SET NULL,
    UNIQUE(user_id, product_id) -- Prevent duplicate products for same user
);

-- Create indexes for better performance
CREATE INDEX IX_cart_items_user_id ON cart_items(user_id);
CREATE INDEX IX_cart_items_product_id ON cart_items(product_id);
CREATE INDEX IX_cart_items_shop_id ON cart_items(shop_id);

-- Add comments for documentation
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Table to store individual cart items for each user', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'cart_items';
GO

-- Keep cart_items.shop_id synced with products.shop_id
IF OBJECT_ID('dbo.TR_cart_items_set_shop', 'TR') IS NOT NULL DROP TRIGGER dbo.TR_cart_items_set_shop;
GO
CREATE TRIGGER TR_cart_items_set_shop
ON dbo.cart_items
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE ci
    SET ci.shop_id = p.shop_id,
        ci.updated_date = GETDATE()
    FROM dbo.cart_items ci
    JOIN inserted i ON ci.cart_item_id = i.cart_item_id
    JOIN dbo.products p ON p.product_id = ci.product_id;
END;
GO

-- Update existing cart items to be selected by default
UPDATE cart_items SET selected = 1 WHERE selected IS NULL;
GO

-- Add comment for documentation
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Indicates whether this cart item is selected for checkout (1 = selected, 0 = not selected)', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'cart_items',
    @level2type = N'COLUMN', @level2name = N'selected';
GO

-- ===============================
-- TABLE: promotions
-- ===============================
USE WebMyPham;
GO

-- Drop table if exists
IF OBJECT_ID('dbo.promotions', 'U') IS NOT NULL 
    DROP TABLE dbo.promotions;
GO

-- Create promotions table
CREATE TABLE dbo.promotions (
    promotion_id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    promotion_name         NVARCHAR(200) NOT NULL,
    description            NVARCHAR(1000),
    promotion_code         NVARCHAR(50) NOT NULL UNIQUE,
    promotion_type         NVARCHAR(20) NOT NULL CHECK (promotion_type IN ('PRODUCT_PERCENTAGE', 'SHIPPING_DISCOUNT', 'FIXED_AMOUNT')),
    discount_value         DECIMAL(10,2) NOT NULL CHECK (discount_value >= 0),
    minimum_order_amount   DECIMAL(10,2) NOT NULL CHECK (minimum_order_amount >= 0),
    maximum_discount_amount DECIMAL(10,2) NOT NULL CHECK (maximum_discount_amount >= 0),
    usage_limit            INT NOT NULL CHECK (usage_limit > 0),
    used_count             INT NOT NULL DEFAULT 0 CHECK (used_count >= 0),
    start_date             DATETIME2 NOT NULL,
    end_date               DATETIME2 NOT NULL,
    is_active              BIT NOT NULL DEFAULT 1,
    created_at             DATETIME2 NOT NULL DEFAULT (GETDATE()),
    updated_at             DATETIME2 NOT NULL DEFAULT (GETDATE()),
    
    -- Constraints
    CONSTRAINT CHK_promotions_dates CHECK (end_date > start_date),
    CONSTRAINT CHK_promotions_usage CHECK (used_count <= usage_limit),
    CONSTRAINT CHK_promotions_discount CHECK (discount_value <= maximum_discount_amount)
);
GO

-- Create indexes for better performance
CREATE INDEX IX_promotions_code ON dbo.promotions(promotion_code);
CREATE INDEX IX_promotions_type ON dbo.promotions(promotion_type);
CREATE INDEX IX_promotions_active ON dbo.promotions(is_active);
CREATE INDEX IX_promotions_dates ON dbo.promotions(start_date, end_date);
CREATE INDEX IX_promotions_usage ON dbo.promotions(used_count, usage_limit);
GO

-- Insert sample promotion data
INSERT INTO dbo.promotions (
    promotion_name, 
    description, 
    promotion_code, 
    promotion_type, 
    discount_value, 
    minimum_order_amount, 
    maximum_discount_amount, 
    usage_limit, 
    used_count, 
    start_date, 
    end_date, 
    is_active
) VALUES 
-- Product percentage discount
(
    N'Giảm giá 20% cho đơn hàng từ 500k',
    N'Áp dụng cho tất cả sản phẩm, giảm 20% cho đơn hàng từ 500,000đ trở lên',
    'SAVE20',
    'PRODUCT_PERCENTAGE',
    20.00,
    500000.00,
    200000.00,
    1000,
    0,
    '2024-01-01 00:00:00',
    '2024-12-31 23:59:59',
    1
),
-- Shipping discount
(
    N'Miễn phí ship cho đơn hàng từ 300k',
    N'Miễn phí vận chuyển cho đơn hàng từ 300,000đ trở lên',
    'FREESHIP',
    'SHIPPING_DISCOUNT',
    50000.00,
    300000.00,
    50000.00,
    500,
    0,
    '2024-01-01 00:00:00',
    '2024-12-31 23:59:59',
    1
),
-- Fixed amount discount
(
    N'Giảm 100k cho đơn hàng từ 1 triệu',
    N'Giảm ngay 100,000đ cho đơn hàng từ 1,000,000đ trở lên',
    'SAVE100K',
    'FIXED_AMOUNT',
    100000.00,
    1000000.00,
    100000.00,
    200,
    0,
    '2024-01-01 00:00:00',
    '2024-12-31 23:59:59',
    1
),
-- Expired promotion
(
    N'Khuyến mãi Tết 2024',
    N'Giảm giá đặc biệt nhân dịp Tết Nguyên Đán 2024',
    'TET2024',
    'PRODUCT_PERCENTAGE',
    15.00,
    200000.00,
    150000.00,
    100,
    95,
    '2024-01-01 00:00:00',
    '2024-02-15 23:59:59',
    0
),
-- Expiring soon promotion
(
    N'Khuyến mãi Black Friday',
    N'Giảm giá lớn nhân dịp Black Friday',
    'BLACKFRIDAY',
    'PRODUCT_PERCENTAGE',
    30.00,
    1000000.00,
    500000.00,
    50,
    5,
    '2024-11-01 00:00:00',
    '2024-12-31 23:59:59',
    1
);
GO

-- Create trigger to update updated_at timestamp
CREATE TRIGGER TR_promotions_update_timestamp
ON dbo.promotions
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.promotions 
    SET updated_at = GETDATE()
    WHERE promotion_id IN (SELECT promotion_id FROM inserted);
END;
GO

-- Create view for active promotions
CREATE VIEW vw_active_promotions AS
SELECT 
    promotion_id,
    promotion_name,
    description,
    promotion_code,
    promotion_type,
    discount_value,
    minimum_order_amount,
    maximum_discount_amount,
    usage_limit,
    used_count,
    start_date,
    end_date,
    created_at,
    updated_at
FROM dbo.promotions
WHERE is_active = 1 
    AND start_date <= GETDATE() 
    AND end_date >= GETDATE()
    AND used_count < usage_limit;
GO

-- Create function to check if promotion is valid
CREATE FUNCTION fn_is_promotion_valid(@promotion_code NVARCHAR(50))
RETURNS BIT
AS
BEGIN
    DECLARE @is_valid BIT = 0;
    
    IF EXISTS (
        SELECT 1 FROM dbo.promotions 
        WHERE promotion_code = @promotion_code 
            AND is_active = 1 
            AND start_date <= GETDATE() 
            AND end_date >= GETDATE()
            AND used_count < usage_limit
    )
    BEGIN
        SET @is_valid = 1;
    END
    
    RETURN @is_valid;
END;
GO

-- Create stored procedure to apply promotion
CREATE PROCEDURE sp_apply_promotion
    @promotion_code NVARCHAR(50),
    @order_amount DECIMAL(10,2),
    @discount_amount DECIMAL(10,2) OUTPUT,
    @is_valid BIT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @promotion_id BIGINT;
    DECLARE @promotion_type NVARCHAR(20);
    DECLARE @discount_value DECIMAL(10,2);
    DECLARE @minimum_order_amount DECIMAL(10,2);
    DECLARE @maximum_discount_amount DECIMAL(10,2);
    DECLARE @used_count INT;
    DECLARE @usage_limit INT;
    
    -- Get promotion details
    SELECT 
        @promotion_id = promotion_id,
        @promotion_type = promotion_type,
        @discount_value = discount_value,
        @minimum_order_amount = minimum_order_amount,
        @maximum_discount_amount = maximum_discount_amount,
        @used_count = used_count,
        @usage_limit = usage_limit
    FROM dbo.promotions
    WHERE promotion_code = @promotion_code 
        AND is_active = 1 
        AND start_date <= GETDATE() 
        AND end_date >= GETDATE();
    
    -- Check if promotion exists and is valid
    IF @promotion_id IS NULL OR @used_count >= @usage_limit OR @order_amount < @minimum_order_amount
    BEGIN
        SET @is_valid = 0;
        SET @discount_amount = 0;
        RETURN;
    END
    
    -- Calculate discount amount based on promotion type
    IF @promotion_type = 'PRODUCT_PERCENTAGE'
    BEGIN
        SET @discount_amount = @order_amount * (@discount_value / 100);
        IF @discount_amount > @maximum_discount_amount
            SET @discount_amount = @maximum_discount_amount;
    END
    ELSE IF @promotion_type = 'FIXED_AMOUNT'
    BEGIN
        SET @discount_amount = @discount_value;
        IF @discount_amount > @maximum_discount_amount
            SET @discount_amount = @maximum_discount_amount;
    END
    ELSE IF @promotion_type = 'SHIPPING_DISCOUNT'
    BEGIN
        SET @discount_amount = @discount_value;
    END
    
    -- Update used count
    UPDATE dbo.promotions 
    SET used_count = used_count + 1
    WHERE promotion_id = @promotion_id;
    
    SET @is_valid = 1;
END;
GO