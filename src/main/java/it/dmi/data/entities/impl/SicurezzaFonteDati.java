package it.dmi.data.entities.impl;

import it.dmi.data.entities.AEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "\"MON_SicurezzaFonteDati\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SicurezzaFonteDati extends AEntity {

    @Id
    @SequenceGenerator(name = "sicurezzaFonteDatiSeq", sequenceName = "SESAMO.\"MON_SicurezzaFonteDatiSeq\"", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sicurezzaFonteDatiSeq")
    @Column(name = "\"UtentiFonteDatiID\"")
    private Long id;

    @Column(name = "\"Descrizione\"")
    private String descrizione;

    @Column(name = "\"UserID\"")
    private String userID;

    @Column(name = "\"Password\"")
    private String password;

}

