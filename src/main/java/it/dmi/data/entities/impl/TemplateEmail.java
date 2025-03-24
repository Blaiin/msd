package it.dmi.data.entities.impl;

import it.dmi.data.entities.AEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Entity
@Table(name = "\"MON_TemplateEmail\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class TemplateEmail extends AEntity {

    @Id
    @SequenceGenerator(name = "templateEmailSeq", sequenceName = "SESAMO.\"MON_TemplateEmailSeq\"", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "templateEmailSeq")
    @Column(name = "\"TemplateEmailID\"")
    private Long id;

    @Column(name = "\"EmailBody\"")
    private String emailBody;
}
