package it.dmi.data.api.repositories.impl;

import it.dmi.data.api.repositories.ARepository;
import it.dmi.data.entities.impl.Azione;
import jakarta.ejb.Stateless;

@Stateless
public class AzioneRP extends ARepository<Azione> {

    public AzioneRP() {}
}
