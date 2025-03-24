package it.dmi.data.dto;

import it.dmi.data.dto.idto.IDTO;
import it.dmi.data.entities.application.ApplicationConfig;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record ApplicationConfigDTO(
        Long id,
        String reload) implements IDTO<ApplicationConfig> {

    public ApplicationConfigDTO(@NotNull ApplicationConfig a) {
        this(a.getId(), a.getReload());
    }

    public ApplicationConfigDTO(@NotNull String reload) {
        this(null, reload);
    }

    @Contract(" -> new")
    @Override
    public @NotNull ApplicationConfig toEntity() {
        return new ApplicationConfig(
                this.id,
                this.reload
        );
    }
}
