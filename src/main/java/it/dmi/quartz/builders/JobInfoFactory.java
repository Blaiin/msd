package it.dmi.quartz.builders;

import it.dmi.data.dto.QuartzTask;
import it.dmi.structure.containers.OutputContainer;
import it.dmi.structure.exceptions.impl.quartz.JobTypeException;
import it.dmi.structure.internal.JobType;
import it.dmi.structure.internal.info.JobInfo;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.quartz.*;

import java.util.Objects;

import static it.dmi.utils.constants.NamingConstants.*;

@Slf4j
public class JobInfoFactory extends MSDJobBuilder {

    public static @NotNull JobInfo build(@NotNull QuartzTask task) {
        try {
            final var identity = JobIdentity.of(task);

            final var dataMap = dataMap(task);

            final var jobDetail = jobDetail(task, dataMap, identity);

            final var trigger = trigger(task, identity);

            Objects.requireNonNull(trigger, "Trigger is required.");
            log.debug("Job identity for {} {} created.", identity.name(), task.strID());
            return JobInfo.buildNew(jobDetail, trigger);
        } catch (NullPointerException e) {
            log.error("", e);
            return new JobInfo(null, null, false);
        } catch (JobTypeException e) {
            log.debug("Job type not valid ", e);
            log.error("Job type was not valid: {}", e.getMessage());
            return new JobInfo(null, null, false);
        }
    }

    private static @NotNull JobDataMap dataMap(@NotNull QuartzTask task) {
        var map = new JobDataMap();
        final var taskID = task.strID();
        map.put(ID, taskID);
        map.put(TASK + taskID, task);
        map.put(CONTAINER + taskID, new OutputContainer(taskID, task.getJobType()));
        return map;
    }

    private static @NotNull JobDetail jobDetail(@NotNull QuartzTask task,
                                                @NotNull JobDataMap map,
                                                @NotNull JobIdentity identity) throws JobTypeException {
        final var jobKey = new JobKey(identity.jobName(), identity.jobGroup());
        if (task.getJobType() == JobType.NOT_VALID) throw new JobTypeException("Invalid job configurations.");
        return JobBuilder
                .newJob(task.getJobType().getJobClass())
                .withIdentity(jobKey)
                .usingJobData(map)
                .build();
    }

    private static Trigger trigger(@NotNull QuartzTask task, @NotNull JobIdentity identity) {
        return JobTriggerBuilder.build(task, identity);
    }
}
