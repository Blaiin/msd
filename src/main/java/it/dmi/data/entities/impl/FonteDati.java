package it.dmi.data.entities.impl;

import it.dmi.data.entities.AEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "\"MON_FonteDati\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FonteDati extends AEntity {

    @Id
    @SequenceGenerator(name = "fonteDatiSeq", sequenceName = "SESAMO.\"MON_FonteDatiSeq\"", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fonteDatiSeq")
    @Column(name = "\"FonteDatiID\"")
    private Long id;

    @Column(name = "\"Descrizione\"")
    private String descrizione;

    @Column(name = "\"NomeDriver\"")
    private String nomeDriver;

    @Column(name = "\"NomeClasse\"")
    private String nomeClasse;

    @Column(name = "\"URL\"")
    private String url;

    @Column(name = "\"JNDIName\"")
    private String jndiName;

}

