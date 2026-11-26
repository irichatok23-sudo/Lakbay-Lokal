const fs = require('fs');

let code = fs.readFileSync('backup_phase2/ApiController_before_stage2_1.java.bak', 'utf8');

// 1. Replace public /api/announcements GET router
const oldPublicAnno = `            if ("/api/announcements".equals(path) && "GET".equalsIgnoreCase(method)) {
                return handleGetAnnouncements(queryParams);
            }`;

const newPublicAnno = `            if ("/api/announcements".equals(path) && "GET".equalsIgnoreCase(method)) {
                return handleGetAnnouncements(queryParams, authToken);
            }
            if ("/api/announcements/stats".equals(path) && "GET".equalsIgnoreCase(method)) {
                User maybeUser = SessionManager.getUser(authToken);
                if (maybeUser == null) return error("Unauthorized. Please log in.", 401);
                return handleGetAnnouncementStats(maybeUser);
            }
            if (path.startsWith("/api/announcements/") && "GET".equalsIgnoreCase(method)) {
                String sub = path.substring("/api/announcements/".length());
                try {
                    int aId = Integer.parseInt(sub);
                    Map<String, String> q = new HashMap<>(queryParams);
                    q.put("id", String.valueOf(aId));
                    return handleGetAnnouncements(q, authToken);
                } catch (NumberFormatException ignored) {}
            }`;

if (!code.includes(oldPublicAnno)) throw new Error('oldPublicAnno not found');
code = code.replace(oldPublicAnno, newPublicAnno);

// 2. Replace authenticated announcements management router
const oldAuthAnno = `            // Announcements (Tourism Office, LGU, Admin)
            if ("/api/announcements".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isLguOrHigher(currentUser)) return error("Access denied. Tourism Office, LGU, or Admin only.", 403);
                return handleSaveAnnouncement(body, currentUser, clientIp);
            }
            if ("/api/announcements/delete".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isLguOrHigher(currentUser)) return error("Access denied. Tourism Office, LGU, or Admin only.", 403);
                return handleDeleteAnnouncement(body, currentUser, clientIp);
            }`;

const newAuthAnno = `            // Announcements (Official Tourism Office Announcement Management)
            if ("/api/announcements".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isTourismOfficeOnly(currentUser)) return error("Access denied. Only Tourism Office officers can create official announcements.", 403);
                return handleSaveAnnouncement(body, currentUser, clientIp);
            }
            if ("/api/announcements/update".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isTourismOfficeOnly(currentUser)) return error("Access denied. Only Tourism Office officers can edit official announcements.", 403);
                return handleSaveAnnouncement(body, currentUser, clientIp);
            }
            if ("/api/announcements".equals(path) && "PUT".equalsIgnoreCase(method)) {
                if (!isTourismOfficeOnly(currentUser)) return error("Access denied. Only Tourism Office officers can edit official announcements.", 403);
                return handleSaveAnnouncement(body, currentUser, clientIp);
            }
            if ("/api/announcements/publish".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isTourismOfficeOnly(currentUser)) return error("Access denied. Only Tourism Office officers can publish announcements.", 403);
                return handlePublishAnnouncement(body, currentUser, clientIp);
            }
            if ("/api/announcements/archive".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isTourismOfficeOnly(currentUser)) return error("Access denied. Only Tourism Office officers can archive announcements.", 403);
                return handleArchiveAnnouncement(body, currentUser, clientIp);
            }
            if ("/api/announcements/delete".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isTourismOfficeOnly(currentUser)) return error("Access denied. Only Tourism Office officers can delete announcements.", 403);
                return handleDeleteAnnouncement(body, currentUser, clientIp);
            }
            if ("/api/announcements/upload-image".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!isTourismOfficeOnly(currentUser)) return error("Access denied. Only Tourism Office officers can upload announcement images.", 403);
                return handleUploadAnnouncementImage(body, currentUser, clientIp);
            }`;

if (!code.includes(oldAuthAnno)) throw new Error('oldAuthAnno not found');
code = code.replace(oldAuthAnno, newAuthAnno);

// 3. Replace announcement handlers
const oldHandlersTarget = `    private String handleGetAnnouncements(Map<String, String> q) {
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
    }`;

const newHandlers = `    private String handleGetAnnouncements(Map<String, String> q, String authToken) {
        User user = SessionManager.getUser(authToken);
        String cat = q.get("category");
        String search = q.get("search");

        // Details by ID check
        if (q.containsKey("id") || q.containsKey("announcementId")) {
            String idStr = q.containsKey("id") ? q.get("id") : q.get("announcementId");
            try {
                int aId = Integer.parseInt(idStr);
                Announcement a = announcementService.getAnnouncementById(aId);
                if (a == null) return error("Announcement not found.", 404);
                if ((user == null || (!isTourismOfficeOnly(user) && !isAdmin(user))) && !"PUBLISHED".equalsIgnoreCase(a.getStatus())) {
                    return error("Access denied: Announcement is not published.", 403);
                }
                return JsonUtil.toJson(a);
            } catch (NumberFormatException e) {
                return error("Invalid announcement ID.", 400);
            }
        }

        // Scoped listing based on caller role
        if (user == null || isTourist(user) || isStakeholder(user)) {
            Integer brgy = q.containsKey("barangayId") && !q.get("barangayId").isBlank()
                    ? Integer.valueOf(q.get("barangayId")) : null;
            return JsonUtil.toJson(announcementService.getFilteredAnnouncements(cat, brgy, null, "PUBLISHED", search, true));
        } else if (isBarangayOfficial(user)) {
            Integer brgy = user.getBarangayId();
            return JsonUtil.toJson(announcementService.getFilteredAnnouncements(cat, brgy, null, "PUBLISHED", search, true));
        } else if (isLguOfficer(user)) {
            Integer muni = user.getMunicipalityId();
            return JsonUtil.toJson(announcementService.getFilteredAnnouncements(cat, null, muni, "PUBLISHED", search, true));
        } else {
            // Tourism Office & System Admin: Can view all statuses (DRAFT, PUBLISHED, ARCHIVED)
            String status = q.get("status");
            Integer brgy = q.containsKey("barangayId") && !q.get("barangayId").isBlank()
                    ? Integer.valueOf(q.get("barangayId")) : null;
            Integer muni = q.containsKey("municipalityId") && !q.get("municipalityId").isBlank()
                    ? Integer.valueOf(q.get("municipalityId")) : null;
            return JsonUtil.toJson(announcementService.getFilteredAnnouncements(cat, brgy, muni, status, search, false));
        }
    }

    private String handleGetAnnouncementStats(User user) {
        if (!isTourismOfficeOnly(user) && !isAdmin(user)) {
            return error("Access denied: Tourism Office or System Admin only.", 403);
        }
        return JsonUtil.toJson(announcementService.getStatistics());
    }

    private String handleSaveAnnouncement(String body, User user, String clientIp) {
        if (!isTourismOfficeOnly(user)) {
            return error("Access denied. Only Tourism Office officers can create or modify official tourism announcements.", 403);
        }

        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        String title = (String) d.get("title");
        String content = (String) d.get("content");

        if (title == null || title.trim().isEmpty()) {
            return error("Announcement title is required.", 400);
        }
        if (title.trim().length() > 160) {
            return error("Announcement title cannot exceed 160 characters.", 400);
        }
        if (content == null || content.trim().isEmpty()) {
            return error("Announcement content is required.", 400);
        }

        Announcement a = new Announcement();
        int existingId = 0;
        if (d.get("announcementId") instanceof Number num) existingId = num.intValue();
        else if (d.get("id") instanceof Number num) existingId = num.intValue();

        String prevStatus = null;
        if (existingId > 0) {
            Announcement existing = announcementService.getAnnouncementById(existingId);
            if (existing == null) {
                return error("Announcement not found.", 404);
            }
            prevStatus = existing.getStatus();
            a = existing;
        }

        a.setTitle(title.trim());
        a.setContent(content.trim());

        String rawCat = (String) d.get("category");
        if (rawCat == null || rawCat.isBlank()) rawCat = (String) d.get("type");
        a.setCategory(normalizeAnnouncementCategory(rawCat));

        if (d.containsKey("targetBarangayId")) {
            Object tb = d.get("targetBarangayId");
            if (tb instanceof Number num && num.intValue() > 0) {
                a.setTargetBarangayId(num.intValue());
            } else {
                a.setTargetBarangayId(null);
            }
        }

        if (d.containsKey("imageUrl")) {
            String img = (String) d.get("imageUrl");
            a.setImageUrl(img != null && !img.isBlank() ? img.trim() : null);
        }

        String reqStatus = (String) d.get("status");
        if (reqStatus == null || reqStatus.isBlank()) {
            reqStatus = existingId > 0 && prevStatus != null ? prevStatus : "DRAFT";
        }
        reqStatus = reqStatus.trim().toUpperCase();
        if (!"DRAFT".equals(reqStatus) && !"PUBLISHED".equals(reqStatus) && !"ARCHIVED".equals(reqStatus)) {
            return error("Invalid announcement status. Allowed: DRAFT, PUBLISHED, ARCHIVED.", 400);
        }
        a.setStatus(reqStatus);

        if (d.containsKey("publishDate") && d.get("publishDate") != null && !((String) d.get("publishDate")).isBlank()) {
            try {
                a.setPublishDate(Date.valueOf((String) d.get("publishDate")));
            } catch (Exception e) {
                return error("Invalid publication date format (expected YYYY-MM-DD).", 400);
            }
        } else if ("PUBLISHED".equals(reqStatus) && a.getPublishDate() == null) {
            a.setPublishDate(Date.valueOf(DateUtil.nowDate()));
        }

        if (d.containsKey("expiryDate") && d.get("expiryDate") != null && !((String) d.get("expiryDate")).isBlank()) {
            try {
                a.setExpiryDate(Date.valueOf((String) d.get("expiryDate")));
            } catch (Exception e) {
                return error("Invalid expiry date format (expected YYYY-MM-DD).", 400);
            }
        } else if (d.containsKey("expiryDate") && (d.get("expiryDate") == null || ((String) d.get("expiryDate")).isBlank())) {
            a.setExpiryDate(null);
        }

        if (a.getPublishDate() != null && a.getExpiryDate() != null) {
            if (a.getExpiryDate().before(a.getPublishDate())) {
                return error("Expiration date cannot be earlier than publication date.", 400);
            }
        }

        a.setPublishedBy(user.getUserId());

        if (existingId > 0) {
            boolean ok = announcementService.updateAnnouncement(a);
            if (ok) {
                auditService.log(user.getUserId(), user.getUsername(), "ANNOUNCEMENT_UPDATE", "ANNOUNCEMENTS", existingId, clientIp,
                        "Updated announcement: " + a.getTitle() + " | Previous status: " + prevStatus + " | New status: " + a.getStatus());
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("success", true);
                resp.put("message", "Announcement updated successfully.");
                resp.put("announcement", a);
                return JsonUtil.toJson(resp);
            }
            return error("Failed to update announcement.", 500);
        } else {
            int newId = announcementService.createAnnouncement(a);
            if (newId > 0) {
                a.setAnnouncementId(newId);
                auditService.log(user.getUserId(), user.getUsername(), "ANNOUNCEMENT_CREATE", "ANNOUNCEMENTS", newId, clientIp,
                        "Created " + a.getStatus() + " announcement: " + a.getTitle());
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("success", true);
                resp.put("message", "DRAFT".equals(a.getStatus()) ? "Announcement saved as draft." : "Announcement published successfully.");
                resp.put("announcementId", newId);
                resp.put("announcement", a);
                return JsonUtil.toJson(resp);
            }
            return error("Failed to create announcement.", 500);
        }
    }

    private String handlePublishAnnouncement(String body, User user, String clientIp) {
        if (!isTourismOfficeOnly(user)) {
            return error("Access denied. Only Tourism Office officers can publish announcements.", 403);
        }

        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        int aId = d.get("announcementId") instanceof Number n ? n.intValue() : 0;
        if (aId <= 0 && d.get("id") instanceof Number n) aId = n.intValue();
        if (aId <= 0) return error("announcementId is required.", 400);

        Announcement a = announcementService.getAnnouncementById(aId);
        if (a == null) return error("Announcement not found.", 404);

        String prevStatus = a.getStatus();
        Date pubDate = Date.valueOf(DateUtil.nowDate());
        if (d.get("publishDate") instanceof String pStr && !pStr.isBlank()) {
            try { pubDate = Date.valueOf(pStr); } catch (Exception ignored) {}
        }

        boolean ok = announcementService.publishAnnouncement(aId, pubDate);
        if (ok) {
            auditService.log(user.getUserId(), user.getUsername(), "ANNOUNCEMENT_PUBLISH", "ANNOUNCEMENTS", aId, clientIp,
                    "Published announcement: " + a.getTitle() + " | Previous status: " + prevStatus + " | New status: PUBLISHED");
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("message", "Announcement published successfully.");
            return JsonUtil.toJson(resp);
        }
        return error("Failed to publish announcement.", 500);
    }

    private String handleArchiveAnnouncement(String body, User user, String clientIp) {
        if (!isTourismOfficeOnly(user)) {
            return error("Access denied. Only Tourism Office officers can archive announcements.", 403);
        }

        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        int aId = d.get("announcementId") instanceof Number n ? n.intValue() : 0;
        if (aId <= 0 && d.get("id") instanceof Number n) aId = n.intValue();
        if (aId <= 0) return error("announcementId is required.", 400);

        Announcement a = announcementService.getAnnouncementById(aId);
        if (a == null) return error("Announcement not found.", 404);

        String prevStatus = a.getStatus();
        boolean ok = announcementService.archiveAnnouncement(aId);
        if (ok) {
            auditService.log(user.getUserId(), user.getUsername(), "ANNOUNCEMENT_ARCHIVE", "ANNOUNCEMENTS", aId, clientIp,
                    "Archived announcement: " + a.getTitle() + " | Previous status: " + prevStatus + " | New status: ARCHIVED");
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("message", "Announcement archived successfully.");
            return JsonUtil.toJson(resp);
        }
        return error("Failed to archive announcement.", 500);
    }

    private String handleDeleteAnnouncement(String body, User user, String clientIp) {
        if (!isTourismOfficeOnly(user)) {
            return error("Access denied. Only Tourism Office officers can delete announcements.", 403);
        }

        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        int aId = d.get("announcementId") instanceof Number n ? n.intValue() : 0;
        if (aId <= 0 && d.get("id") instanceof Number n) aId = n.intValue();
        if (aId <= 0) return error("announcementId is required.", 400);

        Announcement a = announcementService.getAnnouncementById(aId);
        if (a == null) return error("Announcement not found.", 404);

        boolean ok = announcementService.deleteAnnouncement(aId);
        if (ok) {
            auditService.log(user.getUserId(), user.getUsername(), "DELETE_ANNOUNCEMENT", "ANNOUNCEMENTS", aId, clientIp,
                    "Deleted announcement: " + a.getTitle() + " (ID: " + aId + ")");
            return success("Announcement deleted successfully.");
        }
        return error("Delete failed.", 500);
    }

    private String handleUploadAnnouncementImage(String body, User user, String clientIp) {
        if (!isTourismOfficeOnly(user)) {
            return error("Access denied. Only Tourism Office officers can upload announcement images.", 403);
        }

        Map<String, Object> d = JsonUtil.parseJsonObject(body);
        String fileName = (String) d.get("fileName");
        String base64Data = (String) d.get("base64Data");

        if (fileName == null || fileName.isBlank() || base64Data == null || base64Data.isBlank()) {
            return error("fileName and base64Data are required.", 400);
        }

        String cleanName = new File(fileName).getName().toLowerCase();
        String ext = "";
        int dot = cleanName.lastIndexOf('.');
        if (dot >= 0) ext = cleanName.substring(dot);
        if (!".jpg".equals(ext) && !".jpeg".equals(ext) && !".png".equals(ext) && !".webp".equals(ext)) {
            return error("Invalid image format. Allowed: JPG, JPEG, PNG, WEBP.", 400);
        }

        if (base64Data.contains(",")) {
            base64Data = base64Data.substring(base64Data.indexOf(',') + 1);
        }

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            return error("Invalid base64 image data.", 400);
        }

        if (bytes.length > 5 * 1024 * 1024) {
            return error("Image file size exceeds 5MB limit.", 400);
        }

        String safeFileName = "announcement_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

        File[] targets = new File[] {
            new File("src/main/resources/web/assets/announcements", safeFileName),
            new File("bin/web/assets/announcements", safeFileName)
        };

        for (File target : targets) {
            File parent = target.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            try {
                java.nio.file.Files.write(target.toPath(), bytes);
            } catch (java.io.IOException e) {
                return error("Failed saving image: " + e.getMessage(), 500);
            }
        }

        String webPath = "/assets/announcements/" + safeFileName;
        auditService.log(user.getUserId(), user.getUsername(), "UPLOAD_ANNOUNCEMENT_IMAGE", "ANNOUNCEMENTS", 0, clientIp,
                "Uploaded poster: " + safeFileName);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Image uploaded successfully.");
        resp.put("imageUrl", webPath);
        return JsonUtil.toJson(resp);
    }

    private String normalizeAnnouncementCategory(String raw) {
        if (raw == null || raw.isBlank()) return "ANNOUNCEMENT";
        String upper = raw.trim().toUpperCase();
        return switch (upper) {
            case "EVENT" -> "EVENT";
            case "FESTIVAL" -> "FESTIVAL";
            case "TRAVEL_GUIDELINE", "WEATHER_ADVISORY", "ECO_GUIDELINE" -> "TRAVEL_GUIDELINE";
            case "DESTINATION_CLOSURE", "CLOSURE" -> "DESTINATION_CLOSURE";
            case "ADVISORY" -> "ADVISORY";
            default -> "ANNOUNCEMENT";
        };
    }`;

if (!code.includes(oldHandlersTarget)) throw new Error('oldHandlersTarget not found');
code = code.replace(oldHandlersTarget, newHandlers);

// 4. Role helper methods at bottom: add isTourismOfficeOnly(User user)
const oldRoles = `    private boolean isAdmin(User user) {
        return user != null && "SYSTEM_ADMIN".equalsIgnoreCase(user.getRoleCode());
    }

    private boolean isTourismOffice(User user) {
        return user != null && ("TOURISM_OFFICE".equalsIgnoreCase(user.getRoleCode()) || isAdmin(user));
    }`;

const newRoles = `    private boolean isAdmin(User user) {
        return user != null && "SYSTEM_ADMIN".equalsIgnoreCase(user.getRoleCode());
    }

    private boolean isTourismOfficeOnly(User user) {
        return user != null && "TOURISM_OFFICE".equalsIgnoreCase(user.getRoleCode());
    }

    private boolean isTourismOffice(User user) {
        return user != null && ("TOURISM_OFFICE".equalsIgnoreCase(user.getRoleCode()) || isAdmin(user));
    }`;

if (!code.includes(oldRoles)) throw new Error('oldRoles not found');
code = code.replace(oldRoles, newRoles);

fs.writeFileSync('src/main/java/com/lakbaylokal/controller/ApiController.java', code, 'utf8');
console.log('Successfully wrote updated ApiController.java!');
