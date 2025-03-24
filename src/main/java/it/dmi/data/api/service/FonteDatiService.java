package it.dmi.data.api.service;

import it.dmi.data.api.repositories.impl.FonteDatiRP;
import it.dmi.data.entities.impl.FonteDati;
import it.dmi.structure.exceptions.impl.persistence.DatabaseConnectionException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@SuppressWarnings("unused")
@Stateless
@Slf4j
public class FonteDatiService {

    @Inject
    private FonteDatiRP repository;

    public void create (FonteDati fonteDati) {
        repository.save(fonteDati);
    }

    public FonteDati getByID (Long id) {
        return repository.findByID(id);
    }

    public void update (FonteDati fonteDati) {
        repository.update(fonteDati);
    }

    public void delete (Long id) {
        repository.delete(id);
    }

    public List<FonteDati> getAll () {
        try {
            return repository.findAll();
        } catch (DatabaseConnectionException e) {
            log.error("Error while retrieving entities.", e);
            return List.of();
        }
    }
}
