# HealthCare Now - Core Service

## 📖 Giới thiệu

**Core Service** là microservice trung tâm của hệ thống **HealthCare Now System**. Service này chịu trách nhiệm quản lý người dùng, xác thực, hồ sơ bệnh nhân, hồ sơ y tế điện tử và theo dõi sức khỏe (như giấc ngủ, lượng nước uống, dinh dưỡng).

Được xây dựng với **Java 21**, **Spring Boot 3.2.2**, và thiết kế tối ưu hiệu năng và khả năng mở rộng sử dụng **MongoDB** (NoSQL).

## 🚀 Công nghệ sử dụng (Technology Stack)

- **Java 21**: Phiên bản LTS mới nhất.
- **Spring Boot 3.2.2**: Framework phát triển microservices.
- **MongoDB**: Cơ sở dữ liệu chính lưu trữ dạng Document (Users, Profiles, Medical Records).
- **Spring Security & JWT**: Cơ chế xác thực và phân quyền bảo mật.
- **Redis**: Caching (tùy chọn).
- **RabbitMQ**: Message broker cho giao tiếp bất đồng bộ (tùy chọn).
- **Maven**: Quản lý phụ thuộc (dependency management).

## ✨ Tính năng chính (Key Features)

- **Authentication**: Đăng ký, đăng nhập và cấp phát JWT (JSON Web Tokens).
- **User Management**: Quản lý người dùng và phân quyền (USER, ADMIN).
- **Patient Profiles**: Quản lý thông tin chi tiết bệnh nhân (nhân khẩu học, cài đặt quyền riêng tư).
- **Medical Records**: Lưu trữ và quản lý bệnh án điện tử, mã ICD, ghi chú lâm sàng.
- **Health Tracking**:
  - Theo dõi lượng nước uống (Water Intake).
  - Theo dõi giấc ngủ (Sleep Session).
  - Nhật ký dinh dưỡng (Nutrition/Meal logging).
- **NoSQL Architecture**: Thiết kế schema linh hoạt và tối ưu tốc độ truy vấn với MongoDB.

## 🛠️ Yêu cầu hệ thống (Requirements)

- **Java JDK 21**
- **Docker** & **Docker Compose** (để chạy MongoDB, Redis, RabbitMQ)
- **Maven** (đã bao gồm wrapper `mvnw`)

## ⚙️ Cấu hình (Configuration)

Service sử dụng `application.yml` và biến môi trường. Tạo file `.env` tại thư mục gốc hoặc set environment variables:

```properties
# Server Configuration
CORE_SERVICE_PORT=8081

# MongoDB Configuration
MONGO_HOST=localhost
MONGO_CORE_PORT=27017
MONGO_CORE_DB=healthcare_core
# CLOUD_MONGO_URI=mongodb+srv://... (Sử dụng cho profile 'cloud')

# Security (JWT)
JWT_SECRET=your_super_secret_key_change_this_running_production
JWT_EXPIRATION=86400000

# Redis (Optional)
REDIS_HOST=localhost
REDIS_PORT=6379

# RabbitMQ (Optional)
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest
```

## 🏗️ Cài đặt & Chạy ứng dụng

### 1. Clone repository

```bash
git clone <repository_url>
cd core-service
```

### 2. Khởi động Infrastructure (Database)

Đảm bảo MongoDB đang chạy (sử dụng Docker Compose từ repository hạ tầng hoặc chạy riêng lẻ):

```bash
# Ví dụ chạy MongoDB bằng Docker
docker run -d -p 27017:27017 --name core_db mongo:latest
```

### 3. Build ứng dụng

```bash
./mvnw clean install
```

### 4. Chạy ứng dụng

Dùng profile `dev` để chạy local:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Hoặc chạy trực tiếp file JAR sau khi build:

```bash
java -jar target/core-service-0.0.1-SNAPSHOT.jar
```

## 📚 API Documentation

API tuân thủ chuẩn RESTful.

- **Base URL**: `http://localhost:8081/api/v1`
- **Health Check**: `http://localhost:8081/actuator/health`

**Các Endpoints chính:**

- `POST /api/v1/auth/register` - Đăng ký người dùng mới
- `POST /api/v1/auth/login` - Đăng nhập lấy Token
- `GET /api/v1/users/me` - Lấy thông tin profile hiện tại
- `POST /api/v1/medical-records` - Tạo bệnh án mới
- `GET /api/v1/tracking/water` - Lấy lịch sử uống nước

## 🧪 Testing

Chạy unit tests và integration tests:

```bash
./mvnw test
```

## 📂 Cấu trúc dự án

```
core-service/
├── src/main/
│   ├── java/com/healthcarenow/core/
│   │   ├── config/          # Cấu hình (Security, Mongo, etc.)
│   │   ├── controller/      # API Endpoints
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── model/           # MongoDB Documents
│   │   │   └── mongo/       # (User, MedicalRecord,...)
│   │   ├── repository/      # Data Access Layer (MongoRepository)
│   │   │   └── mongo/
│   │   ├── service/         # Business Logic
│   │   └── utils/           # Tiện ích
│   └── resources/
│       └── application.yml  # File cấu hình chính
├── pom.xml                  # Maven dependencies
└── README.md                # Tài liệu dự án
```

---

**HealthCare Now System** - _Core Backend Service_
