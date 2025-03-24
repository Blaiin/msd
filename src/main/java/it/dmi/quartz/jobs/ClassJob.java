package it.dmi.quartz.jobs;

import it.dmi.data.dto.QuartzTask;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static it.dmi.utils.constants.NamingConstants.ID;
import static it.dmi.utils.constants.NamingConstants.TASK;

@Slf4j
public class ClassJob extends MSDQuartzJob {

    @Override
    public void execute (@NotNull JobExecutionContext context) throws JobExecutionException {
        final var dataMap = getDataMap(context);
        final var taskID = dataMap.getString(ID);
        final var task = (QuartzTask) dataMap.get(TASK + taskID);
        log.debug("Executing CLASSE from {} {}", task.taskName(), taskID);
    }
}
