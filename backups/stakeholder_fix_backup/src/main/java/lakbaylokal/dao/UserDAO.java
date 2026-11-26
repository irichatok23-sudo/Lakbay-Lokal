package lakbaylokal.dao;

import lakbaylokal.model.Role;
import lakbaylokal.model.User;

import java.util.List;

public interface UserDAO {
    User findById(int userId);
    User findByUsername(String username);
    User findByEmail(String email);
    List<User> findAll();
    List<User> findByRole(String roleCode);
    int create(User user);
    boolean update(User user);
    boolean updatePassword(int userId, String newHash, String newSalt);
    boolean updateStatus(int userId, String status);
    boolean delete(int userId);
    List<Role> findAllRoles();
}
