package it.dmi.quartz.jobs;

import it.dmi.structure.containers.OutputContainer;
import org.jetbrains.annotations.NotNull;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import static it.dmi.utils.constants.NamingConstants.CONTAINER;

public abstract class MSDQuartzJob implements Job {

    protected OutputContainer getContainer(@NotNull String taskID, @NotNull JobDataMap map) {
        return (OutputContainer) map.get(CONTAINER + taskID);
    }

    protected JobDataMap getDataMap(@NotNull JobExecutionContext context) {
        return context.getJobDetail().getJobDataMap();
    }
}
