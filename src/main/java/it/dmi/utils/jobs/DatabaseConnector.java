package it.dmi.utils.jobs;

import it.dmi.structure.exceptions.impl.persistence.DatabaseConnectionException;
import it.dmi.structure.internal.info.DBInfo;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Optional;

@Slf4j
public class DatabaseConnector {

    public static @NotNull Optional<Connection> connect(@NotNull DBInfo info)
            throws DatabaseConnectionException {

        //Short-circuit invalid DBInfo obj
        validateDBInfo(info);

        return Optional.ofNullable(
                info.throughJNDI() ?
                //Try JNDI connection first
                tryJNDIConnection(info) :
                //If url, username and password != null -> try connection with those values as a fallback
                tryDirectConnection(info)
        );
    }

    private static @Nullable Connection tryJNDIConnection(@NotNull DBInfo info) {
        DataSource datasource;
        Object lookedUp = null;
        try {
            lookedUp = new InitialContext().lookup(info.jndi());
            datasource = (DataSource) lookedUp;
            if (datasource == null) return null;
            Connection jndiConnection = datasource.getConnection();
            if (jndiConnection == null) return null;
            log.info("JNDI connection successful ({} {})", info.taskName(), info.id());
            return jndiConnection;
        } catch (NamingException n) {
            log.debug("JNDI not found. ", n);
            log.error("JNDI not found. {}", n.getMessage());
        } catch (ClassCastException c) {
            log.debug("Looked up a wrong JNDI type, expected: DataSource, found: {}",
                    lookedUp != null ? lookedUp.getClass().getName() : null, c);
            log.error("Looked up a wrong JNDI type. {}", c.getMessage());
        } catch (SQLTimeoutException t) {
            log.debug("Timeout exceeded while trying to connect. ", t);
            log.error("Timed out connection: {}", t.getMessage());
        } catch (SQLException s) {
            log.debug("Database access error while trying to connect. ", s);
            log.error("Database access error while connecting: {}", s.getMessage());
        } return null;
    }

    private static @Nullable Connection tryDirectConnection(@NotNull DBInfo info) {
        Connection directConnection = null;
        try {
            directConnection = DriverManager.getConnection(info.url(), info.user(), info.password());
            log.info("Parameters connection successful ({} {})", info.taskName(), info.id());
        } catch (SQLTimeoutException t) {
            log.debug("Connection timed out ({} {}): ", info.taskName(), info.id(), t);
            log.error("Connection timed out ({} {}): {}", info.taskName(), info.id(), t.getMessage());
        } catch (SQLException s) {
            log.debug("Failed to connect to specified URL ({} {}): ", info.taskName(), info.id(), s);
            log.error("Failed to connect to specified URL ({} {}): {}", info.taskName(), info.id(), s.getMessage());
        } catch (Exception e){
            log.debug("Failed to connect to database. ", e);
            log.error("Failed to connect to database. {}", e.getMessage());
        }
        return directConnection;
    }

    private static void validateDBInfo(@NotNull DBInfo dbInfo) throws DatabaseConnectionException {
        String invalidRequiredFields;
        if ((invalidRequiredFields = dbInfo.invalid()) != null)
            throw new DatabaseConnectionException("Found invalid required fields: " + invalidRequiredFields);
    }
}
