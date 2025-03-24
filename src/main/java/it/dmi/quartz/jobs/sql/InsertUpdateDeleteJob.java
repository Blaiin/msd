package it.dmi.quartz.jobs.sql;

import it.dmi.data.dto.QuartzTask;
import it.dmi.processors.ResultsProcessor;
import it.dmi.structure.containers.OutputContainer;
import it.dmi.structure.internal.info.DBInfo;
import it.dmi.utils.ConnectionParameters;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static it.dmi.utils.constants.NamingConstants.*;

@Slf4j
public class InsertUpdateDeleteJob extends BaseSQLJob {

    @Override
    public void execute(@NotNull JobExecutionContext context) throws JobExecutionException {
        final var dataMap = getDataMap(context);
        final var taskID = dataMap.getString(ID);
        final var task = (QuartzTask) dataMap.get(TASK + taskID);
        executeQuery(task, getContainer(taskID, dataMap));
    }

    private void executeQuery(@NotNull QuartzTask task, @NotNull OutputContainer container)
            throws JobExecutionException {
        final var taskID = task.strID();
        final var dbInfo = DBInfo.from(task);
        try (ConnectionParameters params = query(dbInfo)) {
            Integer queryResult = (Integer) params.result();
            int result = ResultsProcessor.processIUDResultObj(queryResult);
            if (result == NO_ROWS_AFFECTED)
                log.error("No rows were affected by query {} {}.", task.taskName(), taskID);
            else if (result == EXECUTED_WITH_NO_RESULTS)
                log.warn("Query executed successfully but no rows were affected {} {}.", task.taskName(), taskID);
            else log.info("Query executed successfully {} {}.", task.taskName(), taskID);
            if (container.setOutputResults(result)) {
                log.info("Content cached for {} {}", task.taskName(),
                        taskID);
            }
        } catch (ClassCastException e) {
        log.error("Bad result type of insert/update/delete query ({} {}), cannot process results",
                task.taskName(), taskID);
        } catch (Exception e) {
            resolveException(e);
        }
    }
}
