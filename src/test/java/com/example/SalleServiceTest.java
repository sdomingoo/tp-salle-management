package com.example;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.service.SalleService;
import com.example.service.SalleServiceImpl;
import com.example.util.PaginationResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

public class SalleServiceTest {

    private EntityManagerFactory emf;
    private EntityManager em;
    private SalleService salleService;

    @Before
    public void setUp() {
        emf = Persistence.createEntityManagerFactory("SallePU");
        em = emf.createEntityManager();

        salleService = new SalleServiceImpl();
        ((SalleServiceImpl) salleService).setEntityManager(em);

        em.getTransaction().begin();
        em.createQuery("DELETE FROM Reservation").executeUpdate();
        em.createQuery("DELETE FROM Salle").executeUpdate();

        Salle s1 = new Salle("Salle Einstein", 30);
        Salle s2 = new Salle("Salle Turing", 50);
        Salle s3 = new Salle("Salle Lovelace", 20);
        Salle s4 = new Salle("Salle Curie", 100);

        em.persist(s1);
        em.persist(s2);
        em.persist(s3);
        em.persist(s4);

        // Turing occupée 08:30 à 10:30
        Reservation r1 = new Reservation(s2, LocalDateTime.of(2024, 6, 1, 8, 30), LocalDateTime.of(2024, 6, 1, 10, 30));
        em.persist(r1);

        // Curie occupée 10:00 à 12:00
        Reservation r2 = new Reservation(s4, LocalDateTime.of(2024, 6, 1, 10, 0), LocalDateTime.of(2024, 6, 1, 12, 0));
        em.persist(r2);

        // Einstein occupée 14:00 à 16:00
        Reservation r3 = new Reservation(s1, LocalDateTime.of(2024, 6, 1, 14, 0), LocalDateTime.of(2024, 6, 1, 16, 0));
        em.persist(r3);

        em.getTransaction().commit();
    }

    @After
    public void tearDown() {
        if (em != null && em.isOpen()) {
            em.close();
        }
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Test
    public void testFindAvailableRoomsJPQL() {
        // Créneau : 09:00 à 11:00
        // Turing (8h30-10h30) et Curie (10h-12h) chevauchent ce créneau -> non disponibles.
        // Einstein (14h-16h) et Lovelace (pas de réservation) sont disponibles.
        LocalDateTime debut = LocalDateTime.of(2024, 6, 1, 9, 0);
        LocalDateTime fin = LocalDateTime.of(2024, 6, 1, 11, 0);

        List<Salle> disponibles = salleService.getAvailableRooms(debut, fin);

        assertEquals(2, disponibles.size());
        assertTrue(disponibles.stream().anyMatch(s -> s.getNom().equals("Salle Einstein")));
        assertTrue(disponibles.stream().anyMatch(s -> s.getNom().equals("Salle Lovelace")));
        assertFalse(disponibles.stream().anyMatch(s -> s.getNom().equals("Salle Turing")));
        assertFalse(disponibles.stream().anyMatch(s -> s.getNom().equals("Salle Curie")));
    }

    @Test
    public void testDynamicSearchCriteriaAPI() {
        LocalDateTime debut = LocalDateTime.of(2024, 6, 1, 9, 0);
        LocalDateTime fin = LocalDateTime.of(2024, 6, 1, 11, 0);

        // Recherche avec nom contenant 'einstein' et capacite >= 25
        List<Salle> resultats = salleService.searchRooms("einstein", 25, debut, fin, 1, 10);
        assertEquals(1, resultats.size());
        assertEquals("Salle Einstein", resultats.get(0).getNom());

        // Recherche avec capacité minimale trop élevée pour les salles disponibles
        List<Salle> vides = salleService.searchRooms(null, 200, debut, fin, 1, 10);
        assertTrue(vides.isEmpty());
    }

    @Test
    public void testPagination() {
        // Total 4 salles dans la base
        PaginationResult<Salle> page1 = salleService.searchRoomsPaged(null, null, null, null, 1, 2);
        assertEquals(2, page1.getItems().size());
        assertEquals(1, page1.getCurrentPage());
        assertEquals(2, page1.getPageSize());
        assertEquals(4, page1.getTotalItems());
        assertEquals(2, page1.getTotalPages());

        PaginationResult<Salle> page2 = salleService.searchRoomsPaged(null, null, null, null, 2, 2);
        assertEquals(2, page2.getItems().size());
        assertEquals(2, page2.getCurrentPage());

        // Vérification de l'absence de doublons entre page 1 et page 2
        Salle p1Item1 = page1.getItems().get(0);
        Salle p2Item1 = page2.getItems().get(0);
        assertNotEquals(p1Item1.getId(), p2Item1.getId());
    }
}
