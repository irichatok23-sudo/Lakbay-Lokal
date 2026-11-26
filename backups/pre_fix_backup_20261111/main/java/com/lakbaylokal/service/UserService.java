package com.lakbaylokal.service;

import com.lakbaylokal.dao.UserDAO;
import com.lakbaylokal.dao.impl.UserDAOImpl;
import com.lakbaylokal.model.Role;
import com.lakbaylokal.model.User;
import com.lakbaylokal.util.PasswordUtil;

import java.util.List;

public class UserService {

    private final UserDAO userDAO = new UserDAOImpl();

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public User getUserById(int userId) {
        return userDAO.findById(userId);
    }

    public List<Role> getAllRoles() {
        return userDAO.findAllRoles();
    }

    public int createUser(User user, String rawPassword) {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(rawPassword, salt);
        user.setSalt(salt);
        user.setPasswordHash(hash);
        return userDAO.create(user);
    }

    public boolean updateUser(User user) {
        return userDAO.update(user);
    }

    public boolean resetPassword(int userId, String newPassword) {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(newPassword, salt);
        return userDAO.updatePassword(userId, hash, salt);
    }

    public boolean updateStatus(int userId, String status) {
        return userDAO.updateStatus(userId, status);
    }

    public boolean deleteUser(int userId) {
        return userDAO.delete(userId);
    }
}
