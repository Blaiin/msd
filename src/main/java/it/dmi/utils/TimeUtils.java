package it.dmi.utils;

import jakarta.ejb.Local;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TimeUtils {

    public static boolean timeStampLessThan(String a, String b) {
        try {
            final var timeStamp1 = LocalDateTime.parse(a);
            final var timeStamp2 = LocalDateTime.parse(b);
            return timeStamp1.isBefore(timeStamp2);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean timeStampGreaterThan(String a, String b) {
        try {
            final var ts1 = LocalDateTime.parse(a);
            final var ts2 = LocalDateTime.parse(b);
            return ts1.isAfter(ts2);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean dateLessThan(String a, String b) {
        try {
            final var d1 = LocalDate.parse(a);
            final var d2 = LocalDate.parse(b);
            return d1.isBefore(d2);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean dateGreaterThan(String a, String b) {
        try {
            final var d1 = LocalDate.parse(a);
            final var d2 = LocalDate.parse(b);
            return d1.isAfter(d2);
        } catch (Exception e) {
            return false;
        }
    }

    public static String calculateDurata(LocalDateTime inizio, LocalDateTime fine) {
        if (inizio != null && fine != null) {
            return TimeUtils.durationAsTime(inizio, fine);
        }
        return null;
    }

    /**
     * Calculates the duration in seconds between two temporal points.
     *
     * @param inizio (inclusive) the start temporal point
     * @param fine (exclusive) the end temporal point
     * @return the duration in seconds between the start and end temporal points
     */
    public static @NotNull LocalDateTime duration(@NotNull final LocalDateTime inizio,
                                                  @NotNull final LocalDateTime fine) {
        Duration duration = Duration.between(inizio, fine);

        final long days = duration.toDays();
        duration = duration.minusDays(days);
        final long hours = duration.toHours();
        duration = duration.minusHours(hours);
        final long minutes = duration.toMinutes();
        duration = duration.minusMinutes(minutes);
        final long seconds = duration.getSeconds();
        duration = duration.minusSeconds(seconds);
        final long nanos = duration.getNano();

        return LocalDateTime.of(0, 1, 1,
                        0, 0, 0, 0)
                .plusDays(days)
                .plusHours(hours)
                .plusMinutes(minutes)
                .plusSeconds(seconds)
                .plusNanos(nanos);
    }

    public static @NotNull String durationAsTime(@NotNull final LocalDateTime inizio,
                                           @NotNull final LocalDateTime fine) {
        return duration(inizio, fine).toString().substring(11);
    }

    @Contract(" -> new")
    public static @NotNull LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static @NotNull String nowAsTime() {
        return LocalDateTime.now().toString().replace("T", " ");
    }

}
