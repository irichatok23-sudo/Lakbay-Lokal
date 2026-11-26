package com.lakbaylokal.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class TourismService {
    private int serviceId;
    private int stakeholderId;
    private String serviceName;
    private String serviceCategory;
    private String description;
    private BigDecimal basePrice;
    private String pricingUnit;
    private int maxCapacity;
    private boolean available;
    private Timestamp createdAt;

    // Joined fields
    private String businessName;
    private String stakeholderName;
    private String barangayName;

    public TourismService() {}

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public int getStakeholderId() { return stakeholderId; }
    public void setStakeholderId(int stakeholderId) { this.stakeholderId = stakeholderId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getServiceCategory() { return serviceCategory; }
    public void setServiceCategory(String serviceCategory) { this.serviceCategory = serviceCategory; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public String getPricingUnit() { return pricingUnit; }
    public void setPricingUnit(String pricingUnit) { this.pricingUnit = pricingUnit; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getStakeholderName() { return stakeholderName; }
    public void setStakeholderName(String stakeholderName) { this.stakeholderName = stakeholderName; }

    public String getBarangayName() { return barangayName; }
    public void setBarangayName(String barangayName) { this.barangayName = barangayName; }

    // Convenience aliases for backward compatibility
    public String getServiceType() { return serviceCategory; }
    public void setServiceType(String serviceType) { this.serviceCategory = serviceType; }
    public BigDecimal getPrice() { return basePrice; }
    public void setPrice(BigDecimal price) { this.basePrice = price; }
}
