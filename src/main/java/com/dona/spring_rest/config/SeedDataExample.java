package com.dona.spring_rest.config;

import com.dona.spring_rest.feature.company.Company;
import com.dona.spring_rest.feature.company.CompanyRepository;
import com.dona.spring_rest.feature.user.Gender;
import com.dona.spring_rest.feature.user.User;
import com.dona.spring_rest.feature.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * SeedDataExample - Ví dụ mở rộng SeedDataRunner
 * 
 * CÓ THỂ DÙNG ĐỂ:
 * - Thêm seed data cho các bảng khác
 * - Custom logic khi load data
 * - Test với data cụ thể
 * 
 * CÁCH DÙNG:
 * 1. Copy code từ đây
 * 2. Tạo file mới (VD: ImportDataRunner.java)
 * 3. Uncomment @Component để kích hoạt
 * 4. Implement run() method với logic của bạn
 */

// @Component // Uncomment để kích hoạt
public class SeedDataExample implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataExample.class);

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedDataExample(CompanyRepository companyRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("🔄 Example: Custom seed data runner");

        try {
            // Kiểm tra điều kiện trước khi load
            if (companyRepository.count() > 0) {
                log.warn("⚠️  Data already exists");
                return;
            }

            // Example 1: Tạo company
            createSampleCompanies();

            // Example 2: Tạo users cho company
            createSampleUsers();

            // Example 3: Update/Modify existing data
            // updateExistingData();

            log.info("✅ Custom seed data loaded successfully");

        } catch (Exception e) {
            log.error("❌ Error loading custom seed data", e);
        }
    }

    /**
     * Example: Tạo sample companies
     */
    private void createSampleCompanies() {
        log.info("📦 Creating sample companies...");

        // Cách 1: Dùng constructor + setter
        Company company1 = new Company();
        company1.setName("Example Company 1");
        company1.setDescription("This is a sample company");
        company1.setAddress("123 Main Street");
        company1.setCreatedAt(Instant.now());

        // Cách 2: Constructor
        Company company2 = new Company();
        company2.setName("Example Company 2");
        company2.setDescription("Another sample company");
        company2.setAddress("456 Second Avenue");
        company2.setCreatedAt(Instant.now());

        companyRepository.save(company1);
        companyRepository.save(company2);

        log.info("   ✓ 2 sample companies created");
    }

    /**
     * Example: Tạo sample users
     */
    private void createSampleUsers() {
        log.info("👤 Creating sample users...");

        // Lấy company vừa tạo
        Company company = companyRepository.findAll().get(0);

        User user1 = new User();
        user1.setEmail("sample1@example.com");
        user1.setName("Sample User 1");
        user1.setPassword(passwordEncoder.encode("password123"));
        user1.setAge(25);
        user1.setAddress("Sample Address 1");
        user1.setGender(Gender.MALE);
        user1.setCompany(company);
        user1.setCreatedAt(Instant.now());

        User user2 = new User();
        user2.setEmail("sample2@example.com");
        user2.setName("Sample User 2");
        user2.setPassword(passwordEncoder.encode("password123"));
        user2.setAge(28);
        user2.setAddress("Sample Address 2");
        user2.setGender(Gender.FEMALE);
        user2.setCompany(company);
        user2.setCreatedAt(Instant.now());

        userRepository.save(user1);
        userRepository.save(user2);

        log.info("   ✓ 2 sample users created");
    }

    /**
     * Example: Update existing data
     */
    private void updateExistingData() {
        log.info("🔄 Updating existing data...");

        // Lấy user đầu tiên
        User user = userRepository.findAll().get(0);

        // Update field
        user.setAge(30);
        user.setAddress("Updated Address");
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);

        log.info("   ✓ Data updated");
    }

    /**
     * Example: Bulk operations
     */
    private void bulkOperations() {
        log.info("📊 Performing bulk operations...");

        // Lấy tất cả users
        var users = userRepository.findAll();

        // Thay đổi hàng loạt
        users.forEach(user -> {
            user.setAge(user.getAge() + 1); // Tăng tuổi lên 1
        });

        // Save tất cả
        userRepository.saveAll(users);

        log.info("   ✓ {} users updated", users.size());
    }

    /**
     * Example: Xóa data
     */
    private void deleteData() {
        log.info("🗑️  Deleting sample data...");

        // Xóa theo condition
        var users = userRepository.findAll();
        users.stream()
                .filter(u -> u.getEmail().startsWith("sample"))
                .forEach(userRepository::delete);

        log.info("   ✓ Sample data deleted");
    }

    /**
     * Example: Query existing data
     */
    private void queryData() {
        log.info("🔍 Querying existing data...");

        // Lấy tất cả
        var allUsers = userRepository.findAll();
        log.info("   Total users: {}", allUsers.size());

        // Lấy first
        var firstUser = userRepository.findAll().get(0);
        log.info("   First user: {} ({})", firstUser.getName(), firstUser.getEmail());

        // Lấy theo condition (nếu có custom query method)
        // var specificUsers = userRepository.findByGender(Gender.MALE);
    }
}
