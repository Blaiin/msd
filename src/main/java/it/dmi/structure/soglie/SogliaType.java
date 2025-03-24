package it.dmi.structure.soglie;

import it.dmi.data.dto.SogliaDTO;
import it.dmi.data.entities.impl.Soglia;
import it.dmi.structure.definitions.ControlType;
import it.dmi.structure.exceptions.impl.entities.IllegalSogliaDefinitionException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Getter
@Slf4j
public enum SogliaType {

    INFERIOR(1, "Comparazione di minoranza con soglia singola"),

    SUPERIOR(2, "Comparazione di maggioranza con soglia singola"),

    RANGE(3, "Comparazione appartenenza al range dei valori soglia"),

    CONTENT(4, "Comparazione contenuto testuale tramite operatore"),

    VECTOR(5, "Soglia pilota per attivazione diretta di Azioni");

    private static final String SOGLIA_BYPASS_FLAG = "bypass";

    private final int cardinal;

    private final String description;

    private Comparable sogliaTypeObject;

    SogliaType(int cardinal, String description) {
        this.cardinal = cardinal;
        this.description = description;
    }

    public static @NotNull SogliaType resolve(@NotNull Soglia soglia) throws IllegalSogliaDefinitionException {
        var sogliaDTO = new SogliaDTO(soglia);
        return resolve(sogliaDTO);
    }

    public static @NotNull SogliaType resolve(@NotNull SogliaDTO dto) throws IllegalSogliaDefinitionException {

        final var sID = dto.strID();

        //Se solo valore != null e == BYPASS oltre che tipo controllo == 4 -> Soglia Vettore

        if (dto.sogliaInferiore() == null && dto.sogliaSuperiore() == null
                && (dto.valore() != null && dto.operatore() == null
                && dto.valore().trim().equalsIgnoreCase(SOGLIA_BYPASS_FLAG))
                && dto.configurazione().getTipoControllo().getId() == ControlType.ACTION_FIRING.getCardinal()) {
            SogliaType type = SogliaType.VECTOR;
            type.sogliaTypeObject = new SogliaVettore(sID);
            return type;
        }

        // Se solo soglia inferiore valorizzata -> Soglia Inferiore
        if (dto.sogliaInferiore() != null && dto.sogliaSuperiore() == null
                && dto.valore() == null && dto.operatore() == null) {
            SogliaType type = SogliaType.INFERIOR;
            type.sogliaTypeObject = new SogliaInferiore(sID, dto.sogliaInferiore());
            return type;
        }

        // Se solo soglia superiore valorizzata -> Soglia Superiore
        if (dto.sogliaInferiore() == null && dto.sogliaSuperiore() != null
                && dto.valore() == null && dto.operatore() == null) {
            SogliaType type = SogliaType.SUPERIOR;
            type.sogliaTypeObject = new SogliaSuperiore(sID, dto.sogliaSuperiore());
            return type;
        }

        // Se sia soglia inferiore che superiore valorizzate -> Soglia Range
        if (dto.sogliaInferiore() != null && dto.sogliaSuperiore() != null
                && dto.valore() == null && dto.operatore() == null) {
            SogliaType type = SogliaType.RANGE;
            type.sogliaTypeObject = new SogliaRange(sID, dto.sogliaInferiore(), dto.sogliaSuperiore());
            return type;
        }

        // Se solo valore ed operatore valorizzati -> Soglia Contenuto
        if (dto.sogliaInferiore() == null && dto.sogliaSuperiore() == null
                && dto.valore() != null && dto.operatore() != null) {
            SogliaType type = SogliaType.CONTENT;
            type.sogliaTypeObject = new SogliaContenuto(sID, dto.operatore(), dto.valore());
            return type;
        }

        // Fallire obbligatoriamente perchè l'esecuzione di Azioni dipende da questa logica
        throw new IllegalSogliaDefinitionException("Impossible to determine Soglia configuration, " +
                "none or too many fields set.");
    }
}

