-- =====================================================
-- FLASH SALE DATABASE SCHEMA
-- Run this script in SQL Server Management Studio
-- =====================================================

USE WebMyPham;
GO

-- =====================================================
-- TABLE: flash_sales
-- Lưu thông tin session flash sale (tương tự khung giờ của Shopee)
-- =====================================================
CREATE TABLE dbo.flash_sales (
    flash_sale_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    sale_name NVARCHAR(200) NOT NULL,
    description NVARCHAR(1000),
    start_time DATETIME2 NOT NULL,  -- Thời gian bắt đầu (ví dụ: 2025-10-20 08:00:00)
    end_time DATETIME2 NOT NULL,    -- Thời gian kết thúc (ví dụ: 2025-10-20 10:00:00)
    status NVARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'SCHEDULED', 'ACTIVE', 'ENDED', 'CANCELLED')),
    banner_image NVARCHAR(255),
    created_by BIGINT NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT (GETDATE()),
    updated_at DATETIME2 NOT NULL DEFAULT (GETDATE()),
    
    -- Foreign Key Constraints
    CONSTRAINT FK_flash_sales_created_by FOREIGN KEY(created_by) REFERENCES dbo.[user](user_id),
    
    -- Constraints
    CONSTRAINT CHK_flash_sales_dates CHECK (end_time > start_time)
);
GO

-- =====================================================
-- TABLE: flash_sale_products
-- Lưu thông tin sản phẩm trong flash sale: giá flash sale, số lượng giới hạn, số lượng đã bán
-- =====================================================
CREATE TABLE dbo.flash_sale_products (
    flash_sale_product_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    flash_sale_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    flash_sale_price DECIMAL(18,2) NOT NULL CHECK (flash_sale_price > 0),  -- Giá flash sale
    original_price DECIMAL(18,2) NOT NULL,  -- Giá gốc (lưu lại để so sánh)
    quantity_limit INT NOT NULL CHECK (quantity_limit > 0),  -- Số lượng tối đa trong flash sale
    sold_quantity INT NOT NULL DEFAULT 0 CHECK (sold_quantity >= 0),  -- Số lượng đã bán
    max_quantity_per_user INT NOT NULL DEFAULT 1 CHECK (max_quantity_per_user > 0),  -- Giới hạn mua mỗi user
    display_order INT DEFAULT 0,  -- Thứ tự hiển thị
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT (GETDATE()),
    
    -- Foreign Key Constraints
    CONSTRAINT FK_flash_sale_products_flash_sale FOREIGN KEY(flash_sale_id) REFERENCES dbo.flash_sales(flash_sale_id) ON DELETE CASCADE,
    CONSTRAINT FK_flash_sale_products_product FOREIGN KEY(product_id) REFERENCES dbo.products(product_id),
    
    -- Constraints
    CONSTRAINT UK_flash_sale_products UNIQUE(flash_sale_id, product_id),
    CONSTRAINT CHK_flash_sale_products_sold CHECK (sold_quantity <= quantity_limit),
    CONSTRAINT CHK_flash_sale_products_price CHECK (flash_sale_price < original_price)
);
GO

-- =====================================================
-- TABLE: flash_sale_orders
-- Tracking đơn hàng flash sale để kiểm soát số lượng đã bán và giới hạn mua mỗi user
-- =====================================================
CREATE TABLE dbo.flash_sale_orders (
    flash_sale_order_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    flash_sale_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    flash_sale_price DECIMAL(18,2) NOT NULL CHECK (flash_sale_price > 0),
    total_amount DECIMAL(18,2) NOT NULL CHECK (total_amount > 0),
    purchased_at DATETIME2 NOT NULL DEFAULT (GETDATE()),
    
    -- Foreign Key Constraints
    CONSTRAINT FK_flash_sale_orders_flash_sale FOREIGN KEY(flash_sale_id) REFERENCES dbo.flash_sales(flash_sale_id),
    CONSTRAINT FK_flash_sale_orders_order FOREIGN KEY(order_id) REFERENCES dbo.orders(order_id),
    CONSTRAINT FK_flash_sale_orders_product FOREIGN KEY(product_id) REFERENCES dbo.products(product_id),
    CONSTRAINT FK_flash_sale_orders_user FOREIGN KEY(user_id) REFERENCES dbo.[user](user_id)
);
GO

-- =====================================================
-- CREATE INDEXES FOR BETTER PERFORMANCE
-- =====================================================

-- Indexes for flash_sales table
CREATE INDEX IX_flash_sales_status ON dbo.flash_sales(status);
CREATE INDEX IX_flash_sales_dates ON dbo.flash_sales(start_time, end_time);
CREATE INDEX IX_flash_sales_created_by ON dbo.flash_sales(created_by);
GO

-- Indexes for flash_sale_products table
CREATE INDEX IX_flash_sale_products_flash_sale ON dbo.flash_sale_products(flash_sale_id);
CREATE INDEX IX_flash_sale_products_product ON dbo.flash_sale_products(product_id);
CREATE INDEX IX_flash_sale_products_active ON dbo.flash_sale_products(is_active);
CREATE INDEX IX_flash_sale_products_sold ON dbo.flash_sale_products(sold_quantity, quantity_limit);
GO

-- Indexes for flash_sale_orders table
CREATE INDEX IX_flash_sale_orders_flash_sale ON dbo.flash_sale_orders(flash_sale_id);
CREATE INDEX IX_flash_sale_orders_order ON dbo.flash_sale_orders(order_id);
CREATE INDEX IX_flash_sale_orders_product ON dbo.flash_sale_orders(product_id);
CREATE INDEX IX_flash_sale_orders_user ON dbo.flash_sale_orders(user_id);
CREATE INDEX IX_flash_sale_orders_user_flash_sale ON dbo.flash_sale_orders(user_id, flash_sale_id, product_id);
CREATE INDEX IX_flash_sale_orders_purchased_at ON dbo.flash_sale_orders(purchased_at);
GO

-- =====================================================
-- SAMPLE DATA (Optional - for testing)
-- =====================================================
/*
-- Example: Create a flash sale for tomorrow 8:00 AM - 10:00 AM
INSERT INTO dbo.flash_sales (
    sale_name,
    description,
    start_time,
    end_time,
    status,
    created_by
) VALUES (
    N'Flash Sale Sáng - Giảm Sốc',
    N'Flash sale buổi sáng với nhiều ưu đãi hấp dẫn',
    DATEADD(DAY, 1, CAST(CAST(GETDATE() AS DATE) AS DATETIME2) + CAST('08:00:00' AS TIME)),
    DATEADD(DAY, 1, CAST(CAST(GETDATE() AS DATE) AS DATETIME2) + CAST('10:00:00' AS TIME)),
    'SCHEDULED',
    4  -- Admin user_id
);
GO
*/

PRINT 'Flash Sale database schema created successfully!';
GO

