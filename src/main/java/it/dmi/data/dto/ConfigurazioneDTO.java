package it.dmi.data.dto;

import it.dmi.data.dto.idto.IDTO;
import it.dmi.data.entities.impl.Configurazione;
import it.dmi.data.entities.impl.Soglia;
import it.dmi.structure.internal.JobType;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static it.dmi.utils.jobs.QuartzUtils.*;

@Slf4j
public final class ConfigurazioneDTO implements IDTO<Configurazione>, QuartzTask {

    private final Long id;
    private final String nome;
    private final String sqlScript;
    private final String programma;
    private final String classe;
    private final String schedulazione;
    private final ControlloDTO controllo;
    private final TipoControlloDTO tipoControllo;
    private final AmbitoDTO ambito;
    private final FonteDatiDTO fonteDati;
    private final SicurezzaFonteDatiDTO utenteFonteDati;
    private final Integer ordineConfigurazione;
    private final List<Soglia> soglie;

    private JobKey assignedJobKey;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private JobType resolvedJobType;

    public ConfigurazioneDTO(String nome, String sqlScript, String programma, String classe,
                             String schedulazione, int ordineConfigurazione) {
        this.nome = nome;
        this.sqlScript = sqlScript;
        this.programma = programma;
        this.classe = classe;
        this.schedulazione = schedulazione;
        this.ordineConfigurazione = ordineConfigurazione;

        this.id = null;
        this.controllo = null;
        this.tipoControllo = null;
        this.ambito = null;
        this.fonteDati = null;
        this.utenteFonteDati = null;
        this.soglie = null;
    }

    public ConfigurazioneDTO(@NotNull Configurazione c) {
        this.id = c.getId();
        this.nome = c.getNome();
        this.sqlScript = c.getSqlScript();
        this.programma = c.getProgramma();
        this.classe = c.getClasse();
        this.schedulazione = c.getSchedulazione();
        this.controllo = new ControlloDTO(c.getControllo());
        this.tipoControllo = new TipoControlloDTO(c.getTipoControllo());
        this.ambito = new AmbitoDTO(c.getAmbito());
        this.fonteDati = c.getFonteDati() != null ? new FonteDatiDTO(c.getFonteDati()) : null;
        this.utenteFonteDati = c.getUtenteFonteDati() != null ? new SicurezzaFonteDatiDTO(c.getUtenteFonteDati()) : null;
        this.ordineConfigurazione = c.getOrdineConfigurazione();
        this.soglie = c.getSoglie();
    }

    public @NotNull List<SogliaDTO> soglieDTOs() {
        if (this.soglie == null) return new ArrayList<>(5);
        List<SogliaDTO> soglieDTOs = new ArrayList<>(this.soglie.size());
        SogliaDTO sDTO;
        for (var s : this.soglie) {
            sDTO = new SogliaDTO(s);
            soglieDTOs.add(sDTO);
        }
        return soglieDTOs;
    }

    @Override
    public String strID() {
        return String.valueOf(this.id);
    }

    @Override
    public String sqlScript() {
        return this.sqlScript;
    }

    @Override
    public String programma() {
        return this.programma;
    }

    @Override
    public String classe() {
        return this.classe;
    }

    @Override
    public FonteDatiDTO fonteDati() {
        return this.fonteDati;
    }

    @Override
    public SicurezzaFonteDatiDTO utenteFonteDati() {
        return this.utenteFonteDati;
    }

    @Override
    public TipoControlloDTO tipoControllo() {
        return this.tipoControllo;
    }

    @Contract(pure = true)
    @Override
    public @Nullable TipoAzioneDTO tipoAzione() {
        throw new IllegalStateException("Cannot retrieve TipoAzione ref from a ConfigurazioneDTO instance.");
    }

    @Override
    public @NotNull JobType getJobType() {
        if (this.resolvedJobType != null) return this.resolvedJobType;

        JobType resolvedJobType = JobType.NOT_VALID;
        //Skip validation if Tipo Controllo ID == 4 (Action firing)
        if (isActionFiringJob(this)) resolvedJobType = JobType.DUMMY_CONFIG;

        else {
            final boolean sqlJob = isSQLJob(this);
            final boolean programJob = isProgramJob(this);
            final boolean classJob = isClassJob(this);

            if (ambiguousSetUp(sqlJob, programJob, classJob))
                log.error("Invalid configuration for Configurazione {}, none or too many fields found", this.id);

            else if (sqlJob) resolvedJobType = resolveSQLJobType();

            else if (programJob) resolvedJobType = JobType.PROGRAM;

            else if (classJob) resolvedJobType = JobType.CLASS;

            else log.error("Invalid configuration for Configurazione {}, needed fields were not initialized", this.id);
        }

        return this.resolvedJobType = resolvedJobType;
    }

    private JobType resolveSQLJobType() {
        try {
            return JobType.fromSQLScript(this.sqlScript);
        } catch (JSQLParserException e) {
            log.debug("Query parsing exception (Config {}): ", this.id, e);
            log.error("Query parsing exception (Config {}): {}", this.id, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.debug("", e);
            log.error("Error while resolving job type: {}", e.getMessage());
        }
        return JobType.NOT_VALID;
    }

    @Override
    public void setJobKey(@NotNull JobKey key) {
        this.assignedJobKey = key;
    }

    @Override
    public @NotNull JobKey assignedJobKey() {
        return this.assignedJobKey;
    }

    @Override
    public boolean maxFailureReached() {
        return this.failureCount.get() == 3;
    }

    @Override
    public void incrementFailureCount(JobExecutionException e, LocalDateTime now) {
        this.failureCount.incrementAndGet();
    }

    @Override
    public @NotNull Configurazione toEntity() {
        return new Configurazione(
                this.id,
                this.nome,
                this.sqlScript,
                this.programma,
                this.classe,
                this.schedulazione,
                this.controllo.toEntity(),
                this.tipoControllo.toEntity(),
                this.ambito.toEntity(),
                this.fonteDati.toEntity(),
                this.utenteFonteDati.toEntity(),
                this.ordineConfigurazione,
                this.soglie);
    }

    public Long id() {
        return this.id;
    }

    public int ordine() {
        return this.ordineConfigurazione;
    }

    public String nome() {
        return this.nome;
    }

    public String schedulazione() {
        return this.schedulazione;
    }

    public ControlloDTO controllo() {
        return this.controllo;
    }
}