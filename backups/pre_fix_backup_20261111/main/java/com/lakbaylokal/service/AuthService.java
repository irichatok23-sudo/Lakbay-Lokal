package com.lakbaylokal.service;

import com.lakbaylokal.dao.BookingDAO;
import com.lakbaylokal.dao.StakeholderDAO;
import com.lakbaylokal.dao.UserDAO;
import com.lakbaylokal.dao.impl.BookingDAOImpl;
import com.lakbaylokal.dao.impl.StakeholderDAOImpl;
import com.lakbaylokal.dao.impl.UserDAOImpl;
import com.lakbaylokal.database.ConnectionFactory;
import com.lakbaylokal.database.DatabaseInitializer;
import com.lakbaylokal.model.StakeholderProfile;
import com.lakbaylokal.model.TouristProfile;
import com.lakbaylokal.model.User;
import com.lakbaylokal.util.PasswordUtil;
import com.lakbaylokal.util.SessionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles authentication, registration, password hashing, and session management.
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAOImpl();
    private final BookingDAO bookingDAO = new BookingDAOImpl();
    private final StakeholderDAO stakeholderDAO = new StakeholderDAOImpl();
    private static volatile boolean dbSynced = false;

    public Map<String, Object> login(String usernameOrEmail, String password) {
        Map<String, Object> result = new HashMap<>();
        if (usernameOrEmail == null || usernameOrEmail.isBlank() || password == null) {
            result.put("success", false);
            result.put("message", "Username and password are required.");
            return result;
        }

        // Validate database connection upfront to distinguish server connection errors from bad credentials
        if (!ConnectionFactory.testConnection()) {
            result.put("success", false);
            result.put("errorType", "SERVER_CONNECTION_ERROR");
            result.put("message", "Database server connection error. Unable to reach MySQL server on localhost:3306.");
            return result;
        }

        if (!dbSynced) {
            synchronized (AuthService.class) {
                if (!dbSynced) {
                    DatabaseInitializer.initializeIfNeeded();
                    dbSynced = true;
                }
            }
        }

        String identifier = usernameOrEmail.trim();
        User user = userDAO.findByUsername(identifier);
        if (user == null) {
            user = userDAO.findByEmail(identifier);
        }

        if (user == null) {
            result.put("success", false);
            result.put("message", "Invalid username or password.");
            return result;
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            result.put("success", false);
            result.put("message", "This account is " + user.getStatus().toLowerCase() + ". Please contact the administrator.");
            return result;
        }

        boolean verified = PasswordUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash());
        if (!verified) {
            result.put("success", false);
            result.put("message", "Invalid username or password.");
            return result;
        }

        String token = SessionManager.createSession(user);
        result.put("success", true);
        result.put("token", token);
        result.put("user", sanitize(user));

        // Attach profile IDs if applicable
        if ("TOURIST".equalsIgnoreCase(user.getRoleCode())) {
            TouristProfile tp = bookingDAO.findTouristProfileByUserId(user.getUserId());
            if (tp != null) result.put("touristProfileId", tp.getProfileId());
        } else if ("STAKEHOLDER".equalsIgnoreCase(user.getRoleCode())) {
            StakeholderProfile sp = stakeholderDAO.findByUserId(user.getUserId());
            if (sp != null) {
                result.put("stakeholderProfileId", sp.getProfileId());
                result.put("verificationStatus", sp.getVerificationStatus());
            }
        }

        return result;
    }

    public Map<String, Object> registerTourist(String username, String password, String email,
                                                String fullName, String phone, String originCity) {
        Map<String, Object> result = new HashMap<>();
        if (username == null || password == null || email == null || fullName == null) {
            result.put("success", false);
            result.put("message", "All mandatory registration fields must be provided.");
            return result;
        }

        if (userDAO.findByUsername(username.trim()) != null) {
            result.put("success", false);
            result.put("message", "Username is already registered.");
            return result;
        }

        if (userDAO.findByEmail(email.trim()) != null) {
            result.put("success", false);
            result.put("message", "Email address is already registered.");
            return result;
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);

        User user = new User();
        user.setUsername(username.trim());
        user.setPasswordHash(hash);
        user.setSalt(salt);
        user.setEmail(email.trim());
        user.setFullName(fullName.trim());
        user.setPhone(phone);
        user.setRoleId(6); // TOURIST
        user.setStatus("ACTIVE");

        int newUserId = userDAO.create(user);
        if (newUserId > 0) {
            TouristProfile profile = new TouristProfile();
            profile.setUserId(newUserId);
            profile.setOriginCity(originCity);
            profile.setNationality("Filipino");
            bookingDAO.createTouristProfile(profile);

            result.put("success", true);
            result.put("message", "Registration successful. You can now log in.");
        } else {
            result.put("success", false);
            result.put("message", "Registration failed due to a database error.");
        }

        return result;
    }

    public Map<String, Object> forgotPassword(String usernameOrEmail, String newPassword) {
        Map<String, Object> result = new HashMap<>();
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            result.put("success", false);
            result.put("message", "Username or email is required.");
            return result;
        }

        User user = userDAO.findByUsername(usernameOrEmail.trim());
        if (user == null) {
            user = userDAO.findByEmail(usernameOrEmail.trim());
        }

        if (user == null) {
            result.put("success", false);
            result.put("message", "No account found matching that username or email address.");
            return result;
        }

        if (newPassword == null || newPassword.isBlank()) {
            result.put("success", true);
            result.put("message", "Account verified for " + user.getFullName() + ". Please provide a new password.");
            return result;
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(newPassword.trim(), salt);
        boolean updated = userDAO.updatePassword(user.getUserId(), hash, salt);

        if (updated) {
            result.put("success", true);
            result.put("message", "Password successfully updated for " + user.getUsername() + ". You can now sign in.");
        } else {
            result.put("success", false);
            result.put("message", "Database error updating password.");
        }
        return result;
    }

    private Map<String, Object> sanitize(User u) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", u.getUserId());
        map.put("username", u.getUsername());
        map.put("email", u.getEmail());
        map.put("fullName", u.getFullName());
        map.put("phone", u.getPhone());
        map.put("roleId", u.getRoleId());
        map.put("roleCode", u.getRoleCode());
        map.put("roleName", u.getRoleName());
        map.put("barangayId", u.getBarangayId());
        map.put("barangayName", u.getBarangayName());
        map.put("municipalityName", u.getMunicipalityName());
        map.put("status", u.getStatus());
        return map;
    }
}
