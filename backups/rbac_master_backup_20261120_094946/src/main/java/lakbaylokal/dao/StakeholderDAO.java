package lakbaylokal.dao;

import lakbaylokal.model.StakeholderProfile;
import lakbaylokal.model.TourismService;

import java.util.List;

public interface StakeholderDAO {
    StakeholderProfile findByUserId(int userId);
    StakeholderProfile findById(int profileId);
    List<StakeholderProfile> findAll(String category, Integer barangayId, String verificationStatus);
    int create(StakeholderProfile profile);
    boolean update(StakeholderProfile profile);
    boolean updateVerificationStatus(int profileId, String status, int verifiedBy);

    List<TourismService> findServicesByStakeholder(int stakeholderId);
    TourismService findServiceById(int serviceId);
    int createService(TourismService service);
    boolean updateService(TourismService service);
    boolean deleteService(int serviceId);
    List<TourismService> findAllServices(String category, Boolean availableOnly);
}
