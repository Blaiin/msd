package it.dmi.quartz.manager.components;

import it.dmi.data.api.service.OutputService;
import it.dmi.data.dto.AzioneDTO;
import it.dmi.data.dto.OutputDTO;
import it.dmi.data.dto.QuartzTask;
import it.dmi.quartz.listeners.MSDJobListener;
import it.dmi.quartz.manager.Manager;
import it.dmi.structure.internal.info.TaskInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobKey;
import org.quartz.SchedulerException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static it.dmi.utils.jobs.QuartzUtils.removeJob;

@Slf4j
@ApplicationScoped
public class JobEventHandler {

    private static final int MAX_FAILURES = 3;

    public void taskStarting(@NotNull TaskInfo info, @NotNull OutputDTO output, @NotNull MSDJobListener listener) {
        final String taskID = info.id();
        final String taskName = info.name();
        try {
            var oID = outputService.createAndGetID(output);
            log.debug("Output {} for {} {} initialized", oID, taskName, taskID);
            if (listener.setOutputID(oID)) return;
            log.debug("Could not set Output id for {} {}",taskName, taskID);
        } catch (Exception e) {
            log.error("Could not initialize Output for {} {}", taskName, taskID, e);
        }
    }

    public void taskVetoed(@NotNull QuartzTask task) {
        try {
            if (this.manager.getScheduler().checkExists(task.assignedJobKey())) {
                log.warn("{} {} job key exists but job was vetoed", task.taskName(), task.strID());
            }
        } catch (SchedulerException e) {
            log.error("", e);
        }
    }

    public void taskFailed(@NotNull QuartzTask task, @NotNull Exception e) {
        log.error("{} {} job failed. {}", task.taskName(), task.strID(), e.getMessage());
        //log.debug("{} {} job failed. ", task.taskName(), task.strID(), e);
        FAILURE_CACHE.computeIfAbsent(task.assignedJobKey(), jobKey -> new AtomicInteger(0));
        if (FAILURE_CACHE.containsKey(task.assignedJobKey())) {
            if (FAILURE_CACHE.get(task.assignedJobKey()).incrementAndGet() == MAX_FAILURES) {
                if (removeJob(this.manager.getScheduler(), task.assignedJobKey())) {
                    log.warn("Removed scheduled Job ({} {}) because it kept failing.", task.taskName(), task.strID());
                    return;
                }
                log.warn("Could not remove job for {} {}", task.taskName(), task.strID());
            }
        }
    }

    public void taskCompleted(@NotNull QuartzTask task, @NotNull OutputDTO output, List<AzioneDTO> azioni) {
        try {
            final String taskName = task.taskName();
            final String taskID = task.strID();
            log.debug("Job completion verification ({} {})", taskName, taskID);
            final OutputDTO updated = outputService.update(output);
            if (updated != null) {
                log.info("Output {} from {} {} updated.", updated.id(), taskName, taskID);
            } else log.error("Could not update output container for {} {}.", taskName, taskID);

            if (azioni == null) return;

            if (azioni.isEmpty()) {
                log.warn("No actions to be scheduled for Config {}", taskID);
                return;
            }
            final List<AzioneDTO> sortedAzioni = azioni.stream()
                    .sorted(Comparator.comparingInt(AzioneDTO::ordine))
                    .toList();
            log.debug("Scheduling {} Azioni: {}", sortedAzioni.size(), sortedAzioni);
            //this.manager.scheduleActions(task.strID(), sortedAzioni);
            this.manager.getDispatcher().schedule(taskID, sortedAzioni);
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }

    @Inject private OutputService outputService;

    private Manager manager;

    public void bindManager(@NotNull Manager manager) {
        this.manager = manager;
    }

    private static final Map<JobKey, AtomicInteger> FAILURE_CACHE = new HashMap<>();

}
