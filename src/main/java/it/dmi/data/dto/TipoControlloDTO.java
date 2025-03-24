package it.dmi.data.dto;

import it.dmi.data.dto.idto.IDTO;
import it.dmi.data.entities.impl.TipoControllo;
import org.jetbrains.annotations.NotNull;

public record TipoControlloDTO(
        Long id,
        String descrizione
) implements IDTO<TipoControllo> {

    public TipoControlloDTO(@NotNull String descrizione) {
        this(null, descrizione);
    }

    public TipoControlloDTO(@NotNull TipoControllo tc) {
        this(tc.getId(), tc.getDescrizione());
    }

    @Override
    public @NotNull TipoControllo toEntity() {
        return new TipoControllo(
                this.id,
                this.descrizione
        );
    }
}
