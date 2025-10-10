-- Script tạo bảng cart_items cho OneShop
-- Chạy script này trước khi start ứng dụng

USE WebMyPham;
GO

-- Xóa bảng cũ nếu có
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[cart_items]') AND type in (N'U'))
DROP TABLE [dbo].[cart_items];
GO

-- Tạo bảng cart_items
CREATE TABLE cart_items (
    cart_item_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    created_date DATETIME2 DEFAULT GETDATE(),
    updated_date DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES [user](user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    UNIQUE(user_id, product_id)
);

-- Tạo indexes
CREATE INDEX IX_cart_items_user_id ON cart_items(user_id);
CREATE INDEX IX_cart_items_product_id ON cart_items(product_id);

-- Thêm comment
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Table to store individual cart items for each user', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'cart_items';

PRINT '✅ Bảng cart_items đã được tạo thành công!';
GO
