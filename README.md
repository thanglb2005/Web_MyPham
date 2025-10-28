# 🛍️ OneShop - Hệ thống bán mỹ phẩm đa nền tảng

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![SQL Server](https://img.shields.io/badge/SQL%20Server-2019-blue.svg)](https://www.microsoft.com/sql-server)
[![WebSocket](https://img.shields.io/badge/Realtime-WebSocket%20%2B%20STOMP-6aa84f.svg)](#-chat-realtime)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📋 Tổng quan

**OneShop** là hệ thống thương mại điện tử chuyên về mỹ phẩm, xây dựng theo **kiến trúc 3 tầng** (Controller → Service → Repository), hỗ trợ nhiều vai trò người dùng:  
**User**, **Vendor**, **Shipper**, **CSKH**, **Admin**.  
Tích hợp thanh toán **MoMo**, **PayOS**, **One Xu** (điểm thưởng), **WebSocket chat realtime**, và bảo mật **JWT + Session hybrid**.

### 🌐 Công nghệ chính
- Spring Boot 3.5.5 (Java 21)
- Thymeleaf + JSP (SiteMesh)
- Spring Security (JWT, OAuth2)
- WebSocket STOMP (chat realtime)
- SQL Server / MySQL
- Cloudinary, MoMo, PayOS, SMTP Mail

---

## ✨ Tính năng nổi bật (theo vai trò)

### 👤 Khách hàng (USER)

- Chức năng chính:
  - Duyệt danh mục/thương hiệu, tìm kiếm; xem chi tiết, thêm giỏ hàng
  - Áp mã khuyến mãi (toàn hệ thống/thuộc shop), sử dụng One Xu; ước tính phí ship tiêu chuẩn/hỏa tốc
  - Thanh toán COD, MoMo, PayOS (tự động cập nhật trạng thái đơn khi thành công/huỷ)
  - Đánh giá sản phẩm (văn bản + hình ảnh), quản lý danh sách yêu thích
  - Chat realtime với shop/CSKH (gửi văn bản/ảnh)
  - Quản lý đơn hàng: xem chi tiết, trạng thái và lịch sử

### 🏪 Người bán (VENDOR)

- Chức năng chính:
  - Quản lý thông tin shop (logo, địa chỉ, cho phép COD, ngày xử lý…)
  - Quản lý sản phẩm: thêm/sửa/xóa, tồn kho, giảm giá/nổi bật
  - Quản lý khuyến mãi theo shop: tạo/sửa/xóa
  - Quản lý đơn hàng: xác nhận/giao/hủy; in/preview hóa đơn đơn giản
  - Thống kê: tổng doanh thu từ đơn DELIVERED; top sản phẩm
  - Chat realtime với khách (tin nhắn văn bản/ảnh, quick replies)

### 🚚 Shipper

- Chức năng chính: xem đơn có sẵn/được phân công, nhận đơn; cập nhật SHIPPING/DELIVERED
- Thông tin hiển thị: địa chỉ giao, liên hệ, phương thức thanh toán, phí ship

### 💬 CSKH

- Chức năng chính:
  - Xem danh sách phòng chat khách (support), chủ động mở phòng liên hệ Vendor/Shop (liaison)
  - Gửi tin nhắn nhanh, gửi ảnh; xem lịch sử cuộc hội thoại
- Phân quyền: quyền chat quản trị tương đương ADMIN; có thể chọn Vendor/Shop để mở phòng

### 👨‍💼 Admin

- Chức năng chính:
  - Quản trị thương hiệu (JSP): thêm/sửa/xóa, hiển thị ảnh (Cloudinary/local), CSRF bắt buộc
  - Quản trị shipper: tạo mới (gán ROLE_SHIPPER), xóa; phân công/gỡ phân công khỏi shop
  - Quản trị đơn vị vận chuyển: cấu hình phí/biểu tượng; theo dõi thống kê hệ thống
  - Bảng chat hợp nhất (Admin/CSKH/Vendor) + quick replies; xoá lịch sử phòng
- Phân quyền: khu quản trị chỉ ADMIN; bảng chat hợp nhất cho ADMIN, CSKH, VENDOR

---

## 🧱 Công nghệ theo từng tầng

```text
+------------------------------------------------------------+
|                    Presentation Layer                      |
| (Thymeleaf Templates + JSP + SiteMesh + JavaScript + CSS)  |
+------------------------------------------------------------+
                             │
                             ▼
+------------------------------------------------------------+
|                     Controller Layer                       |
|            (Spring MVC + REST Controllers)                 |
+------------------------------------------------------------+
                             │
                             ▼
+------------------------------------------------------------+
|                       Service Layer                        |
|        (Business Logic + Validation + Security)            |
+------------------------------------------------------------+
                             │
                             ▼
+------------------------------------------------------------+
|                     Repository Layer                       |
|           (Spring Data JPA + Query Methods)                |
+------------------------------------------------------------+
                             │
                             ▼
+------------------------------------------------------------+
|                      Database Layer                        |
|            (Microsoft SQL Server / MySQL)                  |
+------------------------------------------------------------+
```

- **View:** Thymeleaf (templates) + JSP (WEB-INF) + SiteMesh Decorators  
- **Controller:** nhận request, validate input, chuyển Service  
- **Service:** xử lý nghiệp vụ (đơn hàng, khuyến mãi, chat, thanh toán...)  
- **Repository:** thao tác CSDL qua Spring Data JPA  
- **Entity / DTO:** ánh xạ bảng, dữ liệu form & response  

---

## 📁 Cấu trúc thư mục (chi tiết)

```text
OneShop/
├─ pom.xml
├─ mvnw, mvnw.cmd
├─ DB.sql                          # Toàn bộ schema + dữ liệu mẫu
├─ JWT_GUIDE.md
├─ docs/
│  ├─ ui-specs/
│  └─ vendor-chm/
├─ build/                          # Thư mục build (nếu IDE tạo)
├─ tools/
│  └─ chm/
├─ upload/                         # Lưu file tĩnh nếu dùng local storage
│  ├─ brands/                      # Ảnh logo thương hiệu
│  ├─ images/                      # Ảnh upload người dùng
│  └─ providers/                   # Logo đơn vị vận chuyển
├─ src/
│  ├─ main/
│  │  ├─ java/vn/
│  │  │  ├─ OneShopApplication.java
│  │  │  ├─ config/                # Cấu hình ứng dụng
│  │  │  │  ├─ SecurityConfig.java
│  │  │  │  ├─ WebSocketConfig.java
│  │  │  │  ├─ SitemeshConfig.java
│  │  │  │  ├─ MailConfig.java
│  │  │  │  └─ (các cấu hình khác ...)
│  │  │  ├─ controller/            # Controllers theo module/role
│  │  │  │  ├─ web/                # Trang web chính (home, product, cart, checkout...)
│  │  │  │  ├─ vendor/             # Vendor dashboard, products, orders, promotions, revenue...
│  │  │  │  ├─ admin/              # Admin quản trị (brands, shippers, ...)
│  │  │  │  ├─ shipper/            # Shipper
│  │  │  │  ├─ cskh/               # CSKH
│  │  │  │  ├─ api/                # REST APIs (auth, delivery, chat, ...)
│  │  │  │  ├─ websocket/          # Message mapping WS/STOMP
│  │  │  │  └─ payment/            # MoMo/PayOS controllers
│  │  │  ├─ dto/                   # DTO (vd: VendorPromotionForm, forms ...)
│  │  │  ├─ entity/                # JPA Entities (User, Role, Shop, Product, Order, ...)
│  │  │  ├─ repository/            # Spring Data JPA Repositories
│  │  │  ├─ service/               # Business services + impl/*
│  │  │  │  ├─ impl/
│  │  │  │  └─ chat/ (nếu có package con)
│  │  │  └─ util/                  # Helpers: UserUtils, chat utils, ...
│  │  ├─ resources/
│  │  │  ├─ application.properties
│  │  │  ├─ templates/             # Thymeleaf views
│  │  │  │  ├─ fragments/          # header, footer, common components
│  │  │  │  ├─ layout/             # layout templates
│  │  │  │  ├─ decorators/         # (nếu dùng với Thymeleaf)
│  │  │  │  ├─ admin/              # Trang quản trị (HTML)
│  │  │  │  ├─ vendor/             # Trang vendor (promotions, products, orders, revenue, settings...)
│  │  │  │  ├─ shipper/            # Trang shipper
│  │  │  │  ├─ cskh/               # Trang CSKH
│  │  │  │  ├─ web/                # Trang web người dùng (home, product-detail, cart, checkout...)
│  │  │  │  ├─ login.html, register.html, profile.html, ...
│  │  │  │  └─ (các view HTML khác ...)
│  │  │  ├─ static/
│  │  │  │  ├─ admin/              # JS/CSS riêng cho admin
│  │  │  │  ├─ assets/             # css/js/scss/img/fonts tổng hợp
│  │  │  │  ├─ css/                # stylesheet chính
│  │  │  │  ├─ fonts/              # eot/woff/woff2/ttf
│  │  │  │  ├─ images/             # ảnh tĩnh
│  │  │  │  ├─ js/                 # script chung (popup chat, ui...)
│  │  │  │  ├─ vendor/             # vendor libs (slick, venobox, ...)
│  │  │  │  └─ db.json             # dữ liệu địa chỉ (province/commune) cho form
│  │  │  └─ WEB-INF/               # Sitemesh decorators cho JSP
│  │  └─ webapp/
│  │     └─ WEB-INF/
│  │        ├─ admin/              # Trang JSP quản trị (legacy)
│  │        ├─ decorators/         # decorator.jsp
│  │        └─ decorators.xml      # cấu hình SiteMesh
│  └─ test/java/vn/
│     ├─ controller/...
│     └─ OneShopApplicationTests.java
├─ target/                         # Build output (tạo khi mvn package)
└─ README.md
```

---

## 🛠️ Công nghệ sử dụng

| Nhóm | Công nghệ | Ghi chú |
|------|------------|---------|
| **Backend** | Spring Boot 3.5.5, Java 21 | Web, Security, JPA, Validation |
| **Frontend** | Thymeleaf, JSP (SiteMesh), Bootstrap 5 | View layer |
| **Database** | SQL Server 2019+, MySQL 8 | ORM Hibernate 6 |
| **Auth** | Spring Security, OAuth2, JWT, BCrypt | Hybrid JWT + Session |
| **Realtime** | WebSocket + STOMP (SockJS) | Chat, thông báo |
| **Storage/CDN** | Cloudinary | Upload hình ảnh |
| **Thanh toán** | MoMo API, PayOS API | Payment gateways |
| **Mail** | Spring Mail (SMTP) | Gửi OTP / thông báo |
| **QR** | ZXing | Tạo mã QR thanh toán |
| **DevTools** | Lombok, Actuator | Giảm boilerplate, health check |

---

## 🔐 Bảo mật

- **Hybrid Auth:** JWT filter + Session  
- **OAuth2 Login:** Google, Facebook  
- **RBAC:** ROLE_USER / ROLE_VENDOR / ROLE_SHIPPER / ROLE_CSKH / ROLE_ADMIN  
- **CSRF:** bảo vệ form  
- **Password:** BCrypt hash  
- **Route Mapping:**
  - `/admin/**` → ADMIN  
  - `/vendor/**` → VENDOR  
  - `/cskh/**` → CSKH  
  - `/shipper/**` → SHIPPER  

---

## 🗂️ CSDL
- `user`, `role`, `users_roles`
- `shops`, `products`, `brands`, `categories`
- `shipping_providers`
- `orders`, `order_details`
- `cart_items`, `favorites`, `comments`
- `promotions` (voucher hệ thống và theo shop)
- `shop_shippers` (phân công shipper cho shop)
- `one_xu_transactions`, `one_xu_weekly_schedule`
- `chat_message` (lịch sử phòng chat)
- `blog_categories`, `blog_posts`, `blog_tags`, `blog_post_tags`, `blog_comments`, `blog_views`

---

## 🚀 Cài đặt & chạy

### Yêu cầu
- Java 21+, Maven 3.9+
- SQL Server 2019+ hoặc MySQL 8
- IDE (IntelliJ / Eclipse / VSCode)

### 1️⃣ Clone dự án
```bash
git clone https://github.com/thanglb2005/Web_MyPham
cd Web_MyPham/OneShop
```

### 2️⃣ Tạo database và dữ liệu mẫu
- Tạo DB `WebMyPham`
- Chạy `DB.sql`

### 3️⃣ Cấu hình `application.properties`
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=WebMyPham;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### 4️⃣ Build & Run
```bash
mvn clean install
./mvnw spring-boot:run
```

Truy cập: http://localhost:8080

---

## 👥 Tài khoản demo

| Vai trò | Email | Mật khẩu |
|----------|--------|-----------|
| Admin | admin@mypham.com | 123456 |
| Vendor A | vendor@mypham.com | 123456 |
| Vendor B | vendor1@mypham.com | 123456 |
| Shipper | shipper@mypham.com | 123456 |
| CSKH | cskh@mypham.com | 123456 |
| User | user@gmail.com | 123456 |

---

## 💬 Chat realtime

- **Protocol:** WebSocket + STOMP (SockJS fallback)  
- **Phòng:** `shop-{shopId}-customer-{userId}` / `support-{userId}`  
- **Lưu lịch sử:** bảng `chat_message`  
- **Upload ảnh:** `/api/chat/uploadImage`

---

## 💳 Thanh toán

- **MoMo**  
  - Controller: `MoMoPaymentController`  
  - Endpoint: `/payment/momo/create`, `/payment/momo/return`, `/payment/momo/notify`
- **PayOS**  
  - Controller: `PayOSController`  
  - Endpoint: `/payos/create-payment`, `/payos/return`, `/payos/cancel`, `/payos/webhook`

---

## 🧪 Kiểm thử

```bash
mvn test
mvn verify
```

- Kiểm tra luồng thanh toán COD / MoMo / PayOS  
- Áp khuyến mãi PERCENTAGE / FIXED_AMOUNT / FREE_SHIPPING  
- Chat TEXT / IMAGE  
- Phân quyền truy cập `/admin`, `/vendor`, `/cskh`  

---

## 👨‍💻 Nhóm phát triển

| Họ tên | MSSV |
|--------|------|
| Võ Thanh Sang | 23110301 |
| Lê Văn Chiến Thắng | 23110328 |
| Trịnh Nguyễn Hoàng Nguyên | 23110272 |
| Nguyễn Phước Khang | 23110236 |
