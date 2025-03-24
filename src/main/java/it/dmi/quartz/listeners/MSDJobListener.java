package it.dmi.quartz.listeners;

import it.dmi.data.dto.AzioneDTO;
import it.dmi.data.dto.ConfigurazioneDTO;
import it.dmi.data.dto.OutputDTO;
import it.dmi.data.dto.QuartzTask;
import it.dmi.quartz.manager.Manager;
import it.dmi.structure.containers.*;
import it.dmi.structure.internal.JobType;
import it.dmi.structure.internal.info.TaskInfo;
import it.dmi.utils.TimeUtils;
import it.dmi.utils.jobs.OutputUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.quartz.*;

import java.time.LocalDateTime;
import java.util.List;

import static it.dmi.utils.Utils.Strings.capitalize;
import static it.dmi.utils.constants.NamingConstants.CONTAINER;

@Slf4j
public final class MSDJobListener implements JobListener {

    private final Manager manager;

    private volatile OutputDTO output;

    private final String taskID;

    @Getter private final QuartzTask task;

    private final String taskName;

    private final String taskTypeName;

    private final TaskInfo taskInfo;

    @Getter
    private Long outputID;

    public MSDJobListener(@NotNull QuartzTask task, @NotNull Manager manager) {
        this.taskID = task.strID();
        this.task = task;
        this.manager = manager;
        this.taskName = task.taskName();
        this.taskTypeName = capitalize(task.getJobType().getName().toLowerCase().replace('_', ' '));
        this.taskInfo = new TaskInfo(this.taskID, this.taskName, this.taskTypeName);
    }

    @Contract(pure = true)
    @Override
    public @NotNull String getName() {
        String parentName = switch (this.task) {
            case AzioneDTO a ->
                    a.soglia().getClass().getSimpleName().replace("DTO", "") + a.soglia().id();
            case ConfigurazioneDTO c ->
                    c.controllo().getClass().getSimpleName().replace("DTO", "") + c.controllo().id();
        };
        return this.taskName + this.taskID + parentName + "JobListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        if (task.getJobType() == JobType.PROGRAM)
            log.debug("Preparing workspace for bat/cmd/sh execution ({} {}), creating output container.",
                    this.taskName, this.taskID);
        else if (task.getJobType() == JobType.DUMMY_CONFIG)
            log.debug("{} ({} {}) about to be executed, creating output container to fire Azioni directly.",
                    this.taskTypeName, this.taskName, this.taskID);
        else log.debug("{} job ({} {}) about to be executed, creating output container.", this.taskTypeName, this.taskName, this.taskID);
        this.output = OutputUtils.initializeOutputDTO(this.task);
        this.manager.getHandler().taskStarting(this.taskInfo, this.output, this);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        log.warn("Job was vetoed for {} {}", this.taskName, this.taskID);
        this.manager.getHandler().taskVetoed(this.task);
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException e) {
        if (e != null) {
            this.task.incrementFailureCount(e, LocalDateTime.now());
            onJobFailure(e);
            return;
        }
        log.debug("Job ({} {}) executed.", this.taskName, this.taskID);
        final var container = getContainer(context);
        onJobCompletion(finalizeOutput(container), container);
    }

    private void onJobFailure(@NotNull JobExecutionException e) {
        this.manager.getHandler().taskFailed(this.task, e);
    }

    private void onJobCompletion(OutputDTO out, OutputContainer container) {
        List<AzioneDTO> azioni = null;
        if (this.task instanceof ConfigurazioneDTO) {
            log.debug("Active Azioni for {} {} retrieved", this.taskName, this.taskID);
            azioni = container.getAzioni();
        }
        this.manager.getHandler().taskCompleted(this.task, out, azioni);
    }

    @Contract("_ -> new")
    private @NotNull OutputDTO finalizeOutput(@NotNull OutputContainer container) {
        return OutputUtils.finalizeOutputDTO(this.output.update(this.outputID),
                container.calculateEsito(),
                container.results(),
                TimeUtils.now());
    }

    private @NotNull OutputContainer getContainer(@NotNull JobExecutionContext jobExecutionContext) {
        final JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        return (OutputContainer) dataMap.get(CONTAINER + this.taskID);
    }

    public boolean setOutputID(Long id) {
        if (id == null) {
            log.warn("Could not keep track of Output ID for {} {}", this.taskName, this.taskID);
            return false;
        }
        this.outputID = id;
        log.debug("Successfully set Output id {} for {} {}, needed for later update",
                this.outputID, this.taskName, this.taskID);
        return true;
    }
}
