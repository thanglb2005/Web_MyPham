-- Tạo database
IF DB_ID('WebMyPham') IS NOT NULL
    DROP DATABASE WebMyPham;
GO
CREATE DATABASE WebMyPham;
GO

USE WebMyPham;
GO

-- Bảng categories
IF OBJECT_ID('categories', 'U') IS NOT NULL DROP TABLE categories;
CREATE TABLE categories (
    category_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    category_image NVARCHAR(255) NULL,
    category_name NVARCHAR(255) NULL
);

INSERT INTO categories(category_image, category_name) 
VALUES 
(NULL, N'Son môi'),
(NULL, N'Kem dưỡng da'),
(NULL, N'Nước hoa'),
(NULL, N'Sữa rửa mặt'),
(NULL, N'Toner');
GO

-- Bảng user
IF OBJECT_ID('[user]', 'U') IS NOT NULL DROP TABLE [user];
CREATE TABLE [user] (
    user_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    avatar NVARCHAR(255),
    email NVARCHAR(255) UNIQUE,
    name NVARCHAR(255),
    password NVARCHAR(255),
    register_date DATE,
    status BIT
);

-- Mật khẩu dạng text "123456"
INSERT INTO [user](avatar, email, name, password, register_date, status)
VALUES 
('user.png','chi@gmail.com',N'Trần Thảo Chi','123456','2025-09-04',1),
('user.png','dong@gmail.com',N'Trần Hữu Đồng','123456','2025-09-04',1),
('user.png','user@gmail.com','User Demo','123456','2025-09-04',1),
('user.png','admin@mypham.com','Admin Mỹ Phẩm','123456','2025-09-04',1);
GO

-- Bảng role
IF OBJECT_ID('role', 'U') IS NOT NULL DROP TABLE role;
CREATE TABLE role (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255)
);

INSERT INTO role(name)
VALUES ('ROLE_USER'),('ROLE_ADMIN');
GO

-- Bảng users_roles
IF OBJECT_ID('users_roles', 'U') IS NOT NULL DROP TABLE users_roles;
CREATE TABLE users_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT FK_users_roles_user FOREIGN KEY(user_id) REFERENCES [user](user_id),
    CONSTRAINT FK_users_roles_role FOREIGN KEY(role_id) REFERENCES role(id)
);

INSERT INTO users_roles(user_id, role_id)
VALUES (1,1),(2,1),(3,1),(4,2);
GO

-- Bảng brands
IF OBJECT_ID('brands', 'U') IS NOT NULL DROP TABLE brands;
CREATE TABLE brands (
    brand_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    brand_name NVARCHAR(255) NOT NULL,
    brand_image NVARCHAR(255) NULL,
    description NVARCHAR(1000) NULL,
    origin NVARCHAR(255) NULL,
    status BIT DEFAULT 1
);

INSERT INTO brands(brand_name, brand_image, description, origin, status) 
VALUES 
(N'Maybelline', 'maybelline.jpg', N'Thương hiệu mỹ phẩm nổi tiếng từ Mỹ', N'Mỹ', 1),
(N'Innisfree', 'innisfree.jpg', N'Mỹ phẩm thiên nhiên từ đảo Jeju', N'Hàn Quốc', 1),
(N'L''Oreal', 'loreal.jpg', N'Tập đoàn mỹ phẩm hàng đầu thế giới', N'Pháp', 1),
(N'Nivea', 'nivea.jpg', N'Thương hiệu dưỡng da lâu đời từ Đức', N'Đức', 1);
GO

-- Bảng products 
IF OBJECT_ID('products', 'U') IS NOT NULL DROP TABLE products;
CREATE TABLE products (
    product_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    description NVARCHAR(1000),
    discount INT NOT NULL,
    entered_date DATETIME,
    price FLOAT NOT NULL,
    product_image NVARCHAR(255),
    product_name NVARCHAR(255),
    quantity INT NOT NULL,
    status BIT NULL,
    category_id BIGINT NULL,
    brand_id BIGINT NULL,
    manufacture_date DATE NULL,
    expiry_date DATE NULL,
    favorite BIT NOT NULL,
    CONSTRAINT FK_products_categories FOREIGN KEY(category_id) REFERENCES categories(category_id),
    CONSTRAINT FK_products_brands FOREIGN KEY(brand_id) REFERENCES brands(brand_id)
);

INSERT INTO products(description, discount, entered_date, price, product_image, product_name, quantity, status, category_id, brand_id, manufacture_date, expiry_date, favorite)
VALUES
(N'Son môi cao cấp, giữ màu lâu, không chì', 10, '2025-09-04', 250000, 'son.jpg', N'Son đỏ Ruby', 100, 1, 1, 1, '2025-01-15', '2028-01-15', 0),
(N'Kem dưỡng trắng da ban đêm, an toàn cho mọi loại da', 5, '2025-09-04', 350000, 'kem.jpg', N'Kem dưỡng ban đêm', 50, 1, 2, 2, '2025-03-20', '2027-03-20', 0),
(N'Nước hoa hương hoa hồng sang trọng, lưu hương 12h', 0, '2025-09-04', 1200000, 'nuochoa.jpg', N'Nước hoa Rose', 30, 1, 3, 3, '2025-05-10', '2030-05-10', 0);
GO

-- Bảng orders
IF OBJECT_ID('orders', 'U') IS NOT NULL DROP TABLE orders;
CREATE TABLE orders (
    order_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    address NVARCHAR(255),
    amount FLOAT,
    order_date DATETIME,
    phone NVARCHAR(50),
    status INT NOT NULL,
    user_id BIGINT NULL,
    CONSTRAINT FK_orders_user FOREIGN KEY(user_id) REFERENCES [user](user_id)
);

INSERT INTO orders(address, amount, order_date, phone, status, user_id)
VALUES
(N'Hà Nội', 500000, '2025-09-05', '0911111111', 1, 1),
(N'Đà Nẵng', 1200000, '2025-09-05', '0922222222', 2, 2);
GO

-- Bảng order_details
IF OBJECT_ID('order_details', 'U') IS NOT NULL DROP TABLE order_details;
CREATE TABLE order_details (
    order_detail_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    price FLOAT,
    quantity INT NOT NULL,
    order_id BIGINT NULL,
    product_id BIGINT NULL,
    CONSTRAINT FK_orderdetails_orders FOREIGN KEY(order_id) REFERENCES orders(order_id),
    CONSTRAINT FK_orderdetails_products FOREIGN KEY(product_id) REFERENCES products(product_id)
);

INSERT INTO order_details(price, quantity, order_id, product_id)
VALUES
(250000, 2, 1, 1),  -- 2 x Son đỏ Ruby
(1200000, 1, 2, 3); -- 1 x Nước hoa Rose
GO

-- Bảng comments
IF OBJECT_ID('comments', 'U') IS NOT NULL DROP TABLE comments;
CREATE TABLE comments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    content NVARCHAR(255),
    rate_date DATETIME,
    rating FLOAT,
    order_detail_id BIGINT,
    product_id BIGINT,
    user_id BIGINT,
    CONSTRAINT FK_comments_orderdetail FOREIGN KEY(order_detail_id) REFERENCES order_details(order_detail_id),
    CONSTRAINT FK_comments_product FOREIGN KEY(product_id) REFERENCES products(product_id),
    CONSTRAINT FK_comments_user FOREIGN KEY(user_id) REFERENCES [user](user_id)
);

INSERT INTO comments(content, rate_date, rating, order_detail_id, product_id, user_id)
VALUES
(N'Son màu đẹp, lâu trôi', '2025-09-06', 5, 1, 1, 1),
(N'Nước hoa mùi thơm dễ chịu, sang trọng', '2025-09-06', 4, 2, 3, 2);
GO

-- Bảng favorites
IF OBJECT_ID('favorites', 'U') IS NOT NULL DROP TABLE favorites;
CREATE TABLE favorites (
    favorite_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id BIGINT,
    user_id BIGINT,
    CONSTRAINT FK_favorites_product FOREIGN KEY(product_id) REFERENCES products(product_id),
    CONSTRAINT FK_favorites_user FOREIGN KEY(user_id) REFERENCES [user](user_id)
);

INSERT INTO favorites(product_id, user_id)
VALUES
(1, 1), -- user 1 thích Son đỏ Ruby
(3, 2); -- user 2 thích Nước hoa Rose
GO
