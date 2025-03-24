package it.dmi.data.entities.application;

import it.dmi.data.entities.AEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "\"MON_ConfigApp\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationConfig extends AEntity {

    @Id
    @Column(name = "\"IDConfigApp\"")
    @SequenceGenerator(name = "configAppSeq", sequenceName = "SESAMO.\"MON_ConfigAppSeq\"", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "configAppSeq")
    private Long id;

    @Column(name = "\"Ricarica\"")
    private String reload;
}
