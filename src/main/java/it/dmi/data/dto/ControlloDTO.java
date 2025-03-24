package it.dmi.data.dto;

import it.dmi.data.dto.idto.IDTO;
import it.dmi.data.entities.impl.Azione;
import it.dmi.data.entities.impl.Configurazione;
import it.dmi.data.entities.impl.Controllo;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public record ControlloDTO(
        Long id,
        String descrizione,
        TipoControlloDTO tipoControllo,
        AmbitoDTO ambito,
        List<Azione> azioni,
        List<Configurazione> configurazioni,
        Integer ordineControllo) implements IDTO<Controllo>, Comparable<ControlloDTO> {

    public List<ConfigurazioneDTO> orderedConfigsAsDTOs() {
        return this.configurazioni.stream()
                .map(ConfigurazioneDTO::new)
                .sorted(Comparator.comparingInt(ConfigurazioneDTO::ordine))
                .toList();
    }

    public ControlloDTO(@NotNull Controllo c) {
        this(c.getId(), c.getDescrizione(), new TipoControlloDTO(c.getTipoControllo()),
                new AmbitoDTO(c.getAmbito()), c.getAzioni(), c.getConfigurazioni(), c.getOrdineControllo());
    }

    @Override
    public @NotNull Controllo toEntity() {
        return new Controllo(
                this.id,
                this.descrizione,
                this.tipoControllo.toEntity(),
                this.ambito.toEntity(),
                this.azioni,
                this.configurazioni,
                this.ordineControllo
        );
    }

    @Override
    public int compareTo(@NotNull ControlloDTO o) {
        return !this.ordineControllo.equals(o.ordineControllo) ?
                Integer.compare(this.ordineControllo, o.ordineControllo) :
                this.id().compareTo(o.id());
    }
}
