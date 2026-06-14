# document-spec-api

Generate a complete API specification document from the current source code and export it as an HTML file.

## Steps

1. **Scan all controllers** — read every `*.java` file under `src/main/java/com/seaman/controller/` (skip `BaseController`, `advice/`). For each controller collect:
   - HTTP method (`@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping`)
   - Route path (resolve the constant from `Routes.java` to its actual string value)
   - Method name and brief purpose (infer from method name + service call)
   - `@PathVariable` and `@RequestParam` parameters
   - `@RequestBody` type (if any)
   - Response type (generic parameter of `SuccessResponse<T>`)
   - Auth required: **No** for public routes (login, register, master data, health), **Yes** for everything else

2. **Scan request/response models** — for each Request/Response class referenced by the endpoints, read its fields (name, type, `@NotNull`/`@NotBlank`/`@Size` constraints, `@JsonProperty` aliases). Skip internal/utility classes.

3. **Read `Routes.java`** to resolve all route constant values.

4. **Build the spec** — group endpoints by resource tag (Auth, Admin, Course, News, Form, Banner, Group, Voucher, Certificate, User Mobile, Master, Menu, Home). For each endpoint produce:
   - Method badge + full path (e.g. `POST /login`)
   - Short description
   - Auth required badge
   - Request headers table (mandatory: `Language`, `device-model`, `correlation-id`; plus `Authorization: Bearer <token>` when auth required)
   - Path/query parameters table (if any)
   - Request body fields table (field | type | required | description)
   - Response body note (shape: `{ code, description, data: <ResponseType> }`)

5. **Write HTML** — generate a well-styled, self-contained single-file HTML document with:
   - Inline CSS (dark sidebar, white content area, method badges colored by HTTP verb: GET=green, POST=blue, PUT=orange, DELETE=red)
   - Sidebar navigation with resource group links
   - Each endpoint as a collapsible `<details>` section (open by default)
   - Tables for parameters and request/response fields
   - A header banner with project name "Smart Seaman BOS API" and base URL `/v1`
   - No external CDN or JS frameworks — plain HTML/CSS only
   - Save the file to `docs/api-spec.html` (create `docs/` if it doesn't exist)

6. **Report** — after writing the file, print the absolute path and total endpoint count.

## Notes
- All routes are prefixed `/v1` (from `Routes.VERSION`).
- Public endpoints (no auth): `POST /login`, `POST /register`, `GET /health`, `GET /master/**`, `/actuator/**`, Swagger paths.
- The standard response envelope is `{ "code": "string", "description": "string", "data": <T> }`.
- Required headers on every authenticated request: `Language` (TH|EN), `device-model`, `correlation-id`, `Authorization: Bearer <token>`.
