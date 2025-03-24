package it.dmi.quartz.builders;

import it.dmi.data.dto.ConfigurazioneDTO;
import it.dmi.data.dto.QuartzTask;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quartz.*;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Slf4j
public class JobTriggerBuilder extends MSDJobBuilder {

    private static final int DEFAULT_DELAY = 10;

    //TODO let Configurazione have a simple trigger when cron schedule not available
    protected static Trigger build(@NotNull QuartzTask task, @NotNull JobIdentity identity) {
        final var key = new TriggerKey(identity.triggerName(), identity.triggerGroup());
        if (task instanceof ConfigurazioneDTO c) {
            if (isNotBlank(c.schedulazione()) && CronExpression.isValidExpression(c.schedulazione()))
                return newTrigger(key, c);
            else if (isNotBlank(c.schedulazione()) && !(CronExpression.isValidExpression(c.schedulazione()))) throw new IllegalArgumentException(
                    String.format("Not a valid CronExpression. Task: %s %s, Expression: %s",
                            c.taskName(), c.strID(), c.schedulazione()));
        }
        return newTrigger(key, task);
    }

    private static Trigger newTrigger(@NotNull TriggerKey key, @NotNull QuartzTask task) {
        final var trigger =
                TriggerBuilder
                        .newTrigger()
                        .withPriority(task.ordine())
                        .withIdentity(key)
                        .startAt(DateBuilder.futureDate(DEFAULT_DELAY, DateBuilder.IntervalUnit.SECOND))
                        .build();
        logTriggerCreation(task);
        return trigger;
    }

    private static @Nullable Trigger newTrigger(@NotNull TriggerKey key, @NotNull ConfigurazioneDTO c) {
        final var trigger = TriggerBuilder.newTrigger()
                .withPriority(c.ordine())
                .withIdentity(key)
                .withSchedule(CronScheduleBuilder.cronSchedule(c.schedulazione()))
                .build();
        logTriggerCreation(c);
        return trigger;
    }

    private static void logTriggerCreation(@NotNull QuartzTask task) {
        log.debug("Trigger for {} {} created", task.taskName(), task.strID());
    }
}
