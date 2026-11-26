package com.lakbaylokal.model;

import java.sql.Timestamp;

public class VerificationRecord {
    private int verificationId;
    private int stakeholderId;
    private int barangayId;
    private int reviewerId;
    private String previousStatus;
    private String status;
    private String requirementsSubmitted;
    private String remarks;
    private Timestamp decisionDate;

    // Joined fields
    private String businessName;
    private String stakeholderName;
    private String barangayName;
    private String reviewerName;

    public VerificationRecord() {}

    public int getVerificationId() { return verificationId; }
    public void setVerificationId(int verificationId) { this.verificationId = verificationId; }

    public int getStakeholderId() { return stakeholderId; }
    public void setStakeholderId(int stakeholderId) { this.stakeholderId = stakeholderId; }

    public int getBarangayId() { return barangayId; }
    public void setBarangayId(int barangayId) { this.barangayId = barangayId; }

    public int getReviewerId() { return reviewerId; }
    public void setReviewerId(int reviewerId) { this.reviewerId = reviewerId; }

    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRequirementsSubmitted() { return requirementsSubmitted; }
    public void setRequirementsSubmitted(String requirementsSubmitted) { this.requirementsSubmitted = requirementsSubmitted; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Timestamp getDecisionDate() { return decisionDate; }
    public void setDecisionDate(Timestamp decisionDate) { this.decisionDate = decisionDate; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getStakeholderName() { return stakeholderName; }
    public void setStakeholderName(String stakeholderName) { this.stakeholderName = stakeholderName; }

    public String getBarangayName() { return barangayName; }
    public void setBarangayName(String barangayName) { this.barangayName = barangayName; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
}
