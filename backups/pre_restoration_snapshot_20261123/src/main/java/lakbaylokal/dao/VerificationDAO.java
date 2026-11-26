package lakbaylokal.dao;

import lakbaylokal.model.VerificationRecord;

import java.util.List;

public interface VerificationDAO {
    VerificationRecord findById(int verificationId);
    List<VerificationRecord> findByStakeholderId(int stakeholderId);
    List<VerificationRecord> findAll(Integer barangayId, String status);
    int create(VerificationRecord record);
}
