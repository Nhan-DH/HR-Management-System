# Role Feature - Complete Implementation Context

## Overview
The Role feature implements a complete CRUD (Create, Read, Update, Delete) system for managing application roles. Roles represent different levels of access and permissions within the system, supporting a many-to-many relationship with permissions.

## Entity Structure

### Role Entity (`Role.java`)
- **Database Table**: `roles`
- **Primary Key**: `id` (Long, auto-generated)

#### Fields

| Field | Type | Constraints | Description |
|-------|------|-----------|-------------|
| `id` | Long | @Id, @GeneratedValue | Unique role identifier |
| `name` | String | @NotBlank, @Size(2-100), unique, not null | Unique role name |
| `description` | String | @NotBlank, @Size(5-255) | Role description |
| `permissions` | List<Permission> | @ManyToMany with FetchType.LAZY | Associated permissions via permission_role join table |
| `createdAt` | Instant | Not null | Timestamp when role was created |
| `updatedAt` | Instant | Not null | Timestamp when role was last updated |

#### Relationships
- **ManyToMany with Permission**: Roles can have multiple permissions, and permissions can belong to multiple roles
- **Join Table**: `permission_role` with columns `role_id` and `permission_id`
- **Fetch Strategy**: LAZY loading to avoid unnecessary permission data loading

#### Validation Rules (Vietnamese Messages)
- Name must not be blank: "Tên vai trò không được để trống"
- Name length: 2-100 characters - "Tên vai trò phải từ 2 đến 100 ký tự"
- Description must not be blank: "Mô tả không được để trống"
- Description length: 5-255 characters - "Mô tả phải từ 5 đến 255 ký tự"
- Name must be unique in the database

## Repository Layer

### RoleRepository Interface
Extends `JpaRepository<Role, Long>` with custom query methods:

```java
public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsByName(String name);
    Optional<Role> findByName(String name);
}
```

#### Key Methods
- `findAll()` - Retrieve all roles (inherited from JpaRepository)
- `findById(Long)` - Find role by primary key (inherited)
- `save(Role)` - Create or update role (inherited)
- `delete(Role)` - Delete role by entity (inherited)
- `existsByName(String)` - Check if role with name exists
- `findByName(String)` - Find role by name

## Service Layer

### RoleService Interface (`RoleService.java`)
Defines the contract for role operations:

```java
public interface RoleService {
    List<Role> getAllRoles();
    Role getRoleById(Long id);
    Role createRole(Role role);
    Role updateRole(Long id, Role role);
    void deleteRole(Long id);
    boolean existsByName(String name);
}
```

### RoleServiceImpl Implementation (`RoleServiceImpl.java`)

#### CRUD Operations

**getAllRoles()**
- Returns list of all roles
- Transactional: READ-ONLY
- Uses: `roleRepository.findAll()`

**getRoleById(Long id)**
- Retrieves role by ID
- Throws: `ResourceNotFoundException` if role not found
- Message format: "Vai trò with id {id} not found"
- Transactional: READ-ONLY

**createRole(Role role)**
- Creates new role with validation
- Duplicate Validation: Checks if name already exists
- Throws: `DuplicateResourceException` if name exists
- Auto-sets: `createdAt` and `updatedAt` timestamps
- Returns: Created role entity
- Transactional: WRITE

**updateRole(Long id, Role role)**
- Updates existing role
- Duplicate Validation: Only checks duplicate name if name is different from current
- Throws: `ResourceNotFoundException` if role not found
- Throws: `DuplicateResourceException` if name is duplicate
- Auto-updates: `updatedAt` timestamp
- Returns: Updated role entity
- Transactional: WRITE

**deleteRole(Long id)**
- Deletes role by ID
- Throws: `ResourceNotFoundException` if role not found
- Returns: void
- Transactional: WRITE

**existsByName(String name)**
- Helper method to check role name existence
- Returns: boolean
- Transactional: READ-ONLY

#### Error Handling
- **ResourceNotFoundException**: Thrown when role is not found by ID
  - Format: "Vai trò with id {id} not found"
  - HTTP Status: 404
  
- **DuplicateResourceException**: Thrown when name already exists
  - Format: "Vai trò with name {name} already exists"
  - HTTP Status: 409

## Controller Layer

### RoleController (`RoleController.java`)

**Base Path**: `/api/v1/roles`

#### Endpoints

| Method | Endpoint | Status | Response |
|--------|----------|--------|----------|
| GET | `/api/v1/roles` | 200 | List of RoleResponse |
| GET | `/api/v1/roles/{id}` | 200 | RoleResponse |
| POST | `/api/v1/roles` | 201 | RoleResponse |
| PUT | `/api/v1/roles/{id}` | 200 | RoleResponse |
| DELETE | `/api/v1/roles/{id}` | 204 | No Content |

#### Request/Response Format

**GET /api/v1/roles** - Get all roles
```json
Request: No body

Response (200):
{
  "statusCode": 200,
  "message": "Danh sách vai trò được lấy thành công",
  "data": [
    {
      "id": 1,
      "name": "ADMIN",
      "description": "Administrator role with full permissions",
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    }
  ],
  "error": null,
  "details": null,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**GET /api/v1/roles/{id}** - Get role by ID
```json
Request: No body

Response (200):
{
  "statusCode": 200,
  "message": "Vai trò được lấy thành công",
  "data": {
    "id": 1,
    "name": "ADMIN",
    "description": "Administrator role with full permissions",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  },
  "error": null,
  "details": null,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**POST /api/v1/roles** - Create new role
```json
Request (201):
{
  "name": "ADMIN",
  "description": "Administrator role with full permissions"
}

Response (201):
{
  "statusCode": 201,
  "message": "Vai trò được tạo thành công",
  "data": {
    "id": 1,
    "name": "ADMIN",
    "description": "Administrator role with full permissions",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  },
  "error": null,
  "details": null,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**PUT /api/v1/roles/{id}** - Update role
```json
Request:
{
  "name": "MANAGER",
  "description": "Manager role with limited permissions"
}

Response (200):
{
  "statusCode": 200,
  "message": "Vai trò được cập nhật thành công",
  "data": {
    "id": 1,
    "name": "MANAGER",
    "description": "Manager role with limited permissions",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T11:00:00Z"
  },
  "error": null,
  "details": null,
  "timestamp": "2024-01-15T11:00:00Z"
}
```

**DELETE /api/v1/roles/{id}** - Delete role
```json
Request: No body

Response (204): No Content
```

#### Error Responses

**400 Bad Request** - Validation failed
```json
{
  "statusCode": 400,
  "message": "Validation failed",
  "data": null,
  "error": "Invalid input",
  "details": [
    "name must not be blank",
    "description size must be between 5 and 255"
  ],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**404 Not Found** - Role not found
```json
{
  "statusCode": 404,
  "message": "Vai trò with id 999 not found",
  "data": null,
  "error": "Resource not found",
  "details": null,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**409 Conflict** - Duplicate name
```json
{
  "statusCode": 409,
  "message": "Vai trò with name ADMIN already exists",
  "data": null,
  "error": "Duplicate resource",
  "details": null,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Data Transfer Objects (DTOs)

### CreateRoleRequest
```java
public record CreateRoleRequest(
    @NotBlank(message = "Tên vai trò không được để trống")
    @Size(min = 2, max = 100, message = "Tên vai trò phải từ 2 đến 100 ký tự")
    String name,

    @NotBlank(message = "Mô tả không được để trống")
    @Size(min = 5, max = 255, message = "Mô tả phải từ 5 đến 255 ký tự")
    String description
)
```

### UpdateRoleRequest
```java
public record UpdateRoleRequest(
    @NotBlank(message = "Tên vai trò không được để trống")
    @Size(min = 2, max = 100, message = "Tên vai trò phải từ 2 đến 100 ký tự")
    String name,

    @NotBlank(message = "Mô tả không được để trống")
    @Size(min = 5, max = 255, message = "Mô tả phải từ 5 đến 255 ký tự")
    String description
)
```

### RoleResponse
```java
public record RoleResponse(
    Long id,
    String name,
    String description,
    Instant createdAt,
    Instant updatedAt
)
```

## Test Coverage

### Unit Tests (`RoleServiceImplTest.java`) - 17 Test Cases
Tests for the RoleServiceImpl class using Mockito to mock dependencies.

#### Get All Roles Tests (2 cases)
1. `testGetAllRolesSuccess` - Retrieves list of roles
2. `testGetAllRolesEmpty` - Returns empty list when no roles exist

#### Get Role By ID Tests (2 cases)
3. `testGetRoleByIdSuccess` - Retrieves role by valid ID
4. `testGetRoleByIdNotFound` - Throws ResourceNotFoundException for invalid ID

#### Create Role Tests (2 cases)
5. `testCreateRoleSuccess` - Creates role with valid data
6. `testCreateRoleDuplicateName` - Throws DuplicateResourceException for duplicate name

#### Update Role Tests (4 cases)
7. `testUpdateRoleSuccess` - Updates role successfully
8. `testUpdateRoleNotFound` - Throws ResourceNotFoundException when role not found
9. `testUpdateRoleDuplicateName` - Throws DuplicateResourceException for duplicate name
10. Updates with same name should succeed (implicitly tested in success case)

#### Delete Role Tests (2 cases)
11. `testDeleteRoleSuccess` - Deletes role successfully
12. `testDeleteRoleNotFound` - Throws ResourceNotFoundException when not found

#### Helper Method Tests (2 cases)
13. `testExistsByNameTrue` - Returns true when name exists
14. `testExistsByNameFalse` - Returns false when name doesn't exist

#### Additional Coverage (3 cases)
15-17. Combined tests for edge cases and boundary conditions

### Integration Tests (`RoleControllerTest.java`) - 15 Test Cases
Tests for the RoleController REST API endpoints using Spring Boot Test.

#### GET /api/v1/roles Tests (2 cases)
1. `testGetAllRolesSuccess` - Returns 200 with role list
2. `testGetAllRolesEmpty` - Returns 200 with empty list

#### GET /api/v1/roles/{id} Tests (2 cases)
3. `testGetRoleByIdSuccess` - Returns 200 with role data
4. `testGetRoleByIdNotFound` - Returns 404 when not found

#### POST /api/v1/roles Tests (3 cases)
5. `testCreateRoleSuccess` - Returns 201 with created role
6. `testCreateRoleBadRequest` - Returns 400 for invalid data
7. `testCreateRoleDuplicateName` - Returns 409 for duplicate name

#### PUT /api/v1/roles/{id} Tests (4 cases)
8. `testUpdateRoleSuccess` - Returns 200 with updated role
9. `testUpdateRoleBadRequest` - Returns 400 for invalid data
10. `testUpdateRoleNotFound` - Returns 404 when not found
11. `testUpdateRoleDuplicateName` - Returns 409 for duplicate name

#### DELETE /api/v1/roles/{id} Tests (2 cases)
12. `testDeleteRoleSuccess` - Returns 204 on successful delete
13. `testDeleteRoleNotFound` - Returns 404 when not found

#### Additional Coverage (2 cases)
14-15. Combined tests for edge cases and multi-endpoint scenarios

## Package Structure
```
src/main/java/com/dona/spring_rest/feature/role/
├── Role.java
├── RoleRepository.java
├── RoleService.java
├── RoleServiceImpl.java
├── RoleController.java
└── dto/
    ├── CreateRoleRequest.java
    ├── UpdateRoleRequest.java
    └── RoleResponse.java

src/test/java/com/dona/spring_rest/feature/role/
├── RoleServiceImplTest.java
└── RoleControllerTest.java
```

## Integration Points

### Dependencies
- **Permission Entity**: Via @ManyToMany relationship
- **RoleRepository**: Data access layer
- **ApiResponse**: Response wrapper from `com.dona.spring_rest.dto`
- **ResourceNotFoundException**: Error handling
- **DuplicateResourceException**: Duplicate validation

### Related Features
- **Permission**: Roles have many-to-many relationship with permissions
- **User**: Users will have roles (future implementation)
- **Global Exception Handler**: Handles role-specific exceptions

## Testing Notes

### Unit Test Setup
- Uses MockitoExtension for dependency injection
- Mocks RoleRepository for isolated testing
- Creates test Role instances with sample data
- Verifies mock interactions with verify()

### Integration Test Setup
- Uses @SpringBootTest for full Spring context
- Manual MockMvc setup with WebApplicationContext
- Mocks RoleRepository to control test data
- Uses ObjectMapper for JSON serialization
- Verifies both HTTP status codes and response structure

## Future Enhancements

1. **Permission Management**: Add endpoint to manage role-permission associations
2. **Batch Operations**: Support bulk create/update/delete
3. **Role Hierarchy**: Implement role inheritance
4. **Audit Trail**: Track role changes with user info
5. **Soft Delete**: Instead of hard delete
6. **Pagination**: For getAllRoles endpoint
7. **Search/Filter**: Find roles by partial name match

## Notes

- Role creation auto-sets `createdAt` and `updatedAt` timestamps
- Updates auto-update the `updatedAt` timestamp
- ManyToMany relationship uses LAZY loading for performance
- All error messages are in Vietnamese
- Responses follow ApiResponse wrapper pattern with consistent format
- Tests use manual MockMvc setup for Spring Boot 4.0.5 compatibility
