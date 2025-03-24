package it.dmi.structure.internal;

import it.dmi.processors.jobs.QueryResolver;
import it.dmi.quartz.jobs.ClassJob;
import it.dmi.quartz.jobs.DummyJob;
import it.dmi.quartz.jobs.EmailJob;
import it.dmi.quartz.jobs.ProgramJob;
import it.dmi.quartz.jobs.sql.InsertUpdateDeleteJob;
import it.dmi.quartz.jobs.sql.SQLResultMailerJob;
import it.dmi.quartz.jobs.sql.SelectCountJob;
import it.dmi.quartz.jobs.sql.SelectJob;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import org.jetbrains.annotations.NotNull;
import org.quartz.Job;

@Slf4j
@Getter
public enum JobType {

    EMAIL("EMAIL", EmailJob.class),

    EMAIL_PLUS_SQL("SQL_RESULT_MAILER", SQLResultMailerJob.class),

    SQL_SELECT("SQL_SELECT", SelectJob.class),
    SQL_SELECT_COUNT("SQL_SELECT_COUNT", SelectCountJob.class),
    SQL_INSERT("SQL_INSERT", InsertUpdateDeleteJob.class),
    SQL_UPDATE("SQL_UPDATE", InsertUpdateDeleteJob.class),
    SQL_DELETE("SQL_DELETE", InsertUpdateDeleteJob.class),

    PROGRAM("PROGRAM", ProgramJob.class),

    CLASS("CLASS", ClassJob.class),

    DUMMY_CONFIG("DUMMY_JOB", DummyJob.class),

    NOT_VALID("NOT_VALID", null);

    private final String name;

    private final Class<? extends Job> jobClass;

    JobType (String name, Class<? extends Job> jobClass) {
        this.name = name;
        this.jobClass = jobClass;
    }

    public static @NotNull JobType fromSQLScript(String script) throws JSQLParserException, IllegalArgumentException {
        final var queryType = QueryResolver.resolveQuery(script);
        return switch (queryType) {
            case SELECT -> JobType.SQL_SELECT;
            case SELECT_COUNT -> JobType.SQL_SELECT_COUNT;
            case INSERT -> JobType.SQL_INSERT;
            case UPDATE -> JobType.SQL_UPDATE;
            case DELETE -> JobType.SQL_DELETE;
            default -> JobType.NOT_VALID;
        };
    }
}
