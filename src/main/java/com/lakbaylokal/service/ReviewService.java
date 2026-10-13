package com.lakbaylokal.service;

import com.lakbaylokal.dao.ReviewDAO;
import com.lakbaylokal.dao.impl.ReviewDAOImpl;
import com.lakbaylokal.model.Review;

import java.util.List;

public class ReviewService {

    private final ReviewDAO reviewDAO = new ReviewDAOImpl();

    public List<Review> getReviewsForTarget(String targetType, int targetId) {
        return reviewDAO.findByTarget(targetType, targetId);
    }

    public List<Review> getReviewsByBooking(int bookingId) {
        return reviewDAO.findByBookingId(bookingId);
    }

    public List<Review> getAllReviews() {
        return reviewDAO.findAll();
    }

    public int addReview(Review review) {
        return reviewDAO.create(review);
    }

    public boolean deleteReview(int reviewId) {
        return reviewDAO.delete(reviewId);
    }
}
