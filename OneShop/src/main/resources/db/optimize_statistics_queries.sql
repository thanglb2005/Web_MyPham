-- Script để tối ưu hiệu suất truy vấn thống kê doanh thu

-- Thêm index cho bảng orders
CREATE INDEX IF NOT EXISTS idx_orders_order_date ON orders (order_date);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders (status);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders (user_id);

-- Thêm index cho bảng order_details
CREATE INDEX IF NOT EXISTS idx_order_details_order_id ON order_details (order_id);
CREATE INDEX IF NOT EXISTS idx_order_details_product_id ON order_details (product_id);

-- Thêm index cho bảng products
CREATE INDEX IF NOT EXISTS idx_products_category_id ON products (category_id);
CREATE INDEX IF NOT EXISTS idx_products_brand_id ON products (brand_id);

-- Thêm index cho trường hợp cụ thể - Truy vấn theo tháng
CREATE INDEX IF NOT EXISTS idx_orders_year_month ON orders (EXTRACT(YEAR FROM order_date), EXTRACT(MONTH FROM order_date));

-- Thêm index cho trường hợp cụ thể - Truy vấn theo quý
CREATE INDEX IF NOT EXISTS idx_orders_year_quarter ON orders (
    EXTRACT(YEAR FROM order_date),
    CASE 
        WHEN EXTRACT(MONTH FROM order_date) BETWEEN 1 AND 3 THEN 1
        WHEN EXTRACT(MONTH FROM order_date) BETWEEN 4 AND 6 THEN 2
        WHEN EXTRACT(MONTH FROM order_date) BETWEEN 7 AND 9 THEN 3
        WHEN EXTRACT(MONTH FROM order_date) BETWEEN 10 AND 12 THEN 4
    END
);