package com.lakbaylokal.controller;

import com.lakbaylokal.database.BackupRestoreService;
import com.lakbaylokal.model.*;
import com.lakbaylokal.service.*;
import com.lakbaylokal.util.DateUtil;
import com.lakbaylokal.util.JsonUtil;
import com.lakbaylokal.util.SessionManager;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
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
                return JsonUtil.toJson(stakeholderService.getAllAvailableServices(cat));
            }
            if ("/api/stakeholders".equals(path) && "GET".equalsIgnoreCase(method)) {
                return handleGetStakeholders(queryParams);
            }
            if ("/api/reviews".equals(path) && "GET".equalsIgnoreCase(method)) {
                return handleGetReviews(queryParams);
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

            // Stakeholders Management (Stakeholders & Admin)
            if ("/api/stakeholders".equals(path) && "GET".equalsIgnoreCase(method)) {
                return handleGetStakeholders(queryParams);
            }
            if ("/api/stakeholders/services".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isStakeholder(currentUser) && !isAdmin(currentUser)) return error("Access denied. Stakeholders only.", 403);
                return handleSaveStakeholderService(body, currentUser, clientIp);
            }
            if ("/api/stakeholders/services/delete".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isStakeholder(currentUser) && !isAdmin(currentUser)) return error("Access denied. Stakeholders only.", 403);
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
                if (!isBarangayOrHigher(currentUser) && !isStakeholder(currentUser)) return error("Access denied.", 403);
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
                return handleRecordArrival(body, currentUser, clientIp);
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
        dest.setOperatingHours((String) d.get("operatingHours"));
        dest.setLocationCoordinates((String) d.get("locationCoordinates"));
        dest.setContactInfo((String) d.get("contactInfo"));
        dest.setImageUrl((String) d.get("imageUrl"));
        dest.setStatus((String) d.getOrDefault("status", "ACTIVE"));

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

    private String handleGetStakeholders(Map<String, String> q) {
        String cat = q.get("category");
        Integer brgy = q.containsKey("barangayId") ? Integer.valueOf(q.get("barangayId")) : null;
        String status = q.get("verificationStatus");
        return JsonUtil.toJson(stakeholderService.getStakeholders(cat, brgy, status));
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
        } else if (isAdmin(user)) {
            if (d.get("stakeholderId") instanceof Number num) {
                svc.setStakeholderId(num.intValue());
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
        svc.setPricingUnit((String) d.getOrDefault("pricingUnit", "PER_PERSON"));
        if (d.get("maxCapacity") instanceof Number num) svc.setMaxCapacity(num.intValue());
        svc.setAvailable(Boolean.TRUE.equals(d.getOrDefault("isAvailable", true)));

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
        } else if (!isAdmin(user)) {
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

        int newId = bookingService.createBooking(b, items);
        if (newId > 0) {
            auditService.log(user.getUserId(), user.getUsername(), "CREATE_BOOKING", "BOOKINGS", newId, clientIp, "New booking created: " + b.getBookingReference());
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("bookingId", newId);
            res.put("reference", b.getBookingReference());
            res.put("message", "Booking submitted successfully! Reference: " + b.getBookingReference());
            return JsonUtil.toJson(res);
        }
        return error("Booking creation failed.", 500);
    }

    private String handleUpdateBookingStatus(String body, User user, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        int bookingId = ((Number) d.get("bookingId")).intValue();
        String status = (String) d.get("status");

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

        Review r = new Review();
        r.setBookingId(((Number) d.get("bookingId")).intValue());
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
        a.setCategory((String) d.get("category"));
        if (d.get("targetBarangayId") instanceof Number num) a.setTargetBarangayId(num.intValue());
        a.setPublishedBy(user.getUserId());
        a.setPublishDate(Date.valueOf((String) d.getOrDefault("publishDate", DateUtil.nowDate())));
        if (d.get("expiryDate") != null) a.setExpiryDate(Date.valueOf((String) d.get("expiryDate")));
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

    private String handleUpdateUserStatus(String body, User admin, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        int userId = ((Number) d.get("userId")).intValue();
        String status = (String) d.get("status");

        boolean ok = userService.updateStatus(userId, status);
        if (ok) {
            auditService.log(admin.getUserId(), admin.getUsername(), "UPDATE_USER_STATUS", "USERS", userId, clientIp, "Set status to: " + status);
            return success("User status updated to " + status);
        }
        return error("Failed to update status.", 500);
    }

    private String handleResetUserPassword(String body, User admin, String clientIp) {
        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        int userId = ((Number) d.get("userId")).intValue();
        String newPassword = (String) d.get("newPassword");

        boolean ok = userService.resetPassword(userId, newPassword);
        if (ok) {
            auditService.log(admin.getUserId(), admin.getUsername(), "RESET_PASSWORD", "USERS", userId, clientIp, "Password reset by admin");
            return success("Password reset successfully.");
        }
        return error("Password reset failed.", 500);
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
