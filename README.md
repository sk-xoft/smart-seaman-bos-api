# Smart Seaman Bos API

Spring Boot 2.6.2 REST API backend for the Smart Seaman mobile application.

---

## Prerequisites

- Java 11+
- Maven 3.6+ (or use included `./mvnw`)
- MySQL 8.0+ (for local DB option)
- Docker (for container deployment)

---

## Run Locally (Development)

### Option A — ใช้ Dev DB บน DigitalOcean (แนะนำ ไม่ต้องติดตั้ง MySQL)

1. แก้ไขไฟล์ `src/main/resources/application-local.properties`
   Uncomment ส่วน Dev DB และ comment ส่วน Local MySQL:

   ```properties
   smart.seaman.datasource.url=jdbc:mysql://dev-smartseaman-db-01-do-user-7722588-0.b.db.ondigitalocean.com:25060/dev-seaman?autoReconnect=true&useSSL=false
   smart.seaman.datasource.username=dev-seaman-user
   smart.seaman.datasource.password=
   ```

2. รัน application:

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

3. API พร้อมใช้งานที่: `http://localhost:8081`
   Swagger UI: `http://localhost:8081/smart-seaman-swagger`

---

### Option B — ใช้ Local MySQL

1. สร้าง database ใน MySQL:

   ```sql
   CREATE DATABASE smartseaman CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. Import SQL views จาก `src/main/resources/SQL/CreateView.sql`

3. แก้ไขไฟล์ `src/main/resources/application-local.properties`
   ตั้งค่า username/password ให้ตรงกับ local MySQL ของคุณ:

   ```properties
   smart.seaman.datasource.url=jdbc:mysql://localhost:3306/smartseaman?autoreconnect=true
   smart.seaman.datasource.username=root
   smart.seaman.datasource.password=P@ssw0rd
   ```

4. รัน application:

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

5. API พร้อมใช้งานที่: `http://localhost:8081`

---

## Build & Run with Docker

### 1. Build JAR

```bash
./mvnw clean package -DskipTests
```

### 2. Build Docker Image

```bash
docker build -t xoftspace/smart-seaman-bos-api:latest .
```

### 3. Run Container

```bash

mvn clean package -DskipTests && docker build -t xoftspace/smart-seaman-bos-api:1.0.0 .


```

### Run Docker with configuration

#### Docker build on local

```bash

docker run --name smart-seaman-bos-api -d \
  --env-file /Users/sarunyook/workspaces/xoftspace/smart-seaman/source_code/config/bos-api/non-prod/.env \
  -v /Users/sarunyook/workspaces/xoftspace/smart-seaman/source_code/config/bos-api/prod/smart-seaman-firebase.json:/app/firebase.json \
  -e FCM_CREDENTIAL_FILE=/app/firebase.json \
  -it -p 20000:8080/tcp \
  xoftspace/smart-seaman-bos-api:1.0.0
  

```

#### Docker build on prod
```bash

docker run --name smart-seaman-bos-api-0.6 -d \
  --env-file /home/ssmuser/apps/config/bos-api/prod/.env \
  -v /home/ssmuser/apps/config/bos-api/prod/smart-seaman-firebase.json:/app/firebase.json \
  -v /home/ssmuser/apps-logs-service/smart-seaman-bos-api/logs:/apps-logs-service/smart-seaman-bos-api/logs \
  -e FCM_CREDENTIAL_FILE=/app/firebase.json \
  -it -p 20000:8080/tcp \
  xoftspace/smart-seaman-bos-api:0.6

```

> **Note:** Container ใช้ config จาก `application.properties` (prod profile) โดย default
> Port mapping: host `30000` → container `8080`

### ดู Logs

```bash
docker logs -f smart-seaman-bos-api
```

### หยุด / ลบ Container

```bash
docker stop smart-seaman-bos-api
docker rm smart-seaman-bos-api
```

---

## Environment Profiles

| Profile | คำสั่ง | Database |
| ------- | ------ | -------- |
| `local` | `-Dspring-boot.run.profiles=local` | Local MySQL หรือ Dev DB |
| `prod` (default) | (ไม่ต้องระบุ) | Production DB บน DigitalOcean |

---
****
## Run Tests

```bash
# รัน tests ทั้งหมด
./mvnw test

# รัน test class เฉพาะ
./mvnw test -Dtest=ClassName
```
