package it.dmi.processors;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
public class ResultsProcessor {

    private static final Integer NO_DATA = -1;

    public static @NotNull Map<String, List<String>> processSelectResultSet(@Nullable ResultSet set)
            throws SQLException {

        Map<String, List<String>> results = new HashMap<>();
        log.debug("Reading select result resultSet...");
        if (set == null) {
            log.error("Tried to process a null Select Result set");
            return results;
        }
        int columnCount = set.getMetaData().getColumnCount();
        while (set.next()) {
            for (int i = 1; i <= columnCount; i++) {
                String columnName = set.getMetaData().getColumnName(i);
                String columnValue = set.getString(i);
                if (results.containsKey(columnName)) {
                    results.get(columnName).add(columnValue);
                } else {
                    List<String> list = new ArrayList<>();
                    list.add(columnValue);
                    results.put(columnName, list);
                }
            }
        }
        return results;
    }

    public static Integer processCountResultSet(@Nullable ResultSet set) throws SQLException {
        int result = NO_DATA;
        if (set == null) {
            log.error("Tried to process a null Count Result set");
            return result;
        }
        log.debug("Reading count result resultSet..");
        if (set.next()) result = set.getInt(1);
        return result;
    }

    public static Integer processIUDResultObj(Object result) {
        int rowsAffected = NO_DATA;
        if (result instanceof Integer i)
            rowsAffected = i;
        return rowsAffected;
    }
}
