-- Migration script to add shop_id and created_by columns to promotions table
-- Date: 2025-10-16
-- Purpose: Support vendor-specific promotions

PRINT 'Starting promotions table migration...';

-- Step 1: Add missing columns if they don't exist
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'promotions' AND COLUMN_NAME = 'shop_id')
BEGIN
    PRINT 'Adding shop_id column...';
    ALTER TABLE promotions ADD shop_id BIGINT;
END
ELSE
BEGIN
    PRINT 'shop_id column already exists.';
END

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'promotions' AND COLUMN_NAME = 'created_by')
BEGIN
    PRINT 'Adding created_by column...';
    ALTER TABLE promotions ADD created_by BIGINT;
END
ELSE
BEGIN
    PRINT 'created_by column already exists.';
END

-- Step 2: Add foreign key constraints if they don't exist
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME = 'promotions' AND COLUMN_NAME = 'shop_id' AND CONSTRAINT_NAME LIKE 'FK_%')
BEGIN
    PRINT 'Adding foreign key constraint for shop_id...';
    ALTER TABLE promotions ADD CONSTRAINT FK_promotions_shop FOREIGN KEY (shop_id) REFERENCES shops(shop_id);
END
ELSE
BEGIN
    PRINT 'Foreign key constraint for shop_id already exists.';
END

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME = 'promotions' AND COLUMN_NAME = 'created_by' AND CONSTRAINT_NAME LIKE 'FK_%')
BEGIN
    PRINT 'Adding foreign key constraint for created_by...';
    ALTER TABLE promotions ADD CONSTRAINT FK_promotions_created_by FOREIGN KEY (created_by) REFERENCES users(user_id);
END
ELSE
BEGIN
    PRINT 'Foreign key constraint for created_by already exists.';
END

-- Step 3: Create index for better performance
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_promotions_shop_id')
BEGIN
    PRINT 'Creating index on shop_id...';
    CREATE INDEX IX_promotions_shop_id ON promotions(shop_id);
END
ELSE
BEGIN
    PRINT 'Index on shop_id already exists.';
END

-- Step 4: Check current promotion_type values
PRINT 'Checking current promotion_type values...';
SELECT DISTINCT promotion_type, COUNT(*) as count FROM promotions GROUP BY promotion_type;

-- Step 5: Drop existing CHECK constraint on promotion_type if it exists
DECLARE @constraint_name NVARCHAR(128);
SELECT @constraint_name = name 
FROM sys.check_constraints 
WHERE parent_object_id = OBJECT_ID('promotions') 
AND definition LIKE '%promotion_type%';

IF @constraint_name IS NOT NULL
BEGIN
    PRINT 'Dropping existing CHECK constraint: ' + @constraint_name;
    EXEC('ALTER TABLE promotions DROP CONSTRAINT ' + @constraint_name);
END

-- Step 6: Clean and standardize promotion_type data
PRINT 'Cleaning and standardizing promotion_type data...';
UPDATE promotions 
SET promotion_type = CASE 
    WHEN promotion_type IN ('PERCENTAGE', 'percentage', 'Percentage') THEN 'PERCENTAGE'
    WHEN promotion_type IN ('FIXED_AMOUNT', 'fixed_amount', 'Fixed_Amount', 'FIXED') THEN 'FIXED_AMOUNT'
    WHEN promotion_type IN ('FREE_SHIPPING', 'free_shipping', 'Free_Shipping', 'FREE') THEN 'FREE_SHIPPING'
    WHEN promotion_type IN ('BUY_X_GET_Y', 'buy_x_get_y', 'Buy_X_Get_Y', 'BOGO') THEN 'BUY_X_GET_Y'
    ELSE 'PERCENTAGE' -- Default fallback
END;

-- Step 7: Recreate CHECK constraint with correct enum values
PRINT 'Creating new CHECK constraint for promotion_type...';
ALTER TABLE promotions 
ADD CONSTRAINT CK_promotions_promotion_type 
CHECK (promotion_type IN ('PERCENTAGE', 'FIXED_AMOUNT', 'FREE_SHIPPING', 'BUY_X_GET_Y'));

-- Step 8: Check for orphaned shop_id references
PRINT 'Checking for orphaned shop_id references...';
SELECT COUNT(*) as orphaned_shop_refs 
FROM promotions p 
LEFT JOIN shops s ON p.shop_id = s.shop_id 
WHERE p.shop_id IS NOT NULL AND s.shop_id IS NULL;

-- Step 9: Check for orphaned created_by references
PRINT 'Checking for orphaned created_by references...';
SELECT COUNT(*) as orphaned_user_refs 
FROM promotions p 
LEFT JOIN users u ON p.created_by = u.user_id 
WHERE p.created_by IS NOT NULL AND u.user_id IS NULL;

-- Step 10: Final verification
PRINT 'Final verification...';
SELECT 
    COUNT(*) as total_promotions,
    COUNT(shop_id) as promotions_with_shop,
    COUNT(created_by) as promotions_with_creator
FROM promotions;

PRINT 'Promotions table migration completed successfully!';
