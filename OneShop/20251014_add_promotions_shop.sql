/* Link promotions to shops and creators; align types; add indexes (SQL Server) */

-- 1) Add missing columns if not present
IF COL_LENGTH('dbo.promotions','shop_id') IS NULL
BEGIN
  ALTER TABLE dbo.promotions ADD shop_id BIGINT NULL;
  PRINT 'Added shop_id column to promotions table';
END;

IF COL_LENGTH('dbo.promotions','created_by') IS NULL
BEGIN
  ALTER TABLE dbo.promotions ADD created_by BIGINT NULL;
  PRINT 'Added created_by column to promotions table';
END;

-- 2) Add foreign keys if not present
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_promotions_shop')
BEGIN
  ALTER TABLE dbo.promotions
    ADD CONSTRAINT FK_promotions_shop
    FOREIGN KEY (shop_id) REFERENCES dbo.shops(shop_id);
  PRINT 'Added FK_promotions_shop foreign key constraint';
END;

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_promotions_created_by')
BEGIN
  ALTER TABLE dbo.promotions
    ADD CONSTRAINT FK_promotions_created_by
    FOREIGN KEY (created_by) REFERENCES dbo.[user](user_id);
  PRINT 'Added FK_promotions_created_by foreign key constraint';
END;

-- 3) Create index for filtering by shop
IF NOT EXISTS (
  SELECT 1 FROM sys.indexes WHERE name = 'IX_promotions_shop_id' AND object_id = OBJECT_ID('dbo.promotions')
)
BEGIN
  CREATE INDEX IX_promotions_shop_id ON dbo.promotions(shop_id);
  PRINT 'Added IX_promotions_shop_id index';
END;

-- 4) Kiểm tra dữ liệu hiện có và constraint cũ
PRINT 'Checking current promotion_type values...';
SELECT DISTINCT promotion_type FROM dbo.promotions ORDER BY promotion_type;

-- 5) Drop TẤT CẢ CHECK constraint cũ trên promotion_type (làm sạch trước)
DECLARE @chk sysname;
DECLARE @sql nvarchar(400);

-- Tìm và drop constraint cũ
SELECT @chk = c.name
FROM sys.check_constraints c
JOIN sys.tables t        ON c.parent_object_id = t.object_id
JOIN sys.columns col     ON col.object_id = t.object_id AND c.parent_column_id = col.column_id
WHERE t.schema_id = SCHEMA_ID('dbo')
  AND t.name = 'promotions'
  AND col.name = 'promotion_type';

IF @chk IS NOT NULL
BEGIN
  SET @sql = N'ALTER TABLE dbo.promotions DROP CONSTRAINT ' + QUOTENAME(@chk) + N';';
  EXEC sp_executesql @sql;
  PRINT 'Dropped existing promotion_type constraint: ' + @chk;
END;

-- 6) Chuẩn hóa dữ liệu promotion_type theo enum mới
PRINT 'Cleaning promotion_type data...';
UPDATE dbo.promotions
SET promotion_type = CASE UPPER(LTRIM(RTRIM(promotion_type)))
    WHEN 'PRODUCT_PERCENTAGE' THEN 'PERCENTAGE'
    WHEN 'PERCENT'            THEN 'PERCENTAGE'
    WHEN 'PERCENTAGE'         THEN 'PERCENTAGE'
    WHEN 'PERCENT_DISCOUNT'   THEN 'PERCENTAGE'
    
    WHEN 'FIXED_AMOUNT'       THEN 'FIXED_AMOUNT'
    WHEN 'FIXED'              THEN 'FIXED_AMOUNT'
    WHEN 'AMOUNT'             THEN 'FIXED_AMOUNT'
    
    WHEN 'SHIPPING_DISCOUNT'  THEN 'FREE_SHIPPING'
    WHEN 'FREE_SHIP'          THEN 'FREE_SHIPPING'
    WHEN 'FREE SHIPPING'      THEN 'FREE_SHIPPING'
    WHEN 'FREESHIP'           THEN 'FREE_SHIPPING'
    WHEN 'FREE_SHIPPING'      THEN 'FREE_SHIPPING'
    
    WHEN 'BUY_X_GET_Y'        THEN 'BUY_X_GET_Y'
    WHEN 'BUY_GET'            THEN 'BUY_X_GET_Y'
    
    ELSE 'PERCENTAGE'  -- Default cho các giá trị lạ
END;

-- Kiểm tra xem còn giá trị nào không hợp lệ không
DECLARE @invalid_count INT;
SELECT @invalid_count = COUNT(*)
FROM dbo.promotions
WHERE UPPER(promotion_type) NOT IN ('PERCENTAGE','FIXED_AMOUNT','FREE_SHIPPING','BUY_X_GET_Y')
   OR promotion_type IS NULL;

IF @invalid_count > 0
BEGIN
  PRINT 'Warning: Found ' + CAST(@invalid_count AS VARCHAR(10)) + ' invalid promotion_type values';
  -- Set default cho các giá trị lạ
  UPDATE dbo.promotions
  SET promotion_type = 'PERCENTAGE'
  WHERE promotion_type IS NULL
     OR UPPER(promotion_type) NOT IN ('PERCENTAGE','FIXED_AMOUNT','FREE_SHIPPING','BUY_X_GET_Y');
END;

PRINT 'Data cleaning completed';

-- 7) Tạo lại CHECK constraint khi dữ liệu đã sạch
ALTER TABLE dbo.promotions WITH CHECK
ADD CONSTRAINT CHK_promotions_type
CHECK (promotion_type IN ('PERCENTAGE','FREE_SHIPPING','FIXED_AMOUNT','BUY_X_GET_Y'));

PRINT 'Added new CHK_promotions_type constraint';

-- 8) Kiểm tra Foreign Key constraints
PRINT 'Checking foreign key constraints...';

-- Kiểm tra shop_id references
DECLARE @orphan_shops INT;
SELECT @orphan_shops = COUNT(*)
FROM dbo.promotions p
LEFT JOIN dbo.shops s ON s.shop_id = p.shop_id
WHERE p.shop_id IS NOT NULL AND s.shop_id IS NULL;

IF @orphan_shops > 0
BEGIN
  PRINT 'Warning: Found ' + CAST(@orphan_shops AS VARCHAR(10)) + ' promotions with invalid shop_id';
  -- Uncomment và set shop_id mặc định nếu cần:
  -- UPDATE dbo.promotions SET shop_id = 1 WHERE shop_id IS NOT NULL AND shop_id NOT IN (SELECT shop_id FROM dbo.shops);
END;

-- Kiểm tra created_by references  
DECLARE @orphan_users INT;
SELECT @orphan_users = COUNT(*)
FROM dbo.promotions p
LEFT JOIN dbo.[user] u ON u.user_id = p.created_by
WHERE p.created_by IS NOT NULL AND u.user_id IS NULL;

IF @orphan_users > 0
BEGIN
  PRINT 'Warning: Found ' + CAST(@orphan_users AS VARCHAR(10)) + ' promotions with invalid created_by';
  -- Uncomment và set user_id mặc định nếu cần:
  -- UPDATE dbo.promotions SET created_by = 1 WHERE created_by IS NOT NULL AND created_by NOT IN (SELECT user_id FROM dbo.[user]);
END;

-- Optional: set all legacy promotions to a default shop (uncomment and set id)
-- UPDATE dbo.promotions SET shop_id = 1 WHERE shop_id IS NULL;

PRINT 'Migration 20251014_add_promotions_shop completed successfully!';


