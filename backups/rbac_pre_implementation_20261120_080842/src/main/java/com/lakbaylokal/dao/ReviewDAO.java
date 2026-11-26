package com.lakbaylokal.dao;

import com.lakbaylokal.model.Review;

import java.util.List;

public interface ReviewDAO {
    List<Review> findByTarget(String targetType, int targetId);
    List<Review> findByBookingId(int bookingId);
    List<Review> findAll();
    int create(Review review);
    boolean delete(int reviewId);
    boolean hasUserReviewed(int touristId, int bookingId, String targetType, int targetId);
}
