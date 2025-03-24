package it.dmi.structure.exceptions.impl.entities;

public class IllegalSogliaDefinitionException extends Exception {

    public IllegalSogliaDefinitionException() {
    }

    public IllegalSogliaDefinitionException(String message) {
        super(message);
    }

    public IllegalSogliaDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalSogliaDefinitionException(Throwable cause) {
        super(cause);
    }

    public IllegalSogliaDefinitionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
