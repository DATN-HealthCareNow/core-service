# HealthCare Now - Quick Start Guide

## 🚀 Khởi chạy trong 5 phút

### 1️⃣ Prerequisites

```bash
# Kiểm tra Docker
docker --version    # 20.10+
docker-compose --version  # 2.0+
```

### 2️⃣ Clone & Setup

```bash
git clone <repo-url>
cd devops-service
cp .env.example .env
```

### 3️⃣ Start Everything

```bash
# Cách 1: Dùng script (Linux/macOS)
chmod +x scripts/start-all.sh
./scripts/start-all.sh

# Cách 2: Dùng Makefile
make start

# Cách 3: Trực tiếp docker-compose
docker-compose up -d
```

### 4️⃣ Verify

```bash
# Check status
docker-compose ps

# Health check
make health
```

### 5️⃣ Access Services

| Service | URL | Credentials |
|---------|-----|---|
| **API Gateway** | http://localhost | - |
| **Grafana** | http://localhost:3000/grafana | admin / admin |
| **Prometheus** | http://localhost:9090 | - |
| **RabbitMQ** | http://localhost:15672 | guest / guest |

---

## 📚 Common Tasks

```bash
# View logs
make logs service=nginx

# Health check
make health

# Stop everything
make stop

# Restart all
make restart

# Backup databases
make backup

# Shell access
make shell-core-db
make shell-redis
```

---

## 🎯 Next Steps

1. **Build your services**
   ```bash
   cd ../core-service
   docker build -t myregistry/core-service .
   docker push myregistry/core-service
   ```

2. **Update docker-compose.yml**
   - Uncomment microservice sections
   - Update image names with your registry

3. **Deploy services**
   ```bash
   docker-compose up -d core-service
   docker-compose logs -f core-service
   ```

4. **Setup monitoring**
   - Open Grafana: http://localhost:3000/grafana
   - Add dashboards
   - Create alerts

---

## 📖 Full Documentation

- [README.md](README.md) - Project overview
- [DEPLOYMENT.md](DEPLOYMENT.md) - Detailed deployment guide
- [nginx/README.md](nginx/README.md) - API Gateway config
- [monitoring/README.md](monitoring/README.md) - Monitoring setup
- [scripts/README.md](scripts/README.md) - Script documentation

---

## ❓ Troubleshooting

**Containers not starting?**
```bash
docker-compose logs
docker-compose ps
```

**Port already in use?**
```bash
# Find process using port
lsof -i :80
# Kill it and restart
```

**Database initialization failed?**
```bash
./scripts/init-databases.sh
```

**Network connectivity issues?**
```bash
./scripts/network-diagnostics.sh
```

---

**Happy Deploying!** 🚀
