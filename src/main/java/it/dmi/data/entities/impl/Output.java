package it.dmi.data.entities.impl;


import it.dmi.data.entities.AEntity;
import it.dmi.utils.jpa_converters.JSONConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "\"MON_Output\"")
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class Output extends AEntity {

    @Id
    @SequenceGenerator(name = "outputSeq", sequenceName = "SESAMO.\"MON_OutputSeq\"", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outputSeq")
    @Column(name = "\"OutputID\"")
    private Long id;

    @Column(name = "\"Inizio\"")
    private LocalDateTime inizio;

    @Column(name = "\"Fine\"")
    private LocalDateTime fine;

    @Column(name = "\"Durata\"")
    private String durata;

    @Column(name = "\"Esito\"")
    private Character esito;

    @Convert(converter = JSONConverter.class)
    @Column(name = "\"Contenuto\"")
    private Map<String, List<Object>> contenuto;

    @Column(name = "\"ConfigurazioneID\"")
    private Long configurazioneId;

    @Column(name = "\"AzioneID\"")
    private Long azioneId;

    @Column(name = "\"TipoAzioneID\"")
    private Long tipoAzioneId;

    @Column(name = "\"ThreadID\"")
    private String threadID;

    @Column(name = "\"ThreadName\"")
    private String threadName;

    @Column(name = "\"ConfigOutputID\"")
    private Long configOutputID;

    public Output(Long id, LocalDateTime inizio, LocalDateTime fine, String durata,
                  Character esito, Map<String, ?> contenuto, Long configurazioneId,
                  Long azioneId, Long tipoAzioneId, String threadID, String threadName, Long configOutputID) {
        this.id = id;
        this.inizio = inizio;
        this.fine = fine;
        this.durata = durata;
        this.esito = esito;
        this.contenuto = adapt(contenuto);
        this.configurazioneId = configurazioneId;
        this.azioneId = azioneId;
        this.tipoAzioneId = tipoAzioneId;
        this.threadID = threadID;
        this.threadName = threadName;
        this.configOutputID = configOutputID;
    }

    @Override
    public String toString() {
        return "Output[id=" + id + ",inizio=" + inizio + ",fine=" + fine +
                ",durata=" + durata + ",esito=" + esito + ",contenuto=" + contenuto +
                ",configurazioneId=" + configurazioneId + ",azioneId=" + azioneId +
                ",tipoAzioneId=" + tipoAzioneId + ",threadID='" + threadID + '\'' +
                ",threadName='" + threadName + ']';
    }

    private @NotNull Map<String, List<Object>> adapt(Map<String, ?> content) {
        Map<String, List<Object>> adaptedContent = new HashMap<>();
        if (content == null) return adaptedContent;
        try {
            content.forEach((k, v) -> {
                if(v instanceof Integer i) {
                    List<Integer> countList = Collections.singletonList(i);
                    adaptedContent.put(k, Collections.singletonList(countList));
                }
                if(v instanceof List<?> l) {
                    List<Object> typeSafeList = new ArrayList<>(l);
                    adaptedContent.put(k, typeSafeList);
                }
                if(v instanceof String s) {
                    adaptedContent.put(k, List.of(s));
                }
            });
        } catch (Exception e) {
            if (this.configurazioneId != null) {
                log.error("There was an error while trying to generate containers for Config {}.",
                        this.configurazioneId, e);
            } else if (this.azioneId != null) {
                log.error("There was an error while trying to generate containers for Azione {}.",
                        this.azioneId, e);
            } else {
                log.error("There was an error while trying to generate an containers.", e);
            }
        }
        return adaptedContent;
    }
}


