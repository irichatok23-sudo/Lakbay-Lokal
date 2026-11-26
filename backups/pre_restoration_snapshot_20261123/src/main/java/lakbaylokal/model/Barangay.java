package lakbaylokal.model;

public class Barangay {
    private int barangayId;
    private int municipalityId;
    private String municipalityName;
    private String name;
    private String contactPerson;
    private String contactNumber;

    public Barangay() {}

    public int getBarangayId() { return barangayId; }
    public void setBarangayId(int barangayId) { this.barangayId = barangayId; }

    public int getMunicipalityId() { return municipalityId; }
    public void setMunicipalityId(int municipalityId) { this.municipalityId = municipalityId; }

    public String getMunicipalityName() { return municipalityName; }
    public void setMunicipalityName(String municipalityName) { this.municipalityName = municipalityName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    @Override
    public String toString() {
        return name + " (" + municipalityName + ")";
    }
}
