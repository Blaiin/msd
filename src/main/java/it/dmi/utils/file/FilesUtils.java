package it.dmi.utils.file;

import it.dmi.structure.exceptions.impl.files.FileException;
import it.dmi.structure.exceptions.impl.files.FileNotUsableException;
import it.dmi.structure.exceptions.impl.files.NotAFileException;
import it.dmi.structure.exceptions.impl.files.NotAValidDirectory;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class FilesUtils {

    /**
     *
     * @param path path to resolve to a file
     * @param checkForReadabilityAndExecution whether to check if the resolved file is readable and executable
     * @return file if and only if it was possible to resolve to a file
     * @throws NotAFileException if the given path did not result in a file
     * @throws FileNotUsableException if resolved file is not readable nor executable
     */
    public static @NotNull File resolveFile(@NotNull String path, boolean checkForReadabilityAndExecution) {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            throw new NotAFileException("The path does not point to a valid file: " + path);
        }
        if (checkForReadabilityAndExecution) {
            if (!file.canRead()) {
                throw new FileNotUsableException("The file is not readable: " + path);
            }
            if (!file.canExecute()) {
                throw new FileNotUsableException("The file is not executable: " + path);
            }
        }

        return file;
    }

    /**
     *
     * @param path path to resolve to the closest directory of a file
     * @return the path to the working directory
     * @throws NotAValidDirectory if resolved directory is not actually a valid directory
     */
    public static @NotNull File resolveWorkingDirectory(@NotNull String path) {
        final var normalPath = Paths.get(path).normalize().toString();
        File directory = new File(normalPath).getParentFile();
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            throw new NotAValidDirectory("Closest directory to file was invalid: " + path);
        }

        return directory;
    }


    /**
     * By defaults uses UTF-8 charset to read from a given file and parse content to a String for execution
     * @param programFile file containing a program code
     * @return {@link String} containing code
     * @throws FileException when an {@link IOException} occurs
     */
    public static @NotNull String fromFile(@NotNull File programFile) {
        try {
            return Files.readString(programFile.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FileException("Failed to read contents from file: " +
                    programFile.getAbsolutePath(), e);
        }
    }
}
