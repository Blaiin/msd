package it.dmi.data.api.service;

import it.dmi.data.api.repositories.impl.TipoAzioneRP;
import it.dmi.data.entities.impl.TipoAzione;
import it.dmi.structure.exceptions.impl.persistence.DatabaseConnectionException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@SuppressWarnings("unused")
@Stateless
@Slf4j
public class TipoAzioneService {

    @Inject
    private TipoAzioneRP repository;

    public void create (TipoAzione tipoAzione) {
        repository.save(tipoAzione);
    }

    public TipoAzione getByID (Long id) {
        return repository.findByID(id);
    }

    public void update (TipoAzione tipoAzione) {
        repository.update(tipoAzione);
    }

    public void delete (Long id) {
        repository.delete(id);
    }

    public List<TipoAzione> getAll () {
        try {
            return repository.findAll();
        } catch (DatabaseConnectionException e) {
            log.error("Error while retrieving entities.", e);
            return List.of();
        }
    }
}
