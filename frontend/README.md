# 🎨 MediPulse Frontend

This directory contains all the **Frontend** files for the MediPulse Online Pharmacy & Healthcare Platform.

---

## 📁 Directory Structure

```
frontend/
├── static/
│   ├── css/
│   │   └── styles.css          # Core design system, glassmorphism, responsive styles
│   ├── js/
│   │   └── app.js              # Realtime Supabase integration, state management & UI logic
│   └── images/                 # SVG icons & medicine/category assets (79+ images)
├── templates/
│   ├── index.html              # Main dashboard view (Medicines, Doctors, Labs, Cart, Orders)
│   ├── login.html              # Authentication portal (Login / Register / Quick Login)
│   ├── reset-password.html     # Password recovery view
│   └── verify-email.html       # Email verification landing page
├── package.json                # Frontend package descriptor
└── README.md                   # Frontend documentation
```

---

## ✨ Features & Technology Stack

- **HTML5 & Thymeleaf**: Server-side rendered views integrated with dynamic UI templates.
- **Vanilla CSS3**: Modern styling featuring glassmorphism, flexbox/grid, CSS variables, dark/light theme accents, and responsive layout primitives.
- **Vanilla JavaScript & Supabase JS SDK**:
  - Direct browser integration with Supabase WebSockets for Realtime updates on:
    - **Orders Queue**: Live order status changes across Customer and Pharmacist dashboards.
    - **Stock Updates**: Instant updates on inventory stock levels.
    - **Prescription Status**: Live feedback when prescriptions are reviewed or uploaded.
  - Multi-role simulation (Customer, Pharmacist, Admin).
  - Dynamic cart management, order checkout, prescription uploads, and search filtering.

---

## 🚀 Running / Developing Frontend

1. **Integrated with Spring Boot Backend**:
   - The files in `frontend/` are synchronized with the Spring Boot server running inside `backend/`.
   - Start the backend server (`cd backend && .\mvnw.cmd spring-boot:run`) and open `http://localhost:8081`.

2. **Standalone Preview**:
   - Run `npx serve static` inside the `frontend` folder to preview static assets.
