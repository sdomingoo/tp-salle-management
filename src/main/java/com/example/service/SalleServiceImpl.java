package com.example.service;

import com.example.model.Salle;
import com.example.repository.SalleRepository;
import com.example.repository.SalleRepositoryImpl;
import com.example.util.PaginationResult;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class SalleServiceImpl implements SalleService {

    @Inject
    private SalleRepository salleRepository;

    public SalleServiceImpl() {
    }

    public SalleServiceImpl(SalleRepository salleRepository) {
        this.salleRepository = salleRepository;
    }

    public void setSalleRepository(SalleRepository salleRepository) {
        this.salleRepository = salleRepository;
    }

    /**
     * Permet d'injecter manuellement l'EntityManager en environnement Java SE
     * comme requis à l'Étape 11.
     */
    public void setEntityManager(EntityManager em) {
        if (this.salleRepository == null) {
            this.salleRepository = new SalleRepositoryImpl(em);
        } else if (this.salleRepository instanceof SalleRepositoryImpl) {
            ((SalleRepositoryImpl) this.salleRepository).setEntityManager(em);
        }
    }

    public SalleRepository getSalleRepository() {
        return salleRepository;
    }

    @Override
    public List<Salle> getAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        if (debut == null || fin == null) {
            throw new IllegalArgumentException("Les dates de début et de fin ne peuvent pas être nulles.");
        }
        if (debut.isAfter(fin)) {
            throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin.");
        }
        return salleRepository.findAvailableRooms(debut, fin);
    }

    @Override
    public List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin, int page, int size) {
        return salleRepository.searchRooms(nom, capaciteMin, debut, fin, page, size);
    }

    @Override
    public long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin) {
        return salleRepository.countRooms(nom, capaciteMin, debut, fin);
    }

    @Override
    public PaginationResult<Salle> searchRoomsPaged(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin, int page, int size) {
        List<Salle> items = salleRepository.searchRooms(nom, capaciteMin, debut, fin, page, size);
        long total = salleRepository.countRooms(nom, capaciteMin, debut, fin);
        return new PaginationResult<>(items, page, size, total);
    }

    @Override
    public void save(Salle salle) {
        salleRepository.save(salle);
    }

    @Override
    public Salle findById(Long id) {
        return salleRepository.findById(id);
    }

    @Override
    public List<Salle> getAllRooms() {
        return salleRepository.findAll();
    }
}
