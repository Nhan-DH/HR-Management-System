package com.dona.spring_rest.config;

import com.dona.spring_rest.feature.company.Company;
import com.dona.spring_rest.feature.company.CompanyRepository;
import com.dona.spring_rest.feature.permission.Permission;
import com.dona.spring_rest.feature.permission.PermissionRepository;
import com.dona.spring_rest.feature.role.Role;
import com.dona.spring_rest.feature.role.RoleRepository;
import com.dona.spring_rest.feature.user.Gender;
import com.dona.spring_rest.feature.user.User;
import com.dona.spring_rest.feature.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * SeedDataRunner - CommandLineRunner implementation
 * Tự động chạy sau khi Spring Boot khởi động xong
 * Load seed data vào database
 */
@Component
public class SeedDataRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataRunner.class);

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor for dependency injection
    public SeedDataRunner(CompanyRepository companyRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            PasswordEncoder passwordEncoder) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("════════════════════════════════════════════════");
        log.info(" >>>>>>> Bắt đầu load seed data vào database...");
        log.info("════════════════════════════════════════════════");

        try {
            // Kiểm tra xem database đã có data chưa
            if (companyRepository.count() > 0) {
                log.warn(">>>>>  Database đã có dữ liệu. Bỏ qua load seed data.");
                return;
            }

            // Load data theo thứ tự: Company -> Role -> Permission -> User -> User-Role
            loadCompanies();
            loadRoles();
            loadPermissions();
            loadUsers();
            assignRolesToUsers();

            log.info("════════════════════════════════════════════════");
            log.info(">>>>>> Seed data đã được load thành công!");
            log.info("════════════════════════════════════════════════");
            log.info(">>>>>> Test Login:");
            log.info("   📧 Email: admin@techsolutions.com");
            log.info("   🔑 Password: password123");
            log.info("════════════════════════════════════════════════");

        } catch (Exception e) {
            log.error("XXXXX Lỗi khi load seed data", e);
            throw new RuntimeException("Không thể load seed data", e);
        }
    }

    /**
     * Load các công ty
     */
    private void loadCompanies() {
        log.info(">>>>>>> Loading companies...");

        List<Company> companies = Arrays.asList(
                createCompany("Tech Solutions Vietnam",
                        "Công ty tư vấn CNTT hàng đầu tại Việt Nam",
                        "123 Nguyễn Huệ, TP.HCM"),

                createCompany("Golden Finance Co.",
                        "Dịch vụ tài chính và ngân hàng",
                        "456 Trần Hưng Đạo, Hà Nội"),

                createCompany("Creative Digital Agency",
                        "Công ty digital marketing và thiết kế",
                        "789 Lê Lợi, Đà Nẵng"),

                createCompany("Healthcare Plus Hospital",
                        "Dịch vụ y tế và chăm sóc sức khỏe",
                        "321 Hoàng Hoa Thám, TP.HCM"));

        companyRepository.saveAll(companies);
        log.info("   ✓ {} công ty được tạo", companies.size());
    }

    private Company createCompany(String name, String description, String address) {
        Company company = new Company();
        company.setName(name);
        company.setDescription(description);
        company.setAddress(address);
        company.setLogo("https://via.placeholder.com/200?text=" + name.replace(" ", "+"));
        company.setCreatedAt(Instant.now());
        return company;
    }

    /**
     * Load các role
     */
    private void loadRoles() {
        log.info("👥 Loading roles...");

        List<Role> roles = Arrays.asList(
                createRole("ADMIN", "Quản trị viên hệ thống - có quyền truy cập toàn bộ"),
                createRole("MANAGER", "Quản lý - quản lý team và báo cáo"),
                createRole("TEAM_LEAD", "Trưởng nhóm - xem báo cáo nhóm"),
                createRole("EMPLOYEE", "Nhân viên - quyền truy cập cơ bản"),
                createRole("HR", "Chuyên viên HR - quản lý nhân sự"),
                createRole("ACCOUNTANT", "Kế toán - quản lý tài chính"));

        roleRepository.saveAll(roles);
        log.info("   ✓ {} vai trò được tạo", roles.size());
    }

    private Role createRole(String name, String description) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        role.setCreatedAt(Instant.now());
        return role;
    }

    /**
     * Load các permission
     */
    private void loadPermissions() {
        log.info("🔐 Loading permissions...");

        List<Permission> permissions = Arrays.asList(
                // User Management
                createPermission("Xem Danh Sách Người Dùng", "/api/v1/users", "GET", "USER_MANAGEMENT"),
                createPermission("Tạo Người Dùng", "/api/v1/users", "POST", "USER_MANAGEMENT"),
                createPermission("Cập Nhật Người Dùng", "/api/v1/users/*", "PUT", "USER_MANAGEMENT"),
                createPermission("Xóa Người Dùng", "/api/v1/users/*", "DELETE", "USER_MANAGEMENT"),

                // Company Management
                createPermission("Xem Danh Sách Công Ty", "/api/v1/companies", "GET", "COMPANY_MANAGEMENT"),
                createPermission("Tạo Công Ty", "/api/v1/companies", "POST", "COMPANY_MANAGEMENT"),
                createPermission("Cập Nhật Công Ty", "/api/v1/companies/*", "PUT", "COMPANY_MANAGEMENT"),
                createPermission("Xóa Công Ty", "/api/v1/companies/*", "DELETE", "COMPANY_MANAGEMENT"),

                // Role Management
                createPermission("Quản Lý Vai Trò", "/api/v1/roles", "GET", "ROLE_MANAGEMENT"),
                createPermission("Tạo Vai Trò", "/api/v1/roles", "POST", "ROLE_MANAGEMENT"),
                createPermission("Cập Nhật Vai Trò", "/api/v1/roles/*", "PUT", "ROLE_MANAGEMENT"),

                // Reports
                createPermission("Xem Báo Cáo", "/api/v1/reports", "GET", "REPORTS"),
                createPermission("Xuất Báo Cáo", "/api/v1/reports/export", "POST", "REPORTS"),

                // Authentication
                createPermission("Đăng Nhập", "/api/v1/auth/login", "POST", "AUTH"),
                createPermission("Đăng Xuất", "/api/v1/auth/logout", "POST", "AUTH"),
                createPermission("Refresh Token", "/api/v1/auth/refresh", "POST", "AUTH"));

        permissionRepository.saveAll(permissions);
        log.info("   ✓ {} quyền được tạo", permissions.size());
    }

    private Permission createPermission(String name, String apiPath, String method, String module) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setApiPath(apiPath);
        permission.setMethod(method);
        permission.setModule(module);
        permission.setCreatedAt(Instant.now());
        return permission;
    }

    /**
     * Load các user
     */
    private void loadUsers() {
        log.info("👤 Loading users...");

        List<Company> companies = companyRepository.findAll();
        Company company1 = companies.stream()
                .filter(c -> c.getName().equals("Tech Solutions Vietnam"))
                .findFirst()
                .orElse(companies.get(0));

        Company company2 = companies.stream()
                .filter(c -> c.getName().equals("Golden Finance Co."))
                .findFirst()
                .orElse(companies.get(1));

        Company company3 = companies.stream()
                .filter(c -> c.getName().equals("Creative Digital Agency"))
                .findFirst()
                .orElse(companies.get(2));

        Company company4 = companies.stream()
                .filter(c -> c.getName().equals("Healthcare Plus Hospital"))
                .findFirst()
                .orElse(companies.get(3));

        String encodedPassword = passwordEncoder.encode("password123");

        List<User> users = Arrays.asList(
                // Tech Solutions - ADMIN
                createUser("admin@techsolutions.com", "Nguyễn Văn An", encodedPassword,
                        35, "123 Nguyễn Huệ, TP.HCM", Gender.MALE, company1),

                // Tech Solutions - MANAGER
                createUser("manager@techsolutions.com", "Trần Thị Bích", encodedPassword,
                        32, "456 Lê Lợi, Đà Nẵng", Gender.FEMALE, company1),

                // Tech Solutions - TEAM_LEAD
                createUser("teamlead@techsolutions.com", "Lê Minh Đức", encodedPassword,
                        28, "789 Trần Hưng Đạo, Hà Nội", Gender.MALE, company1),

                // Tech Solutions - EMPLOYEE 1
                createUser("employee1@techsolutions.com", "Phạm Thu Hương", encodedPassword,
                        26, "321 Hoàng Hoa Thám, TP.HCM", Gender.FEMALE, company1),

                // Tech Solutions - EMPLOYEE 2
                createUser("employee2@techsolutions.com", "Vũ Văn Tuấn", encodedPassword,
                        29, "654 Nguyễn Trãi, Bình Dương", Gender.MALE, company1),

                // Golden Finance - HR
                createUser("hr@goldfinance.com", "Hoàng Đức Vinh", encodedPassword,
                        40, "456 Trần Hưng Đạo, Hà Nội", Gender.MALE, company2),

                // Golden Finance - ACCOUNTANT
                createUser("accountant@goldfinance.com", "Nguyễn Thị Lan", encodedPassword,
                        38, "457 Trần Hưng Đạo, Hà Nội", Gender.FEMALE, company2),

                // Creative Digital - EMPLOYEE
                createUser("creative@creativedigital.com", "Đặng Minh Khoa", encodedPassword,
                        24, "789 Lê Lợi, Đà Nẵng", Gender.MALE, company3),

                // Healthcare Plus - HR
                createUser("hr@healthcareplus.com", "Trần Thị Thảo", encodedPassword,
                        45, "321 Hoàng Hoa Thám, TP.HCM", Gender.FEMALE, company4));

        userRepository.saveAll(users);
        log.info("   ✓ {} người dùng được tạo", users.size());
    }

    private User createUser(String email, String name, String password, int age,
            String address, Gender gender, Company company) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(password);
        user.setAge(age);
        user.setAddress(address);
        user.setGender(gender);
        user.setAvatar("https://via.placeholder.com/150?text=" + name.replace(" ", "+"));
        user.setCompany(company);
        user.setCreatedAt(Instant.now());
        return user;
    }

    /**
     * Gán các role cho user
     */
    private void assignRolesToUsers() {
        log.info("🔗 Assigning roles to users...");

        List<User> users = userRepository.findAll();
        List<Role> roles = roleRepository.findAll();

        // Tìm các role cụ thể
        Role adminRole = roles.stream()
                .filter(r -> r.getName().equals("ADMIN"))
                .findFirst()
                .orElse(null);

        Role managerRole = roles.stream()
                .filter(r -> r.getName().equals("MANAGER"))
                .findFirst()
                .orElse(null);

        Role teamLeadRole = roles.stream()
                .filter(r -> r.getName().equals("TEAM_LEAD"))
                .findFirst()
                .orElse(null);

        Role employeeRole = roles.stream()
                .filter(r -> r.getName().equals("EMPLOYEE"))
                .findFirst()
                .orElse(null);

        Role hrRole = roles.stream()
                .filter(r -> r.getName().equals("HR"))
                .findFirst()
                .orElse(null);

        Role accountantRole = roles.stream()
                .filter(r -> r.getName().equals("ACCOUNTANT"))
                .findFirst()
                .orElse(null);

        // Assign roles to users (tương ứng với thứ tự tạo user ở trên)
        if (users.size() >= 9) {
            users.get(0).setRoles(Arrays.asList(adminRole)); // admin@techsolutions.com -> ADMIN
            users.get(1).setRoles(Arrays.asList(managerRole)); // manager@techsolutions.com -> MANAGER
            users.get(2).setRoles(Arrays.asList(teamLeadRole)); // teamlead@techsolutions.com -> TEAM_LEAD
            users.get(3).setRoles(Arrays.asList(employeeRole)); // employee1@techsolutions.com -> EMPLOYEE
            users.get(4).setRoles(Arrays.asList(employeeRole)); // employee2@techsolutions.com -> EMPLOYEE
            users.get(5).setRoles(Arrays.asList(hrRole)); // hr@goldfinance.com -> HR
            users.get(6).setRoles(Arrays.asList(accountantRole)); // accountant@goldfinance.com -> ACCOUNTANT
            users.get(7).setRoles(Arrays.asList(employeeRole)); // creative@creativedigital.com -> EMPLOYEE
            users.get(8).setRoles(Arrays.asList(hrRole)); // hr@healthcareplus.com -> HR

            userRepository.saveAll(users);
            log.info("   ✓ Roles gán cho users thành công");
        }
    }
}
