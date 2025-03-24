package it.dmi.data.entities.impl;

import it.dmi.data.entities.AEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "\"MON_Controllo\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Controllo extends AEntity {

    @Id
    @SequenceGenerator(name = "controlloSeq", sequenceName = "SESAMO.\"MON_ControlloSeq\"", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "controlloSeq")
    @Column(name = "\"ControlloID\"")
    private Long id;

    @Column(name = "\"Descrizione\"")
    private String descrizione;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"TipoControlloID\"")
    private TipoControllo tipoControllo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"AmbitoID\"")
    private Ambito ambito;

    @OneToMany(mappedBy = "controllo", fetch = FetchType.LAZY)
    private List<Azione> azioni;

    @OneToMany(mappedBy = "controllo", fetch = FetchType.LAZY)
    private List<Configurazione> configurazioni;

    @Column(name = "\"ORDINECONTROLLO\"")
    private int ordineControllo;
}

