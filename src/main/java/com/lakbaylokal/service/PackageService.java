package com.lakbaylokal.service;

import com.lakbaylokal.dao.PackageDAO;
import com.lakbaylokal.dao.impl.PackageDAOImpl;
import com.lakbaylokal.model.TourismPackage;

import java.util.List;

public class PackageService {

    private final PackageDAO packageDAO = new PackageDAOImpl();

    public List<TourismPackage> getPackages(String type, String status) {
        return packageDAO.findAll(type, status);
    }

    public TourismPackage getPackageById(int packageId) {
        return packageDAO.findById(packageId);
    }

    public int createPackage(TourismPackage pkg, List<Integer> destinationIds, List<Integer> serviceIds) {
        return packageDAO.create(pkg, destinationIds, serviceIds);
    }

    public boolean updatePackage(TourismPackage pkg, List<Integer> destinationIds, List<Integer> serviceIds) {
        return packageDAO.update(pkg, destinationIds, serviceIds);
    }

    public boolean deletePackage(int packageId) {
        return packageDAO.delete(packageId);
    }
}
