package com.lakbaylokal.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Destination {
    private int destinationId;
    private int barangayId;
    private String name;
    private String category;
    private String description;
    private BigDecimal entranceFee;
    private String operatingHours;
    private String locationCoordinates;
    private String contactInfo;
    private String imageUrl;
    private String status;
    private Integer validatedBy;
    private Timestamp validatedAt;
    private Timestamp createdAt;

    // Joined fields
    private String barangayName;
    private String municipalityName;
    private double averageRating;
    private int reviewCount;

    public Destination() {}

    public int getDestinationId() { return destinationId; }
    public void setDestinationId(int destinationId) { this.destinationId = destinationId; }

    public int getBarangayId() { return barangayId; }
    public void setBarangayId(int barangayId) { this.barangayId = barangayId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getEntranceFee() { return entranceFee; }
    public void setEntranceFee(BigDecimal entranceFee) { this.entranceFee = entranceFee; }

    public String getOperatingHours() { return operatingHours; }
    public void setOperatingHours(String operatingHours) { this.operatingHours = operatingHours; }

    public String getLocationCoordinates() { return locationCoordinates; }
    public void setLocationCoordinates(String locationCoordinates) { this.locationCoordinates = locationCoordinates; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getValidatedBy() { return validatedBy; }
    public void setValidatedBy(Integer validatedBy) { this.validatedBy = validatedBy; }

    public Timestamp getValidatedAt() { return validatedAt; }
    public void setValidatedAt(Timestamp validatedAt) { this.validatedAt = validatedAt; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getBarangayName() { return barangayName; }
    public void setBarangayName(String barangayName) { this.barangayName = barangayName; }

    public String getMunicipalityName() { return municipalityName; }
    public void setMunicipalityName(String municipalityName) { this.municipalityName = municipalityName; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
}
