package com.lakbaylokal.controller;

import com.lakbaylokal.database.BackupRestoreService;
import com.lakbaylokal.database.ConnectionFactory;
import com.lakbaylokal.model.*;
import com.lakbaylokal.service.*;
import com.lakbaylokal.util.DateUtil;
import com.lakbaylokal.util.JsonUtil;
import com.lakbaylokal.util.SessionManager;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * Dispatches API requests, validates session tokens, enforces Role-Based Access Control,
 * and formats JSON responses for the desktop Javascript interface.
 */
public class ApiController {

    private final AuthService authService = new AuthService();
    private final DestinationService destinationService = new DestinationService();
    private final PackageService packageService = new PackageService();
    private final StakeholderService stakeholderService = new StakeholderService();
    private final BookingService bookingService = new BookingService();
    private final VerificationService verificationService = new VerificationService();
    private final ReviewService reviewService = new ReviewService();
    private final AnnouncementService announcementService = new AnnouncementService();
    private final ReportService reportService = new ReportService();
    private final AuditService auditService = new AuditService();
    private final UserService userService = new UserService();

    public String handleRequest(String path, String method, Map<String, String> queryParams,
                                String body, String authToken, String clientIp) {
        try {
            // Public endpoints
            if ("/api/auth/login".equals(path) && "POST".equalsIgnoreCase(method)) {
                return handleLogin(body, clientIp);
            }
            if ("/api/auth/register".equals(path) && "POST".equalsIgnoreCase(method)) {
                return handleRegister(body, clientIp);
            }
            if ("/api/auth/forgot-password".equals(path) && "POST".equalsIgnoreCase(method)) {
                return handleForgotPassword(body, clientIp);
            }
            if ("/api/auth/logout".equals(path)) {
                SessionManager.invalidate(authToken);
                return success("Logged out successfully.");
            }

            // Public / General catalogs
            if ("/api/destinations".equals(path) && "GET".equalsIgnoreCase(method)) {
                return handleGetDestinations(queryParams);
            }
            if (path.startsWith("/api/destinations/") && "GET".equalsIgnoreCase(method)) {
                int id = Integer.parseInt(path.substring("/api/destinations/".length()));
                return JsonUtil.toJson(destinationService.getDestinationById(id));
            }
            if ("/api/packages".equals(path) && "GET".equalsIgnoreCase(method)) {
                return handleGetPackages(queryParams);
            }
            if (path.startsWith("/api/packages/") && "GET".equalsIgnoreCase(method)) {
                int id = Integer.parseInt(path.substring("/api/packages/".length()));
                return JsonUtil.toJson(packageService.getPackageById(id));
            }
            if ("/api/announcements".equals(path) && "GET".equalsIgnoreCase(method)) {
                return handleGetAnnouncements(queryParams);
            }
            if ("/api/barangays".equals(path) && "GET".equalsIgnoreCase(method)) {
                return JsonUtil.toJson(destinationService.getBarangays());
            }
            if ("/api/roles".equals(path) && "GET".equalsIgnoreCase(method)) {
                return JsonUtil.toJson(userService.getAllRoles());
            }
            if ("/api/stakeholders/services".equals(path) && "GET".equalsIgnoreCase(method)) {
                String cat = queryParams.get("category");
                boolean includeUnavailable = "true".equalsIgnoreCase(queryParams.get("all")) || "false".equalsIgnoreCase(queryParams.get("availableOnly"));
                if (includeUnavailable) {
                    return JsonUtil.toJson(stakeholderService.getAllServices(cat, false));
                }
                return JsonUtil.toJson(stakeholderService.getAllAvailableServices(cat));
            }
            if ("/api/stakeholders".equals(path) && "GET".equalsIgnoreCase(method)) {
                User maybeUser = SessionManager.getUser(authToken);
                return handleGetStakeholders(queryParams, maybeUser);
            }
            if ("/api/reviews".equals(path) && "GET".equalsIgnoreCase(method)) {
                return handleGetReviews(queryParams);
            }
            if ("/api/settings".equals(path) && "GET".equalsIgnoreCase(method)) {
                return handleGetSettings();
            }
            if ("/api/reports/kpis".equals(path) && "GET".equalsIgnoreCase(method)) {
                return JsonUtil.toJson(reportService.getOverviewKPIs());
            }
            if ("/api/reports/footfall".equals(path) && "GET".equalsIgnoreCase(method)) {
                return JsonUtil.toJson(reportService.getDestinationFootfallSummaries());
            }
            if ("/api/reports/stakeholders".equals(path) && "GET".equalsIgnoreCase(method)) {
                return JsonUtil.toJson(reportService.getStakeholderSummaries());
            }
            if ("/api/reports/barangays".equals(path) && "GET".equalsIgnoreCase(method)) {
                return JsonUtil.toJson(reportService.getBarangaySummaries());
            }

            // Authenticated endpoints check
            User currentUser = SessionManager.getUser(authToken);
            if (currentUser == null) {
                return error("Unauthorized. Please log in.", 401);
            }

            // Current user session check
            if ("/api/auth/me".equals(path) && "GET".equalsIgnoreCase(method)) {
                return handleGetMe(currentUser);
            }

            // Destinations Management (Officials & Admin)
            if ("/api/destinations".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isBarangayOrHigher(currentUser)) return error("Access denied. Authorized officials only.", 403);
                return handleSaveDestination(body, currentUser, clientIp);
            }
            if ("/api/destinations/validate".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isBarangayOrHigher(currentUser)) return error("Access denied. Authorized officials only.", 403);
                return handleValidateDestination(body, currentUser, clientIp);
            }
            if ("/api/destinations/delete".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isTourismOffice(currentUser)) return error("Access denied. Tourism Office or Admin only.", 403);
                return handleDeleteDestination(body, currentUser, clientIp);
            }

            // Barangay Records Management (Barangay Officials for own barangay, LGU, Tourism Office, Admin)
            if ("/api/barangays".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isBarangayOrHigher(currentUser)) return error("Access denied. Authorized officials only.", 403);
                return handleUpdateBarangayRecord(body, currentUser, clientIp);
            }

            // Stakeholders Management (Stakeholders, Barangay Officials, Tourism Office, Admin)
            if ("/api/stakeholders".equals(path) && "GET".equalsIgnoreCase(method)) {
                return handleGetStakeholders(queryParams, currentUser);
            }
            if ("/api/stakeholders/me".equals(path) && "GET".equalsIgnoreCase(method)) {
                StakeholderProfile sp = stakeholderService.getProfileByUserId(currentUser.getUserId());
                return sp != null ? JsonUtil.toJson(sp) : error("Stakeholder profile not found.", 404);
            }
            if ("/api/stakeholders".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isStakeholder(currentUser) && !isBarangayOrHigher(currentUser)) {
                    return error("Access denied. Stakeholders or authorized officials only.", 403);
                }
                return handleSaveStakeholderProfile(body, currentUser, clientIp);
            }
            if ("/api/stakeholders/services".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isStakeholder(currentUser) && !isTourismOffice(currentUser) && !isAdmin(currentUser)) {
                    return error("Access denied. Stakeholders or Tourism Office/Admin only.", 403);
                }
                return handleSaveStakeholderService(body, currentUser, clientIp);
            }
            if ("/api/stakeholders/services/delete".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isStakeholder(currentUser) && !isTourismOffice(currentUser) && !isAdmin(currentUser)) {
                    return error("Access denied. Stakeholders or Tourism Office/Admin only.", 403);
                }
                return handleDeleteStakeholderService(body, currentUser, clientIp);
            }

            // Bookings Management
            if ("/api/bookings".equals(path) && "GET".equalsIgnoreCase(method)) {
                return handleGetBookings(queryParams, currentUser);
            }
            if ("/api/bookings".equals(path) && "POST".equalsIgnoreCase(method)) {
                return handleCreateBooking(body, currentUser, clientIp);
            }
            if ("/api/bookings/status".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isBarangayOrHigher(currentUser) && !isStakeholder(currentUser) && !isTourist(currentUser)) {
                    return error("Access denied.", 403);
                }
                return handleUpdateBookingStatus(body, currentUser, clientIp);
            }
            if ("/api/bookings/item-status".equals(path) && "POST".equalsIgnoreCase(method)) {
                return handleUpdateBookingItemStatus(body, currentUser, clientIp);
            }

            // Reviews
            if ("/api/reviews".equals(path) && "GET".equalsIgnoreCase(method)) {
                return handleGetReviews(queryParams);
            }
            if ("/api/reviews".equals(path) && "POST".equalsIgnoreCase(method)) {
                return handleCreateReview(body, currentUser, clientIp);
            }

            // Verifications (Barangay Officials, Tourism Office, Admin)
            if ("/api/verifications".equals(path) && "GET".equalsIgnoreCase(method)) {
                if (!isBarangayOrHigher(currentUser)) return error("Access denied. Authorized officials only.", 403);
                return handleGetVerifications(queryParams, currentUser);
            }
            if ("/api/verifications".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isBarangayOrHigher(currentUser)) return error("Access denied. Authorized officials only.", 403);
                return handleProcessVerification(body, currentUser, clientIp);
            }

            // Announcements (Tourism Office, LGU, Admin)
            if ("/api/announcements".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isLguOrHigher(currentUser)) return error("Access denied. Tourism Office, LGU, or Admin only.", 403);
                return handleSaveAnnouncement(body, currentUser, clientIp);
            }
            if ("/api/announcements/delete".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isLguOrHigher(currentUser)) return error("Access denied. Tourism Office, LGU, or Admin only.", 403);
                return handleDeleteAnnouncement(body, currentUser, clientIp);
            }

            // Packages (Tourism Office, Admin)
            if ("/api/packages".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isTourismOffice(currentUser)) return error("Access denied. Tourism Office or Admin only.", 403);
                return handleSavePackage(body, currentUser, clientIp);
            }
            if ("/api/packages/delete".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isTourismOffice(currentUser)) return error("Access denied. Tourism Office or Admin only.", 403);
                return handleDeletePackage(body, currentUser, clientIp);
            }

            // Reports & Monitoring
            if ("/api/reports/kpis".equals(path) && "GET".equalsIgnoreCase(method)) {
                return JsonUtil.toJson(reportService.getOverviewKPIs());
            }
            if ("/api/reports/footfall".equals(path) && "GET".equalsIgnoreCase(method)) {
                return JsonUtil.toJson(reportService.getDestinationFootfallSummaries());
            }
            if ("/api/reports/stakeholders".equals(path) && "GET".equalsIgnoreCase(method)) {
                return JsonUtil.toJson(reportService.getStakeholderSummaries());
            }
            if ("/api/reports/barangays".equals(path) && "GET".equalsIgnoreCase(method)) {
                return JsonUtil.toJson(reportService.getBarangaySummaries());
            }
            if ("/api/reports/arrival-logs".equals(path) && "GET".equalsIgnoreCase(method)) {
                Integer destId = queryParams.containsKey("destinationId") ? Integer.valueOf(queryParams.get("destinationId")) : null;
                return JsonUtil.toJson(reportService.getArrivalLogs(destId, queryParams.get("startDate"), queryParams.get("endDate")));
            }
            if ("/api/reports/arrival".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isBarangayOrHigher(currentUser)) return error("Access denied. Authorized officials only.", 403);
                return handleRecordArrival(body, currentUser, clientIp);
            }

            // System Settings (Admin only for write)
            if ("/api/settings".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isAdmin(currentUser)) return error("Admin access required.", 403);
                return handleSaveSettings(body, currentUser, clientIp);
            }

            // User Management (Admin only)
            if ("/api/admin/users".equals(path) && "GET".equalsIgnoreCase(method)) {
                if (!isAdmin(currentUser)) return error("Admin access required.", 403);
                return JsonUtil.toJson(userService.getAllUsers());
            }
            if ("/api/admin/users".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isAdmin(currentUser)) return error("Admin access required.", 403);
                return handleCreateUser(body, currentUser, clientIp);
            }
            if ("/api/admin/users/update".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isAdmin(currentUser)) return error("Admin access required.", 403);
                return handleUpdateUser(body, currentUser, clientIp);
            }
            if ("/api/admin/users/status".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isAdmin(currentUser)) return error("Admin access required.", 403);
                return handleUpdateUserStatus(body, currentUser, clientIp);
            }
            if ("/api/admin/users/reset-password".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isAdmin(currentUser)) return error("Admin access required.", 403);
                return handleResetUserPassword(body, currentUser, clientIp);
            }

            // Audit Logs (Admin only)
            if ("/api/admin/logs".equals(path) && "GET".equalsIgnoreCase(method)) {
                if (!isAdmin(currentUser)) return error("Admin access required.", 403);
                int limit = queryParams.containsKey("limit") ? Integer.parseInt(queryParams.get("limit")) : 50;
                return JsonUtil.toJson(auditService.getRecentLogs(limit));
            }

            // Database Maintenance (Admin only)
            if ("/api/admin/database/backup".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isAdmin(currentUser)) return error("Admin access required.", 403);
                File file = BackupRestoreService.createBackup();
                auditService.log(currentUser.getUserId(), currentUser.getUsername(), "DATABASE_BACKUP", "DATABASE", 1, clientIp, "Backup created: " + file.getName());
                Map<String, Object> resp = new HashMap<>();
                resp.put("success", true);
                resp.put("message", "Backup file generated: " + file.getName());
                resp.put("fileName", file.getName());
                return JsonUtil.toJson(resp);
            }

            return error("Endpoint not found: " + path, 404);

        } catch (Exception e) {
            System.err.println("[ApiController] Request processing error: " + e.getMessage());
            return error("Internal Server Error: " + e.getMessage(), 500);
        }
    }

    private String handleLogin(String body, String clientIp) {
        Map<String, Object> data = JsonUtil.parseJsonObject(body);
        String username = (String) data.get("username");
        String password = (String) data.get("password");
        Map<String, Object> res = authService.login(username, password);

        if (Boolean.TRUE.equals(res.get("success"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> u = (Map<String, Object>) res.get("user");
            auditService.log((Integer) u.get("userId"), (String) u.get("username"), "LOGIN_SUCCESS", "AUTH", (Integer) u.get("userId"), clientIp, "User logged in");
        }
        return JsonUtil.toJson(res);
    }

    private String handleRegister(String body, String clientIp) {
        Map<String, Object> data = JsonUtil.parseJsonObject(body);
        String username = (String) data.get("username");
        String password = (String) data.get("password");
        String email = (String) data.get("email");
        String fullName = (String) data.get("fullName");
        String phone = (String) data.get("phone");
        String originCity = (String) data.get("originCity");

        Map<String, Object> res = authService.registerTourist(username, password, email, fullName, phone, originCity);
        if (Boolean.TRUE.equals(res.get("success"))) {
            auditService.log(null, username, "REGISTER_TOURIST", "USERS", null, clientIp, "New tourist registered: " + fullName);
        }
        return JsonUtil.toJson(res);
    }

    private String handleForgotPassword(String body, String clientIp) {
        Map<String, Object> data = JsonUtil.parseJsonObject(body);
        String identifier = (String) data.get("usernameOrEmail");
        if (identifier == null) identifier = (String) data.get("identifier");
        if (identifier == null) identifier = (String) data.get("username");
        if (identifier == null) identifier = (String) data.get("email");
        String newPassword = (String) data.get("newPassword");
        Map<String, Object> res = authService.forgotPassword(identifier, newPassword);
        if (Boolean.TRUE.equals(res.get("success")) && newPassword != null && !newPassword.isBlank()) {
            auditService.log(null, identifier, "PASSWORD_RESET_SELF", "AUTH", null, clientIp, "Password reset via self-recovery");
        }
        return JsonUtil.toJson(res);
    }

    private String handleGetMe(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getUserId());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("fullName", user.getFullName());
        map.put("phone", user.getPhone());
        map.put("roleId", user.getRoleId());
        map.put("roleCode", user.getRoleCode());
        map.put("roleName", user.getRoleName());
        map.put("barangayId", user.getBarangayId());
        map.put("barangayName", user.getBarangayName());
        map.put("municipalityName", user.getMunicipalityName());
        map.put("status", user.getStatus());

        if (isTourist(user)) {
            TouristProfile tp = bookingService.getTouristProfileByUserId(user.getUserId());
            if (tp != null) map.put("touristProfileId", tp.getProfileId());
        } else if (isStakeholder(user)) {
            StakeholderProfile sp = stakeholderService.getProfileByUserId(user.getUserId());
            if (sp != null) {
                map.put("stakeholderProfileId", sp.getProfileId());
                map.put("verificationStatus", sp.getVerificationStatus());
                map.put("category", sp.getCategory());
                map.put("businessName", sp.getBusinessName());
            }
        }
        return JsonUtil.toJson(map);
    }

    private String handleGetDestinations(Map<String, String> q) {
        String cat = q.get("category");
        Integer brgy = q.containsKey("barangayId") ? Integer.valueOf(q.get("barangayId")) : null;
        String status = q.getOrDefault("status", "ACTIVE");
        return JsonUtil.toJson(destinationService.getDestinations(cat, brgy, status));
    }

    private String handleSaveDestination(String body, User user, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        Destination dest = new Destination();
        Object idObj = d.get("destinationId");
        if (idObj instanceof Number num) dest.setDestinationId(num.intValue());

        dest.setName((String) d.get("name"));
        dest.setCategory((String) d.get("category"));
        dest.setDescription((String) d.get("description"));
        if (d.get("barangayId") instanceof Number num) dest.setBarangayId(num.intValue());
        if (d.get("entranceFee") instanceof Number num) dest.setEntranceFee(BigDecimal.valueOf(num.doubleValue()));
        else dest.setEntranceFee(BigDecimal.ZERO);
        dest.setOperatingHours((String) d.getOrDefault("operatingHours", "6:00 AM - 5:00 PM"));
        dest.setLocationCoordinates((String) d.get("locationCoordinates"));
        dest.setContactInfo((String) d.get("contactInfo"));
        dest.setImageUrl((String) d.get("imageUrl"));
        dest.setStatus((String) d.getOrDefault("status", "ACTIVE"));

        if ("BARANGAY_OFFICIAL".equalsIgnoreCase(user.getRoleCode()) && user.getBarangayId() != null) {
            if (dest.getBarangayId() > 0 && dest.getBarangayId() != user.getBarangayId()) {
                return error("Access denied: Barangay Officials can only manage destinations in their assigned barangay.", 403);
            }
            dest.setBarangayId(user.getBarangayId());
        }

        boolean ok;
        if (dest.getDestinationId() > 0) {
            ok = destinationService.updateDestination(dest);
            auditService.log(user.getUserId(), user.getUsername(), "UPDATE_DESTINATION", "DESTINATIONS", dest.getDestinationId(), clientIp, "Updated " + dest.getName());
        } else {
            int newId = destinationService.createDestination(dest);
            ok = newId > 0;
            dest.setDestinationId(newId);
            auditService.log(user.getUserId(), user.getUsername(), "CREATE_DESTINATION", "DESTINATIONS", newId, clientIp, "Created " + dest.getName());
        }
        return ok ? success("Destination saved successfully.") : error("Failed to save destination.", 500);
    }

    private String handleValidateDestination(String body, User user, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        int destId = ((Number) d.get("destinationId")).intValue();
        Destination existing = destinationService.getDestinationById(destId);
        if (existing == null) return error("Destination not found.", 404);

        if ("BARANGAY_OFFICIAL".equalsIgnoreCase(user.getRoleCode()) && user.getBarangayId() != null) {
            if (existing.getBarangayId() != user.getBarangayId()) {
                return error("Access denied: You can only validate destinations within your authorized barangay jurisdiction.", 403);
            }
        }

        boolean ok = destinationService.validateDestination(destId, user.getUserId());
        if (ok) {
            auditService.log(user.getUserId(), user.getUsername(), "VALIDATE_DESTINATION", "DESTINATIONS", destId, clientIp, "Validated by " + user.getFullName());
            return success("Destination validated successfully.");
        }
        return error("Validation failed.", 500);
    }

    private String handleDeleteDestination(String body, User user, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        int destId = ((Number) d.get("destinationId")).intValue();
        boolean ok = destinationService.deleteDestination(destId);
        if (ok) {
            auditService.log(user.getUserId(), user.getUsername(), "DELETE_DESTINATION", "DESTINATIONS", destId, clientIp, "Deleted destination ID: " + destId);
            return success("Destination deleted.");
        }
        return error("Delete failed.", 500);
    }

    private String handleGetPackages(Map<String, String> q) {
        String type = q.get("type");
        String status = q.getOrDefault("status", "ACTIVE");
        return JsonUtil.toJson(packageService.getPackages(type, status));
    }

    private String handleSavePackage(String body, User user, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        TourismPackage pkg = new TourismPackage();
        if (d.get("packageId") instanceof Number num) pkg.setPackageId(num.intValue());
        pkg.setName((String) d.get("name"));
        pkg.setDescription((String) d.get("description"));
        pkg.setPackageType((String) d.getOrDefault("packageType", "PRE_DESIGNED"));
        if (d.get("pricePerPax") instanceof Number num) pkg.setPricePerPax(BigDecimal.valueOf(num.doubleValue()));
        if (d.get("durationDays") instanceof Number num) pkg.setDurationDays(num.intValue());
        if (d.get("minPax") instanceof Number num) pkg.setMinPax(num.intValue());
        if (d.get("maxPax") instanceof Number num) pkg.setMaxPax(num.intValue());
        pkg.setInclusions((String) d.get("inclusions"));
        pkg.setExclusions((String) d.get("exclusions"));
        pkg.setStatus((String) d.getOrDefault("status", "ACTIVE"));
        pkg.setImageUrl((String) d.get("imageUrl"));
        pkg.setCreatedBy(user.getUserId());

        List<Integer> destIds = new ArrayList<>();
        if (d.get("destinationIds") instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Number num) destIds.add(num.intValue());
            }
        }
        List<Integer> svcIds = new ArrayList<>();
        if (d.get("serviceIds") instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Number num) svcIds.add(num.intValue());
            }
        }

        boolean ok;
        if (pkg.getPackageId() > 0) {
            ok = packageService.updatePackage(pkg, destIds, svcIds);
            auditService.log(user.getUserId(), user.getUsername(), "UPDATE_PACKAGE", "PACKAGES", pkg.getPackageId(), clientIp, "Updated " + pkg.getName());
        } else {
            int newId = packageService.createPackage(pkg, destIds, svcIds);
            ok = newId > 0;
            auditService.log(user.getUserId(), user.getUsername(), "CREATE_PACKAGE", "PACKAGES", newId, clientIp, "Created " + pkg.getName());
        }
        return ok ? success("Tourism package saved.") : error("Failed to save package.", 500);
    }

    private String handleDeletePackage(String body, User user, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        int pkgId = ((Number) d.get("packageId")).intValue();
        boolean ok = packageService.deletePackage(pkgId);
        if (ok) {
            auditService.log(user.getUserId(), user.getUsername(), "DELETE_PACKAGE", "PACKAGES", pkgId, clientIp, "Deleted package ID: " + pkgId);
            return success("Package deleted.");
        }
        return error("Failed to delete package.", 500);
    }

    private String handleGetStakeholders(Map<String, String> q, User currentUser) {
        String cat = q.get("category");
        Integer brgy = q.containsKey("barangayId") && !q.get("barangayId").isBlank() && !"ALL".equalsIgnoreCase(q.get("barangayId"))
                ? Integer.valueOf(q.get("barangayId"))
                : null;
        String status = q.get("verificationStatus");

        // Enforce Barangay jurisdiction filtering when Barangay Official requests verification queue or jurisdiction-scoped list
        if (currentUser != null && "BARANGAY_OFFICIAL".equalsIgnoreCase(currentUser.getRoleCode()) && currentUser.getBarangayId() != null) {
            if ("PENDING".equalsIgnoreCase(status) || "true".equalsIgnoreCase(q.get("jurisdictionOnly"))) {
                brgy = currentUser.getBarangayId();
            }
        }
        return JsonUtil.toJson(stakeholderService.getStakeholders(cat, brgy, status));
    }

    private String handleUpdateBarangayRecord(String body, User user, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        if (!(d.get("barangayId") instanceof Number num)) {
            return error("barangayId is required.", 400);
        }
        int brgyId = num.intValue();
        if ("BARANGAY_OFFICIAL".equalsIgnoreCase(user.getRoleCode()) && user.getBarangayId() != null) {
            if (brgyId != user.getBarangayId()) {
                return error("Access denied: Barangay Officials can only update their assigned barangay record.", 403);
            }
        }
        String contactPerson = (String) d.get("contactPerson");
        String contactNumber = (String) d.get("contactNumber");
        boolean ok = destinationService.updateBarangayContact(brgyId, contactPerson, contactNumber);
        if (ok) {
            auditService.log(user.getUserId(), user.getUsername(), "UPDATE_BARANGAY", "BARANGAYS", brgyId, clientIp, "Updated focal contact: " + contactPerson);
            return success("Barangay tourism record updated successfully.");
        }
        return error("Failed to update barangay record.", 500);
    }

    private String handleSaveStakeholderProfile(String body, User user, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        if (isStakeholder(user)) {
            StakeholderProfile sp = stakeholderService.getProfileByUserId(user.getUserId());
            if (sp == null) {
                sp = new StakeholderProfile();
                sp.setUserId(user.getUserId());
                sp.setBarangayId(d.get("barangayId") instanceof Number n ? n.intValue() : (user.getBarangayId() != null ? user.getBarangayId() : 1));
                sp.setBusinessName((String) d.get("businessName"));
                sp.setCategory((String) d.getOrDefault("category", "TOUR_GUIDE"));
                sp.setAccreditationNo((String) d.get("accreditationNo"));
                sp.setContactNumber((String) d.get("contactNumber"));
                sp.setYearsExperience(d.get("yearsExperience") instanceof Number n ? n.intValue() : 1);
                sp.setVerificationStatus("PENDING");
                int newId = stakeholderService.registerStakeholder(sp);
                if (newId > 0) {
                    auditService.log(user.getUserId(), user.getUsername(), "CREATE_STAKEHOLDER_PROFILE", "STAKEHOLDER_PROFILES", newId, clientIp, "Created profile: " + sp.getBusinessName());
                    return success("Stakeholder profile created.");
                }
                return error("Failed to create stakeholder profile.", 500);
            } else {
                if (d.get("barangayId") instanceof Number n) sp.setBarangayId(n.intValue());
                if (d.get("businessName") != null) sp.setBusinessName((String) d.get("businessName"));
                if (d.get("category") != null) sp.setCategory((String) d.get("category"));
                if (d.get("accreditationNo") != null) sp.setAccreditationNo((String) d.get("accreditationNo"));
                if (d.get("contactNumber") != null) sp.setContactNumber((String) d.get("contactNumber"));
                if (d.get("yearsExperience") instanceof Number n) sp.setYearsExperience(n.intValue());
                boolean ok = stakeholderService.updateProfile(sp);
                if (ok) {
                    auditService.log(user.getUserId(), user.getUsername(), "UPDATE_STAKEHOLDER_PROFILE", "STAKEHOLDER_PROFILES", sp.getProfileId(), clientIp, "Updated profile: " + sp.getBusinessName());
                    return success("Stakeholder profile updated successfully.");
                }
                return error("Failed to update stakeholder profile.", 500);
            }
        }

        // Barangay Official, LGU, Tourism Office, or Admin registering or updating a stakeholder
        int profileId = d.get("profileId") instanceof Number n ? n.intValue() : 0;
        int brgyId = d.get("barangayId") instanceof Number n ? n.intValue() : (user.getBarangayId() != null ? user.getBarangayId() : 1);
        if ("BARANGAY_OFFICIAL".equalsIgnoreCase(user.getRoleCode()) && user.getBarangayId() != null) {
            if (brgyId != user.getBarangayId()) {
                return error("Access denied: Barangay Officials can only register stakeholders within their assigned barangay.", 403);
            }
            brgyId = user.getBarangayId();
        }

        if (profileId > 0) {
            StakeholderProfile existing = stakeholderService.getProfileById(profileId);
            if (existing == null) return error("Stakeholder profile not found.", 404);
            if ("BARANGAY_OFFICIAL".equalsIgnoreCase(user.getRoleCode()) && user.getBarangayId() != null && existing.getBarangayId() != user.getBarangayId()) {
                return error("Access denied: Stakeholder belongs to another barangay jurisdiction.", 403);
            }
            existing.setBarangayId(brgyId);
            if (d.get("businessName") != null) existing.setBusinessName((String) d.get("businessName"));
            if (d.get("category") != null) existing.setCategory((String) d.get("category"));
            if (d.get("accreditationNo") != null) existing.setAccreditationNo((String) d.get("accreditationNo"));
            if (d.get("contactNumber") != null) existing.setContactNumber((String) d.get("contactNumber"));
            if (d.get("yearsExperience") instanceof Number n) existing.setYearsExperience(n.intValue());
            boolean ok = stakeholderService.updateProfile(existing);
            if (ok) {
                auditService.log(user.getUserId(), user.getUsername(), "UPDATE_STAKEHOLDER_PROFILE", "STAKEHOLDER_PROFILES", profileId, clientIp, "Updated stakeholder: " + existing.getBusinessName());
                return success("Stakeholder profile updated.");
            }
            return error("Failed to update stakeholder profile.", 500);
        }

        // Creating a new stakeholder (and user account if needed)
        int targetUserId = d.get("userId") instanceof Number n ? n.intValue() : 0;
        if (targetUserId <= 0) {
            String uname = (String) d.get("username");
            String email = (String) d.get("email");
            String fullName = (String) d.get("ownerName");
            if (fullName == null) fullName = (String) d.get("fullName");
            String phone = (String) d.get("contactNumber");
            String rawPass = (String) d.getOrDefault("password", "password123");
            if (uname == null || uname.isBlank() || email == null || email.isBlank() || fullName == null || fullName.isBlank()) {
                return error("Owner full name, username, and email are required to register a new stakeholder.", 400);
            }
            User newStkUser = new User();
            newStkUser.setUsername(uname.trim());
            newStkUser.setEmail(email.trim());
            newStkUser.setFullName(fullName.trim());
            newStkUser.setPhone(phone);
            newStkUser.setRoleId(5); // STAKEHOLDER
            newStkUser.setBarangayId(brgyId);
            newStkUser.setStatus("ACTIVE");
            targetUserId = userService.createUser(newStkUser, rawPass);
            if (targetUserId <= 0) {
                return error("Could not create user account for stakeholder (username or email may already exist).", 400);
            }
        }

        StakeholderProfile sp = new StakeholderProfile();
        sp.setUserId(targetUserId);
        sp.setBarangayId(brgyId);
        sp.setBusinessName((String) d.get("businessName"));
        sp.setCategory((String) d.getOrDefault("category", "TOUR_GUIDE"));
        sp.setAccreditationNo((String) d.get("accreditationNo"));
        sp.setContactNumber((String) d.get("contactNumber"));
        sp.setYearsExperience(d.get("yearsExperience") instanceof Number n ? n.intValue() : 1);
        sp.setVerificationStatus("BARANGAY_OFFICIAL".equalsIgnoreCase(user.getRoleCode()) || isTourismOffice(user) || isAdmin(user)
                ? (String) d.getOrDefault("verificationStatus", "PENDING")
                : "PENDING");

        int newProfId = stakeholderService.registerStakeholder(sp);
        if (newProfId > 0) {
            auditService.log(user.getUserId(), user.getUsername(), "REGISTER_STAKEHOLDER", "STAKEHOLDER_PROFILES", newProfId, clientIp, "Registered stakeholder: " + sp.getBusinessName());
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("profileId", newProfId);
            res.put("userId", targetUserId);
            res.put("message", "Community stakeholder registered successfully.");
            return JsonUtil.toJson(res);
        }
        return error("Failed to register stakeholder profile.", 500);
    }

    private String handleSaveStakeholderService(String body, User user, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        TourismService svc = new TourismService();
        if (d.get("serviceId") instanceof Number num) svc.setServiceId(num.intValue());

        if (isStakeholder(user)) {
            StakeholderProfile sp = stakeholderService.getProfileByUserId(user.getUserId());
            if (sp == null) return error("Stakeholder profile not found for user.", 400);
            svc.setStakeholderId(sp.getProfileId());
            if (svc.getServiceId() > 0) {
                TourismService existing = stakeholderService.getServiceById(svc.getServiceId());
                if (existing != null && existing.getStakeholderId() != sp.getProfileId()) {
                    return error("Access denied: You cannot edit another stakeholder's service.", 403);
                }
            }
        } else if (isAdmin(user) || isTourismOffice(user)) {
            if (d.get("stakeholderId") instanceof Number num) {
                svc.setStakeholderId(num.intValue());
            } else if (svc.getServiceId() > 0) {
                TourismService existing = stakeholderService.getServiceById(svc.getServiceId());
                if (existing != null) svc.setStakeholderId(existing.getStakeholderId());
                else return error("Service not found.", 404);
            } else {
                StakeholderProfile sp = stakeholderService.getProfileByUserId(user.getUserId());
                if (sp != null) svc.setStakeholderId(sp.getProfileId());
                else return error("stakeholderId required.", 400);
            }
        } else {
            return error("Unauthorized.", 403);
        }

        svc.setServiceName((String) d.get("serviceName"));
        svc.setServiceCategory((String) d.get("serviceCategory"));
        svc.setDescription((String) d.get("description"));
        if (d.get("basePrice") instanceof Number num) svc.setBasePrice(BigDecimal.valueOf(num.doubleValue()));
        String rawUnit = (String) d.getOrDefault("pricingUnit", "PER_PERSON");
        if ("PER_DAY".equalsIgnoreCase(rawUnit)) rawUnit = "PER_TRIP";
        svc.setPricingUnit(rawUnit);
        if (d.get("maxCapacity") instanceof Number num) svc.setMaxCapacity(num.intValue());
        Object availObj = d.containsKey("available") ? d.get("available") : d.getOrDefault("isAvailable", true);
        svc.setAvailable(Boolean.TRUE.equals(availObj));

        boolean ok;
        if (svc.getServiceId() > 0) {
            ok = stakeholderService.updateService(svc);
            auditService.log(user.getUserId(), user.getUsername(), "UPDATE_SERVICE", "SERVICES", svc.getServiceId(), clientIp, "Updated " + svc.getServiceName());
        } else {
            int newId = stakeholderService.createService(svc);
            ok = newId > 0;
            auditService.log(user.getUserId(), user.getUsername(), "CREATE_SERVICE", "SERVICES", newId, clientIp, "Created " + svc.getServiceName());
        }
        return ok ? success("Service listing saved.") : error("Failed to save service.", 500);
    }

    private String handleDeleteStakeholderService(String body, User user, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        int svcId = ((Number) d.get("serviceId")).intValue();

        if (isStakeholder(user)) {
            StakeholderProfile sp = stakeholderService.getProfileByUserId(user.getUserId());
            if (sp == null) return error("Stakeholder profile not found.", 400);
            TourismService existing = stakeholderService.getServiceById(svcId);
            if (existing != null && existing.getStakeholderId() != sp.getProfileId()) {
                return error("Access denied: You cannot delete another stakeholder's service.", 403);
            }
        } else if (!isAdmin(user) && !isTourismOffice(user)) {
            return error("Unauthorized.", 403);
        }

        boolean ok = stakeholderService.deleteService(svcId);
        if (ok) {
            auditService.log(user.getUserId(), user.getUsername(), "DELETE_SERVICE", "SERVICES", svcId, clientIp, "Deleted service ID: " + svcId);
            return success("Service deleted.");
        }
        return error("Failed to delete service.", 500);
    }

    private String handleGetBookings(Map<String, String> q, User user) {
        String role = user.getRoleCode();
        if ("TOURIST".equalsIgnoreCase(role)) {
            TouristProfile tp = bookingService.getTouristProfileByUserId(user.getUserId());
            if (tp == null) return "[]";
            return JsonUtil.toJson(bookingService.getBookingsByTourist(tp.getProfileId()));
        } else if ("STAKEHOLDER".equalsIgnoreCase(role)) {
            StakeholderProfile sp = stakeholderService.getProfileByUserId(user.getUserId());
            if (sp == null) return "[]";
            return JsonUtil.toJson(bookingService.getBookingsByStakeholder(sp.getProfileId()));
        } else {
            // Barangay, LGU, Tourism Office, Admin can view all bookings
            String status = q.get("status");
            return JsonUtil.toJson(bookingService.getAllBookings(status));
        }
    }

    private String handleCreateBooking(String body, User user, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        Booking b = new Booking();

        TouristProfile tp = bookingService.getTouristProfileByUserId(user.getUserId());
        if (tp == null) {
            return error("Tourist profile not found. Only registered tourists can make bookings.", 400);
        }
        b.setTouristId(tp.getProfileId());

        if (d.get("packageId") instanceof Number num) {
            b.setPackageId(num.intValue());
        }
        b.setTravelDateStart(Date.valueOf((String) d.get("travelDateStart")));
        b.setTravelDateEnd(Date.valueOf((String) d.get("travelDateEnd")));
        Object paxObj = d.get("numberOfPax");
        int pax = (paxObj instanceof Number num) ? num.intValue() : 1;
        b.setNumberOfPax(pax);
        b.setTotalAmount(BigDecimal.valueOf(((Number) d.get("totalAmount")).doubleValue()));
        b.setStatus("PENDING");
        b.setCustomNotes((String) d.get("customNotes"));

        List<BookingItem> items = new ArrayList<>();
        if (d.get("items") instanceof List<?> list) {
            for (Object obj : list) {
                if (obj instanceof Map<?, ?> m) {
                    BookingItem item = new BookingItem();
                    item.setServiceId(((Number) m.get("serviceId")).intValue());
                    item.setStakeholderId(((Number) m.get("stakeholderId")).intValue());
                    item.setUnitPrice(BigDecimal.valueOf(((Number) m.get("unitPrice")).doubleValue()));
                    Object qObj = m.get("quantity");
                    int qty = (qObj instanceof Number num) ? num.intValue() : 1;
                    item.setQuantity(qty);
                    item.setSubtotal(BigDecimal.valueOf(((Number) m.get("subtotal")).doubleValue()));
                    item.setStatus("PENDING");
                    items.add(item);
                }
            }
        }

        // Automatically assign linked package services when a Tourist books a pre-designed package
        if (items.isEmpty() && b.getPackageId() != null && b.getPackageId() > 0) {
            TourismPackage pkg = packageService.getPackageById(b.getPackageId());
            if (pkg != null && pkg.getServices() != null) {
                for (TourismService svc : pkg.getServices()) {
                    if (svc.getStakeholderId() > 0) {
                        BookingItem item = new BookingItem();
                        item.setServiceId(svc.getServiceId());
                        item.setStakeholderId(svc.getStakeholderId());
                        BigDecimal unitPrice = svc.getBasePrice() != null ? svc.getBasePrice() : BigDecimal.ZERO;
                        int qty = "PER_PERSON".equalsIgnoreCase(svc.getPricingUnit()) ? pax : 1;
                        item.setUnitPrice(unitPrice);
                        item.setQuantity(qty);
                        item.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(qty)));
                        item.setStatus("PENDING");
                        items.add(item);
                    }
                }
            }
        }

        int newId = bookingService.createBooking(b, items);
        if (newId > 0) {
            auditService.log(user.getUserId(), user.getUsername(), "CREATE_BOOKING", "BOOKINGS", newId, clientIp, "New booking created: " + b.getBookingReference());
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("bookingId", newId);
            res.put("reference", b.getBookingReference());
            res.put("assignedServiceItems", items.size());
            res.put("message", "Booking submitted successfully! Reference: " + b.getBookingReference());
            return JsonUtil.toJson(res);
        }
        return error("Booking creation failed.", 500);
    }

    private String handleUpdateBookingStatus(String body, User user, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        int bookingId = ((Number) d.get("bookingId")).intValue();
        String status = (String) d.get("status");

        Booking existing = bookingService.getBookingById(bookingId);
        if (existing == null) return error("Booking not found.", 404);

        if (isTourist(user)) {
            TouristProfile tp = bookingService.getTouristProfileByUserId(user.getUserId());
            if (tp == null || existing.getTouristId() != tp.getProfileId()) {
                return error("Access denied: You can only manage your own bookings.", 403);
            }
            if (!"CANCELLED".equalsIgnoreCase(status)) {
                return error("Access denied: Tourists may only cancel pending bookings.", 403);
            }
        } else if (isStakeholder(user)) {
            StakeholderProfile sp = stakeholderService.getProfileByUserId(user.getUserId());
            if (sp == null) return error("Stakeholder profile not found.", 400);
            boolean assignedToStakeholder = false;
            if (existing.getItems() != null) {
                for (BookingItem item : existing.getItems()) {
                    if (item.getStakeholderId() == sp.getProfileId()) {
                        assignedToStakeholder = true;
                        String itemStatus = "CONFIRMED".equalsIgnoreCase(status) ? "ACCEPTED" : status;
                        if ("PENDING".equalsIgnoreCase(itemStatus) || "ACCEPTED".equalsIgnoreCase(itemStatus)
                                || "REJECTED".equalsIgnoreCase(itemStatus) || "COMPLETED".equalsIgnoreCase(itemStatus)) {
                            bookingService.updateItemStatus(item.getItemId(), itemStatus);
                        }
                    }
                }
            }
            if (!assignedToStakeholder) {
                return error("Access denied: You can only manage bookings assigned to your services.", 403);
            }
        }

        boolean ok = bookingService.updateBookingStatus(bookingId, status);
        if (ok) {
            auditService.log(user.getUserId(), user.getUsername(), "UPDATE_BOOKING_STATUS", "BOOKINGS", bookingId, clientIp, "Booking status set to " + status);
            return success("Booking updated to " + status);
        }
        return error("Failed to update status.", 500);
    }

    private String handleUpdateBookingItemStatus(String body, User user, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        int itemId = ((Number) d.get("itemId")).intValue();
        String status = (String) d.get("status");

        boolean ok = bookingService.updateItemStatus(itemId, status);
        if (ok) {
            auditService.log(user.getUserId(), user.getUsername(), "UPDATE_ITEM_STATUS", "BOOKING_ITEMS", itemId, clientIp, "Item status set to " + status);
            return success("Service item updated to " + status);
        }
        return error("Failed to update item status.", 500);
    }

    private String handleGetReviews(Map<String, String> q) {
        if (q.containsKey("targetType") && q.containsKey("targetId")) {
            return JsonUtil.toJson(reviewService.getReviewsForTarget(q.get("targetType"), Integer.parseInt(q.get("targetId"))));
        }
        if (q.containsKey("bookingId")) {
            return JsonUtil.toJson(reviewService.getReviewsByBooking(Integer.parseInt(q.get("bookingId"))));
        }
        return JsonUtil.toJson(reviewService.getAllReviews());
    }

    private String handleCreateReview(String body, User user, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        TouristProfile tp = bookingService.getTouristProfileByUserId(user.getUserId());
        if (tp == null) return error("Only tourists can submit reviews.", 400);

        int bookingId = ((Number) d.get("bookingId")).intValue();
        Booking existingBooking = bookingService.getBookingById(bookingId);
        if (existingBooking == null || existingBooking.getTouristId() != tp.getProfileId()) {
            return error("Access denied: You can only submit reviews for your own bookings.", 403);
        }
        if (!"COMPLETED".equalsIgnoreCase(existingBooking.getStatus())) {
            return error("Reviews can only be submitted for completed bookings.", 400);
        }

        Review r = new Review();
        r.setBookingId(bookingId);
        r.setTouristId(tp.getProfileId());
        r.setTargetType((String) d.get("targetType"));
        r.setTargetId(((Number) d.get("targetId")).intValue());
        r.setRating(((Number) d.get("rating")).intValue());
        r.setComment((String) d.get("comment"));

        int id = reviewService.addReview(r);
        if (id > 0) {
            auditService.log(user.getUserId(), user.getUsername(), "CREATE_REVIEW", "REVIEWS", id, clientIp, "Rating: " + r.getRating() + " stars");
            return success("Review submitted! Thank you for supporting local community tourism.");
        }
        return error("Failed to submit review.", 500);
    }

    private String handleGetVerifications(Map<String, String> q, User user) {
        Integer brgy = user.getBarangayId();
        if (isAdmin(user) || isTourismOffice(user)) {
            brgy = q.containsKey("barangayId") ? Integer.valueOf(q.get("barangayId")) : null;
        }
        String status = q.get("status");
        return JsonUtil.toJson(verificationService.getVerifications(brgy, status));
    }

    private String handleProcessVerification(String body, User user, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        int stkId = ((Number) d.get("stakeholderId")).intValue();
        Integer userBrgyId = user.getBarangayId();
        int brgyId = (userBrgyId != null && "BARANGAY_OFFICIAL".equalsIgnoreCase(user.getRoleCode()))
                ? userBrgyId
                : ((Number) d.getOrDefault("barangayId", 1)).intValue();
        String status = (String) d.get("status");
        String reqs = (String) d.get("requirementsSubmitted");
        String remarks = (String) d.get("remarks");

        // Enforce Barangay jurisdiction boundary
        if ("BARANGAY_OFFICIAL".equalsIgnoreCase(user.getRoleCode())) {
            StakeholderProfile sp = stakeholderService.getProfileById(stkId);
            if (sp != null && userBrgyId != null && sp.getBarangayId() != userBrgyId.intValue()) {
                return error("Access denied: You can only verify stakeholders within your authorized barangay jurisdiction.", 403);
            }
        }

        boolean ok = verificationService.processVerification(stkId, brgyId, user.getUserId(), status, reqs, remarks);
        if (ok) {
            auditService.log(user.getUserId(), user.getUsername(), "VERIFY_STAKEHOLDER", "STAKEHOLDER_PROFILES", stkId, clientIp, "Decision: " + status + " | " + remarks);
            return success("Stakeholder verification processed successfully.");
        }
        return error("Verification processing failed.", 500);
    }

    private String handleGetAnnouncements(Map<String, String> q) {
        String cat = q.get("category");
        Integer brgy = q.containsKey("barangayId") ? Integer.valueOf(q.get("barangayId")) : null;
        String status = q.getOrDefault("status", "PUBLISHED");
        return JsonUtil.toJson(announcementService.getAnnouncements(cat, brgy, status));
    }

    private String handleSaveAnnouncement(String body, User user, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        Announcement a = new Announcement();
        if (d.get("announcementId") instanceof Number num) a.setAnnouncementId(num.intValue());
        a.setTitle((String) d.get("title"));
        a.setContent((String) d.get("content"));

        String rawCat = (String) d.get("category");
        if (rawCat == null || rawCat.isBlank()) rawCat = (String) d.get("type");
        a.setCategory(normalizeAnnouncementCategory(rawCat));

        if (d.get("targetBarangayId") instanceof Number num && num.intValue() > 0) {
            a.setTargetBarangayId(num.intValue());
        }
        a.setPublishedBy(user.getUserId());
        a.setPublishDate(Date.valueOf((String) d.getOrDefault("publishDate", DateUtil.nowDate())));
        if (d.get("expiryDate") instanceof String exp && !exp.isBlank()) {
            a.setExpiryDate(Date.valueOf(exp));
        }
        a.setStatus((String) d.getOrDefault("status", "PUBLISHED"));

        boolean ok;
        if (a.getAnnouncementId() > 0) {
            ok = announcementService.updateAnnouncement(a);
            auditService.log(user.getUserId(), user.getUsername(), "UPDATE_ANNOUNCEMENT", "ANNOUNCEMENTS", a.getAnnouncementId(), clientIp, "Updated: " + a.getTitle());
        } else {
            int newId = announcementService.createAnnouncement(a);
            ok = newId > 0;
            auditService.log(user.getUserId(), user.getUsername(), "CREATE_ANNOUNCEMENT", "ANNOUNCEMENTS", newId, clientIp, "Created: " + a.getTitle());
        }
        return ok ? success("Announcement saved.") : error("Failed to save announcement.", 500);
    }

    private String normalizeAnnouncementCategory(String raw) {
        if (raw == null || raw.isBlank()) return "ANNOUNCEMENT";
        String upper = raw.trim().toUpperCase();
        return switch (upper) {
            case "EVENT" -> "EVENT";
            case "FESTIVAL" -> "FESTIVAL";
            case "TRAVEL_GUIDELINE", "WEATHER_ADVISORY", "ECO_GUIDELINE" -> "TRAVEL_GUIDELINE";
            default -> "ANNOUNCEMENT";
        };
    }

    private String handleDeleteAnnouncement(String body, User user, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        int aId = ((Number) d.get("announcementId")).intValue();
        boolean ok = announcementService.deleteAnnouncement(aId);
        if (ok) {
            auditService.log(user.getUserId(), user.getUsername(), "DELETE_ANNOUNCEMENT", "ANNOUNCEMENTS", aId, clientIp, "Deleted announcement ID: " + aId);
            return success("Announcement deleted.");
        }
        return error("Delete failed.", 500);
    }

    private String handleRecordArrival(String body, User user, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        ArrivalRecord ar = new ArrivalRecord();
        ar.setDestinationId(((Number) d.get("destinationId")).intValue());
        ar.setLogDate(Date.valueOf((String) d.get("logDate")));
        ar.setLocalTouristsCount(((Number) d.getOrDefault("localTouristsCount", 0)).intValue());
        ar.setDomesticTouristsCount(((Number) d.getOrDefault("domesticTouristsCount", 0)).intValue());
        ar.setForeignTouristsCount(((Number) d.getOrDefault("foreignTouristsCount", 0)).intValue());
        ar.setTotalRevenue(BigDecimal.valueOf(((Number) d.getOrDefault("totalRevenue", 0.0)).doubleValue()));
        ar.setRecordedBy(user.getUserId());

        int id = reportService.recordArrival(ar);
        if (id > 0) {
            auditService.log(user.getUserId(), user.getUsername(), "LOG_ARRIVAL", "ARRIVAL_LOGS", id, clientIp, "Visitors recorded for destination ID: " + ar.getDestinationId());
            return success("Tourist arrival log recorded successfully.");
        }
        return error("Failed to record arrival.", 500);
    }

    private String handleCreateUser(String body, User admin, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        User u = new User();
        u.setUsername((String) d.get("username"));
        u.setEmail((String) d.get("email"));
        u.setFullName((String) d.get("fullName"));
        u.setPhone((String) d.get("phone"));
        u.setRoleId(((Number) d.get("roleId")).intValue());
        if (d.get("barangayId") instanceof Number num) u.setBarangayId(num.intValue());
        u.setStatus((String) d.getOrDefault("status", "ACTIVE"));
        String rawPassword = (String) d.getOrDefault("password", "password123");

        int id = userService.createUser(u, rawPassword);
        if (id > 0) {
            auditService.log(admin.getUserId(), admin.getUsername(), "CREATE_USER", "USERS", id, clientIp, "Created user: " + u.getUsername() + " (Role: " + u.getRoleId() + ")");
            return success("User created successfully with default password.");
        }
        return error("User creation failed.", 500);
    }

    private String handleUpdateUser(String body, User admin, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        if (!(d.get("userId") instanceof Number num)) return error("userId is required.", 400);
        int userId = num.intValue();
        User existing = userService.getUserById(userId);
        if (existing == null) return error("User not found.", 404);

        if (d.get("fullName") != null) existing.setFullName((String) d.get("fullName"));
        if (d.get("phone") != null) existing.setPhone((String) d.get("phone"));
        if (d.get("roleId") instanceof Number rNum) existing.setRoleId(rNum.intValue());
        if (d.containsKey("barangayId")) {
            Object bObj = d.get("barangayId");
            if (bObj instanceof Number bNum && bNum.intValue() > 0) existing.setBarangayId(bNum.intValue());
            else existing.setBarangayId(null);
        }
        if (d.get("status") != null) existing.setStatus((String) d.get("status"));

        boolean ok = userService.updateUser(existing);
        if (ok) {
            if (!"ACTIVE".equalsIgnoreCase(existing.getStatus())) {
                SessionManager.invalidateUserSessions(userId);
            }
            auditService.log(admin.getUserId(), admin.getUsername(), "UPDATE_USER", "USERS", userId, clientIp, "Updated user account: " + existing.getUsername());
            return success("User account updated successfully.");
        }
        return error("Failed to update user account.", 500);
    }

    private String handleUpdateUserStatus(String body, User admin, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        int userId = ((Number) d.get("userId")).intValue();
        String status = (String) d.get("status");

        boolean ok = userService.updateStatus(userId, status);
        if (ok) {
            if (!"ACTIVE".equalsIgnoreCase(status)) {
                SessionManager.invalidateUserSessions(userId);
            }
            auditService.log(admin.getUserId(), admin.getUsername(), "UPDATE_USER_STATUS", "USERS", userId, clientIp, "Set status to: " + status);
            return success("User status updated to " + status);
        }
        return error("Failed to update status.", 500);
    }

    private String handleResetUserPassword(String body, User admin, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        int userId = ((Number) d.get("userId")).intValue();
        String newPassword = (String) d.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            return error("New password must be at least 6 characters.", 400);
        }

        boolean ok = userService.resetPassword(userId, newPassword);
        if (ok) {
            auditService.log(admin.getUserId(), admin.getUsername(), "RESET_PASSWORD", "USERS", userId, clientIp, "Password reset by admin");
            return success("Password reset successfully.");
        }
        return error("Password reset failed.", 500);
    }

    private static final File SETTINGS_FILE = new File("backups/system_settings.properties");
    private static volatile boolean settingsTableChecked = false;

    private synchronized void ensureSettingsTable(Map<String, Object> seedDefaults) {
        if (settingsTableChecked) return;
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS system_settings (" +
                    "  setting_key VARCHAR(64) PRIMARY KEY," +
                    "  setting_value TEXT NOT NULL," +
                    "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB"
            );
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM system_settings")) {
                if (rs.next() && rs.getInt(1) == 0 && seedDefaults != null) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT IGNORE INTO system_settings (setting_key, setting_value) VALUES (?, ?)")) {
                        for (Map.Entry<String, Object> entry : seedDefaults.entrySet()) {
                            ps.setString(1, entry.getKey());
                            ps.setString(2, String.valueOf(entry.getValue()));
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }
            }
            settingsTableChecked = true;
        } catch (Exception ignored) {
        }
    }

    private String normalizeCurrency(String raw) {
        if (raw == null || raw.isBlank()) return "PHP (Philippine Peso \u20B1)";
        String trimmed = raw.trim();
        String upper = trimmed.toUpperCase();
        if (upper.startsWith("PHP")) return "PHP (Philippine Peso \u20B1)";
        if (upper.startsWith("USD")) return "USD (US Dollar $)";
        if (upper.startsWith("EUR")) return "EUR (Euro \u20AC)";
        if (upper.startsWith("JPY")) return "JPY (Japanese Yen \u00A5)";
        if (upper.startsWith("SGD")) return "SGD (Singapore Dollar S$)";
        if (upper.startsWith("AUD")) return "AUD (Australian Dollar A$)";
        return trimmed;
    }

    private Map<String, Object> loadCurrentSettingsMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("appName", "Lakbay Lokal - Biliran Tourism Information System");
        map.put("province", "Biliran Province, Region VIII, Philippines");
        map.put("capacityThreshold", 85);
        map.put("currency", "PHP (Philippine Peso \u20B1)");
        map.put("broadcastMsg", "Welcome to Lakbay Lokal! Experience Sambawan Island, Tinago Falls, and community-guided circuits in Biliran Province.");

        if (SETTINGS_FILE.exists()) {
            try (java.io.InputStreamReader reader = new java.io.InputStreamReader(
                    new java.io.FileInputStream(SETTINGS_FILE), java.nio.charset.StandardCharsets.UTF_8)) {
                Properties props = new Properties();
                props.load(reader);
                if (props.getProperty("appName") != null) map.put("appName", props.getProperty("appName"));
                if (props.getProperty("province") != null) map.put("province", props.getProperty("province"));
                if (props.getProperty("capacityThreshold") != null) {
                    try {
                        map.put("capacityThreshold", Integer.parseInt(props.getProperty("capacityThreshold").trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (props.getProperty("currency") != null) map.put("currency", normalizeCurrency(props.getProperty("currency")));
                if (props.getProperty("broadcastMsg") != null) map.put("broadcastMsg", props.getProperty("broadcastMsg"));
            } catch (Exception ignored) {
            }
        }

        ensureSettingsTable(map);
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT setting_key, setting_value FROM system_settings")) {
            while (rs.next()) {
                String k = rs.getString("setting_key");
                String v = rs.getString("setting_value");
                if (k == null || v == null) continue;
                if ("capacityThreshold".equals(k)) {
                    try {
                        map.put(k, Integer.parseInt(v.trim()));
                    } catch (NumberFormatException ignored) {
                    }
                } else if ("currency".equals(k)) {
                    map.put(k, normalizeCurrency(v));
                } else if (map.containsKey(k)) {
                    map.put(k, v);
                }
            }
        } catch (Exception ignored) {
        }

        return map;
    }

    private String handleGetSettings() {
        return JsonUtil.toJson(loadCurrentSettingsMap());
    }

    private String handleSaveSettings(String body, User admin, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        try {
            Map<String, Object> prev = loadCurrentSettingsMap();

            String prevAppName = String.valueOf(prev.get("appName"));
            String prevProvince = String.valueOf(prev.get("province"));
            int prevThreshold = (prev.get("capacityThreshold") instanceof Number n) ? n.intValue() : Integer.parseInt(String.valueOf(prev.get("capacityThreshold")));
            String prevCurrency = String.valueOf(prev.get("currency"));
            String prevBroadcast = String.valueOf(prev.get("broadcastMsg"));

            String newAppName = d.get("appName") != null ? String.valueOf(d.get("appName")).trim() : prevAppName;
            if (newAppName.isEmpty()) newAppName = prevAppName;

            String newProvince = d.get("province") != null ? String.valueOf(d.get("province")).trim() : prevProvince;
            if (newProvince.isEmpty()) newProvince = prevProvince;

            int newThreshold = prevThreshold;
            if (d.get("capacityThreshold") != null) {
                String rawThresh = String.valueOf(d.get("capacityThreshold")).replaceAll("[^0-9]", "");
                if (!rawThresh.isEmpty()) {
                    newThreshold = Math.max(50, Math.min(100, Integer.parseInt(rawThresh)));
                }
            }

            String newCurrency = d.get("currency") != null ? normalizeCurrency(String.valueOf(d.get("currency"))) : prevCurrency;
            if (newCurrency.isEmpty()) newCurrency = prevCurrency;

            String newBroadcast = d.get("broadcastMsg") != null ? String.valueOf(d.get("broadcastMsg")).trim() : prevBroadcast;

            // Determine which setting(s) changed
            List<String> changedKeys = new ArrayList<>();
            if (!Objects.equals(prevAppName, newAppName)) changedKeys.add("appName");
            if (!Objects.equals(prevProvince, newProvince)) changedKeys.add("province");
            if (prevThreshold != newThreshold) changedKeys.add("capacityThreshold");
            if (!Objects.equals(prevCurrency, newCurrency)) changedKeys.add("currency");
            if (!Objects.equals(prevBroadcast, newBroadcast)) changedKeys.add("broadcastMsg");

            if (changedKeys.isEmpty() && d.get("updatedFields") instanceof List<?> ufList) {
                for (Object item : ufList) {
                    if (item != null) {
                        String k = String.valueOf(item).trim();
                        if (!k.isEmpty() && !changedKeys.contains(k)) changedKeys.add(k);
                    }
                }
            }

            Map<String, String> keyLabels = new LinkedHashMap<>();
            keyLabels.put("appName", "Platform Name");
            keyLabels.put("province", "Provincial Jurisdiction");
            keyLabels.put("capacityThreshold", "Carrying Capacity Alert Threshold");
            keyLabels.put("currency", "Default Currency");
            keyLabels.put("broadcastMsg", "Live Broadcast Notice");

            List<String> changedLabels = new ArrayList<>();
            for (String k : changedKeys) {
                if (keyLabels.containsKey(k)) changedLabels.add(keyLabels.get(k));
            }

            // Persist to properties file
            File parent = SETTINGS_FILE.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            Properties props = new Properties();
            props.setProperty("appName", newAppName);
            props.setProperty("province", newProvince);
            props.setProperty("capacityThreshold", String.valueOf(newThreshold));
            props.setProperty("currency", newCurrency);
            props.setProperty("broadcastMsg", newBroadcast);
            try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(SETTINGS_FILE), java.nio.charset.StandardCharsets.UTF_8)) {
                props.store(writer, "Lakbay Lokal System Settings");
            }

            // Persist to MySQL system_settings table
            Map<String, Object> savedSettings = new LinkedHashMap<>();
            savedSettings.put("appName", newAppName);
            savedSettings.put("province", newProvince);
            savedSettings.put("capacityThreshold", newThreshold);
            savedSettings.put("currency", newCurrency);
            savedSettings.put("broadcastMsg", newBroadcast);

            ensureSettingsTable(savedSettings);
            try (Connection conn = ConnectionFactory.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO system_settings (setting_key, setting_value) VALUES (?, ?) " +
                         "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)")) {
                for (Map.Entry<String, Object> entry : savedSettings.entrySet()) {
                    ps.setString(1, entry.getKey());
                    ps.setString(2, String.valueOf(entry.getValue()));
                    ps.addBatch();
                }
                ps.executeBatch();
            } catch (Exception ignored) {
            }

            // Build setting-specific notification message
            String message;
            if (changedKeys.size() == 1) {
                String k = changedKeys.get(0);
                message = switch (k) {
                    case "appName" -> "Platform Name saved successfully: \"" + newAppName + "\".";
                    case "province" -> "Provincial Jurisdiction saved successfully: \"" + newProvince + "\".";
                    case "capacityThreshold" -> "Carrying Capacity Alert Threshold saved (" + newThreshold + "%).";
                    case "currency" -> "Default Currency saved: " + newCurrency + ".";
                    case "broadcastMsg" -> "Live Broadcast Notice saved successfully.";
                    default -> "System setting saved successfully.";
                };
            } else if (changedLabels.size() > 1) {
                message = "System settings saved: " + String.join(", ", changedLabels) + ".";
            } else {
                message = "System settings saved (Platform Name, Provincial Jurisdiction, Carrying Capacity, Default Currency, Live Broadcast Notice).";
            }

            auditService.log(admin.getUserId(), admin.getUsername(), "UPDATE_SETTINGS", "SETTINGS", 1, clientIp, message);

            Map<String, Object> res = new LinkedHashMap<>();
            res.put("success", true);
            res.put("message", message);
            res.put("changedSettings", changedLabels);
            res.put("settings", savedSettings);
            return JsonUtil.toJson(res);
        } catch (Exception e) {
            return error("Failed saving settings: " + e.getMessage(), 500);
        }
    }

    private boolean isAdmin(User user) {
        return user != null && "SYSTEM_ADMIN".equalsIgnoreCase(user.getRoleCode());
    }

    private boolean isTourismOffice(User user) {
        return user != null && ("TOURISM_OFFICE".equalsIgnoreCase(user.getRoleCode()) || isAdmin(user));
    }

    private boolean isLguOrHigher(User user) {
        return user != null && ("LGU_OFFICER".equalsIgnoreCase(user.getRoleCode()) || isTourismOffice(user));
    }

    private boolean isBarangayOrHigher(User user) {
        return user != null && ("BARANGAY_OFFICIAL".equalsIgnoreCase(user.getRoleCode()) || isLguOrHigher(user));
    }

    private boolean isStakeholder(User user) {
        return user != null && "STAKEHOLDER".equalsIgnoreCase(user.getRoleCode());
    }

    public boolean isTourist(User user) {
        return user != null && "TOURIST".equalsIgnoreCase(user.getRoleCode());
    }

    private String success(String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("message", msg);
        return JsonUtil.toJson(map);
    }

    private String error(String msg, int code) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        map.put("message", msg);
        map.put("code", code);
        return JsonUtil.toJson(map);
    }
}
