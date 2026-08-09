# Smart Seaman BOS API Project Structure Analysis

วันที่วิเคราะห์: 2026-07-26

## ภาพรวม

Project นี้เป็น Java 11 Spring Boot API ที่ใช้ Maven และจัดโครงสร้างหลักแบบ layer-based:

- `src/main/java/com/seaman/controller` สำหรับ REST controllers
- `src/main/java/com/seaman/service` สำหรับ business logic
- `src/main/java/com/seaman/repository` สำหรับ persistence access ผ่าน JDBC SQL
- `src/main/java/com/seaman/entity` สำหรับ entity/data record classes
- `src/main/java/com/seaman/model/request` และ `src/main/java/com/seaman/model/response` สำหรับ DTO
- `src/main/java/com/seaman/config` สำหรับ Spring/security/object storage/data source configuration
- `src/main/resources` สำหรับ application config, logging, Firebase example, และ mobile association files

โครงสร้างปัจจุบันเข้าใจง่ายและเหมาะกับ project ขนาดเล็กถึงกลาง แต่เมื่อจำนวน module เพิ่มขึ้น จะเริ่มเจอปัญหา service/repository ใหญ่, dependency ระหว่าง layer ซับซ้อน, test ยาก และ configuration เสี่ยงต่อ production/local ปะปนกัน

## สิ่งที่ควรทำทันที

### 1. ป้องกัน local secrets หลุดเข้า Git

พบไฟล์ `src/main/resources/application-local.properties` เป็น untracked file และมีค่าลักษณะ secret/local credential อยู่ภายใน เช่น DB password, object storage key/secret, JWT secret และ mail password

ปัจจุบัน `.gitignore` ignore `.env`, `.env.*`, และ Firebase credential บางรูปแบบ แต่ยังไม่ได้ ignore:

```gitignore
src/main/resources/application-local.properties
```

ข้อเสนอ:

- เพิ่ม `src/main/resources/application-local.properties` ลง `.gitignore`
- สร้าง `src/main/resources/application-local.example.properties` ที่ไม่มี secret จริง
- ย้าย secret จริงไป `.env`, environment variables, หรือ deployment secret manager
- ถ้า credential เคยถูก commit ไปแล้ว ควร rotate secret ทันที

### 2. ไม่ควร default profile เป็น production

ใน `src/main/resources/application.properties` มี:

```properties
spring.profiles.active=prod
```

ความเสี่ยงคือ local run, test, หรือ container dev อาจใช้ production profile โดยไม่ตั้งใจ

ข้อเสนอ:

- เอา `spring.profiles.active=prod` ออกจาก default config
- ให้ environment เป็นคนกำหนด profile เช่น `SPRING_PROFILES_ACTIVE=prod`
- ใช้ default เป็น `local` หรือไม่ตั้งค่า default เลย แล้วบังคับตั้งใน runtime

### 3. แก้ dependency cycle แทนการเปิด circular references

มีการตั้งค่า:

```properties
spring.main.allow-circular-references=true
```

ค่านี้ช่วยให้ application start ได้แม้ bean มี circular dependency แต่จะซ่อนปัญหา design และทำให้ refactor/test ยากขึ้น

ข้อเสนอ:

- ปิดค่า `allow-circular-references`
- ไล่ dependency cycle ที่เกิดขึ้นจริง
- แยก responsibility หรือสร้าง component กลางเฉพาะกรณีที่ service เรียกกันเป็นวง

### 4. จำกัด CORS และ public endpoints ด้วย config

ใน `SecurityConfiguration` พบว่า CORS อนุญาต pattern กว้าง:

```java
cors.setAllowedOriginPatterns(Collections.singletonList("https://*"));
cors.setAllowCredentials(true);
```

และ public paths มีทั้ง actuator, swagger, API docs:

```java
"/actuator/**",
"/swagger-ui.html/**",
"/swagger-ui/**",
"/smart-seaman-swagger/**",
```

ข้อเสนอ:

- ย้าย allowed origins ไปเป็น property/env ตาม environment
- หลีกเลี่ยง `https://*` เมื่อ `allowCredentials=true`
- คุมการเปิด Swagger/Actuator ด้วย property เช่น `EXPOSE_API_DOCS` และ `EXPOSE_ACTUATOR`
- Production ควรเปิดเฉพาะ health endpoint ที่จำเป็น และไม่เปิด detailed actuator publicly

## โครงสร้าง Package

### สภาพปัจจุบัน

ปัจจุบัน package เป็นแบบแยกตาม technical layer:

```text
com.seaman
  controller
  service
  repository
  entity
  model
    request
    response
    common
    external
  config
  filter
  interceptor
  exception
  constant
  utils
  event
  push
```

ข้อดี:

- เข้าใจง่ายสำหรับ project Spring Boot ทั่วไป
- หา controller/service/repository ได้ตรงไปตรงมา
- เหมาะกับ codebase ที่ domain ยังไม่ใหญ่มาก

ข้อจำกัด:

- เมื่อ feature โตขึ้น ไฟล์ของ domain เดียวกันกระจายหลาย folder
- Service และ repository มีแนวโน้มใหญ่ขึ้นเรื่อย ๆ
- การทำ change หนึ่ง feature ต้องไล่หลาย package
- Test mirror package ตาม domain ได้ยากกว่า

### โครงสร้างที่แนะนำในระยะกลาง

ค่อย ๆ refactor เป็น package ตาม domain/feature:

```text
com.seaman
  auth
    AuthController
    AuthService
    AuthRepository
    model
  admin
    AdminController
    AdminService
    AdminRepository
    model
  document
    DocumentController
    DocumentService
    DocumentRepository
    model
  course
  certification
  news
  notification
  voucher
  shared
    config
    exception
    security
    utils
    response
```

ไม่จำเป็นต้องย้ายทั้งหมดครั้งเดียว ควรเริ่มจาก module ที่มีการเปลี่ยนบ่อยหรือมี bug บ่อย เช่น `auth`, `document`, `notification`

## Service Layer

พบ service หลายตัวมีขนาดใหญ่ เช่น:

- `AdminService` ประมาณ 567 lines
- `NewsService` ประมาณ 489 lines
- `CertificationService` ประมาณ 486 lines
- `SendNotificationService` ประมาณ 343 lines
- `AuthService` ประมาณ 335 lines

ข้อเสนอ:

- แยก method ที่เป็น use case ชัดเจน เช่น create/update/search/delete/send
- แยก logic ประเภท mapping, validation, file/object storage, notification sending ออกจาก service หลัก
- ลดการ inject `HttpServletRequest` เข้า service โดยตรง เพราะทำให้ test ยากและผูก business logic กับ web layer
- ใช้ request context wrapper หรือส่งค่าที่จำเป็นจาก controller/interceptor เช่น `correlationId`, `language`, `traceId`, `currentUser`
- เพิ่ม `@Transactional` ใน use case ที่มีหลาย DB write ต่อเนื่อง เช่น register, login session insert + last login update, create/update flows

## Repository Layer

Repository ใช้ `NamedParameterJdbcTemplate` และ SQL ตรง ซึ่งควบคุม query ได้ดี แต่มี duplication สูง:

- SQL string กระจายหลาย repository
- pattern `try/catch Exception -> log -> BusinessException` ซ้ำหลาย method
- `BeanPropertyRowMapper` ถูกใช้ซ้ำโดยไม่มี mapper เฉพาะ
- method บางส่วน return `null` เมื่อไม่พบ record ทำให้ service ต้อง wrap `Optional.ofNullable(...)`

ข้อเสนอ:

- เปลี่ยน `CommonRepository` จาก field injection เป็น constructor injection
- เพิ่ม helper กลางสำหรับ `queryForOptional`, `queryForList`, `update`
- กำหนด convention ให้ repository return `Optional<T>` สำหรับ query ที่อาจไม่เจอข้อมูล
- แยก SQL ที่ซับซ้อนให้อ่านง่ายขึ้น หรือใช้ named constant ที่สื่อ business meaning
- เพิ่ม integration tests สำหรับ query สำคัญ ถ้ามี test database หรือ Testcontainers ในอนาคต

## Error Handling

มีจุดที่ควรแก้ใน `ExceptionAdvice`:

```java
@ExceptionHandler(Exception.class)
protected ResponseEntity<Object> handleException(CommonException ex)
```

Annotation ระบุว่า handle `Exception.class` แต่ parameter เป็น `CommonException` ซึ่งไม่ตรงเจตนา ควรแยกเป็น:

- `@ExceptionHandler(CommonException.class)` สำหรับ business/common errors
- `@ExceptionHandler(Exception.class)` สำหรับ unexpected errors

อีกจุดคือเงื่อนไข:

```java
if (null != ex.getMessage() || !"".equals(ex.getMessage())) {
```

ควรใช้ `&&` แทน `||` เพื่อไม่ append message เมื่อ message เป็น null/empty

ข้อเสนอ:

- แยก error response contract ให้ชัดเจน
- ใช้ HTTP status ให้ตรงกับ error จริง
- หลีกเลี่ยงการส่ง raw exception message กลับ client ใน production
- ใช้ language จาก request header/context แทน hardcode ภาษาไทยเสมอ

## Security Filter

ใน `TokenFilter` พบว่า filter ตั้ง response status เป็น `200 OK` ตั้งแต่ต้น:

```java
response.setStatus(HttpStatus.OK.value());
```

ถ้า token invalid หรือเกิด auth error ควรตอบ `401 Unauthorized` หรือ `403 Forbidden` ตามกรณี ไม่ควรตอบ 200 พร้อม error body

ข้อเสนอ:

- ใช้ Spring Security exception flow ให้มากขึ้น
- ตั้ง status เฉพาะเมื่อเกิด error
- อ่าน language จาก request header ไม่ hardcode `TH`
- ตรวจ session/token revocation ถ้าระบบมี session table แล้วควรใช้ให้ครบ
- เพิ่ม tests สำหรับ missing token, malformed bearer token, invalid token, expired token, valid token

## Dependency และ Platform

`pom.xml` ใช้ Spring Boot `2.6.2` และ Java 11 ซึ่งยังใช้งานได้ แต่ dependency หลายตัวค่อนข้างเก่า:

- Spring Boot 2.6.2
- `springdoc-openapi-ui` 1.2.32
- `jjwt` 0.9.1
- AWS SDK v1
- Logstash Logback Encoder 6.3
- Spring Boot Admin version เป็น `3.0.0-M4` ซึ่งดูไม่สอดคล้องกับ Spring Boot 2.6.x และเป็น milestone version

ข้อเสนอ:

- ทำ dependency audit แยกต่างหากก่อน upgrade
- อย่างน้อยควรอัป patch/minor ภายใน Spring Boot 2.x ก่อน ถ้ายังไม่พร้อมย้าย Java/Spring รุ่นใหญ่
- วาง roadmap ไป Java 17 + Spring Boot 3.x เมื่อพร้อม เพราะต้องเปลี่ยน `javax.*` เป็น `jakarta.*`
- ตรวจ compatibility ของ Spring Boot Admin ให้ตรงกับ Spring Boot version
- พิจารณา migration จาก `jjwt` 0.9.1 ไป API รุ่นใหม่กว่า

## Configuration Management

ข้อเสนอ:

- แยก config เป็น profile-specific files:

```text
application.properties
application-local.example.properties
application-dev.properties
application-prod.properties
```

- เก็บค่า default ที่ไม่ secret ใน repo
- เก็บ secret ใน environment variables หรือ secret manager
- เพิ่ม `@ConfigurationProperties` สำหรับกลุ่ม config เช่น object storage, security, FCM, upload limits
- Validate required config ตอน start ด้วย `@Validated`
- ลดการ log ค่า configuration ที่ sensitive แม้จะ mask แล้ว เพราะยัง expose prefix ของ secret

## Testing

รันคำสั่ง:

```sh
./mvnw test
```

ผลลัพธ์:

```text
BUILD SUCCESS
Tests run: 0, Failures: 0, Errors: 0, Skipped: 0
```

แปลว่า build ผ่าน แต่ยังแทบไม่มี safety net เพราะ test ปัจจุบันถูก comment `@Test` ออก

ข้อเสนอเริ่มต้น:

- เพิ่ม unit tests สำหรับ `AuthService.login`
- เพิ่ม unit tests สำหรับ `AuthService.register`
- เพิ่ม tests สำหรับ `ExceptionAdvice`
- เพิ่ม tests สำหรับ `TokenFilter`
- เพิ่ม controller tests สำหรับ endpoint สำคัญ เช่น login/register/profile
- สร้าง `src/test/resources/application-test.properties` ที่ไม่ต้องพึ่ง DB/secret จริง

## ลำดับการปรับปรุงที่แนะนำ

1. เพิ่ม `.gitignore` สำหรับ local config และสร้าง example config ที่ไม่มี secret
2. ย้าย profile/secret/CORS/public docs controls ไป environment-driven config
3. แก้ `ExceptionAdvice` และ `TokenFilter` ให้ HTTP status และ error handling ถูกต้อง
4. เพิ่ม test baseline สำหรับ auth/security/error handling
5. ค่อย ๆ refactor service ที่ใหญ่ที่สุด โดยเริ่มจาก feature ที่เปลี่ยนบ่อย
6. ลด duplication ใน repository ด้วย helper และ Optional return convention
7. ทำ dependency upgrade audit และวาง roadmap Spring Boot/Java upgrade

## สรุป

Project นี้ยังมีโครงสร้างที่ maintain ได้ แต่มี technical debt ชัดเจนในสามแกน:

- Security/config hygiene
- Test coverage
- Service/repository complexity

ถ้าจะลงทุนปรับ ควรเริ่มจาก security/config และ test ก่อน เพราะลดความเสี่ยง production ได้ทันที จากนั้นค่อย refactor package/service/repository แบบ incremental ตาม feature ที่กำลังพัฒนา
