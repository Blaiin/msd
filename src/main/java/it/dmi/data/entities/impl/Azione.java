package it.dmi.data.entities.impl;

import it.dmi.data.entities.AEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Entity
@Table(name = "\"MON_Azione\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Azione extends AEntity {

    @Id
    @SequenceGenerator(name = "azioneSeq", sequenceName = "SESAMO.\"MON_AzioneSeq\"", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "azioneSeq")
    @Column(name = "\"AzioneID\"")
    private Long id;

    @Column(name = "\"Destinatario\"")
    private String destinatario;

    @Column(name = "\"SQLScript\"")
    private String sqlScript;

    @Column(name = "\"Programma\"")
    private String programma;

    @Column(name = "\"Classe\"")
    private String classe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"TipoAzioneID\"")
    private TipoAzione tipoAzione;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"SogliaID\"")
    private Soglia soglia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"ControlloID\"")
    private Controllo controllo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"TipoControlloID\"")
    private TipoControllo tipoControllo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"AmbitoID\"")
    private Ambito ambito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"FonteDatiID\"")
    private FonteDati fonteDati;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"UtentiFonteDatiID\"")
    private SicurezzaFonteDati utenteFonteDati;

    @Column(name = "\"OrdineAzione\"")
    private int ordineAzione;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"TemplateEmailID\"")
    private TemplateEmail templateEmail;

    @Column(name = "\"DestinatarioCC\"")
    private String destinatarioCC;

    @Override
    public String toString() {
        String sql = "SQL Script", prog = "Programma", cl = "Classe", dest = "Destinatario";
        if(sqlScript != null) return "Azione: " + sql;
        else if(programma != null) return "Azione: " + prog;
        else if(classe != null) return "Azione: " + cl;
        else if(destinatario != null) return "Azione: " + dest;
        else return "Azione: not valid.";
    }
}
