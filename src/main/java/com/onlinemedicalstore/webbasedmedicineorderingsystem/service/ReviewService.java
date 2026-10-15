package com.onlinemedicalstore.webbasedmedicineorderingsystem.service;

import com.onlinemedicalstore.webbasedmedicineorderingsystem.dto.ReviewDTO;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.dto.ReviewSubmissionDTO;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.entity.Medicine;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.entity.Review;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.entity.User;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.repository.MedicineRepository;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.repository.ReviewRepository;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MedicineRepository medicineRepository;

    @Transactional(readOnly = true)
    public List<ReviewDTO> getAllReviewsAsDTO() {
        return reviewRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // New method to get approved reviews for the store page
    @Transactional(readOnly = true)
    public List<ReviewDTO> getApprovedReviewsForMedicine(Long medicineId) {
        return reviewRepository.findByMedicineIdAndIsApprovedTrue(medicineId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ReviewDTO convertToDto(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setApproved(review.isApproved());
        if (review.getUser() != null) {
            dto.setCustomerUsername(review.getUser().getUsername());
        }
        if (review.getMedicine() != null) {
            dto.setMedicineName(review.getMedicine().getName());
        }
        return dto;
    }

    @Transactional
    public Review createReview(ReviewSubmissionDTO reviewDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Medicine medicine = medicineRepository.findById(reviewDto.getMedicineId())
                .orElseThrow(() -> new RuntimeException("Medicine not found"));

        Review review = new Review();
        review.setUser(user);
        review.setMedicine(medicine);
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());
        review.setApproved(false); // All reviews must be manually approved

        return reviewRepository.save(review);
    }

    @Transactional
    public Review updateReviewApproval(Long reviewId, boolean isApproved) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
        review.setApproved(isApproved);
        return reviewRepository.save(review);
    }
}
