package it.dmi.quartz.jobs;

import it.dmi.data.dto.AzioneDTO;
import it.dmi.data.dto.ConfigurazioneDTO;
import it.dmi.data.dto.QuartzTask;
import it.dmi.processors.Comparator;
import it.dmi.utils.file.FilesUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static it.dmi.utils.constants.NamingConstants.*;

@Slf4j
public class ProgramJob extends MSDQuartzJob {

    @Override
    public void execute(@NotNull JobExecutionContext context) throws JobExecutionException {
        final var dataMap = getDataMap(context);
        final var taskID = dataMap.getString(ID);
        final var task = (QuartzTask) dataMap.get(TASK + taskID);
        var container = getContainer(taskID, dataMap);

        final var programPath = task.programma();
        final File programFile = FilesUtils.resolveFile(programPath, true);
        String program = FilesUtils.fromFile(programFile);
        final File workingDirectory = FilesUtils.resolveWorkingDirectory(programPath);
        try {
            final var os = System.getProperty("os.name").toLowerCase();
            final ProcessBuilder processBuilder = newProcessBuilder(os, program, workingDirectory);
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(File.separator);
                }
            }
            int exitCode = process.waitFor();

            if (container.setOutputResults(exitCode, output.toString()))
                log.debug("Output content cached for {} {}",
                        task.taskName(), taskID);
            else log.debug("Could not cache contents for {} {}",
                    task.taskName(), taskID);

            if (task instanceof ConfigurazioneDTO c) {
                final var cID = c.strID();
                if (c.soglieDTOs().isEmpty()) {
                    log.warn("No Soglie for Config {}.", cID);
                    return;
                }
                List<AzioneDTO> azioni = Comparator.compareExitCode(cID, c.soglieDTOs(), exitCode);
                if (azioni.isEmpty()) {
                    log.warn("Empty Azioni list (Config {})", cID);
                    return;
                }
                log.debug("Populated Azioni list (Config {}): {}", cID, azioni);
                if (container.addAzioni(azioni))
                    log.debug("(Config {}) Azioni ready to be scheduled {}", cID, azioni);
            }
        } catch (IOException | InterruptedException | IndexOutOfBoundsException | SecurityException e) {
            log.error("", e);
            throw new JobExecutionException(e);
        }
    }

    private @NotNull ProcessBuilder newProcessBuilder(@NotNull String os, String program, File workingDirectory) {
        String[] command;
        if (os.contains("win")) {
            command = new String[]{Commands.CMD.commandType, Commands.CMD.executionType, program};
        } else {
            command = new String[]{Commands.BASH.commandType, Commands.BASH.executionType, program};
        }
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(command);
        processBuilder.directory(workingDirectory);
        processBuilder.redirectErrorStream(true);
        return processBuilder;
    }

    private enum Commands {
        CMD("cmd.exe", "/c"),
        SH("sh", "/c"),
        BASH("/bin/bash", "-c");

        private final String commandType;

        private final String executionType;

        Commands(String commandType, String executionType) {
            this.commandType = commandType;
            this.executionType = executionType;
        }
    }
}
