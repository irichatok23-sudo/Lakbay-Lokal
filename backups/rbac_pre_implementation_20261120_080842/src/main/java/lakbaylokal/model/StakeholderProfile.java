package lakbaylokal.model;

import java.sql.Timestamp;

public class StakeholderProfile {
    private int profileId;
    private int userId;
    private int barangayId;
    private String businessName;
    private String category;
    private String accreditationNo;
    private String contactNumber;
    private int yearsExperience;
    private String verificationStatus;
    private Integer verifiedBy;
    private Timestamp verifiedAt;

    private String ownerName;
    private String email;
    private String barangayName;
    private String municipalityName;

    public StakeholderProfile() {}

    public int getProfileId() { return profileId; }
    public void setProfileId(int profileId) { this.profileId = profileId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getBarangayId() { return barangayId; }
    public void setBarangayId(int barangayId) { this.barangayId = barangayId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAccreditationNo() { return accreditationNo; }
    public void setAccreditationNo(String accreditationNo) { this.accreditationNo = accreditationNo; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public int getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(int yearsExperience) { this.yearsExperience = yearsExperience; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public Integer getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(Integer verifiedBy) { this.verifiedBy = verifiedBy; }

    public Timestamp getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Timestamp verifiedAt) { this.verifiedAt = verifiedAt; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBarangayName() { return barangayName; }
    public void setBarangayName(String barangayName) { this.barangayName = barangayName; }

    public String getMunicipalityName() { return municipalityName; }
    public void setMunicipalityName(String municipalityName) { this.municipalityName = municipalityName; }

    private String address = "Biliran, Philippines";

    public String getBusinessType() { return category; }
    public void setBusinessType(String businessType) { this.category = businessType; }

    public String getMunicipality() { return municipalityName; }
    public void setMunicipality(String municipality) { this.municipalityName = municipality; }

    public String getBarangay() { return barangayName; }
    public void setBarangay(String barangay) { this.barangayName = barangay; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getStakeholderId() { return profileId; }
    public void setStakeholderId(int id) { this.profileId = id; }

    public String getAccreditationStatus() {
        return verificationStatus != null ? verificationStatus : "VERIFIED";
    }
    public void setAccreditationStatus(String s) { this.verificationStatus = s; }

    @Override
    public String toString() {
        return businessName + " (" + ownerName + ")";
    }
}
