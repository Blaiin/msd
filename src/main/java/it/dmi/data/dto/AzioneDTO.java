package it.dmi.data.dto;

import it.dmi.data.dto.idto.IDTO;
import it.dmi.data.entities.impl.Azione;
import it.dmi.structure.internal.JobType;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static it.dmi.utils.Utils.Strings.extrapolateCCs;
import static it.dmi.utils.jobs.QuartzUtils.*;

@Slf4j
public final class AzioneDTO implements IDTO<Azione>, QuartzTask {

    private final Long id;
    private final String destinatario;
    private final String sqlScript;
    private final String programma;
    private final String classe;
    private final TipoAzioneDTO tipoAzione;
    private final SogliaDTO soglia;
    private final ControlloDTO controllo;
    private final TipoControlloDTO tipoControllo;
    private final AmbitoDTO ambito;
    private final FonteDatiDTO fonteDati;
    private final SicurezzaFonteDatiDTO utenteFonteDati;
    private final Integer ordineAzione;
    private final TemplateEmailDTO templateEmail;
    private final String destinatarioCC;
    private final List<String> ccs;

    private final AtomicInteger failureCount = new AtomicInteger(0);
    private JobKey assignedJobKey;
    private JobType resolvedJobType;

    @Override
    public String strID() {
        return String.valueOf(this.id);
    }

    @Override
    public TipoAzioneDTO tipoAzione() {
        return this.tipoAzione;
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
        throw new IllegalStateException("Cannot retrieve TipoControllo ref from an AzioneDTO instance.");
    }

    public @NotNull JobType getJobType() {
        if (this.resolvedJobType != null) return this.resolvedJobType;

        JobType resolvedType = JobType.NOT_VALID;

        if (isSQLAndEmailJob(this)) resolvedType = JobType.EMAIL_PLUS_SQL;
        else {
            final boolean sqlJob = isSQLJob(this);
            final boolean emailJob = isEmailJob(this);
            final boolean programJob = isProgramJob(this);
            final boolean classJob = isClassJob(this);
            if (ambiguousSetUp(
                    emailJob, sqlJob, programJob, classJob)) {
                log.error("Invalid configuration for Azione {}, none or too many fields found", this.id);
            }
            else if (emailJob) resolvedType = JobType.EMAIL;

            else if (sqlJob) resolvedType = resolveSQLJobType();

            else if (programJob) resolvedType = JobType.PROGRAM;

            else if (classJob) resolvedType = JobType.CLASS;

            else log.error("Invalid configuration for Azione {}, needed fields were not initialized", this.id);
        }

        return this.resolvedJobType = resolvedType;
    }

    private JobType resolveSQLJobType() {
        try {
            return JobType.fromSQLScript(this.sqlScript);
        } catch (JSQLParserException e) {
            log.debug("Query parsing exception (Azione {}): ", this.id, e);
            log.error("Query parsing exception (Azione {}): {}", this.id, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.debug("Error while resolving job type: ", e);
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

    public AzioneDTO(@NotNull Azione a) {
        this.id = a.getId();
        this.destinatario = a.getDestinatario();
        this.sqlScript = a.getSqlScript();
        this.programma = a.getProgramma();
        this.classe = a.getClasse();
        this.tipoAzione = new TipoAzioneDTO(a.getTipoAzione());
        this.soglia = new SogliaDTO(a.getSoglia());
        this.controllo = a.getControllo() != null ? new ControlloDTO(a.getControllo()) : null;
        this.tipoControllo = a.getTipoControllo() != null ? new TipoControlloDTO(a.getTipoControllo()) : null;
        this.ambito = a.getAmbito() != null ? new AmbitoDTO(a.getAmbito()) : null;
        this.fonteDati = a.getFonteDati() != null ? new FonteDatiDTO(a.getFonteDati()) : null;
        this.utenteFonteDati = a.getUtenteFonteDati() != null ? new SicurezzaFonteDatiDTO(a.getUtenteFonteDati()) : null;
        this.ordineAzione = a.getOrdineAzione();
        this.templateEmail = a.getTemplateEmail() != null ? new TemplateEmailDTO(a.getTemplateEmail()) : null;
        this.destinatarioCC = a.getDestinatarioCC();
        this.ccs = extrapolateCCs(a.getDestinatarioCC());
    }

    @Override
    public @NotNull Azione toEntity() {
        return new Azione(this.id,
                this.destinatario,
                this.sqlScript,
                this.programma,
                this.classe,
                this.tipoAzione != null ? this.tipoAzione.toEntity() : null,
                this.soglia != null ? this.soglia.toEntity() : null,
                this.controllo != null ? this.controllo.toEntity() : null,
                this.tipoControllo != null ? this.tipoControllo.toEntity() : null,
                this.ambito != null ? this.ambito.toEntity() : null,
                this.fonteDati != null ? this.fonteDati.toEntity() : null,
                this.utenteFonteDati != null ? this.utenteFonteDati.toEntity() : null,
                this.ordineAzione,
                this.templateEmail != null ? this.templateEmail.toEntity() : null,
                this.destinatarioCC
        );
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "[id=" + id + ']';
    }

    public Long id() {
        return this.id;
    }

    public int ordine() {
        return this.ordineAzione;
    }

    public SogliaDTO soglia() {
        return this.soglia;
    }

    public List<String> ccs() {
        return this.ccs;
    }

    public String destinatario() {
        return this.destinatario;
    }

    public String destinatarioCC() {
        return this.destinatarioCC;
    }

    public @Nullable TemplateEmailDTO templateEmail() {
        return this.templateEmail;
    }
}