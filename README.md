# ☽ Carthélys – Architecture Full-Stack

Restaurant Tunisien Gastronomique | Frontend HTML/CSS/JS · Backend Spring Boot · MySQL

---

## Structure du Projet

```
carthelys/
├── admin-carth.html              ← Dashboard responsable (standalone)
│
├── frontend/                     ← Vos fichiers existants (inchangés)
│   ├── index-carth.html
│   ├── menu-carth.html
│   ├── reservation-carth.html
│   ├── contact-carth.html
│   ├── style-carth.css
│   └── main-carth.js
│
└── carthelys-backend/            ← API Spring Boot
    ├── pom.xml
    ├── carthelys_db.sql          ← Script MySQL complet
    └── src/main/java/tn/carthelys/
        ├── CarthelysApplication.java
        ├── model/
        │   ├── Reservation.java
        │   ├── Review.java
        │   ├── ContactMessage.java
        │   └── User.java
        ├── repository/
        │   ├── ReservationRepository.java
        │   ├── ReviewRepository.java
        │   ├── ContactMessageRepository.java
        │   └── UserRepository.java
        ├── service/
        │   ├── ReservationService.java
        │   ├── ReviewService.java
        │   └── ContactService.java
        ├── controller/
        │   ├── ReservationController.java
        │   ├── ReviewController.java
        │   ├── ContactController.java
        │   └── AuthController.java
        ├── config/
        │   ├── SecurityConfig.java    (JWT Filter inclus)
        │   └── JwtService.java
        └── exception/
            ├── GlobalExceptionHandler.java
            └── ResourceNotFoundException.java
```

---

## Installation & Lancement

### 1. Base de données MySQL

```bash
mysql -u root -p < carthelys_db.sql
```

Crée : base `carthelys_db`, toutes les tables, données de test, vues SQL.

**Comptes admin créés :**
| Email | Mot de passe | Rôle |
|-------|-------------|------|
| admin@carthelys.tn | Admin2024! | ADMIN |
| manager@carthelys.tn | Admin2024! | MANAGER |

### 2. Configuration Spring Boot

Modifier `src/main/resources/application.properties` :
```properties
spring.datasource.password=VOTRE_MOT_DE_PASSE_MYSQL
spring.mail.password=VOTRE_MOT_DE_PASSE_SMTP
```

### 3. Lancer le backend

```bash
cd carthelys-backend
./mvnw spring-boot:run
# ou : mvn spring-boot:run
```

L'API démarre sur : **http://localhost:8080**

### 4. Dashboard Admin

Ouvrir `admin-carth.html` dans un navigateur.
→ Le dashboard est autonome et se connecte à l'API via `fetch()`.

---

## API Endpoints

### Authentification
```
POST /api/auth/login
Body: { "email": "admin@carthelys.tn", "password": "Admin2024!" }
→ { "token": "eyJ...", "email": "admin@carthelys.tn" }
```

### Réservations (client)
```
POST /api/reservations          ← créer (public)
```

### Réservations (admin – JWT requis)
```
GET  /api/reservations          ?page=0&size=10&q=nadia&status=EN_ATTENTE
GET  /api/reservations/{id}
PATCH /api/reservations/{id}/status   Body: { "status": "CONFIRMEE" }
PATCH /api/reservations/{id}/note     Body: { "note": "..." }
GET  /api/reservations/stats
```

### Avis (client)
```
POST /api/reviews               ← soumettre (public)
GET  /api/reviews/published     ← affichage site (public)
```

### Avis (admin – JWT requis)
```
GET  /api/reviews               ?page=0&size=12
POST /api/reviews/{id}/respond  Body: { "response": "Merci..." }
PATCH /api/reviews/{id}/read
GET  /api/reviews/stats
```

### Messages (admin – JWT requis)
```
POST /api/contacts              ← formulaire contact (public)
GET  /api/contacts
PATCH /api/contacts/{id}/read
PATCH /api/contacts/{id}/status Body: { "status": "REPLIED" }
DELETE /api/contacts/{id}
```

---

## Intégration Frontend → API

Dans `reservation-carth.html`, remplacer le `setTimeout` par :

```javascript
// Exemple d'appel API depuis le formulaire de réservation
const response = await fetch('http://localhost:8080/api/reservations', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    firstName: document.getElementById('firstName').value,
    lastName:  document.getElementById('lastName').value,
    email:     document.getElementById('email').value,
    phone:     document.getElementById('phone').value,
    reservationDate: document.getElementById('date').value,
    reservationTime: document.getElementById('time').value + ':00',
    guestsCount: parseInt(document.getElementById('guests').value),
    occasion:    document.getElementById('occasion').value,
    preorder:    document.getElementById('preorder').value,
    specialRequests: document.getElementById('requests').value
  })
});
const data = await response.json();
document.getElementById('resRef').textContent = data.reference;
```

Dans le **Dashboard Admin**, authentifier puis utiliser le token :
```javascript
// Login
const res = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});
const { token } = await res.json();
localStorage.setItem('token', token);

// Appel protégé
const reservations = await fetch('/api/reservations', {
  headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
});
```

---

## Technologies

| Couche | Technologie |
|--------|-------------|
| Frontend | HTML5, CSS3, JavaScript ES6+ |
| Backend | Spring Boot 3.2, Java 17 |
| Sécurité | Spring Security + JWT (jjwt) |
| ORM | Spring Data JPA / Hibernate |
| Base de données | MySQL 8.x |
| Validation | Bean Validation (Jakarta) |
| Build | Maven |

---

*☽ Carthélys – Cuisine Tunisienne Gastronomique · Carthage, Tunis*
