# Permission Feature Context

## Overview
The Permission feature implements CRUD operations for managing API permissions in the system. Permissions define which API endpoints (method + path) are allowed for different user roles.

## Database Schema

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| name | VARCHAR(100) | NOT NULL | Unique permission identifier (e.g., CREATE_USER, VIEW_REPORT) |
| apiPath | VARCHAR(255) | NOT NULL | API endpoint path (e.g., /api/v1/users) |
| method | VARCHAR(10) | NOT NULL | HTTP method: GET, POST, PUT, DELETE, PATCH |
| module | VARCHAR(100) | NOT NULL | Feature module (e.g., USER, COMPANY, ROLE) |
| createdAt | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| updatedAt | TIMESTAMP | NULLABLE | |

**Unique Constraint:** `(apiPath, method)` — Ensures each endpoint has unique permissions

## Entity Fields

### id
- **Type:** Long
- **Constraints:** @Id, @GeneratedValue(IDENTITY)
- **Purpose:** Primary key, auto-generated

### name
- **Type:** String
- **Constraints:** @NotBlank, @Size(3-100), @Column(nullable=false)
- **Validation Message:** "Tên quyền không được để trống" | "Tên quyền phải từ 3 đến 100 ký tự"
- **Purpose:** Unique identifier for permission (e.g., CREATE_USER, DELETE_USER)
- **Example:** "CREATE_USER", "VIEW_REPORT"

### apiPath
- **Type:** String
- **Constraints:** @NotBlank, @Size(max 255), @Column(nullable=false)
- **Validation Message:** "Đường dẫn API không được để trống" | "Đường dẫn API không được quá 255 ký tự"
- **Purpose:** API endpoint path that requires this permission
- **Example:** "/api/v1/users", "/api/v1/companies", "/api/v1/roles"

### method
- **Type:** String (Enum-like validation via @Pattern)
- **Constraints:** @NotBlank, @Pattern(GET|POST|PUT|DELETE|PATCH), @Column(nullable=false)
- **Validation Message:** "Phương thức HTTP không được để trống" | "Phương thức HTTP phải là GET, POST, PUT, DELETE hoặc PATCH"
- **Purpose:** HTTP method required for this permission
- **Valid Values:** GET, POST, PUT, DELETE, PATCH
- **Example:** "POST", "GET", "PUT"

### module
- **Type:** String
- **Constraints:** @NotBlank, @Size(2-100), @Column(nullable=false)
- **Validation Message:** "Mô-đun không được để trống" | "Mô-đun phải từ 2 đến 100 ký tự"
- **Purpose:** Feature module this permission belongs to
- **Example:** "USER", "COMPANY", "ROLE", "PERMISSION"

### createdAt / updatedAt
- **Type:** Instant
- **Purpose:** Audit timestamps
- **Set automatically** on entity creation/update

## API Endpoints

### GET /api/v1/permissions
Get all permissions.

**Response:** 200 OK
```json
{
  "statusCode": 200,
  "data": [
    {
      "id": 1,
      "name": "CREATE_USER",
      "apiPath": "/api/v1/users",
      "method": "POST",
      "module": "USER",
      "createdAt": "2024-01-01T10:00:00Z",
      "updatedAt": "2024-01-01T10:00:00Z"
    }
  ],
  "message": "Danh sách quyền được lấy thành công",
  "timestamp": "2024-01-01T10:00:00Z"
}
```

### GET /api/v1/permissions/{id}
Get permission by ID.

**Response:** 200 OK (same structure as above, single object instead of array)  
**Error:** 404 NOT FOUND

### POST /api/v1/permissions
Create new permission.

**Request Body:**
```json
{
  "name": "CREATE_USER",
  "apiPath": "/api/v1/users",
  "method": "POST",
  "module": "USER"
}
```

**Response:** 201 CREATED  
**Errors:**
- 400 BAD REQUEST: Validation failed
- 409 CONFLICT: Duplicate name or (apiPath + method)

### PUT /api/v1/permissions/{id}
Update existing permission.

**Request Body:** Same as POST  
**Response:** 200 OK  
**Errors:**
- 400 BAD REQUEST: Validation failed
- 404 NOT FOUND: Permission doesn't exist
- 409 CONFLICT: Duplicate name or (apiPath + method)

### DELETE /api/v1/permissions/{id}
Delete permission.

**Response:** 204 NO CONTENT  
**Error:** 404 NOT FOUND

## Service Layer

### PermissionService (Interface)
- `getAllPermissions()` → List<Permission>
- `getPermissionById(Long id)` → Permission (throws ResourceNotFoundException)
- `createPermission(Permission)` → Permission (throws DuplicateResourceException)
- `updatePermission(Long id, Permission)` → Permission (throws ResourceNotFoundException, DuplicateResourceException)
- `deletePermission(Long id)` → void (throws ResourceNotFoundException)
- `existsByName(String name)` → boolean
- `existsByApiPathAndMethod(String apiPath, String method)` → boolean

### PermissionServiceImpl
**Key Logic:**

1. **createPermission():**
   - Check if name already exists → throw DuplicateResourceException
   - Check if (apiPath + method) already exists → throw DuplicateResourceException
   - Set createdAt/updatedAt to current Instant
   - Save and return

2. **updatePermission():**
   - Get existing permission by ID (throws ResourceNotFoundException if not found)
   - Only validate duplicates if fields have actually changed
   - Update fields and set updatedAt
   - Save and return

3. **deletePermission():**
   - Get permission by ID (throws ResourceNotFoundException if not found)
   - Delete

4. **getPermissionById():**
   - Find by ID or throw ResourceNotFoundException

5. **getAllPermissions():**
   - Return all permissions

## DTOs

### CreatePermissionRequest (record)
```java
record CreatePermissionRequest(
  String name,      // @NotBlank, @Size(3-100)
  String apiPath,   // @NotBlank, @Size(max 255)
  String method,    // @NotBlank, @Pattern(GET|POST|PUT|DELETE|PATCH)
  String module     // @NotBlank, @Size(2-100)
)
```

### UpdatePermissionRequest (record)
Same structure as CreatePermissionRequest.

### PermissionResponse (record)
```java
record PermissionResponse(
  Long id,
  String name,
  String apiPath,
  String method,
  String module,
  Instant createdAt,
  Instant updatedAt
) {
  static PermissionResponse fromEntity(Permission permission) { ... }
}
```

## Test Coverage

### Unit Tests (PermissionServiceImplTest) — 17 test cases
**Mocks:** PermissionRepository

**Test Categories:**

1. **Get All (2 tests)**
   - Returns list successfully
   - Returns empty list

2. **Get By ID (2 tests)**
   - Returns permission when found
   - Throws ResourceNotFoundException when not found

3. **Create (3 tests)**
   - Creates successfully
   - Throws DuplicateResourceException for duplicate name
   - Throws DuplicateResourceException for duplicate (apiPath + method)

4. **Update (3 tests)**
   - Updates successfully
   - Throws ResourceNotFoundException when not found
   - Throws DuplicateResourceException for duplicate name

5. **Delete (2 tests)**
   - Deletes successfully
   - Throws ResourceNotFoundException when not found

6. **Helper Methods (2 tests)**
   - existsByName() returns true/false
   - existsByApiPathAndMethod() returns true/false

7. **Edge Cases (2 tests)**
   - Update only validates duplicates when fields actually change
   - Delete verifies repository is called exactly once

### Integration Tests (PermissionControllerTest) — 15 test cases
**Mocks:** PermissionService

**Test Categories:**

1. **GET /api/v1/permissions (2 tests)**
   - Returns 200 with permission list
   - Returns 200 with empty list

2. **GET /api/v1/permissions/{id} (2 tests)**
   - Returns 200 with permission when found
   - Returns 404 when not found

3. **POST /api/v1/permissions (4 tests)**
   - Returns 201 when created successfully
   - Returns 400 when validation fails
   - Returns 409 when duplicate name
   - Returns 409 when duplicate (apiPath + method)

4. **PUT /api/v1/permissions/{id} (4 tests)**
   - Returns 200 when updated successfully
   - Returns 400 when validation fails
   - Returns 404 when not found
   - Returns 409 when duplicate name

5. **DELETE /api/v1/permissions/{id} (2 tests)**
   - Returns 204 when deleted successfully
   - Returns 404 when not found

## Repository

### PermissionRepository
Extends `JpaRepository<Permission, Long>`

**Custom Query Methods:**
- `existsByName(String name)` → boolean
- `existsByApiPathAndMethod(String apiPath, String method)` → boolean
- `findByName(String name)` → Optional<Permission>
- `findByApiPathAndMethod(String apiPath, String method)` → Optional<Permission>

## Relationship with Other Features (Planned for Phase 2)

- **Role:** Permission ↔ Role (ManyToMany via permission_role table)
  - After Role feature is complete, permissions can be assigned to roles
  - This enables role-based access control (RBAC)

## Dependencies

- **Frameworks:** Spring Boot, Spring Data JPA, Jakarta Persistence
- **Validation:** Jakarta Validation API
- **Exception Handling:** Custom ResourceNotFoundException, DuplicateResourceException
- **Response Wrapper:** ApiResponse<T>

## Notes

1. **Naming Convention:** Permission names typically follow pattern: `VERB_RESOURCE` (e.g., CREATE_USER, VIEW_REPORT, DELETE_COMPANY)

2. **API Path Uniqueness:** The combination of apiPath + method must be unique to prevent duplicate permission definitions for the same endpoint

3. **Soft Delete:** Currently, deleting a permission completely removes it. For audit purposes, consider implementing soft deletes (add deletedAt timestamp) in future versions.

4. **Lazy Loading:** No relationships in this entity, but when added in Phase 2 (to Roles), use `FetchType.LAZY` for all relationships.

5. **Module Field:** Used for logical grouping of permissions by feature (USER, COMPANY, ROLE, etc.) to make permission lists more readable and maintainable.
