package lakbaylokal.dao;

import lakbaylokal.model.TourismPackage;

import java.util.List;

public interface PackageDAO {
    TourismPackage findById(int packageId);
    List<TourismPackage> findAll(String type, String status);
    int create(TourismPackage pkg, List<Integer> destinationIds, List<Integer> serviceIds);
    boolean update(TourismPackage pkg, List<Integer> destinationIds, List<Integer> serviceIds);
    boolean delete(int packageId);
}
