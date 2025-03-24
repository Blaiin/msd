package it.dmi.data.dto;

import it.dmi.data.dto.idto.IDTO;
import it.dmi.data.entities.impl.Ambito;
import org.jetbrains.annotations.NotNull;

public record AmbitoDTO(
        Long id,
        String nome,
        String destinazione) implements IDTO<Ambito> {

    public AmbitoDTO(@NotNull Ambito a) {
        this(a.getId(), a.getNome(), a.getDestinazione());
    }

    public AmbitoDTO(@NotNull String nome, @NotNull String destinazione) {
        this(null, nome, destinazione);
    }

    @Override
    public @NotNull Ambito toEntity() {
        return new Ambito(
                this.id,
                this.nome,
                this.destinazione
        );
    }
}
