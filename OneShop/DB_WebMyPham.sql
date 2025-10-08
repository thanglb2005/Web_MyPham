/* ===============================
   RESET DATABASE
   =============================== */
IF DB_ID('WebMyPham') IS NOT NULL
BEGIN
    ALTER DATABASE WebMyPham SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE WebMyPham;
END;
GO
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
INSERT INTO dbo.brands(brand_name, brand_image, description, origin, status)
VALUES 
(N'Maybelline', 'maybelline.jpg', N'Thương hiệu mỹ phẩm nổi tiếng từ Mỹ', N'Mỹ', 1),
(N'Innisfree', 'innisfree.jpg', N'Mỹ phẩm thiên nhiên từ đảo Jeju', N'Hàn Quốc', 1),
(N'L''Oreal', 'loreal.jpg', N'Tập đoàn mỹ phẩm hàng đầu thế giới', N'Pháp', 1),
(N'Nivea', 'nivea.jpg', N'Thương hiệu dưỡng da lâu đời từ Đức', N'Đức', 1);
GO
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
INSERT INTO dbo.products(description, discount, price, product_image, product_name, quantity, category_id, brand_id, manufacture_date, expiry_date)
VALUES
(N'Son môi cao cấp, giữ màu lâu, không chì', 10, 250000, 'son.jpg', N'Son đỏ Ruby', 100, 1, 1, '2025-01-15', '2028-01-15'),
(N'Kem dưỡng trắng da ban đêm, an toàn cho mọi loại da', 5, 350000, 'kem.jpg', N'Kem dưỡng ban đêm', 50, 2, 2, '2025-03-20', '2027-03-20'),
(N'Nước hoa hương hoa hồng sang trọng, lưu hương 12h', 0, 1200000, 'nuochoa.jpg', N'Nước hoa Rose', 30, 3, 3, '2025-05-10', '2030-05-10');
GO

/* ===============================
   TABLE: orders
   =============================== */
IF OBJECT_ID('dbo.orders', 'U') IS NOT NULL DROP TABLE dbo.orders;
CREATE TABLE dbo.orders (
    order_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    address NVARCHAR(255),
    amount DECIMAL(18,2),
    order_date DATETIME DEFAULT GETDATE(),
    phone NVARCHAR(50),
    status INT NOT NULL,
    user_id BIGINT,
    shipper_id BIGINT NULL, -- shipper giao hàng
    CONSTRAINT FK_orders_user FOREIGN KEY(user_id) REFERENCES dbo.[user](user_id),
    CONSTRAINT FK_orders_shipper FOREIGN KEY(shipper_id) REFERENCES dbo.[user](user_id)
);
INSERT INTO dbo.orders(address, amount, phone, status, user_id, shipper_id)
VALUES
(N'Hà Nội', 500000, '0911111111', 1, 1, 8),
(N'Đà Nẵng', 1200000, '0922222222', 2, 2, 8);
GO

/* ===============================
   TABLE: order_details
   =============================== */
IF OBJECT_ID('dbo.order_details', 'U') IS NOT NULL DROP TABLE dbo.order_details;
CREATE TABLE dbo.order_details (
    order_detail_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    price DECIMAL(18,2),
    quantity INT NOT NULL,
    order_id BIGINT,
    product_id BIGINT,
    CONSTRAINT FK_orderdetails_orders FOREIGN KEY(order_id) REFERENCES dbo.orders(order_id),
    CONSTRAINT FK_orderdetails_products FOREIGN KEY(product_id) REFERENCES dbo.products(product_id)
);
INSERT INTO dbo.order_details(price, quantity, order_id, product_id)
VALUES
(250000, 2, 1, 1),
(1200000, 1, 2, 3);
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
INSERT INTO dbo.comments(content, rating, order_detail_id, product_id, user_id)
VALUES
(N'Son màu đẹp, lâu trôi', 5, 1, 1, 1),
(N'Nước hoa mùi thơm dễ chịu, sang trọng', 4, 2, 3, 2);
GO

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
INSERT INTO dbo.favorites(product_id, user_id)
VALUES
(1, 1),
(3, 2);
GO
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