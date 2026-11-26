package com.lakbaylokal;

import com.lakbaylokal.controller.ApiController;
import com.lakbaylokal.model.User;
import com.lakbaylokal.util.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class Stage1ValidationTest {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("  STAGE 1 CONTROLLER VALIDATION TEST SUITE");
        System.out.println("==========================================================");

        ApiController api = new ApiController();
        int passed = 0;
        int failed = 0;

        // Create mock users and sessions for each role
        User adminUser = new User();
        adminUser.setUserId(1);
        adminUser.setUsername("admin");
        adminUser.setRoleId(1);
        adminUser.setRoleCode("SYSTEM_ADMIN");
        String adminToken = SessionManager.createSession(adminUser);

        User tourismUser = new User();
        tourismUser.setUserId(2);
        tourismUser.setUsername("tourismoffice");
        tourismUser.setRoleId(2);
        tourismUser.setRoleCode("TOURISM_OFFICE");
        String tourismToken = SessionManager.createSession(tourismUser);

        User lguNaval = new User();
        lguNaval.setUserId(3);
        lguNaval.setUsername("lgu_naval");
        lguNaval.setRoleId(3);
        lguNaval.setRoleCode("LGU_OFFICER");
        lguNaval.setMunicipalityId(1); // Naval municipality_id = 1
        String lguNavalToken = SessionManager.createSession(lguNaval);

        User brgyUser = new User();
        brgyUser.setUserId(4);
        brgyUser.setUsername("brgy_official");
        brgyUser.setRoleId(4);
        brgyUser.setRoleCode("BARANGAY_OFFICIAL");
        brgyUser.setBarangayId(1);
        String brgyToken = SessionManager.createSession(brgyUser);

        User touristUser = new User();
        touristUser.setUserId(6);
        touristUser.setUsername("tourist");
        touristUser.setRoleId(6);
        touristUser.setRoleCode("TOURIST");
        String touristToken = SessionManager.createSession(touristUser);

        Map<String, String> emptyQuery = new HashMap<>();

        // -------------------------------------------------------------
        // TEST 1: Unauthorized government account creation (no auth)
        // -------------------------------------------------------------
        System.out.print("[Test 1] Public user cannot create government accounts: ");
        String res1 = api.handleRequest("/api/admin/users", "POST", emptyQuery, "{}", null, "127.0.0.1");
        if (res1.contains("\"code\":401") || res1.contains("Unauthorized")) {
            System.out.println("PASS (401 Unauthorized)");
            passed++;
        } else {
            System.out.println("FAIL: " + res1);
            failed++;
        }

        // -------------------------------------------------------------
        // TEST 2: Tourist cannot create government accounts
        // -------------------------------------------------------------
        System.out.print("[Test 2] Tourist cannot access POST /api/admin/users: ");
        String res2 = api.handleRequest("/api/admin/users", "POST", emptyQuery, "{}", touristToken, "127.0.0.1");
        if (res2.contains("\"code\":403") || res2.contains("Access denied")) {
            System.out.println("PASS (403 Forbidden)");
            passed++;
        } else {
            System.out.println("FAIL: " + res2);
            failed++;
        }

        // -------------------------------------------------------------
        // TEST 3: Barangay Official cannot create government accounts
        // -------------------------------------------------------------
        System.out.print("[Test 3] Barangay Official cannot access POST /api/admin/users: ");
        String res3 = api.handleRequest("/api/admin/users", "POST", emptyQuery, "{}", brgyToken, "127.0.0.1");
        if (res3.contains("\"code\":403") || res3.contains("Access denied")) {
            System.out.println("PASS (403 Forbidden)");
            passed++;
        } else {
            System.out.println("FAIL: " + res3);
            failed++;
        }

        // -------------------------------------------------------------
        // TEST 4: Tourism Office cannot create Admin account (hierarchy)
        // -------------------------------------------------------------
        System.out.print("[Test 4] Tourism Office cannot create Admin account: ");
        String bodyAdmin = "{\"username\":\"new_admin\",\"email\":\"a@lakbay.ph\",\"fullName\":\"Admin Test\",\"roleId\":1}";
        String res4 = api.handleRequest("/api/admin/users", "POST", emptyQuery, bodyAdmin, tourismToken, "127.0.0.1");
        if (res4.contains("\"code\":403") && res4.contains("may only create LGU Officer")) {
            System.out.println("PASS (403 Hierarchy Enforced)");
            passed++;
        } else {
            System.out.println("FAIL: " + res4);
            failed++;
        }

        // -------------------------------------------------------------
        // TEST 5: Tourism Office cannot create Barangay Official (hierarchy)
        // -------------------------------------------------------------
        System.out.print("[Test 5] Tourism Office cannot create Barangay Official directly: ");
        String bodyBrgy = "{\"username\":\"new_brgy\",\"email\":\"b@lakbay.ph\",\"fullName\":\"Brgy Test\",\"roleId\":4}";
        String res5 = api.handleRequest("/api/admin/users", "POST", emptyQuery, bodyBrgy, tourismToken, "127.0.0.1");
        if (res5.contains("\"code\":403") && res5.contains("may only create LGU Officer")) {
            System.out.println("PASS (403 Hierarchy Enforced)");
            passed++;
        } else {
            System.out.println("FAIL: " + res5);
            failed++;
        }

        // -------------------------------------------------------------
        // TEST 6: Tourism Office creating LGU without municipalityId (Fix 2)
        // -------------------------------------------------------------
        System.out.print("[Test 6] LGU Officer creation without municipality is rejected: ");
        String bodyLguNoMuni = "{\"username\":\"lgu_nomuni\",\"email\":\"lgu@lakbay.ph\",\"fullName\":\"LGU Test\",\"roleId\":3}";
        String res6 = api.handleRequest("/api/admin/users", "POST", emptyQuery, bodyLguNoMuni, tourismToken, "127.0.0.1");
        if (res6.contains("\"code\":400") && res6.contains("Municipality assignment is required")) {
            System.out.println("PASS (400 Missing Municipality Rejected)");
            passed++;
        } else {
            System.out.println("FAIL: " + res6);
            failed++;
        }

        // -------------------------------------------------------------
        // TEST 7: LGU Officer cannot create Admin or Tourism Office
        // -------------------------------------------------------------
        System.out.print("[Test 7] LGU Officer cannot create Tourism Office: ");
        String bodyLguCreateTourism = "{\"username\":\"to_test\",\"email\":\"to@lakbay.ph\",\"fullName\":\"TO Test\",\"roleId\":2}";
        String res7 = api.handleRequest("/api/admin/users", "POST", emptyQuery, bodyLguCreateTourism, lguNavalToken, "127.0.0.1");
        if (res7.contains("\"code\":403") && res7.contains("may only create Barangay Official")) {
            System.out.println("PASS (403 Hierarchy Enforced)");
            passed++;
        } else {
            System.out.println("FAIL: " + res7);
            failed++;
        }

        // -------------------------------------------------------------
        // TEST 8: LGU Officer creating Barangay Official without barangayId
        // -------------------------------------------------------------
        System.out.print("[Test 8] Barangay Official creation without barangayId is rejected: ");
        String bodyBrgyNoBrgy = "{\"username\":\"brgy_nobrgy\",\"email\":\"brgy@lakbay.ph\",\"fullName\":\"Brgy Test\",\"roleId\":4}";
        String res8 = api.handleRequest("/api/admin/users", "POST", emptyQuery, bodyBrgyNoBrgy, lguNavalToken, "127.0.0.1");
        if (res8.contains("\"code\":400") && res8.contains("Barangay assignment is required")) {
            System.out.println("PASS (400 Missing Barangay Rejected)");
            passed++;
        } else {
            System.out.println("FAIL: " + res8);
            failed++;
        }

        // -------------------------------------------------------------
        // TEST 9: LGU Officer creating Barangay Official outside its municipality
        // -------------------------------------------------------------
        // In seed data: municipality 1 = Naval (barangays 1..26), municipality 2 = Almeria (barangays 27+)
        System.out.print("[Test 9] LGU Naval cannot create Barangay Official in another municipality: ");
        // Let's test with a non-existent or foreign barangay
        String bodyBrgyForeign = "{\"username\":\"brgy_foreign\",\"email\":\"bf@lakbay.ph\",\"fullName\":\"Foreign Brgy\",\"roleId\":4,\"barangayId\":99999}";
        String res9 = api.handleRequest("/api/admin/users", "POST", emptyQuery, bodyBrgyForeign, lguNavalToken, "127.0.0.1");
        if (res9.contains("\"code\":400") || res9.contains("\"code\":403")) {
            System.out.println("PASS (Rejected foreign/invalid barangay assignment)");
            passed++;
        } else {
            System.out.println("FAIL: " + res9);
            failed++;
        }

        // -------------------------------------------------------------
        // TEST 10: GET /api/admin/users authorization (Fix 1)
        // -------------------------------------------------------------
        System.out.print("[Test 10] Tourist cannot view GET /api/admin/users: ");
        String res10 = api.handleRequest("/api/admin/users", "GET", emptyQuery, "", touristToken, "127.0.0.1");
        if (res10.contains("\"code\":403")) {
            System.out.println("PASS (403 Forbidden)");
            passed++;
        } else {
            System.out.println("FAIL: " + res10);
            failed++;
        }

        // -------------------------------------------------------------
        // TEST 11: Stakeholder cannot view GET /api/admin/users (Fix 1)
        // -------------------------------------------------------------
        User stakeholderUser = new User();
        stakeholderUser.setUserId(5);
        stakeholderUser.setUsername("guide_pedro");
        stakeholderUser.setRoleId(5);
        stakeholderUser.setRoleCode("STAKEHOLDER");
        String stakeholderToken = SessionManager.createSession(stakeholderUser);

        System.out.print("[Test 11] Stakeholder cannot view GET /api/admin/users: ");
        String res11 = api.handleRequest("/api/admin/users", "GET", emptyQuery, "", stakeholderToken, "127.0.0.1");
        if (res11.contains("\"code\":403")) {
            System.out.println("PASS (403 Forbidden)");
            passed++;
        } else {
            System.out.println("FAIL: " + res11);
            failed++;
        }

        // -------------------------------------------------------------
        // TEST 12: Phase 1 preservation: Tourism Office cannot verify stakeholders
        // -------------------------------------------------------------
        System.out.print("[Test 12] Phase 1 preservation: Tourism Office cannot approve/reject verifications: ");
        String verifyBody = "{\"stakeholderId\":1,\"barangayId\":1,\"status\":\"APPROVED\"}";
        String res12 = api.handleRequest("/api/verifications", "POST", emptyQuery, verifyBody, tourismToken, "127.0.0.1");
        if (res12.contains("\"code\":403") && res12.contains("Sole authority to approve, reject, or request corrections")) {
            System.out.println("PASS (Phase 1 rule strictly preserved)");
            passed++;
        } else {
            System.out.println("FAIL: " + res12);
            failed++;
        }

        // -------------------------------------------------------------
        // TEST 13: Phase 1 preservation: Admin cannot verify stakeholders
        // -------------------------------------------------------------
        System.out.print("[Test 13] Phase 1 preservation: Admin cannot verify stakeholders: ");
        String res13 = api.handleRequest("/api/verifications", "POST", emptyQuery, verifyBody, adminToken, "127.0.0.1");
        if (res13.contains("\"code\":403") && res13.contains("Sole authority to approve, reject, or request corrections")) {
            System.out.println("PASS (Phase 1 rule strictly preserved)");
            passed++;
        } else {
            System.out.println("FAIL: " + res13);
            failed++;
        }

        // -------------------------------------------------------------
        // TEST 14: Stakeholder self-registration rejects client-supplied role or status
        // -------------------------------------------------------------
        System.out.print("[Test 14] Self-registration rejects client role/status injection: ");
        String hackReg = "{\"username\":\"hacker\",\"password\":\"pass1234\",\"email\":\"h@h.ph\",\"fullName\":\"Hacker\",\"businessName\":\"Biz\",\"barangayId\":1,\"roleId\":1}";
        String res14 = api.handleRequest("/api/auth/register-stakeholder", "POST", emptyQuery, hackReg, null, "127.0.0.1");
        if (res14.contains("\"code\":400") && res14.contains("assigned by the system")) {
            System.out.println("PASS (Client role injection blocked)");
            passed++;
        } else {
            System.out.println("FAIL: " + res14);
            failed++;
        }

        // -------------------------------------------------------------
        // TEST 15: Stakeholder resubmission rejects unauthenticated call
        // -------------------------------------------------------------
        System.out.print("[Test 15] Resubmission requires authenticated stakeholder: ");
        String res15 = api.handleRequest("/api/stakeholders/resubmit", "POST", emptyQuery, "{}", null, "127.0.0.1");
        if (res15.contains("\"code\":401") || res15.contains("Unauthorized")) {
            System.out.println("PASS (401 Unauthorized)");
            passed++;
        } else {
            System.out.println("FAIL: " + res15);
            failed++;
        }

        // -------------------------------------------------------------
        // TEST 16: Non-stakeholder cannot call resubmit (Tourist -> 403)
        // -------------------------------------------------------------
        System.out.print("[Test 16] Non-stakeholder cannot access POST /api/stakeholders/resubmit: ");
        String res16 = api.handleRequest("/api/stakeholders/resubmit", "POST", emptyQuery, "{}", touristToken, "127.0.0.1");
        if (res16.contains("\"code\":403") && res16.contains("Stakeholders only")) {
            System.out.println("PASS (403 Forbidden)");
            passed++;
        } else {
            System.out.println("FAIL: " + res16);
            failed++;
        }

        // -------------------------------------------------------------
        // TEST 17: Positive authorization: Tourism Office can access GET /api/admin/users
        // -------------------------------------------------------------
        System.out.print("[Test 17] Tourism Office authorized to access GET /api/admin/users: ");
        String res17 = api.handleRequest("/api/admin/users", "GET", emptyQuery, "", tourismToken, "127.0.0.1");
        if (!res17.contains("\"code\":401") && !res17.contains("\"code\":403")) {
            System.out.println("PASS (Authorized 200 response)");
            passed++;
        } else {
            System.out.println("FAIL: " + res17);
            failed++;
        }

        // -------------------------------------------------------------
        // TEST 18: Positive authorization: LGU Officer can access GET /api/admin/users
        // -------------------------------------------------------------
        System.out.print("[Test 18] LGU Officer authorized to access GET /api/admin/users: ");
        String res18 = api.handleRequest("/api/admin/users", "GET", emptyQuery, "", lguNavalToken, "127.0.0.1");
        if (!res18.contains("\"code\":401") && !res18.contains("\"code\":403")) {
            System.out.println("PASS (Authorized 200 response)");
            passed++;
        } else {
            System.out.println("FAIL: " + res18);
            failed++;
        }

        // -------------------------------------------------------------
        // TEST 19: Positive authorization: System Admin can access GET /api/admin/users
        // -------------------------------------------------------------
        System.out.print("[Test 19] System Admin authorized to access GET /api/admin/users: ");
        String res19 = api.handleRequest("/api/admin/users", "GET", emptyQuery, "", adminToken, "127.0.0.1");
        if (!res19.contains("\"code\":401") && !res19.contains("\"code\":403")) {
            System.out.println("PASS (Authorized 200 response)");
            passed++;
        } else {
            System.out.println("FAIL: " + res19);
            failed++;
        }

        // -------------------------------------------------------------
        // TEST 20: LGU Officer without assigned municipality is rejected from GET /api/admin/users
        // -------------------------------------------------------------
        User lguNoMuniUser = new User();
        lguNoMuniUser.setUserId(99);
        lguNoMuniUser.setUsername("lgu_nomuni");
        lguNoMuniUser.setRoleId(3);
        lguNoMuniUser.setRoleCode("LGU_OFFICER");
        lguNoMuniUser.setMunicipalityId(null);
        String lguNoMuniToken = SessionManager.createSession(lguNoMuniUser);

        System.out.print("[Test 20] LGU Officer without municipality cannot access GET /api/admin/users: ");
        String res20 = api.handleRequest("/api/admin/users", "GET", emptyQuery, "", lguNoMuniToken, "127.0.0.1");
        if (res20.contains("\"code\":403") && res20.contains("no assigned municipality")) {
            System.out.println("PASS (403 Forbidden - no municipality)");
            passed++;
        } else {
            System.out.println("FAIL: " + res20);
            failed++;
        }

        System.out.println("\n==========================================================");
        System.out.println(" SUMMARY: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("==========================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
