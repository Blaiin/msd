package it.dmi.data.api.service;

import it.dmi.data.api.repositories.impl.SogliaRP;
import it.dmi.data.entities.impl.Soglia;
import it.dmi.structure.exceptions.impl.persistence.DatabaseConnectionException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@SuppressWarnings("unused")
@Stateless
@Slf4j
public class SogliaService {

    @Inject
    private SogliaRP repository;

    public void create (Soglia soglia) {
        repository.save(soglia);
    }

    public Soglia getByID (Long id) {
        return repository.findByID(id);
    }

    public void update (Soglia soglia) {
        repository.update(soglia);
    }

    public void delete (Long id) {
        repository.delete(id);
    }

    public List<Soglia> getAll () {
        try {
            return repository.findAll();
        } catch (DatabaseConnectionException e) {
            log.error("Error while retrieving entities.", e);
            return List.of();
        }
    }
}
