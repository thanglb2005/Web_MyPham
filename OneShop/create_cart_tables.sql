-- SQL script to create CartItem table for OneShop
-- This script creates the necessary table to store shopping cart data in database

-- Create cart_items table (simplified - directly linked to user)
CREATE TABLE cart_items (
    cart_item_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    created_date DATETIME2 DEFAULT GETDATE(),
    updated_date DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES [user](userId) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(productId) ON DELETE CASCADE,
    UNIQUE(user_id, product_id) -- Prevent duplicate products for same user
);

-- Create indexes for better performance
CREATE INDEX IX_cart_items_user_id ON cart_items(user_id);
CREATE INDEX IX_cart_items_product_id ON cart_items(product_id);

-- Add comments for documentation
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Table to store individual cart items for each user', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'cart_items';

-- Sample data (optional - for testing)
-- Note: Uncomment these lines if you want to insert sample data for testing

/*
-- Insert sample cart items for user with ID 1
INSERT INTO cart_items (user_id, product_id, quantity, unit_price, total_price) 
VALUES 
    (1, 1, 2, 150000.00, 300000.00),
    (1, 2, 1, 200000.00, 200000.00);
*/
