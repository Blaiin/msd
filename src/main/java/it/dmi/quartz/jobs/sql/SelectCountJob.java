package it.dmi.quartz.jobs.sql;

import it.dmi.data.dto.AzioneDTO;
import it.dmi.data.dto.ConfigurazioneDTO;
import it.dmi.data.dto.QuartzTask;
import it.dmi.processors.Comparator;
import it.dmi.processors.ResultsProcessor;
import it.dmi.structure.containers.OutputContainer;
import it.dmi.structure.internal.info.DBInfo;
import it.dmi.utils.ConnectionParameters;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static it.dmi.utils.constants.NamingConstants.*;

@Slf4j
public class SelectCountJob extends BaseSQLJob {

    @Override
    public void execute (@NotNull JobExecutionContext context) throws JobExecutionException {
        final var dataMap = getDataMap(context);
        final var taskID = dataMap.getString(ID);
        final var task = (QuartzTask) dataMap.get(TASK + taskID);
        final var container = getContainer(taskID, dataMap);
        logTaskExecution(task, taskID);
        DBInfo dbInfo = DBInfo.from(task);
        try (ConnectionParameters params = query(dbInfo)) {
            if (params == null) {
                log.error("There was a problem at persistence level ({} {})", task.taskName(), taskID);
                return;
            }
            final int result = processSQLResults(params, task, taskID, container);

            if (!(task instanceof ConfigurazioneDTO config)) return;
            applyThresholds(config, taskID, result, container);
        } catch (ClassCastException e) {
            log.error("Bad result type of select count query ({} {}), cannot process results", task.taskName(), taskID);
        } catch (Exception e) {
            resolveException(e);
        }
    }

    private void applyThresholds(@NotNull ConfigurazioneDTO config, String taskID, int result, OutputContainer container) {
        if (config.soglieDTOs().isEmpty()) {
            log.warn("No soglie found for {} {}.", config.taskName(), taskID);
            return;
        }
        List<AzioneDTO> azioni = Comparator.compareCount(taskID, config.soglieDTOs(), result);
        if (azioni.isEmpty()) {
            log.warn("No azioni were retrieved for {} {}.", config.taskName(), taskID);
            return;
        }
        log.debug("Retrieved {} azioni for {} {}.", azioni.size(), config.taskName(), taskID);
        if (!container.addAzioni(azioni)) {
            log.warn("Failed to store azioni for {} {}.", config.taskName(), config.id());
        } else log.debug("Azioni ready to be scheduled: {}", azioni);
    }

    private int processSQLResults(@NotNull ConnectionParameters params,
                                         QuartzTask task, String taskID, OutputContainer container)
            throws SQLException {
        ResultSet set = (ResultSet) params.result();
        int result = ResultsProcessor.processCountResultSet(set);
        if (result == NO_DATA) log.warn("No data found ({} {}).", task.taskName(), taskID);
        else log.info("Data found ({} {}).", task.taskName(), taskID);

        if (container.setOutputResults(result))
            log.debug("Output content cached for {} {}", task.taskName(), taskID);
        else log.debug("Could not cache contents for {} {}", task.taskName(), taskID);
        return result;
    }
}
