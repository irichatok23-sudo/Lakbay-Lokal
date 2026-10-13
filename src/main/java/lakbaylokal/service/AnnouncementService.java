package lakbaylokal.service;

import lakbaylokal.dao.AnnouncementDAO;
import lakbaylokal.dao.impl.AnnouncementDAOImpl;
import lakbaylokal.model.Announcement;

import java.util.List;

public class AnnouncementService {

    private final AnnouncementDAO announcementDAO = new AnnouncementDAOImpl();

    public List<Announcement> getAnnouncements(String category, Integer barangayId, String status) {
        return announcementDAO.findAll(category, barangayId, status);
    }

    public List<Announcement> getAllAnnouncements() {
        return announcementDAO.findAll(null, null, null);
    }

    public Announcement getAnnouncementById(int id) {
        return announcementDAO.findById(id);
    }

    public int createAnnouncement(Announcement a) {
        return announcementDAO.create(a);
    }

    public boolean updateAnnouncement(Announcement a) {
        return announcementDAO.update(a);
    }

    public boolean deleteAnnouncement(int id) {
        return announcementDAO.delete(id);
    }
}
