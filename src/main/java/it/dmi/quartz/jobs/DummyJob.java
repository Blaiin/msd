package it.dmi.quartz.jobs;

import it.dmi.data.dto.AzioneDTO;
import it.dmi.data.dto.ConfigurazioneDTO;
import it.dmi.structure.soglie.SogliaVettore;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionContext;

import java.util.List;
import java.util.Objects;

import static it.dmi.utils.constants.NamingConstants.ID;
import static it.dmi.utils.constants.NamingConstants.TASK;

@Slf4j
public class DummyJob extends MSDQuartzJob {

    @Override
    public void execute(@NotNull JobExecutionContext context) {
        final var dataMap = getDataMap(context);
        final var cID = dataMap.getString(ID);
        final var c = (ConfigurazioneDTO) dataMap.get(TASK + cID);
        var container = getContainer(cID, dataMap);
        final var azioni = getAzioniFromSoglieVettore(c);
        container.addAzioni(azioni);
    }

    private @NotNull List<AzioneDTO> getAzioniFromSoglieVettore(@NotNull ConfigurazioneDTO c) {
        return c.soglieDTOs().stream()
            .filter(Objects::nonNull)
            .filter(s -> s.getSogliaType() instanceof SogliaVettore)
            .flatMap(s -> s.azioni().stream())
            .filter(Objects::nonNull)
            .map(AzioneDTO::new)
            .toList();
    }
}
