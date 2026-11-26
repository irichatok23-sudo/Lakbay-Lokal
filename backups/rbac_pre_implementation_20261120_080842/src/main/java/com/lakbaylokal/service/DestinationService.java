package com.lakbaylokal.service;

import com.lakbaylokal.dao.DestinationDAO;
import com.lakbaylokal.dao.impl.DestinationDAOImpl;
import com.lakbaylokal.model.Barangay;
import com.lakbaylokal.model.Destination;

import java.util.List;

public class DestinationService {

    private final DestinationDAO destinationDAO = new DestinationDAOImpl();

    public List<Destination> getDestinations(String category, Integer barangayId, String status) {
        return destinationDAO.findAll(category, barangayId, status);
    }

    public List<Destination> getDestinations(String category, Integer barangayId, Integer municipalityId, String status) {
        return destinationDAO.findAll(category, barangayId, municipalityId, status);
    }

    public Destination getDestinationById(int destinationId) {
        return destinationDAO.findById(destinationId);
    }

    public int createDestination(Destination d) {
        return destinationDAO.create(d);
    }

    public boolean updateDestination(Destination d) {
        return destinationDAO.update(d);
    }

    public boolean validateDestination(int destinationId, int validatorUserId) {
        return destinationDAO.validateDestination(destinationId, validatorUserId);
    }

    public boolean deleteDestination(int destinationId) {
        return destinationDAO.delete(destinationId);
    }

    public List<Barangay> getBarangays() {
        return destinationDAO.findAllBarangays();
    }

    public boolean updateBarangayContact(int barangayId, String contactPerson, String contactNumber) {
        return destinationDAO.updateBarangayContact(barangayId, contactPerson, contactNumber);
    }
}
