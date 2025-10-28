# 🛍️ OneShop - Hệ thống bán mỹ phẩm đa nền tảng

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![SQL Server](https://img.shields.io/badge/SQL%20Server-2019-blue.svg)](https://www.microsoft.com/en-us/sql-server)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Tổng quan

**OneShop** là một **nền tảng thương mại điện tử toàn diện** chuyên về mỹ phẩm, được phát triển với công nghệ **Spring Boot** hiện đại và kiến trúc **microservices**. Hệ thống hỗ trợ **đa vai trò người dùng** (Admin, Vendor, Shipper, CSKH, Customer) với giao diện **responsive** và trải nghiệm người dùng tối ưu.

### 🎯 **Điểm nổi bật:**
- 🚀 **Hiệu suất cao**: Xử lý hàng nghìn đơn hàng đồng thời
- 🔒 **Bảo mật enterprise**: JWT + OAuth2 + CSRF Protection
- 🤖 **AI-powered**: Google Gemini AI cho chat hỗ trợ thông minh
- 📱 **Mobile-first**: Responsive design với Bootstrap 5
- ⚡ **Real-time**: WebSocket cho chat và notifications
- 🌐 **Multi-integration**: MoMo, PayOS, Cloudinary, Goong Maps
- 💰 **Gamification**: Hệ thống One Xu với check-in và rewards
- 📊 **Analytics**: Báo cáo và thống kê chi tiết real-time

## ✨ Tính năng chính

### 🛒 **Mua sắm & Quản lý sản phẩm**
- **Danh mục đa dạng**: Son môi, kem dưỡng da, nước hoa, sữa rửa mặt, toner, mặt nạ, kem chống nắng, phấn phủ, tẩy tế bào chết, serum dưỡng da
- **Tìm kiếm thông minh**: Tìm kiếm theo tên sản phẩm, thương hiệu, danh mục
- **Giỏ hàng**: Thêm/xóa sản phẩm, cập nhật số lượng
- **Yêu thích**: Lưu sản phẩm vào danh sách yêu thích
- **Đánh giá sản phẩm**: Hệ thống đánh giá với hình ảnh và video

### 💳 **Thanh toán đa dạng**
- **COD**: Thanh toán khi nhận hàng
- **Chuyển khoản**: Chuyển khoản ngân hàng
- **MoMo**: Thanh toán qua ví điện tử MoMo
- **PayOS**: Tích hợp cổng thanh toán PayOS

### 🚚 **Giao hàng linh hoạt**
- **Giao hỏa tốc**: Giao trong 2-4 giờ (cùng tỉnh, có shipper riêng)
- **Giao tiêu chuẩn**: Giao trong 1-3 ngày qua đơn vị vận chuyển
- **Tự động tính phí**: Phí ship 30k (cùng tỉnh), 50k (khác tỉnh)
- **Voucher giảm ship**: Hệ thống voucher giảm phí vận chuyển

### 🎯 **Hệ thống vai trò**
- **👤 USER**: Khách hàng mua sắm
- **👨‍💼 ADMIN**: Quản trị hệ thống
- **🏪 VENDOR**: Chủ shop bán hàng
- **🚚 SHIPPER**: Nhân viên giao hàng
- **💬 CSKH**: Chăm sóc khách hàng

### 💰 **Hệ thống One Xu**
- **Check-in hàng ngày**: Nhận xu mỗi ngày
- **Thưởng đơn hàng**: 1% giá trị đơn hàng
- **Thưởng đánh giá**: 300 xu cho đánh giá có ảnh/video
- **Sử dụng xu**: Thanh toán một phần đơn hàng

### 🔐 **Bảo mật & Xác thực**
- **JWT + Session**: Hybrid authentication
- **OAuth2**: Đăng nhập qua Facebook, Google
- **BCrypt**: Mã hóa mật khẩu
- **CSRF Protection**: Bảo vệ chống tấn công CSRF

### 💬 **Chat hỗ trợ**
- **WebSocket**: Chat real-time
- **AI Chat**: Tích hợp Google Gemini AI
- **Upload file**: Gửi hình ảnh trong chat
- **Lịch sử chat**: Lưu trữ cuộc trò chuyện

### 🎁 **Khuyến mãi & Voucher**
- **Loại ưu đãi**: Phần trăm, số tiền cố định, miễn phí/giảm phí vận chuyển, mua X tặng Y
- **Điều kiện áp dụng**: Đơn tối thiểu, giới hạn giảm tối đa, giới hạn lượt dùng
- **Phạm vi**: Voucher hệ thống hoặc voucher riêng từng shop
- **Áp mã trong giỏ hàng**: Kiểm tra hợp lệ và tính tiền giảm tự động

### 📰 **Hệ thống Bài viết (Blog)**
- **Danh mục & thẻ**: Phân loại bài viết theo `category` và `tag`
- **Bình luận**: Hỗ trợ bình luận theo luồng, duyệt/moderation
- **SEO**: `slug`, `meta title/description`, bài viết nổi bật, đếm lượt xem
- **Trang hiển thị**: Danh sách blog, tìm kiếm, chi tiết bài viết

### 🗺️ **Bản đồ & Địa chỉ (Goong Maps/Google-compatible)**
- **Autocomplete địa chỉ**: Gợi ý địa điểm theo từ khóa
- **Geocoding/Reverse**: Đổi địa chỉ ↔ tọa độ để lưu/hiển thị
- **Tích hợp giao hàng**: Hỗ trợ xác định khu vực, tùy chọn giao phù hợp

### ⭐ **Đánh giá & Yêu thích**
- **Hệ thống đánh giá**: Rating 1-5 sao, bình luận có hình ảnh/video
- **Thưởng One Xu**: 300 xu cho đánh giá đầu tiên có media
- **Danh sách yêu thích**: Lưu sản phẩm, toggle trạng thái yêu thích
- **Chỉ đánh giá sau mua**: Yêu cầu đã mua và nhận hàng mới được đánh giá

### 🚚 **Hệ thống Giao hàng & Vận chuyển**
- **Đa nhà cung cấp**: GHN, GHTK, J&T Express, Viettel Post, VNPost
- **Giao hỏa tốc**: Shipper riêng cùng tỉnh (2-4h)
- **Giao tiêu chuẩn**: Qua đơn vị vận chuyển (1-5 ngày)
- **Tính phí tự động**: Theo khoảng cách và loại giao hàng
- **Tracking**: Mã vận đơn, trạng thái giao hàng real-time

### 🖼️ **Quản lý Hình ảnh & Media**
- **Cloudinary tích hợp**: Upload ảnh sản phẩm, avatar, banner
- **Fallback local storage**: Tự động chuyển local nếu Cloudinary lỗi
- **Phân loại folder**: Products, users, categories, brands, ratings, chat
- **Validation**: Kiểm tra định dạng, kích thước file
- **Multi-banner**: Shop có thể upload nhiều banner

### 📞 **Liên hệ & Hỗ trợ**
- **Form liên hệ**: Gửi tin nhắn qua email
- **Hotline 24/7**: 1800 6324 miễn phí
- **Email support**: support@oneshop.vn
- **Chat real-time**: WebSocket với AI và CSKH
- **3 chi nhánh**: Tại TP.HCM

### 📊 **Báo cáo & Thống kê (Admin)**
- **Doanh thu**: Theo sản phẩm, danh mục, thương hiệu, thời gian
- **Khách hàng**: Thống kê đăng ký, hoạt động, phân bố
- **Đơn hàng**: Theo shop, khu vực, trạng thái
- **Bộ lọc nâng cao**: Theo ngày, tháng, quý, năm, shop
- **Xuất báo cáo**: Charts và bảng số liệu chi tiết

### 🔐 **Xác thực & Bảo mật nâng cao**
- **Hybrid Auth**: JWT + Session cho web và API
- **OAuth2 Social**: Facebook, Google login
- **Forgot Password**: OTP qua email, đặt lại mật khẩu
- **Email verification**: Xác thực tài khoản qua email
- **Role-based access**: USER, ADMIN, VENDOR, SHIPPER, CSKH
- **CSRF Protection**: Bảo vệ chống tấn công cross-site

## 🏗️ Kiến trúc hệ thống

### **Backend**
- **Spring Boot 3.5.5**: Framework chính
- **Spring Data JPA**: ORM và database access
- **Spring Security**: Bảo mật và xác thực
- **Spring WebSocket**: Chat real-time
- **Thymeleaf**: Template engine cho web pages
- **SiteMesh 3.2.0**: Decorator pattern cho admin layout
- **JSP + JSTL**: Template cho admin pages với decorator

### **Database**
- **SQL Server**: Database chính
- **MySQL**: Hỗ trợ thay thế
- **JPA/Hibernate**: ORM mapping

### **Frontend**
- **Bootstrap 5**: UI framework
- **jQuery**: JavaScript library
- **Font Awesome**: Icons
- **Slick Slider**: Carousel
- **Venobox**: Lightbox
- **Hybrid Template System**: Thymeleaf (web) + JSP/SiteMesh 

### **Tích hợp bên ngoài**
- **Cloudinary**: Quản lý lưu trữ hình ảnh
- **MoMo Payment**: Thanh toán ví điện tử
- **PayOS**: Cổng thanh toán ngân hàng trực tuyến
- **Google Gemini AI**: Chat AI
- **Gmail SMTP**: Gửi email
 - **Goong Maps**: Geocoding, Places (tương thích Google Maps API)

## 🚀 Cài đặt và chạy

### **Yêu cầu hệ thống**
- Java 21+
- Maven 3.6+
- SQL Server 2019+ hoặc MySQL 8.0+
- IDE (IntelliJ IDEA, Eclipse, VS Code)

### **Bước 1: Clone repository**
```bash
git clone https://github.com/thanglb2005/Web_MyPham
cd OneShop
```

### **Bước 2: Cấu hình database**
1. Tạo database `WebMyPham` trong SQL Server
2. Chạy script `DB.sql` để tạo bảng và dữ liệu mẫu
3. Cập nhật thông tin kết nối trong `application.properties`:

```properties
# SQL Server
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=WebMyPham;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=your_password
```

### **Bước 3: Cấu hình email**
Cập nhật thông tin Gmail SMTP trong `application.properties`:
```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### **Bước 4: Cấu hình Cloudinary**
Tạo tài khoản Cloudinary và cập nhật:
```properties
cloudinary.cloud-name=your-cloud-name
cloudinary.api-key=your-api-key
cloudinary.api-secret=your-api-secret
```

### **Bước 5: Cấu hình Goong Maps (Google Maps-compatible)**
Cập nhật khóa Goong Maps trong `application.properties`:
```properties
goong.maps.api-key=your-goong-api-key
goong.maps.tiles-key=your-goong-tiles-key
goong.maps.geocoding-url=https://rsapi.goong.io/Geocode
goong.maps.places-url=https://rsapi.goong.io/Place
```

### **Bước 6: Cấu hình Google Gemini AI**
Tạo API key từ [Google AI Studio](https://makersuite.google.com/app/apikey):
```properties
gemini.api.key=your-gemini-api-key
gemini.model.name=gemini-2.5-flash
gemini.max.tokens=1000
gemini.temperature=0.7
```

### **Bước 7: Cấu hình thanh toán (Tùy chọn)**
#### **MoMo Payment:**
```properties
momo.partner.code=your-partner-code
momo.access.key=your-access-key
momo.secret.key=your-secret-key
momo.api.endpoint=https://payment.momo.vn/v2/gateway/api/create
momo.return.url=http://localhost:8080/payment/momo/return
momo.notify.url=http://localhost:8080/payment/momo/notify
```

#### **PayOS (Ngân hàng):**
```properties
payos.client-id=your-client-id
payos.api-key=your-api-key
payos.checksum-key=your-checksum-key
payos.return-url=http://localhost:8080/payos/return
payos.cancel-url=http://localhost:8080/payos/cancel
```

### **Bước 8: Cấu hình OAuth2 Social Login (Tùy chọn)**
#### **Facebook Login:**
```properties
spring.security.oauth2.client.registration.facebook.client-id=your-facebook-app-id
spring.security.oauth2.client.registration.facebook.client-secret=your-facebook-app-secret
spring.security.oauth2.client.registration.facebook.scope=email,public_profile
spring.security.oauth2.client.registration.facebook.redirect-uri=http://localhost:8080/login/oauth2/code/facebook
```

#### **Google Login:**
```properties
spring.security.oauth2.client.registration.google.client-id=your-google-client-id
spring.security.oauth2.client.registration.google.client-secret=your-google-client-secret
spring.security.oauth2.client.registration.google.scope=email,profile
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8080/login/oauth2/code/google
```

### **Bước 9: Cấu hình JWT (Tùy chọn - có mặc định)**
```properties
jwt.secret=OneShopJWTSecretKey2025VerySecureAndLongEnoughForHS512Algorithm
jwt.access-token.expiration=3600000
jwt.refresh-token.expiration=86400000
jwt.issuer=OneShop
jwt.audience=OneShop-Users
```

### **Bước 10: Chạy ứng dụng**
```bash
mvn clean install
mvn spring-boot:run
```

Truy cập: `http://localhost:8080`

## ⚠️ **Lưu ý quan trọng**

### **Cấu hình tối thiểu để chạy:**
- ✅ **Database** (SQL Server/MySQL) - **BẮT BUỘC**
- ✅ **Email SMTP** - **BẮT BUỘC** (cho forgot password, OTP)
- ✅ **Cloudinary** - **BẮT BUỘC** (cho upload ảnh)

### **Cấu hình tùy chọn:**
- 🔧 **Goong Maps** - Tắt tính năng bản đồ nếu không có
- 🤖 **Gemini AI** - Chat sẽ không hoạt động nếu không có
- 💳 **MoMo/PayOS** - Chỉ thanh toán COD nếu không có
- 🔐 **OAuth2** - Chỉ đăng nhập email/password nếu không có

### **Kiểm tra cấu hình:**
1. Khởi động ứng dụng và kiểm tra console log
2. Truy cập `/actuator/health` để kiểm tra health status
3. Test upload ảnh, gửi email, thanh toán theo từng tính năng

## 👥 Tài khoản demo

| Vai trò | Email | Mật khẩu | Mô tả |
|---------|-------|----------|-------|
| **Admin** | admin@mypham.com | 123456 | Quản trị hệ thống |
| **Vendor** | vendor@mypham.com | 123456 | Chủ shop |
| **Shipper** | shipper@mypham.com | 123456 | Nhân viên giao hàng |
| **CSKH** | cskh@mypham.com | 123456 | Chăm sóc khách hàng |
| **User** | user@gmail.com | 123456 | Khách hàng |

## 📱 Các trang chính

### **👤 Khách hàng**
- `/` - Trang chủ
- `/product/{id}` - Chi tiết sản phẩm
- `/cart` - Giỏ hàng
- `/checkout` - Thanh toán
- `/orders` - Đơn hàng của tôi
- `/profile` - Thông tin cá nhân
- `/favorites` - Sản phẩm yêu thích
 - `/blogs` - Danh sách bài viết
- `/contact` - Liên hệ hỗ trợ
- `/onexu/dashboard` - Quản lý One Xu
- `/onexu/checkins` - Lịch sử check-in

### **🏪 Vendor**
- `/vendor/home` - Dashboard vendor
- `/vendor/dashboard` - Dashboard tổng quan
- `/vendor/products` - Quản lý sản phẩm
- `/vendor/orders` - Quản lý đơn hàng
- `/vendor/revenue` - Thống kê doanh thu
- `/vendor/shop` - Thông tin shop
- `/vendor/shop/settings` - Cài đặt shop (logo, banner)
 - `/vendor/promotions` - Khuyến mãi của shop

### **🚚 Shipper**
- `/shipper/home` - Dashboard shipper
- `/shipper/orders` - Đơn hàng cần giao
- `/shipper/statistics` - Thống kê giao hàng

### **👨‍💼 Admin**
- `/admin` - Dashboard admin
- `/admin/users` - Quản lý người dùng
- `/admin/accounts` - Quản lý tài khoản và phân quyền
- `/admin/products` - Quản lý sản phẩm
- `/admin/orders` - Quản lý đơn hàng
- `/admin/shops` - Quản lý cửa hàng (duyệt, từ chối)
- `/admin/shippers-list` - Quản lý shipper
- `/admin/categories` - Quản lý danh mục
- `/admin/brands` - Quản lý thương hiệu
- `/admin/providers` - Quản lý đơn vị vận chuyển
- `/admin/reports` - Báo cáo thống kê
- `/admin/revenue-statistics` - Thống kê doanh thu chi tiết
- `/admin/customer-statistics` - Thống kê khách hàng
 - `/admin/promotions` - Quản lý khuyến mãi
 - `/admin/blog` - Quản lý bài viết

### **💬 CSKH**
- `/cskh/chat` - Chat hỗ trợ khách hàng

### **🔧 API & WebSocket**
- `/chat` - Giao diện chat khách hàng
- `/api/images/upload` - Upload hình ảnh
- `/api/ai/chat` - Chat với AI Gemini

## 🔧 API Endpoints

### **Authentication**
```
POST /api/auth/login          # Đăng nhập
POST /api/auth/logout         # Đăng xuất
POST /api/auth/refresh        # Refresh token
GET  /api/auth/me            # Thông tin user
```

### **Products**
```
GET  /api/products           # Danh sách sản phẩm
GET  /api/products/{id}      # Chi tiết sản phẩm
GET  /api/products/search    # Tìm kiếm sản phẩm
```

### **Orders**
```
POST /api/orders             # Tạo đơn hàng
GET  /api/orders             # Danh sách đơn hàng
GET  /api/orders/{id}        # Chi tiết đơn hàng
PUT  /api/orders/{id}/status # Cập nhật trạng thái
```

### **Chat**
```
GET  /api/chat/history       # Lịch sử chat
POST /api/chat/send          # Gửi tin nhắn
POST /api/chat/upload        # Upload file
POST /api/chat/uploadImage   # Upload ảnh chat
GET  /api/chat/conversations # Danh sách cuộc trò chuyện
```

### **Promotions**
```
POST /cart/apply-promotion   # Áp mã khuyến mãi vào giỏ hàng
```

### **Maps (Goong)**
```
GET  /api/goong/geocode          # Địa chỉ → tọa độ
GET  /api/goong/reverse-geocode  # Tọa độ → địa chỉ
GET  /api/goong/search-places    # Gợi ý địa điểm (autocomplete)
GET  /api/goong/place-detail     # Chi tiết địa điểm
```

### **Delivery**
```
GET  /api/delivery/check-options # Kiểm tra tùy chọn giao hàng theo khu vực
```

### **Images & Media**
```
POST /api/images/upload         # Upload hình ảnh tổng quát
POST /api/images/product        # Upload ảnh sản phẩm
POST /api/images/chat           # Upload ảnh chat
```

### **Reviews & Favorites**
```
POST /reviews                   # Gửi đánh giá sản phẩm
POST /reviews/product           # Đánh giá không cần order
POST /addToFavorites           # Thêm vào yêu thích
POST /removeFromFavorites      # Xóa khỏi yêu thích
POST /toggleFavorite           # Toggle trạng thái yêu thích
```

### **Contact & Support**
```
POST /contact/send             # Gửi tin nhắn liên hệ
GET  /forgotPassword           # Trang quên mật khẩu
POST /forgotPassword           # Gửi OTP reset password
POST /confirmOtpReset          # Xác thực OTP
POST /resetPassword            # Đặt lại mật khẩu
```

### **One Xu System**
```
POST /onexu/checkin            # Check-in hàng ngày
GET  /onexu/dashboard          # Dashboard One Xu
GET  /onexu/transactions       # Lịch sử giao dịch
```

### **AI & Search**
```
GET  /api/ai/test              # Test kết nối Gemini AI
POST /api/ai/chat              # Chat với AI Gemini
POST /api/ai/search-products   # Tìm sản phẩm cho AI
GET  /api/search/autocomplete  # Gợi ý tìm kiếm tự động
```

## 📊 Cấu trúc Database

### **Bảng chính**
- `user` - Thông tin người dùng
- `role` - Vai trò người dùng
- `users_roles` - Liên kết user-role
- `categories` - Danh mục sản phẩm
- `brands` - Thương hiệu
- `products` - Sản phẩm
- `shops` - Cửa hàng
- `orders` - Đơn hàng
- `order_details` - Chi tiết đơn hàng
- `cart_items` - Giỏ hàng
- `one_xu_transactions` - Giao dịch One Xu
- `chat_messages` - Tin nhắn chat
- `promotions` - Khuyến mãi/voucher
- `blog_categories`, `blog_posts`, `blog_tags`, `blog_post_tags`, `blog_comments` - Hệ thống blog
- `shipping_providers` - Đơn vị vận chuyển (GHN, GHTK, J&T...)
- `favorites` - Danh sách yêu thích
- `comments`, `comment_media` - Đánh giá sản phẩm với media
- `one_xu_weekly_schedule` - Lịch trình check-in One Xu

## 🎨 Giao diện

### **Responsive Design**
- Mobile-first approach
- Bootstrap 5 grid system
- Custom CSS cho mỹ phẩm
- Dark/Light theme support

### **UI Components**
- Product cards với hover effects
- Shopping cart với animation
- Checkout wizard
- Admin dashboard với charts
- Chat interface real-time

## 🔒 Bảo mật

### **Authentication**
- JWT tokens với refresh mechanism
- Session-based authentication
- OAuth2 social login
- Password encryption với BCrypt

### **Authorization**
- Role-based access control (RBAC)
- Method-level security
- CSRF protection
- SQL injection prevention

## 📈 Performance

### **Optimization**
- Lazy loading cho entities
- Database indexing
- Image optimization với Cloudinary
- Caching với Spring Cache
- Connection pooling

### **Monitoring**
- Spring Boot Actuator
- Health checks
- Metrics collection
- Error logging

## 🧪 Testing

### **Unit Tests**
```bash
mvn test
```

### **Integration Tests**
```bash
mvn verify
```

## 📝 Changelog

### **v1.0.0** (2025-01-15) - Initial Release
#### ✨ **Core Features**
- 🛒 **E-commerce Platform**: Sản phẩm mỹ phẩm với danh mục đa dạng
- 👥 **Multi-role System**: USER, ADMIN, VENDOR, SHIPPER, CSKH
- 🛍️ **Shopping Cart**: Giỏ hàng với tính năng cập nhật real-time
- 📦 **Order Management**: Quản lý đơn hàng với tracking đầy đủ

#### 💳 **Payment & Rewards**
- 💰 **Multiple Payment Methods**: COD, MoMo, PayOS, Bank Transfer
- 💰 **One Xu System**: Check-in hàng ngày, thưởng đánh giá, thanh toán
- 🎁 **Promotion System**: Voucher hệ thống và shop với nhiều loại ưu đãi

#### 🚚 **Delivery & Logistics**
- 📍 **Multi-provider Shipping**: GHN, GHTK, J&T Express, Viettel Post, VNPost
- ⚡ **Express Delivery**: Giao hỏa tốc cùng tỉnh trong 2-4 giờ
- 🗺️ **Maps Integration**: Goong Maps cho geocoding và địa chỉ

#### 💬 **Communication & Support**
- 🤖 **AI Chat**: Google Gemini AI cho hỗ trợ khách hàng
- 💬 **Real-time Chat**: WebSocket chat giữa khách hàng và CSKH
- 📞 **Contact System**: Form liên hệ, hotline 24/7

#### 📰 **Content Management**
- 📝 **Blog System**: Bài viết với categories, tags, comments
- ⭐ **Review System**: Đánh giá sản phẩm với hình ảnh/video
- ❤️ **Favorites**: Danh sách yêu thích sản phẩm

#### 🔐 **Security & Authentication**
- 🔑 **Hybrid Auth**: JWT + Session authentication
- 🌐 **OAuth2 Social Login**: Facebook, Google integration
- 🔒 **Password Security**: BCrypt encryption, OTP reset

#### 🖼️ **Media & Storage**
- ☁️ **Cloudinary Integration**: Upload và quản lý hình ảnh
- 📱 **Responsive Design**: Mobile-first với Bootstrap 5
- 🎨 **SiteMesh Layout**: Decorator pattern cho admin pages

#### 📊 **Analytics & Reporting**
- 📈 **Revenue Statistics**: Báo cáo doanh thu theo nhiều tiêu chí
- 👥 **Customer Analytics**: Thống kê khách hàng và hoạt động
- 📋 **Order Reports**: Báo cáo đơn hàng theo shop, khu vực, thời gian


## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

## 👨‍💻 Tác giả

**OneShop Team**
- Lê Văn Chiến Thắng                	    23110328
- Võ Thanh Sang                         	23110301
- Trịnh Nguyễn Hoàng Nguyên  	            23110272
- Nguyễn Phước Khang              	        23110236


## 🙏 Acknowledgments

- Spring Boot community
- Bootstrap team
- Font Awesome
- Cloudinary
- MoMo Payment
- PayOS Payment Gateway
- Google Gemini AI
- Goong Maps
- SiteMesh
- Thymeleaf

---
