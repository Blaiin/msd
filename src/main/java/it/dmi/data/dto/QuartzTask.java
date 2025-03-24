package it.dmi.data.dto;

import it.dmi.structure.definitions.ActionType;
import it.dmi.structure.definitions.ControlType;
import it.dmi.structure.internal.JobType;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import java.time.LocalDateTime;

public sealed interface QuartzTask permits AzioneDTO, ConfigurazioneDTO {

    String strID();

    String sqlScript();

    String programma();

    String classe();

    FonteDatiDTO fonteDati();

    SicurezzaFonteDatiDTO utenteFonteDati();

    TipoControlloDTO tipoControllo();

    TipoAzioneDTO tipoAzione();

    default @NotNull ControlType controlType() {
        return ControlType.fromID(tipoControllo());
    }

    default @NotNull ActionType actionType() {
        return ActionType.fromID(tipoAzione());
    }

    @NotNull JobType getJobType();

    /**
     *
     * @param email if is an email job
     * @param sql if is a sql job
     * @param program if is a program job
     * @param classe if is a class job
     * @return {@code true} if none are true or more than {@code 1} is true, otherwise {@code false}
     */
    default boolean ambiguousSetUp(boolean email, boolean sql, boolean program, boolean classe) {
        if (!email && !sql && !program && !classe) {
            return true;
        }
        return (email && sql) || (email && program) || (email && classe) ||
                (sql && program) || (sql && classe) ||
                (program && classe);
    }

    /**
     *
     * @param sql if is a sql job
     * @param program if is a program job
     * @param classe if is a class job
     * @return {@code true} if none are true, {@code true} if more than {@code 1} is true,
     * otherwise {@code false}
     */
    default boolean ambiguousSetUp(boolean sql, boolean program, boolean classe) {
        return ambiguousSetUp(false, sql, program, classe);
    }

    default String taskName() {
        return this.getClass().getSimpleName().replace("DTO", "");
    }

    void setJobKey(@NotNull JobKey key);

    @NotNull JobKey assignedJobKey();

    boolean maxFailureReached();

    void incrementFailureCount(JobExecutionException e, LocalDateTime now);

    int ordine();
}
