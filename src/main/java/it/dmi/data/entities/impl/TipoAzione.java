package it.dmi.data.entities.impl;

import it.dmi.data.entities.AEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "\"MON_TipoAzione\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoAzione extends AEntity {

    @Id
    @SequenceGenerator(name = "tipoAzioneSeq", sequenceName = "SESAMO.\"MON_TipoAzioneSeq\"", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tipoAzioneSeq")
    @Column(name = "\"TipoAzioneID\"")
    private Long id;

    @Column(name = "\"Descrizione\"")
    private String descrizione;

}
