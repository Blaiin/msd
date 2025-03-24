package it.dmi.quartz.jobs.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.dmi.data.dto.ConfigurazioneDTO;
import it.dmi.data.dto.QuartzTask;
import it.dmi.quartz.jobs.MSDQuartzJob;
import it.dmi.structure.exceptions.impl.internal.InvalidStateException;
import it.dmi.structure.exceptions.impl.persistence.DatabaseConnectionException;
import it.dmi.structure.exceptions.impl.persistence.InvalidCredentialsException;
import it.dmi.structure.exceptions.impl.persistence.QueryFailureException;
import it.dmi.structure.internal.info.DBInfo;
import it.dmi.utils.ConnectionParameters;
import it.dmi.utils.jobs.DatabaseConnector;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quartz.JobExecutionException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Slf4j
public abstract class BaseSQLJob extends MSDQuartzJob {

    protected static final int NO_ROWS_AFFECTED = -1;
    protected static final int EXECUTED_WITH_NO_RESULTS = 0;
    protected static final int NO_DATA = -1;


    protected void resolveException(@NotNull Throwable exc) throws JobExecutionException {
        final String msg = switch (exc) {
            case QueryFailureException qfE -> "Error while executing query. " + qfE.getMessage();
            case DatabaseConnectionException dcE -> "Error while connecting to database. " + dcE.getMessage();
            case InvalidCredentialsException icE -> "Could not connect to database. " + icE.getMessage();
            case SQLException sqlE -> "Query execution had problems. " + sqlE.getMessage();
            case JobExecutionException jeE -> "Jobs encountered an error while executing. " + jeE.getMessage();
            case NullPointerException npE -> "Necessary value was null. " + npE.getMessage();
            case InvalidStateException isE -> "Active state for object was illegal." + isE.getMessage();
            case JsonProcessingException jpE -> "Could not parse query results as a string. {}" + jpE.getMessage();
            default -> String.format("Nested exception: %s", exc.getMessage());
        };
        handleException(msg, exc);
    }

    protected @Nullable ConnectionParameters query(@NotNull DBInfo dbInfo) throws DatabaseConnectionException, QueryFailureException {
        //Don't do driver loading if jndi is available
        if (!dbInfo.throughJNDI()) {
            if (!loadDriver(dbInfo)) return null;
        }
        try {
            var connection = DatabaseConnector.connect(dbInfo);
            if (connection.isEmpty()) {
                log.warn("Could not establish a connection to database");
                return null;
            }
            log.debug("Connection established successfully.");
            PreparedStatement statement = connection.get().prepareStatement(dbInfo.sqlScript());
            ResultSet resultSet = null;
            final boolean executed = statement.execute();
            if (!executed && statement.getUpdateCount() == NO_ROWS_AFFECTED)
                throw new QueryFailureException("Query failed, result was not valid.");
            if (executed) resultSet = statement.getResultSet();
            if (resultSet != null) {
                log.debug("Select/SelectCount query executed successfully.");
                return new ConnectionParameters(connection.get(), statement, resultSet);
            }
            return new ConnectionParameters(connection.get(), statement, statement.getUpdateCount());
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    protected boolean loadDriver(@Nullable DBInfo dbInfo) throws DatabaseConnectionException {
        if (dbInfo == null) {
            log.error("Not able to load driver class.");
            throw  new DatabaseConnectionException("Not able to load driver class.");
        }
        //URL valorizzato
        if (isNotBlank(dbInfo.url())) {

            //Ma driver class non specificata -> default to Oracle
            if (isBlank(dbInfo.driverName())) {
                log.warn("URL found but no driver class specified, defaulting to Oracle driver");
                return loadDriverClass(Driver.ORACLE);
            }

            //Prova risoluzione driver
            final var resolvedDriver = Driver.resolve(dbInfo.driverName());
            if (resolvedDriver.isEmpty()) {
                throw new DatabaseConnectionException("No such driver found");
            }
            log.debug("Loading driver {}", resolvedDriver.get().name);
            return loadDriverClass(resolvedDriver.get());
        } return false;
    }

    private boolean loadDriverClass(@NotNull Driver driver) {
        try {
            Class.forName(driver.driver);
            log.debug("{} driver loading was successful.", driver.name);
            return true;
        } catch (ClassNotFoundException e) {
            log.error("Loading driver class failed: {}", driver.name, e);
            throw new RuntimeException(e);
        }
    }

    protected void logTaskExecution(QuartzTask task, String taskID) {
        if (task instanceof ConfigurazioneDTO c)
            log.info("Trying to execute job for {} {}, name: {}.", task.taskName(), taskID, c.nome());
        else log.info("Trying to execute job for {} {}", task.taskName(), taskID);
    }

    private void handleException(String msg, Throwable exc) throws JobExecutionException {
        log.debug("", exc);
        log.error(msg);
        throw new JobExecutionException(msg, exc);
    }

    private enum Driver {

        ORACLE("Oracle", "oracle.jdbc.OracleDriver"),

        POSTGRES("Postgres", "org.postgresql.Driver");

        private final String name;
        private final String driver;

        Driver(String name, String driver) {
            this.name = name;
            this.driver = driver;
        }

        static @NotNull Optional<Driver> resolve(@NotNull String driver) {
            return Arrays.stream(values())
                    .filter(d -> driver.contains(d.driver))
                    .findFirst();
        }
    }

}
