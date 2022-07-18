package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Dao {
    protected final Database DATABASE;
    protected final Logger LOGGER = LoggerFactory.getLogger(Dao.class);

    public Dao(Database database) {
        this.DATABASE = database;
    }
}
