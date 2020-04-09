package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class Dao {
    public abstract String getSqlTableGenerator();
    protected final Logger LOGGER = LoggerFactory.getLogger(Dao.class);

    public void generateTable() {
        try (Connection c = Database.getConnection()) {
            c.prepareStatement(getSqlTableGenerator()).execute();
        } catch (SQLException e) {
            LOGGER.error("Failed to create table!", e);
        }
    }
}
