package com.lakbaylokal.dao;

import com.lakbaylokal.model.Barangay;
import com.lakbaylokal.model.Destination;

import java.util.List;

public interface DestinationDAO {
    Destination findById(int destinationId);
    List<Destination> findAll(String category, Integer barangayId, String status);
    List<Destination> findAll(String category, Integer barangayId, Integer municipalityId, String status);
    int create(Destination destination);
    boolean update(Destination destination);
    boolean updateStatus(int destinationId, String status);
    boolean validateDestination(int destinationId, int validatorUserId);
    boolean delete(int destinationId);
    List<Barangay> findAllBarangays();
    boolean updateBarangayContact(int barangayId, String contactPerson, String contactNumber);
}
