package it.dmi.data.dto;

import it.dmi.data.dto.idto.IDTO;
import it.dmi.data.entities.impl.FonteDati;
import org.jetbrains.annotations.NotNull;

public record FonteDatiDTO(
        Long id,
        String descrizione,
        String jndiName,
        String nomeDriver,
        String nomeClasse,
        String url) implements IDTO<FonteDati> {

    public FonteDatiDTO(@NotNull FonteDati fd) {
        this(fd.getId(), fd.getDescrizione(), fd.getJndiName(), fd.getNomeDriver(), fd.getNomeClasse(), fd.getUrl());
    }

    public FonteDatiDTO(String descrizione, String jndiName, String nomeDriver, String nomeClasse, String url) {
        this(null, descrizione, jndiName, nomeDriver, nomeClasse, url);
    }

    @Override
    public @NotNull FonteDati toEntity() {
        return new FonteDati(
                this.id,
                this.descrizione,
                this.nomeDriver,
                this.nomeClasse,
                this.url,
                this.jndiName
        );
    }
}
