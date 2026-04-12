# 🦷 DentalCare Management System

<p align="center">
  <img src="https://github.com/user-attachments/assets/3058daff-3dc9-449c-99d8-004219c5d939" width="600"/>
</p>

## ⚠️ Disclaimer

The mention of brands such as Oral-B and Crest is for illustrative purposes only.
This project is not affiliated with, sponsored by, or endorsed by any of these companies.
All brand references are used as part of a UI demonstration.


## 📌 Overview

DentalCare is a desktop-based Java application developed to manage dental clinic operations efficiently.
The system supports multiple user roles, each with a customized interface, workflow, and permissions.

---

## 🎨 User Interface

* Each staff member has a **unique design and theme**
* Animated UI using **GIFs**
* Dynamic layout:

  * Navigation panels
  * Moving visual elements
  * Personalized dashboards per role
* Includes a **daily changing tip**
* Logout returns the user to the main public page

---

## 🏠 Main Public Page

* Animated main screen
* Feedback option
* Search bar
* Daily tip display
* Direct contact with secretary for appointment booking
* Login options:

  * Patient
  * Staff

---

## 👤 Patient Module

### Features

* View personal profile
* View upcoming appointments
* View issued payments
* Book appointments
* Manage appointments (based on date)
* View treatment history

### Additional Data

* Medical history
* Dental history
* Insurance details

### Logic

* Each patient initially has sample data
* Can add new appointments
* Management works only for future appointments
* Based on **appointment date**

---

## 🧑‍💼 Secretary Module

### Features

* Book appointments for patients
* Handle urgent appointments (auto current time)
* Prevent double booking (filter unavailable time slots)
* Confirm appointments (paid / unpaid)
* View pending appointments
* Send sterilization requests
* View alerts:

  * Low stock
  * Expiring items

### Notes

* Includes demo feature: *Calls to Return*

---

## 👨‍⚕️ Dentist Module

### Dashboard

* Messages
* Upcoming appointments
* Personal information
* Animated panels

### Treatment Plans

* Create treatment plans
* Add treatments automatically
* Automatic cost calculation
* Edit plans
* View:

  * Active plans
  * Completed plans
* Complete plans → moved automatically

### Reports (JasperReports)

* Generate **Invoice (PDF)**
* Generate **Treatment Progress Report (for all patients)**

### Logic

* Plan flow based on **status**

---

## 🧑‍💼 Clinic Manager Module

### Dashboard

* Personal data
* Messages
* Revenue information
* Customized design

### Features

* Manage inventory (Add / Update / Delete)
* Manage suppliers
* Import monthly data from XML *(not fully working)*
* Access doctor features
* Generate reports
* Export data

### Reports (JasperReports)

* Revenue Report
* Inventory Usage Report

### Data Export

* Export appointments report using **JSON**

---

## 📊 Technologies Used

* Java
* Java Swing (GUI)
* Microsoft Access Database
* JDBC (UCanAccess)
* JasperReports (for generating reports such as invoices, revenue reports, and treatment progress reports)
* XML (for importing inventory data)
* JSON (for exporting appointment reports)

---

## 📂 Project Structure

* `src/` → source code
* `resources/` → images, GIFs, reports, database
* `bin/` → compiled files (not included in GitHub)
* `lib/` → external libraries

---

## ▶️ How to Run

1. Download the full project files from the Google Drive link provided below

2. Extract the project folder

3. Open the project using Eclipse IDE

4. Make sure all required libraries are added (located in the `lib/` folder)

5. Ensure the database and resource files are in their correct locations

6. Run the project:

   - Open the main class (e.g., `Main.java`)
   - Right-click → Run As → Java Application

---

## ⚠️ Notes

- The project is intended to run within Eclipse IDE
- All required resources (database, reports, XML files) are included in the Google Drive folder
- Some features (such as XML import) depend on file path configuration

---

## 🔗 Full Project Files

Due to GitHub upload limitations, the full project files ( with resources) are available here:


👉 Full Project Files:
https://drive.google.com/file/d/1M5EmAmC2wqOB-RwKmCPRknaWjcgHSh1P/view?usp=sharing

---

## ⚠️ Notes

* Patient management is based on date
* Dentist management is based on plan status
* Some parts are demo features
* Developed under academic constraints and time pressure

---

## 👩‍💻 Author

Developed as part of an academic project for dental clinic management.


## 📌 Inspiration & Credits

This project is inspired by  (https://www.dentalcare.com/en-us) .
Some images used in this project are taken from publicly available sources for demonstration purposes only.

All rights belong to their respective owners.  
