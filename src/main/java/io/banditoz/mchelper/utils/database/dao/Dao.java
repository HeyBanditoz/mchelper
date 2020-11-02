package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public abstract class Dao {
    public abstract String getSqlTableGenerator();
    protected final Database DATABASE;
    protected final Logger LOGGER = LoggerFactory.getLogger(Dao.class);

    public Dao(Database database) {
        this.DATABASE = database;
    }

    public void generateTable() {
        String[] list = getSqlTableGenerator().split("; ");
        try (Connection c = DATABASE.getConnection()) {
            for (String s : list) {
                c.prepareStatement(s).execute();
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to create table!", e);
        }
    }
}
