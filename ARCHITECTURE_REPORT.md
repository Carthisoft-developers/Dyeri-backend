# 📄 Rapport d'Architecture Logicielle - Cook Mobile App

Ce rapport présente l'architecture conçue pour l'application Cook Mobile (Cuisin'Voisin), en mettant l'accent sur la robustesse, la scalabilité et les meilleures pratiques modernes.

## 🏗️ Backend (Spring Boot 3.4 + Java 21)

L'architecture backend a été initialisée dans le dépôt `Dyeri-backend` avec les caractéristiques suivantes :

### 1. Modèle Architectural : Modular Monolith
Nous avons choisi un **Monolithe Modulaire** pour équilibrer la simplicité de déploiement et la séparation des préoccupations. Chaque domaine métier (Users, Catalogue, Orders, Payments, Notifications) est isolé dans son propre module.

### 2. Clean Architecture (Hexagonale)
Chaque module suit une structure interne rigoureuse :
- **Domain** : Cœur métier (Entités, Records Java 21 pour l'immuabilité).
- **Application** : Logique de cas d'utilisation (Use Cases).
- **Infrastructure** : Détails techniques (Persistance JPA, Sécurité).
- **Web** : Interface REST (Controllers, DTOs).

### 3. Innovations Java 21
- **Virtual Threads** : Configurés pour une scalabilité massive des entrées/sorties.
- **Records** : Utilisés pour les DTOs afin de garantir l'immuabilité et la clarté.
- **Pattern Matching** : Utilisé dans la logique de service pour une syntaxe plus propre.

### 4. Systèmes Distribués & Patterns
- **CQRS** : Séparation des lectures et écritures.
- **Outbox Pattern** : Préparé pour une communication asynchrone fiable entre modules via des événements.
- **Saga Pattern** : Recommandé pour la gestion des transactions complexes (ex: Commande -> Paiement).

---

## 📱 Frontend (Flutter) - Recommandations Architecturales

Basé sur l'analyse du dépôt `Dyeri-Frontend`, voici les meilleures pratiques recommandées :

### 1. Feature-First Layered Architecture
Le frontend utilise déjà une structure par fonctionnalités (`features/`). Nous recommandons de renforcer la séparation en 4 couches par fonctionnalité :
- **Presentation** : Widgets UI et State Management (Bloc/Riverpod).
- **Domain** : Entités métier et interfaces de repository.
- **Data** : Implémentations de repository, DataSources (API REST/Local).
- **Application** : Services optionnels pour coordonner plusieurs repositories.

### 2. State Management
- **Riverpod** ou **BLoC** sont recommandés pour une gestion d'état robuste et testable.
- Éviter le couplage direct entre les widgets et les appels API.

### 3. Design System
- Utilisation systématique de la couche `core/theme` pour garantir une cohérence visuelle.
- Composants atomiques réutilisables dans `core/widgets`.

---

## 🛠️ Prochaines Étapes Recommandées

1. **Implémentation des Sagas** : Utiliser Spring Events ou un message broker (RabbitMQ/Kafka) pour coordonner les commandes et les paiements.
2. **Observabilité** : Configurer Prometheus et Grafana via l'Actuator déjà présent.
3. **Tests d'Intégration** : Utiliser **Testcontainers** pour valider la persistance avec une vraie base PostgreSQL.
4. **CI/CD** : Mettre en place des GitHub Actions pour le build Maven et l'analyse SonarQube.

---

**Architecte Logiciel :** Manus AI  
**Date :** 17 Mars 2026
