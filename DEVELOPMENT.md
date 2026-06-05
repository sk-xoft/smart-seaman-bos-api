# Local Development Guide

## Prerequisites

- Java 11
- Maven (or use `./mvnw` wrapper)
- Docker (for MySQL)
- VS Code หรือ Terminal

---

## 1. Setup Environment Variables

```sh
cp .env.example .env
```

แก้ไขค่าใน `.env`:

| Variable | Description | หมายเหตุ |
|---|---|---|
| `DB_URL` | MySQL JDBC URL | เช่น `jdbc:mysql://localhost:3306/smartseaman?autoreconnect=true` |
| `DB_USERNAME` | DB username | |
| `DB_PASSWORD` | DB password | |
| `JWT_SECRET` | JWT signing key | **Base64 standard** — generate ด้วย `openssl rand -base64 32` |
| `ENCRYPT_KEY` | Certificate encryption key | **Base64 standard** — generate ด้วย `openssl rand -base64 32` |
| `DO_SPACES_KEY` | DigitalOcean Spaces access key | |
| `DO_SPACES_SECRET` | DigitalOcean Spaces secret key | |
| `FCM_CREDENTIAL_FILE` | Path ไปยัง Firebase service account JSON | absolute path หรือชื่อไฟล์ใน classpath |
| `MAIL_PASSWORD` | Gmail SMTP password | |

Generate security keys:

```sh
echo "JWT_SECRET=$(openssl rand -base64 32)"
echo "ENCRYPT_KEY=$(openssl rand -base64 32)"
```

> **หมายเหตุ:** `JWT_SECRET` และ `ENCRYPT_KEY` ต้องเป็น Base64 standard เท่านั้น (ห้ามมีอักขระ `-` หรือ `_`)

---

## 2. Firebase Credential File

วางไฟล์ Firebase service account JSON ไว้ที่ path ที่กำหนดใน `FCM_CREDENTIAL_FILE`

ถ้า download มาจาก internet ต้องลบ macOS quarantine attribute ก่อน:

```sh
xattr -d com.apple.quarantine /path/to/smart-seaman-firebase.json
```

ดูโครงสร้างไฟล์ได้ที่ `src/main/resources/firebase-service-account.example.json`

---

## 3. Run Options

### Option A — Terminal (พร้อม hot reload)

```sh
./run-dev.sh
```

script จะโหลด `.env` และ run app อัตโนมัติ

หรือ run ด้วย Maven โดยตรง:

```sh
# โหลด .env ก่อน
export $(grep -v '^#' .env | xargs)

# Run
./mvnw spring-boot:run
```

เมื่อแก้ไข `.java` แล้ว save — DevTools จะ restart app อัตโนมัติภายใน ~1-2 วินาที

### Option B — VS Code

**Extensions ที่ต้องติดตั้ง:**

```
ext install vscjava.vscode-java-pack
ext install vmware.vscode-spring-boot
ext install vscjava.vscode-spring-boot-dashboard
```

**Run:**

1. เปิด VS Code ที่ folder `smart-seaman-bos-api`
2. กด `F5` → เลือก **Run (Dev)**
3. VS Code โหลด `.env` อัตโนมัติ

**Debug:** เลือก **Debug (Dev)** แล้ววาง breakpoint ได้เลย

---

## 4. Verify Startup

```sh
# Health check
curl http://localhost:8080/actuator/health
# ควรได้ {"status":"UP"}
```

Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 5. MySQL (Docker)

ถ้าต้องการ run MySQL บน local ด้วย Docker:

```sh
# Start
docker compose -f docker-compose.dev.yml up -d

# Stop
docker compose -f docker-compose.dev.yml down
```

---

## 6. Build Production JAR

```sh
./mvnw clean package
```

JAR จะอยู่ที่ `target/*.jar` — DevTools จะ**ไม่ถูกรวม**เข้าไปใน production JAR โดยอัตโนมัติ

---

## Common Commands

```sh
./mvnw test                     # Run all tests
./mvnw -Dtest=ClassName test    # Run single test class
./mvnw compile                  # Compile only (trigger hot reload)
./mvnw clean package            # Build JAR
./mvnw spring-boot:run          # Run locally
```
