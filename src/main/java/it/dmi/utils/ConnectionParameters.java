package it.dmi.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public record ConnectionParameters(
        Connection connection,
        PreparedStatement statement,
        Object result) implements AutoCloseable {

    @Override
    public void close() throws SQLException {
        if (result instanceof ResultSet resultSet)
            if (!resultSet.isClosed()) resultSet.close();
        if (statement != null && !statement.isClosed()) statement.close();
        if (connection != null && !connection.isClosed()) connection.close();
    }
}
