package it.dmi.quartz.scheduler;

import it.dmi.data.api.service.ConfigurazioneService;
import it.dmi.data.dto.SogliaDTO;
import it.dmi.data.entities.impl.Configurazione;
import it.dmi.structure.exceptions.MSDRuntimeException;
import it.dmi.utils.file.PropsLoader;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import static it.dmi.utils.file.PropsLoader.loadQuartzProperties;

@Getter
@Singleton
@Startup
@Slf4j
public class MSDScheduler {

    private org.quartz.Scheduler msdScheduler;

    @Inject
    @Getter(AccessLevel.NONE)
    private ConfigurazioneService configurazioneService;

    @PostConstruct
    public void initialize() {
        try {
            var configsList = configurazioneService.getAll();
            int configCount = configsList.size();
            log.info("Detected {} possible CONFIGS to be scheduled.", configCount);
            int azioniCount = configsList.stream()
                    .flatMap(Configurazione::getSoglieDTOAsStream)
                    .mapToInt(SogliaDTO::getAzioniSize)
                    .sum();
            log.info("Detected {} possible AZIONI to be executed.", azioniCount);
            var props = loadQuartzProperties();
            if (!props.isEmpty()) {
                StdSchedulerFactory factory = new StdSchedulerFactory(props);
                msdScheduler = factory.getScheduler();
                msdScheduler.start();
                log.info("Job scheduler initialized.");
                return;
            }
            log.error("Could not load properties for Monitoraggio Sistema Documentale Scheduler initialization");
        } catch (SchedulerException | RuntimeException e) {
            log.error("Failed to start Quartz scheduler. {}", e.getMessage(), e.getCause());
            throw new MSDRuntimeException("Failed to start Quartz scheduler", e);
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            if (msdScheduler != null) {
                log.debug("Shutting down job scheduler..");
                msdScheduler.shutdown();
            }
        } catch (SchedulerException e) {
            log.error("Failed to shutdown Quartz scheduler. {}", e.getMessage(), e.getCause());
            throw new MSDRuntimeException("Failed to shutdown Quartz scheduler", e);
        }
    }

}
