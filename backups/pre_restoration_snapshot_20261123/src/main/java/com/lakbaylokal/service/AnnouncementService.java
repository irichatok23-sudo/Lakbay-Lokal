package com.lakbaylokal.service;

import com.lakbaylokal.dao.AnnouncementDAO;
import com.lakbaylokal.dao.impl.AnnouncementDAOImpl;
import com.lakbaylokal.model.Announcement;

import java.sql.Date;
import java.util.List;
import java.util.Map;

public class AnnouncementService {

    private final AnnouncementDAO announcementDAO = new AnnouncementDAOImpl();

    public List<Announcement> getAnnouncements(String category, Integer barangayId, String status) {
        return announcementDAO.findAll(category, barangayId, status);
    }

    public List<Announcement> getFilteredAnnouncements(String category, Integer barangayId, Integer municipalityId,
                                                      String status, String search, boolean excludeExpired) {
        return announcementDAO.findFiltered(category, barangayId, municipalityId, status, search, excludeExpired);
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

    public boolean publishAnnouncement(int id, Date publishDate) {
        return announcementDAO.updateStatus(id, "PUBLISHED", publishDate);
    }

    public boolean archiveAnnouncement(int id) {
        return announcementDAO.updateStatus(id, "ARCHIVED", null);
    }

    public boolean deleteAnnouncement(int id) {
        return announcementDAO.delete(id);
    }

    public Map<String, Object> getStatistics() {
        return announcementDAO.getStatistics();
    }
}
