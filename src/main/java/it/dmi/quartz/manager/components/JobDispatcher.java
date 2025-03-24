package it.dmi.quartz.manager.components;

import it.dmi.data.dto.AzioneDTO;
import it.dmi.data.dto.ConfigurazioneDTO;
import it.dmi.data.dto.QuartzTask;
import it.dmi.quartz.builders.JobInfoFactory;
import it.dmi.quartz.manager.Manager;
import it.dmi.quartz.threadPool.MSDThreadFactory;
import it.dmi.structure.exceptions.MSDRuntimeException;
import it.dmi.structure.internal.info.JobInfo;
import it.dmi.structure.internal.qualifiers.PossiblyEmpty;
import it.dmi.utils.jobs.QuartzUtils;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quartz.SchedulerException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static it.dmi.utils.jobs.QuartzUtils.jobExists;
import static it.dmi.utils.jobs.QuartzUtils.removeJob;

@Slf4j
@ApplicationScoped
public class JobDispatcher {

    public void schedule(@NotNull @PossiblyEmpty List<ConfigurazioneDTO> configurazioni) {
        log.info("Scheduling configs..");
        configurazioni.forEach(c -> {
            final var cID = c.strID();
            final String taskName = c.taskName();
            log.debug("Reading {} {}", taskName, cID);
            final var jobInfo = JobInfoFactory.build(c);
            if (!jobInfo.isValid()) {
                logConstructionError(taskName, cID);
                return;
            }
            if (jobExists(this.manager.getScheduler(), jobInfo.jobDetail().getKey())) {
                log.warn("Not scheduling job for {} {} because it already exists.", taskName, cID);
                return;
            }
            log.debug("Processing {} {}", taskName, cID);
            QuartzUtils.addListener(this.manager, c, jobInfo);
            CompletableFuture.runAsync(() -> this.manager.getDispatcher().schedule(c, jobInfo), executor);
        });
    }

    public void schedule(@NotNull String cID, @Nullable List<AzioneDTO> azioni) {
        if (azioni == null || azioni.isEmpty()) {
            log.warn("Could not find any Azione to be scheduled for Configurazione {}", cID);
            return;
        }
        azioni.forEach(a -> {
            log.debug("Reading {} {} from Soglia {}", a.taskName(), a.strID(), a.soglia().id());
            final var jobInfo = JobInfoFactory.build(a);
            if (!jobInfo.isValid()) {
                logConstructionError(a.taskName(), a.strID());
                return;
            }

            if(jobExists(this.manager.getScheduler(), jobInfo.jobDetail().getKey())) {
                removeJob(this.manager.getScheduler(), a.assignedJobKey());
                log.debug("Deleted job key for {} {} to allow refiring", a.taskName(), a.strID());
            }
            QuartzUtils.addListener(this.manager, a, jobInfo);
            CompletableFuture.runAsync(() -> this.manager.getDispatcher().schedule(a, jobInfo), executor);
        });
    }

    public void schedule(@NotNull QuartzTask task, @NotNull JobInfo info) {
        final var taskID = task.strID();
        final String taskName = task.taskName();
        try {
            log.info("Scheduling job for {} {}", taskName, taskID);
            this.manager.getScheduler().scheduleJob(info.jobDetail(), info.trigger());
        } catch (SchedulerException e) {
            log.error("Scheduler error for {} {}: {}", taskName, taskID, e.getMessage());
            log.debug("", e);
            throw new MSDRuntimeException(e);
        } catch (Exception e) {
            log.error("Unexpected error while scheduling job for {} {}: {}", taskName, taskID, e.getMessage());
            log.debug("", e);
        }
    }

    public void bindManager(@NotNull Manager manager) {
        this.manager = manager;
    }

    private void logConstructionError(String taskName, String cID) {
        log.error("Error constructing job for {} {}", taskName, cID);
    }

    private Manager manager;

    private final ExecutorService executor;

    public JobDispatcher() {
        this.executor = MSDThreadFactory.createExecutor();
    }
}
