package com.lakbaylokal.service;

import com.lakbaylokal.dao.StakeholderDAO;
import com.lakbaylokal.dao.VerificationDAO;
import com.lakbaylokal.dao.impl.StakeholderDAOImpl;
import com.lakbaylokal.dao.impl.VerificationDAOImpl;
import com.lakbaylokal.model.VerificationRecord;

import java.util.List;

public class VerificationService {

    private final VerificationDAO verificationDAO = new VerificationDAOImpl();
    private final StakeholderDAO stakeholderDAO = new StakeholderDAOImpl();

    public List<VerificationRecord> getVerifications(Integer barangayId, String status) {
        return verificationDAO.findAll(barangayId, status);
    }

    public List<VerificationRecord> getVerifications(Integer barangayId, Integer municipalityId, String status) {
        return verificationDAO.findAll(barangayId, municipalityId, status);
    }

    public List<VerificationRecord> getVerificationsForStakeholder(int stakeholderId) {
        return verificationDAO.findByStakeholderId(stakeholderId);
    }

    public VerificationRecord getLatestVerificationForStakeholder(int stakeholderId) {
        List<VerificationRecord> list = verificationDAO.findByStakeholderId(stakeholderId);
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    public boolean processVerification(int stakeholderId, int barangayId, int reviewerId,
                                       String status, String requirements, String remarks) {
        var sp = stakeholderDAO.findById(stakeholderId);
        String prevStatus = (sp != null && sp.getVerificationStatus() != null) ? sp.getVerificationStatus() : "PENDING";

        String profileStatus;
        String recordStatus;

        if ("APPROVED".equalsIgnoreCase(status) || "VERIFIED".equalsIgnoreCase(status)) {
            profileStatus = "VERIFIED";
            recordStatus = "APPROVED";
        } else if ("REQUEST_CORRECTION".equalsIgnoreCase(status) || "NEEDS_CORRECTION".equalsIgnoreCase(status)) {
            profileStatus = "NEEDS_CORRECTION";
            recordStatus = "REQUEST_CORRECTION";
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            profileStatus = "REJECTED";
            recordStatus = "REJECTED";
        } else if ("UNDER_REVIEW".equalsIgnoreCase(status)) {
            profileStatus = "UNDER_REVIEW";
            recordStatus = "PENDING";
        } else {
            profileStatus = "PENDING";
            recordStatus = "PENDING";
        }

        VerificationRecord record = new VerificationRecord();
        record.setStakeholderId(stakeholderId);
        record.setBarangayId(barangayId);
        record.setReviewerId(reviewerId);
        record.setPreviousStatus(prevStatus);
        record.setStatus(recordStatus);
        record.setRequirementsSubmitted(requirements != null ? requirements : "");
        record.setRemarks(remarks != null ? remarks : "");

        int recId = verificationDAO.create(record);
        if (recId > 0) {
            return stakeholderDAO.updateVerificationStatus(stakeholderId, profileStatus, reviewerId);
        }
        return false;
    }
}
