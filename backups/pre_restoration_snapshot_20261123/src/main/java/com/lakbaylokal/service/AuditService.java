package com.lakbaylokal.service;

import com.lakbaylokal.dao.AuditLogDAO;
import com.lakbaylokal.dao.impl.AuditLogDAOImpl;
import com.lakbaylokal.model.AuditLog;

import java.util.List;

public class AuditService {

    private final AuditLogDAO auditLogDAO = new AuditLogDAOImpl();

    public void log(Integer userId, String username, String action, String entityName, Integer entityId, String details) {
        log(userId, username, action, entityName, entityId, "127.0.0.1", details);
    }

    public void log(Integer userId, String username, String action, String entityName, Integer entityId, String ipAddress, String details) {
        AuditLog l = new AuditLog(userId, username, action, entityName, entityId, details);
        if (ipAddress != null && !ipAddress.isBlank()) {
            l.setIpAddress(ipAddress);
        }
        auditLogDAO.log(l);
    }

    public List<AuditLog> getRecentLogs(int limit) {
        return auditLogDAO.findAll(limit);
    }
}
