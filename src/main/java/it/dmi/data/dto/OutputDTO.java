package it.dmi.data.dto;

import it.dmi.data.dto.idto.IDTO;
import it.dmi.data.entities.impl.Output;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public record OutputDTO(
        Long id,
        LocalDateTime inizio,
        LocalDateTime fine,
        String durata,
        Character esito,
        Map<String, ?> contenuto,
        Long configurazioneId,
        Long azioneId,
        Long tipoAzioneId,
        String threadID,
        String threadName,
        Long configOutputID) implements IDTO<Output> {

    public OutputDTO(LocalDateTime inizio, Long azioneId, Long tipoAzioneId, Long configurazioneId,
                     String threadID, String threadName) {
        this(null, inizio, null, null, null, null, configurazioneId, azioneId, tipoAzioneId,
                threadID, threadName, null);
    }

    public OutputDTO(@NotNull Output o) {
        this(o.getId(), o.getInizio(), o.getFine(), o.getDurata(), o.getEsito(), o.getContenuto(),
                o.getConfigurazioneId(), o.getAzioneId(), o.getTipoAzioneId(),
                o.getThreadID(), o.getThreadName(), o.getConfigOutputID());
    }

    @Contract("_ -> new")
    public @NotNull OutputDTO update(Long outputID) {
        return new OutputDTO(outputID, this.inizio, null, null, null, null,
                this.configurazioneId, this.azioneId, this.tipoAzioneId, this.threadID, this.threadName, this.configOutputID);
    }

    @Override
    public @NotNull Output toEntity() {
        return new Output(
                this.id,
                this.inizio,
                this.fine,
                this.durata,
                this.esito,
                this.contenuto,
                this.configurazioneId,
                this.azioneId,
                this.tipoAzioneId,
                this.threadID,
                this.threadName,
                this.configOutputID
        );
    }
}
