package com.lakbaylokal.service;

import com.lakbaylokal.dao.AuditLogDAO;
import com.lakbaylokal.dao.impl.AuditLogDAOImpl;
import com.lakbaylokal.model.AuditLog;

import java.util.List;

public class AuditService {

    private final AuditLogDAO auditLogDAO = new AuditLogDAOImpl();

    public void log(Integer userId, String username, String action, String entityName, Integer entityId, String details) {
        AuditLog l = new AuditLog(userId, username, action, entityName, entityId, details);
        auditLogDAO.log(l);
    }

    public List<AuditLog> getRecentLogs(int limit) {
        return auditLogDAO.findAll(limit);
    }
}
