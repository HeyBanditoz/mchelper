package io.banditoz.mchelper.database.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.banditoz.mchelper.database.Database;
import io.jenetics.facilejdbc.ResultSetParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Dao {
    protected final Database database;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    public Dao(Database database) {
        this.database = database;
    }

    /**
     * Take a {@link ResultSet} with at least one row, and parse it into a {@link List list.}
     *
     * @param rs The {@link ResultSet} to use to build the elements.
     * @param c  The underlying database {@link Connection connection.}
     * @param parser The {@link ResultSetParser} to get a single element from the {@link ResultSet ResultSet.}
     * @return A {@link List} of T if they're in the {@link ResultSet ResultSet,} empty {@link List list} otherwise.
     * @param <T> The type of what we're trying to parse.
     */
    protected <T> List<T> parseMany(ResultSet rs, Connection c, ResultSetParser<T> parser) {
        List<T> elements = new ArrayList<>();
        try {
            while (!rs.isLast()) {
                T t = parser.parse(rs, c);
                if (t != null) {
                    elements.add(t);
                }
                // this branch will cover an empty ResultSet, I believe rs.isLast() should return true, but doesn't in the
                // case of an empty ResultSet, may be a library bug, will need to investigate further.
                else {
                    break;
                }
            }
        } catch (Exception ex) {
            log.error("Exception while trying to parse many elements! Returning empty list instead.", ex);
            return Collections.emptyList();
        }
        return elements.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(elements);
    }
}
