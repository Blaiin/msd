package it.dmi.data.api.service;

import it.dmi.data.api.repositories.impl.OutputRP;
import it.dmi.data.dto.OutputDTO;
import it.dmi.data.entities.impl.Output;
import it.dmi.structure.exceptions.impl.persistence.DatabaseConnectionException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Stateless
@Slf4j
public class OutputService {

    @Inject
    private OutputRP repository;

    public Long createAndGetID(OutputDTO output) {
        if (output == null) {
            log.error("Could not save and get ID from a null containers to database.");
            return null;
        }
        if(output.configurazioneId() == null && output.azioneId() == null) {
            log.error("Could not save and get ID an containers to database, necessary fields were invalid.");
            return null;
        }
        return create(output.toEntity()).getId();
    }

    public void create(OutputDTO output) {
        if (output == null) {
            log.error("Could not save a null containers to database.");
            return;
        }
        if(output.configurazioneId() == null && output.azioneId() == null) {
            log.error("Could not save containers to database, necessary fields were invalid.");
            return;
        }
        create(output.toEntity());
    }

    private Output create(Output output) {
        return repository.save(output);
    }

    public Output getByID(Long id) {
        return repository.findByID(id);
    }

    public OutputDTO update(OutputDTO output) {
        if (output == null) {
            log.error("Could not update an Output record with an invalid instance.");
            return null;
        }
        if(output.configurazioneId() == null && output.azioneId() == null) {
            log.error("Could not update an Output with identity fields invalid (AzioneID OR ConfigurazioneID)");
            return null;
        }
        final Output updated = repository.update(output.toEntity());
        return updated != null ? new OutputDTO(updated) : null;
    }
    public void update(Output output) {
        repository.update(output);
    }

    public void delete(Long id) {
        repository.delete(id);
    }

    public List<Output> getAll() {
        try {
            return repository.findAll();
        } catch (DatabaseConnectionException e) {
            log.error("Error while retrieving entities.", e);
            return List.of();
        }
    }
}
