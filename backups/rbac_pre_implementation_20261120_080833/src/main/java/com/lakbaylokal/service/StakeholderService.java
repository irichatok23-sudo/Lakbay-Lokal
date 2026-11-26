package com.lakbaylokal.service;

import com.lakbaylokal.dao.StakeholderDAO;
import com.lakbaylokal.dao.impl.StakeholderDAOImpl;
import com.lakbaylokal.model.AuditLog;
import com.lakbaylokal.model.StakeholderProfile;
import com.lakbaylokal.model.TourismService;

import java.util.List;

public class StakeholderService {

    private final StakeholderDAO stakeholderDAO = new StakeholderDAOImpl();

    public StakeholderProfile getProfileByUserId(int userId) {
        return stakeholderDAO.findByUserId(userId);
    }

    public StakeholderProfile getProfileById(int profileId) {
        return stakeholderDAO.findById(profileId);
    }

    public List<StakeholderProfile> getStakeholders(String category, Integer barangayId, String verificationStatus) {
        return stakeholderDAO.findAll(category, barangayId, verificationStatus);
    }

    public List<StakeholderProfile> getStakeholders(String category, Integer barangayId, Integer municipalityId, String verificationStatus) {
        return stakeholderDAO.findAll(category, barangayId, municipalityId, verificationStatus);
    }

    public int registerStakeholder(StakeholderProfile profile) {
        return stakeholderDAO.create(profile);
    }

    public boolean updateProfile(StakeholderProfile profile) {
        return stakeholderDAO.update(profile);
    }

    public boolean resetToPending(int profileId) {
        return stakeholderDAO.resetToPending(profileId);
    }

    /**
     * Atomically applies corrected profile fields, resets verification status to
     * PENDING, clears verified_by / verified_at, and writes the STAKEHOLDER_RESUBMIT
     * audit log entry — all within a single database transaction.
     */
    public boolean resubmitProfile(StakeholderProfile profile) {
        return stakeholderDAO.resubmitProfile(profile);
    }

    /**
     * Atomically applies corrected profile fields, resets verification status to
     * PENDING, clears verified_by / verified_at, and writes the provided audit log
     * entry — all within a single database transaction.
     */
    public boolean resubmitProfile(StakeholderProfile profile, AuditLog auditLog) {
        return stakeholderDAO.resubmitProfile(profile, auditLog);
    }

    public boolean resubmitProfile(StakeholderProfile profile, String username) {
        profile.setOwnerName(username);
        return stakeholderDAO.resubmitProfile(profile);
    }

    public List<TourismService> getServicesForStakeholder(int stakeholderId) {
        return stakeholderDAO.findServicesByStakeholder(stakeholderId);
    }

    public TourismService getServiceById(int serviceId) {
        return stakeholderDAO.findServiceById(serviceId);
    }

    public int createService(TourismService service) {
        return stakeholderDAO.createService(service);
    }

    public boolean updateService(TourismService service) {
        return stakeholderDAO.updateService(service);
    }

    public boolean deleteService(int serviceId) {
        return stakeholderDAO.deleteService(serviceId);
    }

    public List<TourismService> getAllAvailableServices(String category) {
        return stakeholderDAO.findAllServices(category, true);
    }

    public List<TourismService> getAllServices(String category, Boolean availableOnly) {
        return stakeholderDAO.findAllServices(category, availableOnly);
    }
}
