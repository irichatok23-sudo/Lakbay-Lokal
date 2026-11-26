package lakbaylokal.service;

import lakbaylokal.dao.StakeholderDAO;
import lakbaylokal.dao.VerificationDAO;
import lakbaylokal.dao.impl.StakeholderDAOImpl;
import lakbaylokal.dao.impl.VerificationDAOImpl;
import lakbaylokal.model.VerificationRecord;

import java.time.LocalDate;
import java.util.List;

public class VerificationService {

    private final VerificationDAO verificationDAO = new VerificationDAOImpl();
    private final StakeholderDAO stakeholderDAO = new StakeholderDAOImpl();

    public List<VerificationRecord> getVerifications(Integer barangayId, String status) {
        return verificationDAO.findAll(barangayId, status);
    }

    public List<VerificationRecord> getPendingVerifications() {
        return verificationDAO.findAll(null, "PENDING");
    }

    public List<VerificationRecord> getVerificationsForStakeholder(int stakeholderId) {
        return verificationDAO.findByStakeholderId(stakeholderId);
    }

    public boolean processVerification(int stakeholderId, int barangayId, int reviewerId,
                                        String status, String requirements, String remarks) {
        VerificationRecord record = new VerificationRecord();
        record.setStakeholderId(stakeholderId);
        record.setBarangayId(barangayId);
        record.setReviewerId(reviewerId);
        record.setStatus(status);
        record.setRequirementsSubmitted(requirements);
        record.setRemarks(remarks);

        int recId = verificationDAO.create(record);
        if (recId > 0) {
            String profileStatus = "APPROVED".equalsIgnoreCase(status) ? "VERIFIED" : "REJECTED";
            return stakeholderDAO.updateVerificationStatus(stakeholderId, profileStatus, reviewerId);
        }
        return false;
    }

    public boolean verifyStakeholder(int verificationId, int reviewerId, String status, String remarks, LocalDate validity) {
        VerificationRecord rec = verificationDAO.findById(verificationId);
        int stakeholderId = (rec != null) ? rec.getStakeholderId() : verificationId;
        String profileStatus = "APPROVED".equalsIgnoreCase(status) ? "VERIFIED" : "REJECTED";
        return stakeholderDAO.updateVerificationStatus(stakeholderId, profileStatus, reviewerId);
    }
}
