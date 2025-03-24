package it.dmi.data.dto;

import it.dmi.data.dto.idto.IDTO;
import it.dmi.data.entities.impl.SicurezzaFonteDati;
import org.jetbrains.annotations.NotNull;

public record SicurezzaFonteDatiDTO(
        Long id,
        String descrizione,
        String userID,
        String password) implements IDTO<SicurezzaFonteDati> {

    public SicurezzaFonteDatiDTO(String descrizione, String userID, String password) {
        this(null, descrizione, userID, password);
    }

    public SicurezzaFonteDatiDTO(@NotNull SicurezzaFonteDati sfd) {
        this(sfd.getId(), sfd.getDescrizione(), sfd.getUserID(), sfd.getPassword());
    }

    @Override
    public @NotNull SicurezzaFonteDati toEntity() {
        return new SicurezzaFonteDati(
                this.id,
                this.descrizione,
                this.userID,
                this.password
        );
    }
}
