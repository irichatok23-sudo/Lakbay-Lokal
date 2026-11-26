package lakbaylokal.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class ArrivalRecord {
    private int logId;
    private int destinationId;
    private Date logDate;
    private int localTouristsCount;
    private int domesticTouristsCount;
    private int foreignTouristsCount;
    private BigDecimal totalRevenue;
    private int recordedBy;
    private Timestamp createdAt;

    private String destinationName;
    private String barangayName;
    private String recorderName;

    public ArrivalRecord() {}

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getDestinationId() { return destinationId; }
    public void setDestinationId(int destinationId) { this.destinationId = destinationId; }

    public Date getLogDate() { return logDate; }
    public void setLogDate(Date logDate) { this.logDate = logDate; }

    public int getLocalTouristsCount() { return localTouristsCount; }
    public void setLocalTouristsCount(int localTouristsCount) { this.localTouristsCount = localTouristsCount; }

    public int getDomesticTouristsCount() { return domesticTouristsCount; }
    public void setDomesticTouristsCount(int domesticTouristsCount) { this.domesticTouristsCount = domesticTouristsCount; }

    public int getForeignTouristsCount() { return foreignTouristsCount; }
    public void setForeignTouristsCount(int foreignTouristsCount) { this.foreignTouristsCount = foreignTouristsCount; }

    public int getTotalVisitors() {
        return localTouristsCount + domesticTouristsCount + foreignTouristsCount;
    }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public int getRecordedBy() { return recordedBy; }
    public void setRecordedBy(int recordedBy) { this.recordedBy = recordedBy; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getDestinationName() { return destinationName; }
    public void setDestinationName(String destinationName) { this.destinationName = destinationName; }

    public String getBarangayName() { return barangayName; }
    public void setBarangayName(String barangayName) { this.barangayName = barangayName; }

    public String getRecorderName() { return recorderName; }
    public void setRecorderName(String recorderName) { this.recorderName = recorderName; }
}
