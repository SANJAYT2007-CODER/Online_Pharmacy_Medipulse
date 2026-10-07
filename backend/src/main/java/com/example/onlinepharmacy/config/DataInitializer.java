package com.example.onlinepharmacy.config;

import com.example.onlinepharmacy.model.*;
import com.example.onlinepharmacy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final MedicineRepository medicineRepository;
    private final AddressRepository addressRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorPrescriptionRepository doctorPrescriptionRepository;
    private final LabTestRepository labTestRepository;
    private final HealthPackageRepository healthPackageRepository;
    private final HealthBlogRepository healthBlogRepository;

    @Autowired
    public DataInitializer(UserRepository userRepository, CategoryRepository categoryRepository,
                           MedicineRepository medicineRepository, AddressRepository addressRepository,
                           PrescriptionRepository prescriptionRepository, DoctorRepository doctorRepository,
                           DoctorPrescriptionRepository doctorPrescriptionRepository, LabTestRepository labTestRepository,
                           HealthPackageRepository healthPackageRepository, HealthBlogRepository healthBlogRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.medicineRepository = medicineRepository;
        this.addressRepository = addressRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.doctorRepository = doctorRepository;
        this.doctorPrescriptionRepository = doctorPrescriptionRepository;
        this.labTestRepository = labTestRepository;
        this.healthPackageRepository = healthPackageRepository;
        this.healthBlogRepository = healthBlogRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            seedUsers();
            seedCategoriesAndMedicines();
            seedAddressesAndPrescriptions();
            seedDoctors();
            seedLabTests();
            seedHealthPackages();
            seedHealthBlogs();
            seedDoctorPrescriptions();
            System.out.println(">>> Online Pharmacy Seed Data Successfully Initialized! <<<");
        } else {
            ensureDemoUsersVerified();
            // Guarantee 19 reference medicines are populated if missing
            if (medicineRepository.count() < 19) {
                medicineRepository.deleteAll();
                seedCategoriesAndMedicines();
            }
            if (doctorRepository.count() == 0) seedDoctors();
            if (labTestRepository.count() == 0) seedLabTests();
            if (healthPackageRepository.count() == 0) seedHealthPackages();
            if (healthBlogRepository.count() == 0) seedHealthBlogs();
            if (doctorPrescriptionRepository.count() == 0) seedDoctorPrescriptions();
        }
        syncMedicineImages();
    }

    private void ensureDemoUsersVerified() {
        List<String> demoEmails = List.of("admin@pharmacy.com", "pharmacist@pharmacy.com", "customer@pharmacy.com");
        for (String email : demoEmails) {
            userRepository.findByEmail(email).ifPresent(user -> {
                if (!user.isEmailVerified() || !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
                    user.setEmailVerified(true);
                    user.setStatus("ACTIVE");
                    userRepository.save(user);
                }
            });
        }
    }

    private void syncMedicineImages() {
        Map<String, String> imageMap = Map.ofEntries(
            Map.entry("Zofran 4mg", "/images/ondansetron.svg"),
            Map.entry("Kytril 1mg", "/images/granisetron.svg"),
            Map.entry("Anzemet 100mg", "/images/dolasetron.svg"),
            Map.entry("Decadron 4mg", "/images/dexamethasone.svg"),
            Map.entry("Aloxi 0.25mg", "/images/palonosetron.svg"),
            Map.entry("Zyprexa 5mg", "/images/olanzapine.svg"),
            Map.entry("Remeron 15mg", "/images/mirtazapine.svg"),
            Map.entry("Emend 80mg", "/images/aprepitant.svg"),
            Map.entry("Reglan 10mg", "/images/metoclopramide.svg"),
            Map.entry("Ativan 1mg", "/images/lorazepam.svg"),
            Map.entry("Benadryl 25mg", "/images/benadryl.svg"),
            Map.entry("Compazine 5mg", "/images/compazine.svg"),
            Map.entry("Haldol 2mg", "/images/haloperidol.svg"),
            Map.entry("Phenergan 25mg", "/images/phenergan.svg"),
            Map.entry("Levsin 0.125mg", "/images/levsin.svg"),
            Map.entry("Transderm Scop Patch", "/images/transderm.svg"),
            Map.entry("Sandostatin 100mcg", "/images/sandostatin.svg"),
            Map.entry("Vistaril 25mg", "/images/vistaril.svg"),
            Map.entry("Antivert 25mg", "/images/antivert.svg")
        );

        imageMap.forEach((medName, imgUrl) -> {
            medicineRepository.findAll().stream()
                .filter(m -> medName.equalsIgnoreCase(m.getMedicineName()))
                .findFirst()
                .ifPresent(m -> {
                    if (!imgUrl.equals(m.getImageUrl())) {
                        m.setImageUrl(imgUrl);
                        medicineRepository.save(m);
                    }
                });
        });
    }

    private void seedUsers() {
        User admin = new User("Dr. Sarah Admin", "admin@pharmacy.com", "admin123", "+91-9876543210", "ADMIN", "ACTIVE", true);
        User pharmacist = new User("Pharm. John Rx", "pharmacist@pharmacy.com", "pharmacy123", "+91-9876543211", "PHARMACIST", "ACTIVE", true);
        User customer = new User("Alex Johnson", "customer@pharmacy.com", "customer123", "+91-9876543212", "CUSTOMER", "ACTIVE", true);

        userRepository.save(admin);
        userRepository.save(pharmacist);
        userRepository.save(customer);
    }

    private void seedCategoriesAndMedicines() {
        Category antiEmetic = categoryRepository.save(new Category("Anti-emetic & Gastrointestinal", "Medications for nausea, vomiting, motion sickness and digestive care.", "ACTIVE"));
        Category steroid = categoryRepository.save(new Category("Steroids & Anti-inflammatory", "Anti-inflammatory and corticosteroid formulations.", "ACTIVE"));
        Category psych = categoryRepository.save(new Category("Psychiatric & Neurological", "Antipsychotics, antidepressants, and anti-anxiety medications.", "ACTIVE"));
        Category antihistamine = categoryRepository.save(new Category("Antihistamines & Allergy", "Allergy relief and sedative antihistamines.", "ACTIVE"));
        Category general = categoryRepository.save(new Category("General Prescription Care", "Essential prescription and specialty medical care.", "ACTIVE"));

        // 19 Reference Medicines with exact Generic Name, Brand Name, Manufacturer, Location, Price in ₹
        medicineRepository.save(new Medicine(antiEmetic, "Zofran 4mg", "Ondansetron",
                "Prevents nausea and vomiting caused by surgery or chemotherapy.", "Zofran", "GlaxoSmithKline", "Research Triangle Park, NC",
                new BigDecimal("199.00"), new BigDecimal("10.00"), 100, true, "/images/ondansetron.svg", "ACTIVE"));

        medicineRepository.save(new Medicine(antiEmetic, "Kytril 1mg", "Granisetron",
                "Potent 5-HT3 receptor antagonist for nausea prevention.", "Kytril", "Roche Laboratories, Inc.", "Nutley, NJ",
                new BigDecimal("249.00"), new BigDecimal("15.00"), 80, true, "/images/granisetron.svg", "ACTIVE"));

        medicineRepository.save(new Medicine(antiEmetic, "Anzemet 100mg", "Dolasetron",
                "Selective serotonin 5-HT3 receptor antagonist tablet.", "Anzemet", "Aventis Pharmaceuticals", "Bridgewater, NJ",
                new BigDecimal("310.00"), new BigDecimal("5.00"), 60, true, "/images/dolasetron.svg", "ACTIVE"));

        medicineRepository.save(new Medicine(steroid, "Decadron 4mg", "Dexamethasone",
                "Powerful corticosteroid used to reduce inflammation and immune responses.", "Decadron", "Merck & Co., Inc.", "West Point, PA",
                new BigDecimal("85.00"), new BigDecimal("10.00"), 150, true, "/images/dexamethasone.svg", "ACTIVE"));

        medicineRepository.save(new Medicine(antiEmetic, "Aloxi 0.25mg", "Palonosetron",
                "Long-acting second-generation 5-HT3 receptor antagonist.", "Aloxi", "MGI Pharma, Inc., and Helsinn Healthcare", "Bloomington, MN",
                new BigDecimal("450.00"), new BigDecimal("20.00"), 50, true, "/images/palonosetron.svg", "ACTIVE"));

        medicineRepository.save(new Medicine(psych, "Zyprexa 5mg", "Olanzapine",
                "Atypical antipsychotic for schizophrenia and bipolar disorder treatment.", "Zyprexa", "Eli Lilly and Company", "Indianapolis, IN",
                new BigDecimal("175.00"), new BigDecimal("12.00"), 90, true, "/images/olanzapine.svg", "ACTIVE"));

        medicineRepository.save(new Medicine(psych, "Remeron 15mg", "Mirtazapine",
                "Tetracyclic antidepressant for mood elevation and sleep support.", "Remeron", "Organon Inc.", "West Orange, NJ",
                new BigDecimal("130.00"), new BigDecimal("8.00"), 110, true, "/images/mirtazapine.svg", "ACTIVE"));

        medicineRepository.save(new Medicine(antiEmetic, "Emend 80mg", "Aprepitant",
                "Substance P/neurokinin 1 (NK1) receptor antagonist.", "Emend", "Merck & Co., Inc.", "West Point, PA",
                new BigDecimal("520.00"), new BigDecimal("15.00"), 40, true, "/images/aprepitant.svg", "ACTIVE"));

        medicineRepository.save(new Medicine(antiEmetic, "Reglan 10mg", "Metoclopramide",
                "Prokinetic agent that accelerates gastric emptying and stops nausea.", "Reglan", "Baxter Healthcare Corporation", "Glendale, CA",
                new BigDecimal("65.00"), new BigDecimal("0.00"), 200, false, "/images/metoclopramide.svg", "ACTIVE"));

        medicineRepository.save(new Medicine(psych, "Ativan 1mg", "Lorazepam",
                "Benzodiazepine medication for short-term anxiety relief and sedation.", "Ativan", "Baxter Healthcare Corporation", "Glendale, CA",
                new BigDecimal("110.00"), new BigDecimal("10.00"), 75, true, "/images/lorazepam.svg", "ACTIVE"));

        medicineRepository.save(new Medicine(antihistamine, "Benadryl 25mg", "Diphenhydramine",
                "First-generation antihistamine for allergies, motion sickness, and sleep aid.", "Benadryl", "Parke-Davis", "Morris Plains, NJ",
                new BigDecimal("45.00"), new BigDecimal("5.00"), 250, false, "/images/benadryl.svg", "ACTIVE"));

        medicineRepository.save(new Medicine(antiEmetic, "Compazine 5mg", "Prochlorperazine",
                "Phenothiazine anti-emetic used for severe nausea and vertigo.", "Compazine", "GlaxoSmithKline", "Research Triangle Park, NC",
                new BigDecimal("95.00"), new BigDecimal("10.00"), 95, true, "/images/compazine.svg", "ACTIVE"));

        medicineRepository.save(new Medicine(psych, "Haldol 2mg", "Haloperidol",
                "First-generation typical antipsychotic medication.", "Haldol", "Geneva Pharmaceuticals, Inc.", "Princeton, NJ",
                new BigDecimal("140.00"), new BigDecimal("15.00"), 65, true, "/images/haloperidol.svg", "ACTIVE"));

        medicineRepository.save(new Medicine(antihistamine, "Phenergan 25mg", "Promethazine",
                "Antihistamine and sedative for nausea, motion sickness, and allergy symptoms.", "Phenergan", "Wyeth", "Philadelphia, PA",
                new BigDecimal("75.00"), new BigDecimal("0.00"), 130, true, "/images/phenergan.svg", "ACTIVE"));

        medicineRepository.save(new Medicine(general, "Levsin 0.125mg", "Hyoscyamine",
                "Anticholinergic agent for gastrointestinal cramps and IBS relief.", "Levsin", "SchwarzPharma, Inc.", "Mequon, WI",
                new BigDecimal("160.00"), new BigDecimal("10.00"), 85, true, "/images/levsin.svg", "ACTIVE"));

        medicineRepository.save(new Medicine(antiEmetic, "Transderm Scop Patch", "Transdermal Scopolamine",
                "Transdermal patch for prevention of motion sickness and postoperative nausea.", "Transderm Scop", "Novartis Consumer Health, Inc.", "Parsippany, NJ",
                new BigDecimal("290.00"), new BigDecimal("20.00"), 70, false, "/images/transderm.svg", "ACTIVE"));

        medicineRepository.save(new Medicine(general, "Sandostatin 100mcg", "Octreotide",
                "Somatostatin analog for gastrointestinal and endocrine care.", "Sandostatin", "Novartis Consumer Health, Inc.", "Parsippany, NJ",
                new BigDecimal("680.00"), new BigDecimal("25.00"), 30, true, "/images/sandostatin.svg", "ACTIVE"));

        medicineRepository.save(new Medicine(antihistamine, "Vistaril 25mg", "Hydroxyzine",
                "Antihistamine used for itching, allergy relief, and anxiety management.", "Vistaril", "Pfizer Inc.", "New York, NY",
                new BigDecimal("125.00"), new BigDecimal("10.00"), 120, true, "/images/vistaril.svg", "ACTIVE"));

        medicineRepository.save(new Medicine(antihistamine, "Antivert 25mg", "Meclizine",
                "Antihistamine for motion sickness and vertigo symptom control.", "Antivert", "Pfizer Inc.", "New York, NY",
                new BigDecimal("55.00"), new BigDecimal("0.00"), 180, false, "/images/antivert.svg", "ACTIVE"));
    }

    private void seedDoctors() {
        doctorRepository.save(new Doctor("Dr. Sarah Jenkins", "MBBS, MD (Internal Medicine)", "General Physician", 12, new BigDecimal("500.00"), "Today & Tomorrow", "09:00 AM, 11:30 AM, 02:00 PM, 05:30 PM", "/images/doc1.svg", "ACTIVE"));
        doctorRepository.save(new Doctor("Dr. Rajesh Kumar", "MBBS, DM (Cardiology)", "Cardiologist", 18, new BigDecimal("800.00"), "Today & Tomorrow", "10:00 AM, 01:00 PM, 04:00 PM, 07:00 PM", "/images/doc2.svg", "ACTIVE"));
        doctorRepository.save(new Doctor("Dr. Priya Sharma", "MBBS, MD (Dermatology)", "Dermatologist", 10, new BigDecimal("650.00"), "Today & Tomorrow", "11:00 AM, 02:30 PM, 06:00 PM", "/images/doc3.svg", "ACTIVE"));
        doctorRepository.save(new Doctor("Dr. Amit Patel", "MBBS, MD (Pediatrics)", "Pediatrician", 15, new BigDecimal("600.00"), "Today & Tomorrow", "09:30 AM, 12:00 PM, 03:30 PM", "/images/doc4.svg", "ACTIVE"));
        doctorRepository.save(new Doctor("Dr. Ananya Gupta", "MBBS, MS (OB-GYN)", "Gynecologist", 14, new BigDecimal("750.00"), "Today & Tomorrow", "10:30 AM, 02:00 PM, 05:00 PM", "/images/doc5.svg", "ACTIVE"));
        doctorRepository.save(new Doctor("Dr. Vikram Verma", "MBBS, MS (Orthopedics)", "Orthopedic", 16, new BigDecimal("700.00"), "Today & Tomorrow", "11:30 AM, 03:00 PM, 06:30 PM", "/images/doc6.svg", "ACTIVE"));
        doctorRepository.save(new Doctor("Dr. Neha Kapoor", "BDS, MDS (Cosmetic Dentistry)", "Dentist", 9, new BigDecimal("550.00"), "Today & Tomorrow", "10:00 AM, 01:30 PM, 04:30 PM", "/images/doc7.svg", "ACTIVE"));
    }

    private void seedLabTests() {
        labTestRepository.save(new LabTest("Full Body Diagnostic Profile", "Full Body", "64 essential blood tests covering kidney, liver, cholesterol, and blood count.", new BigDecimal("1499.00"), new BigDecimal("50.00"), "10-12 hours fasting required overnight before sample collection.", "/images/lab1.svg"));
        labTestRepository.save(new LabTest("Complete Blood Count (CBC)", "Blood Test", "Measures red blood cells, white blood cells, hemoglobin, and platelets.", new BigDecimal("299.00"), new BigDecimal("20.00"), "No special fasting required.", "/images/lab2.svg"));
        labTestRepository.save(new LabTest("HbA1c Glycated Hemoglobin Test", "Diabetes", "Evaluates average blood sugar control over the past 3 months.", new BigDecimal("499.00"), new BigDecimal("30.00"), "No fasting required.", "/images/lab3.svg"));
        labTestRepository.save(new LabTest("Thyroid Function Profile (T3, T4, TSH)", "Thyroid", "Complete screening for hyperthyroidism and hypothyroidism disorders.", new BigDecimal("599.00"), new BigDecimal("25.00"), "Morning sample preferred.", "/images/lab4.svg"));
        labTestRepository.save(new LabTest("Vitamin D3 & B12 Panel", "Vitamins", "Measures vital vitamin levels for bone strength, nerve health and immunity.", new BigDecimal("899.00"), new BigDecimal("35.00"), "No fasting required.", "/images/lab5.svg"));
        labTestRepository.save(new LabTest("Liver Function Test (LFT)", "Liver", "Evaluates bilirubin, enzymes SGOT/SGPT, protein levels and liver health.", new BigDecimal("699.00"), new BigDecimal("15.00"), "8 hours fasting required.", "/images/lab6.svg"));
        labTestRepository.save(new LabTest("Kidney Function Test (KFT)", "Kidney", "Checks urea, creatinine, uric acid, and electrolytes for renal care.", new BigDecimal("649.00"), new BigDecimal("15.00"), "Fasting recommended.", "/images/lab7.svg"));
        labTestRepository.save(new LabTest("Lipid Profile (Cholesterol Panel)", "Heart", "Measures HDL, LDL, triglycerides and total cardiac cholesterol risks.", new BigDecimal("549.00"), new BigDecimal("20.00"), "12 hours fasting mandatory.", "/images/lab8.svg"));
    }

    private void seedHealthPackages() {
        healthPackageRepository.save(new HealthPackage("Comprehensive Full Body Checkup", "CBC, HbA1c, Lipid Profile, LFT, KFT, Thyroid Profile, Vitamin D & B12, Urine Examination", "Complete diagnostic evaluation for total body wellness and early disease detection.", new BigDecimal("3999.00"), new BigDecimal("1999.00"), "/images/pkg1.svg"));
        healthPackageRepository.save(new HealthPackage("Basic Wellness Health Checkup", "CBC, Blood Sugar Fasting, Lipid Profile, Kidney Function Test, Urine Analysis", "Essential routine health check for active adults and preventive screening.", new BigDecimal("1799.00"), new BigDecimal("899.00"), "/images/pkg2.svg"));
        healthPackageRepository.save(new HealthPackage("Advanced Diabetes Care Package", "HbA1c, Fasting & PP Glucose, Lipid Profile, Kidney Function Test, Microalbuminuria", "Specialized screening for diabetic patients to monitor organ wellness.", new BigDecimal("2399.00"), new BigDecimal("1199.00"), "/images/pkg3.svg"));
        healthPackageRepository.save(new HealthPackage("Executive Heart Health Checkup", "Lipid Profile, High-Sensitivity CRP, ECG, HbA1c, Kidney Function, Cardiac Biomarkers", "Complete cardiovascular health screening to assess heart disease risks.", new BigDecimal("4999.00"), new BigDecimal("2499.00"), "/images/pkg4.svg"));
        healthPackageRepository.save(new HealthPackage("Comprehensive Women's Health Checkup", "Thyroid Profile, Pap Smear, Complete Blood Count, Calcium & Vit D, Iron Profile, Hormone Panel", "Tailored health evaluation designed for women's reproductive & bone wellness.", new BigDecimal("4399.00"), new BigDecimal("2199.00"), "/images/pkg5.svg"));
        healthPackageRepository.save(new HealthPackage("Senior Citizen Care Package", "Full Body Profile, Bone Density Screening, Cardiac Risk Indicators, Prostate/Mammogram Indicator", "Holistic health assessment for seniors to support active aging and vitality.", new BigDecimal("4599.00"), new BigDecimal("2299.00"), "/images/pkg6.svg"));
    }

    private void seedHealthBlogs() {
        healthBlogRepository.save(new HealthBlog(
                "10 Essential Tips for Managing Blood Sugar Naturally",
                "Learn evidence-based dietary habits, low glycemic foods, and lifestyle modifications to maintain optimal glucose levels.",
                "Maintaining steady blood sugar levels is vital for preventing type 2 diabetes and sustaining long-term energy. Incorporate high-fiber vegetables, lean proteins, and daily 30-minute brisk walks. Avoid sugary sodas and processed carbohydrates. Regular testing with HbA1c provides clear guidance on your metabolic progress.",
                "Dr. Sarah Jenkins", "Sep 15, 2026", "Diabetes", "/images/blog1.svg"
        ));
        healthBlogRepository.save(new HealthBlog(
                "Understanding Antibiotic Resistance: What You Should Know",
                "Discover why taking prescription antibiotics correctly matters for your immune health and global medicine safety.",
                "Antibiotics are powerful lifesaving medicines designed specifically for bacterial infections. Taking them for viral colds or stopping courses early allows resistant bacteria to survive. Always consult certified pharmacists and physicians before starting or stopping any prescription regimen.",
                "Pharm. John Rx", "Sep 12, 2026", "Medicine", "/images/blog2.svg"
        ));
        healthBlogRepository.save(new HealthBlog(
                "Top Superfoods to Boost Immune System & Daily Energy",
                "Incorporate Vitamin C, Omega-3 fatty acids, and zinc-rich foods into your daily meals for vibrant health.",
                "Nutrient-dense foods provide the building blocks for immune cells. Citrus fruits, dark leafy greens, almonds, seeds, and wild salmon offer essential antioxidants. Combine balanced nutrition with 7-8 hours of restful sleep for maximum stamina.",
                "NutriVital Care", "Sep 10, 2026", "Nutrition", "/images/blog3.svg"
        ));
        healthBlogRepository.save(new HealthBlog(
                "How 30 Minutes of Daily Exercise Protects Heart Health",
                "Simple cardiovascular exercises lower bad cholesterol, reduce blood pressure, and strengthen cardiac muscle.",
                "Cardiovascular health improves significantly with moderate aerobic activity. Swimming, cycling, or brisk walking for 150 minutes a week improves arterial flexibility and lowers LDL cholesterol. Pair daily movement with heart-healthy omega-3 supplements.",
                "Dr. Rajesh Kumar", "Sep 08, 2026", "Heart Health", "/images/blog4.svg"
        ));
        healthBlogRepository.save(new HealthBlog(
                "Mental Wellness in a Fast-Paced World: Practical Mindfulness",
                "Reduce stress, improve sleep quality, and foster mental resilience with simple mindfulness and breathing techniques.",
                "Mental health is deeply connected to physical health. Practicing 10 minutes of deep diaphragmatic breathing, disconnecting from digital screens an hour before bed, and prioritizing outdoor daylight exposure regulates cortisol and melatonin production naturally.",
                "Dr. Ananya Gupta", "Sep 05, 2026", "Mental Wellness", "/images/blog5.svg"
        ));
    }

    private void seedDoctorPrescriptions() {
        User customer = userRepository.findByEmail("customer@pharmacy.com").orElse(null);
        if (customer != null) {
            DoctorPrescription rx = new DoctorPrescription(
                    customer, "Dr. Sarah Jenkins", "General Physician",
                    "Alex Johnson", "Sep 14, 2026", "Post-op anti-nausea and anti-inflammatory regimen.", "Verified"
            );

            List<PrescriptionItem> items = new ArrayList<>();
            items.add(new PrescriptionItem("Zofran 4mg (Ondansetron)", "1 tablet", "Twice daily", "5 days", "Take after food for nausea control.", "After food", new BigDecimal("199.00"), 1L));
            items.add(new PrescriptionItem("Decadron 4mg (Dexamethasone)", "1 tablet", "Once daily", "3 days", "Take with morning meal.", "After food", new BigDecimal("85.00"), 4L));
            items.add(new PrescriptionItem("Reglan 10mg (Metoclopramide)", "1 tablet", "Three times daily", "3 days", "Take 30 mins before meals.", "Before food", new BigDecimal("65.00"), 9L));

            rx.setItems(items);
            doctorPrescriptionRepository.save(rx);
        }
    }

    private void seedAddressesAndPrescriptions() {
        User customer = userRepository.findByEmail("customer@pharmacy.com").orElse(null);
        if (customer != null) {
            Address address = new Address(
                customer, "Alex Johnson", "+91-9876543212",
                "742 Evergreen Terrace", "Suite 4B",
                "Springfield", "OR", "97477", true
            );
            addressRepository.save(address);

            Prescription rx = new Prescription(
                customer,
                "/images/placeholder-medicine.svg",
                "Uploaded Rx for Ondansetron 4mg & Decadron 4mg - 5 days course.",
                "APPROVED"
            );
            prescriptionRepository.save(rx);
        }
    }
}
