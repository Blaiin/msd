package it.dmi.data.api.service;

import it.dmi.data.api.repositories.impl.ApplicationConfigRP;
import it.dmi.data.dto.ApplicationConfigDTO;
import it.dmi.data.entities.application.ApplicationConfig;
import it.dmi.structure.exceptions.impl.persistence.DatabaseConnectionException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Stateless
@Slf4j
public class ApplicationConfigService {

    @Inject
    private ApplicationConfigRP repository;

    public ApplicationConfig getReloadCfg() {
        return repository.getReloadCfg();
    }

    public boolean updateReloadCfg(String reloadStatus) {
        log.info("Setting reload status to {}", reloadStatus);
        this.repository.updateReloadStatus(reloadStatus);
        return false;
    }

    private boolean create(ApplicationConfig ambito) {
        if (ambito == null) return false;
        if (repository.findByID(ambito.getId()) != null) return false;
        return repository.save(ambito) != null;
    }

    @SuppressWarnings("unused")
    public ApplicationConfigDTO createOrFind(@NotNull ApplicationConfigDTO dto) {
        if (create(dto.toEntity())) return dto;
        else return null;
    }

    public ApplicationConfig getByID(Long id) {
        return repository.findByID(id);
    }

    public void update(ApplicationConfig entity) {
        repository.update(entity);
    }

    public void delete(Long id) {
        repository.delete(id);
    }

    public List<ApplicationConfig> getAll() {
        try {
            return repository.findAll();
        } catch (DatabaseConnectionException e) {
            log.error("Error while retrieving entities.", e);
            return List.of();
        }
    }

}
