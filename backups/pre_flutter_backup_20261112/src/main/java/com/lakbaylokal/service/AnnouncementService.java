package com.lakbaylokal.service;

import com.lakbaylokal.dao.AnnouncementDAO;
import com.lakbaylokal.dao.impl.AnnouncementDAOImpl;
import com.lakbaylokal.model.Announcement;

import java.util.List;

public class AnnouncementService {

    private final AnnouncementDAO announcementDAO = new AnnouncementDAOImpl();

    public List<Announcement> getAnnouncements(String category, Integer barangayId, String status) {
        return announcementDAO.findAll(category, barangayId, status);
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
