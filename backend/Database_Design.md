# Online Pharmacy Ordering Platform

# Database Design

## 1. Database Overview

**Database Name:** `online_pharmacy`

The database is used to store and manage:

- User accounts
- Medicine information
- Medicine categories
- Customer addresses
- Shopping carts
- Prescriptions
- Orders
- Order items
- Payments

---

# 2. Database Tables and Their Uses

## 2.1 Users Table

### Table Name: `users`

### Purpose

The `users` table stores the account information of all users in the system.

It can store:

- Customers
- Admins
- Pharmacists

### Table Structure

| Column | Data Type | Key | Purpose |
|---|---|---|---|
| user_id | BIGINT | PK | Unique user ID |
| full_name | VARCHAR(100) | | User name |
| email | VARCHAR(150) | UNIQUE | Login email |
| password | VARCHAR(255) | | User password |
| phone | VARCHAR(15) | | Contact number |
| role | VARCHAR(20) | | CUSTOMER / ADMIN / PHARMACIST |
| status | VARCHAR(20) | | Account status |
| created_at | TIMESTAMP | | Account creation date |

### Used For

```text
Login
Registration
User management
Role management
Customer identification