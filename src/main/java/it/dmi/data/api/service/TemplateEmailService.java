package it.dmi.data.api.service;

import it.dmi.data.api.repositories.impl.TemplateEmailRP;
import it.dmi.data.entities.impl.TemplateEmail;
import it.dmi.structure.exceptions.impl.persistence.DatabaseConnectionException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@SuppressWarnings("unused")
@Stateless
@Slf4j
public class TemplateEmailService {

    @Inject
    private TemplateEmailRP repository;

    public void create(TemplateEmail template) {
        repository.save(template);
    }

    public TemplateEmail getByID(Long id) {
        return repository.findByID(id);
    }

    public void update(TemplateEmail template) {
        repository.update(template);
    }

    public void delete(Long id) {
        repository.delete(id);
    }

    public List<TemplateEmail> getAll() {
        try {
            return repository.findAll();
        } catch (DatabaseConnectionException e) {
            log.error("Error while retrieving entities.", e);
            return List.of();
        }
    }
}
