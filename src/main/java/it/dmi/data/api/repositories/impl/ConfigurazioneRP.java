package it.dmi.data.api.repositories.impl;

import it.dmi.data.api.repositories.ARepository;
import it.dmi.data.entities.impl.Configurazione;
import jakarta.ejb.Stateless;

@Stateless
public class ConfigurazioneRP extends ARepository<Configurazione> {

    public ConfigurazioneRP() {}
}
