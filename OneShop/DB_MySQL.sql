-- ================================================
-- MySQL Database Script for OneShop E-commerce Platform
-- Converted from SQL Server to MySQL
-- ================================================

-- ================================================
-- MySQL Database Setup & Configuration
-- ================================================

-- Disable safe update mode and configure MySQL for script execution
SET SQL_SAFE_UPDATES = 0;
SET FOREIGN_KEY_CHECKS = 0;
SET SESSION sql_mode = 'TRADITIONAL,NO_AUTO_VALUE_ON_ZERO,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';

-- Create database with UTF8 charset
DROP DATABASE IF EXISTS WebMyPham;
CREATE DATABASE WebMyPham 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE WebMyPham;

-- ===============================
-- TABLE: categories
-- ===============================
CREATE TABLE categories (
    category_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_image  VARCHAR(255),
    category_name   VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO categories(category_image, category_name) VALUES
(NULL, 'Son môi'),
(NULL, 'Kem dưỡng da'),
(NULL, 'Nước hoa'),
(NULL, 'Sữa rửa mặt'),
(NULL, 'Toner'),
(NULL, 'Mặt nạ'),
(NULL, 'Kem chống nắng'),
(NULL, 'Phấn phủ'),
(NULL, 'Tẩy tế bào chết'),
(NULL, 'Serum dưỡng da');

-- ===============================
-- TABLE: user
-- ===============================
CREATE TABLE user (
    user_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    avatar         VARCHAR(255),
    email          VARCHAR(255) UNIQUE NOT NULL,
    name           VARCHAR(255) NOT NULL,
    password       VARCHAR(255) NOT NULL,
    register_date  DATE DEFAULT (CURDATE()),
    status         BOOLEAN DEFAULT TRUE,
    one_xu_balance DECIMAL(18,2) NOT NULL DEFAULT 0,
    -- OAuth2 fields for social login
    provider       VARCHAR(50),     -- facebook, google, etc.
    provider_id    VARCHAR(100),    -- ID from OAuth2 provider
    enabled        BOOLEAN DEFAULT TRUE     -- account enabled status
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert 9 users (including shipper and CSKH)
INSERT INTO user(avatar, email, name, password, register_date, status)
VALUES 
('user.png','chi@gmail.com','Trần Thảo Chi','123456','2025-09-04',TRUE),
('user.png','dong@gmail.com','Trần Hữu Đồng','123456','2025-09-04',TRUE),
('user.png','user@gmail.com','User Demo','123456','2025-09-04',TRUE),
('user.png','admin@mypham.com','Admin Mỹ Phẩm','123456','2025-09-04',TRUE),
('user.png','vendor@mypham.com','Nguyễn Văn An','123456','2025-10-07',TRUE),
('user.png','vendor1@mypham.com','Trần Thị Bình','123456','2025-10-07',TRUE),
('user.png','vendor2@mypham.com','Lê Quốc Cường','123456','2025-10-07',TRUE),
('user.png','shipper@mypham.com','Phạm Văn Giao','123456','2025-10-08',TRUE),
('user.png','cskh@mypham.com','Nguyễn Thị Linh - CSKH','123456','2025-10-10',TRUE);

-- ===============================
-- TABLE: role
-- ===============================
CREATE TABLE role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO role(name)
VALUES 
('ROLE_USER'),
('ROLE_ADMIN'),
('ROLE_VENDOR'),
('ROLE_SHIPPER'),
('ROLE_CSKH');

-- ===============================
-- TABLE: users_roles
-- ===============================
CREATE TABLE users_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY(user_id, role_id),
    CONSTRAINT FK_users_roles_user FOREIGN KEY(user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_users_roles_role FOREIGN KEY(role_id) REFERENCES role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Assign roles to users
INSERT INTO users_roles(user_id, role_id)
VALUES 
(1,1),  -- chi: regular user
(2,1),  -- dong: regular user  
(3,1),  -- user: regular user
(4,2),  -- admin: ADMIN
(5,1),(5,3),  -- vendor An: USER + VENDOR
(6,1),(6,3),  -- vendor Bình: USER + VENDOR
(7,1),(7,3),  -- vendor Cường: USER + VENDOR
(8,4),  -- shipper
(9,5);  -- cskh: CSKH

-- ===============================
-- TABLE: brands
-- ===============================
CREATE TABLE brands (
    brand_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand_name VARCHAR(255) NOT NULL,
    brand_image VARCHAR(255),
    description VARCHAR(1000),
    origin VARCHAR(255),
    status BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===============================
-- TABLE: shops (NEW - ADD BEFORE PRODUCTS)
-- ===============================
CREATE TABLE shops (
    shop_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Basic information
    shop_name VARCHAR(255) NOT NULL UNIQUE,
    shop_slug VARCHAR(100) UNIQUE,
    shop_description VARCHAR(2000),
    shop_logo VARCHAR(255),
    shop_banner VARCHAR(255),
    
    -- Vendor connection (1 vendor can have multiple shops)
    vendor_id BIGINT NOT NULL,
    
    -- Contact information
    phone_number VARCHAR(15),
    address VARCHAR(500),
    city VARCHAR(100),
    district VARCHAR(100),
    ward VARCHAR(100),
    
    -- Status: PENDING, ACTIVE, SUSPENDED, REJECTED
    status ENUM('PENDING', 'ACTIVE', 'SUSPENDED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at DATETIME DEFAULT NOW(),
    approved_at DATETIME,
    rejection_reason VARCHAR(500),
    
    -- Statistics
    total_products INT DEFAULT 0,
    total_orders INT DEFAULT 0,
    total_revenue DECIMAL(18,2) DEFAULT 0,
    
    -- Settings
    allow_cod BOOLEAN DEFAULT TRUE,
    preparation_days INT DEFAULT 2,
    
    CONSTRAINT FK_shops_vendor FOREIGN KEY(vendor_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert 3 sample shops
INSERT INTO shops(
    shop_name, shop_slug, shop_description, shop_logo, shop_banner,
    vendor_id, phone_number, address, city, district, ward,
    status, created_at, approved_at,
    total_products, total_orders, total_revenue,
    allow_cod, preparation_days
)
VALUES
-- Shop 1: Vendor An (user_id=5) - ACTIVE
('Mỹ Phẩm An Nguyễn', 'my-pham-an-nguyen',
 'Chuyên cung cấp mỹ phẩm chính hãng từ Hàn Quốc, Nhật Bản. Cam kết 100% hàng chính hãng.',
 'shop_an_logo.png', 'shop_an_banner.jpg',
 5, '0901234567', '123 Nguyễn Huệ', 'TP. Hồ Chí Minh', 'Quận 1', 'Phường Bến Nghé',
 'ACTIVE', '2025-09-10', '2025-09-12',
 0, 0, 0, TRUE, 2),

-- Shop 2: Vendor Bình (user_id=6) - ACTIVE
('Cosmetic House Bình', 'cosmetic-house-binh',
 'Chuyên dược mỹ phẩm cao cấp: La Roche-Posay, Vichy, Eucerin. Uy tín - Chất lượng.',
 'shop_binh_logo.png', 'shop_binh_banner.jpg',
 6, '0912345678', '456 Lê Lợi', 'TP. Hồ Chí Minh', 'Quận 1', 'Phường Phạm Ngũ Lão',
 'ACTIVE', '2025-09-15', '2025-09-17',
 0, 0, 0, TRUE, 1),

-- Shop 3: Vendor Cường (user_id=7) - PENDING (waiting for approval)
('Shop Mỹ Phẩm Cường', 'shop-my-pham-cuong',
 'Mỹ phẩm thiên nhiên Việt Nam: Cocoon, Lemonade. Thuần chay - An toàn.',
 'shop_cuong_logo.png', 'shop_cuong_banner.jpg',
 7, '0923456789', '789 Trần Hưng Đạo', 'TP. Hồ Chí Minh', 'Quận 1', 'Phường Cầu Kho',
 'PENDING', '2025-10-05', NULL,
 0, 0, 0, TRUE, 3);

-- ===============================
-- TABLE: products (ADDED shop_id)
-- ===============================
CREATE TABLE products (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(1000),
    discount INT CHECK (discount >= 0 AND discount <= 90),
    entered_date DATETIME DEFAULT NOW(),
    price DECIMAL(18,2) NOT NULL CHECK (price > 0),
    product_image VARCHAR(255),
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL CHECK (quantity >= 0),
    status BOOLEAN DEFAULT TRUE,
    category_id BIGINT,
    brand_id BIGINT,
    shop_id BIGINT NULL, -- NULL = admin/platform products
    manufacture_date DATE,
    expiry_date DATE,
    favorite BOOLEAN DEFAULT FALSE,
    CONSTRAINT FK_products_categories FOREIGN KEY(category_id) REFERENCES categories(category_id) ON DELETE SET NULL,
    CONSTRAINT FK_products_brands FOREIGN KEY(brand_id) REFERENCES brands(brand_id) ON DELETE SET NULL,
    CONSTRAINT FK_products_shops FOREIGN KEY(shop_id) REFERENCES shops(shop_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_products_shop ON products(shop_id);

-- ===============================
-- TABLE: favorites
-- ===============================
CREATE TABLE favorites (
    favorite_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    CONSTRAINT FK_favorites_user FOREIGN KEY(user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_favorites_product FOREIGN KEY(product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    CONSTRAINT UK_favorites_user_product UNIQUE(user_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IX_favorites_user ON favorites(user_id);
CREATE INDEX IX_favorites_product ON favorites(product_id);

-- ===============================
-- TABLE: orders
-- ===============================
CREATE TABLE orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    shipping_address VARCHAR(500) NOT NULL,
    pickup_address VARCHAR(500) NULL,  -- Pickup address (from shop/vendor)
    package_type VARCHAR(100) NULL,   -- Package type: Small items, Fragile, Food, etc.
    weight FLOAT NULL,                  -- Weight (kg)
    note VARCHAR(1000),
    status ENUM('PENDING', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'CANCELLED', 'RETURNED') NOT NULL DEFAULT 'PENDING',
    payment_method ENUM('COD', 'MOMO', 'BANK_TRANSFER', 'VNPAY') NOT NULL DEFAULT 'COD',
    total_amount DECIMAL(18,2) NOT NULL CHECK (total_amount > 0),
    order_date DATETIME NOT NULL DEFAULT NOW(),
    shipped_date DATETIME,
    delivered_date DATETIME,
    cancelled_date DATETIME,
    cancellation_reason TEXT,
    tracking_number VARCHAR(100),
    shipping_fee DECIMAL(10,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    final_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    payment_status BOOLEAN NOT NULL DEFAULT FALSE,
    payment_date DATETIME,
    estimated_delivery_date DATETIME,
    shipper_id BIGINT NULL,
    shop_id BIGINT NULL,
    momo_transaction_id VARCHAR(255) NULL,
    momo_request_id VARCHAR(255) NULL,
    CONSTRAINT FK_orders_user FOREIGN KEY(user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_orders_shipper FOREIGN KEY(shipper_id) REFERENCES user(user_id) ON DELETE SET NULL,
    CONSTRAINT FK_orders_shop FOREIGN KEY(shop_id) REFERENCES shops(shop_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for better performance
CREATE INDEX IX_orders_status ON orders(status);
CREATE INDEX IX_orders_user_id ON orders(user_id);
CREATE INDEX IX_orders_shop_id ON orders(shop_id);
CREATE INDEX IX_orders_order_date ON orders(order_date);
CREATE INDEX IX_orders_payment_status ON orders(payment_status);
CREATE INDEX IX_orders_payment_method ON orders(payment_method);

-- ===============================
-- TABLE: order_details
-- ===============================
CREATE TABLE order_details (
    order_detail_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    unit_price DECIMAL(18,2) NOT NULL CHECK (unit_price > 0),
    quantity INT NOT NULL CHECK (quantity > 0),
    total_price DECIMAL(18,2) NOT NULL CHECK (total_price > 0),
    CONSTRAINT FK_orderdetails_orders FOREIGN KEY(order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT FK_orderdetails_products FOREIGN KEY(product_id) REFERENCES products(product_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===============================
-- TABLE: comments
-- ===============================
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(255),
    rate_date DATETIME DEFAULT NOW(),
    rating DECIMAL(2,1) CHECK (rating BETWEEN 1 AND 5),
    order_detail_id BIGINT,
    product_id BIGINT,
    user_id BIGINT,
    CONSTRAINT FK_comments_orderdetail FOREIGN KEY(order_detail_id) REFERENCES order_details(order_detail_id) ON DELETE CASCADE,
    CONSTRAINT FK_comments_product FOREIGN KEY(product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    CONSTRAINT FK_comments_user FOREIGN KEY(user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===============================
-- TABLE: chat_message
-- ===============================
CREATE TABLE chat_message (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  room_id VARCHAR(100) NOT NULL,
  sender VARCHAR(255) NOT NULL,
  sender_type VARCHAR(50) NOT NULL,
  message_type VARCHAR(20) NOT NULL,
  content TEXT NOT NULL,
  sent_at BIGINT NOT NULL,
  customer_name VARCHAR(255) NULL,
  vendor_name VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_chat_message_room_time ON chat_message(room_id, sent_at DESC);

-- ===============================
-- TABLE: shipping_providers
-- ===============================
CREATE TABLE shipping_providers (
    provider_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_name VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(15),
    contact_email VARCHAR(255),
    description VARCHAR(1000),
    website VARCHAR(255),
    address VARCHAR(255),
    shipping_fees DECIMAL(18,2),
    delivery_time_range VARCHAR(255),
    logo VARCHAR(255),
    status BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO shipping_providers (provider_name, contact_phone, contact_email, description, website, address, shipping_fees, delivery_time_range, logo, status)
VALUES
('Giao Hàng Nhanh', '1900636677', 'support@ghn.vn', 'Dịch vụ giao hàng nhanh trên toàn quốc', 'https://ghn.vn', 'Hà Nội, Việt Nam', 30000, '1-3 ngày', 'ghn-logo.png', TRUE),
('Giao Hàng Tiết Kiệm', '1900636677', 'support@giaohangtietkiem.vn', 'Dịch vụ giao hàng tiết kiệm toàn quốc', 'https://giaohangtietkiem.vn', 'Hồ Chí Minh, Việt Nam', 25000, '2-4 ngày', 'ghtk-logo.png', TRUE),
('J&T Express', '19001088', 'cskh@jtexpress.vn', 'Dịch vụ chuyển phát nhanh J&T Express', 'https://jtexpress.vn', 'Hồ Chí Minh, Việt Nam', 28000, '1-3 ngày', 'jt-express-logo.png', TRUE),
('Viettel Post', '1900818820', 'cskh@viettelpost.com.vn', 'Dịch vụ chuyển phát Viettel Post', 'https://viettelpost.com.vn', 'Hà Nội, Việt Nam', 27000, '2-4 ngày', 'viettel-post-logo.png', TRUE),
('Vietnam Post', '18006422', 'info@vnpost.vn', 'Bưu điện Việt Nam - Vietnam Post', 'https://www.vnpost.vn', 'Hà Nội, Việt Nam', 26000, '2-5 ngày', 'vnpost-logo.png', TRUE);

-- ===================== INSERT BRANDS =====================
INSERT INTO brands(brand_name, brand_image, description, origin, status)
VALUES
('La Roche-Posay', 'laroche.png', 'Dược mỹ phẩm cho da nhạy cảm', 'Pháp', TRUE),
('Olay', 'olay.png', 'Dưỡng ẩm và chống lão hoá', 'Mỹ', TRUE),
('Yves Saint Laurent (YSL)', 'ysl.png', 'Thương hiệu cao cấp Pháp', 'Pháp', TRUE),
('ZO Skin Health', 'zo.png', 'Chăm sóc da chuyên sâu', 'Mỹ', TRUE),
('3CE (3 Concept Eyes)', '3ce.png', 'Makeup Hàn Quốc', 'Hàn Quốc', TRUE),
('Avène', 'avene.png', 'Dược mỹ phẩm suối khoáng', 'Pháp', TRUE),
('Chanel', 'chanel.png', 'Thương hiệu cao cấp Pháp', 'Pháp', TRUE),
('Eucerin', 'eucerin.png', 'Dược mỹ phẩm Đức', 'Đức', TRUE),
('Sebamed', 'sebamed.png', 'pH 5.5 dịu nhẹ', 'Đức', TRUE),
('Aloins', 'aloins.png', 'Kem dưỡng nha đam Nhật', 'Nhật Bản', TRUE),
('CeraVe', 'cerave.png', 'Ceramide & HA', 'Mỹ', TRUE),
('Ailus', 'ailus.png', 'Son bình dân Nhật', 'Nhật Bản', TRUE),
('The Ordinary', 'the_ordinary.jpg', 'Mỹ phẩm tối giản', 'Canada', TRUE),
('Vichy', 'vichy.jpg', 'Khoáng núi lửa', 'Pháp', TRUE),
('Skin1004', 'skin1004.jpg', 'Rau má Madagascar', 'Hàn Quốc', TRUE),
('Some By Mi', 'some_by_mi.jpg', 'AHA-BHA-PHA', 'Hàn Quốc', TRUE),
('Melano CC', 'melano_cc.jpg', 'Vitamin C Nhật', 'Nhật Bản', TRUE),
('Klairs', 'klairs.jpg', 'Vitamin C dịu nhẹ', 'Hàn Quốc', TRUE),
('Paula''s Choice', 'paulas_choice.jpg', 'Booster chuyên trị', 'Mỹ', TRUE),
('Hada Labo', 'hada_labo.jpg', 'Dưỡng ẩm HA', 'Nhật Bản', TRUE),
('Cocoon', 'cocoon.jpg', 'Vegan Việt Nam', 'Việt Nam', TRUE),
('Cetaphil', 'cetaphil.jpg', 'Dược mỹ phẩm Canada', 'Canada', TRUE),
('Fixderma', 'fixderma.jpg', 'Dược mỹ phẩm Ấn Độ', 'Ấn Độ', TRUE),
('L''Oréal Paris', 'loreal_paris.jpg', 'Thương hiệu Pháp', 'Pháp', TRUE),
('Sắc Ngọc Khang', 'sacngockhang.jpg', 'Thuộc Hoa Linh', 'Việt Nam', TRUE),
('Reihaku Hatomugi', 'reihaku_hatomugi.jpg', 'Hatomugi dưỡng ẩm', 'Nhật Bản', TRUE),
('Innisfree', 'innisfree.jpg', 'Thiên nhiên Jeju', 'Hàn Quốc', TRUE),
('Catrice', 'catrice.jpg', 'Mỹ phẩm Đức', 'Đức', TRUE),
('Eglips', 'eglips.jpg', 'Phấn phủ kiềm dầu', 'Hàn Quốc', TRUE),
('I''m Meme', 'im_meme.jpg', 'Phong cách trẻ trung', 'Hàn Quốc', TRUE),
('Lemonade', 'lemonade.jpg', 'Thuần chay VN', 'Việt Nam', TRUE),
('Silkygirl', 'silkygirl.jpg', 'Giá mềm Malaysia', 'Malaysia', TRUE),
('Too Cool For School', 'tcfs.jpg', 'Nghệ thuật Hàn', 'Hàn Quốc', TRUE),
('Dr.G', 'drg.jpg', 'Dược mỹ phẩm Hàn', 'Hàn Quốc', TRUE),
('Himalaya', 'himalaya.jpg', 'Thảo mộc Ấn Độ', 'Ấn Độ', TRUE),
('Exclusive Cosmetic', 'exclusivecosmetic.jpg', 'Mỹ phẩm Nga', 'Nga', TRUE),
('Meishoku', 'meishoku.jpg', 'Nội địa Nhật', 'Nhật Bản', TRUE),
('Naruko', 'naruko.jpg', 'Tràm trà Đài Loan', 'Đài Loan', TRUE),
('Organic Shop', 'organicshop.jpg', 'Hữu cơ thiên nhiên', 'Nga', TRUE),
('Simple', 'simple.png', 'Lành tính Anh Quốc', 'Anh Quốc', TRUE),
('Pyunkang Yul', 'pyunkangyul.png', 'Y học cổ truyền Hàn', 'Hàn Quốc', TRUE),
('Bioderma', 'bioderma.png', 'Dược mỹ phẩm Pháp', 'Pháp', TRUE),
('Colorkey', 'colorkey.png', 'Makeup Trung Quốc', 'Trung Quốc', TRUE),
('Rwine', 'rwine.png', 'Mask Nhật Bản', 'Nhật Bản', TRUE),
('Nature Republic', 'nature_republic.png', 'Thiên nhiên Hàn', 'Hàn Quốc', TRUE),
('Saborino', 'saborino.png', 'Mask buổi sáng', 'Nhật Bản', TRUE),
('Caryophy', 'caryophy.png', 'Trị mụn Hàn Quốc', 'Hàn Quốc', TRUE),
('The Face Shop', 'the_face_shop.png', 'Thiên nhiên Hàn', 'Hàn Quốc', TRUE),
('Anua', 'anua.png', 'Lành tính Hàn Quốc', 'Hàn Quốc', TRUE),
('SVR', 'svr.png', 'Dược mỹ phẩm Pháp', 'Pháp', TRUE),
('Image Skincare', 'image_skincare.png', 'Chuyên nghiệp Mỹ', 'Mỹ', TRUE),
('Laneige', 'laneige.png', 'Mỹ phẩm cao cấp Hàn', 'Hàn Quốc', TRUE),
('Dior', 'dior.png', 'Nước hoa Pháp', 'Pháp', TRUE),
('Versace', 'versace.png', 'Nước hoa Ý', 'Ý', TRUE),
('Calvin Klein', 'calvin_klein.png', 'Nước hoa Mỹ', 'Mỹ', TRUE),
('Hugo Boss', 'hugo_boss.png', 'Nước hoa Đức', 'Đức', TRUE),
('Lacoste', 'lacoste.png', 'Nước hoa thể thao', 'Pháp', TRUE),
('Montblanc', 'montblanc.png', 'Nước hoa Đức', 'Đức', TRUE),
('Burberry', 'burberry.png', 'Nước hoa Anh', 'Anh', TRUE),
('Tommy Hilfiger', 'tommy_hilfiger.png', 'Nước hoa Mỹ', 'Mỹ', TRUE);

-- =====================================================================
-- INSERT PRODUCTS
-- - shop_id = NULL → Admin/Platform products (official)
-- - shop_id = 1 → "Mỹ Phẩm An Nguyễn" shop products (vendor An)
-- - shop_id = 2 → "Cosmetic House Bình" shop products (vendor Bình)
-- =====================================================================

INSERT INTO products(
  description, discount, entered_date, price, product_image, product_name,
  quantity, status, category_id, brand_id, shop_id, manufacture_date, expiry_date, favorite
)
VALUES
-- ═══════ SON MÔI - SHOP 1 (An Nguyễn) ═══════
('Son thỏi YSL Rouge Pur Couture #01 tông đỏ tươi rực rỡ, chất son lì sang trọng', 0, '2025-10-08', 1220000,
 'SonYSLDoTuoi.jpg', 'YSL Rouge Pur Couture #01 Đỏ Tươi', 70, TRUE, 1, 3, 1, '2025-06-01', '2028-06-01', FALSE),

('Son kem lì YSL Tatouage Couture Velvet Cream #216 tông hồng đất dịu nhẹ', 0, '2025-10-08', 1180000,
 'YSLHongDat.jpg', 'YSL Tatouage Couture Velvet Cream #216 Hồng Đất', 80, TRUE, 1, 3, 1, '2025-07-01', '2028-07-01', FALSE),

('Son thỏi Ailus M4V #03 tông cam cháy trẻ trung, giá bình dân', 0, '2025-10-08', 150000,
 'aliusCam.jpg', 'Ailus Stress Free Lipstick M4V #03 Cam Cháy', 100, TRUE, 1, 12, 1, '2025-06-20', '2028-06-20', FALSE),

('Son thỏi Ailus M3V #01 tông đỏ tươi, mềm mượt dễ tán', 0, '2025-10-08', 150000,
 'aliusDo.jpg', 'Ailus Stress Free Lipstick M3V #01 Đỏ Tươi', 110, TRUE, 1, 12, 1, '2025-06-25', '2028-06-25', FALSE),

('Son thỏi Chanel Rouge Coco Flash #116 tông cam san hô tươi sáng', 0, '2025-10-08', 1150000,
 'SonChanelCam.png', 'Chanel Rouge Coco Flash #116 Cam San Hô', 60, TRUE, 1, 7, 1, '2025-06-10', '2028-06-10', FALSE),

('Son thỏi Chanel Rouge Coco Flash #106 tông đỏ tươi cổ điển', 0, '2025-10-08', 1150000,
 'SonChanelDo.png', 'Chanel Rouge Coco Flash #106 Đỏ Tươi', 55, TRUE, 1, 7, 1, '2025-06-15', '2028-06-15', FALSE),

('Son thỏi Chanel Rouge Coco Flash #108 tông đỏ hồng nữ tính', 0, '2025-10-08', 1150000,
 'SonChannelDoHong.png', 'Chanel Rouge Coco Flash #108 Đỏ Hồng', 65, TRUE, 1, 7, 1, '2025-06-18', '2028-06-18', FALSE),

('Son lì 3CE Cashmere Hue Lipstick tông đỏ đất trendy', 0, '2025-10-08', 380000,
 'sonli3CE.jpg', '3CE Cashmere Hue Lipstick Đỏ Đất', 90, TRUE, 1, 5, 1, '2025-07-05', '2028-07-05', FALSE),

('Son thỏi YSL Rouge Pur Couture The Slim tông đỏ quyến rũ', 5, '2025-10-08', 1250000,
 'sonSYL_Rouge.png', 'YSL Rouge Pur Couture The Slim Đỏ Quyến Rũ', 75, TRUE, 1, 3, 1, '2025-05-25', '2028-05-25', FALSE),

('Son kem lì YSL Tatouage Couture Matte Stain #13 tông đỏ cam', 5, '2025-10-08', 1180000,
 'SonYSLDoCam.jpg', 'YSL Tatouage Couture Matte Stain #13 Đỏ Cam', 70, TRUE, 1, 3, 1, '2025-06-05', '2028-06-05', FALSE),

-- ═══════ KEM DƯỠNG DA - SHOP 2 (Bình - Dược) & ADMIN ═══════
('Kem dưỡng Olay Total Effects 7 in One Day Moisturiser SPF30', 5, '2025-10-08', 280000,
 'KemOlay.png', 'Olay Total Effects 7 in One Day Moisturiser SPF30', 120, TRUE, 2, 2, NULL, '2025-07-01', '2028-07-01', FALSE),

('Kem dưỡng Sebamed Relief Face Cream 5% Urea dành cho da khô', 0, '2025-10-08', 320000,
 'kemSebamed.png', 'Sebamed Relief Face Cream 5% Urea', 100, TRUE, 2, 9, 2, '2025-06-15', '2028-06-15', FALSE),

('ZO Skin Health Retinol Skin Brightener 1% cải thiện sắc tố', 5, '2025-10-08', 2500000,
 'kemZO.jpg', 'ZO Skin Health Retinol Skin Brightener 1%', 60, TRUE, 2, 4, NULL, '2025-06-01', '2028-06-01', FALSE),

('Kem dưỡng ẩm Aloins Eaude Cream S chiết xuất nha đam', 0, '2025-10-08', 180000,
 'kemALONIS.png', 'Aloins Eaude Cream S Aloe Extract', 150, TRUE, 2, 10, 1, '2025-05-20', '2028-05-20', FALSE),

('Kem dưỡng ẩm CeraVe Moisturising Cream chứa Ceramides & HA', 0, '2025-10-08', 350000,
 'kemCeraVe.png', 'CeraVe Moisturising Cream 340g', 130, TRUE, 2, 11, 2, '2025-05-10', '2028-05-10', FALSE),

('Balm dưỡng ẩm Avène XeraCalm A.D dành cho da rất khô', 0, '2025-10-08', 420000,
 'kemEAU.png', 'Avène XeraCalm A.D Lipid-Replenishing Balm 200ml', 90, TRUE, 2, 6, 2, '2025-06-01', '2028-06-01', FALSE),

('Kem dưỡng ẩm Eucerin AQUAporin ACTIVE cấp nước tức thì', 0, '2025-10-08', 380000,
 'kemEucerin.png', 'Eucerin AQUAporin ACTIVE Moisturising Cream', 110, TRUE, 2, 8, 2, '2025-07-01', '2028-07-01', FALSE),

('Kem dưỡng Eucerin Q10 ACTIVE giảm nếp nhăn', 0, '2025-10-08', 450000,
 'kemEucerinSang.png', 'Eucerin Q10 ACTIVE Anti-Wrinkle Face Cream', 100, TRUE, 2, 8, 2, '2025-07-10', '2028-07-10', FALSE),

('Kem phục hồi La Roche-Posay Cicaplast Baume B5 làm dịu da', 0, '2025-10-08', 280000,
 'kemLaRoche.png', 'La Roche-Posay Cicaplast Baume B5', 150, TRUE, 2, 1, 2, '2025-06-20', '2028-06-20', FALSE),

('Kem dưỡng ZO Skin Health Pigment Control 4% Hydroquinone', 5, '2025-10-08', 3200000,
 'kemNamZO.png', 'ZO Skin Health Pigment Control Crème 4% Hydroquinone', 50, TRUE, 2, 4, NULL, '2025-06-15', '2028-06-15', FALSE),

-- ═══════ SERUM - SHOP 2 (Bình) & ADMIN ═══════
('Serum The Ordinary Niacinamide 10% + Zinc 1% kiểm dầu, se lỗ chân lông', 0, '2025-10-07', 250000,
 'theordinary_niacinamide.jpg', 'The Ordinary Niacinamide 10% + Zinc 1%', 120, TRUE, 10, 13, NULL, '2025-07-01', '2028-07-01', FALSE),

('Serum La Roche-Posay Hyalu B5 phục hồi hàng rào da', 5, '2025-10-07', 1600000,
 'larocheposay_hyalu_b5.jpg', 'La Roche-Posay Hyalu B5 Serum', 80, TRUE, 10, 1, 2, '2025-06-01', '2028-06-01', FALSE),

('Dưỡng chất khoáng Vichy Minéral 89% + HA', 10, '2025-10-07', 850000,
 'vichy_mineral89.jpg', 'Vichy Minéral 89 Hyaluronic Acid Serum', 100, TRUE, 10, 14, 2, '2025-05-01', '2028-05-01', FALSE),

('Chiết xuất rau má Madagascar làm dịu da', 0, '2025-10-07', 300000,
 'skin1004_centella.jpg', 'Skin1004 Madagascar Centella Ampoule 55ml', 90, TRUE, 10, 15, 2, '2025-07-10', '2028-07-10', FALSE),

('Some By Mi AHA-BHA-PHA hỗ trợ sáng da & giảm mụn', 0, '2025-10-07', 350000,
 'somebymi_miracle.jpg', 'Some By Mi 30 Days Miracle Serum', 110, TRUE, 10, 16, 2, '2025-04-01', '2028-04-01', FALSE),

('Melano CC Vitamin C tinh khiết dưỡng sáng', 0, '2025-10-07', 220000,
 'melano_cc_vitc.jpg', 'Melano CC Vitamin C Brightening Serum 20ml', 150, TRUE, 10, 17, 1, '2025-08-01', '2028-08-01', FALSE),

('Serum Klairs Vitamin C 5% dịu nhẹ cho da nhạy cảm', 0, '2025-10-07', 320000,
 'klairs_vitc_drop.jpeg', 'Klairs Freshly Juiced Vitamin C Drop 35ml', 70, TRUE, 10, 18, 2, '2025-06-15', '2028-06-15', FALSE),

('L''Oréal Revitalift HA 1.5% đa kích thước cấp ẩm sâu', 5, '2025-10-07', 360000,
 'loreal_revitalift_ha15.jpg', 'L''Oréal Revitalift 1.5% Hyaluronic Acid Serum', 130, TRUE, 10, 24, NULL, '2025-05-20', '2028-05-20', FALSE),

('Paula''s Choice 10% Niacinamide Booster se lỗ chân lông', 0, '2025-10-07', 1790000,
 'paulaschoice_niacinamide10.jpg', 'Paula''s Choice 10% Niacinamide Booster 20ml', 40, TRUE, 10, 19, NULL, '2025-03-01', '2028-03-01', FALSE),

('Hada Labo Retinol B3 Serum cải thiện dấu hiệu lão hóa', 10, '2025-10-07', 300000,
 'hadalabo_retinol_b3.jpg', 'Hada Labo Retinol B3 Pro-Aging Serum 30ml', 85, TRUE, 10, 20, 1, '2025-06-01', '2028-06-01', FALSE),

-- ===================== KEM CHỐNG NẮNG - SHOP 1 & 2 & ADMIN =====================
('Kem chống nắng bí đao Cocoon SPF50+ PA++++, nhẹ mặt', 0, NOW(), 245000,
 'cocoon_winter_melon_spf50.png', 'COCOON Winter Melon Sunscreen SPF50+ PA++++ 50ml',
 60, TRUE, 7, (SELECT brand_id FROM brands WHERE brand_name='Cocoon'), 1, '2025-06-01', '2028-06-01', FALSE),

('Cetaphil Sun Light Gel SPF50+ PA++++, không nhờn rít', 0, NOW(), 390000,
 'cetaphil_sun_spf50_light_gel.png', 'Cetaphil Sun Light Gel SPF50+ PA++++ 50ml',
 80, TRUE, 7, (SELECT brand_id FROM brands WHERE brand_name='Cetaphil'), 2, '2025-06-01', '2028-06-01', FALSE),

('Vichy Capital Soleil Dry Touch SPF50 PA++++, kiềm dầu', 0, NOW(), 495000,
 'vichy_capital_soleil_dry_touch_spf50.png', 'Vichy Capital Soleil Dry Touch SPF50 50ml',
 90, TRUE, 7, (SELECT brand_id FROM brands WHERE brand_name='Vichy'), 2, '2025-05-01', '2028-05-01', FALSE),

('Fixderma Shadow SPF50+ PA+++, chống nắng mạnh', 0, NOW(), 260000,
 'fixderma_shadow_spf50_cream.png', 'Fixderma Shadow SPF50+ Cream 75g',
 70, TRUE, 7, (SELECT brand_id FROM brands WHERE brand_name='Fixderma'), NULL, '2025-04-01', '2028-04-01', FALSE),

('L''Oréal UV Defender Invisible Fluid SPF50+ PA++++, thấm nhanh', 0, NOW(), 330000,
 'loreal_uv_defender_invisible_fluid_spf50.png', 'L''Oréal UV Defender Invisible Fluid SPF50+ 50ml',
 100, TRUE, 7, (SELECT brand_id FROM brands WHERE brand_name='L''Oréal Paris'), NULL, '2025-06-01', '2028-06-01', FALSE),

('La Roche-Posay Anthelios UVMune 400 Oil Control Fluid SPF50+', 0, NOW(), 620000,
 'larocheposay_uvmune400_oil_control_spf50.png', 'La Roche-Posay Anthelios UVMune 400 Oil Control SPF50+ 50ml',
 120, TRUE, 7, (SELECT brand_id FROM brands WHERE brand_name='La Roche-Posay'), 2, '2025-06-01', '2028-06-01', FALSE),

('Anthelios XL Dry Touch Gel-Cream SPF50+, chống bóng nhờn', 0, NOW(), 580000,
 'larocheposay_anthelios_xl_dry_touch_spf50.png', 'La Roche-Posay Anthelios XL Dry Touch SPF50+ 50ml',
 90, TRUE, 7, (SELECT brand_id FROM brands WHERE brand_name='La Roche-Posay'), 2, '2025-05-01', '2028-05-01', FALSE),

('Sắc Ngọc Khang Tone Up Sun Gel-Cream SPF50+ PA++++', 0, NOW(), 155000,
 'sac_ngoc_khang_tone_up_spf50.png', 'Sắc Ngọc Khang Tone Up Sun SPF50+ 50g',
 110, TRUE, 7, (SELECT brand_id FROM brands WHERE brand_name='Sắc Ngọc Khang'), 1, '2025-06-01', '2028-06-01', FALSE),

('Sebamed Anti-Redness Light Day Care SPF20', 0, NOW(), 420000,
 'sebamed_anti_redness_day_spf20.png', 'Sebamed Anti-Redness Light Day Care SPF20 50ml',
 50, TRUE, 7, (SELECT brand_id FROM brands WHERE brand_name='Sebamed'), 2, '2025-05-01', '2027-05-01', FALSE),

('Reihaku Hatomugi UV Milky Gel SPF50+ PA++++', 0, NOW(), 210000,
 'reihaku_hatomugi_uv_milky_gel_spf50.png', 'Reihaku Hatomugi UV Milky Gel SPF50+ 80g',
 140, TRUE, 7, (SELECT brand_id FROM brands WHERE brand_name='Reihaku Hatomugi'), 1, '2025-06-01', '2028-06-01', FALSE),

-- ===================== PHẤN PHỦ - SHOP 1 (An) =====================
('Phấn phủ bột kiềm dầu Innisfree No-Sebum', 0, NOW(), 165000,
 'innisfree-no-sebum-mineral-5g.png', 'Innisfree No-Sebum Mineral Powder 5g', 
 120, TRUE, 8, (SELECT brand_id FROM brands WHERE brand_name='Innisfree'), 1, '2025-06-01', '2028-06-01', FALSE),

('Phấn nền kiềm dầu Catrice All Matt Plus', 0, NOW(), 195000,
 'catrice-all-matt-10g.png', 'Catrice All Matt Plus Shine Control Powder 10g', 
 150, TRUE, 8, (SELECT brand_id FROM brands WHERE brand_name='Catrice'), 1, '2025-05-01', '2028-05-01', FALSE),

('Phấn nền Eglips Oil Cut Powder Pact', 0, NOW(), 175000,
 'eglips-oil-cut-pact.png', 'Eglips Oil Cut Powder Pact', 
 140, TRUE, 8, (SELECT brand_id FROM brands WHERE brand_name='Eglips'), 1, '2025-04-01', '2028-04-01', FALSE),

('Phấn nền Eglips Glow Powder Pact', 0, NOW(), 185000,
 'eglips-glow-pact.png', 'Eglips Glow Powder Pact', 
 120, TRUE, 8, (SELECT brand_id FROM brands WHERE brand_name='Eglips'), 1, '2025-06-01', '2028-06-01', FALSE),

('Phấn Eglips Air Fit Powder Pact 8g', 0, NOW(), 185000,
 'eglips-air-fit-8g.png', 'Eglips Air Fit Powder Pact 8g', 
 130, TRUE, 8, (SELECT brand_id FROM brands WHERE brand_name='Eglips'), 1, '2025-05-01', '2028-05-01', FALSE),

('Phấn I''m Meme Oil Cut Pact 9.5g', 0, NOW(), 210000,
 'im-meme-oil-cut-9-5g.png', 'I''m Meme Oil Cut Pact 9.5g', 
 110, TRUE, 8, (SELECT brand_id FROM brands WHERE brand_name='I''m Meme'), 1, '2025-06-01', '2028-06-01', FALSE),

('Phấn bột Lemonade Supermatte thuần chay', 0, NOW(), 245000,
 'lemonade-supermatte-9g.png', 'Lemonade Supermatte No Makeup Loose Powder 9g', 
 100, TRUE, 8, (SELECT brand_id FROM brands WHERE brand_name='Lemonade'), 1, '2025-06-01', '2028-06-01', FALSE),

('Phấn bột Silkygirl No-Sebum Mineral Powder', 0, NOW(), 120000,
 'silkygirl-no-sebum-5g.png', 'Silkygirl No-Sebum Mineral Powder 5g', 
 160, TRUE, 8, (SELECT brand_id FROM brands WHERE brand_name='Silkygirl'), 1, '2025-04-01', '2028-04-01', FALSE),

('Phấn Silkygirl Let It Glow Tone Up Powder', 0, NOW(), 155000,
 'silkygirl-let-it-glow-7g.png', 'Silkygirl Let It Glow Tone Up Powder 7g', 
 140, TRUE, 8, (SELECT brand_id FROM brands WHERE brand_name='Silkygirl'), 1, '2025-05-01', '2028-05-01', FALSE),

('Phấn Too Cool For School Artclass By Rodin', 0, NOW(), 320000,
 'tcfs-artclass-rodin-4g.png', 'Too Cool For School Artclass By Rodin Finish Setting Pact 4g', 
 90, TRUE, 8, (SELECT brand_id FROM brands WHERE brand_name='Too Cool For School'), 1, '2025-06-01', '2028-06-01', FALSE),

-- ===================== TẨY TẾ BÀO CHẾT - ADMIN & SHOP =====================
('Gel tẩy tế bào chết Dr.G dịu nhẹ, làm sáng da', 0, NOW(), 290000,
 'drg-brightening-peeling-gel-120g.png', 'Dr.G Brightening Peeling Gel 120g', 
 80, TRUE, 9, (SELECT brand_id FROM brands WHERE brand_name='Dr.G'), 2, '2025-06-01', '2028-06-01', FALSE),

('Scrub Eucerin Pro Acne hỗ trợ giảm dầu, hạn chế mụn', 0, NOW(), 320000,
 'eucerin-pro-acne-scrub-100ml.png', 'Eucerin Pro Acne Solution Scrub 100ml', 
 90, TRUE, 9, (SELECT brand_id FROM brands WHERE brand_name='Eucerin'), 2, '2025-06-01', '2028-06-01', FALSE),

('Tẩy da chết Himalaya neem và mơ, ngừa mụn', 0, NOW(), 120000,
 'himalaya-neem-scrub-100ml.png', 'Himalaya Purifying Neem Scrub 100ml', 
 150, TRUE, 9, (SELECT brand_id FROM brands WHERE brand_name='Himalaya'), NULL, '2025-06-01', '2028-06-01', FALSE),

('Tẩy tế bào chết hóa học Paula''s Choice BHA 2%', 0, NOW(), 380000,
 'paulaschoice-bha-2-liquid-30ml.png', 'Paula''s Choice 2% BHA Liquid Exfoliant 30ml', 
 100, TRUE, 9, (SELECT brand_id FROM brands WHERE brand_name='Paula''s Choice'), NULL, '2025-06-01', '2028-06-01', FALSE),

('Gel scrub cà phê Exclusive Cosmetic Nga', 0, NOW(), 95000,
 'exclusive-cosmetic-coffee-scrub-100g.png', 'Exclusive Cosmetic Coffee Gel Scrub 100g', 
 120, TRUE, 9, (SELECT brand_id FROM brands WHERE brand_name='Exclusive Cosmetic'), 1, '2025-06-01', '2028-06-01', FALSE),

('Gel tẩy tế bào chết Meishoku Detclear AHA BHA', 0, NOW(), 310000,
 'meishoku-detclear-peeling-jelly-180ml.png', 'Meishoku Detclear Bright & Peel Peeling Jelly 180ml', 
 110, TRUE, 9, (SELECT brand_id FROM brands WHERE brand_name='Meishoku'), 1, '2025-06-01', '2028-06-01', FALSE),

('Tẩy tế bào chết môi Cocoon cà phê Đắk Lắk', 0, NOW(), 85000,
 'cocoon-dak-lak-coffee-lip-scrub-5g.png', 'Cocoon Dak Lak Coffee Lip Scrub 5g', 
 200, TRUE, 9, (SELECT brand_id FROM brands WHERE brand_name='Cocoon'), 1, '2025-06-01', '2028-06-01', FALSE),

('Gel tẩy tế bào chết Naruko tràm trà', 0, NOW(), 260000,
 'naruko-tea-tree-peeling-gel-120ml.png', 'Naruko Tea Tree Peeling Gel 120ml', 
 100, TRUE, 9, (SELECT brand_id FROM brands WHERE brand_name='Naruko'), 2, '2025-06-01', '2028-06-01', FALSE),

('Tẩy tế bào chết Organic Shop chiết xuất cà phê', 0, NOW(), 145000,
 'organicshop-soft-face-gommage-coffee-75ml.png', 'Organic Shop Soft Face Gommage Coffee 75ml', 
 90, TRUE, 9, (SELECT brand_id FROM brands WHERE brand_name='Organic Shop'), 1, '2025-06-01', '2028-06-01', FALSE),

('Tẩy tế bào chết toàn thân Organic Shop Body Desserts', 0, NOW(), 265000,
 'organicshop-body-desserts-scrub-450ml.png', 'Organic Shop Body Desserts Body Scrub 450ml', 
 70, TRUE, 9, (SELECT brand_id FROM brands WHERE brand_name='Organic Shop'), 1, '2025-06-01', '2028-06-01', FALSE),

-- ===================== TONER - SHOP 2 (Bình) & ADMIN =====================
('Toner Simple dịu nhẹ, không cồn', 0, NOW(), 107000,
 'simple_soothing_toner.png', 'Simple Kind To Skin Soothing Facial Toner 200ml', 
 120, TRUE, 5, (SELECT brand_id FROM brands WHERE brand_name='Simple'), NULL, '2025-07-01', '2027-07-01', FALSE),

('Toner Klairs Supple Preparation cân bằng pH', 0, NOW(), 233000,
 'klairs_supple_toner.png', 'Klairs Supple Preparation Facial Toner 180ml', 
 90, TRUE, 5, (SELECT brand_id FROM brands WHERE brand_name='Klairs'), 2, '2025-06-01', '2027-06-01', FALSE),

('Toner Skin1004 rau má Madagascar làm dịu da', 0, NOW(), 312000,
 'skin1004_centella_toner.png', 'Skin1004 Madagascar Centella Toning Toner 210ml', 
 100, TRUE, 5, (SELECT brand_id FROM brands WHERE brand_name='Skin1004'), 2, '2025-06-15', '2027-06-15', FALSE),

('Toner Some By Mi AHA-BHA-PHA 30 Days Miracle', 0, NOW(), 289000,
 'somebymi_miracle_toner.png', 'Some By Mi AHA-BHA-PHA 30 Days Miracle Toner 150ml', 
 130, TRUE, 5, (SELECT brand_id FROM brands WHERE brand_name='Some By Mi'), 2, '2025-05-01', '2027-05-01', FALSE),

('Toner Pyunkang Yul Essence dưỡng ẩm sâu', 0, NOW(), 210000,
 'pyunkangyul_essence_toner.png', 'Pyunkang Yul Essence Toner 200ml', 
 120, TRUE, 5, (SELECT brand_id FROM brands WHERE brand_name='Pyunkang Yul'), 2, '2025-04-20', '2027-04-20', FALSE),

('Toner Cocoon Sen Hậu Giang thuần chay', 0, NOW(), 173000,
 'cocoon_sen_toner.png', 'Cocoon Sen Hậu Giang Soothing Toner 140ml', 
 150, TRUE, 5, (SELECT brand_id FROM brands WHERE brand_name='Cocoon'), 1, '2025-07-10', '2027-07-10', FALSE),

('Toner La Roche-Posay Effaclar chứa BHA & LHA', 5, NOW(), 360000,
 'larocheposay_effaclar_toner.png', 'La Roche-Posay Effaclar Clarifying Toner 200ml', 
 80, TRUE, 5, (SELECT brand_id FROM brands WHERE brand_name='La Roche-Posay'), 2, '2025-06-01', '2027-06-01', FALSE),

('Toner Vichy Normaderm se khít lỗ chân lông', 5, NOW(), 380000,
 'vichy_normaderm_toner.png', 'Vichy Normaderm Purifying Pore-Tightening Toner 200ml', 
 90, TRUE, 5, (SELECT brand_id FROM brands WHERE brand_name='Vichy'), 2, '2025-05-01', '2027-05-01', FALSE),

('Toner Hada Labo Gokujyun với 3 loại HA', 0, NOW(), 245000,
 'hadalabo_gokujyun_toner.png', 'Hada Labo Gokujyun Hyaluronic Acid Lotion 170ml', 
 140, TRUE, 5, (SELECT brand_id FROM brands WHERE brand_name='Hada Labo'), 1, '2025-05-15', '2027-05-15', FALSE),

('Toner Bioderma Sensibio dịu nhẹ, không cồn', 0, NOW(), 320000,
 'bioderma_sensibio_tonique.png', 'Bioderma Sensibio Tonique 250ml', 
 85, TRUE, 5, (SELECT brand_id FROM brands WHERE brand_name='Bioderma'), 2, '2025-04-10', '2027-04-10', FALSE),

-- ===================== SỮA RỬA MẶT - SHOP 2 & ADMIN =====================
('Sữa rửa mặt CeraVe Foaming làm sạch sâu', 0, NOW(), 280000,
 'cerave_foaming.png', 'CeraVe Foaming Facial Cleanser 236ml', 
 100, TRUE, 4, (SELECT brand_id FROM brands WHERE brand_name='CeraVe'), 2, '2025-05-01', '2028-05-01', FALSE),

('Sữa rửa mặt Simple làm sạch dịu nhẹ', 0, NOW(), 91000,
 'simple_refreshing_wash.png', 'Simple Kind To Skin Refreshing Facial Wash 150ml', 
 150, TRUE, 4, (SELECT brand_id FROM brands WHERE brand_name='Simple'), NULL, '2025-06-01', '2027-06-01', FALSE),

('Sữa rửa mặt The Face Shop chiết xuất nước gạo', 0, NOW(), 30000,
 'tfs_rice_water.png', 'The Face Shop Rice Water Bright Cleanser 150ml', 
 200, TRUE, 4, (SELECT brand_id FROM brands WHERE brand_name='The Face Shop'), NULL, '2025-07-01', '2028-07-01', FALSE),

('Sữa rửa mặt Anua Heartleaf kiểm soát dầu', 0, NOW(), 220000,
 'anua_heartleaf.png', 'Anua Heartleaf Pore Deep Cleansing Foam 150ml', 
 120, TRUE, 4, (SELECT brand_id FROM brands WHERE brand_name='Anua'), 2, '2025-06-15', '2028-06-15', FALSE),

('Gel rửa mặt Cocoon bí đao thuần chay', 0, NOW(), 105000,
 'cocoon_winter_melon.png', 'Cocoon Winter Melon Cleanser 140ml', 
 180, TRUE, 4, (SELECT brand_id FROM brands WHERE brand_name='Cocoon'), 1, '2025-05-10', '2027-05-10', FALSE),

('Gel rửa mặt SVR Sebiaclear làm sạch dầu', 0, NOW(), 180000,
 'svr_sebiaclear.png', 'SVR Sebiaclear Gel Moussant 200ml', 
 90, TRUE, 4, (SELECT brand_id FROM brands WHERE brand_name='SVR'), 2, '2025-06-01', '2028-06-01', FALSE),

('Sữa rửa mặt Cetaphil Gentle Skin Cleanser', 0, NOW(), 150000,
 'cetaphil_gentle.png', 'Cetaphil Gentle Skin Cleanser 236ml', 
 140, TRUE, 4, (SELECT brand_id FROM brands WHERE brand_name='Cetaphil'), 2, '2025-07-01', '2028-07-01', FALSE),

('Sữa rửa mặt La Roche-Posay Toleriane dịu nhẹ', 0, NOW(), 320000,
 'laroche_toleriane.png', 'La Roche-Posay Toleriane Hydrating Gentle Cleanser 200ml', 
 95, TRUE, 4, (SELECT brand_id FROM brands WHERE brand_name='La Roche-Posay'), 2, '2025-06-01', '2028-06-01', FALSE),

('Gel rửa mặt Bioderma Sébium kiểm soát dầu', 0, NOW(), 210000,
 'bioderma_sebium.png', 'Bioderma Sébium Gel Moussant 200ml', 
 100, TRUE, 4, (SELECT brand_id FROM brands WHERE brand_name='Bioderma'), 2, '2025-05-15', '2028-05-15', FALSE),

('Sữa rửa mặt La Roche-Posay Effaclar dành cho da dầu', 0, NOW(), 350000,
 'laroche_effaclar.png', 'La Roche-Posay Effaclar Purifying Foaming Gel 200ml', 
 80, TRUE, 4, (SELECT brand_id FROM brands WHERE brand_name='La Roche-Posay'), 2, '2025-06-10', '2028-06-10', FALSE),

-- ===================== MẶT NẠ - SHOP 1 & ADMIN =====================
('Mask đất sét Innisfree Super Volcanic hút dầu', 0, NOW(), 270000,
 'innisfree_super_volcanic.png', 'Innisfree Super Volcanic Pore Clay Mask 100ml', 
 100, TRUE, 6, (SELECT brand_id FROM brands WHERE brand_name='Innisfree'), 1, '2025-05-01', '2028-05-01', FALSE),

('Mask đất sét SKIN1004 Centella Stick', 0, NOW(), 263000,
 'skin1004_centella_clay_stick.png', 'SKIN1004 Mad Centella Poremizing Clay Stick Mask 55g', 
 80, TRUE, 6, (SELECT brand_id FROM brands WHERE brand_name='Skin1004'), 2, '2025-06-01', '2028-06-01', FALSE),

('Mask enzyme Image Skincare Vital C', 0, NOW(), 873499,
 'image_vitalc_enzyme.png', 'Image Skincare Vital C Hydrating Enzyme Masque 57g', 
 50, TRUE, 6, (SELECT brand_id FROM brands WHERE brand_name='Image Skincare'), NULL, '2025-04-01', '2028-04-01', FALSE),

('Mask Colorkey Luminous B3 Brightening', 0, NOW(), 15000,
 'colorkey_b3_mask.png', 'Colorkey Luminous B3 Brightening Facial Mask 30ml', 
 200, TRUE, 6, (SELECT brand_id FROM brands WHERE brand_name='Colorkey'), NULL, '2025-07-01', '2027-07-01', FALSE),

('Mask Rwine Placenta Face Mask', 0, NOW(), 11000,
 'rwine_placenta_mask.png', 'Rwine Placenta Face Mask', 
 150, TRUE, 6, (SELECT brand_id FROM brands WHERE brand_name='Rwine'), NULL, '2025-08-01', '2027-08-01', FALSE),

('Mặt nạ ngủ Laneige Water Sleeping Mask', 0, NOW(), 227000,
 'laneige_water_sleeping_ex.png', 'Laneige Water Sleeping Mask Ex 70ml', 
 120, TRUE, 6, (SELECT brand_id FROM brands WHERE brand_name='Laneige'), 1, '2025-06-10', '2028-06-10', FALSE),

('Mặt nạ giấy Nature Republic chiết xuất cam', 0, NOW(), 18000,
 'nature_orange_sheet.png', 'Nature Republic Real Nature Orange Sheet Mask 23ml', 
 250, TRUE, 6, (SELECT brand_id FROM brands WHERE brand_name='Nature Republic'), NULL, '2025-07-15', '2028-07-15', FALSE),

('Mask buổi sáng Saborino', 0, NOW(), 364163,
 'saborino_morning_mask.png', 'Saborino Morning Facial Mask', 
 90, TRUE, 6, (SELECT brand_id FROM brands WHERE brand_name='Saborino'), NULL, '2025-05-20', '2028-05-20', FALSE),

('Mặt nạ Caryophy Portulaca giảm mụn', 0, NOW(), 22950,
 'caryophy_portulaca_mask.png', 'Caryophy Portulaca Mask Sheet 3-in-1', 
 180, TRUE, 6, (SELECT brand_id FROM brands WHERE brand_name='Caryophy'), 1, '2025-06-25', '2028-06-25', FALSE),

('Mặt nạ Klairs Midnight Blue làm dịu da', 0, NOW(), 44000,
 'klairs_midnight_blue.png', 'Klairs Midnight Blue Calming Sheet Mask', 
 140, TRUE, 6, (SELECT brand_id FROM brands WHERE brand_name='Klairs'), 2, '2025-06-01', '2028-06-01', FALSE),

-- ===================== NƯỚC HOA - ADMIN (Platform bán chính hãng) =====================
('Nước hoa Chanel No.5 huyền thoại', 0, NOW(), 2500000,
 'chanel_no5_edp.png', 'Chanel No.5 Eau de Parfum 50ml', 
 50, TRUE, 3, (SELECT brand_id FROM brands WHERE brand_name='Chanel'), NULL, '2025-06-01', '2028-06-01', FALSE),

('Nước hoa nam Dior Sauvage cay nồng', 0, NOW(), 2500000,
 'dior_sauvage_edt.png', 'Dior Sauvage Eau de Toilette 60ml', 
 80, TRUE, 3, (SELECT brand_id FROM brands WHERE brand_name='Dior'), NULL, '2025-06-01', '2028-06-01', FALSE),

('Nước hoa nam Versace Eros quyến rũ', 0, NOW(), 1200000,
 'versace_eros_edt.png', 'Versace Eros Eau de Toilette 50ml', 
 70, TRUE, 3, (SELECT brand_id FROM brands WHERE brand_name='Versace'), NULL, '2025-06-01', '2028-06-01', FALSE),

('Nước hoa unisex Calvin Klein CK One', 0, NOW(), 800000,
 'ck_one_edt.png', 'Calvin Klein CK One Eau de Toilette 100ml', 
 120, TRUE, 3, (SELECT brand_id FROM brands WHERE brand_name='Calvin Klein'), NULL, '2025-06-01', '2028-06-01', FALSE),

('Nước hoa nam Hugo Boss Bottled gỗ ấm', 0, NOW(), 1300000,
 'hugo_boss_bottled_edt.png', 'Hugo Boss Bottled Eau de Toilette 75ml', 
 90, TRUE, 3, (SELECT brand_id FROM brands WHERE brand_name='Hugo Boss'), NULL, '2025-06-01', '2028-06-01', FALSE),

('Nước hoa nam Lacoste L.12.12 Blanc thể thao', 0, NOW(), 1000000,
 'lacoste_l1212_blanc_edt.png', 'Lacoste L.12.12 Blanc Eau de Toilette 75ml', 
 100, TRUE, 3, (SELECT brand_id FROM brands WHERE brand_name='Lacoste'), NULL, '2025-06-01', '2028-06-01', FALSE),

('Nước hoa nam Montblanc Legend gỗ sang trọng', 0, NOW(), 1200000,
 'montblanc_legend_edt.png', 'Montblanc Legend Eau de Toilette 50ml', 
 85, TRUE, 3, (SELECT brand_id FROM brands WHERE brand_name='Montblanc'), NULL, '2025-06-01', '2028-06-01', FALSE),

('Nước hoa nam Burberry Brit cổ điển', 0, NOW(), 1100000,
 'burberry_brit_edt.png', 'Burberry Brit Eau de Toilette 50ml', 
 75, TRUE, 3, (SELECT brand_id FROM brands WHERE brand_name='Burberry'), NULL, '2025-06-01', '2028-06-01', FALSE),

('Nước hoa nam Tommy Hilfiger Tommy trẻ trung', 0, NOW(), 1300000,
 'tommy_hilfiger_tommy_edt.png', 'Tommy Hilfiger Tommy Eau de Toilette 100ml', 
 110, TRUE, 3, (SELECT brand_id FROM brands WHERE brand_name='Tommy Hilfiger'), NULL, '2025-06-01', '2028-06-01', FALSE),

('Nước hoa nữ Chanel Coco Mademoiselle quyến rũ', 0, NOW(), 4400000,
 'chanel_coco_mademoiselle_edp.png', 'Chanel Coco Mademoiselle Eau de Parfum 50ml', 
 60, TRUE, 3, (SELECT brand_id FROM brands WHERE brand_name='Chanel'), NULL, '2025-06-01', '2028-06-01', FALSE);

-- ===============================
-- INSERT SAMPLE ORDERS FOR TESTING
-- ===============================
INSERT INTO orders(
    user_id, customer_name, customer_email, customer_phone, shipping_address, note,
    status, payment_method, total_amount, final_amount, shipping_fee, discount_amount,
    order_date, estimated_delivery_date, shop_id, payment_status
)
VALUES
-- Order 1: User Chi orders from Shop 1 (An Nguyễn)
(1, 'Trần Thảo Chi', 'chi@gmail.com', '0901234567', 
 '123 Nguyễn Huệ, Quận 1, TP.HCM', 'Giao hàng vào buổi chiều',
 'PENDING', 'COD', 150000, 150000, 0, 0,
 '2025-10-10 10:30:00', '2025-10-14 10:30:00', 1, FALSE),

-- Order 2: User Đồng orders from Shop 2 (Bình)
(2, 'Trần Hữu Đồng', 'dong@gmail.com', '0912345678',
 '456 Lê Lợi, Quận 1, TP.HCM', 'Cần giao nhanh',
 'CONFIRMED', 'MOMO', 320000, 320000, 0, 0,
 '2025-10-09 14:20:00', '2025-10-13 14:20:00', 2, TRUE),

-- Order 3: User Demo orders from Admin/Platform
(3, 'User Demo', 'user@gmail.com', '0923456789',
 '789 Trần Hưng Đạo, Quận 1, TP.HCM', 'Giao hàng cuối tuần',
 'SHIPPING', 'BANK_TRANSFER', 2500000, 2500000, 0, 0,
 '2025-10-08 09:15:00', '2025-10-12 09:15:00', NULL, TRUE);

-- Insert order details
INSERT INTO order_details(order_id, product_id, product_name, unit_price, quantity, total_price)
VALUES
-- Order 1 details (Ailus lipstick)
(1, (SELECT product_id FROM products WHERE product_name LIKE '%Ailus%' AND product_name LIKE '%Cam%' LIMIT 1), 
 'Ailus Stress Free Lipstick M4V #03 Cam Cháy', 150000, 1, 150000),

-- Order 2 details (Sebamed cream)
(2, (SELECT product_id FROM products WHERE product_name LIKE '%Sebamed%' LIMIT 1), 
 'Sebamed Relief Face Cream 5% Urea', 320000, 1, 320000),

-- Order 3 details (Admin product - will add after inserting more products)
(3, 1, 'Sample Product', 2500000, 1, 2500000);

-- ===============================
-- UPDATE SHOP STATISTICS
-- ===============================
-- Disable safe update mode temporarily
SET SQL_SAFE_UPDATES = 0;

UPDATE shops SET total_products = (
    SELECT COUNT(*) FROM products WHERE shop_id = shops.shop_id
);

-- Note: Safe update mode will be re-enabled at the end of script

-- ===============================
-- TABLE: cart_items (Shopping Cart)
-- ===============================
CREATE TABLE cart_items (
    cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    shop_id BIGINT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    selected BOOLEAN NOT NULL DEFAULT TRUE,
    created_date DATETIME DEFAULT NOW(),
    updated_date DATETIME DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (shop_id) REFERENCES shops(shop_id) ON DELETE SET NULL,
    UNIQUE(user_id, product_id) -- Prevent duplicate products for same user
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for better performance
CREATE INDEX IX_cart_items_user_id ON cart_items(user_id);
CREATE INDEX IX_cart_items_product_id ON cart_items(product_id);
CREATE INDEX IX_cart_items_shop_id ON cart_items(shop_id);

-- ===============================
-- TABLE: promotions
-- ===============================
CREATE TABLE promotions (
    promotion_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    promotion_name         VARCHAR(200) NOT NULL,
    description            VARCHAR(1000),
    promotion_code         VARCHAR(50) NOT NULL UNIQUE,
    promotion_type         ENUM('PERCENTAGE', 'FIXED_AMOUNT', 'FREE_SHIPPING', 'BUY_X_GET_Y') NOT NULL,
    discount_value         DECIMAL(10,2) NOT NULL CHECK (discount_value >= 0),
    minimum_order_amount   DECIMAL(10,2) NOT NULL CHECK (minimum_order_amount >= 0),
    maximum_discount_amount DECIMAL(10,2) NOT NULL CHECK (maximum_discount_amount >= 0),
    usage_limit            INT NOT NULL CHECK (usage_limit > 0),
    used_count             INT NOT NULL DEFAULT 0 CHECK (used_count >= 0),
    start_date             DATETIME NOT NULL,
    end_date               DATETIME NOT NULL,
    is_active              BOOLEAN NOT NULL DEFAULT TRUE,
    shop_id                BIGINT NOT NULL,
    created_by             BIGINT NOT NULL,
    created_at             DATETIME NOT NULL DEFAULT NOW(),
    updated_at             DATETIME NOT NULL DEFAULT NOW() ON UPDATE NOW(),
    
    -- Foreign Key Constraints
    CONSTRAINT FK_promotions_shop FOREIGN KEY (shop_id) REFERENCES shops(shop_id) ON DELETE CASCADE,
    CONSTRAINT FK_promotions_created_by FOREIGN KEY (created_by) REFERENCES user(user_id) ON DELETE CASCADE,
    
    -- Constraints
    CONSTRAINT CHK_promotions_dates CHECK (end_date > start_date),
    CONSTRAINT CHK_promotions_usage CHECK (used_count <= usage_limit),
    CONSTRAINT CHK_promotions_discount CHECK (discount_value <= maximum_discount_amount)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for better performance
CREATE INDEX IX_promotions_code ON promotions(promotion_code);
CREATE INDEX IX_promotions_type ON promotions(promotion_type);
CREATE INDEX IX_promotions_active ON promotions(is_active);
CREATE INDEX IX_promotions_dates ON promotions(start_date, end_date);
CREATE INDEX IX_promotions_usage ON promotions(used_count, usage_limit);
CREATE INDEX IX_promotions_shop_id ON promotions(shop_id);

-- Insert sample promotion data
INSERT INTO promotions (
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
    is_active,
    shop_id,
    created_by
) VALUES 
-- Product percentage discount
(
    'Giảm giá 20% cho đơn hàng từ 500k',
    'Áp dụng cho tất cả sản phẩm, giảm 20% cho đơn hàng từ 500,000đ trở lên',
    'SAVE20',
    'PERCENTAGE',
    20.00,
    500000.00,
    200000.00,
    1000,
    0,
    '2025-01-01 00:00:00',
    '2025-12-31 23:59:59',
    TRUE,
    1,  -- shop_id = 1 (Mỹ Phẩm An Nguyễn)
    4   -- created_by = 4 (Admin)
),
-- Shipping discount
(
    'Miễn phí ship cho đơn hàng từ 300k',
    'Miễn phí vận chuyển cho đơn hàng từ 300,000đ trở lên',
    'FREESHIP',
    'FREE_SHIPPING',
    50000.00,
    300000.00,
    50000.00,
    500,
    0,
    '2025-01-01 00:00:00',
    '2025-12-31 23:59:59',
    TRUE,
    1,  -- shop_id = 1 (Mỹ Phẩm An Nguyễn)
    4   -- created_by = 4 (Admin)
),
-- Fixed amount discount
(
    'Giảm 100k cho đơn hàng từ 1 triệu',
    'Giảm ngay 100,000đ cho đơn hàng từ 1,000,000đ trở lên',
    'SAVE100K',
    'FIXED_AMOUNT',
    100000.00,
    1000000.00,
    100000.00,
    200,
    0,
    '2025-01-01 00:00:00',
    '2025-12-31 23:59:59',
    TRUE,
    2,  -- shop_id = 2 (Cosmetic House Bình)
    4   -- created_by = 4 (Admin)
),
-- Expired promotion
(
    'Khuyến mãi Tết 2025',
    'Giảm giá đặc biệt nhân dịp Tết Nguyên Đán 2025',
    'TET2025',
    'PERCENTAGE',
    15.00,
    200000.00,
    150000.00,
    100,
    95,
    '2025-01-01 00:00:00',
    '2025-02-15 23:59:59',
    FALSE,
    1,  -- shop_id = 1 (Mỹ Phẩm An Nguyễn)
    4   -- created_by = 4 (Admin)
),
-- Expiring soon promotion
(
    'Khuyến mãi Black Friday',
    'Giảm giá lớn nhân dịp Black Friday',
    'BLACKFRIDAY',
    'PERCENTAGE',
    30.00,
    1000000.00,
    500000.00,
    50,
    5,
    '2025-11-01 00:00:00',
    '2025-12-31 23:59:59',
    TRUE,
    2,  -- shop_id = 2 (Cosmetic House Bình)
    4   -- created_by = 4 (Admin)
);

-- =====================================================
-- TABLE: shop_shippers (MANY-TO-MANY RELATIONSHIP)
-- =====================================================

CREATE TABLE shop_shippers (
    shop_id BIGINT NOT NULL,
    shipper_id BIGINT NOT NULL,
    assigned_date DATETIME DEFAULT NOW(),
    status BOOLEAN DEFAULT TRUE,
    notes VARCHAR(500),
    
    PRIMARY KEY (shop_id, shipper_id),
    CONSTRAINT FK_shop_shippers_shop FOREIGN KEY (shop_id) REFERENCES shops(shop_id) ON DELETE CASCADE,
    CONSTRAINT FK_shop_shippers_user FOREIGN KEY (shipper_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add sample data for shop_shippers
INSERT INTO shop_shippers (shop_id, shipper_id, assigned_date, status, notes)
SELECT 
    s.shop_id, 
    u.user_id, 
    NOW(), 
    TRUE,
    CONCAT('Shipper mặc định cho shop ', s.shop_name)
FROM shops s
CROSS JOIN user u
INNER JOIN users_roles ur ON u.user_id = ur.user_id
INNER JOIN role r ON ur.role_id = r.id
WHERE r.name = 'ROLE_SHIPPER' 
  AND s.status = 'ACTIVE'
LIMIT 2;

-- Create indexes for performance optimization
CREATE INDEX idx_shop_shippers_shop ON shop_shippers(shop_id, status);
CREATE INDEX idx_shop_shippers_shipper ON shop_shippers(shipper_id, status);

-- ===============================
-- ONE XU SYSTEM
-- ===============================

-- TABLE: one_xu_transactions
CREATE TABLE one_xu_transactions (
    transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    transaction_type ENUM('CHECKIN', 'ORDER_REWARD', 'PURCHASE', 'REFUND') NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    balance_after DECIMAL(18,2) NOT NULL,
    description VARCHAR(500),
    order_id BIGINT NULL,
    created_at DATETIME DEFAULT NOW(),
    CONSTRAINT FK_xu_trans_user FOREIGN KEY(user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_xu_trans_order FOREIGN KEY(order_id) REFERENCES orders(order_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLE: one_xu_weekly_schedule
CREATE TABLE one_xu_weekly_schedule (
    day_of_week INT NOT NULL PRIMARY KEY CHECK (day_of_week BETWEEN 1 AND 7),
    xu_reward DECIMAL(18,2) NOT NULL,
    description VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add sample data for weekly schedule
INSERT INTO one_xu_weekly_schedule (day_of_week, xu_reward, description) VALUES 
(1, 100, 'Thứ 2 - 100 Xu'),
(2, 100, 'Thứ 3 - 100 Xu'),
(3, 200, 'Thứ 4 - 200 Xu'),
(4, 200, 'Thứ 5 - 200 Xu'),
(5, 300, 'Thứ 6 - 300 Xu'),
(6, 300, 'Thứ 7 - 300 Xu'),
(7, 1000, 'Chủ nhật - 1000 Xu');

-- Create indexes for One Xu system
CREATE INDEX IX_one_xu_transactions_user ON one_xu_transactions(user_id);
CREATE INDEX IX_one_xu_transactions_type ON one_xu_transactions(transaction_type);
CREATE INDEX IX_one_xu_transactions_created ON one_xu_transactions(created_at);

-- Add sample data for One Xu system
-- Update One Xu balance for existing users (safe - using primary key)
UPDATE user SET one_xu_balance = 1000 WHERE user_id = 1; -- Chi
UPDATE user SET one_xu_balance = 500 WHERE user_id = 2;  -- Đồng
UPDATE user SET one_xu_balance = 2000 WHERE user_id = 3; -- User Demo

-- Add sample transactions (including check-in)
INSERT INTO one_xu_transactions (user_id, transaction_type, amount, balance_after, description, order_id, created_at) VALUES
(1, 'CHECKIN', 100, 100, 'Check-in Thứ 2 - 100 Xu', NULL, '2025-10-10 09:00:00'),
(1, 'CHECKIN', 100, 200, 'Check-in Thứ 3 - 100 Xu', NULL, '2025-10-11 08:30:00'),
(1, 'CHECKIN', 200, 400, 'Check-in Thứ 4 - 200 Xu', NULL, '2025-10-12 10:15:00'),
(1, 'ORDER_REWARD', 150, 550, 'Thưởng từ đơn hàng #1 (1% giá trị đơn hàng)', 1, '2025-10-12 14:30:00'),
(2, 'CHECKIN', 100, 100, 'Check-in Thứ 2 - 100 Xu', NULL, '2025-10-10 14:20:00'),
(3, 'CHECKIN', 100, 100, 'Check-in Thứ 2 - 100 Xu', NULL, '2025-10-10 16:45:00'),
(3, 'CHECKIN', 100, 200, 'Check-in Thứ 3 - 100 Xu', NULL, '2025-10-11 11:30:00'),
(3, 'CHECKIN', 200, 400, 'Check-in Thứ 4 - 200 Xu', NULL, '2025-10-12 13:15:00'),
(3, 'CHECKIN', 200, 600, 'Check-in Thứ 5 - 200 Xu', NULL, '2025-10-13 09:45:00'),
(3, 'CHECKIN', 300, 900, 'Check-in Thứ 6 - 300 Xu', NULL, '2025-10-14 12:00:00'),
(3, 'CHECKIN', 300, 1200, 'Check-in Thứ 7 - 300 Xu', NULL, '2025-10-15 15:30:00'),
(3, 'CHECKIN', 1000, 2200, 'Check-in Chủ nhật - 1000 Xu', NULL, '2025-10-16 10:00:00'),
(3, 'ORDER_REWARD', 250, 2450, 'Thưởng từ đơn hàng #3 (1% giá trị đơn hàng)', 3, '2025-10-16 16:20:00');

-- ===============================
-- TABLE: comment_media
-- ===============================
CREATE TABLE comment_media (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id   BIGINT       NOT NULL,
    media_type   ENUM('IMAGE','VIDEO') NOT NULL,
    url          VARCHAR(500) NOT NULL,
    CONSTRAINT FK_comment_media_comment
        FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create index for better query performance by comment
CREATE INDEX IX_comment_media_comment ON comment_media(comment_id);

-- ================================================
-- TRIGGERS (MySQL Format)
-- ================================================

-- Trigger to keep cart_items.shop_id synced with products.shop_id
DELIMITER $$

CREATE TRIGGER TR_cart_items_set_shop
AFTER INSERT ON cart_items
FOR EACH ROW
BEGIN
    UPDATE cart_items ci
    JOIN products p ON p.product_id = ci.product_id
    SET ci.shop_id = p.shop_id,
        ci.updated_date = NOW()
    WHERE ci.cart_item_id = NEW.cart_item_id;
END$$

CREATE TRIGGER TR_cart_items_update_shop
AFTER UPDATE ON cart_items
FOR EACH ROW
BEGIN
    UPDATE cart_items ci
    JOIN products p ON p.product_id = ci.product_id
    SET ci.shop_id = p.shop_id,
        ci.updated_date = NOW()
    WHERE ci.cart_item_id = NEW.cart_item_id;
END$$

DELIMITER ;

-- ================================================
-- VIEWS (MySQL Format)
-- ================================================

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
FROM promotions
WHERE is_active = TRUE 
    AND start_date <= NOW() 
    AND end_date >= NOW()
    AND used_count < usage_limit;

-- ================================================
-- STORED PROCEDURES AND FUNCTIONS (MySQL Format)
-- ================================================

DELIMITER $$

-- Function to check if promotion is valid
CREATE FUNCTION fn_is_promotion_valid(promotion_code_param VARCHAR(50))
RETURNS BOOLEAN
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE is_valid BOOLEAN DEFAULT FALSE;
    
    IF EXISTS (
        SELECT 1 FROM promotions 
        WHERE promotion_code = promotion_code_param 
            AND is_active = TRUE 
            AND start_date <= NOW() 
            AND end_date >= NOW()
            AND used_count < usage_limit
    ) THEN
        SET is_valid = TRUE;
    END IF;
    
    RETURN is_valid;
END$$

-- Stored procedure to apply promotion
CREATE PROCEDURE sp_apply_promotion(
    IN promotion_code_param VARCHAR(50),
    IN order_amount_param DECIMAL(10,2),
    OUT discount_amount_param DECIMAL(10,2),
    OUT is_valid_param BOOLEAN
)
BEGIN
    DECLARE promotion_id_var BIGINT;
    DECLARE promotion_type_var VARCHAR(20);
    DECLARE discount_value_var DECIMAL(10,2);
    DECLARE minimum_order_amount_var DECIMAL(10,2);
    DECLARE maximum_discount_amount_var DECIMAL(10,2);
    DECLARE used_count_var INT;
    DECLARE usage_limit_var INT;
    
    -- Get promotion details
    SELECT 
        promotion_id,
        promotion_type,
        discount_value,
        minimum_order_amount,
        maximum_discount_amount,
        used_count,
        usage_limit
    INTO 
        promotion_id_var,
        promotion_type_var,
        discount_value_var,
        minimum_order_amount_var,
        maximum_discount_amount_var,
        used_count_var,
        usage_limit_var
    FROM promotions
    WHERE promotion_code = promotion_code_param 
        AND is_active = TRUE 
        AND start_date <= NOW() 
        AND end_date >= NOW();
    
    -- Check if promotion exists and is valid
    IF promotion_id_var IS NULL OR used_count_var >= usage_limit_var OR order_amount_param < minimum_order_amount_var THEN
        SET is_valid_param = FALSE;
        SET discount_amount_param = 0;
    ELSE
        -- Calculate discount amount based on promotion type
        IF promotion_type_var = 'PERCENTAGE' THEN
            SET discount_amount_param = order_amount_param * (discount_value_var / 100);
            IF discount_amount_param > maximum_discount_amount_var THEN
                SET discount_amount_param = maximum_discount_amount_var;
            END IF;
        ELSEIF promotion_type_var = 'FIXED_AMOUNT' THEN
            SET discount_amount_param = discount_value_var;
            IF discount_amount_param > maximum_discount_amount_var THEN
                SET discount_amount_param = maximum_discount_amount_var;
            END IF;
        ELSEIF promotion_type_var = 'FREE_SHIPPING' THEN
            SET discount_amount_param = discount_value_var;
        END IF;
        
        -- Update used count
        UPDATE promotions 
        SET used_count = used_count + 1
        WHERE promotion_id = promotion_id_var;
        
        SET is_valid_param = TRUE;
    END IF;
END$$

DELIMITER ;

-- ================================================
-- FINAL SETUP & RESTORE SETTINGS
-- ================================================

-- Add comments for documentation
ALTER TABLE cart_items COMMENT = 'Table to store individual cart items for each user';
ALTER TABLE cart_items MODIFY COLUMN selected BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Indicates whether this cart item is selected for checkout (1 = selected, 0 = not selected)';

-- Restore MySQL settings to default/safe values
SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;
SET SESSION sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';

-- Commit all changes
COMMIT;

-- ================================================
-- SCRIPT EXECUTION COMPLETED SUCCESSFULLY! 
-- Total tables created: 20+
-- Total sample data: 100+ products, 57+ brands, 9 users
-- ================================================
