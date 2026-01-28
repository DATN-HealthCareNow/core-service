# HealthCare Now System - Architecture Diagrams (Updated)

## 1. High-Level System Architecture

Hệ thống sử dụng mô hình Microservices với API Gateway đóng vai trò điều phối trung tâm.

┌─────────────────────────────────────────────────────────────────┐
│ Mobile App (Expo) & Web (Next.js) │
└───────────────────────────┬─────────────────────────────────────┘
│ HTTP/HTTPS & WebSockets
┌───────────────────▼──────────────────┐
│ NGINX API GATEWAY (Port 80) │
│ - /api/v1/core* -> Core (8081)│
│ - /api/v1/iot* -> IoT (8082)│
│ - /api/v1/ai* -> AI (8000)│
│ - /api/v1/notif* -> Notif(8084)│
└───────────┬───────────┬────────────┬─┘
│ │ │
┌─────────▼─┐ ┌──────▼──┐ ┌──────▼──┐ ┌──────▼──┐
│ Core │ │ IoT │ │ AI │ │ Notif │
│ Service │ │ Service │ │ Service │ │ Service │
│ :8081 │ │ :8082 │ │ :8000 │ │ :8084 │
└─────┬─────┘ └────┬────┘ └────┬────┘ └────┬────┘
│ │ │ │
┌───────────┴────────────┴───────────┴───────────┴────────┐
│ Shared Infrastructure (Docker) │
│ ┌───────┐ ┌───────┐ ┌───────┐ ┌────────┐ ┌─────┐ ┌────┐ │
│ │core_db│ │iot_db │ │ai_db │ │notif_db│ │Redis│ │PGS │ │
│ │:27017 │ │:27018 │ │:27019 │ │:27020 │ │:6379│ │:5432 │ │
│ └───────┘ └───────┘ └───────┘ └────────┘ └─────┘ └─┬──┘ │
│ │ │
│ ┌──▼──┐ │
│ │PgAdm│ │
│ │:5050│ │
│ └─────┘ │
└─────────────────────────────────────────────────────────┘

## 2. Communication Strategy

Synchronous (Đồng bộ): Mobile/Web gọi qua Nginx Gateway bằng REST API.

Asynchronous (Bất đồng bộ): Các Service giao tiếp qua RabbitMQ cho các tác vụ nặng như:

- iot-service phát hiện té ngã -> Gửi Event -> notification-service gửi Email SOS.
- core-service cập nhật bữa ăn -> Gửi Event -> ai-service tính toán gợi ý dinh dưỡng.

## 3. Database Mapping Strategy

- **MongoDB**: Nested Data & High Volume
  - `core_db` (27017): Medical Records (Nested Documents)
  - `iot_db` (27018): Sensor Data (TimeSeries)
  - `ai_db` (27019): Analysis results
- **PostgreSQL**: Relational & Strict Consistency
  - `postgres` (5432):
    - DB `healthcare_auth`: Users, Roles, Subscriptions
    - DB `healthcare_catalog`: Medications, Foods (Many-to-Many)

## 4. Monitoring Stack

Sử dụng Prometheus để thu thập số liệu (Scraping) và Grafana để hiển thị.

[Services] --(/actuator/prometheus)--> [Prometheus:9090] --(Query)--> [Grafana:3000]

## 5. Tổng hợp các lưu ý "đắt giá" cho bạn và bạn BE

- Cổng Database (Ports): Trong docker-compose.yml, bạn phải ánh xạ (map) các port MongoDB khác nhau ra máy thật (Host) để không bị trùng: 27017, 27018, 27019, 27020.
- Shared UserID: Đây là "chìa khóa". Dù dữ liệu nằm ở 4 DB khác nhau, nhưng tất cả đều phải dùng chung user_id sinh ra từ core-service.
- Môi trường Mobile: Repo mobile-app sẽ gọi về http://<IP_MAY_TINH>:80/api/v1/.... Nhớ dùng IP máy tính thật thay vì localhost để iPhone có thể truy cập được qua Wifi.

## Bước tiếp theo

Bạn hãy cập nhật file này vào Repo devops-service. Sau đó, chúng ta nên xem xét file ENV_SETUP_GUIDE.md để chốt danh sách tên các biến môi trường, giúp bạn BE có thể code Service mà không cần hỏi lại bạn.
