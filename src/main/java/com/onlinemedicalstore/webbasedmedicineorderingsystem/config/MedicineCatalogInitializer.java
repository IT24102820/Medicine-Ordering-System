package com.onlinemedicalstore.webbasedmedicineorderingsystem.config;

import com.onlinemedicalstore.webbasedmedicineorderingsystem.entity.Medicine;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.repository.MedicineRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Configuration
public class MedicineCatalogInitializer {

    private static final List<CatalogMedicine> CATALOG = List.of(
            new CatalogMedicine("Paracetamol 500 mg", "For temporary relief of mild pain and fever. Pack of 20 tablets.", 120, "45.00", "/images/paracetamol.jpg"),
            new CatalogMedicine("Cetirizine 10 mg", "An antihistamine commonly used to relieve allergy symptoms. Pack of 10 tablets.", 80, "60.00", "/images/cetirizine.jpg"),
            new CatalogMedicine("Oral Rehydration Salts", "Electrolyte powder for preparing an oral rehydration solution. Pack of 10 sachets.", 100, "100.00", "/images/ors.jpg"),
            new CatalogMedicine("Ibuprofen 200 mg", "For temporary relief of minor aches, pain, and fever. Pack of 20 tablets.", 65, "75.00", "/images/ibuprofen.jpg"),
            new CatalogMedicine("Vitamin C 500 mg", "Vitamin C supplement to support normal immune function. Pack of 20 tablets.", 90, "150.00", "/images/vitamin-c.jpg"),
            new CatalogMedicine("Antacid Chewable Tablets", "Chewable tablets for short-term relief from heartburn and indigestion. Pack of 20.", 75, "120.00", "/images/antacid.jpg")
    );

    @Bean
    CommandLineRunner seedMedicineCatalog(MedicineRepository repository) {
        return args -> {
            List<Medicine> existing = repository.findAll().stream()
                    .sorted(Comparator.comparing((Medicine medicine) -> medicine.getId()))
                    .toList();

            boolean containsPlaceholder = existing.stream().anyMatch(medicine ->
                    List.of("abc", "hh", "gg", "jsjsj").contains(medicine.getName().toLowerCase()));
            boolean usesRemoteCatalogImages = existing.stream().anyMatch(medicine ->
                    medicine.getPhotoFilename() != null && medicine.getPhotoFilename().startsWith("http"));

            if (!containsPlaceholder && !usesRemoteCatalogImages) {
                return;
            }

            for (int index = 0; index < CATALOG.size(); index++) {
                Medicine medicine = index < existing.size() ? existing.get(index) : new Medicine();
                CatalogMedicine catalogMedicine = CATALOG.get(index);
                medicine.setName(catalogMedicine.name());
                medicine.setDescription(catalogMedicine.description());
                medicine.setStockQuantity(catalogMedicine.stockQuantity());
                medicine.setPrice(new BigDecimal(catalogMedicine.price()));
                medicine.setExpiryDate(LocalDate.now().plusYears(2).plusMonths(index));
                medicine.setPhotoFilename(catalogMedicine.imageUrl());
                repository.save(medicine);
            }
        };
    }

    private record CatalogMedicine(String name, String description, int stockQuantity, String price, String imageUrl) {
    }
}
