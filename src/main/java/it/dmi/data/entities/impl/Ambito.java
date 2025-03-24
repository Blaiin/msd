package it.dmi.data.entities.impl;

import it.dmi.data.entities.AEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "\"MON_Ambito\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ambito extends AEntity {

    @Id
    @SequenceGenerator(name = "ambitoSeq", sequenceName = "SESAMO.\"MON_AmbitoSeq\"", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ambitoSeq")
    @Column(name = "\"AmbitoID\"")
    private Long id;

    @Column(name = "\"Nome\"")
    private String nome;

    @Column(name = "\"Destinazione\"")
    private String destinazione;

}