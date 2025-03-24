package it.dmi.data.dto;

import it.dmi.data.dto.idto.IDTO;
import it.dmi.data.entities.impl.Azione;
import it.dmi.data.entities.impl.Configurazione;
import it.dmi.data.entities.impl.Soglia;
import it.dmi.structure.exceptions.impl.entities.IllegalSogliaDefinitionException;
import it.dmi.structure.soglie.Comparable;
import it.dmi.structure.soglie.SogliaType;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Slf4j
public record SogliaDTO(
        Long id,
        Double sogliaInferiore,
        Double sogliaSuperiore,
        String valore,
        String operatore,
        Configurazione configurazione,
        List<Azione> azioni) implements IDTO<Soglia> {

    public SogliaDTO(double sogliaInferiore, double sogliaSuperiore, String valore, String operatore) {
        this(null, sogliaInferiore, sogliaSuperiore, valore, operatore, null, null);
    }

    public int getAzioniSize() {
        return azioni.size();
    }

    public String strID() {
        return String.valueOf(this.id);
    }

    public SogliaDTO(@NotNull Soglia s) {
        this(s.getId(), s.getSogliaInferiore(), s.getSogliaSuperiore(), s.getValore(), s.getOperatore(),
                s.getConfigurazione(), s.getAzioni());
    }

    public @Nullable Comparable getSogliaType() {
        try {
            return SogliaType.resolve(this).getSogliaTypeObject();
        } catch (IllegalSogliaDefinitionException e) {
            log.warn("Invalid Soglia type. (id: {})", this.id);
            return null;
        }
    }

    @Override
    public @NotNull Soglia toEntity() {
        return new Soglia(
                this.id,
                this.sogliaInferiore,
                this.sogliaSuperiore,
                this.valore,
                this.operatore,
                this.configurazione,
                this.azioni
        );
    }
}
