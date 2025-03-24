package it.dmi.utils.jobs;

import it.dmi.data.dto.AzioneDTO;
import it.dmi.data.dto.ConfigurazioneDTO;
import it.dmi.data.dto.OutputDTO;
import it.dmi.data.dto.QuartzTask;
import it.dmi.utils.TimeUtils;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

import static it.dmi.utils.TimeUtils.calculateDurata;

@Slf4j
@ApplicationScoped
public class OutputUtils {

    public static @NotNull OutputDTO initializeOutputDTO(@NotNull QuartzTask task) {
        Long aID = null;
        Long taID = null;
        Long cID = null;
        switch (task) {
            case AzioneDTO a -> {
                aID = a.id();
                taID = a.tipoAzione().id();
            }
            case ConfigurazioneDTO c -> cID = c.id();
        }
        return new OutputDTO(TimeUtils.now(), aID, taID, cID,
                String.valueOf(Thread.currentThread().threadId()), Thread.currentThread().getName());
    }

    public static @NotNull OutputDTO finalizeOutputDTO(@NotNull OutputDTO toFinalize, Character esito,
                                                       Map<String, ?> contenuto, LocalDateTime fine) {
        return new OutputDTO(toFinalize.id(), toFinalize.inizio(), fine, calculateDurata(toFinalize.inizio(), fine),
                esito, contenuto, toFinalize.configurazioneId(), toFinalize.azioneId(), toFinalize.tipoAzioneId(),
                toFinalize.threadID(), toFinalize.threadName(), toFinalize.configOutputID());
    }
}
