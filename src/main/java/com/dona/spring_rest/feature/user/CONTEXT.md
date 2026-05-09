# User Feature - Complete Implementation Context

## Overview
The User feature implements a complete CRUD (Create, Read, Update, Delete) system for managing application users. Users have email-based authentication, can be assigned to companies, and can have multiple roles for authorization.

## Entity Structure

### User Entity (`User.java`)
- **Database Table**: `users`
- **Primary Key**: `id` (Long, auto-generated)

#### Fields

| Field | Type | Constraints | Description |
|-------|------|-----------|-------------|
| `id` | Long | @Id, @GeneratedValue | Unique user identifier |
| `name` | String | @NotBlank, @Size(2-100), not null | Full name |
| `email` | String | @NotBlank, @Email, unique, not null | Login email |
| `password` | String | @NotBlank, @Size(8+), not null | BCrypt hashed password |
| `age` | Integer | Nullable | User age |
| `address` | String | Nullable, max 255 | Physical address |
| `gender` | Gender Enum | Nullable | Gender (MALE, FEMALE, OTHER) |
| `avatar` | String | Nullable | Avatar image path/URL |
| `company` | Company | @ManyToOne, FetchType.LAZY, nullable | Employee's company |
| `roles` | List<Role> | @ManyToMany via user_role, FetchType.LAZY | Assigned roles |
| `createdAt` | Instant | Not null | Creation timestamp |
| `updatedAt` | Instant | Not null | Last update timestamp |

#### Relationships
- **ManyToOne Company**: Each user can belong to one company (optional)
- **ManyToMany Role**: Users can have multiple roles via `user_role` join table
- **Fetch Strategy**: LAZY loading for both relationships to avoid N+1 queries

#### Validation Rules (Vietnamese Messages)
- Email must be valid format: "Email không đúng định dạng"
- Name must not be blank: "Tên không được để trống"
- Name length: 2-100 characters - "Tên phải từ 2 đến 100 ký tự"
- Password must not be blank: "Mật khẩu không được để trống"
- Password minimum length: 8 characters - "Mật khẩu phải ít nhất 8 ký tự"
- Email must be unique in the database
- Address max 255 characters

### Gender Enum
```java
public enum Gender {
    MALE("Nam"),
    FEMALE("Nữ"),
    OTHER("Khác");
}
```

## Repository Layer

### UserRepository Interface
Extends `JpaRepository<User, Long>` with custom query methods:

```java
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
```

#### Key Methods
- `findAll()` - Retrieve all users (inherited from JpaRepository)
- `findById(Long)` - Find user by primary key (inherited)
- `save(User)` - Create or update user (inherited)
- `delete(User)` - Delete user by entity (inherited)
- `existsByEmail(String)` - Check if email already exists
- `findByEmail(String)` - Find user by email address

## Service Layer

### UserService Interface (`UserService.java`)
Defines the contract for user operations:

```java
public interface UserService {
    List<User> getAllUsers();
    User getUserById(Long id);
    User createUser(User user, List<Long> roleIds);
    User updateUser(Long id, User user, List<Long> roleIds);
    void deleteUser(Long id);
    boolean existsByEmail(String email);
    User getUserByEmail(String email);
}
```

### UserServiceImpl Implementation (`UserServiceImpl.java`)

#### CRUD Operations with Advanced Features

**getAllUsers()**
- Returns list of all users with relationships populated
- Transactional: READ-ONLY
- Uses: `userRepository.findAll()`

**getUserById(Long id)**
- Retrieves user by ID
- Throws: `ResourceNotFoundException` if user not found
- Message format: "Người dùng with id {id} not found"
- Transactional: READ-ONLY

**createUser(User user, List<Long> roleIds)**
- Creates new user with email validation and password hashing
- **Email Validation**: Checks if email already exists
  - Throws: `DuplicateResourceException` if email exists
- **Company Assignment**: If companyId provided:
  - Validates company exists
  - Throws: `ResourceNotFoundException` if not found
- **Password Hashing**: Encodes password using PasswordEncoder (BCrypt)
- **Role Assignment**: Validates and assigns all provided roles
  - Throws: `ResourceNotFoundException` if any role not found
- Auto-sets: `createdAt` and `updatedAt` timestamps
- Returns: Created user entity with relationships
- Transactional: WRITE

**updateUser(Long id, User user, List<Long> roleIds)**
- Updates existing user with smart password handling
- **Email Validation**: Only checks duplicate if email is changed
- **Password Handling**:
  - If password field is provided and not blank → hash it
  - If password is null or blank → keep existing password
- **Company Assignment**: Supports clearing company (set to null)
- **Role Assignment**: Replaces entire role list with new one
- Auto-updates: `updatedAt` timestamp
- Returns: Updated user entity
- Throws: `ResourceNotFoundException` if user not found
- Throws: `DuplicateResourceException` if email duplicate (excluding self)
- Throws: `ResourceNotFoundException` if company/roles not found
- Transactional: WRITE

**deleteUser(Long id)**
- Deletes user by ID
- Automatically clears `user_role` join table entries (cascade)
- Throws: `ResourceNotFoundException` if user not found
- Returns: void
- Transactional: WRITE

**existsByEmail(String email)**
- Helper method to check email existence
- Returns: boolean
- Transactional: READ-ONLY

**getUserByEmail(String email)**
- Retrieves user by email
- Throws: `ResourceNotFoundException` if not found
- Returns: User entity
- Transactional: READ-ONLY

#### Error Handling
- **ResourceNotFoundException**: Thrown when user/company/role not found
  - HTTP Status: 404
  
- **DuplicateResourceException**: Thrown when email already exists
  - HTTP Status: 409

## Controller Layer

### UserController (`UserController.java`)

**Base Path**: `/api/v1/users`

#### Endpoints

| Method | Endpoint | Status | Response |
|--------|----------|--------|----------|
| GET | `/api/v1/users` | 200 | List of UserResponse |
| GET | `/api/v1/users/{id}` | 200 | UserResponse |
| POST | `/api/v1/users` | 201 | UserResponse |
| PUT | `/api/v1/users/{id}` | 200 | UserResponse |
| DELETE | `/api/v1/users/{id}` | 204 | No Content |

#### Request/Response Format

**GET /api/v1/users** - Get all users
```json
Request: No body

Response (200):
{
  "statusCode": 200,
  "message": "Danh sách người dùng được lấy thành công",
  "data": [
    {
      "id": 1,
      "name": "Nguyen Van A",
      "email": "user@example.com",
      "age": 25,
      "address": "Ho Chi Minh City",
      "gender": "MALE",
      "avatar": null,
      "company": {
        "id": 1,
        "name": "Tech Corp"
      },
      "roles": [
        { "id": 1, "name": "ADMIN" }
      ],
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    }
  ],
  "error": null,
  "details": null,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**POST /api/v1/users** - Create new user
```json
Request (201):
{
  "name": "Tran Thi B",
  "email": "tran@example.com",
  "password": "password123",
  "age": 30,
  "gender": "FEMALE",
  "address": "Ha Noi",
  "companyId": 1,
  "roleIds": [3, 5]
}

Response (201):
{
  "statusCode": 201,
  "message": "Người dùng được tạo thành công",
  "data": {
    "id": 2,
    "name": "Tran Thi B",
    "email": "tran@example.com",
    "age": 30,
    "address": "Ha Noi",
    "gender": "FEMALE",
    "avatar": null,
    "company": { "id": 1, "name": "Tech Corp" },
    "roles": [
      { "id": 3, "name": "HR" },
      { "id": 5, "name": "USER" }
    ],
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  },
  "error": null,
  "details": null,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**PUT /api/v1/users/{id}** - Update user
```json
Request:
{
  "name": "Tran Thi B Updated",
  "email": "tran_updated@example.com",
  "age": 31,
  "gender": "FEMALE",
  "address": "Da Nang",
  "companyId": 2,
  "roleIds": [3],
  "password": "newPassword123"
}

Response (200):
{
  "statusCode": 200,
  "message": "Người dùng được cập nhật thành công",
  "data": {
    "id": 2,
    "name": "Tran Thi B Updated",
    "email": "tran_updated@example.com",
    "age": 31,
    "address": "Da Nang",
    "gender": "FEMALE",
    "avatar": null,
    "company": { "id": 2, "name": "Another Corp" },
    "roles": [{ "id": 3, "name": "HR" }],
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T11:00:00Z"
  },
  "error": null,
  "details": null,
  "timestamp": "2024-01-15T11:00:00Z"
}
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
    "email must be valid",
    "password must be at least 8 characters"
  ],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**404 Not Found** - User/Company/Role not found
```json
{
  "statusCode": 404,
  "message": "Người dùng with id 999 not found",
  "data": null,
  "error": "Resource not found",
  "details": null,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**409 Conflict** - Duplicate email
```json
{
  "statusCode": 409,
  "message": "Người dùng with email user@example.com already exists",
  "data": null,
  "error": "Duplicate resource",
  "details": null,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Data Transfer Objects (DTOs)

### CreateUserRequest
```java
public record CreateUserRequest(
    @NotBlank
    @Size(min = 2, max = 100)
    String name,

    @NotBlank
    @Email
    String email,

    @NotBlank
    @Size(min = 8)
    String password,

    Integer age,
    String address,
    Gender gender,
    Long companyId,
    List<Long> roleIds
)
```

### UpdateUserRequest
```java
public record UpdateUserRequest(
    @NotBlank
    @Size(min = 2, max = 100)
    String name,

    @NotBlank
    @Email
    String email,

    Integer age,
    String address,
    Gender gender,
    Long companyId,
    List<Long> roleIds,
    String password  // Optional: if null/blank, password is not changed
)
```

### UserResponse
```java
public record UserResponse(
    Long id,
    String name,
    String email,
    Integer age,
    String address,
    Gender gender,
    String avatar,
    CompanyBasicResponse company,  // Nested: id + name only
    List<RoleBasicResponse> roles, // Nested: id + name only
    Instant createdAt,
    Instant updatedAt
)
```

**Important**: UserResponse does NOT include password field for security.

## Test Coverage

### Unit Tests (`UserServiceImplTest.java`) - 20+ Test Cases
Tests for the UserServiceImpl class using Mockito to mock dependencies.

#### Get All Users Tests (2 cases)
- Retrieves list of users
- Returns empty list when no users exist

#### Get User By ID Tests (2 cases)
- Retrieves user by valid ID
- Throws ResourceNotFoundException for invalid ID

#### Create User Tests (4 cases)
- Creates user with company and roles
- Throws DuplicateResourceException for duplicate email
- Throws ResourceNotFoundException for invalid company
- Validates role IDs

#### Update User Tests (5 cases)
- Updates user successfully
- Hash new password only if provided
- Does not hash password if null or blank
- Throws ResourceNotFoundException when not found
- Throws DuplicateResourceException for duplicate email

#### Delete User Tests (2 cases)
- Deletes user successfully
- Throws ResourceNotFoundException when not found

#### Helper Method Tests (3 cases)
- existsByEmail returns true when email exists
- existsByEmail returns false when email doesn't exist
- getUserByEmail returns user or throws exception

### Integration Tests (`UserControllerTest.java`) - 15 Test Cases
Tests for the UserController REST API endpoints using Spring Boot Test.

#### GET /api/v1/users Tests (2 cases)
- Returns 200 with user list
- Returns 200 with empty list

#### GET /api/v1/users/{id} Tests (2 cases)
- Returns 200 with user data
- Returns 404 when not found

#### POST /api/v1/users Tests (4 cases)
- Returns 201 with created user
- Returns 400 for invalid data
- Returns 409 for duplicate email
- Returns 404 for invalid company

#### PUT /api/v1/users/{id} Tests (4 cases)
- Returns 200 with updated user
- Returns 400 for invalid data
- Returns 404 when not found
- Returns 409 for duplicate email

#### DELETE /api/v1/users/{id} Tests (2 cases)
- Returns 204 on successful delete
- Returns 404 when not found

## Package Structure
```
src/main/java/com/dona/spring_rest/feature/user/
├── User.java
├── Gender.java
├── UserRepository.java
├── UserService.java
├── UserServiceImpl.java
├── UserController.java
└── dto/
    ├── CreateUserRequest.java
    ├── UpdateUserRequest.java
    ├── UserResponse.java
    ├── CompanyBasicResponse.java
    └── RoleBasicResponse.java

src/test/java/com/dona/spring_rest/feature/user/
├── UserServiceImplTest.java
└── UserControllerTest.java
```

## Integration Points

### Dependencies
- **Company Entity**: Via @ManyToOne relationship
- **Role Entity**: Via @ManyToMany relationship
- **UserRepository**: Data access layer
- **PasswordEncoder**: Spring Security bean for password hashing (BCrypt)
- **ApiResponse**: Response wrapper from `com.dona.spring_rest.dto`
- **ResourceNotFoundException**: Error handling
- **DuplicateResourceException**: Duplicate validation

### Related Features
- **Company**: Users are assigned to companies (ManyToOne)
- **Role**: Users have roles (ManyToMany) for authorization
- **Permission**: Roles have permissions (for future RBAC implementation)

## Security Considerations

1. **Password Hashing**: All passwords are hashed using BCrypt (PasswordEncoder)
2. **Password Never Exposed**: UserResponse DTO excludes password field
3. **Email Uniqueness**: Email is unique constraint to prevent duplicates
4. **Company Privacy**: Users can only see basic company info in nested response
5. **Role Privacy**: Roles returned with minimal info (id + name)

## Notes

- User creation auto-sets `createdAt` and `updatedAt` timestamps
- User updates auto-update the `updatedAt` timestamp
- ManyToMany relationships use LAZY loading for performance
- Company assignment is optional (nullable)
- Role assignment is flexible (can add/remove roles on update)
- Password update is smart: only hash if explicitly provided and not blank
- All error messages are in Vietnamese
- Responses follow ApiResponse wrapper pattern with consistent format
- Tests use Mockito for unit tests and manual MockMvc setup for integration tests
- Spring Boot 4.0.5 compatible test framework

## Future Enhancements

1. **Pagination**: Add pagination support for getAllUsers endpoint
2. **Search/Filter**: Find users by name, email, company, or role
3. **Batch Operations**: Support bulk create/update/delete
4. **Avatar Upload**: Implement file upload for user avatars
5. **User Preferences**: Store user preferences (theme, language, etc.)
6. **Soft Delete**: Instead of hard delete with deletion timestamp
7. **Audit Trail**: Track all user changes with who/when/what
8. **Profile Completion**: Track user profile completion percentage
