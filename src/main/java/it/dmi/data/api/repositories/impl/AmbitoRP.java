package it.dmi.data.api.repositories.impl;

import it.dmi.data.api.repositories.ARepository;
import it.dmi.data.entities.impl.Ambito;
import jakarta.ejb.Stateless;

@Stateless
public class AmbitoRP extends ARepository<Ambito> {

    public AmbitoRP() {}
}
