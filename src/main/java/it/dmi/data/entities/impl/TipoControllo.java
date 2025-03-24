package it.dmi.data.entities.impl;

import it.dmi.data.entities.AEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "\"MON_TipoControllo\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoControllo extends AEntity {

    @Id
    @SequenceGenerator(name = "tipoControlloSeq", sequenceName = "SESAMO.\"MON_TipoControlloSeq\"", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tipoControlloSeq")
    @Column(name = "\"TipoControlloID\"")
    private Long id;

    @Column(name = "\"Descrizione\"")
    private String descrizione;

}
