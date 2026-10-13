package lakbaylokal.authentication;

import lakbaylokal.dao.BookingDAO;
import lakbaylokal.dao.UserDAO;
import lakbaylokal.dao.impl.BookingDAOImpl;
import lakbaylokal.dao.impl.UserDAOImpl;
import lakbaylokal.model.TouristProfile;
import lakbaylokal.model.User;
import lakbaylokal.util.PasswordUtil;

public class AuthenticationService {

    private final UserDAO userDAO = new UserDAOImpl();
    private final BookingDAO bookingDAO = new BookingDAOImpl();

    public User authenticate(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || password == null) return null;

        User user = userDAO.findByUsername(usernameOrEmail.trim());
        if (user == null) {
            user = userDAO.findByEmail(usernameOrEmail.trim());
        }

        if (user == null || !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return null;
        }

        boolean verified = PasswordUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash());
        if (verified) {
            SessionManager.setCurrentUser(user);
            return user;
        }
        return null;
    }

    public boolean registerTourist(String username, String password, String email, String fullName, String phone, String originCity) {
        if (userDAO.findByUsername(username.trim()) != null || userDAO.findByEmail(email.trim()) != null) {
            return false;
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
            return true;
        }
        return false;
    }
}
