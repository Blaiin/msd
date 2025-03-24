package it.dmi.data.dto;

import it.dmi.data.dto.idto.IDTO;
import it.dmi.data.entities.impl.TemplateEmail;
import org.jetbrains.annotations.NotNull;

public record TemplateEmailDTO(
        Long templateEmailID,
        String emailBody) implements IDTO<TemplateEmail> {

    public TemplateEmailDTO(@NotNull TemplateEmail t) {
        this(t.getId(), t.getEmailBody());
    }

    @Override
    public @NotNull TemplateEmail toEntity() {
        return new TemplateEmail(
                this.templateEmailID,
                this.emailBody
        );
    }
}
