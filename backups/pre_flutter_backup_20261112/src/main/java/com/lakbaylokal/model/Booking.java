package com.lakbaylokal.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Booking {
    private int bookingId;
    private String bookingReference;
    private int touristId;
    private Integer packageId;
    private Timestamp bookingDate;
    private Date travelDateStart;
    private Date travelDateEnd;
    private int numberOfPax;
    private BigDecimal totalAmount;
    private String status;
    private String customNotes;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Joined fields
    private String touristName;
    private String touristEmail;
    private String touristPhone;
    private String packageName;
    private List<BookingItem> items = new ArrayList<>();

    public Booking() {}

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

    public int getTouristId() { return touristId; }
    public void setTouristId(int touristId) { this.touristId = touristId; }

    public Integer getPackageId() { return packageId; }
    public void setPackageId(Integer packageId) { this.packageId = packageId; }

    public Timestamp getBookingDate() { return bookingDate; }
    public void setBookingDate(Timestamp bookingDate) { this.bookingDate = bookingDate; }

    public Date getTravelDateStart() { return travelDateStart; }
    public void setTravelDateStart(Date travelDateStart) { this.travelDateStart = travelDateStart; }

    public Date getTravelDateEnd() { return travelDateEnd; }
    public void setTravelDateEnd(Date travelDateEnd) { this.travelDateEnd = travelDateEnd; }

    public int getNumberOfPax() { return numberOfPax; }
    public void setNumberOfPax(int numberOfPax) { this.numberOfPax = numberOfPax; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCustomNotes() { return customNotes; }
    public void setCustomNotes(String customNotes) { this.customNotes = customNotes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getTouristName() { return touristName; }
    public void setTouristName(String touristName) { this.touristName = touristName; }

    public String getTouristEmail() { return touristEmail; }
    public void setTouristEmail(String touristEmail) { this.touristEmail = touristEmail; }

    public String getTouristPhone() { return touristPhone; }
    public void setTouristPhone(String touristPhone) { this.touristPhone = touristPhone; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public List<BookingItem> getItems() { return items; }
    public void setItems(List<BookingItem> items) { this.items = items; }
}
