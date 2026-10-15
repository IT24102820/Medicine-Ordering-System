package com.onlinemedicalstore.webbasedmedicineorderingsystem.repository;

import com.onlinemedicalstore.webbasedmedicineorderingsystem.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // New method to fetch only approved reviews for a specific medicine
    List<Review> findByMedicineIdAndIsApprovedTrue(Long medicineId);
}
