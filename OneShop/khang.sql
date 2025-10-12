
USE WebMyPham;
GO



-- =====================================================
-- PHẦN 1: CẬP NHẬT SCHEMA CHO BẢNG ORDERS
-- =====================================================

-- Thêm cột pickup_address (Địa chỉ lấy hàng)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'pickup_address')
BEGIN
    ALTER TABLE orders ADD pickup_address NVARCHAR(500) NULL;
END
GO

-- Thêm cột package_type (Loại hàng)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'package_type')
BEGIN
    ALTER TABLE orders ADD package_type NVARCHAR(100) NULL;
END
GO

-- Thêm cột weight (Khối lượng)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'weight')
BEGIN
    ALTER TABLE orders ADD weight FLOAT NULL;
END
GO


-- =====================================================
-- PHẦN 2: TẠO BẢNG SHOP_SHIPPERS (QUAN HỆ MANY-TO-MANY)
-- =====================================================

-- Kiểm tra và tạo bảng shop_shippers
IF OBJECT_ID('dbo.shop_shippers', 'U') IS NOT NULL
BEGIN
    DROP TABLE dbo.shop_shippers;
END

CREATE TABLE dbo.shop_shippers (
    shop_id BIGINT NOT NULL,
    shipper_id BIGINT NOT NULL,
    assigned_date DATETIME DEFAULT GETDATE(),
    status BIT DEFAULT 1,
    notes NVARCHAR(500),
    
    CONSTRAINT PK_shop_shippers PRIMARY KEY (shop_id, shipper_id),
    CONSTRAINT FK_shop_shippers_shop FOREIGN KEY (shop_id) REFERENCES dbo.shops(shop_id) ON DELETE CASCADE,
    CONSTRAINT FK_shop_shippers_user FOREIGN KEY (shipper_id) REFERENCES dbo.[user](user_id) ON DELETE CASCADE
);
GO

-- =====================================================
-- PHẦN 3: THÊM DỮ LIỆU MẪU
-- =====================================================

-- Lấy ID của shipper mẫu
DECLARE @shipperId BIGINT;
SELECT TOP 1 @shipperId = u.user_id 
FROM [user] u
INNER JOIN users_roles ur ON u.user_id = ur.user_id
INNER JOIN role r ON ur.role_id = r.id
WHERE r.name = 'ROLE_SHIPPER';

-- Gán shipper cho 2 shop đầu tiên (nếu có)
IF @shipperId IS NOT NULL
BEGIN
    INSERT INTO shop_shippers (shop_id, shipper_id, assigned_date, status, notes)
    SELECT TOP 2 
        s.shop_id, 
        @shipperId, 
        GETDATE(), 
        1,
        N'Shipper mặc định cho shop ' + s.shop_name
    FROM shops s
    WHERE s.status = 'ACTIVE'
    AND NOT EXISTS (
        SELECT 1 FROM shop_shippers ss 
        WHERE ss.shop_id = s.shop_id AND ss.shipper_id = @shipperId
    );
END
GO

-- =====================================================
-- PHẦN 4: TẠO INDEX ĐỂ TỐI ƯU PERFORMANCE
-- =====================================================

-- Index để tìm shipper theo shop
CREATE INDEX idx_shop_shippers_shop 
ON shop_shippers(shop_id, status);

-- Index để tìm shop theo shipper
CREATE INDEX idx_shop_shippers_shipper 
ON shop_shippers(shipper_id, status);
GO

