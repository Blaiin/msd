package it.dmi.structure.definitions;

import it.dmi.data.dto.TipoAzioneDTO;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

@Getter
public enum ActionType {

    /*
        Invio di email

        Esecuzione di script SQL

        Esecuzione di bat/cmd/sh

        Esecuzione di altro controllo
     */

    SEND_EMAIL(1, "Invio di email"),

    EXECUTE_SQL(2, "Esecuzione di script SQL"),

    EXECUTE_PROGRAM(3, "Esecuzione di bat/cmd/sh"),

    FIRE_OTHER_CONTROL(4, "Esecuzione di altro controllo"),

    NOT_VALID(5, "Invalid Database configuration"),

    SQL_SEND_EMAIL_RESULTS(6, "Esegue uno script sql ed invia per email il risultato");

    private final int cardinal;

    private final String description;

    ActionType(int cardinal, String description) {
        this.cardinal = cardinal;
        this.description = description;
    }

    private static @NotNull ActionType fromID(@Range(from = 1, to = 4) @NotNull Long tipoControlloID) {
        return switch (tipoControlloID.intValue()) {
            case 1 -> ActionType.SEND_EMAIL;
            case 2 -> ActionType.EXECUTE_SQL;
            case 3 -> ActionType.EXECUTE_PROGRAM;
            case 4 -> ActionType.FIRE_OTHER_CONTROL;
            case 5 -> ActionType.SQL_SEND_EMAIL_RESULTS;
            default -> ActionType.NOT_VALID;
        };
    }

    public static @NotNull ActionType fromID(TipoAzioneDTO tipoAzione) {
        if (tipoAzione == null || tipoAzione.id() == null) return ActionType.NOT_VALID;
        return fromID(tipoAzione.id());
    }
}
