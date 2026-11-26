package com.lakbaylokal.dao;

import com.lakbaylokal.model.AuditLog;

import java.util.List;

public interface AuditLogDAO {
    int log(AuditLog log);
    List<AuditLog> findAll(int limit);
    List<AuditLog> findByUser(int userId, int limit);
}
