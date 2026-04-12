# Docker: Spring Boot + SQL Server (OneShop)

Chạy cả **ứng dụng** và **SQL Server** bằng một lệnh:

```bash
docker compose up -d --build
```

(Lần đầu nên có `--build`; các lần sau có thể chỉ `docker compose up -d`.)

---

## 1. Vì sao JDBC URL dùng hostname `sqlserver`?

Trong `docker-compose.yml`, service database có tên **`sqlserver`**. Docker Compose tạo một **mạng nội bộ** (`oneshop-net`); mỗi service có **DNS name** trùng tên service.

- Từ container **app**, chuỗi kết nối phải là  
  `jdbc:sqlserver://sqlserver:1433;...`  
  vì **127.0.0.1 / localhost bên trong container app** là chính container app, **không** phải máy chứa SQL Server.
- Từ **máy Windows/Mac của bạn** (trình duyệt, SSMS), bạn vẫn dùng `localhost:1433` nhờ **map port** `1433:1433`.

---

## 2. Cấu hình Spring Boot (biến môi trường)

Trong `application.properties`:

| Property | Env tương ứng (Spring Boot) | Ý nghĩa |
|----------|------------------------------|---------|
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` | JDBC URL (Docker dùng host `sqlserver`) |
| `spring.datasource.username` | `SPRING_DATASOURCE_USERNAME` | User SQL Server |
| `spring.datasource.password` | `SPRING_DATASOURCE_PASSWORD` | Mật khẩu |
| `app.base-url` | `APP_BASE_URL` | Link public (email, redirect) khi chạy Docker |

Giá trị sau dấu `:` trong `${VAR:default}` là **mặc định khi chạy local** không set env.

**Driver:** `com.microsoft.sqlserver.jdbc.SQLServerDriver` — dependency `com.microsoft.sqlserver:mssql-jdbc` (phiên bản do Spring Boot BOM quản lý trong `pom.xml`).

---

## 3. Xử lý “SQL Server chậm, app connect fail” (3 lớp)

1. **`healthcheck` trên service `sqlserver`**  
   Chỉ khi `sqlcmd` chạy `SELECT 1` thành công thì container được coi là **healthy**.

2. **Service `db-init`**  
   Chạy sau khi SQL Server healthy, thực thi `CREATE DATABASE WebMyPham` nếu chưa có (Hibernate `ddl-auto=update` **không** tạo database, chỉ tạo/cập nhật bảng).

3. **`spring.datasource.hikari.initialization-fail-timeout=-1`**  
   HikariCP **không thoát** khi chưa kết nối được lần đầu; sẽ **retry** cho đến khi SQL Server sẵn sàng (phòng trường hợp race nhỏ).

4. **`depends_on` có điều kiện**  
   - `app` chờ `sqlserver` **healthy** và `db-init` **completed successfully**.

Không cần `wait-for-it.sh` nếu dùng đủ các bước trên.

---

## 4. File trong repo

| File | Vai trò |
|------|---------|
| `Dockerfile` | Multi-stage: Maven build → JRE 21 chạy `app.jar` |
| `docker-compose.yml` | `sqlserver` + `db-init` + `app` |
| `.dockerignore` | Giảm context build, bỏ `target/`, `.git`, … |
| `.env.example` | Mẫu biến môi trường (copy thành `.env`) |

---

## 5. Database và ảnh: vì sao đăng nhập sai / không thấy sản phẩm?

**SQL Server trong Docker vẫn chạy bình thường.** Triệu chứng bạn mô tả thường do **dữ liệu khác** với lúc chạy app trực tiếp trên máy (SQL Server cũ + thư mục `upload/`).

### 5.1. Database `WebMyPham` trong container gần như mới

- `db-init` chỉ tạo **database rỗng**; Hibernate `ddl-auto=update` tạo **bảng** nhưng **không copy** user/sản phẩm từ instance SQL Server bạn dùng trước đây (`localhost` với mật khẩu `1`, v.v.).
- Đăng nhập bằng email/mật khẩu “đúng trên máy cũ” sẽ **sai** vì trong bảng `[user]` của Docker **không có** bản ghi đó (hoặc mật khẩu hash khác).
- Trang chủ **không có sản phẩm** vì bảng `products` (và bảng liên quan) **trống**.

**Chứng minh app đang đọc đúng SQL trong Docker** (đổi mật khẩu cho khớp `.env` nếu bạn đã đổi):

```powershell
docker exec oneshop-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "YourStrong!Passw0rd" -C -d WebMyPham -Q "SELECT COUNT(*) AS users FROM [user]; SELECT COUNT(*) AS products FROM products;"
```

Nếu hai số đều `0` (hoặc rất nhỏ), cần **đưa dữ liệu vào** (mục 5.3).

### 5.2. Ảnh sản phẩm (`/loadImage?imageName=...`)

- Ảnh lưu **trên đĩa** nằm dưới `upload/images`, `upload/brands`, `upload/providers` (theo `application.properties`).
- Image Docker **không** chứa thư mục đó từ máy bạn. `docker-compose.yml` đã **mount** `./upload/...` → `/app/upload/...`: hãy đảm bảo trên máy Windows, trong thư mục project OneShop, có đủ file ảnh như khi chạy local (copy từ bản backup project nếu thiếu).
- Nếu trong DB cột ảnh là **URL Cloudinary** (bắt đầu bằng `http`), app redirect tới URL đó — không cần file local.

### 5.3. Cách có lại dữ liệu như bản local

1. **Đăng ký tài khoản mới** trên site Docker rồi dùng admin/vendor để nhập dữ liệu (phù hợp demo trắng).
2. **Import từ SQL Server cũ** (khuyến nghị nếu cần đúng user/cũ):  
   - Dùng **SSMS** kết nối cả hai server (máy: `localhost,1433` user `sa` + mật khẩu trong `.env`, database `WebMyPham`).  
   - **Backup** DB cũ → **Restore** vào `WebMyPham` trong container (hoặc dùng Generate Scripts / Import Export Wizard).  
   - Lưu ý: restore `.bak` đôi khi ghi đè DB; nên làm trên máy dev hoặc export **chỉ dữ liệu** (INSERT) nếu bạn muốn giữ schema do Hibernate tạo.
3. Sau khi có DB, **copy** thư mục `upload` từ project đang có ảnh sang `D:\...\OneShop\upload\...`, rồi `docker compose up -d` (volume đã map sẵn).

### 5.4. Import từ `DB.sql`, `refund.sql`, `FlashSales.sql` (giống môi trường cũ)

**Thứ tự bắt buộc:** `DB.sql` → `refund.sql` → `FlashSales.sql` (refund phụ thuộc bảng trong DB.sql; flash sale phụ thuộc `products`, `[user]`, `orders`).

**Lưu ý quan trọng**

- `DB.sql` có `CREATE DATABASE WebMyPham` — trong Docker database **đã được tạo** (`db-init`). Script tự **bỏ 4 dòng đầu** (`CREATE DATABASE` / `GO` / `USE` / `GO`) rồi thêm lại `USE WebMyPham`.
- **Không** được để Spring Boot chạy **trước** khi import: Hibernate `ddl-auto=update` sẽ tạo bảng trước → `CREATE TABLE` trong `DB.sql` sẽ **lỗi “already exists”**.
- Cách sạch nhất: **xóa volume DB** rồi chỉ khởi động SQL Server + `db-init`, import, sau đó mới bật app.

**Các bước (Windows PowerShell)**

```powershell
cd D:\DoAnWebMyPham\Web_MyPham\OneShop

docker compose stop app
docker compose down -v
docker compose up -d sqlserver db-init
# Đợi vài chục giây: sqlserver healthy và db-init chạy xong (docker compose ps / logs)

.\docker\scripts\import-db.ps1

docker compose up -d app
```

**Git Bash / WSL / Linux**

```bash
cd /path/to/OneShop
chmod +x docker/scripts/import-db.sh
docker compose stop app 2>/dev/null || true
docker compose down -v
docker compose up -d sqlserver db-init
# đợi db-init xong
./docker/scripts/import-db.sh
docker compose up -d app
```

**Làm tay bằng SSMS (nếu không dùng script)**

1. Kết nối `localhost,1433` — `sa` — mật khẩu trong `.env` — database `WebMyPham`.
2. Mở `DB.sql`, **xóa hoặc comment 4 dòng đầu** (`CREATE DATABASE` … đến `GO` thứ hai), giữ phần còn lại, **Execute**.
3. Mở `refund.sql` → Execute.
4. Mở `FlashSales.sql` → Execute (chỉ chạy **một lần** trên DB sạch; chạy lại sẽ lỗi vì `CREATE TABLE` đã tồn tại).

**Mật khẩu demo trong `DB.sql`:** các user mẫu dùng cùng hash BCrypt (thường mật khẩu giống như lúc bạn tạo script - thử mật khẩu hay dùng khi dev).

**Nếu `import-db.ps1` báo lỗi parse (Missing parenthesis):** dùng bản script đã sửa (chỉ dùng dấu `-` ASCII trong chuỗi). Sau đó chạy lại từ bước `docker compose down -v` và `up -d sqlserver db-init` rồi `.\docker\scripts\import-db.ps1` **trước** `docker compose up -d app`.

**Chạy lại import từ đầu (khi lỗi font / chữ Việt trong DB):** script dùng `docker cp` + file UTF-8 để tránh hỏng Unicode khi pipe từ PowerShell. Làm đúng thứ tự:

```powershell
cd D:\DoAnWebMyPham\Web_MyPham\OneShop
docker compose stop app
docker compose down -v
docker compose up -d sqlserver db-init
# Doi ~30-60 giay (sqlserver Healthy, db-init Exited 0)
.\docker\scripts\import-db.ps1
# Xem dong "category_name" co dung tieng Viet khong
docker compose up -d --build app
```

Đảm bảo `DB.sql`, `refund.sql`, `FlashSales.sql` lưu **UTF-8** (VS Code góc phải thanh trạng thái: UTF-8).

### 5.5. Copy ảnh vào thư mục đã mount (cho người mới)

**Mount nghĩa là gì?** Trong `docker-compose.yml`, dòng `./upload/images:/app/upload/images` có nghĩa: thư mục **`upload\images` ngay trong project OneShop trên máy bạn** và thư mục **`/app/upload/images` bên trong container** là **cùng một nơi**. Bạn **không** cần `docker cp` vào container; chỉ cần bỏ file ảnh vào đúng folder trên Windows.

**Đường dẫn trên máy bạn (ví dụ đúng với ổ D):**

- Ảnh sản phẩm (tên file trùng với DB, ví dụ `sp001.jpg`):  
  `D:\DoAnWebMyPham\Web_MyPham\OneShop\upload\images\`
- Logo thương hiệu:  
  `D:\DoAnWebMyPham\Web_MyPham\OneShop\upload\brands\`
- Ảnh nhà cung cấp / provider:  
  `D:\DoAnWebMyPham\Web_MyPham\OneShop\upload\providers\`

**Các bước cụ thể:**

1. Mở File Explorer, vào `D:\DoAnWebMyPham\Web_MyPham\OneShop`.
2. Nếu **chưa có** thư mục `upload\images` (hoặc `brands`, `providers`): tạo mới (chuột phải → New → Folder).
3. Copy toàn bộ file ảnh từ bản backup / máy cũ (nơi bạn từng chạy OneShop không Docker) vào đúng thư mục trên. Tên file phải khớp với cột ảnh trong database (ví dụ `user.png`, tên file trong bảng sản phẩm).
4. Khởi động lại container app nếu cần: `docker compose restart app` (thường không bắt buộc; F5 trình duyệt là đủ).
5. Ảnh hiển thị qua URL dạng `http://localhost:8080/loadImage?imageName=tenfile.jpg` - nếu file không nằm trong `upload\images` thì sẽ 404.

**Lưu ý:** Sản phẩm chỉ lưu **URL Cloudinary** trong DB thì không cần file trong `upload\images`.

---

## 6. Lệnh thường dùng

```bash
# Tạo .env (khuyến nghị)
copy .env.example .env
# Chỉnh MSSQL_SA_PASSWORD trong .env nếu cần

# Build image app
docker compose build

# Chạy nền
docker compose up -d

# Xem log
docker compose logs -f app
docker compose logs -f sqlserver

# Dừng (giữ volume DB)
docker compose down

# Dừng và xóa volume (mất dữ liệu DB trong Docker)
docker compose down -v
```

---

## 7. Kiểm tra hoạt động

**Log app (đợi dòng tương tự “Started …Application”):**

```bash
docker compose logs -f app
```

**Health (từ máy host):**

```bash
curl -s http://localhost:8080/actuator/health
```

**SQL Server từ host (cần `sqlcmd` trên máy hoặc dùng SSMS):** server `localhost,1433`, user `sa`, password như trong `.env`.

**API / trang web:** mở `http://localhost:8080` (tùy Security, một số URL cần đăng nhập).

---

## 8. Lỗi thường gặp

| Hiện tượng | Hướng xử lý |
|------------|-------------|
| Đăng nhập sai / không có sản phẩm dù web lên được | DB Docker **mới, trống** — không phải lỗi kết nối. Xem **mục 5**; import dữ liệu hoặc đăng ký mới; copy thư mục `upload/`. |
| `platform linux/amd64` chậm trên Mac M1/M2 | Bình thường (emulation); có thể bỏ `platform` nếu dùng image hỗ trợ arm64 (SQL Server Linux chính thức thường cần amd64). |
| Healthcheck SQL Server fail | Kiểm tra log `sqlserver`; thử đổi mật khẩu đủ mạnh; xem trong container có `/opt/mssql-tools18/bin/sqlcmd` không (`docker exec -it oneshop-sqlserver bash`). |
| `db-init` exit 1 | Xem `docker compose logs db-init`; tạo thủ công DB `WebMyPham` bằng SSMS rồi `docker compose up -d` lại. |
| OAuth / Momo redirect sai | Set `APP_BASE_URL` trong `.env` trỏ tới URL public thật (không dùng `localhost` nếu test từ điện thoại mạng khác). |

---

## 9. (Bonus) Deploy lên Render / PaaS tương tự

- **Không nên** chạy **SQL Server trong container** trên nền free tier PaaS: image nặng, cần **volume bền**, **CPU/RAM cao**, khó đáp ứng SLA; nhiều nền tảng **không hỗ trợ** chạy DB container lâu dài như production.
- **Hướng thay thế phổ biến:**  
  - **Azure SQL Database**, **AWS RDS SQL Server**, hoặc nhà cung cấp **managed SQL Server**.  
  - App trên Render chỉ cần set **biến môi trường** `SPRING_DATASOURCE_*` trỏ tới host JDBC của dịch vụ managed (host **không** phải `sqlserver` mà là FQDN nhà cung cấp).

Docker Compose phù hợp **dev / demo / máy chủ riêng**; production SQL Server thường là **dịch vụ quản lý**, không gói chung container với app trên PaaS giới hạn.
