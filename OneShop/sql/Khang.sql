-- Migration: Thêm transaction type REVIEW_REWARD vào bảng one_xu_transactions
-- Date: 2025-01-16
-- Description: Cho phép người dùng nhận 300 xu khi đánh giá sản phẩm lần đầu

-- Cập nhật CHECK constraint để thêm REVIEW_REWARD
-- Xóa constraint cũ nếu tồn tại
IF EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'CK_one_xu_tr_trans_29221CFB')
    ALTER TABLE dbo.one_xu_transactions DROP CONSTRAINT CK_one_xu_tr_trans_29221CFB;

IF EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'CK_one_xu_transactions_type')
    ALTER TABLE dbo.one_xu_transactions DROP CONSTRAINT CK_one_xu_transactions_type;

-- Thêm constraint mới
ALTER TABLE dbo.one_xu_transactions 
ADD CONSTRAINT CK_one_xu_transactions_type 
CHECK (transaction_type IN ('CHECKIN', 'ORDER_REWARD', 'PURCHASE', 'REFUND', 'REVIEW_REWARD'));

-- Thêm comment để giải thích transaction type mới
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Transaction types: CHECKIN (check-in hàng ngày), ORDER_REWARD (thưởng từ đơn hàng), PURCHASE (mua sắm), REFUND (hoàn xu), REVIEW_REWARD (thưởng từ đánh giá sản phẩm lần đầu)', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'one_xu_transactions', 
    @level2type = N'CONSTRAINT', @level2name = N'CK_one_xu_transactions_type';

PRINT 'Đã cập nhật transaction type REVIEW_REWARD thành công!';
