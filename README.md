# HealthCare Now System - DevOps Infrastructure Repository

## 📋 Mục đích Repository

Repository này chứa toàn bộ cấu hình DevOps và infrastructure cho hệ thống **HealthCare Now**, bao gồm:

- **Orchestration**: Docker Compose để điều phối 4 microservices + 4 databases + message broker
- **API Gateway**: Nginx làm cổng vào duy nhất (port 80/443)
- **Database Management**: Hybrid Architecture (MongoDB for Unstructured Data + PostgreSQL for Structured Data)
- **Monitoring**: Prometheus + Grafana để theo dõi performance
- **Cache & Messaging**: Redis + RabbitMQ

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     Mobile/Web Client                       │
└─────────────────────────────────┬───────────────────────────┘
                                  │
                          Port 80 / 443
                                  │
                    ┌─────────────▼──────────────┐
                    │   NGINX (API Gateway)      │
                    │  - Routing Rules           │
                    │  - Load Balancing          │
                    │  - SSL/TLS Termination     │
                    └─────────────┬──────────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────┼─────────────────────────┐
        │                         │                         │                         │
        ▼                         ▼                         ▼                         ▼
   ┌─────────┐            ┌─────────────┐          ┌──────────┐              ┌──────────────┐
   │   Core  │            │    IoT      │          │    AI    │              │ Notification │
   │ Service │            │  Service    │          │ Service  │              │   Service    │
   │ :8081   │            │  :8082      │          │  :8000   │              │   :8084      │
   └────┬────┘            └─────┬───────┘          └────┬─────┘              └─────┬────────┘
        │                       │                       │                         │
        └───────────────────────┼───────────────────────┼─────────────────────────┘
                                │
            ┌───────────────────┼───────────────────┐
            │                   │                   │
            ▼                   ▼                   ▼
      ┌──────────┐        ┌──────────┐       ┌──────────┐
      │  RabbitMQ│        │  Redis   │       │ 4 MongoDB│
      │  :5672   │        │ :6379    │       │ Instances│
      └──────────┘        └──────────┘       └──────────┘
            │                   │                   │
            └───────────────────┼───────────────────┘
                                │
            ┌───────────────────┼───────────────────┐
            │                   │                   │
            ▼                   ▼                   ▼
      ┌──────────┐      ┌────────────┐      ┌──────────┐
      │Prometheus│      │  Grafana   │      │ Log/Alert│
      │ :9090    │      │  :3000     │      │  System  │
      └──────────┘      └────────────┘      └──────────┘
```

---

## 📦 Services & Components

### Databases (4 Logical DBs)

| Database            | Container Name  | Port  | Purpose                                     |
| ------------------- | --------------- | ----- | ------------------------------------------- |
| **Core DB**         | core_db         | 27017 | Người dùng, bệnh nhân, hồ sơ                |
| **IoT DB**          | iot_db          | 27018 | Dữ liệu cảm biến, thiết bị                  |
| **AI DB**           | ai_db           | 27019 | Kết quả phân tích từ AI                     |
| **Notification DB** | notification_db | 27020 | Lưu log/template thông báo                  |
| **PostgreSQL**      | postgres        | 5432  | Users, Roles, Finance, Catalog (Structured) |

75

### Infrastructure Services

| Service               | Port        | Purpose                 |
| --------------------- | ----------- | ----------------------- |
| **Nginx**             | 80, 443     | API Gateway             |
| **PgAdmin**           | 5050        | Web UI for PostgreSQL   |
| **Postgres Exporter** | 9187        | Metrics for Prometheus  |
| **RabbitMQ**          | 5672, 15672 | Message Broker          |
| **Redis**             | 6379        | Cache & Session Storage |
| **Prometheus**        | 9090        | Metrics Collection      |
| **Grafana**           | 3000        | Visualization Dashboard |

---

## 🚀 Quick Start

### Prerequisites

```bash
# Kiểm tra Docker & Docker Compose
docker --version
docker-compose --version
```

### 1. Clone Repository

```bash
git clone https://github.com/your-org/devops-service.git
cd devops-service
```

### 2. Cấu hình Environment

```bash
# Copy file mẫu
cp .env.example .env

# Chỉnh sửa nếu cần
vi .env
```

### 3. Khởi động hệ thống

```bash
# Cách 1: Dùng script (Recommended)
chmod +x scripts/start-all.sh
./scripts/start-all.sh

# Cách 2: Dùng docker-compose trực tiếp
docker-compose up -d
```

### 4. Kiểm tra trạng thái

```bash
./scripts/health-check.sh
# hoặc
docker-compose ps
```

### 5. Khởi tạo Database (nếu lần đầu)

```bash
chmod +x scripts/init-databases.sh
./scripts/init-databases.sh
```

---

## 🐘 PostgreSQL Management

### Accessing PgAdmin

1. Truy cập: [http://localhost:5050](http://localhost:5050)
2. Login: Email/Password defined in `.env` (Default: `admin@healthcare.com` / `admin`)
3. **Connect to Server**:
   - Host: `postgres`
   - Username: `postgres` (or as defined in .env)
   - Password: `postgres` (or as defined in .env)

### Backup & Restore

Backup toàn bộ data PostgreSQL:

```bash
./scripts/backup-databases.sh
```

Restore data (cần copy file dump vào container hoặc mount volume):

```bash
# PostgreSQL
cat backup_file.sql | docker exec -i postgres psql -U postgres -d healthcare_auth
```

> **Note on Migration**: Việc chuyển đổi dữ liệu từ MongoDB sang PostgreSQL (nếu có) đòi hỏi quy trình ETL (Extract-Transform-Load) riêng biệt và không được bao gồm trong các script backup/restore này.

---

## 📊 Accessing Services

| Service                 | URL                           | Credentials        |
| ----------------------- | ----------------------------- | ------------------ |
| **API Gateway**         | http://localhost              | N/A                |
| **Grafana**             | http://localhost:3000/grafana | admin / admin      |
| **Prometheus**          | http://localhost:9090         | N/A                |
| **RabbitMQ Management** | http://localhost:15672        | guest / guest      |
| **MongoDB**             | mongodb://localhost:27017     | (depends on setup) |
| **Redis CLI**           | redis://localhost:6379        | N/A                |

---

## 🔌 API Routing (Nginx Configuration)

Mobile App chỉ cần gọi port 80/443 của Nginx, tất cả routing đều tự động:

```
GET  /api/v1/auth/*        → Core Service (8081)
POST /api/v1/user/*        → Core Service (8081)
GET  /api/v1/patient/*     → Core Service (8081)

GET  /api/v1/tracking/*    → IoT Service (8082)
POST /api/v1/device/*      → IoT Service (8082)
GET  /api/v1/sensor/*      → IoT Service (8082)

GET  /api/v1/analysis/*    → AI Service (8000)
POST /api/v1/predict/*     → AI Service (8000)
GET  /api/v1/report/*      → AI Service (8000)

POST /api/v1/notify/*      → Notification Service (8084)
GET  /api/v1/alerts/*      → Notification Service (8084)

GET  /grafana/*            → Grafana (3000)
GET  /health               → Health Check
```

---

## 🛠️ Useful Commands

```bash
# ===== START/STOP =====
./scripts/start-all.sh         # Khởi động tất cả
./scripts/stop-all.sh          # Dừng tất cả
./scripts/restart-all.sh       # Khởi động lại

# ===== MONITORING =====
./scripts/health-check.sh      # Kiểm tra trạng thái
docker-compose logs -f         # Xem logs realtime
docker-compose logs <service>  # Xem log của service cụ thể

# ===== DATABASE =====
./scripts/init-databases.sh    # Khởi tạo databases

# ===== DOCKER =====
docker-compose ps             # Danh sách containers
docker-compose exec core_db mongosh   # Kết nối MongoDB
docker-compose exec redis redis-cli   # Kết nối Redis

# ===== BUILD & DEPLOY =====
docker-compose build           # Build lại các images
docker-compose pull            # Pull latest images
```

---

### 5. **Monitoring dengan Grafana**

#### Cách truy cập

1. Mở browser: http://localhost:3000/grafana
2. Login: admin / admin
3. Datasource tự động là Prometheus

#### Pre-built Dashboards (Có sẵn!)

✅ **3 Dashboard đã được tạo sẵn:**

- **healthcare-metrics.json** - CPU, Memory, Request Rate, Response Time
- **service-status.json** - Real-time status của tất cả services
- **jvm-performance.json** - JVM metrics (memory, threads, GC, requests)

Các dashboard sẽ tự động load khi Grafana khởi động!

> 💡 **Tip:** Để monitor PostgreSQL chuyên sâu, bạn có thể Import thêm dashboard ID **9628** (PostgreSQL Database) từ thư viện Grafana.

#### Cách xem Metrics

Prometheus tự động collect metrics từ các service thông qua endpoint `/actuator/prometheus`. Bạn cần:

1. Thêm Spring Boot Actuator vào các Java service
2. Cấu hình Spring Boot để expose Prometheus metrics
3. Đảm bảo service URL trong prometheus.yml là đúng

#### Tạo Dashboard Custom

Xem hướng dẫn tại: https://grafana.com/docs/grafana/latest/getting-started/build-first-dashboard/

#### 🎯 Demo Strategy (Cực quan trọng!)

Khi phòng vấ đề xuất:

1. Chạy các hành động liên tục (import data, search, analysis)
2. Mở Grafana dashboard
3. Show biểu đồ CPU/RAM tăng lên theo thời gian thực
4. Giải thích: "Prometheus lấy metrics mỗi 15 giây từ `/actuator/prometheus`"
5. Thầy cô sẽ rất ấn tượng! 📊

---

## 🐛 Troubleshooting

### Service không khởi động?

```bash
# Xem chi tiết lỗi
docker-compose logs <service_name>

# Kiểm tra port có bị chiếm?
netstat -ano | findstr :<port>  # Windows
lsof -i :<port>                 # macOS/Linux
```

### Database connection failed?

```bash
# Đảm bảo network name đúng
docker network ls
docker network inspect healthcare-network

# Kiểm tra MongoDB
docker-compose exec core_db mongosh
```

### Grafana không hiển thị metrics?

1. Kiểm tra Prometheus UI: http://localhost:9090
2. Vào Status → Targets để xem service status
3. Đảm bảo service có `/actuator/prometheus` endpoint
4. Kiểm tra firewall/network connectivity

---

## 📂 Project Structure

```
devops-service/
├── docker-compose.yml              # Orchestration file
├── .env.example                    # Environment variables template
├── README.md                       # This file
│
├── nginx/                          # API Gateway
│   ├── Dockerfile
│   ├── conf.d/
│   │   └── app.conf              # Routing rules
│   └── html/
│       └── index.html            # Health check page
│
├── monitoring/                     # Monitoring system
│   ├── prometheus/
│   │   └── prometheus.yml        # Prometheus config
│   └── grafana/
│       ├── datasources/          # Datasource configs
│       └── provisioning/         # Dashboard provisioning
│
├── scripts/                        # Automation scripts
│   ├── start-all.sh
│   ├── stop-all.sh
│   ├── restart-all.sh
│   ├── init-databases.sh
│   └── health-check.sh
│
└── .gitignore
```

---

## 🔐 Security Notes

### For Production

1. Thay đổi GRAFANA_ADMIN_PASSWORD
2. Thay đổi RABBITMQ_DEFAULT_USER/PASSWORD
3. Cấu hình SSL/TLS cho Nginx
4. Bật authentication cho Prometheus & Grafana
5. Giới hạn database access từ authorized IPs
6. Sử dụng secrets management (Docker Secrets, Vault, etc.)

---

## 🔄 CI/CD Integration

Để integrate vào CI/CD pipeline:

```bash
# 1. Build services
docker build -t your-registry/core-service ./core-service
docker build -t your-registry/iot-service ./iot-service
docker build -t your-registry/ai-service ./ai-service

# 2. Push to registry
docker push your-registry/core-service
docker push your-registry/iot-service
docker push your-registry/ai-service

# 3. Update docker-compose.yml với new image tags
# 4. Deploy
docker-compose up -d
```

---

## 📞 Support & Documentation

- [Docker Documentation](https://docs.docker.com)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)
- [Nginx Proxy Configuration](https://nginx.org/en/docs/)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Dashboards](https://grafana.com/docs/grafana/latest/)
- [MongoDB Guide](https://docs.mongodb.com/)
- [RabbitMQ Guide](https://www.rabbitmq.com/documentation.html)

---

## 👥 Contributors

- DevOps Team
- Backend Team
- Infrastructure Team

---

## 📝 License

Specify your license here

---

## 🎯 Next Steps

1. Clone các repo khác (core-service, iot-service, ai-service)
2. Build Docker images cho mỗi service
3. Update docker-compose.yml với image names
4. Kiểm tra cấu hình Prometheus cho các service
5. Tạo Grafana dashboards custom
6. Deploy lên server

---

**Maintained by DevOps Team** ❤️
