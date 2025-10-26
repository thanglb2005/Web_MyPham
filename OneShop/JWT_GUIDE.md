# 🔐 JWT Authentication Guide - OneShop

## 📋 Tổng quan

OneShop sử dụng **Hybrid JWT + Session** approach:
- **JWT** cho API authentication
- **Session** cho web page navigation
- **HttpOnly Cookies** để bảo mật

## 🚀 Quick Start

### 1. Login
```javascript
// Frontend
const result = await jwtAuth.login('admin@mypham.com', '123456');
if (result.success) {
    // Redirect tự động
}
```

### 2. Logout
```javascript
// Frontend
await jwtAuth.logout(); // Redirect về /login
```

### 3. Gọi API với JWT
```javascript
// Frontend
const response = await fetch('/api/products', {
    headers: {
        'Authorization': `Bearer ${jwtAuth.getAccessToken()}`
    }
});
```

## 🔧 API Endpoints

### Authentication APIs

| Endpoint | Method | Mô tả |
|----------|--------|-------|
| `/api/auth/login` | POST | Đăng nhập |
| `/api/auth/logout` | POST | Đăng xuất |
| `/api/auth/refresh` | POST | Refresh token |
| `/api/auth/validate` | POST | Validate token |
| `/api/auth/me` | GET | Lấy thông tin user |
| `/api/auth/authenticate` | POST | Set auth context |

### Login Request
```json
POST /api/auth/login
{
    "email": "admin@mypham.com",
    "password": "123456"
}
```

### Login Response
```json
{
    "access_token": "eyJhbGciOiJIUzM4NCJ9...",
    "refresh_token": "eyJhbGciOiJIUzM4NCJ9...",
    "token_type": "Bearer",
    "expires_in": 3600000,
    "user_info": {
        "user_id": 4,
        "email": "admin@mypham.com",
        "name": "Admin Mỹ Phẩm",
        "avatar": "user.png",
        "roles": ["ROLE_ADMIN"],
        "one_xu_balance": 0
    }
}
```

## 🍪 Cookies & Session

### Cookies được tạo
```http
Set-Cookie: jwt_access_token=eyJhbGciOiJIUzM4NCJ9...; HttpOnly; Path=/; Max-Age=3600
Set-Cookie: jwt_refresh_token=eyJhbGciOiJIUzM4NCJ9...; HttpOnly; Path=/; Max-Age=86400
Set-Cookie: JSESSIONID=ABC123; Path=/; HttpOnly
```

### Session Data
```java
// Server-side
session.setAttribute("user", userObject);
```

## ⏰ Token Expiration

| Token | Thời gian | Mục đích |
|-------|-----------|----------|
| **Access Token** | 1 giờ | API calls |
| **Refresh Token** | 24 giờ | Token refresh |

## 🔄 Authentication Flow

### 1. Login Flow
```
User Login → JWT API → Generate Tokens → Store in Cookies → Store in Session → Redirect
```

### 2. API Call Flow
```
Request → JWT Filter → Validate Token → Set Auth Context → Process Request
```

### 3. Web Navigation Flow
```
Navigate → JWT Filter → Read from Cookies → Set Session → Allow Access
```

## 🛠️ Frontend Usage

### JWT Auth Object
```javascript
// Available methods
jwtAuth.login(email, password)
jwtAuth.logout()
jwtAuth.getAccessToken()
jwtAuth.getRefreshToken()
jwtAuth.clearTokens()
jwtAuth.isAuthenticated()
jwtAuth.getCurrentUser()
```

### Navigation
```javascript
// JWT Navigation
window.jwtNavigation.navigateTo('/admin/home')
window.jwtNavigation.redirectBasedOnRole(userInfo)
```

## 🔒 Security Features

### HttpOnly Cookies
- ✅ JavaScript không đọc được
- ✅ Bảo vệ khỏi XSS
- ✅ Tự động gửi với requests

### JWT Validation
- ✅ Signature verification
- ✅ Expiration check
- ✅ Role-based access

### Session Management
- ✅ Server-side session
- ✅ Automatic cleanup on logout
- ✅ Security context

## 📱 Role-based Redirects

| Role | Redirect URL |
|------|--------------|
| `ROLE_ADMIN` | `/admin/home` |
| `ROLE_VENDOR` | `/vendor/my-shops` |
| `ROLE_CSKH` | `/cskh/chat` |
| `ROLE_SHIPPER` | `/shipper/home` |
| Other | `/` |

## 🚨 Error Handling

### Common Errors
```json
// Invalid credentials
{
    "error": "Invalid email or password"
}

// Token expired
{
    "error": "Token has expired"
}

// Access denied
{
    "error": "Access denied"
}
```

### Frontend Error Handling
```javascript
try {
    const result = await jwtAuth.login(email, password);
    if (!result.success) {
        showError(result.error);
    }
} catch (error) {
    showError('Network error');
}
```

## 🔧 Configuration

### application.properties
```properties
# JWT Configuration
jwt.secret=OneShopJWTSecretKey2025VerySecureAndLongEnoughForHS512Algorithm
jwt.access-token.expiration=3600000
jwt.refresh-token.expiration=86400000
jwt.issuer=OneShop
jwt.audience=OneShop-Users
```

### Security Config
```java
// Session management
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
)
```

## 📝 Best Practices

### ✅ Do's
- Sử dụng HttpOnly cookies
- Validate tokens trên server
- Clear tokens khi logout
- Handle token expiration
- Use HTTPS in production

### ❌ Don'ts
- Không lưu JWT trong localStorage
- Không gửi JWT qua URL
- Không ignore token expiration
- Không hardcode secrets

## 🎯 Testing

### Test Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@mypham.com","password":"123456"}'
```

### Test API với JWT
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 🔍 Debug

### Check JWT Status
```javascript
// Browser console
console.log('Token:', jwtAuth.getAccessToken());
console.log('User:', jwtAuth.getCurrentUser());
console.log('Authenticated:', jwtAuth.isAuthenticated());
```

### Server Logs
```
JWT token validation failed: Token has expired
Authentication context set successfully
User roles: [ROLE_ADMIN]
```

---

**🎉 JWT Authentication hoàn chỉnh cho OneShop!**
