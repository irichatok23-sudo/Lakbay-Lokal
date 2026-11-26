package com.lakbaylokal.security;

import java.util.*;

/**
 * Maps the six Lakbay Lokal roles to their authorized permissions.
 * Serves as the trusted server-side authorization policy definition.
 */
public class RolePermissions {

    private static final Map<String, Set<Permission>> ROLE_PERMISSIONS = new HashMap<>();

    static {
        // 1. TOURIST (Role Code: TOURIST)
        Set<Permission> tourist = EnumSet.of(
                Permission.DESTINATION_VIEW,
                Permission.PACKAGE_VIEW,
                Permission.SERVICE_VIEW,
                Permission.STAKEHOLDER_VIEW,
                Permission.ANNOUNCEMENT_VIEW,
                Permission.REVIEW_VIEW,
                Permission.BOOKING_CREATE,
                Permission.BOOKING_VIEW_OWN,
                Permission.REVIEW_CREATE
        );
        ROLE_PERMISSIONS.put("TOURIST", Collections.unmodifiableSet(tourist));

        // 2. LOCAL TOURISM STAKEHOLDER (Role Code: STAKEHOLDER)
        Set<Permission> stakeholder = EnumSet.of(
                Permission.DESTINATION_VIEW,
                Permission.PACKAGE_VIEW,
                Permission.SERVICE_VIEW,
                Permission.STAKEHOLDER_VIEW,
                Permission.ANNOUNCEMENT_VIEW,
                Permission.REVIEW_VIEW,
                Permission.SERVICE_MANAGE_OWN,
                Permission.STAKEHOLDER_MANAGE_OWN,
                Permission.BOOKING_VIEW_OWN,
                Permission.BOOKING_UPDATE_STATUS
        );
        ROLE_PERMISSIONS.put("STAKEHOLDER", Collections.unmodifiableSet(stakeholder));

        // 3. BARANGAY OFFICIAL (Role Code: BARANGAY_OFFICIAL)
        Set<Permission> barangay = EnumSet.of(
                Permission.DESTINATION_VIEW,
                Permission.PACKAGE_VIEW,
                Permission.SERVICE_VIEW,
                Permission.STAKEHOLDER_VIEW,
                Permission.ANNOUNCEMENT_VIEW,
                Permission.REVIEW_VIEW,
                Permission.DESTINATION_VALIDATE,
                Permission.STAKEHOLDER_VERIFY,
                Permission.BOOKING_VIEW_JURISDICTION,
                Permission.REPORT_RECORD_ARRIVAL,
                Permission.REPORT_VIEW_BARANGAY
        );
        ROLE_PERMISSIONS.put("BARANGAY_OFFICIAL", Collections.unmodifiableSet(barangay));

        // 4. LGU OFFICER (Role Code: LGU_OFFICER)
        Set<Permission> lgu = EnumSet.of(
                Permission.DESTINATION_VIEW,
                Permission.PACKAGE_VIEW,
                Permission.SERVICE_VIEW,
                Permission.STAKEHOLDER_VIEW,
                Permission.ANNOUNCEMENT_VIEW,
                Permission.REVIEW_VIEW,
                Permission.BOOKING_VIEW_JURISDICTION,
                Permission.REPORT_RECORD_ARRIVAL,
                Permission.REPORT_VIEW_BARANGAY,
                Permission.REPORT_VIEW_MUNICIPALITY
        );
        ROLE_PERMISSIONS.put("LGU_OFFICER", Collections.unmodifiableSet(lgu));

        // 5. TOURISM OFFICE PERSONNEL (Role Code: TOURISM_OFFICE)
        Set<Permission> tourismOffice = EnumSet.of(
                Permission.DESTINATION_VIEW,
                Permission.DESTINATION_CREATE,
                Permission.DESTINATION_UPDATE,
                Permission.DESTINATION_DELETE,
                Permission.DESTINATION_VALIDATE,
                Permission.PACKAGE_VIEW,
                Permission.PACKAGE_CREATE,
                Permission.PACKAGE_UPDATE,
                Permission.PACKAGE_DELETE,
                Permission.SERVICE_VIEW,
                Permission.SERVICE_MANAGE_ALL,
                Permission.STAKEHOLDER_VIEW,
                Permission.BOOKING_VIEW_ALL,
                Permission.REVIEW_VIEW,
                Permission.REVIEW_MANAGE,
                Permission.ANNOUNCEMENT_VIEW,
                Permission.ANNOUNCEMENT_PUBLISH,
                Permission.ANNOUNCEMENT_DELETE,
                Permission.REPORT_RECORD_ARRIVAL,
                Permission.REPORT_VIEW_BARANGAY,
                Permission.REPORT_VIEW_MUNICIPALITY,
                Permission.REPORT_VIEW_PROVINCIAL
        );
        ROLE_PERMISSIONS.put("TOURISM_OFFICE", Collections.unmodifiableSet(tourismOffice));

        // 6. SYSTEM ADMINISTRATOR (Role Code: SYSTEM_ADMIN)
        // System admin receives all permissions including user management, audit logs, and backups
        Set<Permission> admin = EnumSet.allOf(Permission.class);
        ROLE_PERMISSIONS.put("SYSTEM_ADMIN", Collections.unmodifiableSet(admin));
    }

    /**
     * Checks if a role has the specified permission.
     */
    public static boolean hasPermission(String roleCode, Permission permission) {
        if (roleCode == null || permission == null) return false;
        Set<Permission> perms = ROLE_PERMISSIONS.get(roleCode.toUpperCase().trim());
        return perms != null && perms.contains(permission);
    }

    /**
     * Returns the set of permissions assigned to a given role.
     */
    public static Set<Permission> getPermissionsForRole(String roleCode) {
        if (roleCode == null) return Collections.emptySet();
        Set<Permission> perms = ROLE_PERMISSIONS.get(roleCode.toUpperCase().trim());
        return perms != null ? perms : Collections.emptySet();
    }

    /**
     * Returns a list of permission codes (String) for API serialization.
     */
    public static List<String> getPermissionCodes(String roleCode) {
        Set<Permission> perms = getPermissionsForRole(roleCode);
        List<String> list = new ArrayList<>(perms.size());
        for (Permission p : perms) {
            list.add(p.name());
        }
        Collections.sort(list);
        return list;
    }
}
