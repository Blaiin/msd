package it.dmi.data.api.service;

import it.dmi.data.api.repositories.impl.ConfigurazioneRP;
import it.dmi.data.entities.impl.Configurazione;
import it.dmi.structure.exceptions.impl.persistence.DatabaseConnectionException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Stateless
@Slf4j
public class ConfigurazioneService {

    @Inject
    private ConfigurazioneRP repository;

    public boolean create(Configurazione config) {
        if (config == null) return false;
        if (repository.findByID(config.getId()) != null) return false;
        return repository.save(config) != null;
    }

    public Configurazione getByID(Long id) {
        return repository.findByID(id);
    }

    public void update(Configurazione config) {
        repository.update(config);
    }

    public void delete(Long id) {
        repository.delete(id);
    }

    public List<Configurazione> getAll() {
        try {
            return repository.findAll();
        } catch (DatabaseConnectionException e) {
            log.error("Error while retrieving entities.", e);
            return List.of();
        }
    }
}
