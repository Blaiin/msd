package it.dmi.data.dto;

import it.dmi.data.dto.idto.IDTO;
import it.dmi.data.entities.impl.TipoAzione;
import org.jetbrains.annotations.NotNull;

public record TipoAzioneDTO(
        Long id,
        String descrizione) implements IDTO<TipoAzione> {

    public TipoAzioneDTO(@NotNull TipoAzione ta) {
        this(ta.getId(), ta.getDescrizione());
    }

    @Override
    public @NotNull TipoAzione toEntity() {
        return new TipoAzione(
                this.id,
                this.descrizione
        );
    }
}
