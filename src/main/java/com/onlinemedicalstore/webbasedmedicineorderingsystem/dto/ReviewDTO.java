package com.onlinemedicalstore.webbasedmedicineorderingsystem.dto;

/**
 * A Data Transfer Object to safely send review details to the frontend
 * without causing serialization loops. It flattens the complex Review object
 * into a simple structure with only the necessary information.
 */
public class ReviewDTO {
    private Long id;
    private int rating;
    private String comment;
    private boolean isApproved;
    private String customerUsername;
    private String medicineName;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public String getCustomerUsername() {
        return customerUsername;
    }

    public void setCustomerUsername(String customerUsername) {
        this.customerUsername = customerUsername;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }
}

