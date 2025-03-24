package it.dmi.utils.jobs;

import it.dmi.data.dto.AzioneDTO;
import it.dmi.data.dto.ConfigurazioneDTO;
import it.dmi.data.dto.QuartzTask;
import it.dmi.quartz.manager.Manager;
import it.dmi.quartz.listeners.MSDJobListener;
import it.dmi.structure.definitions.ActionType;
import it.dmi.structure.definitions.ControlType;
import it.dmi.structure.exceptions.MSDRuntimeException;
import it.dmi.structure.internal.JobType;
import it.dmi.structure.internal.info.JobInfo;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.quartz.*;
import org.quartz.impl.matchers.KeyMatcher;

import java.util.List;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Slf4j
public class QuartzUtils {

    public static boolean validateAndLog(@NotNull QuartzTask task) {
        JobType jobType = task.getJobType();
        log.debug("Validation phase: {} {} job type detected -> {}", task.taskName(), task.strID(), jobType);
        return jobType != JobType.NOT_VALID;
    }

    public static boolean isEmailJob(@NotNull QuartzTask task) {
        return task instanceof AzioneDTO a && isNotBlank(a.destinatario()) && a.actionType() == ActionType.SEND_EMAIL;
    }

    public static boolean isSQLAndEmailJob(@NotNull QuartzTask task) {
        return task instanceof AzioneDTO a && isNotBlank(a.destinatario()) && a.actionType() == ActionType.SQL_SEND_EMAIL_RESULTS;
    }

    public static boolean isSQLJob(@NotNull QuartzTask task) {
        return switch (task) {
            case AzioneDTO a -> isNotBlank(a.sqlScript()) && (a.actionType() == ActionType.EXECUTE_SQL);
            case ConfigurazioneDTO c -> {
                log.debug("Tipo controllo (Config {}): {}", task.strID(), c.controlType());
                final boolean valid = isNotBlank(c.sqlScript()) &&
                        (c.controlType() == ControlType.CONTENT_BASED || c.controlType() == ControlType.COUNT_BASED);
                log.debug("Valid query ({} {}): {}", task.taskName(), task.strID(), valid);
                yield valid;
            }
        };
    }

    public static boolean isProgramJob(@NotNull QuartzTask task) {
        return isNotBlank(task.programma());
    }

    public static boolean isClassJob(@NotNull QuartzTask task) {
        return isNotBlank(task.classe());
    }

    public static boolean isActionFiringJob(@NotNull QuartzTask task) {
        return task instanceof ConfigurazioneDTO c && c.tipoControllo().id().intValue() == ControlType.ACTION_FIRING.getCardinal();
    }

    public static boolean removeJob(@NotNull Scheduler scheduler,
                                 @NotNull JobKey jobKey) {
        try {
            ListenerManager listenerManager = scheduler.getListenerManager();
            List<JobListener> jobListeners = listenerManager.getJobListeners();

            for (JobListener listener : jobListeners) {
                if (listener instanceof MSDJobListener msdListener &&
                        KeyMatcher.keyEquals(msdListener.getTask().assignedJobKey()).isMatch(jobKey)) {
                    listenerManager.removeJobListener(listener.getName());
                }
            }
            return scheduler.deleteJob(jobKey);
        } catch (Exception ex) {
            log.error("Error trying to erase job data (jobKey: {}).", jobKey);
            return false;
        }
    }

    public static boolean jobExists(@NotNull Scheduler scheduler, JobKey jobKey) {
        try {
            return scheduler.checkExists(jobKey);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract(mutates = "param2")
    public static void addListener(@NotNull Manager manager, @NotNull QuartzTask task, @NotNull JobInfo jobInfo) {
        try {
            JobListener listener = new MSDJobListener(task, manager);
            manager.getScheduler().getListenerManager().addJobListener(listener,
                    KeyMatcher.keyEquals(jobInfo.jobDetail().getKey()));
        } catch (SchedulerException e) {
            log.error("Error adding Jobs Listener for {} {}", task.taskName(), task.strID());
            throw new MSDRuntimeException(e);
        }
    }
}
