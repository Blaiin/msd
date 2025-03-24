package it.dmi.data.api.repositories.impl;

import it.dmi.data.api.repositories.ARepository;
import it.dmi.data.entities.impl.TemplateEmail;
import jakarta.ejb.Stateless;

@Stateless
public class TemplateEmailRP extends ARepository<TemplateEmail> {

    public TemplateEmailRP() {}
}
