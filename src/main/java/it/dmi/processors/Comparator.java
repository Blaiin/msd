package it.dmi.processors;

import it.dmi.data.dto.AzioneDTO;
import it.dmi.data.dto.SogliaDTO;
import it.dmi.data.entities.impl.Azione;
import it.dmi.structure.soglie.*;
import it.dmi.structure.soglie.Comparable;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class Comparator {

    public static @NotNull @Unmodifiable List<AzioneDTO> compareCount(
            @NotNull final String cID, @Nullable List<SogliaDTO> soglie, final int result) {
        if (soglie == null) {
            logEmptySoglieList(cID);
            return List.of();
        }
        List<Azione> azioni = new ArrayList<>();
        soglie.forEach(s -> {
            final var soglia = s.getSogliaType();
            if (soglia == null) return;
            if (!(soglia instanceof SogliaInferiore) &&
                    !(soglia instanceof SogliaSuperiore) &&
                    !(soglia instanceof SogliaRange)) {
                logWrongSogliaType(cID, soglia);
                return;
            }
            if (!soglia.compare(result)) {
                log.warn("Result ({}) did not meet {} ({}) defined expectations.",
                        result, soglia.getClass().getSimpleName(), soglia.id());
                return;
            }
            log.info("Result ({}) met {} (id: {}) defined expectations.",
                    result, soglia.getClass().getSimpleName(), soglia.id());
            azioni.addAll(s.azioni());
        });
        final List<AzioneDTO> azioniDTOs = azioni.stream().map(AzioneDTO::new).toList();
        logActiveAzioni(cID, azioniDTOs);
        return azioniDTOs;
    }

    public static @NotNull @Unmodifiable List<AzioneDTO> compareContent(
            @NotNull String cID, @Nullable List<SogliaDTO> soglie, @NotNull Map<String, List<String>> mapToCompare) {
        if (soglie == null) {
            logEmptySoglieList(cID);
            return List.of();
        }
        List<Azione> azioni = new ArrayList<>();
        soglie.forEach(s -> {
            final var soglia = s.getSogliaType();
            if (soglia == null) return;
            if (!(soglia instanceof SogliaContenuto contentSoglia)) {
                logWrongSogliaType(cID, soglia);
                return;
            }
            if (!contentSoglia.compare(mapToCompare)) {
                log.warn("Results (Config {} select query) did not meet Soglia ({}) defined expectations.",
                        cID, contentSoglia.id());
                return;
            }
            log.info("Results (Select Query) met {} ({}) defined expectations, scheduling Azioni.",
                    soglia.getClass().getSimpleName(), soglia.id());
            azioni.addAll(s.azioni());
        });
        final List<AzioneDTO> azioniDTOs = azioni.stream().map(AzioneDTO::new).toList();
        logActiveAzioni(cID, azioniDTOs);
        return azioniDTOs;
    }

    public static @NotNull @Unmodifiable List<AzioneDTO> compareExitCode(
            @NotNull final String cID, @Nullable List<SogliaDTO> soglie, final int exitCode) {
        return compareCount(cID, soglie, exitCode);
    }

    private static void logEmptySoglieList(@NotNull String cID) {
        log.warn("No Soglie to be applied for Config {}", cID);
    }

    private static void logActiveAzioni(@NotNull String cID, List<AzioneDTO> azioniDTOs) {
        log.info("(Config {}) Active Azioni: {}", cID, azioniDTOs);
    }

    private static void logWrongSogliaType(@NotNull String cID, @NotNull Comparable soglia) {
        log.warn("Incompatible Soglia type (type: {}, id: {})  for query results (Config {}).",
                soglia.getClass().getSimpleName(), soglia.id(), cID);
    }
}

