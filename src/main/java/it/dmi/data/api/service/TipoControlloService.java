package it.dmi.data.api.service;

import it.dmi.data.api.repositories.impl.TipoControlloRP;
import it.dmi.data.dto.TipoControlloDTO;
import it.dmi.data.entities.impl.TipoControllo;
import it.dmi.structure.exceptions.impl.persistence.DatabaseConnectionException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
@Stateless
@Slf4j
public class TipoControlloService {

    @Inject
    private TipoControlloRP repository;

    private boolean create(TipoControllo tipoControllo) {
        if (tipoControllo == null) return false;
        if (repository.findByID(tipoControllo.getId()) != null) return false;
        return repository.save(tipoControllo) != null;
    }

    public TipoControlloDTO createOrFind(@NotNull TipoControlloDTO dto) {
        if(create(dto.toEntity())) return dto;
        else return null;
    }

    public TipoControllo getByID(Long id) {
        return repository.findByID(id);
    }

    public void update(TipoControllo tipoControllo) {
        repository.update(tipoControllo);
    }

    public void delete(Long id) {
        repository.delete(id);
    }

    public List<TipoControllo> getAll() {
        try {
            return repository.findAll();
        } catch (DatabaseConnectionException e) {
            log.error("Error while retrieving entities.", e);
            return List.of();
        }
    }
}
