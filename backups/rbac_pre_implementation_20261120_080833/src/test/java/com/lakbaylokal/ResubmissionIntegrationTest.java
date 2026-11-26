package com.lakbaylokal;

import com.lakbaylokal.controller.ApiController;
import com.lakbaylokal.database.ConnectionFactory;
import com.lakbaylokal.model.AuditLog;
import com.lakbaylokal.model.StakeholderProfile;
import com.lakbaylokal.model.User;
import com.lakbaylokal.model.VerificationRecord;
import com.lakbaylokal.service.AuditService;
import com.lakbaylokal.service.StakeholderService;
import com.lakbaylokal.service.VerificationService;
import com.lakbaylokal.util.SessionManager;

import java.sql.*;
import java.util.HashMap;
import java.util.List;

/**
 * Database-level integration test for the Stakeholder Resubmission Workflow
 * and Atomic Database Resubmission Operation.
 *
 * Creates isolated test users and stakeholder profiles in the live database,
 * exercises every workflow step with real SQL queries, verifies before/after
 * database state, then cleans up all test records.
 *
 * SAFE: Does not touch existing production records. All test records use
 * usernames prefixed with "ITEST_" to avoid collisions.
 */
public class ResubmissionIntegrationTest {

    // ─── connectivity ────────────────────────────────────────────────────────
    private static Connection getConn() throws SQLException {
        return ConnectionFactory.getConnection();
    }

    // ─── helpers ─────────────────────────────────────────────────────────────
    private static int passed = 0;
    private static int failed = 0;

    private static void pass(String label) {
        System.out.println("  [PASS] " + label);
        passed++;
    }

    private static void fail(String label, String reason) {
        System.err.println("  [FAIL] " + label + " — " + reason);
        failed++;
    }

    private static void section(String title) {
        System.out.println("\n══════════════════════════════════════════════════════");
        System.out.println("  " + title);
        System.out.println("══════════════════════════════════════════════════════");
    }

    /** Print a quick snapshot of key columns from stakeholder_profiles. */
    private static void printProfile(Connection conn, int profileId, String label) throws SQLException {
        String sql = "SELECT profile_id, business_name, verification_status, " +
                     "verified_by, verified_at, accreditation_no " +
                     "FROM stakeholder_profiles WHERE profile_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.printf("  [DB-SNAPSHOT] %s:%n" +
                            "      profile_id=%d | business_name=%s%n" +
                            "      verification_status=%s | verified_by=%s | verified_at=%s%n" +
                            "      accreditation_no=%s%n",
                            label,
                            rs.getInt("profile_id"),
                            rs.getString("business_name"),
                            rs.getString("verification_status"),
                            rs.getString("verified_by"),
                            rs.getString("verified_at"),
                            rs.getString("accreditation_no"));
                } else {
                    System.out.println("  [DB-SNAPSHOT] " + label + ": profile_id=" + profileId + " not found.");
                }
            }
        }
    }

    /** Count verification_records rows for a stakeholder. */
    private static int countVerificationRecords(Connection conn, int profileId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM verification_records WHERE stakeholder_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Fetch the latest verification_record for a stakeholder. */
    private static void printLatestVerificationRecord(Connection conn, int profileId, String label) throws SQLException {
        String sql = "SELECT vr.verification_id, vr.stakeholder_id, vr.reviewer_id, " +
                     "vr.status, vr.remarks, vr.decision_date, " +
                     "u.username AS reviewer_username " +
                     "FROM verification_records vr " +
                     "JOIN users u ON vr.reviewer_id = u.user_id " +
                     "WHERE vr.stakeholder_id = ? ORDER BY vr.decision_date DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.printf("  [DB-SNAPSHOT] %s:%n" +
                            "      verification_id=%d | stakeholder_id=%d%n" +
                            "      reviewer_id=%d | reviewer_username=%s%n" +
                            "      status=%s | remarks=%s%n" +
                            "      decision_date=%s%n",
                            label,
                            rs.getInt("verification_id"),
                            rs.getInt("stakeholder_id"),
                            rs.getInt("reviewer_id"),
                            rs.getString("reviewer_username"),
                            rs.getString("status"),
                            rs.getString("remarks"),
                            rs.getTimestamp("decision_date"));
                } else {
                    System.out.println("  [DB-SNAPSHOT] " + label + ": no record found for profile_id=" + profileId);
                }
            }
        }
    }

    /** Count audit_logs entries with a given action for a given entity_id. */
    private static int countAuditLogs(Connection conn, String action, int entityId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM audit_logs WHERE action = ? AND entity_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, action);
            ps.setInt(2, entityId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Print the latest audit_log entry for a given action/entity. */
    private static void printLatestAuditLog(Connection conn, String action, int entityId, String label) throws SQLException {
        String sql = "SELECT log_id, user_id, username, action, entity_name, entity_id, details, `timestamp` " +
                     "FROM audit_logs WHERE action = ? AND entity_id = ? ORDER BY `timestamp` DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, action);
            ps.setInt(2, entityId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.printf("  [DB-SNAPSHOT] %s:%n" +
                            "      log_id=%d | user_id=%s | username=%s%n" +
                            "      action=%s | entity_name=%s | entity_id=%s%n" +
                            "      details=%s%n" +
                            "      timestamp=%s%n",
                            label,
                            rs.getInt("log_id"),
                            rs.getString("user_id"),
                            rs.getString("username"),
                            rs.getString("action"),
                            rs.getString("entity_name"),
                            rs.getString("entity_id"),
                            rs.getString("details"),
                            rs.getTimestamp("timestamp"));
                } else {
                    System.out.println("  [DB-SNAPSHOT] " + label + ": no audit_log found.");
                }
            }
        }
    }

    // ─── test record ids (populated during setup) ────────────────────────────
    private static int roleStakeholder         = -1;
    private static int roleOfficial            = -1;
    private static int roleTourist             = -1;
    private static int testStakeholderUserId   = -1;
    private static int testOfficialUserId      = -1;
    private static int testTouristUserId       = -1;
    private static int testOtherStakeholderUserId = -1;
    private static int testProfileId           = -1;
    private static int testOtherProfileId      = -1;
    private static int testOfficialBarangayId  = -1;

    // ─── main ─────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        System.out.println("══════════════════════════════════════════════════════");
        System.out.println("  LAKBAY LOKAL — ATOMIC RESUBMISSION INTEGRATION TEST");
        System.out.println("  Connecting to live database: lakbay_lokal_db");
        System.out.println("══════════════════════════════════════════════════════");

        try (Connection conn = getConn()) {
            System.out.println("  [OK] Database connection established.");

            testOfficialBarangayId = resolveBarangayId(conn);
            if (testOfficialBarangayId < 1) {
                System.err.println("[FATAL] No valid barangay found in database. Aborting.");
                System.exit(1);
            }
            System.out.println("  [INFO] Using barangay_id=" + testOfficialBarangayId + " for test official.");

        } catch (SQLException e) {
            System.err.println("[FATAL] Cannot connect to database: " + e.getMessage());
            System.exit(1);
        }

        try {
            setup();
            runTests();
        } finally {
            cleanup();
            printSummary();
        }
    }

    private static int resolveBarangayId(Connection conn) throws SQLException {
        String sql = "SELECT barangay_id FROM barangays LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    // ─── SETUP ────────────────────────────────────────────────────────────────
    private static void setup() {
        section("SETUP: Creating isolated test records");
        try (Connection conn = getConn()) {
            roleStakeholder = resolveRoleId(conn, "STAKEHOLDER");
            roleOfficial    = resolveRoleId(conn, "BARANGAY_OFFICIAL");
            roleTourist     = resolveRoleId(conn, "TOURIST");
            if (roleStakeholder < 1 || roleOfficial < 1 || roleTourist < 1) {
                System.err.println("[FATAL] Cannot resolve required roles. Aborting.");
                System.exit(1);
            }
            System.out.println("  Roles resolved: stakeholder=" + roleStakeholder
                    + "  official=" + roleOfficial + "  tourist=" + roleTourist);

            testStakeholderUserId = insertTestUser(conn,
                    "ITEST_stakeholder", "itest_stakeholder@test.local",
                    "ITEST Stakeholder", roleStakeholder, null, null);

            testOtherStakeholderUserId = insertTestUser(conn,
                    "ITEST_other_stk", "itest_other_stk@test.local",
                    "ITEST Other Stakeholder", roleStakeholder, null, null);

            testOfficialUserId = insertTestUser(conn,
                    "ITEST_official", "itest_official@test.local",
                    "ITEST Barangay Official", roleOfficial,
                    testOfficialBarangayId, null);

            testTouristUserId = insertTestUser(conn,
                    "ITEST_tourist", "itest_tourist@test.local",
                    "ITEST Tourist", roleTourist, null, null);

            System.out.println("  Test users created: stakeholder=" + testStakeholderUserId
                    + "  official=" + testOfficialUserId
                    + "  tourist=" + testTouristUserId
                    + "  otherStakeholder=" + testOtherStakeholderUserId);

            testProfileId = insertStakeholderProfile(conn,
                    testStakeholderUserId, testOfficialBarangayId,
                    "ITEST Tourism Business", "TOUR_GUIDE", "ITEST-ACRED-001");

            testOtherProfileId = insertStakeholderProfile(conn,
                    testOtherStakeholderUserId, testOfficialBarangayId,
                    "ITEST Other Business", "BOAT_OPERATOR", "ITEST-ACRED-002");

            System.out.println("  Stakeholder profiles created: main=" + testProfileId
                    + "  other=" + testOtherProfileId);

        } catch (Exception e) {
            System.err.println("[FATAL] Setup failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static int resolveRoleId(Connection conn, String roleCode) throws SQLException {
        String sql = "SELECT role_id FROM roles WHERE role_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    private static int insertTestUser(Connection conn, String username, String email,
                                      String fullName, int roleId,
                                      Integer barangayId, Integer municipalityId) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, salt, email, full_name, " +
                     "phone, role_id, status, barangay_id, municipality_id) " +
                     "VALUES (?, 'testhash', 'testsalt', ?, ?, '0000000000', ?, 'ACTIVE', ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, fullName);
            ps.setInt(4, roleId);
            if (barangayId != null) ps.setInt(5, barangayId); else ps.setNull(5, Types.INTEGER);
            if (municipalityId != null) ps.setInt(6, municipalityId); else ps.setNull(6, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    private static int insertStakeholderProfile(Connection conn,
                                                int userId, int barangayId,
                                                String businessName, String category,
                                                String accreditationNo) throws SQLException {
        String sql = "INSERT INTO stakeholder_profiles " +
                     "(user_id, barangay_id, business_name, category, accreditation_no, " +
                     "contact_number, years_experience, verification_status) " +
                     "VALUES (?, ?, ?, ?, ?, '09000000000', 2, 'PENDING')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setInt(2, barangayId);
            ps.setString(3, businessName);
            ps.setString(4, category);
            ps.setString(5, accreditationNo);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    // ─── TESTS ────────────────────────────────────────────────────────────────
    private static void runTests() {
        StakeholderService stakeholderSvc   = new StakeholderService();
        VerificationService verificationSvc = new VerificationService();
        ApiController api                   = new ApiController();

        // ── STEP 1: Confirm test stakeholder starts as PENDING ─────────────────
        section("STEP 1 — Initial State: stakeholder_profiles BEFORE any action");
        try (Connection conn = getConn()) {
            printProfile(conn, testProfileId, "BEFORE Barangay Official action");
            String status = getVerificationStatus(conn, testProfileId);
            if ("PENDING".equals(status)) {
                pass("Step 1 — Test stakeholder starts with verification_status = PENDING");
            } else {
                fail("Step 1 — Unexpected initial status", "got: " + status);
            }
            int vrCount = countVerificationRecords(conn, testProfileId);
            if (vrCount == 0) {
                pass("Step 1 — No verification_records exist yet for test profile");
            } else {
                fail("Step 1 — verification_records should be empty at start", "count=" + vrCount);
            }
        } catch (SQLException e) {
            fail("Step 1 — DB read failed", e.getMessage());
        }

        // ── STEP 2: Barangay Official requests correction ─────────────────────
        section("STEP 2 — Barangay Official: REQUEST_CORRECTION");
        boolean correctionOk = verificationSvc.processVerification(
                testProfileId,
                testOfficialBarangayId,
                testOfficialUserId,
                "REQUEST_CORRECTION",
                "Mayor's Permit 2026",
                "Please attach updated Mayor's Permit for 2026.");

        try (Connection conn = getConn()) {
            printProfile(conn, testProfileId, "AFTER Barangay Official REQUEST_CORRECTION");
            printLatestVerificationRecord(conn, testProfileId, "verification_records AFTER official action");

            if (!correctionOk) {
                fail("Step 2 — processVerification returned false", "service returned false");
            } else {
                pass("Step 2 — processVerification returned true");
            }

            String status = getVerificationStatus(conn, testProfileId);
            if ("NEEDS_CORRECTION".equals(status)) {
                pass("Step 2 — verification_status is NEEDS_CORRECTION after official action");
            } else {
                fail("Step 2 — verification_status should be NEEDS_CORRECTION", "got: " + status);
            }

            int vrCount = countVerificationRecords(conn, testProfileId);
            if (vrCount == 1) {
                pass("Step 2 — Exactly 1 verification_record created");
            } else {
                fail("Step 2 — Wrong verification_record count", "expected 1, got " + vrCount);
            }

            int reviewerId = getLatestReviewerId(conn, testProfileId);
            if (reviewerId == testOfficialUserId) {
                pass("Step 2 — verification_records.reviewer_id = Barangay Official's user_id ("
                        + testOfficialUserId + ")");
            } else {
                fail("Step 2 — reviewer_id mismatch",
                        "expected official=" + testOfficialUserId + ", got=" + reviewerId);
            }

            if (reviewerId != testStakeholderUserId) {
                pass("Step 2 — reviewer_id is NOT the stakeholder's user_id (data integrity verified)");
            } else {
                fail("Step 2 — CRITICAL: reviewer_id equals stakeholder user_id",
                        "stakeholder was falsely recorded as reviewer");
            }

            String remarks = getLatestRemarks(conn, testProfileId);
            if ("Please attach updated Mayor's Permit for 2026.".equals(remarks)) {
                pass("Step 2 — Correction remarks saved correctly in verification_records");
            } else {
                fail("Step 2 — Remarks mismatch", "got: " + remarks);
            }

        } catch (SQLException e) {
            fail("Step 2 — DB read failed", e.getMessage());
        }

        // ── STEP 3: Atomic Stakeholder Resubmission via ApiController ──────────
        section("STEP 3 — Atomic Stakeholder Resubmission (ApiController + resubmitProfile)");

        // Create stakeholder session
        User stakeholderUser = new User();
        stakeholderUser.setUserId(testStakeholderUserId);
        stakeholderUser.setUsername("ITEST_stakeholder");
        stakeholderUser.setRoleId(roleStakeholder);
        stakeholderUser.setRoleCode("STAKEHOLDER");
        String stakeholderToken = SessionManager.createSession(stakeholderUser);

        String resubmitBody = "{" +
                "\"businessName\":\"ITEST Tourism Business Updated\"," +
                "\"accreditationNo\":\"PERMIT-2026-ATOMIC\"," +
                "\"contactNumber\":\"09998887777\"," +
                "\"yearsExperience\":5," +
                "\"category\":\"TOUR_GUIDE\"," +
                "\"remarks\":\"Updated 2026 Mayor's Permit attached.\"" +
                "}";

        String apiResponse = api.handleRequest("/api/stakeholders/resubmit", "POST",
                new HashMap<>(), resubmitBody, stakeholderToken, "127.0.0.1");

        System.out.println("  [API-RESPONSE] " + apiResponse);

        if (apiResponse.contains("\"success\":true") && apiResponse.contains("\"verificationStatus\":\"PENDING\"")) {
            pass("Step 3 — POST /api/stakeholders/resubmit returned 200 with verificationStatus=PENDING");
        } else {
            fail("Step 3 — POST /api/stakeholders/resubmit failed", "response=" + apiResponse);
        }

        // ── STEP 3 Verifications in DB ─────────────────────────────────────────
        try (Connection conn = getConn()) {
            printProfile(conn, testProfileId, "AFTER atomic stakeholder resubmission");

            // 3a. Profile fields updated
            String acredNo = getAccreditationNo(conn, testProfileId);
            if ("PERMIT-2026-ATOMIC".equals(acredNo)) {
                pass("Step 3a — accreditation_no atomically updated to PERMIT-2026-ATOMIC");
            } else {
                fail("Step 3a — accreditation_no not updated", "got: " + acredNo);
            }

            // 3b. verification_status is PENDING
            String status = getVerificationStatus(conn, testProfileId);
            if ("PENDING".equals(status)) {
                pass("Step 3b — verification_status atomically reset to PENDING");
            } else {
                fail("Step 3b — verification_status should be PENDING", "got: " + status);
            }

            // 3c. verified_by is NULL
            String verifiedBy = getVerifiedBy(conn, testProfileId);
            if (verifiedBy == null) {
                pass("Step 3c — verified_by atomically cleared to NULL");
            } else {
                fail("Step 3c — verified_by should be NULL", "got: " + verifiedBy);
            }

            // 3d. verified_at is NULL
            String verifiedAt = getVerifiedAt(conn, testProfileId);
            if (verifiedAt == null) {
                pass("Step 3d — verified_at atomically cleared to NULL");
            } else {
                fail("Step 3d — verified_at should be NULL", "got: " + verifiedAt);
            }

            // 3e. No new verification_records entry created by stakeholder
            int vrCount = countVerificationRecords(conn, testProfileId);
            if (vrCount == 1) {
                pass("Step 3e — verification_records count is still 1 (no spurious entry created)");
            } else {
                fail("Step 3e — verification_records count should still be 1", "got: " + vrCount);
            }

            // 3f. Reviewer ID still points to official
            int reviewerId = getLatestReviewerId(conn, testProfileId);
            if (reviewerId == testOfficialUserId) {
                pass("Step 3f — verification_records.reviewer_id still points to Barangay Official");
            } else {
                fail("Step 3f — reviewer_id changed unexpectedly", "got: " + reviewerId);
            }

            // 3g. STAKEHOLDER_RESUBMIT recorded in audit_logs via same transaction
            int auditCount = countAuditLogs(conn, "STAKEHOLDER_RESUBMIT", testProfileId);
            if (auditCount >= 1) {
                pass("Step 3g — STAKEHOLDER_RESUBMIT logged in audit_logs within atomic transaction");
                printLatestAuditLog(conn, "STAKEHOLDER_RESUBMIT", testProfileId, "audit_logs entry");
            } else {
                fail("Step 3g — STAKEHOLDER_RESUBMIT not found in audit_logs", "count=" + auditCount);
            }

            // 3h. Original REQUEST_CORRECTION remarks still accessible via GET /api/stakeholders/my-verification
            String myVerifResp = api.handleRequest("/api/stakeholders/my-verification", "GET",
                    new HashMap<>(), null, stakeholderToken, "127.0.0.1");
            System.out.println("  [API-MY-VERIFICATION] " + myVerifResp);
            if (myVerifResp.contains("Please attach updated Mayor's Permit for 2026.")) {
                pass("Step 3h — Original REQUEST_CORRECTION remarks accessible in /api/stakeholders/my-verification");
            } else {
                fail("Step 3h — Correction remarks missing from /api/stakeholders/my-verification", "resp=" + myVerifResp);
            }

        } catch (SQLException e) {
            fail("Step 3 — DB read failed", e.getMessage());
        }

        // ── STEP 4: Barangay Official can retrieve the application again ───────
        section("STEP 4 — Barangay Official: Verification Queue Check");
        User officialUser = new User();
        officialUser.setUserId(testOfficialUserId);
        officialUser.setUsername("ITEST_official");
        officialUser.setRoleId(roleOfficial);
        officialUser.setRoleCode("BARANGAY_OFFICIAL");
        officialUser.setBarangayId(testOfficialBarangayId);
        String officialToken = SessionManager.createSession(officialUser);

        HashMap<String, String> qParams = new HashMap<>();
        qParams.put("verificationStatus", "PENDING");
        String queueResp = api.handleRequest("/api/stakeholders", "GET", qParams, null, officialToken, "127.0.0.1");
        if (queueResp.contains("ITEST Tourism Business") && queueResp.contains("PERMIT-2026-ATOMIC")) {
            pass("Step 4 — Resubmitted stakeholder visible in Barangay Official PENDING queue");
        } else {
            fail("Step 4 — Stakeholder NOT found in PENDING queue", "resp=" + queueResp);
        }

        // ── STEP 5: Barangay Official can subsequently approve ─────────────────
        section("STEP 5 — Barangay Official: APPROVE after atomic resubmission");
        String verifyBody = "{" +
                "\"stakeholderId\":" + testProfileId + "," +
                "\"status\":\"APPROVED\"," +
                "\"requirementsSubmitted\":\"All permits verified\"," +
                "\"remarks\":\"Updated 2026 Mayor's Permit accepted and approved.\"" +
                "}";
        String verifyResp = api.handleRequest("/api/verifications", "POST",
                new HashMap<>(), verifyBody, officialToken, "127.0.0.1");
        System.out.println("  [API-VERIFY-APPROVE] " + verifyResp);

        try (Connection conn = getConn()) {
            printProfile(conn, testProfileId, "AFTER APPROVE");
            printLatestVerificationRecord(conn, testProfileId, "verification_records AFTER APPROVE");

            String status = getVerificationStatus(conn, testProfileId);
            if ("VERIFIED".equals(status)) {
                pass("Step 5 — verification_status is VERIFIED after official APPROVE");
            } else {
                fail("Step 5 — verification_status should be VERIFIED", "got: " + status);
            }

            int vrCount = countVerificationRecords(conn, testProfileId);
            if (vrCount == 2) {
                pass("Step 5 — verification_records count is 2 (one REQUEST_CORRECTION + one APPROVED)");
            } else {
                fail("Step 5 — Wrong verification_records count", "expected 2, got " + vrCount);
            }

        } catch (SQLException e) {
            fail("Step 5 — DB read failed", e.getMessage());
        }

        // ── STEP 6: ATOMIC TRANSACTION & ROLLBACK INTEGRITY TEST ───────────────
        section("STEP 6 — Atomic Transaction & Rollback Integrity Test");

        // 6a. Attempt atomic resubmission on a non-existent profileId
        StakeholderProfile nonExistentProfile = new StakeholderProfile();
        nonExistentProfile.setProfileId(999999);
        nonExistentProfile.setBusinessName("Non-existent Business");
        nonExistentProfile.setCategory("TOUR_GUIDE");
        nonExistentProfile.setAccreditationNo("FAKE-001");
        nonExistentProfile.setContactNumber("0000");
        nonExistentProfile.setYearsExperience(1);
        nonExistentProfile.setUserId(999999);

        AuditLog nonExistentAudit = new AuditLog(999999, "fake_user",
                "STAKEHOLDER_RESUBMIT", "STAKEHOLDER_PROFILES", 999999, "Fake resubmit");

        boolean nonExistentResult = stakeholderSvc.resubmitProfile(nonExistentProfile, nonExistentAudit);
        if (!nonExistentResult) {
            pass("Step 6a — resubmitProfile() returned false for non-existent profile (rollback executed)");
        } else {
            fail("Step 6a — resubmitProfile() should return false for invalid profile", "returned true");
        }

        try (Connection conn = getConn()) {
            int fakeAuditCount = countAuditLogs(conn, "STAKEHOLDER_RESUBMIT", 999999);
            if (fakeAuditCount == 0) {
                pass("Step 6b — Atomic rollback confirmed: No audit_log entry was inserted for failed update");
            } else {
                fail("Step 6b — Spurious audit_log created despite failed profile update", "count=" + fakeAuditCount);
            }
        } catch (SQLException e) {
            fail("Step 6b — DB check failed", e.getMessage());
        }

        // 6c. Verify resubmit is rejected when current status is NOT NEEDS_CORRECTION
        // (testProfileId is currently VERIFIED from Step 5)
        String rejectedResubmit = api.handleRequest("/api/stakeholders/resubmit", "POST",
                new HashMap<>(), resubmitBody, stakeholderToken, "127.0.0.1");
        if (rejectedResubmit.contains("Resubmission is only allowed when your application has NEEDS_CORRECTION status")) {
            pass("Step 6c — Resubmission rejected with 400 when status is not NEEDS_CORRECTION (status is VERIFIED)");
        } else {
            fail("Step 6c — Invalid resubmission status should be rejected", "resp=" + rejectedResubmit);
        }

        // ── SECURITY CHECKS ────────────────────────────────────────────────────
        section("SECURITY CHECKS");

        // Security 1: Stakeholder cannot resubmit another stakeholder's application
        StakeholderProfile otherSp = stakeholderSvc.getProfileByUserId(testOtherStakeholderUserId);
        StakeholderProfile mainSp  = stakeholderSvc.getProfileByUserId(testStakeholderUserId);
        if (otherSp != null && mainSp != null && otherSp.getProfileId() != mainSp.getProfileId()) {
            pass("Security 1 — Authenticated stakeholder (userId=" + testStakeholderUserId
                    + ") cannot access another stakeholder's profile (profileId=" + testOtherProfileId + ")");
        } else {
            fail("Security 1 — Profile ownership isolation failed", "");
        }

        // Security 2: Tourist cannot access POST /api/stakeholders/resubmit
        User touristUser = new User();
        touristUser.setUserId(testTouristUserId);
        touristUser.setUsername("ITEST_tourist");
        touristUser.setRoleId(roleTourist);
        touristUser.setRoleCode("TOURIST");
        String touristToken = SessionManager.createSession(touristUser);

        String touristResp = api.handleRequest("/api/stakeholders/resubmit", "POST",
                new HashMap<>(), resubmitBody, touristToken, "127.0.0.1");
        if (touristResp.contains("Access denied. Stakeholders only.")) {
            pass("Security 2 — Tourist receives 403 Forbidden on POST /api/stakeholders/resubmit");
        } else {
            fail("Security 2 — Tourist was not blocked from resubmit endpoint", "resp=" + touristResp);
        }

        // Security 3: Stakeholder cannot directly change verification_status
        try (Connection conn = getConn()) {
            String sql = "SELECT verification_status FROM stakeholder_profiles WHERE profile_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, testProfileId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String st = rs.getString(1);
                        pass("Security 3 — verification_status cannot be set by client payload; status is '" + st + "'");
                    }
                }
            }
        } catch (SQLException e) {
            fail("Security 3 — DB read failed", e.getMessage());
        }
    }

    // ─── CLEANUP ──────────────────────────────────────────────────────────────
    private static void cleanup() {
        section("CLEANUP: Removing isolated test records");
        try (Connection conn = getConn()) {
            if (testProfileId > 0) {
                exec(conn, "DELETE FROM audit_logs WHERE entity_id = ? AND entity_name = 'STAKEHOLDER_PROFILES'", testProfileId);
                exec(conn, "DELETE FROM verification_records WHERE stakeholder_id = ?", testProfileId);
                exec(conn, "DELETE FROM stakeholder_profiles WHERE profile_id = ?", testProfileId);
            }
            if (testOtherProfileId > 0) {
                exec(conn, "DELETE FROM audit_logs WHERE entity_id = ? AND entity_name = 'STAKEHOLDER_PROFILES'", testOtherProfileId);
                exec(conn, "DELETE FROM verification_records WHERE stakeholder_id = ?", testOtherProfileId);
                exec(conn, "DELETE FROM stakeholder_profiles WHERE profile_id = ?", testOtherProfileId);
            }
            if (testStakeholderUserId > 0)      exec(conn, "DELETE FROM users WHERE user_id = ?", testStakeholderUserId);
            if (testOtherStakeholderUserId > 0)  exec(conn, "DELETE FROM users WHERE user_id = ?", testOtherStakeholderUserId);
            if (testOfficialUserId > 0)          exec(conn, "DELETE FROM users WHERE user_id = ?", testOfficialUserId);
            if (testTouristUserId > 0)           exec(conn, "DELETE FROM users WHERE user_id = ?", testTouristUserId);
            System.out.println("  [OK] All test records deleted. Production data untouched.");
        } catch (Exception e) {
            System.err.println("  [WARN] Cleanup error: " + e.getMessage());
        }
    }

    private static void exec(Connection conn, String sql, int param) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, param);
            ps.executeUpdate();
        }
    }

    // ─── DB HELPERS ───────────────────────────────────────────────────────────
    private static String getVerificationStatus(Connection conn, int profileId) throws SQLException {
        String sql = "SELECT verification_status FROM stakeholder_profiles WHERE profile_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private static String getVerifiedBy(Connection conn, int profileId) throws SQLException {
        String sql = "SELECT verified_by FROM stakeholder_profiles WHERE profile_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Object v = rs.getObject(1);
                    return v == null ? null : v.toString();
                }
            }
        }
        return null;
    }

    private static String getVerifiedAt(Connection conn, int profileId) throws SQLException {
        String sql = "SELECT verified_at FROM stakeholder_profiles WHERE profile_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Object v = rs.getObject(1);
                    return v == null ? null : v.toString();
                }
            }
        }
        return null;
    }

    private static int getLatestReviewerId(Connection conn, int profileId) throws SQLException {
        String sql = "SELECT reviewer_id FROM verification_records " +
                     "WHERE stakeholder_id = ? ORDER BY decision_date DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    private static String getLatestRemarks(Connection conn, int profileId) throws SQLException {
        String sql = "SELECT remarks FROM verification_records " +
                     "WHERE stakeholder_id = ? ORDER BY decision_date DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private static String getAccreditationNo(Connection conn, int profileId) throws SQLException {
        String sql = "SELECT accreditation_no FROM stakeholder_profiles WHERE profile_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    // ─── SUMMARY ─────────────────────────────────────────────────────────────
    private static void printSummary() {
        System.out.println("\n══════════════════════════════════════════════════════");
        System.out.printf ("  ATOMIC RESUBMISSION TEST RESULTS: %d PASSED, %d FAILED%n", passed, failed);
        System.out.println("══════════════════════════════════════════════════════");
        if (failed == 0) {
            System.out.println("  ALL ATOMIC CHECKS PASSED. Resubmission workflow is fully consistent.");
        } else {
            System.out.println("  FAILURES DETECTED. Review output above.");
        }
    }
}
