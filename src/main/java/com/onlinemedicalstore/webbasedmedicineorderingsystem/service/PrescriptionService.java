package com.onlinemedicalstore.webbasedmedicineorderingsystem.service;

import com.onlinemedicalstore.webbasedmedicineorderingsystem.dto.PrescriptionDTO;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.entity.Prescription;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.entity.PrescriptionStatus;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.entity.User;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.repository.PrescriptionRepository;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FileStorageService fileStorageService;

    @Transactional
    public Prescription savePrescription(MultipartFile file, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        String filename = fileStorageService.storeFile(file);

        Prescription prescription = new Prescription();
        prescription.setFilename(filename);
        prescription.setUser(user);
        prescription.setUploadDate(LocalDateTime.now());
        prescription.setStatus(PrescriptionStatus.PENDING); // Set default status

        return prescriptionRepository.save(prescription);
    }

    @Transactional(readOnly = true)
    public List<Prescription> getPrescriptionsForUser(String username) {
        return prescriptionRepository.findByUserUsername(username);
    }

    @Transactional
    public Prescription updatePrescription(Long id, MultipartFile file, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Prescription prescription = prescriptionRepository.findById(id).orElseThrow(() -> new RuntimeException("Prescription not found"));

        if (!Objects.equals(prescription.getUser().getId(), user.getId())) {
            throw new AccessDeniedException("You do not have permission to update this prescription.");
        }

        // Delete the old file and store the new one
        fileStorageService.deleteFile(prescription.getFilename());
        String newFilename = fileStorageService.storeFile(file);

        prescription.setFilename(newFilename);
        prescription.setUploadDate(LocalDateTime.now());
        prescription.setStatus(PrescriptionStatus.PENDING); // Reset status on update
        return prescriptionRepository.save(prescription);
    }

    @Transactional
    public void deletePrescription(Long id, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Prescription prescription = prescriptionRepository.findById(id).orElseThrow(() -> new RuntimeException("Prescription not found"));

        if (!Objects.equals(prescription.getUser().getId(), user.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this prescription.");
        }

        fileStorageService.deleteFile(prescription.getFilename());
        prescriptionRepository.delete(prescription);
    }

    // --- Admin Methods ---

    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getAllPrescriptionsAsDTO() {
        return prescriptionRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private PrescriptionDTO convertToDto(Prescription prescription) {
        PrescriptionDTO dto = new PrescriptionDTO();
        dto.setId(prescription.getId());
        dto.setFilename(prescription.getFilename());
        dto.setUploadDate(prescription.getUploadDate());
        dto.setStatus(prescription.getStatus());
        if (prescription.getUser() != null) {
            dto.setCustomerUsername(prescription.getUser().getUsername());
        }
        return dto;
    }

    @Transactional
    public Prescription updatePrescriptionStatus(Long id, PrescriptionStatus status) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));
        prescription.setStatus(status);
        return prescriptionRepository.save(prescription);
    }
}


