package it.dmi.structure.internal;

import lombok.Getter;

@Getter
public enum Esito {

    POSITIVE("Success", 'S'),

    NEGATIVE("Failure", 'N'),

    INVALID("N/A", 'I');

    private final String value;
    private final Character charValue;

    Esito(String value, Character charValue) {
        this.value = value;
        this.charValue = charValue;
    }
}


