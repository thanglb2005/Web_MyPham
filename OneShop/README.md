# 🛍️ OneShop - Hệ thống bán mỹ phẩm đa nền tảng

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![SQL Server](https://img.shields.io/badge/SQL%20Server-2019-blue.svg)](https://www.microsoft.com/sql-server)
[![WebSocket](https://img.shields.io/badge/Realtime-WebSocket%20%2B%20STOMP-6aa84f.svg)](#-chat-realtime)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Tổng quan

OneShop là hệ thống thương mại điện tử chuyên về mỹ phẩm, xây dựng theo kiến trúc 3 tầng (Controller → Service → Repository), hỗ trợ nhiều vai trò người dùng (User, Vendor, Shipper, CSKH, Admin), tích hợp thanh toán MoMo/PayOS, giao hàng, điểm thưởng One Xu và chat realtime.

- Triển khai web MVC (Thymeleaf, JSP + SiteMesh)
- Bảo mật Spring Security (JWT + Session hybrid, OAuth2 Google/Facebook)
- Realtime qua WebSocket/STOMP, lưu lịch sử chat
- Hỗ trợ đa CSDL (ưu tiên SQL Server, có cấu hình MySQL)

## ✨ Tính năng nổi bật (theo vai trò)

### 👤 Khách hàng (USER)

- Chức năng chính:
  - Duyệt danh mục/thương hiệu, tìm kiếm; xem chi tiết, thêm giỏ hàng
  - Áp mã khuyến mãi (toàn hệ thống/thuộc shop), sử dụng One Xu; ước tính phí ship tiêu chuẩn/hỏa tốc
  - Thanh toán COD, MoMo, PayOS (tự động cập nhật trạng thái đơn khi thành công/huỷ)
  - Đánh giá sản phẩm (văn bản + hình ảnh), quản lý danh sách yêu thích
  - Chat realtime với shop/CSKH (gửi văn bản/ảnh)
  - Quản lý đơn hàng: xem chi tiết, trạng thái và lịch sử
- Quy tắc/Validation:
  - Không vượt quá tồn kho; phí vận chuyển theo cấu hình; One Xu không âm
  - Bảo vệ CSRF ở form; yêu cầu đăng nhập khi đặt hàng

### 🏪 Người bán (VENDOR)

- Chức năng chính:
  - Quản lý thông tin shop (logo, địa chỉ, cho phép COD, ngày xử lý…)
  - Quản lý sản phẩm: thêm/sửa/xóa, tồn kho, giảm giá/nổi bật
  - Quản lý khuyến mãi theo shop: tạo/sửa/xóa; nhập form theo `VendorPromotionForm` với kiểm tra giá trị
  - Quản lý đơn hàng: xác nhận/giao/hủy; in/preview hóa đơn đơn giản
  - Thống kê: tổng doanh thu từ đơn DELIVERED; top sản phẩm
  - Chat realtime với khách (tin nhắn văn bản/ảnh, quick replies)
- Quy tắc/Validation:
  - Mọi thao tác theo ngữ cảnh `shopId`; `promotion_code` duy nhất toàn hệ thống
  - Định dạng ngày `yyyy-MM-dd'T'HH:mm`; PERCENTAGE (0–100), FIXED_AMOUNT > 0

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
  - Quản trị shipper: tạo mới (gán ROLE_SHIPPER), xóa; phân công/gỡ phân công khỏi shop (`shop_shippers`)
  - Quản trị đơn vị vận chuyển: cấu hình phí/biểu tượng; theo dõi thống kê hệ thống
  - Bảng chat hợp nhất (Admin/CSKH/Vendor) + quick replies; xoá lịch sử phòng
- Phân quyền: khu quản trị chỉ ADMIN; bảng chat hợp nhất cho ADMIN, CSKH, VENDOR

## 🧱 Kiến trúc 3 tầng & thành phần

```mermaid
graph LR
  A[Client (Browser)] -->|HTTP| B[Controllers (vn.controller.*)]
  B -->|DTO/Model| C[Services (vn.service.*)]
  C -->|JPA| D[Repositories (vn.repository.*)]
  D -->|JDBC| E[(SQL Server / MySQL)]

  subgraph Realtime_Chat
    A -->|WS/STOMP| G[WebSocket Endpoints]
    G --> C
    C --> D
  end

  subgraph Security
    A -->|OAuth2/JWT/Session| H[Spring Security Filter Chain]
    H --> B
  end

  subgraph Integrations
    C -.-> I[(Cloudinary)]
    C -.-> J[(MoMo API)]
    C -.-> K[(PayOS API)]
    C -.-> L[(SMTP Email)]
  end
```

- Trình bày (View): Thymeleaf (resources/templates) + JSP (webapp/WEB-INF) với SiteMesh
- Controller: tiếp nhận request, validate đầu vào, chuyển tiếp sang Service
- Service: nghiệp vụ (khuyến mãi, đơn hàng, chat, thanh toán, tài khoản…)
- Repository: Spring Data JPA thao tác CSDL
- Entity: ánh xạ bảng; DTO: dữ liệu form/response

### 🧩 Công nghệ theo từng tầng (Layered Mapping)

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

- Presentation Layer

  - Thymeleaf 3.x (spring-boot-starter-thymeleaf), Thymeleaf Layout Dialect 3.4.0
  - JSP (tomcat-embed-jasper) + JSTL API 3.0.0 / Impl 3.0.1
  - SiteMesh 3.2.0 (decorators cho trang JSP legacy)
  - Front-end libs: Bootstrap 5, jQuery, Font Awesome, Slick Slider, Venobox/Lightbox, custom CSS/SCSS
  - WebSocket clients: SockJS 1.5.1 (WebJar), STOMP 2.3.4 (WebJar)
  - Moment.js 2.29.4 (WebJar) cho hiển thị thời gian UI
  - CSRF token trong form (JSP/Thymeleaf), fragments header/footer

- Controller Layer

  - Spring MVC (spring-boot-starter-web), REST Controllers (web/vendor/admin/shipper/cskh/api)
  - WebSocket endpoints (spring-boot-starter-websocket + spring-messaging) cho chat realtime
  - Jackson + jackson-datatype-hibernate6 để serialize entity/lazy proxy
  - Jakarta Validation (@Valid, @NotBlank, @FutureOrPresent, ...) trên DTO/form
  - Upload file ảnh qua Servlet multipart + tích hợp Cloudinary
  - Payment controllers: `MoMoPaymentController` (MoMo API), `PayOSController` (java.net.http)

- Service Layer

  - Spring @Service, @Transactional (quản lý giao dịch)
  - Business services: OrderService, PromotionService, UserService, ChatHistoryService, ShopService...
  - Bảo mật: BCryptPasswordEncoder, JWT (jjwt 0.12.3), OAuth2 Client (CustomOAuth2UserService)
  - Email: Spring Mail (SMTP Gmail)
  - Ảnh/CDN: Cloudinary Java SDK 1.36.0
  - QR/Barcode: ZXing core/javase 3.5.2
  - Scheduling/Background (Spring Scheduling) – dùng cho tác vụ định kỳ (log có "SCHEDULER RUNNING")

- Repository Layer

  - Spring Data JPA Repositories, Query Methods, @Query JPQL
  - Hibernate 6 (qua spring-boot-starter-data-jpa)
  - Connection pool: HikariCP (mặc định của Spring Boot)

- Database Layer

  - Microsoft SQL Server 2019+ (chính), MySQL 8 (tuỳ chọn)
  - Lược đồ trong `DB.sql`: bảng, ràng buộc CHECK, UNIQUE, FK, chỉ mục (IX\_\*)
  - Kiểu dữ liệu: DECIMAL, DATETIME2, NVARCHAR, BIT; chuẩn hóa tiền tệ/enum bằng CHECK

- Cross-cutting / Dev & Ops
  - Spring Boot Actuator (health/metrics)
  - Lombok 1.18.38 (giảm boilerplate), Commons IO 2.11.0 (tiện ích IO)
  - Spring Boot Devtools (reload dev), Embedded Tomcat, logging chuẩn Spring Boot

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

Gợi ý mapping package → layer:

- `vn.controller.*` → Controller Layer; `vn.controller.websocket.*` → WebSocket endpoints
- `vn.service.*` / `vn.service.impl.*` → Service Layer
- `vn.repository.*` → Repository Layer
- `vn.entity.*` → Domain/Entity Layer
- `resources/templates/*`, `webapp/WEB-INF/*` → Presentation Layer (Thymeleaf/JSP)

## 🛠️ Công nghệ sử dụng

- Backend: Spring Boot 3.5.5, Java 21
  - Spring Web, Thymeleaf, JSP/JSTL (tomcat-embed-jasper), SiteMesh 3.2
  - Spring Data JPA (Hibernate 6), Validation
  - Spring Security, OAuth2 Client (Google/Facebook), BCrypt
  - JWT (jjwt 0.12.x) + Session (hybrid)
  - WebSocket + STOMP (sockjs, stomp-websocket)
  - Mail (Spring Mail)
  - Actuator (health, metrics)
  - Jackson Hibernate module
  - ZXing (QR)
- Database: Microsoft SQL Server 2019+ (ưu tiên), MySQL 8 (tuỳ chọn cấu hình)
- Frontend: Bootstrap 5, jQuery, Font Awesome, Slick, Venobox, custom SCSS/CSS
- Storage/CDN: Cloudinary (tuỳ chọn; fallback local /upload)
- Thanh toán: MoMo (tạo payment URL, return/notify), PayOS (create link, return, cancel, webhook)

### 📦 Chi tiết phụ thuộc (từ pom.xml)

| Nhóm           | Artifact                                                   | Phiên bản          | Mục đích                  |
| -------------- | ---------------------------------------------------------- | ------------------ | ------------------------- |
| Spring Boot    | spring-boot-starter-web                                    | 3.5.5 (qua parent) | REST/MVC                  |
|                | spring-boot-starter-thymeleaf                              |                    | Template engine           |
|                | spring-boot-starter-data-jpa                               |                    | ORM Hibernate 6           |
|                | spring-boot-starter-validation                             |                    | Bean Validation (Jakarta) |
|                | spring-boot-starter-security                               |                    | Bảo mật, RBAC             |
|                | spring-boot-starter-oauth2-client                          |                    | OAuth2 social login       |
|                | spring-boot-starter-websocket                              |                    | WebSocket server          |
|                | spring-messaging                                           |                    | STOMP messaging           |
|                | spring-boot-starter-mail                                   |                    | SMTP email                |
|                | spring-boot-starter-actuator                               |                    | Health/metrics            |
| Dev            | spring-boot-devtools                                       | runtime            | Reload dev                |
| View/JSP       | tomcat-embed-jasper                                        |                    | JSP compile               |
|                | jakarta.servlet.jsp.jstl-api                               | 3.0.0              | JSTL API                  |
|                | org.glassfish.web:jakarta.servlet.jsp.jstl                 | 3.0.1              | JSTL impl                 |
| Layout         | nz.net.ultraq:thymeleaf-layout-dialect                     | 3.4.0              | Layout cho Thymeleaf      |
| Decorators     | org.sitemesh:sitemesh                                      | 3.2.0              | SiteMesh cho JSP legacy   |
| DB Drivers     | com.microsoft.sqlserver:mssql-jdbc                         | 10.2.3.jre17       | SQL Server driver         |
|                | com.mysql:mysql-connector-j                                |                    | MySQL driver (tuỳ chọn)   |
| JWT            | io.jsonwebtoken:jjwt-api                                   | 0.12.3             | JWT API                   |
|                | io.jsonwebtoken:jjwt-impl                                  | 0.12.3 (runtime)   | JWT impl                  |
|                | io.jsonwebtoken:jjwt-jackson                               | 0.12.3 (runtime)   | JWT + Jackson             |
| WebJars        | org.webjars:sockjs-client                                  | 1.5.1              | SockJS client             |
|                | org.webjars:stomp-websocket                                | 2.3.4              | STOMP client              |
|                | org.webjars:momentjs                                       | 2.29.4             | Xử lý thời gian front-end |
| Ảnh/CDN        | com.cloudinary:cloudinary-http44                           | 1.36.0             | Upload/URL hình ảnh       |
| Tiện ích       | org.projectlombok:lombok                                   | 1.18.38            | Giảm boilerplate          |
|                | commons-io:commons-io                                      | 2.11.0             | IO helpers                |
| JSON/Hibernate | com.fasterxml.jackson.datatype:jackson-datatype-hibernate6 |                    | Serialize lazy proxies    |
| QR             | com.google.zxing:core                                      | 3.5.2              | QR/barcode core           |
|                | com.google.zxing:javase                                    | 3.5.2              | QR/barcode Java SE        |
| Test           | spring-boot-starter-test                                   |                    | JUnit/Mockito             |

Ghi chú kỹ thuật:

- HikariCP là pool mặc định của Spring Boot; không cần khai báo riêng.
- Embedded Tomcat chạy mặc định (có cấu hình `spring-boot-maven-plugin`).
- WebSocket dùng STOMP endpoints với SockJS fallback; phía client dùng WebJars.

## 🔐 Bảo mật

- Hybrid Auth: JWT filter + Session (IF_REQUIRED)
- OAuth2 Login: Google/Facebook (trang /login)
- RBAC: ROLE_USER, ROLE_VENDOR, ROLE_SHIPPER, ROLE_CSKH, ROLE_ADMIN
- CSRF: bảo vệ ở form (JSP/Thymeleaf) nơi cần; một số endpoint API dạng POST dùng token riêng
- Password: BCrypt
- Phân quyền tiêu biểu (trích):
  - /admin/** → ADMIN; /vendor/** → VENDOR; /cskh/** → CSKH; /shipper/** → SHIPPER
  - /admin/vendor-chat → ADMIN|CSKH|VENDOR

## 🗂️ CSDL chính (đầy đủ, theo DB.sql)

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

> Chi tiết schema, ràng buộc (FK/CHECK/UNIQUE/INDEX), sample data: xem file `DB.sql`.

## 🚀 Cài đặt & chạy

### Yêu cầu

- Java 21+, Maven 3.9+
- SQL Server 2019+ (hoặc MySQL 8)
- IDE (IntelliJ/Eclipse/VS Code) hoặc CLI

### 1) Clone & mở dự án

```bash
# HTTPS
git clone https://github.com/thanglb2005/Web_MyPham
cd Web_MyPham/OneShop
```

### 2) Tạo CSDL & dữ liệu mẫu (SQL Server)

- Tạo database `WebMyPham` và chạy file `DB.sql`

### 3) Cấu hình `application.properties`

```properties
# SQL Server
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=WebMyPham;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=YOUR_PASSWORD

# JPA
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Mail (Gmail SMTP)
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# Cloudinary (tuỳ chọn)
cloudinary.cloud-name=your-cloud
cloudinary.api-key=your-key
cloudinary.api-secret=your-secret

# MoMo
momo.partner.code=...
momo.access.key=...
momo.secret.key=...
momo.api.endpoint=https://payment.momo.vn/v2/gateway/api/create
momo.return.url=http://localhost:8080/payment/momo/return
momo.notify.url=http://localhost:8080/payment/momo/notify

# PayOS
payos.client-id=...
payos.api-key=...
payos.checksum-key=...
payos.return-url=http://localhost:8080/payos/return
payos.cancel-url=http://localhost:8080/payos/cancel
```

> MySQL: đổi `spring.datasource.url`/driver tương ứng và cập nhật schema nếu cần.

### 4) Build & Run

```bash
# Build
mvn clean install

# Chạy (Linux/macOS)
./mvnw spring-boot:run

# Chạy (Windows)
.mvnw.cmd spring-boot:run
```

Truy cập: http://localhost:8080

## 👥 Tài khoản demo (từ DB.sql)

| Vai trò  | Email              | Mật khẩu |
| -------- | ------------------ | -------- |
| Admin    | admin@mypham.com   | 123456   |
| Vendor A | vendor@mypham.com  | 123456   |
| Vendor B | vendor1@mypham.com | 123456   |
| Vendor C | vendor2@mypham.com | 123456   |
| Shipper  | shipper@mypham.com | 123456   |
| CSKH     | cskh@mypham.com    | 123456   |
| User     | user@gmail.com     | 123456   |

> Lưu ý: mật khẩu demo đã băm BCrypt tương ứng 123456 trong DB.sql

## 🔎 Hướng dẫn theo vai trò (Quick Start)

### Khách hàng

- Mua sắm: Trang chủ → chọn sản phẩm → Thêm giỏ hàng → Checkout
- Thanh toán:
  - COD: xác nhận → tạo đơn ngay
  - MoMo: chuyển sang MoMo → thanh toán → return/notify cập nhật đơn
  - Chuyển khoản (PayOS): tạo link → thanh toán → return/webhook cập nhật đơn
- Chat realtime: bật popup chat (header) hoặc vào `/chat?shopId={id}`

### Vendor

- Bảng điều khiển: `/vendor/dashboard?shopId={id}`
- Sản phẩm: `/vendor/products`
- Đơn hàng: `/vendor/orders`
- Khuyến mãi: `/vendor/promotions?shopId={id}` (tạo/sửa, validate theo loại)
- Chat với khách: `/admin/vendor-chat` (đã mở quyền cho VENDOR)

### Shipper

- Trang giao hàng: `/shipper/home`, nhận/giao đơn → cập nhật trạng thái

### CSKH

- Bảng chat: `/cskh/chat` hoặc `/cskh/vendor-chat` để liên hệ shop/vendor

### Admin

- Quản trị: `/admin/**` (VD: `/admin/brands`, `/admin/shippers-list`)
- Bảng chat: `/admin/vendor-chat`

## 💬 Chat realtime

- Giao thức: WebSocket + STOMP (SockJS fallback)
- Phòng: `shop-{shopId}-customer-{userId}`, `support-{userId|guestKey}`, phòng liaison CSKH↔Vendor
- Lưu lịch sử: bảng `chat_message` (content, type TEXT/IMAGE)
- Upload ảnh chat: `/api/chat/uploadImage`

## 💳 Thanh toán

- MoMo: `MoMoPaymentController`
  - Tạo thanh toán: `/payment/momo/create?orderId={id}`
  - Return: `/payment/momo/return` – cập nhật trạng thái đơn
  - Notify: `/payment/momo/notify`
- PayOS: `PayOSController`
  - Tạo link: `/payos/create-payment?orderId={id}`
  - Return/Cancel: `/payos/return`, `/payos/cancel`
  - Webhook: `/payos/webhook` (HMAC SHA256 `x-payos-signature`)

## 🧪 Testing

- Unit tests/Smoke: `mvn test`
- Integration (tối thiểu): `mvn verify`
- Kiểm thử thủ công theo vai trò (đề xuất):
  - Đặt hàng COD/MoMo/PayOS → kiểm tra cập nhật trạng thái
  - Áp khuyến mãi theo loại (PERCENTAGE/FIXED_AMOUNT/FREE_SHIPPING/BUY_X_GET_Y)
  - Chat: gửi TEXT/IMAGE, kiểm tra hiển thị ở cả popup và trang chat
  - Phân quyền: thử truy cập trang /admin,/vendor,/cskh khi không đủ quyền

## 🐞 Gỡ lỗi & lưu ý

- Nếu text chat không hiển thị: hard refresh, kiểm tra CSS scope của popup, hàm `escapeHtml`
- Nếu thanh toán không return: kiểm tra cấu hình `return-url`, `notify-url`, port
- Nếu lỗi JDBC: xác thực URL, user/pass DB và quyền SQL Server
- Nếu lỗi tĩnh (favicon.ico 404): không ảnh hưởng, có thể thêm favicon

## 📄 License

Dự án theo giấy phép MIT. Xem file `LICENSE` (hoặc bổ sung nếu chưa có).

## 👨‍💻 Nhóm phát triển

- Võ Thanh Sang — 23110301
- Lê Văn Chiến Thắng — 23110328
- Trịnh Nguyễn Hoàng Nguyên — 23110272
- Nguyễn Phước Khang — 23110236
