package com.example.onlinepharmacy.config;

import com.example.onlinepharmacy.model.*;
import com.example.onlinepharmacy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final MedicineRepository medicineRepository;
    private final AddressRepository addressRepository;
    private final PrescriptionRepository prescriptionRepository;

    @Autowired
    public DataInitializer(UserRepository userRepository, CategoryRepository categoryRepository,
                           MedicineRepository medicineRepository, AddressRepository addressRepository,
                           PrescriptionRepository prescriptionRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.medicineRepository = medicineRepository;
        this.addressRepository = addressRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            seedUsers();
            seedCategoriesAndMedicines();
            seedAddressesAndPrescriptions();
            System.out.println(">>> Online Pharmacy Seed Data Successfully Initialized! <<<");
        }
    }

    private void seedUsers() {
        User admin = new User("Dr. Sarah Admin", "admin@pharmacy.com", "admin123", "+1-800-555-0100", "ADMIN", "ACTIVE");
        User pharmacist = new User("Pharm. John Rx", "pharmacist@pharmacy.com", "pharmacy123", "+1-800-555-0101", "PHARMACIST", "ACTIVE");
        User customer = new User("Alex Johnson", "customer@pharmacy.com", "customer123", "+1-800-555-0102", "CUSTOMER", "ACTIVE");

        userRepository.save(admin);
        userRepository.save(pharmacist);
        userRepository.save(customer);
    }

    private void seedCategoriesAndMedicines() {
        Category painRelief = categoryRepository.save(new Category("Pain Relief", "Analgesics, antipyretics and anti-inflammatory medications.", "ACTIVE"));
        Category antibiotics = categoryRepository.save(new Category("Antibiotics", "Prescription antibacterial medicines for infections.", "ACTIVE"));
        Category vitamins = categoryRepository.save(new Category("Vitamins & Supplements", "Multivitamins, minerals, and immunity boosters.", "ACTIVE"));
        Category cardiac = categoryRepository.save(new Category("Cardiac & BP Care", "Cardiovascular health, hypertension, and cholesterol care.", "ACTIVE"));
        Category skincare = categoryRepository.save(new Category("Skincare & Dermatological", "Topical creams, ointments, and acne solutions.", "ACTIVE"));
        Category wellness = categoryRepository.save(new Category("General Wellness", "Digestive aids, cold & flu relievers, first aid.", "ACTIVE"));

        // Medicines
        medicineRepository.save(new Medicine(
            painRelief, "Paracetamol Extra 650mg",
            "Fast-acting pain reliever and fever reducer for headaches and flu symptoms.",
            "HealthCare Pharma", new BigDecimal("12.50"), new BigDecimal("10.00"), 150, false,
            "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=500&auto=format&fit=crop&q=60", "ACTIVE"
        ));

        medicineRepository.save(new Medicine(
            painRelief, "Ibuprofen Softgels 400mg",
            "Anti-inflammatory medication for muscle aches, joint pain, and toothaches.",
            "BioMed Labs", new BigDecimal("18.99"), new BigDecimal("15.00"), 80, false,
            "https://images.unsplash.com/photo-1550572017-edd951aa8f72?w=500&auto=format&fit=crop&q=60", "ACTIVE"
        ));

        medicineRepository.save(new Medicine(
            antibiotics, "Amoxicillin Trihydrate 500mg",
            "Broad-spectrum antibiotic used to treat bacterial infections.",
            "Apex Pharma", new BigDecimal("45.00"), new BigDecimal("0.00"), 50, true,
            "https://images.unsplash.com/photo-1471864190281-a93a3070b6de?w=500&auto=format&fit=crop&q=60", "ACTIVE"
        ));

        medicineRepository.save(new Medicine(
            antibiotics, "Azithromycin 250mg Tablets",
            "Potent antibiotic for respiratory, skin, and ear infections.",
            "Global Rx", new BigDecimal("55.75"), new BigDecimal("12.00"), 40, true,
            "https://images.unsplash.com/photo-1584017911766-d451b3d0e843?w=500&auto=format&fit=crop&q=60", "ACTIVE"
        ));

        medicineRepository.save(new Medicine(
            vitamins, "Vitamin C 1000mg + Zinc",
            "Effervescent immunity booster tablets for daily health and energy.",
            "NutriVital", new BigDecimal("24.99"), new BigDecimal("20.00"), 200, false,
            "https://images.unsplash.com/photo-1577401239170-897942555fb3?w=500&auto=format&fit=crop&q=60", "ACTIVE"
        ));

        medicineRepository.save(new Medicine(
            vitamins, "Omega-3 Fish Oil 1000mg",
            "High potency EPA/DHA softgels supporting heart and brain health.",
            "OceanPure", new BigDecimal("32.00"), new BigDecimal("10.00"), 120, false,
            "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=500&auto=format&fit=crop&q=60", "ACTIVE"
        ));

        medicineRepository.save(new Medicine(
            cardiac, "Atorvastatin 20mg",
            "Cholesterol-lowering statin medication for cardiovascular management.",
            "CardioGuard", new BigDecimal("38.50"), new BigDecimal("5.00"), 90, true,
            "https://images.unsplash.com/photo-1550572017-edd951aa8f72?w=500&auto=format&fit=crop&q=60", "ACTIVE"
        ));

        medicineRepository.save(new Medicine(
            skincare, "Hydrating Hydrocortisone Cream 1%",
            "Relieves skin itching, redness, inflammation, and eczema symptoms.",
            "Dermacare", new BigDecimal("15.20"), new BigDecimal("0.00"), 75, false,
            "https://images.unsplash.com/photo-1607613009820-a29f7bb81c04?w=500&auto=format&fit=crop&q=60", "ACTIVE"
        ));

        medicineRepository.save(new Medicine(
            wellness, "Digital Infrared Thermometer",
            "Non-contact quick body temperature scanner with fever alert display.",
            "MedTech Pro", new BigDecimal("49.99"), new BigDecimal("25.00"), 35, false,
            "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=500&auto=format&fit=crop&q=60", "ACTIVE"
        ));
    }

    private void seedAddressesAndPrescriptions() {
        User customer = userRepository.findByEmail("customer@pharmacy.com").orElse(null);
        if (customer != null) {
            Address address = new Address(
                customer, "Alex Johnson", "+1-800-555-0102",
                "742 Evergreen Terrace", "Suite 4B",
                "Springfield", "OR", "97477", true
            );
            addressRepository.save(address);

            Prescription rx = new Prescription(
                customer,
                "https://images.unsplash.com/photo-1584515979956-d9f6e5d09982?w=500&auto=format&fit=crop&q=60",
                "Rx for Amoxicillin 500mg - Take twice daily after meals for 7 days.",
                "APPROVED"
            );
            prescriptionRepository.save(rx);
        }
    }
}
