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

PRINT 'Promotions table and related objects created successfully!';
