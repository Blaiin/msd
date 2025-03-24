package it.dmi.quartz.jobs.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.dmi.data.dto.AzioneDTO;
import it.dmi.data.dto.QuartzTask;
import it.dmi.data.dto.TemplateEmailDTO;
import it.dmi.processors.ResultsProcessor;
import it.dmi.processors.jobs.QueryResolver;
import it.dmi.structure.containers.OutputContainer;
import it.dmi.structure.containers.SQLMailerResult;
import it.dmi.structure.exceptions.impl.persistence.DatabaseConnectionException;
import it.dmi.structure.exceptions.impl.persistence.QueryFailureException;
import it.dmi.structure.internal.QueryType;
import it.dmi.structure.internal.info.DBInfo;
import it.dmi.system.emails.EmailUtils;
import it.dmi.utils.ConnectionParameters;
import it.dmi.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static it.dmi.utils.constants.NamingConstants.ID;
import static it.dmi.utils.constants.NamingConstants.TASK;
import static org.apache.logging.log4j.util.Strings.isBlank;

@Slf4j
public class SQLResultMailerJob extends BaseSQLJob {

    @Override
    public void execute(@NotNull JobExecutionContext context) throws JobExecutionException {
        final var dataMap = getDataMap(context);
        final var taskID = dataMap.getString(ID);
        final var task = (QuartzTask) dataMap.get(TASK + taskID);
        final var container = getContainer(taskID, dataMap);
        final var queryResults = executeQuery(task, container);
        sendEmail(task, queryResults);
    }

    private void sendEmail(@NotNull QuartzTask task, @Nullable String queryResults) throws JobExecutionException {
        if (!(task instanceof AzioneDTO azione)) {
            log.error("Cannot send email from a Configurazione, invalid database set up");
            return;
        }
        final var emailTo = azione.destinatario();
        final var emailCC = azione.destinatarioCC();
        final TemplateEmailDTO template = azione.templateEmail();
        if (template == null) {
            log.error("Not a valid template found, cannot send email");
            return;
        }
        var populatedBody = populateBody(azione, template.emailBody(), queryResults);
        if (sendEmail(emailTo, emailCC, populatedBody)) {
            log.info("Successfully sent email to: {}, Task: {} {}", emailTo, task.taskName(), task.strID());
        } else {
            log.error("Could not send email to: {}, Task: {} {}", emailTo, task.taskName(), task.strID());
        }
    }

    //Email di report per esecuzione query ->
    // Informazioni task:
    //              nome = {__TASKNAME__},
    //              id = {__TASKID__},
    //              script da eseguire = {__SQLSCRIPT__},
    //              schedulato per = {__SCHEDULAZIONE_AS_TIME},
    //              eseguito alle = {__EXECUTED_AT},
    //              stato = {__STATUS__}
    //
    //Risultato query -> {__QUERYRESULT__}

    private String populateBody(@NotNull AzioneDTO azione,  String emailBody, @Nullable String queryResults) {
        Map<String, String> replacements = Map.of(
                "{__TASKNAME__}", azione.taskName(),
                "{__TASKID__}", azione.strID(),
                "{__SQLSCRIPT__}", azione.sqlScript(),
                "{__SCHEDULAZIONE_AS_TIME__}", TimeUtils.nowAsTime(),
                "{__EXECUTED_AT__}", TimeUtils.nowAsTime(),
                "{__STATUS__}", queryResults != null ? "success" : "failure",
                "{__QUERYRESULT__}", queryResults != null ? queryResults : "N/A"
        );

        for (Map.Entry<String, String> entry : replacements.entrySet())
            emailBody = emailBody.replace(entry.getKey(), entry.getValue());

        return emailBody;
    }

    private boolean sendEmail(String emailTo, String emailCC, String body) throws JobExecutionException {
        if (isBlank(emailTo)) {
            log.error("Email recipients were null, cannot sent email");
            return false;
        }
        log.debug("Trying to send email to: {}, cc: {}", emailTo, emailCC);
        try {
            final boolean sent = EmailUtils.sendEmail(emailTo, emailCC, "Query execution and report",
                    body != null ? body : "No content");
            if (sent) log.info("Email sent → To: {}, CC: {}, Body: {}", emailTo, emailCC, body);
            else log.warn("Couldn't send email.");
            return sent;
        } catch (Exception e) {
            log.error("Failed to sent message: {}", e.getMessage());
            return false;
        }
    }

    private String executeQuery(@NotNull QuartzTask task, @NotNull OutputContainer container)
            throws JobExecutionException {
        final var dbInfo = DBInfo.from(task);
        final QueryType queryType = parseQuery(task.sqlScript());
        final var mapper = new ObjectMapper();
        switch (queryType) {
            case SELECT -> {
                try (ConnectionParameters params = query(dbInfo)) {
                    if (params == null) {
                        logDatabaseError(task);
                        return "";
                    }
                    ResultSet set = (ResultSet) params.result();
                    Map<String, List<String>> results = ResultsProcessor.processSelectResultSet(set);
                    if (container.setOutputResults(results)) logContentCached(task);
                    else logContentCachingFailure(task);
                    return mapper.writeValueAsString(results);
                } catch (ClassCastException | QueryFailureException | DatabaseConnectionException | SQLException e) {
                    resolveException(e);
                    return "QUERY_EXECUTION_FAILED";
                } catch (JsonProcessingException e) {
                    resolveException(e);
                    return "RESULTS_NOT_READABLE";
                }
            }
            case SELECT_COUNT -> {
                try (ConnectionParameters params = query(dbInfo)) {
                    if (params == null) {
                        logDatabaseError(task);
                        return "";
                    }
                    ResultSet set = (ResultSet) params.result();
                    int result = ResultsProcessor.processCountResultSet(set);
                    if (container.setOutputResults(result)) logContentCached(task);
                    else logContentCachingFailure(task);

                    return mapper.writeValueAsString(result);
                } catch (ClassCastException | QueryFailureException | DatabaseConnectionException | SQLException e) {
                    resolveException(e);
                    return "QUERY_EXECUTION_FAILED";
                } catch (JsonProcessingException e) {
                    resolveException(e);
                    return "RESULTS_NOT_READABLE";
                }
            }
            case INSERT, UPDATE, DELETE -> {
                try (ConnectionParameters params = query(dbInfo)) {
                    if (params == null) {
                        logDatabaseError(task);
                        return "";
                    }
                    String sqlMailerResultsKey = "results";
                    if (queryType == QueryType.INSERT) sqlMailerResultsKey = "insert_results";
                    if (queryType == QueryType.UPDATE) sqlMailerResultsKey = "update_results";
                    if (queryType == QueryType.DELETE) sqlMailerResultsKey = "delete_results";
                    Integer queryResult = (Integer) params.result();
                    int result = ResultsProcessor.processIUDResultObj(queryResult);

                    if (container.setOutputResults(new SQLMailerResult(sqlMailerResultsKey, result))) logContentCached(task);
                    else logContentCachingFailure(task);

                    return mapper.writeValueAsString(result);
                } catch (ClassCastException | QueryFailureException | DatabaseConnectionException | SQLException e) {
                    resolveException(e);
                    return "QUERY_EXECUTION_FAILED";
                } catch (JsonProcessingException e) {
                    resolveException(e);
                    return "RESULTS_NOT_READABLE";
                }
            }
            default -> throw new IllegalStateException("Could not execute query to retrieve result to email");
        }
    }

    private static void logDatabaseError(@NotNull QuartzTask task) {
        log.error("There was a problem at persistence level ({} {})", task.taskName(), task.strID());
    }

    private void logContentCachingFailure(@NotNull QuartzTask task) {
        log.debug("Could not cache contents for {} {}", task.taskName(), task.strID());
    }

    private void logContentCached(@NotNull QuartzTask task) {
        log.info("Content cached for {} {}", task.taskName(),
                task.strID());
    }

    private @NotNull QueryType parseQuery(String script) {
        try {
            return QueryResolver.resolveQuery(script);
        } catch (JSQLParserException e) {
            log.error("", e);
            return QueryType.INVALID;
        }
    }
}
