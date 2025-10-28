USE WebMyPham;
GO

UPDATE shops
SET shop_banner = 'banner_1.jpg,banner_2.jpg,banner_3.jpg'
WHERE shop_slug = 'my-pham-an-nguyen';


UPDATE shops
SET shop_banner = 'banner_4.jpg,banner_5.jpg,banner_6.jpg'
WHERE shop_id = 2;
