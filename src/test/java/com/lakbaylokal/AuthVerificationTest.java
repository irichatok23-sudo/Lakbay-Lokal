package com.lakbaylokal;

import com.lakbaylokal.controller.ApiController;
import com.lakbaylokal.database.DatabaseInitializer;
import com.lakbaylokal.service.AuthService;
import com.lakbaylokal.util.PasswordUtil;

import java.util.HashMap;
import java.util.Map;

public class AuthVerificationTest {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println(" RUNNING LAKBAY LOKAL AUTHENTICATION VERIFICATION SUITE");
        System.out.println("==========================================================");

        int passed = 0;
        int failed = 0;

        // 0. Ensure Database Initializer & password sync has run
        boolean dbOk = DatabaseInitializer.initializeIfNeeded();
        if (!dbOk) {
            System.err.println("[FATAL] Database connection failed. Aborting tests.");
            System.exit(1);
        }

        AuthService authService = new AuthService();
        ApiController apiController = new ApiController();

        // TEST 1: PasswordUtil Hashing & Verification
        System.out.println("\n[Test 1] Testing PasswordUtil SHA-256 Hashing...");
        String expectedHash = "8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6";
        String computedHash = PasswordUtil.hashPassword("password123", "lakbaysalt2026");
        if (expectedHash.equals(computedHash) &&
            PasswordUtil.verifyPassword("password123", "lakbaysalt2026", expectedHash) &&
            !PasswordUtil.verifyPassword("wrongpass", "lakbaysalt2026", expectedHash)) {
            System.out.println("  -> PASS: SHA-256 cryptographic hash and salt verification verified.");
            passed++;
        } else {
            System.err.println("  -> FAIL: PasswordUtil hash computation mismatch.");
            failed++;
        }

        // TEST 2: Valid Tourist Login ('tourist_ana' / 'password123')
        System.out.println("\n[Test 2] Testing Valid Tourist Login (tourist_ana)...");
        Map<String, Object> touristLogin = authService.login("tourist_ana", "password123");
        if (Boolean.TRUE.equals(touristLogin.get("success")) &&
            touristLogin.get("token") != null &&
            touristLogin.get("user") != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> u = (Map<String, Object>) touristLogin.get("user");
            String roleCode = (String) u.get("roleCode");
            String fullName = (String) u.get("fullName");
            if ("TOURIST".equals(roleCode) && fullName.contains("Ana")) {
                System.out.println("  -> PASS: tourist_ana authenticated successfully with role TOURIST. Token issued: " + touristLogin.get("token"));
                passed++;
            } else {
                System.err.println("  -> FAIL: Unexpected user details: " + u);
                failed++;
            }
        } else {
            System.err.println("  -> FAIL: tourist_ana login failed: " + touristLogin.get("message"));
            failed++;
        }

        // TEST 3: Invalid Password for Existing Tourist ('tourist_ana' / 'wrongpassword')
        System.out.println("\n[Test 3] Testing Invalid Password for Existing Tourist...");
        Map<String, Object> badPassLogin = authService.login("tourist_ana", "wrongpassword");
        if (Boolean.FALSE.equals(badPassLogin.get("success")) &&
            "Invalid username or password.".equals(badPassLogin.get("message"))) {
            System.out.println("  -> PASS: Correctly rejected incorrect password with message: " + badPassLogin.get("message"));
            passed++;
        } else {
            System.err.println("  -> FAIL: Bad password test failed: " + badPassLogin);
            failed++;
        }

        // TEST 4: Nonexistent Username ('unknown_guest' / 'password123')
        System.out.println("\n[Test 4] Testing Nonexistent Username...");
        Map<String, Object> ghostLogin = authService.login("unknown_guest", "password123");
        if (Boolean.FALSE.equals(ghostLogin.get("success")) &&
            "Invalid username or password.".equals(ghostLogin.get("message"))) {
            System.out.println("  -> PASS: Correctly rejected nonexistent user with message: " + ghostLogin.get("message"));
            passed++;
        } else {
            System.err.println("  -> FAIL: Nonexistent user test failed: " + ghostLogin);
            failed++;
        }

        // TEST 5: Blank Credentials
        System.out.println("\n[Test 5] Testing Blank Credentials...");
        Map<String, Object> blankLogin = authService.login("", "");
        if (Boolean.FALSE.equals(blankLogin.get("success")) &&
            blankLogin.get("message") != null) {
            System.out.println("  -> PASS: Blank credentials handled gracefully: " + blankLogin.get("message"));
            passed++;
        } else {
            System.err.println("  -> FAIL: Blank credentials test failed: " + blankLogin);
            failed++;
        }

        // TEST 6: All Six Pre-seeded System Roles
        System.out.println("\n[Test 6] Testing All Six Pre-seeded System Roles...");
        String[][] roles = {
            {"admin", "SYSTEM_ADMIN"},
            {"tourismoffice", "TOURISM_OFFICE"},
            {"lgu_naval", "LGU_OFFICER"},
            {"brgy_olog", "BARANGAY_OFFICIAL"},
            {"guide_pedro", "STAKEHOLDER"},
            {"tourist_ana", "TOURIST"}
        };
        boolean allRolesPass = true;
        for (String[] r : roles) {
            String uname = r[0];
            String expectedRole = r[1];
            Map<String, Object> res = authService.login(uname, "password123");
            if (!Boolean.TRUE.equals(res.get("success"))) {
                System.err.println("    FAILED: " + uname + " - " + res.get("message"));
                allRolesPass = false;
            } else {
                @SuppressWarnings("unchecked")
                Map<String, Object> u = (Map<String, Object>) res.get("user");
                if (!expectedRole.equals(u.get("roleCode"))) {
                    System.err.println("    ROLE MISMATCH for " + uname + ": expected " + expectedRole + " got " + u.get("roleCode"));
                    allRolesPass = false;
                } else {
                    System.out.println("    PASS: " + uname + " -> " + expectedRole + " (" + u.get("fullName") + ")");
                }
            }
        }
        if (allRolesPass) {
            System.out.println("  -> PASS: All 6 system roles authenticated successfully.");
            passed++;
        } else {
            System.err.println("  -> FAIL: Not all roles authenticated correctly.");
            failed++;
        }

        // TEST 7: Role-Based Authorization Enforcement in API Controller
        System.out.println("\n[Test 7] Testing Role-Based API Authorization Enforcement...");
        // 7a. Unauthenticated request to protected endpoint /api/auth/me
        String unauthMe = apiController.handleRequest("/api/auth/me", "GET", new HashMap<>(), "", null, "127.0.0.1");
        boolean unauthBlocked = unauthMe.contains("Unauthorized");

        // 7b. Authenticate tourist and verify access to /api/auth/me
        String touristToken = (String) touristLogin.get("token");
        String touristMe = apiController.handleRequest("/api/auth/me", "GET", new HashMap<>(), "", touristToken, "127.0.0.1");
        boolean touristMeOk = touristMe.contains("tourist_ana");

        // 7c. Tourist attempting to perform admin destination validation
        String touristForbidden = apiController.handleRequest("/api/destinations/validate", "POST", new HashMap<>(), "{}", touristToken, "127.0.0.1");
        boolean touristBlockedFromAdmin = touristForbidden.contains("Access denied");

        if (unauthBlocked && touristMeOk && touristBlockedFromAdmin) {
            System.out.println("  -> PASS: Authorization strictly enforced (unauth 401, tourist session OK, admin action 403 denied).");
            passed++;
        } else {
            System.err.println("  -> FAIL: Authorization enforcement check failed. unauth=" + unauthBlocked + ", touristMe=" + touristMeOk + ", touristForbidden=" + touristBlockedFromAdmin);
            failed++;
        }

        // SUMMARY
        System.out.println("\n==========================================================");
        System.out.println(" TEST RESULTS: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("==========================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
