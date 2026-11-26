package com.lakbaylokal.dao;

import com.lakbaylokal.model.Announcement;

import java.sql.Date;
import java.util.List;
import java.util.Map;

public interface AnnouncementDAO {
    Announcement findById(int announcementId);
    List<Announcement> findAll(String category, Integer barangayId, String status);
    List<Announcement> findFiltered(String category, Integer barangayId, Integer municipalityId, String status, String search, boolean excludeExpired);
    int create(Announcement announcement);
    boolean update(Announcement announcement);
    boolean updateStatus(int announcementId, String status, Date publishDate);
    boolean delete(int announcementId);
    Map<String, Object> getStatistics();
}
