# Company Feature Context

## Overview
Company CRUD feature manages company information with complete validation, duplicate checks, and RESTful API endpoints.

## Directory Structure
```
src/main/java/com/dona/spring_rest/feature/company/
├── Company.java                    # JPA Entity
├── CompanyRepository.java          # Spring Data JPA Repository
├── CompanyService.java             # Service Interface
├── CompanyServiceImpl.java          # Service Implementation
├── CompanyController.java          # REST Controller
└── dto/
    ├── CompanyRequest.java         # Request DTO
    └── CompanyResponse.java        # Response DTO

src/test/java/com/dona/spring_rest/feature/company/
├── CompanyServiceImplTest.java     # Unit Tests
└── CompanyControllerTest.java      # Integration Tests
```

## Entity Fields
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | Long | PK, AUTO_INCREMENT | |
| name | String | NOT NULL, UNIQUE, 2-200 chars | Company name |
| description | String | NOT NULL, 10-1000 chars | Company description |
| address | String | NOT NULL, 5-255 chars | Physical address |
| email | String | NOT NULL, UNIQUE, valid email | Contact email |
| phone | String | NOT NULL, 10-11 digits | Contact phone |
| website | String | NOT NULL, max 255 chars | Company website URL |
| taxCode | String | NOT NULL, UNIQUE, max 50 chars | Tax identification number |
| numberOfEmployees | Integer | NULLABLE, max 50 | Employee count |
| logo | String | NULLABLE, LONGTEXT | Company logo (base64 or URL) |
| createdAt | Instant | NOT NULL | Timestamp |
| updatedAt | Instant | NOT NULL | Timestamp |

## API Endpoints
```
GET    /api/companies              - Get all companies
GET    /api/companies/{id}         - Get company by ID
POST   /api/companies              - Create new company
PUT    /api/companies/{id}         - Update company
DELETE /api/companies/{id}         - Delete company
```

## Validation Rules
- **Name**: Required, 2-200 characters, must be unique
- **Description**: Required, 10-1000 characters
- **Address**: Required, 5-255 characters
- **Email**: Required, valid email format, must be unique
- **Phone**: Required, 10-11 digits
- **Website**: Required, max 255 characters
- **Tax Code**: Required, max 50 characters, must be unique
- **Number of Employees**: Optional, max 50
- **Logo**: Optional, can be base64 or URL

## Exception Handling
- **ResourceNotFoundException**: Thrown when company not found by ID
- **DuplicateResourceException**: Thrown when email, taxCode, or name already exists

## Service Logic
1. **Create**: Validates all unique fields before persisting
2. **Read**: Throws exception if company not found
3. **Update**: Checks for duplicates only if field values changed
4. **Delete**: Simple deletion with existence check
5. **Helper Methods**: `existsByEmail()`, `existsByTaxCode()` for pre-validation

## Testing
### Unit Tests (CompanyServiceImplTest)
- `getAllCompanies_returnsListOfCompanies_whenCompaniesExist`
- `getAllCompanies_returnsEmptyList_whenNoCompaniesExist`
- `getCompanyById_returnsCompany_whenCompanyExists`
- `getCompanyById_throwsResourceNotFoundException_whenCompanyNotFound`
- `createCompany_createsCompany_whenValidDataProvided`
- `createCompany_throwsDuplicateResourceException_whenEmailAlreadyExists`
- `createCompany_throwsDuplicateResourceException_whenTaxCodeAlreadyExists`
- `createCompany_throwsDuplicateResourceException_whenNameAlreadyExists`
- `updateCompany_updatesCompany_whenValidDataProvided`
- `updateCompany_throwsResourceNotFoundException_whenCompanyNotFound`
- `updateCompany_throwsDuplicateResourceException_whenEmailChanged_andEmailAlreadyExists`
- `deleteCompany_deletesCompany_whenCompanyExists`
- `deleteCompany_throwsResourceNotFoundException_whenCompanyNotFound`
- `existsByEmail_returnTrue_whenEmailExists`
- `existsByTaxCode_returnTrue_whenTaxCodeExists`

### Integration Tests (CompanyControllerTest)
- `getAllCompanies_returns200_withListOfCompanies`
- `getAllCompanies_returns200_withEmptyList_whenNoCompaniesExist`
- `getCompanyById_returns200_withCompanyData`
- `getCompanyById_returns404_whenCompanyNotFound`
- `createCompany_returns201_withCreatedCompany`
- `createCompany_returns400_whenValidationFails`
- `createCompany_returns409_whenEmailAlreadyExists`
- `createCompany_returns409_whenTaxCodeAlreadyExists`
- `updateCompany_returns200_withUpdatedCompany`
- `updateCompany_returns400_whenValidationFails`
- `updateCompany_returns404_whenCompanyNotFound`
- `updateCompany_returns409_whenEmailAlreadyExists`
- `deleteCompany_returns204_whenCompanyDeleted`
- `deleteCompany_returns404_whenCompanyNotFound`

## Architecture Pattern
Follows feature-based architecture:
- **Controller**: Handles HTTP requests/responses
- **Service Interface**: Defines business contract
- **Service Implementation**: Contains business logic
- **Repository**: Database access via Spring Data JPA
- **Entity**: JPA-mapped persistence class
- **DTOs**: Request/Response data transfer objects

## Key Design Decisions
1. **Constructor Injection**: All dependencies injected via constructor
2. **ApiResponse Wrapper**: All endpoints return wrapped responses
3. **Validation**: Both Bean Validation (@Valid) and custom logic
4. **Timestamps**: Auto-managed via `@Column(insertable=false, updatable=false)`
5. **Status Codes**: Standard HTTP codes (200, 201, 204, 400, 404, 409)

## Related Features
- Auth (JWT authentication required)
- User (companies can have multiple users)
- Global Exception Handler (handles all exceptions)

## Future Enhancements
- Pagination support (GET /api/companies with page/size params)
- Search/Filter (by name, email, taxCode)
- Bulk operations (create/update multiple companies)
- Company-User relationship (assign users to company)
- Audit logging (track who created/modified companies)
- Soft delete (mark as deleted instead of removing)
