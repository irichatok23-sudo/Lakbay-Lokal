package lakbaylokal.service;

import lakbaylokal.dao.PackageDAO;
import lakbaylokal.dao.impl.PackageDAOImpl;
import lakbaylokal.model.TourismPackage;

import java.util.ArrayList;
import java.util.List;

public class PackageService {

    private final PackageDAO packageDAO = new PackageDAOImpl();

    public List<TourismPackage> getPackages(String type, String status) {
        return packageDAO.findAll(type, status);
    }

    public List<TourismPackage> getAllPackages() {
        return packageDAO.findAll(null, null);
    }

    public TourismPackage getPackageById(int packageId) {
        return packageDAO.findById(packageId);
    }

    public int createPackage(TourismPackage pkg, List<Integer> destinationIds, List<Integer> serviceIds) {
        return packageDAO.create(pkg, destinationIds, serviceIds);
    }

    public int createPackage(TourismPackage pkg) {
        return packageDAO.create(pkg, new ArrayList<>(), new ArrayList<>());
    }

    public boolean updatePackage(TourismPackage pkg, List<Integer> destinationIds, List<Integer> serviceIds) {
        return packageDAO.update(pkg, destinationIds, serviceIds);
    }

    public boolean updatePackage(TourismPackage pkg) {
        return packageDAO.update(pkg, new ArrayList<>(), new ArrayList<>());
    }

    public boolean deletePackage(int packageId) {
        return packageDAO.delete(packageId);
    }
}
