package it.dmi.data.api.service;

import it.dmi.data.api.repositories.impl.ControlloRP;
import it.dmi.data.dto.ControlloDTO;
import it.dmi.data.entities.impl.Controllo;
import it.dmi.structure.exceptions.impl.persistence.DatabaseConnectionException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Comparator;
import java.util.List;

@Stateless
@Slf4j
public class ControlloService {

    @Inject
    private ControlloRP repository;

    public void create(Controllo controllo) {
        repository.save(controllo);
    }

    public Controllo getByID(Long id) {
        return repository.findByID(id);
    }

    public void update(Controllo controllo) {
        repository.update(controllo);
    }

    public void delete(Long id) {
        repository.delete(id);
    }

    public List<Controllo> getAll() {
        try {
            return repository.findAll();
        } catch (DatabaseConnectionException e) {
            log.error("Error while retrieving entities.", e);
            return List.of();
        }
    }

    @Unmodifiable
    public List<ControlloDTO> getAllDTOs() {
        try {
            return repository.findAll().stream().map(ControlloDTO::new).toList();
        } catch (DatabaseConnectionException e) {
            log.error("Error while retrieving entities.", e);
            return List.of();
        }
    }

    @Unmodifiable
    public List<ControlloDTO> getAllOrderedAsDTOs() {
        return getAllDTOs()
                .stream()
                .sorted(Comparator.comparingInt(ControlloDTO::ordineControllo))
                .toList();
    }
}
