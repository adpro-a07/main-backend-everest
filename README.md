# PerbaikiinAja Main Resource Backend - Everest

## Table of Contents
- [About the Project](#about-the-project)
- [Team Members](#team-members)
- [System Architecture](#system-architecture)
  - [Shared Diagrams](#shared-diagrams)
    - [Context Diagram](#context-diagram)
    - [Container Diagram](#container-diagram)
    - [Deployment Diagram](#deployment-diagram)
  - [Feature-Specific Diagrams](#feature-specific-diagrams)
    - [Request Repair Orders](#request-repair-orders)
    - [Accept and Confirm Repair Orders](#accept-and-confirm-repair-orders)
    - [Manage Reviews and Ratings](#manage-reviews-and-ratings)
    - [View Reports and Manage Coupons](#view-reports-and-manage-coupons)
    - [Manage Payment Methods](#manage-payment-methods)
- [Future Architecture](#future-architecture)

## About the Project
PerbaikiinAja is a repair service management system with a backend codenamed "Everest". This platform facilitates repair orders, service provider management, customer reviews, and payment processing.

## Team Members
**Kelompok A07**
- Arditheus Immanuel Hanfree (2206083451) - *Manage Payment Methods*
- Yudayana Arif Prasojo (2306215160) - *Request Repair Orders*
- Khansa Khairunisa (2306152462) - *Manage Reviews and Ratings*
- Danniel (2306152090) - *Accept and Confirm Repair Orders*
- Muhammad Farid Hasabi (2306152512) - *View Reports and Manage Coupons*

## System Architecture

### Shared Diagrams

#### Context Diagram
![Context Diagram](https://github.com/user-attachments/assets/94235161-6a3a-4ee9-9308-36c018654e5d)

#### Container Diagram
![image](https://github.com/user-attachments/assets/0d706f9e-faab-4c3d-b4d0-e52867db3fd4)

#### Deployment Diagram
![image](https://github.com/user-attachments/assets/875fac09-9715-4f3a-8a45-032fbc3fc031)

### Feature-Specific Diagrams

#### Request Repair Orders
*Developed by: Yudayana Arif Prasojo*
- Component Diagram
  ![image](https://github.com/user-attachments/assets/f4cb258b-e046-40a8-8acd-68eb6fad8ec9)

- Code Diagram
  ![image](https://github.com/user-attachments/assets/8aad545f-9eb6-47d1-b661-916b540abd5a)


#### Accept and Confirm Repair Orders
*Developed by: Danniel*
- Component Diagram
- Code Diagram

#### Manage Reviews and Ratings
*Developed by: Khansa Khairunisa*
- Component Diagram
  ![Rating Component Diagram](src/main/resources/diagram/assets/rating_component_diagram.png)
- Code Diagram
  ![Rating Code Diagram](src/main/resources/diagram/assets/rating_code_diagram.png)

#### View Reports and Manage Coupons
*Developed by: Muhammad Farid Hasabi*
- Component Diagram
- Code Diagram

#### Manage Payment Methods
*Developed by: Arditheus Immanuel Hanfree*
- Component Diagram
- Code Diagram

## Future Architecture

### Future Architecture

Untuk mempersiapkan sistem yang lebih modular dan maintainable, arsitektur aplikasi PerbaikiinAja dirancang ulang dengan pendekatan container-based. Perubahan utama yang dilakukan antara lain:

- **API Gateway:** Ditambahkan sebagai pintu masuk utama untuk semua request dari client, memudahkan routing dan security management
- **Containerization:** Frontend, Auth Service, dan Main Resource Service dijalankan dalam Docker container terpisah yang dikelola oleh Docker Compose
- **RabbitMQ:** Dipersiapkan untuk menangani komunikasi asynchronous untuk fitur notifikasi di masa depan
- **Persistent Storage:** Volume terpisah untuk data persistensi setiap service

### Keuntungan dari Arsitektur Ini:

- **Modularitas Sederhana:** Pemisahan layanan dalam container memudahkan pengelolaan tanpa berlebihan kompleks
- **Kemudahan Deployment:** Docker Compose memungkinkan deployment yang konsisten dan mudah direplikasi
- **Fleksibilitas Scaling:** Setiap container dapat di-scale secara independen sesuai kebutuhan
- **Persiapan untuk Fitur Baru:** RabbitMQ siap digunakan ketika fitur notifikasi diimplementasikan
- **Isolasi yang Jelas:** Setiap layanan memiliki batas dan tanggung jawab yang jelas