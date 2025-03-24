package it.dmi.utils;

import it.dmi.data.dto.ConfigurazioneDTO;
import it.dmi.data.dto.ControlloDTO;
import it.dmi.processors.jobs.QueryResolver;
import it.dmi.utils.jobs.QuartzUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j(topic = "Utilities class")
public class Utils {

    public static class Dev {
       @Contract(mutates = "param5")
        public static void populateConfigs(boolean selectOnly, boolean countOnly, boolean programOnly, boolean customFilter,
                                           List<ConfigurazioneDTO> toPopulate, ControlloDTO controllo) {
            if (countOnly) {
                //Filter only select COUNT jobs
                toPopulate.addAll(controllo.orderedConfigsAsDTOs()
                        .stream()
                        .filter(QueryResolver::acceptCount)
                        .toList());
                return;
            }

            if (selectOnly) {
                //Filter only SELECT jobs
                toPopulate.addAll(controllo.orderedConfigsAsDTOs()
                        .stream()
                        .filter(QueryResolver::acceptSelect)
                        .toList());
                return;
            }

            if (programOnly) {
                //Filter only PROGRAM jobs
                toPopulate.addAll(controllo.orderedConfigsAsDTOs()
                        .stream()
                        .filter(c -> c.sqlScript() == null
                                && c.programma() != null
                                && c.classe() == null)
                        .toList());
                return;
            }

           if (customFilter) {
               //TODO remember to eliminate hardcoded test filtering
               //FILTER ACTUAL TEST CASES (CONFIG ID > 3)
               toPopulate.addAll(controllo.orderedConfigsAsDTOs()
                      .stream()
                      .filter(Objects::nonNull)
                      .filter(c -> c.id() == 4 || c.id() == 5 || c.id() == 6)
                      .filter(QuartzUtils::validateAndLog)
                      .toList());
               return;
           }
           //NO FILTER
            toPopulate.addAll(controllo.orderedConfigsAsDTOs()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(QuartzUtils::validateAndLog)
                    .toList());
        }
    }

    public static class Strings {

        @Contract("null -> fail")
        public static @NotNull String capitalize(String s) {
            if (s == null) throw new IllegalArgumentException("String to capitalize cannot be null");
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }

        public static @Nullable List<String> extrapolateCCs(String ccs) {
            if (ccs == null || ccs.isBlank()) return null;
            if (ccs.contains(";")) {
                String[] ccsFromString = ccs.trim().split(";");
                return Arrays.stream(ccsFromString).toList();
            }
            return Collections.singletonList(ccs);
        }
    }

    public static class Math {

        public static long calculateMB(long from) {
            return from / (long) (java.lang.Math.pow(1024, 2));
        }

        public static boolean lesserThan(String a, String b) {
            try {
                final var valueA = Integer.parseInt(a);
                final var valueB = Integer.parseInt(b);
                return valueA < valueB;
            } catch (ClassCastException e) {
                //Se il parsing delle stringhe tira un'eccezione probabilmente non sono semplici numeri
                log.debug("Valori a confronto con \"<\": {}, {}", a, b);
                if (TimeUtils.timeStampLessThan(a, b)) return true;
                return TimeUtils.dateLessThan(a, b);
            }
        }

        public static boolean greaterThan(String a, String b) {
            try {
                final var valueA = Integer.parseInt(a);
                final var valueB = Integer.parseInt(b);
                return valueA > valueB;
            } catch (ClassCastException e) {
                log.debug("Valori a confronto con \">\": {}, {}", a, b);
                if (TimeUtils.timeStampGreaterThan(a, b)) return true;
                return TimeUtils.dateGreaterThan(a, b);
            }
        }
    }

    public static boolean differ(String a, String b) {
        try {
            Float num1 = Float.parseFloat(a);
            Float num2 = Float.parseFloat(b);
            return !num1.equals(num2);
        } catch (NumberFormatException e) {
            try {
                LocalDate date1 = LocalDate.parse(a);
                LocalDate date2 = LocalDate.parse(b);
                return !date1.isEqual(date2);
            } catch (Exception ex) {
                try {
                    LocalDateTime ts1 = LocalDateTime.parse(a);
                    LocalDateTime ts2 = LocalDateTime.parse(b);
                    return !ts1.isEqual(ts2);
                } catch (Exception exc) {
                    return !Objects.equals(a, b);
                }
            }
        }
    }
}
