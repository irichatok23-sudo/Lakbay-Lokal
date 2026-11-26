package com.lakbaylokal.dao;

import com.lakbaylokal.model.VerificationRecord;

import java.util.List;

public interface VerificationDAO {
    VerificationRecord findById(int verificationId);
    List<VerificationRecord> findByStakeholderId(int stakeholderId);
    List<VerificationRecord> findAll(Integer barangayId, String status);
    List<VerificationRecord> findAll(Integer barangayId, Integer municipalityId, String status);
    int create(VerificationRecord record);
}
