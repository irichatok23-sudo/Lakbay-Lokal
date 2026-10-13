package lakbaylokal.model;

import java.sql.Timestamp;

public class AuditLog {
    private int logId;
    private Integer userId;
    private String username;
    private String action;
    private String entityName;
    private Integer entityId;
    private String ipAddress;
    private String details;
    private Timestamp timestamp;

    public AuditLog() {}

    public AuditLog(Integer userId, String username, String action, String entityName, Integer entityId, String details) {
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.entityName = entityName;
        this.entityId = entityId;
        this.ipAddress = "127.0.0.1";
        this.details = details;
    }

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public Integer getEntityId() { return entityId; }
    public void setEntityId(Integer entityId) { this.entityId = entityId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public Timestamp getCreatedAt() { return timestamp; }
    public void setCreatedAt(Timestamp createdAt) { this.timestamp = createdAt; }

    public String getEntityType() { return entityName; }
    public void setEntityType(String entityType) { this.entityName = entityType; }
}
