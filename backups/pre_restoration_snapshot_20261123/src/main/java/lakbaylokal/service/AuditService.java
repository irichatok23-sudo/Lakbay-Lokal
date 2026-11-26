package lakbaylokal.service;

import lakbaylokal.dao.AuditLogDAO;
import lakbaylokal.dao.impl.AuditLogDAOImpl;
import lakbaylokal.model.AuditLog;

import java.util.List;

public class AuditService {

    private final AuditLogDAO auditLogDAO = new AuditLogDAOImpl();

    public void log(Integer userId, String username, String action, String entityName, Integer entityId, String details) {
        AuditLog l = new AuditLog(userId, username, action, entityName, entityId, details);
        auditLogDAO.log(l);
    }

    public void logAction(Integer userId, String action, String entityType, Integer entityId, String details, String ipAddress) {
        AuditLog l = new AuditLog(userId, "user" + userId, action, entityType, entityId, details);
        l.setIpAddress(ipAddress);
        auditLogDAO.log(l);
    }

    public List<AuditLog> getRecentLogs(int limit) {
        return auditLogDAO.findAll(limit);
    }
}
