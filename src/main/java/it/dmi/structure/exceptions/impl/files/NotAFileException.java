package it.dmi.structure.exceptions.impl.files;

public class NotAFileException extends FileException {

    public NotAFileException(String path) {
        super(String.format("Path '%s' did not result in a file.", path));
    }


}
