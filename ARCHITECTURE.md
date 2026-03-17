# 🏗️ Architecture Backend - Cook Mobile App (Cuisin'Voisin)

Ce document décrit l'architecture de référence pour le backend de l'application Cook Mobile, conçue pour être évolutive, maintenable et robuste.

## 🚀 Principes Fondamentaux

L'architecture repose sur trois piliers majeurs :
1. **Clean Architecture (Hexagonale)** : Indépendance vis-à-vis des frameworks et des bases de données.
2. **Domain-Driven Design (DDD)** : Alignement du code sur les concepts métier.
3. **Modular Monolith** : Séparation claire des contextes (Bounded Contexts) pour faciliter une future transition vers des microservices.

---

## 📁 Structure du Projet (Modular Monolith)

Le projet est organisé en modules métier indépendants :

```text
com.cuisinvoisin.backend
├── common (Code partagé, exceptions de base, utilitaires)
├── modules
│   ├── auth (Gestion des sessions, OTP, JWT)
│   ├── users (Client, Cook, DeliveryDriver)
│   ├── catalogue (Plats, Catégories, Options)
│   ├── orders (Commandes, Panier, Suivi)
│   ├── payments (Transactions, Payouts)
│   └── notifications (Email, SMS, Push)
└── infrastructure (Configuration globale, Sécurité, Persistence)
```

### Structure interne d'un module (Hexagonale) :

Chaque module suit cette structure interne :
- `domain` : Entités, Objets de valeur (Records Java 21), Interfaces de Repository.
- `application` : Use Cases, Services, Command/Query handlers.
- `infrastructure` : Implémentations JPA, Adaptateurs API externes.
- `web` : Controllers REST, DTOs, Mappers.

---

## 🛠️ Stack Technique

- **Langage** : Java 21 (Utilisation des Virtual Threads pour la scalabilité).
- **Framework** : Spring Boot 3.4+.
- **Base de données** : PostgreSQL (Relationnel pour la cohérence forte).
- **Sécurité** : Spring Security + JWT.
- **Communication** : REST pour le synchrone, Spring Events/RabbitMQ pour l'asynchrone.
- **Documentation** : OpenAPI / Swagger.

---

## 🎨 Design Patterns & Meilleures Pratiques

### 1. CQRS (Command Query Responsibility Segregation)
Séparation des opérations de lecture (Query) et d'écriture (Command) pour optimiser les performances et la clarté du code.

### 2. Domain Events & Outbox Pattern
Pour assurer la cohérence entre les modules (ex: notifier le client quand sa commande est acceptée) sans couplage fort, nous utilisons des événements de domaine. L'Outbox pattern garantit qu'un événement n'est envoyé que si la transaction en base de données réussit.

### 3. Saga Pattern (Choreography)
Gestion des transactions distribuées entre modules (ex: Commande -> Paiement -> Notification).

### 4. Meilleures pratiques Java 21
- **Records** pour les DTOs et les Value Objects (Immuabilité).
- **Pattern Matching** pour simplifier la logique métier.
- **Virtual Threads** pour gérer un grand nombre de requêtes concurrentes avec peu de ressources.

---

## 📊 Conception des Données (DDD)

### Bounded Contexts :
- **Catalogue** : Gère l'offre des chefs.
- **Ordering** : Gère le cycle de vie d'une commande.
- **Identity** : Gère les profils et l'authentification.
- **Wallet** : Gère les paiements et les reversements aux chefs.

---

## 🛡️ Sécurité & Robustesse

- **Validation** : Bean Validation (JSR 380) à tous les niveaux.
- **Observabilité** : Micrometer + Prometheus + Grafana.
- **Tests** : JUnit 5, Mockito, et Testcontainers pour les tests d'intégration réels.
