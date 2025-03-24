package it.dmi.structure.containers;

import it.dmi.data.dto.AzioneDTO;
import it.dmi.structure.internal.Esito;
import it.dmi.structure.internal.JobType;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

@Slf4j
public class OutputContainer {

    private static final int INITIALIZER_VALUE = -10;

    private final String tID;

    private final JobType jobType;

    private Integer rowsAffected;

    private Integer countResults;

    private Integer exitCode;
    private String programOutput;

    private Map<String, List<String>> selectResults;

    private Map<String, List<String>> emailResults;

    private @Nullable List<AzioneDTO> azioni;

    private SQLMailerResult sqlMailerResult;

    public OutputContainer(@NotNull String tID, @NotNull JobType type) {
        this.tID = tID;
        this.jobType = type;
        this.rowsAffected = INITIALIZER_VALUE;
        this.countResults = INITIALIZER_VALUE;
        this.exitCode = INITIALIZER_VALUE;
        this.programOutput = null;
        this.selectResults = null;
        this.emailResults = null;
        this.azioni = null;
    }

    /**
     * Calculates job Esito based on Job Type set at Container(this) construction time
     * @return String representing Esito value for db
     */
    public Character calculateEsito() {
        return switch (this.jobType) {
            case DUMMY_CONFIG -> Esito.POSITIVE.getCharValue();

            case EMAIL -> {
                if (this.emailResults == null || this.emailResults.get("success").getFirst().equals("false"))
                    yield Esito.NEGATIVE.getCharValue();
                yield Esito.POSITIVE.getCharValue();
            }
            case SQL_SELECT -> {
                if (selectResults == null) yield Esito.INVALID.getCharValue();
                if (selectResults.isEmpty()) yield Esito.NEGATIVE.getCharValue();
                yield Esito.POSITIVE.getCharValue();
            }
            case SQL_SELECT_COUNT -> {
                if (countResults == null) yield Esito.INVALID.getCharValue();
                if (countResults > INITIALIZER_VALUE) {
                    if (countResults == -1) yield Esito.INVALID.getCharValue();
                    yield Esito.POSITIVE.getCharValue();
                } yield Esito.NEGATIVE.getCharValue();
            }
            case SQL_INSERT, SQL_UPDATE, SQL_DELETE -> {
                if (rowsAffected == null) yield Esito.INVALID.getCharValue();
                if (rowsAffected > INITIALIZER_VALUE) {
                    if (rowsAffected == -1) yield Esito.NEGATIVE.getCharValue();
                    yield Esito.POSITIVE.getCharValue();
                } yield Esito.NEGATIVE.getCharValue();
            }
            case PROGRAM -> {
                if (exitCode != null && programOutput != null) yield Esito.POSITIVE.getCharValue();
                yield Esito.NEGATIVE.getCharValue();
            }
            default -> Esito.NEGATIVE.getCharValue();
        };
    }

    public @NotNull Map<String, ?> results() {
        return switch (this.jobType) {
            case DUMMY_CONFIG -> Map.of("dummy_results", List.of("dummy_result"));

            case EMAIL -> this.emailResults != null ?
                    this.emailResults : Map.of("email_sent", List.of("false"));

            case EMAIL_PLUS_SQL -> this.sqlMailerResult != null ?
                    Map.of(this.sqlMailerResult.resultIdentifier(), this.sqlMailerResult.result()) : Map.of();

            case SQL_SELECT -> this.selectResults != null ?
                    this.selectResults : Map.of("select_results", List.of("invalid"));

            case SQL_SELECT_COUNT -> {
                if (this.countResults == null) yield Map.of("count_results", List.of("invalid"));
                yield Map.of("count_results", List.of(this.countResults.toString()));
            }
            case SQL_INSERT -> {
                if (this.rowsAffected == null) yield Map.of("insert_rows_affected", List.of("invalid"));
                yield Map.of("insert_rows_affected", List.of(this.rowsAffected));
            }
            case SQL_UPDATE -> {
                if (this.rowsAffected == null) yield Map.of("update_rows_affected", List.of("invalid"));
                yield Map.of("update_rows_affected", List.of(this.rowsAffected));
            }
            case SQL_DELETE -> {
                if (this.rowsAffected == null) yield Map.of("delete_rows_affected", List.of("invalid"));
                yield Map.of("delete_rows_affected", List.of(this.rowsAffected));
            }
            case PROGRAM -> {
                if (this.exitCode == null || programOutput == null)
                    yield Map.of("program_exit_code", List.of("invalid"),
                            "program_output_txt", List.of("invalid"));
                yield Map.of("program_exit_code", List.of(this.exitCode),
                        "program_output_txt", List.of(this.programOutput));
            }
            default -> Map.of("invalid", List.of("invalid"));
        };
    }

    public boolean addAzioni(@Unmodifiable final List<AzioneDTO> azioniToAdd) {
        if (azioniToAdd == null || azioniToAdd.isEmpty()) return false;
        if (this.azioni == null) {
            this.azioni = new ArrayList<>();
        }
        final List<AzioneDTO> listToAdd = azioniToAdd.stream().filter(Objects::nonNull).toList();
        if (listToAdd.isEmpty()) return false;
        return this.azioni.addAll(listToAdd);
    }

    public boolean setOutputResults(@Nullable Integer results) {
        if (results == null) return false;
        if (results > INITIALIZER_VALUE) {
            switch (this.jobType) {
                case SQL_SELECT_COUNT -> this.countResults = results;
                case SQL_INSERT, SQL_UPDATE, SQL_DELETE -> this.rowsAffected = results;
                default -> {
                    return false;
                }
            }
            return true;
        } return false;
    }

    public boolean setOutputResults(@Nullable Integer exitCode, @Nullable String programResults) {
        if (exitCode == null || programResults == null) return false;
        if (this.jobType == JobType.PROGRAM) {
            this.exitCode = exitCode;
            this.programOutput = programResults;
            return true;
        }
        return false;
    }

    public boolean setOutputResults(SQLMailerResult result) {
        if (result == null) {
            log.warn("Cannot cache invalid results");
            return false;
        }
        this.sqlMailerResult = result;
        return true;
    }

    public boolean setOutputResults(@Nullable Map<String, List<String>> results) {
        if (results == null) {
            log.warn("Cannot cache null results");
            return false;
        }
        switch (this.jobType) {
            case EMAIL -> this.emailResults = results;
            case SQL_SELECT -> this.selectResults = results;
            default -> {
                return false;
            }
        }
        return true;
    }

    public @NotNull @Unmodifiable List<AzioneDTO> getAzioni() {
        if (this.azioni == null) return List.of();
        if (this.azioni.stream().filter(Objects::nonNull).toList().isEmpty()) {
            log.warn("List of Azioni to be scheduled found for Config {} contained only null values. ", this.tID);
            return List.of();
        }
        List<AzioneDTO> azioniCopy = List.copyOf(this.azioni);
        this.azioni = null;
        return azioniCopy;
    }
}
