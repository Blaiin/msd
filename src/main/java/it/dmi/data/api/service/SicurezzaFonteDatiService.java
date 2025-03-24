package it.dmi.data.api.service;

import it.dmi.data.api.repositories.impl.SicurezzaFonteDatiRP;
import it.dmi.data.entities.impl.SicurezzaFonteDati;
import it.dmi.structure.exceptions.impl.persistence.DatabaseConnectionException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@SuppressWarnings("unused")
@Stateless
@Slf4j
public class SicurezzaFonteDatiService {

    @Inject
    private SicurezzaFonteDatiRP repository;

    public void create (SicurezzaFonteDati sfd) {
        repository.save(sfd);
    }

    public SicurezzaFonteDati getByID (Long id) {
        return repository.findByID(id);
    }

    public void update (SicurezzaFonteDati sfd) {
        repository.update(sfd);
    }

    public void delete (Long id) {
        repository.delete(id);
    }

    public List<SicurezzaFonteDati> getAll () {
        try {
            return repository.findAll();
        } catch (DatabaseConnectionException e) {
            log.error("Error while retrieving entities.", e);
            return List.of();
        }
    }
}
