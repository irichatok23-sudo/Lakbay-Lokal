package com.lakbaylokal.dao;

import com.lakbaylokal.model.Announcement;

import java.util.List;

public interface AnnouncementDAO {
    Announcement findById(int announcementId);
    List<Announcement> findAll(String category, Integer barangayId, String status);
    int create(Announcement announcement);
    boolean update(Announcement announcement);
    boolean delete(int announcementId);
}
