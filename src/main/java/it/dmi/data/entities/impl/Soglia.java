package it.dmi.data.entities.impl;

import it.dmi.data.entities.AEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "\"MON_Soglia\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Soglia extends AEntity {

    @Id
    @SequenceGenerator(name = "sogliaSeq", sequenceName = "SESAMO.\"MON_SogliaSeq\"", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sogliaSeq")
    @Column(name = "\"SogliaID\"")
    private Long id;

    @Column(name = "\"SogliaInferiore\"")
    private Double sogliaInferiore;

    @Column(name = "\"SogliaSuperiore\"")
    private Double sogliaSuperiore;

    @Column(name = "\"Valore\"")
    private String valore;

    @Column(name = "\"Operatore\"", length = 10)
    private String operatore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"ConfigurazioneID\"")
    private Configurazione configurazione;

    @OneToMany(mappedBy = "soglia", fetch = FetchType.LAZY)
    private List<Azione> azioni;

    public Soglia(Long id, Double sogliaInferiore, Double sogliaSuperiore,
                   String valore, String operatore, Configurazione configurazione) {
        this.id = id;
        this.sogliaInferiore = sogliaInferiore;
        this.sogliaSuperiore = sogliaSuperiore;
        this.valore = valore;
        this.operatore = operatore;
        this.configurazione = configurazione;
    }



    public String getStringID() {
        return String.valueOf(this.id);
    }
}

