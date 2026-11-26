package com.lakbaylokal.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Announcement {
    private int announcementId;
    private String title;
    private String content;
    private String category;
    private Integer targetBarangayId;
    private String imageUrl;
    private int publishedBy;
    private Date publishDate;
    private Date expiryDate;
    private String status;
    private Timestamp createdAt;

    // Joined fields
    private String publisherName;
    private String targetBarangayName;
    private Integer municipalityId;
    private String municipalityName;

    public Announcement() {}

    public int getAnnouncementId() { return announcementId; }
    public void setAnnouncementId(int announcementId) { this.announcementId = announcementId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getTargetBarangayId() { return targetBarangayId; }
    public void setTargetBarangayId(Integer targetBarangayId) { this.targetBarangayId = targetBarangayId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getPublishedBy() { return publishedBy; }
    public void setPublishedBy(int publishedBy) { this.publishedBy = publishedBy; }

    public Date getPublishDate() { return publishDate; }
    public void setPublishDate(Date publishDate) { this.publishDate = publishDate; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }

    public String getTargetBarangayName() { return targetBarangayName; }
    public void setTargetBarangayName(String targetBarangayName) { this.targetBarangayName = targetBarangayName; }

    public Integer getMunicipalityId() { return municipalityId; }
    public void setMunicipalityId(Integer municipalityId) { this.municipalityId = municipalityId; }

    public String getMunicipalityName() { return municipalityName; }
    public void setMunicipalityName(String municipalityName) { this.municipalityName = municipalityName; }
}
