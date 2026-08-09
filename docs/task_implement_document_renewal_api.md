# Mobile Document Renewal API Tasks

อ้างอิง: `documents/mvp1/document_renewal_figma_spec.md` ส่วน `Mobile APIs`

ตรวจสถานะจาก source code ณ วันที่ 4 กรกฎาคม 2026 โดยถือว่า task เป็น **ทำแล้ว** เมื่อมี route, controller, service, repository/SQL mapping, request/response model, validation และ test ที่เกี่ยวข้องครบตาม acceptance criteria

## Status Legend

- [x] ทำแล้ว
- [~] ทำบางส่วน
- [ ] ยังไม่ได้ทำ
- [?] มี script/design แล้ว แต่ยังยืนยัน runtime/deployment ไม่ได้

## Documentation Rule

- เมื่อ API task ใด implement เสร็จและเปลี่ยน Status เป็น `[x]` ต้องเพิ่มตัวอย่าง cURL ที่เรียกใช้งานได้ไว้ใน section ของ task นั้นทุกครั้ง
- cURL ต้องใช้ `${base_url}` โดยไม่รวม `/v1`, ใช้ `${access_token}` สำหรับ JWT และแสดง required headers, query parameters หรือ request body ให้ครบ
- task ที่เป็น internal foundation และไม่มี public endpoint ต้องระบุว่าไม่มี cURL โดยตรง พร้อมอ้างอิง API ที่ใช้ foundation นั้น

## Current Status

| ลำดับ | Status | Method | API | หมายเหตุ |
|---:|:---:|---|---|---|
| 1 | [x] | GET | `/v1/document-renewals/statuses` | implement controller/service/repository/model และ mobile progress mapping แล้ว; focused tests ผ่าน |
| 2 | [x] | GET | `/v1/document-renewals/prices?documentCode={code}` | implement API, validation, BigDecimal, effective-date filtering และ overlapping-config guard แล้ว |
| 3 | [x] | POST | `/v1/document-renewals` | สร้าง unpaid draft, อ้าง price/address records เดิม, snapshot ยอดรวม, running number และ request items ใน transaction เดียวกัน |
| 4 | [x] | GET | `/v1/document-renewals/my?offSet={n}` | implement owner-scoped pagination, summary/status mapping และ date formatting แล้ว |
| 5 | [x] | GET | `/v1/document-renewals/{requestNo}` | implement owner-scoped detail, correction state, conditional status detail และ signed file access แล้ว |
| 6 | [x] | GET | `/v1/document-renewals/{requestNo}/timeline` | implement owner-scoped append-only timeline, deterministic ordering และ safe display detail แล้ว |
| 7 | [x] | POST | `/v1/document-renewals/{requestNo}/items/{documentRequestItemCode}/file` | implement multipart replace, ownership/item/state guard และ reuse secure profile-file storage flow แล้ว |
| 8 | [x] | POST | `/v1/document-renewals/{requestNo}/resubmit` | implement corrected-item validation, atomic transition/timeline และ after-commit notification แล้ว |
| 9 | [x] | POST | `/v1/document-renewals/{requestId}/payments` | implement Omise PromptPay QR และ Mobile Banking app redirect charge flow แล้ว |
| 10 | [x] | GET | `/v1/document-renewals/{requestId}/payments/{transactionId}` | implement owner-scoped payment status แล้ว; webhook เป็น source of truth สำหรับ success transition |

สรุป Mobile APIs: **ทำแล้ว 10/10, ทำบางส่วน 0/10, ยังไม่ได้ทำ 0/10**

## Prerequisites And Supporting Work

| Status | งาน | หลักฐาน/หมายเหตุ |
|:---:|---|---|
| [x] | API ตรวจ required document ที่ขาด/ต้องแก้ไข | `GET /v1/documents/request-items/validate`; มี controller, service, repository query และ response model แล้ว |
| [?] | Schema สำหรับ renewal flow | มี DDL ใน `RUN1_2026_create_table.sql`; ไม่สามารถสรุปว่า deploy แล้วจาก source code |
| [?] | Status master seed | มี upsert 7 statuses ใน `RUN2_2026_insert_master.sql`; ไม่สามารถสรุปว่า run แล้วจาก source code |
| [~] | Required document master/setting | มี schema, seed ตัวอย่าง `DOC001` และ validation API แล้ว แต่ยังไม่ยืนยัน master จริงครบทุก `document_code` |
| [ ] | Renewal price master data | มี table แต่ยังไม่พบ seed/config ราคาที่ใช้งานจริง |
| [x] | Delivery address decision | request เก็บ `delivery_address_id` อ้าง `m_delivery_address`; ไม่สร้าง snapshot table ซ้ำ |
| [x] | Create/update delivery address APIs | มี route/controller/service/repository/model, concurrency guard, OpenAPI examples, cURL และ focused tests ครบแล้ว |
| [x] | Thailand address master APIs | implement route/controller/service/repository/response model แล้ว; focused tests ผ่าน 5/5 และ pin Lombok ให้รองรับ JDK 21 แล้ว |
| [x] | Update mobile number with change history | `POST /v1/profile-update` ใช้ row lock และ transaction เพื่ออัปเดต `MOBILE_NUMBER` พร้อม append history |
| [x] | Payment provider decision | ใช้ Omise `promptpay` และ `mobile_banking_*`; server-side create source+charge, `charge.complete` webhook + retrieve charge verify ก่อนเปลี่ยน request status |
| [x] | Automated tests สำหรับ renewal flow | มี focused controller/service/repository/model tests ครอบคลุม 10 mobile endpoints, ownership, validation, transition, payment retry/webhook duplicate และ date/money contract |

## Task Breakdown

### MR-MOB-01: Shared Renewal Foundation

Status: [x] ทำแล้ว

- [x] เพิ่ม route constants กลุ่ม `/document-renewals` สำหรับ status และ price
- [x] เพิ่ม entity/DTO สำหรับ renewal request, status, price, request item, transaction, payment, department submission, delivery และ delivery address
- [x] เพิ่ม service/repository กลางสำหรับตรวจ ownership ด้วย `mobile_user_uuid` จาก authenticated request context
- [x] กำหนด enum ของ status/action โดยใช้ stable internal code และ English master name ไม่ผูก business logic กับชื่อภาษาไทย
- [x] lock owned request และกำหนด `@Transactional` boundary ครอบ status update กับ append timeline
- [x] ป้องกัน stale/concurrent transition ด้วย expected current status ใน update condition
- [x] เพิ่ม focused tests สำหรับ ownership, invalid request ID, invalid state และ status/timeline write

API coverage:

- MR-MOB-01 เป็น internal/shared foundation จึงไม่มี public API เฉพาะของตัวเอง
- foundation ถูกใช้งานโดย `GET /v1/document-renewals/statuses` (MR-MOB-02) และ
  `GET /v1/document-renewals/prices` (MR-MOB-03)
- ownership guard และ atomic status transition จะถูกเรียกจาก request detail, timeline,
  resubmit และ payment APIs เมื่อ MR-MOB-04 ถึง MR-MOB-11 implement
- ยังไม่มี endpoint สำหรับเรียก status transition โดยตรง และไม่ควร expose generic transition API ให้ mobile

ตัวอย่าง cURL สำหรับตรวจ foundation ผ่าน APIs ที่เปิดใช้งานแล้ว:

```bash
# Active renewal status master
curl --request GET \
  --url "${base_url}/v1/document-renewals/statuses" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"

# Active/effective renewal price
curl --request GET \
  --get \
  --url "${base_url}/v1/document-renewals/prices" \
  --data-urlencode "documentCode=DOC001" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"
```

หมายเหตุ:

- `${base_url}` ไม่รวม `/v1` เช่น `https://api.example.com`
- `${access_token}` เป็น JWT ของ mobile user ที่ login แล้ว
- price API จะคืน `MA00016` เมื่อไม่มี active/effective price ของ `documentCode`
- ownership/transition foundation ยังไม่มี cURL จนกว่า endpoint ที่ใช้ request ownership จะ implement

Acceptance criteria:

- user อ่านหรือแก้ไขได้เฉพาะ request ของตัวเอง
- ทุก query ใช้ชื่อ table/column ตรงกับ RUN1
- การเปลี่ยนสถานะทุกครั้งเพิ่ม `m_document_transaction` ใน transaction เดียวกัน

### MR-MOB-02: Get Renewal Status Master

Status: [x] ทำแล้ว

API: `GET /v1/document-renewals/statuses`

- ดึงเฉพาะ `is_active = 'YES'`
- map `id`, `nameTh`, `nameEn`, `cssColor` และ mobile progress step
- กำหนดลำดับ 5 main steps และแยก correction/cancel ให้ตรง spec

Acceptance criteria:

- response ครบ 7 statuses จาก spec เมื่อ master data ครบ
- `รอผู้ยื่นแก้ไข` map เป็น step 1 และ `ยกเลิก` ไม่เป็น normal progress step

Implementation: map step ใน application จาก stable English status name; correction เป็น step 1 และ cancelled มี `progressStep = null`

ตัวอย่าง cURL:

```bash
curl --request GET \
  --url "${base_url}/v1/document-renewals/statuses" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"
```

ตัวแปร:

- `${base_url}` ไม่รวม `/v1` เช่น `https://api.example.com`
- `${access_token}` เป็น JWT ของ mobile user ที่ login แล้ว

### MR-MOB-03: Get Renewal Price

Status: [x] ทำแล้ว

API: `GET /v1/document-renewals/prices?documentCode={code}`

- validate `documentCode`
- ดึง active/effective price ของเอกสาร
- ส่ง fee breakdown และยอดรวมด้วย `DECIMAL`/`BigDecimal`
- กำหนด behavior เมื่อไม่พบราคา หรือมีช่วง effective date ซ้อนกัน

Acceptance criteria:

- ไม่ใช้ floating-point กับจำนวนเงิน
- คืนราคาเฉพาะ document ที่เปิด renewal และราคาอยู่ในช่วงใช้งาน
- มี test กรณีพบราคา, ไม่พบราคา และ config ซ้ำ

Implementation status:

- [x] validate และ normalize `documentCode`
- [x] query เฉพาะ active price ของ active document
- [x] ใช้ `BigDecimal` และคืน fee breakdown/total
- [x] มี test กรณีพบราคา, ไม่พบราคา และ config ซ้ำ
- [x] เพิ่ม `effective_from`/`effective_to`, effective-period constraint และ lookup index ใน RUN1
- [x] เพิ่ม migration `RUN5_document_renewal_foundation.sql` สำหรับฐานข้อมูลเดิม
- [x] filter ช่วงวันที่แบบ inclusive และรองรับ `effective_to = NULL`
- [x] fail เมื่อพบ active/effective config ซ้อนกันมากกว่าหนึ่งรายการ

Operational prerequisite: ต้องกำหนด production price master data จริงก่อนเปิด renewal; ไม่ hard-code ราคาใน application หรือ seed ตัวอย่างเป็นราคาจริง

### MR-MOB-04: Create Renewal Request

Status: [x] ทำแล้ว

API: `POST /v1/document-renewals`

- validate document เปิดให้ renewal
- validate required document items จาก profile
- เก็บ `price_setting_id` และ snapshot ยอดรวม `amount` ลง request
- generate `request_no` แบบ concurrency-safe
- สร้าง request items จาก required document setting
- กำหนด initial status ตาม payment decision
- append transaction action `CREATE` เมื่อสร้าง unpaid draft สถานะ `PAYMENT_PENDING`; payment success flow ต้อง append transaction ก่อนเข้า `รอตรวจเอกสาร`

Acceptance criteria:

- ไม่เชื่อถือ `mobileUserUuid`, ราคา หรือ status จาก request body
- `request_no` unique แม้สร้างพร้อมกัน
- ไม่เข้า `รอตรวจเอกสาร` ก่อน payment success เว้นแต่ product ยืนยัน unpaid draft flow
- rollback ทั้ง request/items/transaction เมื่อขั้นตอนใดล้มเหลว

Implementation decisions:

- สร้าง unpaid draft ก่อนชำระเงินด้วย internal status `PAYMENT_PENDING` (`รอชำระเงิน`)
- `PAYMENT_PENDING` active สำหรับ workflow แต่ `is_mobile_visible = 'NO'` จึงไม่เพิ่มรายการใน status progress master 7 สถานะ
- `request_no` ใช้ `YYMM` + running 5 หลัก เช่น `260700001`; sequence แยกตามเดือนและ lock ด้วย database transaction
- request body รับเฉพาะ `documentCode` และ `deliveryAddressId`; user, ราคา และ status มาจาก server
- `m_document_request.price_setting_id` อ้าง `m_document_prices_setting.id`; ไม่สร้าง `m_document_request_price`
- `m_document_request.delivery_address_id` อ้าง `m_delivery_address.id`; ไม่สร้าง `m_document_request_delivery_address`
- คง `m_document_request.amount` เป็นยอดรวม ณ เวลาสร้าง request เพื่อไม่ให้ยอดชำระเปลี่ยนตาม price setting ภายหลัง
- สร้าง header, required request items และ `CREATE` timeline ภายใน transaction เดียวกัน
- เพิ่ม fresh-install schema ใน RUN1 และ migration `RUN7_create_renewal_request_draft.sql` สำหรับฐานข้อมูลเดิม

ตัวอย่าง cURL:

```bash
curl --request POST \
  --url "${base_url}/v1/document-renewals" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Content-Type: application/json" \
  --header "Accept-Language: TH" \
  --data '{
    "documentCode": "DOC001",
    "deliveryAddressId": "11111111-2222-3333-4444-555555555555"
  }'
```

ตัวอย่าง response:

```json
{
  "code": "MA00000",
  "description": "Success",
  "data": {
    "requestId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
    "requestNo": "260700001",
    "documentCode": "DOC001",
    "status": "PAYMENT_PENDING",
    "amount": 1500.00,
    "deliveryAddressId": "11111111-2222-3333-4444-555555555555"
  }
}
```

ก่อนเรียก API:

- ต้องมี active/effective price ของ `documentCode`
- required profile document items ต้อง upload ครบและไม่มีผลตรวจ `fix`
- `deliveryAddressId` ต้องเป็น active address ของ user จาก JWT

### MR-MOB-05: List My Renewal Requests

Status: [x] ทำแล้ว

API: `GET /v1/document-renewals/my?offSet={n}`

- ดึงรายการด้วย user จาก JWT/request context เท่านั้น
- paginate และเรียงล่าสุดก่อน
- map request summary, current status, amount และ `isResubmit`

Acceptance criteria:

- response มี `itemTotal`, `isLast` และ items
- วันที่แสดงเป็น `DD/MM/YYYY HH:mm`
- ไม่คืน request ของ user อื่น

Implementation status:

- [x] ใช้ `mobile_user_uuid` จาก authenticated user เท่านั้น
- [x] validate `offSet >= 0`, page size 10 และเรียง `submitted_at DESC, id DESC`
- [x] query `itemTotal` ด้วย owner filter เดียวกับรายการและคำนวณ `isLast`
- [x] map request/document/current status/amount/`isResubmit` ตาม response summary
- [x] map status progress step และเลือกชื่อเอกสารตาม `Accept-Language` โดย fallback อย่างปลอดภัย
- [x] format `submittedAt` เป็น `DD/MM/YYYY HH:mm` ใน timezone `Asia/Bangkok`
- [x] MR-MOB-09 ตั้ง `m_document_request.is_resubmit = 1` ภายใน resubmit transaction
- [x] เพิ่ม focused controller/service/repository tests

ตัวอย่าง cURL:

```bash
curl --request GET \
  --get \
  --url "${base_url}/v1/document-renewals/my" \
  --data-urlencode "offSet=0" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"
```

### MR-MOB-06: Get Renewal Request Detail

Status: [x] ทำแล้ว

API: `GET /v1/document-renewals/{requestNo}`

- ตรวจ ownership
- คืน header, current status, supporting items, current status detail, department submission และ delivery เฉพาะส่วนที่เกี่ยวข้อง
- แสดง `checkNote` สำหรับ item ที่เป็น `fix`
- สร้าง file access ที่ไม่เปิด object storage path แบบถาวรโดยไม่มี authorization

Acceptance criteria:

- mobile เห็นเฉพาะรายละเอียดของ current status ไม่รวม future status detail
- response แสดง correction state และ rejected item note ชัดเจน
- delivery data แสดงเมื่อ status เกี่ยวกับการจัดส่ง

Implementation status:

- [x] ใช้ `requestNo` จาก path และตรวจ ownership ด้วย authenticated `mobile_user_uuid`
- [x] คืน request header, localized document name, current status, amount และ `isResubmit`
- [x] คืน supporting items เรียงตาม master sort order พร้อม `FIX/PASS/PENDING` state
- [x] แสดง `checkNote` เฉพาะ item สถานะ `FIX` และรองรับ multi-file identity document
- [x] ไม่คืน object-storage key; สร้าง signed file URL อายุ 10 นาทีหลังผ่าน ownership check
- [x] คืน department submission เฉพาะ status ตั้งแต่รอผล/รับเอกสารจากกรมจนถึงจัดส่ง
- [x] คืน delivery เฉพาะ status `Delivering` หรือ `Delivered`
- [x] ไม่ query/ไม่คืน future status detail สำหรับสถานะก่อนหน้า
- [x] format date/time เป็น `DD/MM/YYYY HH:mm` และ date เป็น `DD/MM/YYYY`
- [x] เพิ่ม focused controller/service/repository tests

ตัวอย่าง cURL:

```bash
curl --request GET \
  --url "${base_url}/v1/document-renewals/${request_no}" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"
```

### MR-MOB-07: Get Renewal Timeline

Status: [x] ทำแล้ว

API: `GET /v1/document-renewals/{requestNo}/timeline`

- ตรวจ ownership
- อ่าน append-only rows จาก `m_document_transaction`
- map action/from/to status, timestamp และ display detail

Acceptance criteria:

- เรียงตามเวลาที่เกิดเหตุการณ์อย่างแน่นอน
- วันที่แสดงเป็น `DD/MM/YYYY HH:mm`
- ไม่ expose internal/admin-only note ที่ไม่ควรแสดงบน mobile

Implementation status:

- [x] ใช้ `requestNo` จาก path และ `mobile_user_uuid` จาก authenticated user
- [x] validate ownership ก่อนอ่าน `m_document_transaction`
- [x] query timeline แบบ read-only และเรียง `actioned_at ASC, id ASC` อย่าง deterministic
- [x] map `action`, `fromStatus`, `toStatus`, `actionedAt` และ controlled display `detail`
- [x] format `actionedAt` เป็น `DD/MM/YYYY HH:mm` ใน timezone `Asia/Bangkok`
- [x] รองรับ display detail ภาษาไทย/อังกฤษตาม `Accept-Language`
- [x] ไม่คืน `note` และ `actionedBy` เพื่อป้องกัน internal/admin detail หลุดไป mobile
- [x] เพิ่ม focused controller/service/repository tests

ตัวอย่าง cURL:

```bash
curl --request GET \
  --url "${base_url}/v1/document-renewals/${request_no}/timeline" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"
```

### MR-MOB-08: Upload Or Replace Supporting File

Status: [x] ทำแล้ว

API: `POST /v1/document-renewals/{requestNo}/items/{documentRequestItemCode}/file`

- ตรวจ ownership และ item ต้องอยู่ใน request
- รองรับ multipart upload
- validate MIME type, size และ empty payload
- upload object storage แล้ว update metadata อย่างปลอดภัย
- เมื่อ replace ใน correction flow ให้ตั้ง `is_updated = 1` และ reset review fields ตามกติกา

Acceptance criteria:

- user แก้ file ได้เฉพาะ state ที่อนุญาต
- DB ไม่ชี้ไฟล์ใหม่หาก upload ล้มเหลว และจัดการ orphan object เมื่อ DB update ล้มเหลว
- ไม่ใช้ชื่อไฟล์จาก client เป็น storage key โดยตรง

Implementation status:

- [x] ใช้ `multipart/form-data` พร้อม fields `documentType`, `slotCode` และ `file`
- [x] validate `requestNo` และ `documentRequestItemCode`, authenticated ownership และ item membership ภายใต้ row lock
- [x] อนุญาตเฉพาะ request สถานะ `Pending Applicant Correction` และ item สถานะ `FIX`
- [x] reuse MIME/size validation, UUID storage key และ transaction-aware object cleanup จาก MR-MOB-15
- [x] replace profile file metadata, reset review fields และตั้ง `is_updated = 1`
- [x] เพิ่ม focused service/controller tests สำหรับ success, invalid state, non-FIX item และ invalid ID

ตัวอย่าง cURL สำหรับเอกสารทั่วไป:

```bash
curl --request POST \
  --url "${base_url}/v1/document-renewals/${request_no}/items/${document_request_item_code}/file" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH" \
  --form "documentType=GENERAL" \
  --form "slotCode=MAIN" \
  --form "file=@${file_path};type=application/pdf"
```

สำหรับ `MRI001` ใช้ `documentType=ID_CARD` กับ `slotCode=FRONT|BACK` หรือ
`documentType=PASSPORT` กับ `slotCode=MAIN`

### MR-MOB-09: Resubmit Corrected Documents

Status: [x] ทำแล้ว

API: `POST /v1/document-renewals/{requestNo}/resubmit`

- อนุญาตเฉพาะ status `รอผู้ยื่นแก้ไข`
- validate ว่า item ที่เป็น `fix` ถูก replace ครบ
- เปลี่ยน status เป็น `รอตรวจเอกสาร`
- append transaction action `RESUBMIT`
- trigger FCM/in-app notification ตาม spec

Acceptance criteria:

- concurrent/double submit ไม่สร้าง transition ซ้ำ
- status update และ timeline เป็น atomic transaction
- item ที่แก้แล้วแสดง `is_updated = 1`

Implementation status:

- [x] ใช้ `requestNo` จาก path และ authenticated user จาก JWT; ไม่รับ user/status จาก client
- [x] lock request row และอนุญาตเฉพาะ `Pending Applicant Correction`
- [x] require อย่างน้อยหนึ่ง request item สถานะ `FIX`
- [x] ตรวจไฟล์แก้ไขให้ครบทั้ง `GENERAL/MAIN`, `ID_CARD/FRONT+BACK` และ `PASSPORT/MAIN`
- [x] require `is_updated = 1`, `file_uploaded = 1` และไม่มี `check_result = 'fix'`
- [x] reset corrected request items เป็น `PENDING`, เปลี่ยน request เป็น `Pending Document Review`
  และ append `RESUBMIT` transaction ภายใน database transaction เดียวกัน
- [x] publish renewal event ใน transaction และสร้าง in-app/FCM notification หลัง commit เท่านั้น
- [x] row lock และ expected-current-status update ป้องกัน concurrent/double submit
- [x] เพิ่ม focused controller/service/repository/notification tests

ตัวอย่าง cURL:

```bash
curl --request POST \
  --url "${base_url}/v1/document-renewals/${request_no}/resubmit" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"
```

### MR-MOB-10: Create Payment Attempt

Status: [x] ทำแล้ว

API: `POST /v1/document-renewals/{requestId}/payments`

- [x] ตรวจ ownership และ request ยังชำระได้เฉพาะ status `PAYMENT_PENDING`
- [x] สร้างหนึ่ง `m_payment_transaction` ต่อ attempt
- [x] เรียก Omise แบบ idempotent ด้วย client `idempotencyKey`
- [x] รองรับ `promptpay` คืน QR `download_uri`
- [x] รองรับ Mobile Banking app redirect: `mobile_banking_bbl`, `mobile_banking_kbank`, `mobile_banking_ktb`, `mobile_banking_bay`, `mobile_banking_scb`
- [x] บันทึก normalized status, provider charge/source id, expiry, authorize URI และ raw provider response ที่ไม่รวม secret

Acceptance criteria:

- [x] amount มาจาก server-side request snapshot `m_document_request.amount`
- [x] client retry ไม่สร้าง charge ซ้ำเมื่อ `idempotencyKey` เดิม
- [x] ไม่ log credential หรือข้อมูล payment sensitive
- [x] success จาก client callback อย่างเดียวไม่สามารถเปลี่ยน request status ได้

Implementation notes:

- Omise PromptPay docs ระบุว่า flow เป็น offline QR; หลังสร้าง charge จะได้ status `pending` และต้องรอ completion webhook `charge.complete` แล้วค่อย verify/retrieve charge
- Omise Mobile Banking docs ระบุ source type เช่น `mobile_banking_kbank`, มี `return_uri` และ `authorize_uri` สำหรับ app redirect; charge completion ยังต้องใช้ webhook เช่นกัน
- เพิ่ม public webhook endpoint `POST /v1/payments/omise/webhook`
- เพิ่ม config:
  - `omise.api-url=https://api.omise.co`
  - `omise.secret-key=${OMISE_SECRET_KEY:}`
  - `omise.webhook-secret=${OMISE_WEBHOOK_SECRET:}`
- เพิ่ม migration `RUN13_payment_omise_promptpay_mobile_banking.sql` เพื่อแก้ CHECK constraint สำหรับ `MOBILE_BANKING` และ action `PAYMENT_SUCCESS`
- แหล่งอ้างอิง Omise: `https://docs.omise.co/promptpay`, `https://docs.omise.co/mobile-banking-kbank`, `https://docs.omise.co/api-webhooks`

ตัวอย่าง cURL PromptPay:

```bash
curl --request POST \
  --url "${base_url}/v1/document-renewals/${request_id}/payments" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Content-Type: application/json" \
  --header "Accept-Language: TH" \
  --data '{
    "paymentMethod": "promptpay",
    "idempotencyKey": "renewal-260700001-attempt-1"
  }'
```

ตัวอย่าง cURL Mobile Banking:

```bash
curl --request POST \
  --url "${base_url}/v1/document-renewals/${request_id}/payments" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Content-Type: application/json" \
  --header "Accept-Language: TH" \
  --data '{
    "paymentMethod": "mobile_banking_kbank",
    "platformType": "IOS",
    "returnUri": "smartseaman://document-renewals/payment-return",
    "idempotencyKey": "renewal-260700001-kbank-1"
  }'
```

### MR-MOB-11: Get Payment Status

Status: [x] ทำแล้ว

API: `GET /v1/document-renewals/{requestId}/payments/{transactionId}`

- [x] ตรวจ ownership และ transaction ต้องอยู่ใน request
- [x] คืน internal normalized status พร้อม provider reference/expiry ที่ mobile ต้องใช้
- [x] ใช้ Omise webhook เป็น source of truth สำหรับการเปลี่ยน request status

Acceptance criteria:

- [x] ไม่ expose raw provider payload หรือข้อมูล sensitive
- [x] เมื่อ payment success ให้ request เข้า `รอตรวจเอกสาร` เพียงครั้งเดียวด้วย row lock + current status guard
- [x] payment success append transaction action `PAYMENT_SUCCESS` และส่ง notification หลัง commit

Webhook flow:

- Omise ส่ง `charge.complete` มาที่ `POST /v1/payments/omise/webhook`
- ระบบ verify `Omise-Signature`/`Omise-Signature-Timestamp` เมื่อมี `OMISE_WEBHOOK_SECRET`
- ระบบ retrieve charge จาก Omise ด้วย charge id เพื่อยืนยัน status ก่อน update DB
- ถ้า charge status เป็น `successful`:
  - update `m_payment_transaction.status = SUCCESS`
  - เปลี่ยน `m_document_request` จาก `PAYMENT_PENDING` เป็น `PENDING_DOCUMENT_REVIEW`
  - append `m_document_transaction.action = PAYMENT_SUCCESS`
  - publish notification หลัง commit
- ถ้า charge status เป็น `failed`/`expired` จะ update เฉพาะ payment transaction และไม่เปลี่ยน request status

ตัวอย่าง cURL ตรวจสถานะ:

```bash
curl --request GET \
  --url "${base_url}/v1/document-renewals/${request_id}/payments/${transaction_id}" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"
```

### MR-MOB-12: Mobile Renewal API Tests And Contract

Status: [x] ทำแล้ว

- [x] controller contract tests สำหรับทั้ง 10 mobile renewal endpoints
- [x] service tests สำหรับ ownership, validation และ state transition
- [x] repository SQL contract tests สำหรับ owner scope, idempotency และ row lock queries
- [x] concurrency/idempotency guard tests สำหรับ request number, resubmit และ payment success webhook duplicate
- [x] request validation tests สำหรับ payment provider contract
- [x] cURL examples ครบทุก public mobile renewal endpoint ใน task doc

Acceptance criteria:

- [x] ครอบคลุม happy path, invalid state, forbidden ownership, not found และ duplicate callback/retry
- [x] test ยืนยันทุก status change มี timeline row ผ่าน `appendTransaction` verification
- [x] test ยืนยัน date format และ money precision

Implementation evidence:

- Controller contract: `DocumentRenewalControllerTest` มี 10 tests สำหรับ 10 endpoints ของ MR-MOB-02 ถึง MR-MOB-11
- Service coverage:
  - `DocumentRenewalServiceTest`: status progress mapping, price precision และ invalid price config
  - `DocumentRenewalCreateServiceTest`: ownership, missing required docs, address owner guard, CREATE timeline
  - `DocumentRenewalListServiceTest`: pagination, `DD/MM/YYYY HH:mm` date format และ language mapping
  - `DocumentRenewalDetailServiceTest`: owner-scoped detail และ signed file access behavior
  - `DocumentRenewalTimelineServiceTest`: deterministic timeline display และ safe detail mapping
  - `DocumentRenewalItemFileServiceTest`: correction-state upload guard
  - `DocumentRenewalResubmitServiceTest`: atomic resubmit transition, invalid state และ RESUBMIT timeline
  - `DocumentRenewalPaymentServiceTest`: server amount, idempotent retry, mobile banking validation และ invalid state
  - `OmiseWebhookServiceTest`: payment success transition และ duplicate webhook guard
- Repository SQL contract:
  - `DocumentRenewalFoundationRepositoryTest`
  - `DocumentRenewalPaymentRepositoryTest`
  - `DocumentRenewalRepositoryTest`
  - `DocumentRenewalListRepositoryTest`
  - `DocumentRenewalDetailRepositoryTest`
- Model/request validation:
  - `DocumentRenewalPaymentRequestValidationTest`
  - existing delivery/mobile-number validation tests

Verification command:

```bash
./mvnw test
```

ผลล่าสุด: `Tests run: 99, Failures: 0, Errors: 0, Skipped: 0`

หมายเหตุเรื่อง MySQL 8 disposable schema:

- ยังไม่ได้เพิ่ม Testcontainers/MySQL integration test เพราะโปรเจกต์ปัจจุบันไม่มี disposable DB test harness และไม่มี dependency สำหรับ Testcontainers
- MR-MOB-12 ปิดด้วย runnable unit/contract/repository-SQL tests ที่ไม่ต้องพึ่ง external Docker/DB เพื่อให้ CI ปัจจุบันรันได้เสถียร
- ถ้าต้องการ strict MySQL 8 integration จริง ควรเปิด task แยกสำหรับเพิ่ม Testcontainers, schema bootstrap จาก RUN scripts และ migration smoke test

### MR-MOB-13: Thailand Address Master APIs

Status: [x] ทำแล้ว

APIs:

| Method | API | Purpose |
|---|---|---|
| GET | `/v1/master/provinces` | ดึงรายการจังหวัดทั้งหมด |
| GET | `/v1/master/districts?provinceCode={provinceCode}` | ดึงรายการอำเภอ/เขตตามจังหวัด |
| GET | `/v1/master/subdistricts?districtCode={districtCode}` | ดึงรายการตำบล/แขวงตามอำเภอ/เขต |

- import และกำหนด source of truth ของข้อมูลเขตการปกครองจากแหล่งที่ระบุใน `documents/mvp1/script/RUN4_external_data`
- ตรวจชื่อ table, column, primary key, foreign key, charset และ collation ของ external data ก่อนเขียน repository query
- เพิ่ม route, controller, service, repository, entity และ response model
- เรียงข้อมูลตามชื่อภาษาไทย
- รองรับ `Accept-Language` โดยคืน `name` ตามภาษา และคง `nameTh`/`nameEn` ใน response หาก mobile ต้องใช้ทั้งสองภาษา
- คืน postal code ใน subdistrict response ถ้าชุดข้อมูลต้นทางมีค่า
- validate `provinceCode` และ `districtCode`; ไม่รับ parent code ที่ว่าง
- กำหนด behavior ให้ชัดเจนระหว่างคืน empty list กับ not found เมื่อ parent code ไม่มีอยู่จริง

Response fields ขั้นต่ำ:

```json
{
  "code": "10",
  "name": "กรุงเทพมหานคร",
  "nameTh": "กรุงเทพมหานคร",
  "nameEn": "Bangkok"
}
```

สำหรับ subdistrict เพิ่ม `postalCode` เมื่อข้อมูลต้นทางรองรับ

Acceptance criteria:

- province API คืนจังหวัดที่ active/ใช้งานได้ทั้งหมดโดยไม่ซ้ำ
- district API คืนเฉพาะอำเภอ/เขตที่อยู่ใต้ `provinceCode` ที่ระบุ
- subdistrict API คืนเฉพาะตำบล/แขวงที่อยู่ใต้ `districtCode` ที่ระบุ
- query ใช้ index บน parent code และไม่มี full table scan สำหรับ district/subdistrict lookup
- response อยู่ภายใต้ `SuccessResponse` ตามมาตรฐาน API ปัจจุบัน
- endpoints ต้องผ่าน JWT authentication ตาม security convention ปัจจุบัน เว้นแต่ product ระบุให้เป็น public master data
- มี controller/service/repository tests สำหรับ valid parent, invalid parent, missing parameter และ empty result

Blocked decisions:

- ยืนยันว่าต้องการให้ master address endpoints เป็น public หรือ authenticated

Implementation decisions:

- ใช้ schema ต้นทาง `provinces`, `districts`, `subdistricts` ตาม `RUN4_external_data`
- endpoints เป็น authenticated ตาม security convention ปัจจุบัน
- `Accept-Language: EN` คืน `name` ภาษาอังกฤษ; ค่าอื่นหรือไม่ส่ง header คืนภาษาไทย
- parent code ที่ไม่มีข้อมูลคืน empty list; code ที่ไม่เป็นบวกคืน validation error
- subdistrict response คืน `postalCode` เมื่อมี `zip_code`
- runtime database ต้อง import tables/data จาก `RUN4_external_data` ก่อนใช้งานจริง
- focused test: `./mvnw test -Dtest=MasterDataServiceAddressTest` ผ่าน 5/5 tests
- full test suite: `./mvnw test` ผ่าน 6/6 tests

### MR-MOB-14: Create And Update Delivery Address

Status: [x] ทำแล้ว

APIs:

| Method | API | Purpose |
|---|---|---|
| POST | `/v1/delivery-addresses` | สร้างที่อยู่จัดส่ง |
| PUT | `/v1/delivery-addresses/{addressId}` | แก้ไขที่อยู่จัดส่ง |

- สร้างที่อยู่สำหรับจัดส่งเอกสารของผู้ใช้ที่ login อยู่
- อ่าน `mobile_user_uuid` จาก JWT/request context เท่านั้น ห้ามรับจาก request body
- บันทึกข้อมูลลง `m_delivery_address` โดยใช้ fields:
  - `firstName`
  - `lastName`
  - `addressLine`
  - `province`
  - `district`
  - `subDistrict`
  - `postalCode`
  - `isDefault`
- validate required fields, ความยาวข้อมูล และรูปแบบ `postalCode`
- validate จังหวัด อำเภอ/เขต ตำบล/แขวง และรหัสไปรษณีย์กับ Thailand address master data
- ถ้าเป็นที่อยู่แรกของผู้ใช้ ให้ตั้ง `is_default = 1` อัตโนมัติ
- ถ้า request ระบุ `isDefault = true` ให้ยกเลิก default เดิมก่อนตั้งรายการใหม่เป็น default ภายใน transaction เดียวกัน
- กำหนด `is_active = 'YES'` จากฝั่ง server เท่านั้น
- response คืน `id` และข้อมูลที่อยู่ที่สร้างสำเร็จภายใต้ `SuccessResponse`
- การแก้ไขต้องค้นหาที่อยู่ด้วย `addressId` และ `mobile_user_uuid` จาก JWT เพื่อยืนยัน ownership
- อนุญาตให้แก้ไขเฉพาะที่อยู่ที่มี `is_active = 'YES'`
- การแก้ไขรองรับ fields ชุดเดียวกับ create แต่ห้ามเปลี่ยน `id`, `mobile_user_uuid` และ `is_active` จาก request body
- หากแก้ไข `isDefault = true` ให้ยกเลิก default เดิมและตั้งรายการนี้เป็น default ภายใน transaction เดียวกัน
- หากแก้ไข default address ปัจจุบันเป็น `isDefault = false` ให้ปฏิเสธคำขอด้วย validation error
- update `updated_at` เมื่อแก้ไขสำเร็จ และ response คืนข้อมูลล่าสุดภายใต้ `SuccessResponse`

Acceptance criteria:

- user ไม่สามารถสร้างที่อยู่ให้ `mobile_user_uuid` ของ user อื่นได้
- user ไม่สามารถอ่านหรือแก้ไขที่อยู่ของ user อื่นผ่าน `addressId` ได้
- ข้อมูลจังหวัด อำเภอ/เขต และตำบล/แขวงต้องสัมพันธ์กันตาม master data
- ผู้ใช้มี active default address ได้ไม่เกินหนึ่งรายการ
- การเปลี่ยน default ต้อง atomic และ rollback ทั้งหมดเมื่อบันทึกไม่สำเร็จ
- duplicate request/retry ต้องไม่ทำให้เกิด default address มากกว่าหนึ่งรายการ
- update address ที่ไม่มีอยู่, เป็นของ user อื่น หรือ inactive ต้องไม่แก้ไขข้อมูล
- มี controller/service/repository tests สำหรับ create/update happy path, invalid address hierarchy, invalid postal code, first address, replace default, not found, forbidden ownership, inactive address และ unauthenticated request
- OpenAPI ระบุ request/response example และ validation error

Concurrency and data integrity:

- lock row ของ user ใน `m_mobile_users` ด้วย `FOR UPDATE` ก่อน create/update address เพื่อ serialize mutations แม้ผู้ใช้ยังไม่มี address
- lock active address rows ก่อนนับ, clear หรือ update default
- เพิ่ม generated column `default_owner_uuid` และ unique key
  `uq_delivery_address_active_default` เพื่อบังคับ active default ไม่เกินหนึ่งรายการต่อ user ที่ระดับฐานข้อมูล
- fresh install ใช้ schema ใน RUN1; ฐานข้อมูลเดิมให้รัน `RUN9_harden_delivery_address_default.sql`

ตัวอย่าง cURL สำหรับสร้างที่อยู่:

```bash
curl --request POST \
  --url "${base_url}/v1/delivery-addresses" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Content-Type: application/json" \
  --header "Accept-Language: TH" \
  --data '{
    "firstName": "ศรัญญู",
    "lastName": "แก้วโสภา",
    "addressLine": "16 ม. 8",
    "province": "39",
    "district": "3902",
    "subDistrict": "390202",
    "postalCode": "39170",
    "isDefault": true
  }'
```

ตัวอย่าง cURL สำหรับแก้ไขที่อยู่:

```bash
curl --request PUT \
  --url "${base_url}/v1/delivery-addresses/${address_id}" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Content-Type: application/json" \
  --header "Accept-Language: TH" \
  --data '{
    "firstName": "ศรัญญู",
    "lastName": "แก้วโสภา",
    "addressLine": "99 ม. 1",
    "province": "39",
    "district": "3902",
    "subDistrict": "390202",
    "postalCode": "39170",
    "isDefault": true
  }'
```

Implementation evidence:

- controller/service/repository tests ครอบคลุม create, update, default replacement, ownership,
  inactive/not found, unauthenticated context และ default-address query
- request validation tests ครอบคลุม required fields และ postal code format
- OpenAPI models มี field examples และ controller ระบุ success/validation responses

### MR-MOB-15: Identity Document Multi-File Upload

Status: [x] ทำแล้ว

API:

| Method | API | Content-Type | Purpose |
|---|---|---|---|
| POST | `/v1/documents/request-items/{itemCode}/files` | `multipart/form-data` | upload หรือ replace ไฟล์ในแต่ละ slot ของ profile supporting document |

รองรับ `m_document_master_request_item` รหัส `MRI001` ซึ่งเป็น master item เดียวสำหรับ
`สำเนาบัตรประชาชน / Passport` แต่มีจำนวนไฟล์ที่ต้อง upload ต่างกัน:

| `documentType` | Required `slotCode` | จำนวนไฟล์ |
|---|---|---:|
| `ID_CARD` | `FRONT`, `BACK` | 2 |
| `PASSPORT` | `MAIN` | 1 |

Request multipart fields:

- `documentType`: required สำหรับ `MRI001`; รับเฉพาะ `ID_CARD` หรือ `PASSPORT`
- `slotCode`: `FRONT`/`BACK` สำหรับ `ID_CARD` และ `MAIN` สำหรับ `PASSPORT`
- `file`: binary file; รองรับ JPEG, PNG และ PDF ขนาดไม่เกิน 10 MB ต่อไฟล์

Schema changes:

- คง `MRI001` เป็นหนึ่ง row ใน `m_document_master_request_item`; ห้ามแตกเป็นสาม master items เพราะเป็น requirement ทางธุรกิจรายการเดียว
- ไม่เพิ่ม slot/file table ใหม่; reuse `m_document_master_request_item` และ `m_document_profile_request_item`
- slot rules (`ID_CARD/FRONT+BACK`, `PASSPORT/MAIN`, `GENERAL/MAIN`) กำหนดด้วย application constants
- ปรับ `m_document_profile_request_item` ให้หนึ่ง row ต่อ upload slot โดยเพิ่ม `document_type`,
  `slot_code`, `original_file_name`, `mime_type` และ `file_size`
- เปลี่ยน unique key เป็น `(mobile_user_uuid, document_master_request_item_code, document_type, slot_code)`
- document items ทั่วไปใช้ `document_type = 'GENERAL'` และ `slot_code = 'MAIN'`

Upload behavior:

- อ่าน `mobile_user_uuid` จาก JWT/request context เท่านั้น และหา/create profile item ด้วย user + `itemCode`
- validate ว่า `itemCode`, `documentType` และ `slotCode` เป็น active combination ใน master slot
- ตรวจ MIME จาก file signature; ห้ามเชื่อเฉพาะ `Content-Type` หรือ extension จาก client
- generate storage key ด้วย UUID ฝั่ง server; ห้ามนำ original filename มาใช้เป็น key โดยตรง
- upload object ใหม่ให้สำเร็จก่อน update metadata ใน DB
- replace slot เดิมด้วย transaction; หลัง commit จึงลบ object เก่า
- ถ้า object upload ล้มเหลว ห้ามแก้ DB; ถ้า DB update ล้มเหลวหลัง upload ต้องลบ orphan object ใหม่
- เมื่อ upload ชนิดใหม่ต่างจากชนิด active เดิม (`ID_CARD` ↔ `PASSPORT`) ให้ลบ metadata/files ของชนิดเดิมและเริ่ม validation ใหม่
- ห้ามคืน permanent public object-storage path; file view/download ต้องผ่าน authenticated API หรือ short-lived signed URL

Validation API changes:

- ปรับ `GET /v1/documents/request-items/validate?documentCode={code}` ให้คำนวณความครบจาก required slots
- `ID_CARD` ถือว่าครบเมื่อมีทั้ง `FRONT` และ `BACK`; `PASSPORT` ถือว่าครบเมื่อมี `MAIN`
- slot ที่ไม่มีไฟล์หรือมี `checkResult = fix` ถือว่ายังไม่ครบ
- response ของ item เพิ่ม `documentType` และ `files[]`; แต่ละ file คืน `fileId`, `slotCode`,
  `fileUploaded`, `fileUploadedAt`, `checkResult`, `checkNote`, `isUpdated`
- `fileUploaded` ระดับ item เป็น aggregate flag และเป็น `true` เมื่อ required slots ครบเท่านั้น

Migration and compatibility:

- migrate profile items อื่นที่มีไฟล์เดิมเป็น `GENERAL/MAIN` โดยคง storage key และ upload timestamp เดิม
- ไฟล์เดิมของ `MRI001` ไม่มีข้อมูลระบุว่าเป็นด้านใด จึงเก็บ legacy metadata ไว้เพื่อ audit แต่ไม่นับว่าครบ
- หลัง deploy ผู้ใช้ที่มี `MRI001` เดิมต้องเลือก `ID_CARD` หรือ `PASSPORT` และ upload ตาม required slots ใหม่
- deployment ต้องทำ schema/seed ก่อน application rollout; application version ใหม่ต้องไม่อ่าน legacy single-file columns เพื่อสรุปความครบของ `MRI001`

Acceptance criteria:

- upload `ID_CARD/FRONT` เพียงไฟล์เดียวยังคงคืน `BACK` เป็น missing
- upload `ID_CARD/FRONT` และ `ID_CARD/BACK` ครบแล้ว item ผ่าน file-completeness validation
- upload `PASSPORT/MAIN` แล้ว item ผ่าน file-completeness validation
- reject combination เช่น `ID_CARD/MAIN`, `PASSPORT/FRONT` และ document type ที่ไม่รองรับ
- reject empty file, MIME/signature ไม่ตรง allowlist และไฟล์เกิน 10 MB
- user ไม่สามารถอ่าน, replace หรือลบไฟล์ของ user อื่น
- replace ไฟล์ไม่ทำให้ DB ชี้ object ที่ upload ไม่สำเร็จ และไม่ทิ้ง orphan object เมื่อ DB ล้มเหลว
- การเปลี่ยน `ID_CARD`/`PASSPORT` ไม่นำไฟล์ชนิดเดิมมาคำนวณความครบ
- มี controller/service/repository tests สำหรับ happy paths, missing slot, invalid combination,
  ownership, replace, type switch, MIME/size validation, storage failure และ DB rollback
- อัปเดต OpenAPI multipart request/response examples และ validation errors

Implementation evidence:

- reuse ตารางเดิมและปรับ multi-slot columns/unique key ใน RUN1 พร้อม migration
  `RUN10_identity_document_multi_file.sql`
- upload ใช้ authenticated user จาก JWT, UUID storage key และไม่ expose object-storage path
- ตรวจ signature ของ JPEG/PNG/PDF และจำกัดขนาด 10 MB ต่อไฟล์
- ใช้ transaction synchronization: rollback ลบ object ใหม่ และ commit แล้วจึงลบ object ที่ถูก replace
- validation API คำนวณความครบจาก required slots และคืน `documentType`/`files[]`
- focused controller/service/repository tests ผ่าน 6/6 cases

ตัวอย่าง cURL อัปโหลดบัตรประชาชนด้านหน้า:

```bash
curl --request POST \
  --url "${base_url}/v1/documents/request-items/MRI001/files" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH" \
  --form "documentType=ID_CARD" \
  --form "slotCode=FRONT" \
  --form "file=@${file_path};type=image/jpeg"
```

อัปโหลดด้านหลังโดยเปลี่ยน `slotCode=BACK`:

```bash
curl --request POST \
  --url "${base_url}/v1/documents/request-items/MRI001/files" \
  --header "Authorization: Bearer ${access_token}" \
  --form "documentType=ID_CARD" \
  --form "slotCode=BACK" \
  --form "file=@${file_path};type=image/jpeg"
```

ตัวอย่าง cURL อัปโหลด Passport:

```bash
curl --request POST \
  --url "${base_url}/v1/documents/request-items/MRI001/files" \
  --header "Authorization: Bearer ${access_token}" \
  --form "documentType=PASSPORT" \
  --form "slotCode=MAIN" \
  --form "file=@${file_path};type=application/pdf"
```

ตัวอย่างตรวจ required document slots หลัง upload:

```bash
curl --request GET \
  --get \
  --url "${base_url}/v1/documents/request-items/validate" \
  --data-urlencode "documentCode=DOC001" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"
```

### MR-MOB-16: Get Default Delivery Address

Status: [x] ทำแล้ว

API:

| Method | API | Purpose |
|---|---|---|
| GET | `/v1/delivery-addresses` | ดึง active default delivery address ของ mobile user ที่ login อยู่ |

Behavior:

- อ่าน `mobile_user_uuid` จาก JWT/request context เท่านั้น ห้ามรับ `mobileUserUuid` จาก query, path หรือ request body
- query `m_delivery_address` ด้วย `mobile_user_uuid`, `is_default = 1` และ `is_active = 'YES'`
- response อยู่ภายใต้ `SuccessResponse` และคืน fields:
  - `id`
  - `firstName`
  - `lastName`
  - `addressLine`
  - `province`
  - `district`
  - `subDistrict`
  - `postalCode`
  - `isDefault`
- เมื่อไม่พบ active default address ให้คืน business error `MA00016` (`DATA_NOT_FOUND`) สำหรับ `deliveryAddress`
- หากพบ active default address มากกว่าหนึ่งรายการ ให้ถือเป็น data-integrity error และคืน `MA00012` (`EXCEPTION_DATABASE`); ห้ามเลือก row แรกโดยเงียบ ๆ
- endpoint ต้อง authenticated ตาม security convention ปัจจุบัน

Acceptance criteria:

- คืนเฉพาะ default address ของ user จาก JWT
- ไม่คืน inactive address แม้มี `is_default = 1`
- user ไม่สามารถส่งหรือแก้ `mobile_user_uuid` เพื่ออ่าน address ของ user อื่น
- not found และ duplicate default มี behavior ตามที่กำหนด
- response ใช้ field naming เดียวกับ create/update delivery address APIs
- มี controller/service/repository tests สำหรับ happy path, no default, inactive default,
  duplicate default, unauthenticated request และ ownership isolation
- เพิ่ม OpenAPI response description และตัวอย่าง cURL

ตัวอย่าง cURL:

```bash
curl --request GET \
  --url "${base_url}/v1/delivery-addresses" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"
```

ตัวอย่าง response:

```json
{
  "code": "MA00000",
  "description": "Success",
  "data": {
    "id": "11111111-2222-3333-4444-555555555555",
    "firstName": "ศรัญญู",
    "lastName": "แก้วโสภา",
    "addressLine": "16 ม. 8",
    "province": "39",
    "district": "3902",
    "subDistrict": "390202",
    "postalCode": "39170",
    "isDefault": true
  }
}
```

Implementation evidence:

- controller ใช้ `GET` บน `Routes.DELIVERY_ADDRESSES` และ endpoint ยังอยู่หลัง JWT authentication
- service อ่าน `mobile_user_uuid` จาก `userObject` ใน request context เท่านั้น
- repository filter `mobile_user_uuid`, `is_default = 1` และ `is_active = 'YES'`
- focused tests `DeliveryAddressControllerTest`, `DeliveryAddressServiceTest` และ
  `DeliveryAddressRepositoryTest` ผ่าน 10/10 tests

### MR-MOB-17: Update Mobile Number With History

Status: [x] ทำแล้ว

API:

| Method | API | Purpose |
|---|---|---|
| POST | `/v1/profile-update` | อัปเดต `MOBILE_NUMBER` ของ mobile user และเก็บประวัติเมื่อเบอร์เปลี่ยน |

Implementation status:

- reuse endpoint และ request field `mobileNumber` เดิม
- lock ค่าเดิมด้วย `SELECT MOBILE_NUMBER ... FOR UPDATE`
- บันทึก history เฉพาะเมื่อค่าเปลี่ยน และ update profile ภายใน transaction เดียวกัน
- เพิ่ม validation หมายเลขโทรศัพท์ไทย 10 หลักและ focused tests สำหรับ changed/unchanged/ค่าเดิมเป็น `NULL`

Schema changes:

- fresh install ใช้ schema ใน RUN1 และฐานข้อมูลเดิมใช้ migration `RUN12_mobile_number_history.sql`
- table เก็บ `id CHAR(36)`, `mobile_user_uuid VARCHAR(36)`, `old_mobile_number VARCHAR(10) NULL`,
  `new_mobile_number VARCHAR(10) NOT NULL`, `changed_by VARCHAR(255) NOT NULL` และ
  `changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP`
- กำหนด primary key ที่ `id`, index `(mobile_user_uuid, changed_at)` และ foreign key
  `mobile_user_uuid` ไปยัง `m_mobile_users.MOBILE_UUID` โดยไม่ใช้ `ON DELETE CASCADE`
- history เป็น append-only: application ห้าม update หรือลบ row เดิม

Update behavior:

- ระบุตัวผู้ใช้จาก JWT/request context เท่านั้น ห้ามรับ `mobileUserUuid` จาก client
- ภายใน transaction ให้ query
  `SELECT MOBILE_NUMBER FROM m_mobile_users WHERE MOBILE_UUID = :mobileUserUuid FOR UPDATE`
- normalize และ validate `mobileNumber` เป็นหมายเลขโทรศัพท์ไทย 10 หลักก่อนเขียนข้อมูล
- ถ้าเบอร์ใหม่เท่ากับเบอร์ปัจจุบัน ให้ถือว่า update profile สำเร็จโดยไม่เพิ่ม history
- ถ้าเบอร์เปลี่ยน ให้ insert ค่าเดิมและค่าใหม่ลง `m_mobile_number_history` ก่อน update
  `m_mobile_users.MOBILE_NUMBER`, `UPDATE_BY` และ `UPDATE_DATE`
- history insert และ user update ต้องอยู่ใน transaction เดียวกัน; เมื่อขั้นตอนใดล้มเหลวต้อง rollback ทั้งคู่
- `changed_by` ใช้ username/email ของ authenticated user จาก server ห้ามรับจาก request body
- ไม่กำหนดให้เบอร์โทร unique ใน task นี้ เพราะ schema ปัจจุบันไม่มี unique constraint และยังไม่มี requirement ยืนยัน

Acceptance criteria:

- เปลี่ยนเบอร์จากค่าเดิมเป็นค่าใหม่แล้ว `m_mobile_users.MOBILE_NUMBER` มีค่าใหม่ และ history มีหนึ่ง row ที่ค่า old/new ถูกต้อง
- เปลี่ยนจาก `NULL` เป็นเบอร์ใหม่ได้และ history เก็บ `old_mobile_number = NULL`
- ส่งเบอร์เดิมซ้ำไม่สร้าง history ซ้ำ
- reject ค่า blank, ไม่ใช่ตัวเลข หรือความยาวไม่ครบ 10 หลัก โดยไม่เปลี่ยน user/history
- concurrent updates ของ user เดียวกันถูก serialize ด้วย `FOR UPDATE` และแต่ละ history row ต่อค่าจาก state ก่อนหน้าจริง
- เมื่อ insert history หรือ update user ล้มเหลว ต้องไม่มี partial write
- มี repository/service/controller tests สำหรับ changed, unchanged, old value เป็น `NULL`, invalid format,
  unauthenticated request, rollback และ concurrent update
- อัปเดต OpenAPI description ให้ระบุการเก็บประวัติการเปลี่ยนเบอร์

Implementation evidence:

- `ProfileService.profileUpdate()` กำหนด `@Transactional` และอ่าน authenticated user จาก request context
- `UserRepository` lock ค่าเดิม, insert append-only history และใช้ authenticated username ใน `UPDATE_BY`
- focused tests `ProfileMobileNumberUpdateTest` และ `ProfileMobileNumberValidationTest` ผ่าน 5/5 cases

ตัวอย่าง cURL:

```bash
curl --request POST \
  --url "${base_url}/v1/profile-update" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Content-Type: application/json" \
  --header "Accept-Language: TH" \
  --data '{
    "firstName": "ศรัญญู",
    "lastName": "แก้วโสภา",
    "companyCode": "COMP001",
    "dateOfBirth": "1990-01-01",
    "positionCode": "POS001",
    "email": "user@example.com",
    "mobileNumber": "0812345678",
    "isChangeFile": "N"
  }'
```

## Recommended Implementation Order

1. ปิด open decisions: request number, unpaid draft, address และ payment channel
2. MR-MOB-01 shared foundation
3. MR-MOB-02 status และ MR-MOB-03 price
4. MR-MOB-15 identity document multi-file upload และปรับ profile document validation
5. MR-MOB-13 Thailand address master APIs ก่อนเริ่ม delivery-address UI/API
6. MR-MOB-14 create/update delivery address APIs และ MR-MOB-16 get default delivery address
7. MR-MOB-04 create request
8. MR-MOB-08 renewal item upload และ MR-MOB-09 resubmit โดย reuse file validation/storage component จาก MR-MOB-15
9. MR-MOB-05 list, MR-MOB-06 detail และ MR-MOB-07 timeline
10. MR-MOB-10 payment attempt, payment webhook และ MR-MOB-11 payment status
11. MR-MOB-12 contract/integration/concurrency tests
12. MR-MOB-17 atomic mobile-number update และ change history

## Evidence Checked

- `src/main/java/com/seaman/constant/Routes.java`
- `src/main/java/com/seaman/controller/DocumentController.java`
- `src/main/java/com/seaman/service/DocumentService.java`
- `src/main/java/com/seaman/repository/DocumentRepository.java`
- `src/main/java/com/seaman/entity/DocumentRequestItemEntity.java`
- `src/main/java/com/seaman/model/response/DocumentRequestItemResponse.java`
- `documents/mvp1/script/RUN1_2026_create_table.sql`
- `documents/mvp1/script/RUN2_2026_insert_master.sql`
- `documents/mvp1/script/RUN3_create_index.sql`
- `documents/mvp1/script/RUN4_external_data`
- `documents/mvp1/script/RUN5_document_renewal_foundation.sql`
- `documents/mvp1/script/RUN7_create_renewal_request_draft.sql`
- `documents/mvp1/script/RUN9_harden_delivery_address_default.sql`
- `documents/mvp1/script/RUN10_identity_document_multi_file.sql`
- `documents/mvp1/script/RUN12_mobile_number_history.sql`
- `src/main/java/com/seaman/controller/ProfileController.java`
- `src/main/java/com/seaman/service/ProfileService.java`
- `src/main/java/com/seaman/repository/UserRepository.java`
- `documents/non_prod_db_schema.md`
