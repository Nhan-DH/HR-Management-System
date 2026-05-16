package com.dona.spring_rest.util;

/**
 * SeedDataConstants - Hằng số cho seed data
 * Tập trung quản lý dữ liệu mẫu
 */
public class SeedDataConstants {

    // ========== COMPANIES ==========
    public static final String[] COMPANY_NAMES = {
            "Tech Solutions Vietnam",
            "Golden Finance Co.",
            "Creative Digital Agency",
            "Healthcare Plus Hospital"
    };

    public static final String[] COMPANY_DESCRIPTIONS = {
            "Công ty tư vấn CNTT hàng đầu tại Việt Nam",
            "Dịch vụ tài chính và ngân hàng",
            "Công ty digital marketing và thiết kế",
            "Dịch vụ y tế và chăm sóc sức khỏe"
    };

    public static final String[] COMPANY_ADDRESSES = {
            "123 Nguyễn Huệ, TP.HCM",
            "456 Trần Hưng Đạo, Hà Nội",
            "789 Lê Lợi, Đà Nẵng",
            "321 Hoàng Hoa Thám, TP.HCM"
    };

    // ========== ROLES ==========
    public static final String[] ROLE_NAMES = {
            "ADMIN",
            "MANAGER",
            "TEAM_LEAD",
            "EMPLOYEE",
            "HR",
            "ACCOUNTANT"
    };

    public static final String[] ROLE_DESCRIPTIONS = {
            "Quản trị viên hệ thống - có quyền truy cập toàn bộ",
            "Quản lý - quản lý team và báo cáo",
            "Trưởng nhóm - xem báo cáo nhóm",
            "Nhân viên - quyền truy cập cơ bản",
            "Chuyên viên HR - quản lý nhân sự",
            "Kế toán - quản lý tài chính"
    };

    // ========== USER PASSWORDS ==========
    public static final String DEFAULT_PASSWORD = "password123";
    public static final String TEST_USER_EMAIL = "admin@techsolutions.com";

    // ========== API PATHS ==========
    public static final String[] API_PATHS = {
            "/api/v1/users",
            "/api/v1/users/*",
            "/api/v1/companies",
            "/api/v1/companies/*",
            "/api/v1/roles",
            "/api/v1/roles/*",
            "/api/v1/reports",
            "/api/v1/reports/export",
            "/api/v1/auth/login",
            "/api/v1/auth/logout",
            "/api/v1/auth/refresh"
    };

    // ========== MODULES ==========
    public static final String MODULE_USER_MANAGEMENT = "USER_MANAGEMENT";
    public static final String MODULE_COMPANY_MANAGEMENT = "COMPANY_MANAGEMENT";
    public static final String MODULE_ROLE_MANAGEMENT = "ROLE_MANAGEMENT";
    public static final String MODULE_REPORTS = "REPORTS";
    public static final String MODULE_AUTH = "AUTH";

    // ========== HTTP METHODS ==========
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    public static final String PATCH = "PATCH";

    private SeedDataConstants() {
        // Prevent instantiation
    }
}
