package lakbaylokal.service;

import lakbaylokal.dao.StakeholderDAO;
import lakbaylokal.dao.impl.StakeholderDAOImpl;
import lakbaylokal.model.StakeholderProfile;
import lakbaylokal.model.TourismService;

import java.util.List;

public class StakeholderService {

    private final StakeholderDAO stakeholderDAO = new StakeholderDAOImpl();

    public StakeholderProfile getProfileByUserId(int userId) {
        return stakeholderDAO.findByUserId(userId);
    }

    public StakeholderProfile getStakeholderByUserId(int userId) {
        return getProfileByUserId(userId);
    }

    public StakeholderProfile getProfileById(int profileId) {
        return stakeholderDAO.findById(profileId);
    }

    public List<StakeholderProfile> getStakeholders(String category, Integer barangayId, String verificationStatus) {
        return stakeholderDAO.findAll(category, barangayId, verificationStatus);
    }

    public int registerStakeholder(StakeholderProfile profile) {
        return stakeholderDAO.create(profile);
    }

    public boolean updateProfile(StakeholderProfile profile) {
        return stakeholderDAO.update(profile);
    }

    public boolean updateStakeholderProfile(StakeholderProfile profile) {
        return updateProfile(profile);
    }

    public List<TourismService> getServicesForStakeholder(int stakeholderId) {
        return stakeholderDAO.findServicesByStakeholder(stakeholderId);
    }

    public List<TourismService> getServicesByStakeholder(int stakeholderId) {
        return getServicesForStakeholder(stakeholderId);
    }

    public TourismService getServiceById(int serviceId) {
        return stakeholderDAO.findServiceById(serviceId);
    }

    public int createService(TourismService service) {
        return stakeholderDAO.createService(service);
    }

    public int addService(TourismService service) {
        return createService(service);
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
}
