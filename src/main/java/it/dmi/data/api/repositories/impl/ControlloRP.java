package it.dmi.data.api.repositories.impl;

import it.dmi.data.api.repositories.ARepository;
import it.dmi.data.entities.impl.Controllo;
import jakarta.ejb.Stateless;

@Stateless
public class ControlloRP extends ARepository<Controllo> {

    public ControlloRP() {}
}
