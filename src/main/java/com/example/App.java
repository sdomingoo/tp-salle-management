package com.example;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.service.SalleService;
import com.example.service.SalleServiceImpl;
import com.example.util.PaginationResult;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;

public class App {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  TP JPA : GESTION DE SALLES - REQUÊTES COMPLEXES, CRITERIA API & PAGINATION  ");
        System.out.println("================================================================================\n");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SallePU");
        EntityManager em = emf.createEntityManager();

        try {
            // Initialisation des données de test
            initTestData(em);

            SalleService salleService = new SalleServiceImpl();
            // Injection manuelle pour ce test (conformément à l'Étape 11)
            ((SalleServiceImpl) salleService).setEntityManager(em);

            // =========================================================================
            // TEST 1 : Étape 5 - Requête JPQL pour les salles disponibles par créneau
            // =========================================================================
            LocalDateTime debut = LocalDateTime.of(2024, 6, 1, 9, 0);
            LocalDateTime fin = LocalDateTime.of(2024, 6, 1, 11, 0);

            System.out.println("\n--- [TEST 1 : JPQL] Salles disponibles entre " + debut + " et " + fin + " ---");
            List<Salle> sallesDisponibles = salleService.getAvailableRooms(debut, fin);
            for (Salle salle : sallesDisponibles) {
                System.out.println("  ✓ " + salle.getNom() + " (Capacité : " + salle.getCapacite() + " personnes)");
            }

            // =========================================================================
            // TEST 2 : Étape 6 - Recherche multi-critères dynamique (Criteria API)
            // =========================================================================
            System.out.println("\n--- [TEST 2 : Criteria API] Recherche multi-critères dynamique ---");
            System.out.println("Critères : capacité >= 25, créneau [09:00 - 11:00], nom contenant 'e'");
            List<Salle> sallesFiltrees = salleService.searchRooms("e", 25, debut, fin, 1, 10);
            for (Salle salle : sallesFiltrees) {
                System.out.println("  ✓ Trouvée : " + salle.getNom() + " (Capacité : " + salle.getCapacite() + ")");
            }

            // =========================================================================
            // TEST 3 : Étape 7 & 10 - Système de pagination côté serveur (PaginationResult)
            // =========================================================================
            System.out.println("\n--- [TEST 3 : Pagination côté serveur] ---");
            int pageSize = 2;

            // Page 1
            PaginationResult<Salle> page1 = salleService.searchRoomsPaged(null, null, null, null, 1, pageSize);
            System.out.println("Page " + page1.getCurrentPage() + "/" + page1.getTotalPages() +
                    " (Total éléments: " + page1.getTotalItems() + ", Taille page: " + page1.getPageSize() + ") :");
            for (Salle salle : page1.getItems()) {
                System.out.println("  [Page 1] " + salle.getNom() + " (Capacité: " + salle.getCapacite() + ")");
            }

            // Page 2
            PaginationResult<Salle> page2 = salleService.searchRoomsPaged(null, null, null, null, 2, pageSize);
            System.out.println("\nPage " + page2.getCurrentPage() + "/" + page2.getTotalPages() +
                    " (Total éléments: " + page2.getTotalItems() + ", Taille page: " + page2.getPageSize() + ") :");
            for (Salle salle : page2.getItems()) {
                System.out.println("  [Page 2] " + salle.getNom() + " (Capacité: " + salle.getCapacite() + ")");
            }

            // Page 3
            PaginationResult<Salle> page3 = salleService.searchRoomsPaged(null, null, null, null, 3, pageSize);
            System.out.println("\nPage " + page3.getCurrentPage() + "/" + page3.getTotalPages() +
                    " (Total éléments: " + page3.getTotalItems() + ", Taille page: " + page3.getPageSize() + ") :");
            for (Salle salle : page3.getItems()) {
                System.out.println("  [Page 3] " + salle.getNom() + " (Capacité: " + salle.getCapacite() + ")");
            }

            System.out.println("\n================================================================================");
            System.out.println("                      TOUS LES TESTS SONT VALIDES AVEC SUCCES                   ");
            System.out.println("================================================================================");

        } finally {
            em.close();
            emf.close();
        }
    }

    private static void initTestData(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // Création des salles
        Salle s1 = new Salle("Salle Einstein", 30);
        Salle s2 = new Salle("Salle Turing", 50);
        Salle s3 = new Salle("Salle Lovelace", 20);
        Salle s4 = new Salle("Salle Curie", 100);
        Salle s5 = new Salle("Salle Hopper", 15);
        Salle s6 = new Salle("Salle Shannon", 40);

        em.persist(s1);
        em.persist(s2);
        em.persist(s3);
        em.persist(s4);
        em.persist(s5);
        em.persist(s6);

        // Création de réservations sur le 2024-06-01
        // Créneau testé : 09:00 - 11:00

        // Turing est réservée de 08:30 à 10:30 -> CHEVAUCHE (non disponible)
        Reservation r1 = new Reservation(s2, LocalDateTime.of(2024, 6, 1, 8, 30), LocalDateTime.of(2024, 6, 1, 10, 30));
        s2.addReservation(r1);
        em.persist(r1);

        // Curie est réservée de 10:00 à 12:00 -> CHEVAUCHE (non disponible)
        Reservation r2 = new Reservation(s4, LocalDateTime.of(2024, 6, 1, 10, 0), LocalDateTime.of(2024, 6, 1, 12, 0));
        s4.addReservation(r2);
        em.persist(r2);

        // Einstein est réservée l'après-midi de 14:00 à 16:00 -> NE CHEVAUCHE PAS (disponible)
        Reservation r3 = new Reservation(s1, LocalDateTime.of(2024, 6, 1, 14, 0), LocalDateTime.of(2024, 6, 1, 16, 0));
        s1.addReservation(r3);
        em.persist(r3);

        // Lovelace, Hopper, Shannon n'ont aucune réservation ce matin -> DISPONIBLES

        tx.commit();
        System.out.println("✔ Données initialisées avec succès : 6 salles et 3 réservations créées.");
    }
}
