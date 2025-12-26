USE WebMyPham;
GO

-- ===============================
-- TABLE: refunds
-- ===============================
-- Kiểm tra xem bảng đã tồn tại chưa
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'refunds' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.refunds (
        refund_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        order_id BIGINT NOT NULL,
        user_id BIGINT NOT NULL,
        refund_amount DECIMAL(18,2) NOT NULL CHECK (refund_amount > 0),
        refund_method NVARCHAR(20) NOT NULL CHECK (refund_method IN ('ONEXU', 'BANK_TRANSFER')),
        refund_status NVARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (refund_status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
        
        -- Thông tin OneXu (nếu chọn ONEXU)
        one_xu_transaction_id BIGINT NULL,
        
        -- Thông tin ngân hàng (nếu chọn BANK_TRANSFER)
        bank_name NVARCHAR(255) NULL,
        bank_account_number NVARCHAR(50) NULL,
        account_holder_name NVARCHAR(255) NULL,
        bank_branch NVARCHAR(255) NULL,
        contact_phone NVARCHAR(20) NULL,
        
        -- Metadata & Audit
        created_at DATETIME2 DEFAULT GETDATE(),
        processed_at DATETIME2 NULL,
        completed_at DATETIME2 NULL,
        failed_at DATETIME2 NULL,
        failure_reason NVARCHAR(500) NULL,
        notes NVARCHAR(1000) NULL,
        
        -- Audit fields
        created_by NVARCHAR(100) NULL,  -- 'VENDOR' hoặc user_id
        updated_by NVARCHAR(100) NULL,
        updated_at DATETIME2 DEFAULT GETDATE(),
        
        CONSTRAINT FK_refunds_order FOREIGN KEY(order_id) REFERENCES dbo.orders(order_id),
        CONSTRAINT FK_refunds_user FOREIGN KEY(user_id) REFERENCES dbo.[user](user_id),
        CONSTRAINT FK_refunds_one_xu_transaction FOREIGN KEY(one_xu_transaction_id) REFERENCES dbo.one_xu_transactions(transaction_id),
        
        -- Mỗi order chỉ có 1 refund record
        CONSTRAINT UK_refunds_order UNIQUE(order_id)
    );
    PRINT 'Table dbo.refunds created successfully';
END
ELSE
BEGIN
    PRINT 'Table dbo.refunds already exists';
END
GO

-- Indexes for performance (chỉ tạo nếu chưa tồn tại)
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_refunds_order' AND object_id = OBJECT_ID('dbo.refunds'))
BEGIN
    CREATE INDEX IX_refunds_order ON dbo.refunds(order_id);
    PRINT 'Index IX_refunds_order created';
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_refunds_user' AND object_id = OBJECT_ID('dbo.refunds'))
BEGIN
    CREATE INDEX IX_refunds_user ON dbo.refunds(user_id);
    PRINT 'Index IX_refunds_user created';
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_refunds_status' AND object_id = OBJECT_ID('dbo.refunds'))
BEGIN
    CREATE INDEX IX_refunds_status ON dbo.refunds(refund_status);
    PRINT 'Index IX_refunds_status created';
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_refunds_created_at' AND object_id = OBJECT_ID('dbo.refunds'))
BEGIN
    CREATE INDEX IX_refunds_created_at ON dbo.refunds(created_at);
    PRINT 'Index IX_refunds_created_at created';
END
GO

-- Trigger để tự động update updated_at (chỉ tạo nếu chưa tồn tại)
IF NOT EXISTS (SELECT * FROM sys.triggers WHERE name = 'TR_refunds_update_timestamp' AND parent_id = OBJECT_ID('dbo.refunds'))
BEGIN
    EXEC('
    CREATE TRIGGER TR_refunds_update_timestamp
    ON dbo.refunds
    AFTER UPDATE
    AS
    BEGIN
        SET NOCOUNT ON;
        UPDATE dbo.refunds 
        SET updated_at = GETDATE()
        WHERE refund_id IN (SELECT refund_id FROM inserted);
    END;
    ');
    PRINT 'Trigger TR_refunds_update_timestamp created';
END
ELSE
BEGIN
    PRINT 'Trigger TR_refunds_update_timestamp already exists';
END
GO

-- ===============================
-- TABLE: refund_audit_log (Lịch sử audit chi tiết)
-- ===============================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'refund_audit_log' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.refund_audit_log (
        log_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        refund_id BIGINT NOT NULL,
        action NVARCHAR(50) NOT NULL,  -- CREATED, STATUS_CHANGED, METHOD_SELECTED, COMPLETED, FAILED
        old_status NVARCHAR(20) NULL,
        new_status NVARCHAR(20) NULL,
        old_method NVARCHAR(20) NULL,
        new_method NVARCHAR(20) NULL,
        performed_by NVARCHAR(100) NOT NULL,  -- user_id hoặc 'SYSTEM'
        performed_by_type NVARCHAR(20) NOT NULL,  -- 'USER', 'VENDOR', 'SYSTEM'
        description NVARCHAR(500) NULL,
        metadata NVARCHAR(MAX) NULL,  -- JSON string để lưu thông tin chi tiết
        created_at DATETIME2 DEFAULT GETDATE(),
        
        CONSTRAINT FK_refund_audit_refund FOREIGN KEY(refund_id) REFERENCES dbo.refunds(refund_id) ON DELETE CASCADE
    );
    PRINT 'Table dbo.refund_audit_log created successfully';
END
ELSE
BEGIN
    PRINT 'Table dbo.refund_audit_log already exists';
END
GO

-- Indexes cho refund_audit_log
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_refund_audit_refund' AND object_id = OBJECT_ID('dbo.refund_audit_log'))
BEGIN
    CREATE INDEX IX_refund_audit_refund ON dbo.refund_audit_log(refund_id);
    PRINT 'Index IX_refund_audit_refund created';
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_refund_audit_created' AND object_id = OBJECT_ID('dbo.refund_audit_log'))
BEGIN
    CREATE INDEX IX_refund_audit_created ON dbo.refund_audit_log(created_at);
    PRINT 'Index IX_refund_audit_created created';
END
GO

-- =====================================================
-- Script để thêm trạng thái "RETURN_REQUESTED" vào bảng orders
-- =====================================================

-- Bước 1: Tìm và drop constraint cũ (nếu tồn tại)
DECLARE @constraintName NVARCHAR(255);
SELECT @constraintName = name
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID('dbo.orders') 
AND name LIKE '%status%';

IF @constraintName IS NOT NULL
BEGIN
    DECLARE @sqlDrop NVARCHAR(MAX) = 'ALTER TABLE dbo.orders DROP CONSTRAINT ' + QUOTENAME(@constraintName);
    EXEC sp_executesql @sqlDrop;
    PRINT 'Dropped existing CHECK constraint: ' + @constraintName;
END
ELSE
BEGIN
    PRINT 'No existing CHECK constraint found on dbo.orders.status';
END
GO

-- Bước 2: Thêm constraint mới với trạng thái RETURN_REQUESTED và tất cả các status khác
-- Bao gồm tất cả status từ DB.sql: PENDING, NEW, CONFIRMED, SHIPPING, DELIVERED, OVERDUE, CANCELLED, RETURNED
-- Và thêm RETURN_REQUESTED
IF NOT EXISTS (SELECT * FROM sys.check_constraints WHERE parent_object_id = OBJECT_ID('dbo.orders') AND name = 'CK_orders_status')
BEGIN
    ALTER TABLE dbo.orders
    ADD CONSTRAINT CK_orders_status 
    CHECK (status IN ('PENDING', 'NEW', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'OVERDUE', 'CANCELLED', 'RETURNED', 'RETURN_REQUESTED'));
    PRINT 'Successfully added CHECK constraint CK_orders_status with RETURN_REQUESTED status';
END
ELSE
BEGIN
    PRINT 'Constraint CK_orders_status already exists';
END
GO

-- Bước 3: Kiểm tra constraint đã được tạo thành công
SELECT 
    name AS constraint_name,
    definition
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID('dbo.orders')
AND name = 'CK_orders_status';
GO

-- =====================================================================
-- END OF REFUND SYSTEM SCHEMA
-- =====================================================================
PRINT 'Refund system schema setup completed successfully!';
GO
