package lakbaylokal.service;

import lakbaylokal.dao.DestinationDAO;
import lakbaylokal.dao.impl.DestinationDAOImpl;
import lakbaylokal.model.Barangay;
import lakbaylokal.model.Destination;

import java.util.ArrayList;
import java.util.List;

public class DestinationService {

    private final DestinationDAO destinationDAO = new DestinationDAOImpl();

    public List<Destination> getDestinations(String category, Integer barangayId, String status) {
        return destinationDAO.findAll(category, barangayId, status);
    }

    public List<Destination> getAllDestinations() {
        return destinationDAO.findAll(null, null, null);
    }

    public List<Destination> getFeaturedDestinations() {
        List<Destination> all = getAllDestinations();
        List<Destination> featured = new ArrayList<>();
        for (Destination d : all) {
            if (d.isFeatured()) {
                featured.add(d);
            }
        }
        if (featured.isEmpty() && !all.isEmpty()) {
            return all;
        }
        return featured;
    }

    public Destination getDestinationById(int destinationId) {
        return destinationDAO.findById(destinationId);
    }

    public int createDestination(Destination d) {
        return destinationDAO.create(d);
    }

    public int addDestination(Destination d) {
        return createDestination(d);
    }

    public boolean updateDestination(Destination d) {
        return destinationDAO.update(d);
    }

    public boolean updateDestinationStatus(int destinationId, String status) {
        return destinationDAO.updateStatus(destinationId, status);
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
}
