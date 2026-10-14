# TP JPA : Gestion de Salles de Réunion

Ce projet implémente une solution complète de gestion de salles avec **JPA 2.2**, **Hibernate 5.4.30** et une base de données en mémoire **H2**.

---

## 🎯 Objectifs Réalisés

1. **Requêtes JPQL avancées** : Algorithme d'exclusion par chevauchement horaire (`:debut < r.fin AND :fin > r.debut`) pour trouver les salles disponibles.
2. **Recherche multi-critères dynamique (Criteria API)** : Filtrage conditionnel sur le nom (insensible à la casse), la capacité minimale et la disponibilité par créneau avec sous-requête.
3. **Pagination côté serveur** : Découpage par page avec `setFirstResult` et `setMaxResults`, calcul dynamique du nombre total de pages et comptage via `CriteriaQuery<Long>`.
4. **Architecture découplée en couches** : Entités JPA, `SalleRepository` / `SalleRepositoryImpl`, `SalleService` / `SalleServiceImpl` et utilitaire générique `PaginationResult<T>`.

---

## 📂 Structure du Projet

```
salle-management/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/example/
    │   │       ├── model/
    │   │       │   ├── Salle.java
    │   │       │   └── Reservation.java
    │   │       ├── repository/
    │   │       │   ├── SalleRepository.java
    │   │       │   └── SalleRepositoryImpl.java
    │   │       ├── service/
    │   │       │   ├── SalleService.java
    │   │       │   └── SalleServiceImpl.java
    │   │       ├── util/
    │   │       │   └── PaginationResult.java
    │   │       └── App.java
    │   └── resources/
    │       └── META-INF/
    │           └── persistence.xml
    └── test/
        └── java/
            └── com/example/
                └── SalleServiceTest.java
```

---

## 🚀 Commandes d'Exécution

### 1. Compiler le projet
```bash
mvn clean compile
```

### 2. Lancer les tests unitaires
```bash
mvn test
```

### 3. Exécuter l'application principale de démonstration
```bash
mvn exec:java
```
ou
```bash
mvn exec:java -Dexec.mainClass="com.example.App"
```

---

## 💡 Détails des Algorithmes Clés

### 1. Chevauchement de créneaux (JPQL)
Deux créneaux $[D_1, F_1]$ et $[D_2, F_2]$ se chevauchent si et seulement si :
$$D_1 < F_2 \quad \text{ET} \quad F_1 > D_2$$
En JPQL :
```sql
SELECT s FROM Salle s WHERE s.id NOT IN (
    SELECT r.salle.id FROM Reservation r 
    WHERE (:debut < r.fin AND :fin > r.debut)
)
```

### 2. Requête Criteria API dynamique avec sous-requête
```java
Subquery<Long> subquery = cq.subquery(Long.class);
Root<Reservation> reservation = subquery.from(Reservation.class);
subquery.select(reservation.get("salle").get("id"));
subquery.where(cb.and(
    cb.lessThan(reservation.get("debut"), finRecherche),
    cb.greaterThan(reservation.get("fin"), debutRecherche)
));
predicates.add(cb.not(salle.get("id").in(subquery)));
```
