# Projet Cuisine Tunisienne – Full Stack

Ce projet contient :
- Frontend : HTML/CSS/JS (pages publiques + pages admin)
- Backend : Java Spring Boot + MySQL

## ✅ Ce qui est implémenté

- Connexion admin via `/admin` (page login dédiée)
- Authentification JWT (API protégée côté admin)
- Entité SQL `Admin`
- Entité SQL `Reservation`
- Réservation depuis `reservation.html` enregistrée en base
- Gestion des réservations côté admin (`resAdmin.html`) :
  - liste dynamique
  - filtres
  - confirmation/annulation
  - export CSV
- Dashboard admin (`DashboradAdmin.html`) connecté à la base :
  - stats en temps réel
  - graphe des 7 derniers jours

## Structure

- `backend/` : API Spring Boot
- `adminLogin.html` : écran de connexion admin
- `adminLogin.js` : logique de login
- `adminApi.js` : utilitaires API protégée
- `reservation.html` : envoi réservation vers backend
- `resAdmin.html` : gestion réservations admin (live)
- `DashboradAdmin.html` : dashboard admin (live)

## 1) Préparer MySQL

Créer la base et les tables (optionnel, sinon Hibernate les crée automatiquement) :

```sql
SOURCE backend/database/mondelys_db.sql;
```

## 2) Configurer le backend

Fichier : `backend/src/main/resources/application.properties`

Mettre vos valeurs :

- `spring.datasource.username`
- `spring.datasource.password`
- `app.jwt.secret` (clé longue et sécurisée)

## 3) Lancer l’application

```bash
cd backend
mvn spring-boot:run
```

L’application tourne sur :
- `http://localhost:8080`

Le backend sert aussi les fichiers HTML du dossier racine du projet.

### Démarrage rapide (Windows PowerShell)

Si votre MySQL a un mot de passe `root`, utilisez :

```powershell
cd backend
$env:DB_USERNAME="root"
$env:DB_PASSWORD="VOTRE_MOT_DE_PASSE_MYSQL"
mvn spring-boot:run
```

### Démarrage ultra simple (automatique)

Depuis la racine du projet, lancez :

```powershell
./start-project.ps1
```

ou double-cliquez :

- `start-project.bat`

Ce script fait automatiquement :
- vérification Java / Maven / MySQL CLI
- test connexion MySQL
- création DB + tables depuis `backend/database/mondelys_db.sql`
- compilation Maven
- lancement Spring Boot avec variables DB
- ouverture du site dans le navigateur

Ensuite ouvrez :
- `http://localhost:8080/index.html`
- `http://localhost:8080/admin`

## 4) Accès admin

Ouvrir :
- `http://localhost:8080/admin`

Compte admin seed automatiquement au démarrage :
- Email : `admin@mondelys.tn`
- Mot de passe : `Admin2026!`

## API principales

### Public
- `POST /api/reservations` : créer une réservation

### Admin
- `POST /api/admin/auth/login` : login admin
- `GET /api/admin/reservations` : liste réservations
- `PATCH /api/admin/reservations/{id}/status` : changer statut
- `GET /api/admin/dashboard` : statistiques dashboard
- `GET /api/admin/dashboard/weekly-reservations` : graphe hebdomadaire

## Notes

- Les pages admin sont protégées côté frontend : sans token, redirection vers `/admin`.
- Le vrai contrôle d’accès est sur les routes API `/api/admin/**`.
- Toute la logique métier des réservations est stockée en base (pas dans HTML).

## Dépannage

### `Cannot GET /admin` sur `127.0.0.1:5500`

Vous êtes sur Live Server, qui ne connaît pas les routes backend.
Utilisez `http://localhost:8080/admin` (backend Spring Boot lancé).

### `Access denied for user 'root'@'localhost'`

Votre mot de passe MySQL ne correspond pas.
- Vérifiez la connexion MySQL
- Lancez l’app avec `DB_USERNAME` et `DB_PASSWORD` (voir section "Démarrage rapide")
