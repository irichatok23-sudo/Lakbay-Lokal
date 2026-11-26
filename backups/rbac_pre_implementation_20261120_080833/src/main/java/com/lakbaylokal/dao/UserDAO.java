package com.lakbaylokal.dao;

import com.lakbaylokal.model.Role;
import com.lakbaylokal.model.User;

import java.util.List;

public interface UserDAO {
    User findById(int userId);
    User findByUsername(String username);
    User findByEmail(String email);
    List<User> findAll();
    List<User> findByRole(String roleCode);
    List<User> findFiltered(String roleCode, Integer municipalityId, Integer barangayId);
    int create(User user);
    boolean update(User user);
    boolean updatePassword(int userId, String newHash, String newSalt);
    boolean updateStatus(int userId, String status);
    boolean delete(int userId);
    List<Role> findAllRoles();
}
