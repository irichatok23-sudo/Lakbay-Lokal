package com.lakbaylokal.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TourismPackage {
    private int packageId;
    private String name;
    private String description;
    private String packageType;
    private BigDecimal pricePerPax;
    private int durationDays;
    private int minPax;
    private int maxPax;
    private String inclusions;
    private String exclusions;
    private String status;
    private String imageUrl;
    private Integer createdBy;
    private Timestamp createdAt;

    // Associated destinations and services
    private List<Destination> destinations = new ArrayList<>();
    private List<TourismService> services = new ArrayList<>();
    private double averageRating;
    private int reviewCount;

    public TourismPackage() {}

    public int getPackageId() { return packageId; }
    public void setPackageId(int packageId) { this.packageId = packageId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPackageType() { return packageType; }
    public void setPackageType(String packageType) { this.packageType = packageType; }

    public BigDecimal getPricePerPax() { return pricePerPax; }
    public void setPricePerPax(BigDecimal pricePerPax) { this.pricePerPax = pricePerPax; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public int getMinPax() { return minPax; }
    public void setMinPax(int minPax) { this.minPax = minPax; }

    public int getMaxPax() { return maxPax; }
    public void setMaxPax(int maxPax) { this.maxPax = maxPax; }

    public String getInclusions() { return inclusions; }
    public void setInclusions(String inclusions) { this.inclusions = inclusions; }

    public String getExclusions() { return exclusions; }
    public void setExclusions(String exclusions) { this.exclusions = exclusions; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public List<Destination> getDestinations() { return destinations; }
    public void setDestinations(List<Destination> destinations) { this.destinations = destinations; }

    public List<TourismService> getServices() { return services; }
    public void setServices(List<TourismService> services) { this.services = services; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
}
