package it.dmi.structure.exceptions.impl.files;

import it.dmi.structure.exceptions.MSDRuntimeException;

/**
 * Special type of {@link RuntimeException} that wraps up Files errors
 */
public class FileException extends MSDRuntimeException {
    public FileException(String message) {
        super(message);
    }

    public FileException(String message, Throwable e) {
        super(message, e);
    }
}
