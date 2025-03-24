package it.dmi.structure.definitions;

import it.dmi.data.dto.TipoControlloDTO;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

@Getter
public enum ControlType {

    /*
        Controllo basato su COUNT (script SQL – scalare aggregato)

        Controllo basato su contenuto (script SQL – tabella)

        Controllo basato su esito programma (bat/cmd/sh)

        Controllo scatenante direttamente una azione
     */

    COUNT_BASED(1, "Controllo basato su COUNT (script SQL – scalare aggregato)"),

    CONTENT_BASED(2, "Controllo basato su contenuto (script SQL – tabella)"),

    PROGRAM_BASED(3, "Controllo basato su esito programma (bat/cmd/sh)"),

    ACTION_FIRING(4, "Controllo scatenante direttamente una azione"),

    NOT_VALID(5, "Invalid Database Configuration");

    private final int cardinal;
    private final String description;

    ControlType(int cardinal, String description) {
        this.cardinal = cardinal;
        this.description = description;
    }

    private static @NotNull ControlType fromID(@Range(from = 1, to = 4) @NotNull Long tipoControlloID) {
        return switch (tipoControlloID.intValue()) {
            case 1 -> ControlType.COUNT_BASED;
            case 2 -> ControlType.CONTENT_BASED;
            case 3 -> ControlType.PROGRAM_BASED;
            case 4 -> ControlType.ACTION_FIRING;
            default -> ControlType.NOT_VALID;
        };
    }

    public static @NotNull ControlType fromID(TipoControlloDTO tipoControllo) {
        if (tipoControllo == null || tipoControllo.id() == null) return ControlType.NOT_VALID;
        return fromID(tipoControllo.id());
    }
}
