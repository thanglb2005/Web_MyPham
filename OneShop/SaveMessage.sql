-- Create table to persist chat messages
IF OBJECT_ID('chat_message','U') IS NULL
BEGIN
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
END
GO

-- Helpful index for room timeline queries
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = 'ix_chat_message_room_time' AND object_id = OBJECT_ID('chat_message')
)
BEGIN
    CREATE INDEX ix_chat_message_room_time ON chat_message(room_id, sent_at DESC);
END
GO
