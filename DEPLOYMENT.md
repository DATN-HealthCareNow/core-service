# Deployment Guide

## 🎯 Pre-Deployment Checklist

- [ ] Docker & Docker Compose installed (version 20.10+)
- [ ] All microservices built and pushed to registry
- [ ] Environment variables configured in .env
- [ ] Database backups created
- [ ] SSL certificates ready (if using HTTPS)
- [ ] System requirements met (4GB RAM minimum)

---

## 📋 Deployment Stages

### Stage 1: Infrastructure Setup

```bash
# 1. Navigate to devops-service directory
cd devops-service

# 2. Create environment file
cp .env.example .env

# 3. Edit .env with your values
vi .env

# 4. Verify configuration
cat .env | grep -v '^#'
```

### Stage 2: Database Initialization (per service DB)

```bash
# 1. Start infrastructure (databases, cache, broker)
./scripts/start-all.sh

# 2. Check status
./scripts/health-check.sh

# 3. Initialize databases cho từng DB (core_db, iot_db, ai_db, notification_db)
#    Script này tạo collection/index riêng cho từng service
./scripts/init-databases.sh

# 4. Verify all containers running
docker-compose ps
```

### Stage 3: Deploy Microservices (ports chuẩn)

Ports chuẩn đã thống nhất:
- core-service: 8081
- iot-service: 8082
- ai-service: 8000 (FastAPI)
- notification-service: 8084

Once you have built and pushed your microservice images:

```bash
# 1. Update docker-compose.yml with service images
# Uncomment the microservice sections and update image names

# 2. Deploy services
docker-compose up -d core-service
docker-compose up -d iot-service
docker-compose up -d ai-service
docker-compose up -d notification-service

# 3. Verify deployment
docker-compose logs -f core-service
```

### Deployment Flow (tham khảo nhanh)

```
Stage 1: Env & infra files (.env, compose) ready
Stage 2: Start infra → ./scripts/start-all.sh
Stage 2b: Init DB per service → ./scripts/init-databases.sh
Stage 3: Bring up gateway (nginx) + monitoring (prometheus, grafana)
Stage 4: Deploy services (core 8081, iot 8082, ai 8000, notif 8084)
Stage 5: Verify health → ./scripts/health-check.sh + Grafana targets
Stage 6: Demo/operation → run troubleshooting scenarios if needed
```

### Stage 4: Setup Monitoring

```bash
# 1. Grafana automatically starts
#    Access: http://your-server:3000/grafana
#    Login: admin / admin

# 2. Add Datasource (if not auto-provisioned)
#    - Grafana → Configuration → Datasources
#    - Name: Prometheus
#    - URL: http://prometheus:9090

# 3. Import dashboards
#    - Pre-built: Search "Node Exporter Full" (ID: 1860)
#    - Custom: Create your own for business metrics
```

---

## 🚀 Deployment Strategies

### Strategy 1: All-in-One (Development)

```bash
# Everything in one command
./scripts/start-all.sh
```

Pros:
- Simple, fast
- Good for development

Cons:
- Single failure point
- Not production-ready

---

### Strategy 2: Gradual Rollout (Recommended)

```bash
# Step 1: Infrastructure only
docker-compose up -d core_db iot_db ai_db notification_db
docker-compose up -d redis rabbitmq
./scripts/init-databases.sh

# Step 2: Wait and verify
./scripts/health-check.sh

# Step 3: Monitoring
docker-compose up -d prometheus grafana

# Step 4: Gateway
docker-compose up -d nginx

# Step 5: Services (one by one)
docker-compose up -d core-service
# Wait for it to stabilize
docker-compose logs -f core-service

docker-compose up -d iot-service
docker-compose logs -f iot-service

docker-compose up -d ai-service
docker-compose logs -f ai-service

docker-compose up -d notification-service
docker-compose logs -f notification-service

# Final check
./scripts/health-check.sh
```

Pros:
- Can catch issues early
- Rollback easier
- Better monitoring

---

## 🔄 Updating Services

### Blue-Green Deployment

> Lưu ý: Blue-Green cần chạy song song 2 cụm nên tốn RAM (≥4GB). Đề xuất áp dụng ở Production; khi demo trên máy cá nhân có thể chuyển sang Rolling Update để tiết kiệm tài nguyên.

```bash
# 1. Build new image
docker build -t registry.example.com/core-service:v2.0 .

# 2. Push to registry
docker push registry.example.com/core-service:v2.0

# 3. Create new container (green)
docker-compose up -d core-service-green

# 4. Test green deployment
curl http://localhost:8081/health

# 5. Switch traffic (update nginx config)
# Modify nginx/conf.d/app.conf to point to green

# 6. Reload nginx
docker-compose exec nginx nginx -s reload

# 7. Keep blue running for quick rollback (30 minutes)

# 8. Stop blue after verification
docker-compose down core-service-blue
```

### Rolling Update

```bash
# Update docker-compose.yml with new image tag
sed -i 's/core-service:v1.0/core-service:v2.0/g' docker-compose.yml

# Pull new image
docker-compose pull core-service

# Recreate container
docker-compose up -d core-service

# Verify
docker-compose logs -f core-service
```

### Health Check Logic (Gateway)

```nginx
# NGINX kiểm tra sức khỏe trước khi nhận traffic
location /api/v1/core {
  proxy_pass http://core-service:8081;
  health_check interval=5s fails=3 passes=2;
}

location /api/v1/iot {
  proxy_pass http://iot-service:8082;
  health_check interval=5s fails=3 passes=2;
}

location /api/v1/ai {
  proxy_pass http://ai-service:8000;
  health_check interval=5s fails=3 passes=2;
}

location /api/v1/notif {
  proxy_pass http://notification-service:8084;
  health_check interval=5s fails=3 passes=2;
}
```

### Troubleshooting Demo Scenario

1) Hệ thống đang chạy ổn định.
2) Giả lập sự cố: `docker stop core-service`.
3) Mở Grafana để trình chiếu panel "Service Down" chuyển sang đỏ.
4) Khôi phục: `docker-compose up -d core-service` và cho thấy trạng thái trở lại "Up".

---

## 🔐 Production Deployment

### SSL/TLS Setup

```bash
# 1. Generate certificates
mkdir -p nginx/ssl
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout nginx/ssl/key.pem \
  -out nginx/ssl/cert.pem

# 2. Update nginx config (uncomment SSL section)
vi nginx/conf.d/app.conf

# 3. Restart nginx
docker-compose restart nginx
```

### Environment Security

```bash
# .env file should contain sensitive data
# Make sure it's in .gitignore
echo ".env" >> .gitignore

# Set restrictive permissions
chmod 600 .env

# Never commit .env to git
git rm --cached .env
```

### Database Authentication

```bash
# MongoDB with authentication
MONGO_INITDB_ROOT_USERNAME: admin
MONGO_INITDB_ROOT_PASSWORD: $(openssl rand -base64 32)

# Update connection string
mongodb://admin:password@core_db:27017/healthcare_core?authSource=admin
```

### Backup Strategy

```bash
# Daily backup at 2 AM
0 2 * * * /path/to/devops-service/scripts/backup-databases.sh

# Weekly full backup
0 2 * * 0 /path/to/devops-service/scripts/backup-databases.sh && \
  tar czf /backups/full_backup_$(date +\%Y\%m\%d).tar.gz /backups/

# Verify backups exist
ls -lah /backups/
```

---

## 📊 Post-Deployment Verification

### System Check

```bash
# 1. Container status
docker-compose ps

# 2. Resource usage
docker stats --no-stream

# 3. Network connectivity
./scripts/network-diagnostics.sh

# 4. Service health
./scripts/health-check.sh

# 5. Logs review
./scripts/view-logs.sh nginx
./scripts/view-logs.sh prometheus
```

### Application Check

```bash
# 1. API Gateway
curl http://localhost/health

# 2. Auth Service
curl http://localhost/api/v1/auth/health

# 3. Database connectivity
docker-compose exec core_db mongosh --eval "db.runCommand('ping')"

# 4. Redis connectivity
docker-compose exec redis redis-cli ping

# 5. RabbitMQ connectivity
curl http://localhost:15672/api/overview
```

### Monitoring Check

```bash
# 1. Prometheus targets
curl http://localhost:9090/api/v1/targets

# 2. Grafana dashboards
curl http://localhost:3000/api/search

# 3. Active metrics
curl http://localhost:9090/api/v1/query?query=up
```

---

## 🆘 Rollback Procedure

### Quick Rollback

```bash
# 1. Stop current deployment
docker-compose down

# 2. Checkout previous version
git checkout HEAD~1

# 3. Restore from backup
./scripts/backup-databases.sh  # Create backup first
mongorestore --archive=/backups/previous_backup.archive

# 4. Restart with previous config
./scripts/start-all.sh
```

### Service-Level Rollback

```bash
# 1. Revert docker-compose.yml
git checkout docker-compose.yml

# 2. Rebuild old image
docker-compose pull core-service

# 3. Restart service
docker-compose up -d core-service

# 4. Verify
docker-compose logs -f core-service
```

---

## 📈 Scaling

### Horizontal Scaling (Add more instances)

```yaml
# docker-compose.yml
services:
  core-service:
    image: registry.example.com/core-service:latest
    deploy:
      replicas: 3
    environment:
      - INSTANCE_ID=1
```

Update nginx config for load balancing:

```nginx
upstream core_service {
    least_conn;
  # Với deploy.replicas=3, Docker DNS quay vòng giữa các replica
  server core-service:8081;
}
```

### Vertical Scaling (More resources)

```yaml
services:
  core-service:
    environment:
      JAVA_OPTS: -Xmx2g -Xms2g
    mem_limit: 2.5gb
    cpus: "2.0"
```

---

## 📞 Support & Monitoring

### Set up alerting

- Prometheus Alert Rules
- Grafana Alerts
- Email/Slack notifications
- Uptime monitoring services

### Documentation

- Keep deployment logs
- Document custom configurations
- Maintain runbook for common issues
- Regular disaster recovery drills

---

**Maintained by DevOps Team** ❤️
