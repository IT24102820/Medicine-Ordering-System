package com.onlinemedicalstore.webbasedmedicineorderingsystem.controller;

import com.onlinemedicalstore.webbasedmedicineorderingsystem.entity.Medicine;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.service.FileStorageService;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "http://localhost:8080")
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = fileStorageService.loadFileAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @ResponseStatus(HttpStatus.CREATED)
    public Medicine addMedicine(@RequestPart("medicine") Medicine medicine, @RequestPart(value="photo", required=false) MultipartFile photo) {
        if (photo != null && !photo.isEmpty()) {
            String filename = fileStorageService.storeFile(photo);
            medicine.setPhotoFilename(filename);
        }
        return medicineService.addMedicine(medicine);
    }

    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Medicine> updateMedicine(
            @PathVariable Long id,
            @RequestPart("medicine") Medicine medicineDetails,
            @RequestPart(value="photo", required=false) MultipartFile photo
    ) {
        if (photo != null && !photo.isEmpty()) {
            String filename = fileStorageService.storeFile(photo);
            medicineDetails.setPhotoFilename(filename);
        }
        Medicine updatedMedicine = medicineService.updateMedicine(id, medicineDetails);
        return ResponseEntity.ok(updatedMedicine);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicine(@PathVariable Long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<Medicine> getAllMedicines() {
        return medicineService.getAllMedicines();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medicine> getMedicineById(@PathVariable Long id) {
        Medicine medicine = medicineService.getMedicineById(id);
        return ResponseEntity.ok(medicine);
    }
}



