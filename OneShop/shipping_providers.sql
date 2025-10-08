/* ===============================
   TABLE: shipping_providers
   =============================== */
IF OBJECT_ID('dbo.shipping_providers', 'U') IS NOT NULL DROP TABLE dbo.shipping_providers;
CREATE TABLE dbo.shipping_providers (
    provider_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    provider_name NVARCHAR(255) NOT NULL,
    contact_phone NVARCHAR(15),
    contact_email NVARCHAR(255),
    description NVARCHAR(1000),
    website NVARCHAR(255),
    address NVARCHAR(255),
    shipping_fees DECIMAL(18,2),
    delivery_time_range NVARCHAR(255),
    logo NVARCHAR(255),
    status BIT DEFAULT 1
);
INSERT INTO dbo.shipping_providers (provider_name, contact_phone, contact_email, description, website, address, shipping_fees, delivery_time_range, logo, status)
VALUES
(N'Giao Hàng Nhanh', N'1900636677', N'support@ghn.vn', N'Dịch vụ giao hàng nhanh trên toàn quốc', N'https://ghn.vn', N'Hà Nội, Việt Nam', 30000, N'1-3 ngày', N'ghn-logo.png', 1),
(N'Giao Hàng Tiết Kiệm', N'1900636677', N'support@giaohangtietkiem.vn', N'Dịch vụ giao hàng tiết kiệm toàn quốc', N'https://giaohangtietkiem.vn', N'Hồ Chí Minh, Việt Nam', 25000, N'2-4 ngày', N'ghtk-logo.png', 1),
(N'J&T Express', N'19001088', N'cskh@jtexpress.vn', N'Dịch vụ chuyển phát nhanh J&T Express', N'https://jtexpress.vn', N'Hồ Chí Minh, Việt Nam', 28000, N'1-3 ngày', N'jt-express-logo.png', 1),
(N'Viettel Post', N'1900818820', N'cskh@viettelpost.com.vn', N'Dịch vụ chuyển phát Viettel Post', N'https://viettelpost.com.vn', N'Hà Nội, Việt Nam', 27000, N'2-4 ngày', N'viettel-post-logo.png', 1),
(N'Vietnam Post', N'18006422', N'info@vnpost.vn', N'Bưu điện Việt Nam - Vietnam Post', N'https://www.vnpost.vn', N'Hà Nội, Việt Nam', 26000, N'2-5 ngày', N'vnpost-logo.png', 1);
GO