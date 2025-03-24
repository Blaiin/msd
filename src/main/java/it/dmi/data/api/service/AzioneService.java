package it.dmi.data.api.service;

import it.dmi.data.api.repositories.impl.AzioneRP;
import it.dmi.data.entities.impl.Azione;
import it.dmi.structure.exceptions.impl.persistence.DatabaseConnectionException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unused")
@Stateless
@Slf4j
public class AzioneService {

    @Inject
    private AzioneRP repository;

    public void create(Azione azione) {
        repository.save(azione);
    }

    public Azione getByID(Long id) {
        return repository.findByID(id);
    }

    public void update(Azione azione) {
        repository.update(azione);
    }

    public void delete(Long id) {
        repository.delete(id);
    }

    public List<Azione> getAll() {
        try {
            return repository.findAll();
        } catch (DatabaseConnectionException e) {
            log.error("Error while retrieving entities.", e);
            return List.of();
        }
    }

    public List<Azione> getAllOrdered() {
        return getAll().stream().sorted(Comparator.comparingInt(Azione::getOrdineAzione)).toList();
    }
}
