# 🛍️ OneShop - Hệ thống bán mỹ phẩm đa nền tảng

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![SQL Server](https://img.shields.io/badge/SQL%20Server-2019-blue.svg)](https://www.microsoft.com/en-us/sql-server)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Tổng quan

**OneShop** là một hệ thống thương mại điện tử chuyên về mỹ phẩm, được xây dựng với Spring Boot và hỗ trợ đa vai trò người dùng. Hệ thống tích hợp nhiều tính năng hiện đại như thanh toán điện tử, giao hàng hỏa tốc, hệ thống điểm thưởng One Xu, và chat hỗ trợ khách hàng.

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

### 🪙 **Hệ thống One Xu**
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

## 🏗️ Kiến trúc hệ thống

### **Backend**
- **Spring Boot 3.5.5**: Framework chính
- **Spring Data JPA**: ORM và database access
- **Spring Security**: Bảo mật và xác thực
- **Spring WebSocket**: Chat real-time
- **Thymeleaf**: Template engine

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

### **Tích hợp bên ngoài**
- **Cloudinary**: Quản lý hình ảnh
- **MoMo Payment**: Thanh toán điện tử
- **PayOS**: Cổng thanh toán
- **Google Gemini AI**: Chat AI
- **Gmail SMTP**: Gửi email

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

### **Bước 5: Chạy ứng dụng**
```bash
mvn clean install
mvn spring-boot:run
```

Truy cập: `http://localhost:8080`

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

### **🏪 Vendor**
- `/vendor/home` - Dashboard vendor
- `/vendor/products` - Quản lý sản phẩm
- `/vendor/orders` - Quản lý đơn hàng
- `/vendor/revenue` - Thống kê doanh thu
- `/vendor/shop` - Thông tin shop

### **🚚 Shipper**
- `/shipper/home` - Dashboard shipper
- `/shipper/orders` - Đơn hàng cần giao
- `/shipper/statistics` - Thống kê giao hàng

### **👨‍💼 Admin**
- `/admin` - Dashboard admin
- `/admin/users` - Quản lý người dùng
- `/admin/products` - Quản lý sản phẩm
- `/admin/orders` - Quản lý đơn hàng
- `/admin/reports` - Báo cáo thống kê

### **💬 CSKH**
- `/cskh/chat` - Chat hỗ trợ khách hàng

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

### **v1.0.0** (2025-01-15)
- ✨ Initial release
- 🛒 E-commerce functionality
- 👥 Multi-role system
- 💳 Payment integration
- 🚚 Delivery system
- 🪙 One Xu rewards
- 💬 Chat support
- 🤖 AI integration


## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

## 👨‍💻 Tác giả

**OneShop Team**
- Võ Thanh Sang                        	23110301
- Lê Văn Chiến Thắng                	  23110328
- Trịnh Nguyễn Hoàng Nguyên  	          23110272
- Nguyễn Phươc Khang              	    23110236


## 🙏 Acknowledgments

- Spring Boot community
- Bootstrap team
- Font Awesome
- Cloudinary
- MoMo Payment
- Google Gemini AI

---
