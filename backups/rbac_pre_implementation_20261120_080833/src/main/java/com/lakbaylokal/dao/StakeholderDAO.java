package com.lakbaylokal.dao;

import com.lakbaylokal.model.AuditLog;
import com.lakbaylokal.model.StakeholderProfile;
import com.lakbaylokal.model.TourismService;

import java.util.List;

public interface StakeholderDAO {
    StakeholderProfile findByUserId(int userId);
    StakeholderProfile findById(int profileId);
    List<StakeholderProfile> findAll(String category, Integer barangayId, String verificationStatus);
    List<StakeholderProfile> findAll(String category, Integer barangayId, Integer municipalityId, String verificationStatus);
    int create(StakeholderProfile profile);
    boolean update(StakeholderProfile profile);
    boolean updateVerificationStatus(int profileId, String status, int verifiedBy);
    boolean resetToPending(int profileId);

    /**
     * Atomically updates all correctable profile fields AND resets verification
     * status to PENDING, cleared verified_by and verified_at — in a single SQL
     * UPDATE executed on one database connection.
     * Returns true only if the row was actually updated.
     */
    boolean resubmitProfile(StakeholderProfile profile);

    /**
     * Atomically updates profile fields, resets verification status to PENDING,
     * cleared verified_by and verified_at, and records the resubmission in audit_logs
     * within a single database transaction.
     */
    boolean resubmitProfile(StakeholderProfile profile, AuditLog auditLog);

    List<TourismService> findServicesByStakeholder(int stakeholderId);
    TourismService findServiceById(int serviceId);
    int createService(TourismService service);
    boolean updateService(TourismService service);
    boolean deleteService(int serviceId);
    List<TourismService> findAllServices(String category, Boolean availableOnly);
}
