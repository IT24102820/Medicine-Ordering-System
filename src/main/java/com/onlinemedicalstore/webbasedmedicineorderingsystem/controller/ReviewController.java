package com.onlinemedicalstore.webbasedmedicineorderingsystem.controller;

import com.onlinemedicalstore.webbasedmedicineorderingsystem.dto.ReviewDTO;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.dto.ReviewSubmissionDTO;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.entity.Review;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:8080")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // Endpoint for customers to submit a review
    @PostMapping
    public ResponseEntity<?> submitReview(@RequestBody ReviewSubmissionDTO reviewDto, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("You must be logged in to submit a review.");
        }
        try {
            Review newReview = reviewService.createReview(reviewDto, principal.getName());
            return ResponseEntity.ok(newReview);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // New public endpoint to get approved reviews for a medicine
    @GetMapping("/medicine/{medicineId}")
    public ResponseEntity<List<ReviewDTO>> getApprovedReviews(@PathVariable Long medicineId) {
        return ResponseEntity.ok(reviewService.getApprovedReviewsForMedicine(medicineId));
    }
}
