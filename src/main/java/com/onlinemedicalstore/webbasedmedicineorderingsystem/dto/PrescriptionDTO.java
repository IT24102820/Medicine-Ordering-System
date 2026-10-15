package com.onlinemedicalstore.webbasedmedicineorderingsystem.dto;

import com.onlinemedicalstore.webbasedmedicineorderingsystem.entity.PrescriptionStatus;
import java.time.LocalDateTime;

/**
 * A Data Transfer Object to safely send prescription details to the frontend
 * without causing serialization loops.
 */
public class PrescriptionDTO {
    private Long id;
    private String filename;
    private LocalDateTime uploadDate;
    private PrescriptionStatus status;
    private String customerUsername;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
    public PrescriptionStatus getStatus() { return status; }
    public void setStatus(PrescriptionStatus status) { this.status = status; }
    public String getCustomerUsername() { return customerUsername; }
    public void setCustomerUsername(String customerUsername) { this.customerUsername = customerUsername; }
}

