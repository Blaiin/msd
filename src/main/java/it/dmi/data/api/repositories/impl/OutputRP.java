package it.dmi.data.api.repositories.impl;

import it.dmi.data.api.repositories.ARepository;
import it.dmi.data.entities.impl.Output;
import jakarta.ejb.Stateless;

@Stateless
public class OutputRP extends ARepository<Output> {

    public OutputRP() {}
}
