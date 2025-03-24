package it.dmi.quartz.jobs.sql;

import it.dmi.data.dto.AzioneDTO;
import it.dmi.data.dto.ConfigurazioneDTO;
import it.dmi.data.dto.QuartzTask;
import it.dmi.processors.ResultsProcessor;
import it.dmi.processors.Comparator;
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
import java.util.Map;

import static it.dmi.utils.constants.NamingConstants.*;

@Slf4j
public class SelectJob extends BaseSQLJob {

    @Override
    public void execute(@NotNull JobExecutionContext context) throws JobExecutionException {
        final var dataMap = getDataMap(context);
        final var taskID = dataMap.getString(ID);
        final var task = (QuartzTask) dataMap.get(TASK + taskID);
        final var container = getContainer(taskID, dataMap);

        if (task == null) {
            log.error("Task not found in context for ID: {}", taskID);
            return;
        }
        logTaskExecution(task, taskID);
        DBInfo dbInfo = DBInfo.from(task);
        try (ConnectionParameters params = query(dbInfo)) {
            if (params == null) {
                log.error("There was a problem at persistence level ({} {})", task.taskName(), taskID);
                return;
            }
            final Map<String, List<String>> results = processSQLResults(params, container, task, taskID);
            if (task instanceof ConfigurazioneDTO config)
                applyThresholds(config, results, container);
        } catch (ClassCastException e) {
            log.error("Bad result type of select query ({} {}), cannot process results", task.taskName(), taskID);
        } catch (Exception e) {
            resolveException(e);
        }
    }

    private void applyThresholds(@NotNull ConfigurazioneDTO config, Map<String,
            List<String>> results, OutputContainer container) {
        if (config.soglieDTOs().isEmpty()) {
            log.warn("No soglie found for {} {}.", config.taskName(), config.id());
            return;
        }
        List<AzioneDTO> azioni = Comparator.compareContent(config.strID(), config.soglieDTOs(), results);
        if (azioni.isEmpty()) {
            log.debug("No azioni were retrieved for {} {}.", config.taskName(), config.id());
            return;
        }
        log.info("Retrieved {} azioni for {} {}.", azioni.size(), config.taskName(), config.id());
        if (!container.addAzioni(azioni)) {
            log.warn("Failed to store azioni for {} {}.", config.taskName(), config.id());
        } else log.debug("Azioni ready to be scheduled: {}", azioni);
    }

    private @NotNull Map<String, List<String>> processSQLResults(@NotNull ConnectionParameters params,
                                                                        @NotNull OutputContainer container,
                                                                        @NotNull QuartzTask task,
                                                                        @NotNull String taskID)
            throws SQLException {
        ResultSet set = (ResultSet) params.result();
        Map<String, List<String>> results = ResultsProcessor.processSelectResultSet(set);
        if (!results.isEmpty()) log.info("Data found ({} {}).", task.taskName(), taskID);
        else log.warn("No data found ({} {}).", task.taskName(), taskID);

        if (container.setOutputResults(results)) log.debug("Output content cached for {} {}", task.taskName(), taskID);
        else log.debug("Could not cache contents for {} {}", task.taskName(), taskID);
        return results;
    }
}
